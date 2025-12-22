package com.github.jackieonway.copier.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterizedTypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成器。
 *
 * 使用 JavaPoet 库生成 Copier 类的代码，
 * 包括 toDto、fromDto、toDtoList、toDtoSet 等方法。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public final class CodeGenerator {

    /**
     * 处理环境，用于访问编译时的各种信息。
     */
    private final ProcessingEnvironment processingEnv;

    /**
     * 源类型元素。
     */
    private final TypeElement sourceType;

    /**
     * 目标类型元素。
     */
    private final TypeElement targetType;

    /**
     * 字段映射列表。
     */
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /**
     * 构造方法。
     *
     * @param processingEnv 处理环境
     * @param sourceType    源类型元素
     * @param targetType    目标类型元素
     */
    public CodeGenerator(ProcessingEnvironment processingEnv, TypeElement sourceType, TypeElement targetType) {
        this.processingEnv = processingEnv;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 设置字段映射列表。
     *
     * @param fieldMappings 字段映射列表
     */
    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    /**
     * 生成 Copier 类。
     *
     * 生成的类名为 {TargetClassName}Copier，
     * 包含 toDto、fromDto、toDtoList、toDtoSet 等方法。
     */
    public void generateCopierClass() {
        try {
            // 获取目标类的简单名称
            String targetClassName = targetType.getSimpleName().toString();
            String copierClassName = targetClassName + "Copier";
            
            // 获取包名
            String packageName = getPackageName(targetType);
            
            // 创建 toDto 方法
            MethodSpec toDtoMethod = generateToDto();
            
            // 创建 fromDto 方法
            MethodSpec fromDtoMethod = generateFromDto();
            
            // 创建集合方法
            MethodSpec toDtoListMethod = generateToDtoList();
            MethodSpec toDtoSetMethod = generateToDtoSet();
            MethodSpec fromDtoListMethod = generateFromDtoList();
            MethodSpec fromDtoSetMethod = generateFromDtoSet();
            
            // 创建 Copier 类
            TypeSpec copierClass = TypeSpec.classBuilder(copierClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(createPrivateConstructor())
                    .addMethod(toDtoMethod)
                    .addMethod(fromDtoMethod)
                    .addMethod(toDtoListMethod)
                    .addMethod(toDtoSetMethod)
                    .addMethod(fromDtoListMethod)
                    .addMethod(fromDtoSetMethod)
                    .build();
            
            // 生成 Java 文件
            JavaFile javaFile = JavaFile.builder(packageName, copierClass)
                    .build();
            
            // 写入文件
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException("生成 Copier 类失败", e);
        }
    }

    /**
     * 生成 toDto 方法。
     *
     * 该方法将源对象拷贝到目标对象。
     * 方法签名：public static TargetType toDto(SourceType source)
     */
    private MethodSpec generateToDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(targetType))
                .addParameter(ClassName.get(sourceType), "source");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建目标对象
        methodBuilder.addStatement("$T target = new $T()", ClassName.get(targetType), ClassName.get(targetType));
        
        // 生成字段拷贝代码
        for (FieldMapping mapping : fieldMappings) {
            generateFieldCopyCode(methodBuilder, mapping, false);
        }
        
        // 返回目标对象
        methodBuilder.addStatement("return target");
        
        return methodBuilder.build();
    }

    /**
     * 生成 fromDto 方法。
     *
     * 该方法将目标对象拷贝回源对象（反向拷贝）。
     * 方法签名：public static SourceType fromDto(TargetType source)
     */
    private MethodSpec generateFromDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(sourceType))
                .addParameter(ClassName.get(targetType), "source");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建源对象
        methodBuilder.addStatement("$T target = new $T()", ClassName.get(sourceType), ClassName.get(sourceType));
        
        // 生成反向字段拷贝代码
        for (FieldMapping mapping : fieldMappings) {
            generateFieldCopyCode(methodBuilder, mapping, true);
        }
        
        // 返回源对象
        methodBuilder.addStatement("return target");
        
        return methodBuilder.build();
    }

    /**
     * 生成 toDtoList 方法。
     *
     * 该方法将源对象列表拷贝到目标对象列表。
     * 方法签名：public static List&lt;TargetType&gt; toDtoList(List&lt;SourceType&gt; sources)
     *
     * @return MethodSpec 对象
     */
    public MethodSpec generateToDtoList() {
        // 使用 List<SourceType> / List<TargetType> 的强类型声明，避免原始类型和强制类型转换
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources");

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果列表：List<TargetType> result = new ArrayList<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfTarget,
                ClassName.get(java.util.ArrayList.class));

        // 遍历源列表并拷贝（无 Object，无强转）
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName)
                .addStatement("result.add(toDto(source))")
                .endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 toDtoSet 方法。
     *
     * 该方法将源对象集合拷贝到目标对象集合。
     * 方法签名：public static Set&lt;TargetType&gt; toDtoSet(Set&lt;SourceType&gt; sources)
     *
     * @return MethodSpec 对象
     */
    public MethodSpec generateToDtoSet() {
        // 使用 Set<SourceType> / Set<TargetType> 的强类型声明
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources");

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果集合：Set<TargetType> result = new LinkedHashSet<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfTarget,
                ClassName.get(java.util.LinkedHashSet.class));

        // 遍历源集合并拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName)
                .addStatement("result.add(toDto(source))")
                .endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoList 方法。
     *
     * 该方法将目标对象列表拷贝回源对象列表（反向拷贝）。
     * 方法签名：public static List&lt;SourceType&gt; fromDtoList(List&lt;TargetType&gt; sources)
     *
     * @return MethodSpec 对象
     */
    public MethodSpec generateFromDtoList() {
        // List<SourceType> 和 List<TargetType> 的强类型声明（fromDto 方向 source/target 角色反转）
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources");

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果列表：List<SourceType> result = new ArrayList<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfSource,
                ClassName.get(java.util.ArrayList.class));

        // 遍历源列表并反向拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName)
                .addStatement("result.add(fromDto(source))")
                .endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoSet 方法。
     *
     * 该方法将目标对象集合拷贝回源对象集合（反向拷贝）。
     * 方法签名：public static Set&lt;SourceType&gt; fromDtoSet(Set&lt;TargetType&gt; sources)
     *
     * @return MethodSpec 对象
     */
    public MethodSpec generateFromDtoSet() {
        // Set<SourceType> 和 Set<TargetType> 的强类型声明（fromDto 方向 source/target 角色反转）
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources");

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果集合：Set<SourceType> result = new LinkedHashSet<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfSource,
                ClassName.get(java.util.LinkedHashSet.class));

        // 遍历源集合并反向拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName)
                .addStatement("result.add(fromDto(source))")
                .endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 创建私有构造方法，防止实例化。
     *
     * @return 私有构造方法
     */
    private MethodSpec createPrivateConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("throw new $T(\"No instances of $L\")", AssertionError.class, 
                    targetType.getSimpleName().toString() + "Copier")
                .build();
    }

    /**
     * 获取类型元素的包名。
     *
     * @param typeElement 类型元素
     * @return 包名
     */
    private String getPackageName(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot > 0) {
            return qualifiedName.substring(0, lastDot);
        }
        return "";
    }

    /**
     * 将字符串的首字母大写。
     *
     * @param str 要转换的字符串
     * @return 首字母大写后的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 判断是否需要类型转换。
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果需要转换，返回 true；否则返回 false
     */
    private boolean needsTypeConversion(javax.lang.model.type.TypeMirror sourceType, javax.lang.model.type.TypeMirror targetType) {
        // 如果类型完全相同，不需要转换
        if (sourceType.toString().equals(targetType.toString())) {
            return false;
        }
        
        // 检查是否为基本类型和包装类型的转换
        return (TypeUtils.isPrimitive(sourceType) && TypeUtils.isWrapper(targetType)) ||
               (TypeUtils.isWrapper(sourceType) && TypeUtils.isPrimitive(targetType));
    }

    /**
     * 生成类型转换代码。
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @param valueCode  值的代码表示
     * @return 转换后的代码
     */
    private String generateConversionCode(javax.lang.model.type.TypeMirror sourceType, 
                                         javax.lang.model.type.TypeMirror targetType, 
                                         String valueCode) {
        // 基本类型 -> 包装类型（自动装箱）
        if (TypeUtils.isPrimitive(sourceType) && TypeUtils.isWrapper(targetType)) {
            return valueCode;
        }
        
        // 包装类型 -> 基本类型（自动拆箱 + null 处理）
        if (TypeUtils.isWrapper(sourceType) && TypeUtils.isPrimitive(targetType)) {
            String defaultValue = TypeUtils.getDefaultValue(targetType);
            return valueCode + " != null ? " + valueCode + " : " + defaultValue;
        }
        
        return valueCode;
    }

    /**
     * 生成字段拷贝代码，支持集合深拷贝的扩展。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝（fromDto）
     */
    private void generateFieldCopyCode(MethodSpec.Builder methodBuilder, FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);

        javax.lang.model.type.TypeMirror sourceFieldType = reverse ? mapping.getTargetType() : mapping.getSourceType();
        javax.lang.model.type.TypeMirror targetFieldType = reverse ? mapping.getSourceType() : mapping.getTargetType();

        if (TypeUtils.isList(sourceFieldType) && TypeUtils.isList(targetFieldType)) {
            generateListDeepCopyCode(methodBuilder, getterName, setterName, sourceFieldType, targetFieldType, mapping, reverse);
            return;
        }

        if (TypeUtils.isSet(sourceFieldType) && TypeUtils.isSet(targetFieldType)) {
            generateSetDeepCopyCode(methodBuilder, getterName, setterName, sourceFieldType, targetFieldType, mapping, reverse);
            return;
        }

        if (TypeUtils.isArrayType(sourceFieldType) && TypeUtils.isArrayType(targetFieldType)) {
            generateArrayDeepCopyCode(methodBuilder, getterName, setterName, sourceFieldType, targetFieldType, mapping, reverse);
            return;
        }

        if (TypeUtils.isMap(sourceFieldType) && TypeUtils.isMap(targetFieldType)) {
            generateMapDeepCopyCode(methodBuilder, getterName, setterName, sourceFieldType, targetFieldType, mapping, reverse);
            return;
        }

        if (needsTypeConversion(sourceFieldType, targetFieldType)) {
            String conversionCode = generateConversionCode(sourceFieldType, targetFieldType, "source." + getterName + "()");
            methodBuilder.addStatement("target.$L($L)", setterName, conversionCode);
            return;
        }

        methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
    }

    /**
     * 生成 List 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     */
    private void generateListDeepCopyCode(MethodSpec.Builder methodBuilder,
                                          String getterName,
                                          String setterName,
                                          javax.lang.model.type.TypeMirror sourceFieldType,
                                          javax.lang.model.type.TypeMirror targetFieldType,
                                          FieldMapping mapping,
                                          boolean reverse) {
        java.util.List<javax.lang.model.type.TypeMirror> sourceArgs = TypeUtils.extractTypeArguments(sourceFieldType);
        java.util.List<javax.lang.model.type.TypeMirror> targetArgs = TypeUtils.extractTypeArguments(targetFieldType);
        java.util.List<javax.lang.model.type.TypeMirror> dtoArgs = TypeUtils.extractTypeArguments(mapping.getTargetType());

        javax.lang.model.type.TypeMirror sourceElementType = sourceArgs.isEmpty() ? null : sourceArgs.get(0);
        javax.lang.model.type.TypeMirror targetElementType = targetArgs.isEmpty() ? null : targetArgs.get(0);
        javax.lang.model.type.TypeMirror dtoElementType = dtoArgs.isEmpty() ? null : dtoArgs.get(0);

        // List 循环元素类型：优先使用源元素类型，其次目标元素类型，最后退回 Object
        TypeName loopElementType = sourceElementType != null
                ? TypeName.get(sourceElementType)
                : (targetElementType != null ? TypeName.get(targetElementType) : TypeName.get(Object.class));

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceList = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetList = new java.util.ArrayList(sourceList.size())", TypeName.get(targetFieldType))
                .beginControlFlow("for ($T item : sourceList)", loopElementType);

        // 一层元素：基本类型 / 对象 / DTO 拷贝
        if (sourceElementType != null && TypeUtils.needsDeepCopy(sourceElementType) && dtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoElementType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            // 集合元素为对象类型时，需要对 null 元素安全处理，避免 NPE
            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("targetList.add($T.$L(item))", copierClass, methodName)
                    .nextControlFlow("else")
                    .addStatement("targetList.add(null)")
                    .endControlFlow();
        } else if (targetElementType != null && TypeUtils.needsDeepCopy(targetElementType) && dtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoElementType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("targetList.add($T.$L(item))", copierClass, methodName)
                    .nextControlFlow("else")
                    .addStatement("targetList.add(null)")
                    .endControlFlow();
        }
        // 嵌套 List：例如 List<List<User>> / List<Map<K, V>>
        else if (sourceElementType != null && TypeUtils.isList(sourceElementType)) {
            // List<List<...>> 或 List<Map<...>>，此处我们在生成代码时使用强类型 List
            TypeName nestedSourceListType = TypeName.get(sourceElementType);
            TypeName nestedTargetListType = targetElementType != null ? TypeName.get(targetElementType) : nestedSourceListType;

            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("$T nestedSource = item", nestedSourceListType)
                    .addStatement("$T nestedTarget = new java.util.ArrayList(nestedSource.size())", nestedTargetListType);

            java.util.List<javax.lang.model.type.TypeMirror> nestedSourceArgs = TypeUtils.extractTypeArguments(sourceElementType);
            java.util.List<javax.lang.model.type.TypeMirror> nestedTargetArgs = targetElementType != null
                    ? TypeUtils.extractTypeArguments(targetElementType) : java.util.Collections.emptyList();
            java.util.List<javax.lang.model.type.TypeMirror> nestedDtoArgs = dtoElementType != null
                    ? TypeUtils.extractTypeArguments(dtoElementType) : java.util.Collections.emptyList();

            javax.lang.model.type.TypeMirror nestedSourceElementType = nestedSourceArgs.isEmpty() ? null : nestedSourceArgs.get(0);
            javax.lang.model.type.TypeMirror nestedTargetElementType = nestedTargetArgs.isEmpty() ? null : nestedTargetArgs.get(0);
            javax.lang.model.type.TypeMirror nestedDtoElementType = nestedDtoArgs.isEmpty() ? null : nestedDtoArgs.get(0);

            // 计算内部循环元素类型（尽量用具体泛型，而不是 Object）
            javax.lang.model.type.TypeMirror loopNestedMirror =
                    nestedSourceElementType != null ? nestedSourceElementType :
                            (nestedTargetElementType != null ? nestedTargetElementType : null);
            TypeName loopNestedType = loopNestedMirror != null
                    ? TypeName.get(loopNestedMirror)
                    : TypeName.get(Object.class);

            methodBuilder.beginControlFlow("for ($T nestedItem : nestedSource)", loopNestedType);

            if (nestedSourceElementType != null && TypeUtils.needsDeepCopy(nestedSourceElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else if (nestedTargetElementType != null && TypeUtils.needsDeepCopy(nestedTargetElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else {
                methodBuilder.addStatement("nestedTarget.add(nestedItem)");
            }

            methodBuilder.endControlFlow()
                    .addStatement("targetList.add(nestedTarget)")
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("targetList.add(null)")
                    .endControlFlow();
        }
        // 嵌套 Map：例如 List<Map<K, V>>
        else if (sourceElementType != null && TypeUtils.isMap(sourceElementType)) {
            TypeName nestedSourceMapType = TypeName.get(sourceElementType);
            TypeName nestedTargetMapType = targetElementType != null ? TypeName.get(targetElementType) : nestedSourceMapType;

            javax.lang.model.type.TypeMirror nestedSourceKeyType = TypeUtils.extractMapKeyType(sourceElementType);
            javax.lang.model.type.TypeMirror nestedTargetKeyType = targetElementType != null
                    ? TypeUtils.extractMapKeyType(targetElementType) : null;

            javax.lang.model.type.TypeMirror nestedSourceValueType = TypeUtils.extractMapValueType(sourceElementType);
            javax.lang.model.type.TypeMirror nestedTargetValueType = targetElementType != null
                    ? TypeUtils.extractMapValueType(targetElementType) : null;
            javax.lang.model.type.TypeMirror nestedDtoValueType = dtoElementType != null
                    ? TypeUtils.extractMapValueType(dtoElementType) : null;

            // key 的循环类型
            javax.lang.model.type.TypeMirror nestedLoopKeyMirror =
                    nestedSourceKeyType != null ? nestedSourceKeyType :
                            (nestedTargetKeyType != null ? nestedTargetKeyType : null);
            TypeName nestedKeyTypeName = nestedLoopKeyMirror != null
                    ? TypeName.get(nestedLoopKeyMirror)
                    : TypeName.get(Object.class);

            // value 的循环类型
            javax.lang.model.type.TypeMirror nestedLoopValueMirror =
                    nestedSourceValueType != null ? nestedSourceValueType :
                            (nestedTargetValueType != null ? nestedTargetValueType : null);
            TypeName nestedValueTypeName = nestedLoopValueMirror != null
                    ? TypeName.get(nestedLoopValueMirror)
                    : TypeName.get(Object.class);

            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("$T nestedSource = item", nestedSourceMapType)
                    .addStatement("$T nestedTarget = new java.util.HashMap(nestedSource.size())", nestedTargetMapType)
                    .beginControlFlow("for (java.util.Map.Entry<$T, $T> nestedEntry : nestedSource.entrySet())", nestedKeyTypeName, nestedValueTypeName)
                    .addStatement("$T nestedKey = nestedEntry.getKey()", nestedKeyTypeName)
                    .addStatement("$T nestedValue = nestedEntry.getValue()", nestedValueTypeName)
                    .beginControlFlow("if (nestedValue != null)");

            if (nestedSourceValueType != null && TypeUtils.needsDeepCopy(nestedSourceValueType) && nestedDtoValueType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.put(nestedKey, $T.$L(nestedValue))", copierClass, methodName);
            } else if (nestedTargetValueType != null && TypeUtils.needsDeepCopy(nestedTargetValueType) && nestedDtoValueType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.put(nestedKey, $T.$L(nestedValue))", copierClass, methodName);
            } else {
                methodBuilder.addStatement("nestedTarget.put(nestedKey, nestedValue)");
            }

            methodBuilder.endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("nestedTarget.put(nestedKey, null)")
                    .endControlFlow()
                    .endControlFlow()
                    .addStatement("targetList.add(nestedTarget)")
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("targetList.add(null)")
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("targetList.add(item)");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetList)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }

    /**
     * 生成 Set 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     */
    private void generateSetDeepCopyCode(MethodSpec.Builder methodBuilder,
                                         String getterName,
                                         String setterName,
                                         javax.lang.model.type.TypeMirror sourceFieldType,
                                         javax.lang.model.type.TypeMirror targetFieldType,
                                         FieldMapping mapping,
                                         boolean reverse) {
        java.util.List<javax.lang.model.type.TypeMirror> sourceArgs = TypeUtils.extractTypeArguments(sourceFieldType);
        java.util.List<javax.lang.model.type.TypeMirror> targetArgs = TypeUtils.extractTypeArguments(targetFieldType);
        java.util.List<javax.lang.model.type.TypeMirror> dtoArgs = TypeUtils.extractTypeArguments(mapping.getTargetType());

        javax.lang.model.type.TypeMirror sourceElementType = sourceArgs.isEmpty() ? null : sourceArgs.get(0);
        javax.lang.model.type.TypeMirror targetElementType = targetArgs.isEmpty() ? null : targetArgs.get(0);
        javax.lang.model.type.TypeMirror dtoElementType = dtoArgs.isEmpty() ? null : dtoArgs.get(0);

        // Set 循环元素类型：优先使用源元素类型，其次目标元素类型，最后退回 Object
        TypeName loopElementType = sourceElementType != null
                ? TypeName.get(sourceElementType)
                : (targetElementType != null ? TypeName.get(targetElementType) : TypeName.get(Object.class));

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceSet = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetSet = new java.util.LinkedHashSet(sourceSet.size())", TypeName.get(targetFieldType))
                .beginControlFlow("for ($T item : sourceSet)", loopElementType);

        if (sourceElementType != null && TypeUtils.needsDeepCopy(sourceElementType) && dtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoElementType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            // Set 元素为对象类型时，对 null 元素做安全处理
            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("targetSet.add($T.$L(item))", copierClass, methodName)
                    .nextControlFlow("else")
                    .addStatement("targetSet.add(null)")
                    .endControlFlow();
        } else if (targetElementType != null && TypeUtils.needsDeepCopy(targetElementType) && dtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoElementType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("targetSet.add($T.$L(item))", copierClass, methodName)
                    .nextControlFlow("else")
                    .addStatement("targetSet.add(null)")
                    .endControlFlow();
        } else if (sourceElementType != null && TypeUtils.isList(sourceElementType)) {
            // Set<List<T>> 场景：对内部 List 做深拷贝，使用强类型声明
            TypeName nestedSourceListType = TypeName.get(sourceElementType);
            TypeName nestedTargetListType = targetElementType != null ? TypeName.get(targetElementType) : nestedSourceListType;

            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("$T nestedSource = item", nestedSourceListType)
                    .addStatement("$T nestedTarget = new java.util.ArrayList(nestedSource.size())", nestedTargetListType);

            java.util.List<javax.lang.model.type.TypeMirror> nestedSourceArgs = TypeUtils.extractTypeArguments(sourceElementType);
            java.util.List<javax.lang.model.type.TypeMirror> nestedTargetArgs = targetElementType != null
                    ? TypeUtils.extractTypeArguments(targetElementType) : java.util.Collections.emptyList();
            java.util.List<javax.lang.model.type.TypeMirror> nestedDtoArgs = dtoElementType != null
                    ? TypeUtils.extractTypeArguments(dtoElementType) : java.util.Collections.emptyList();

            javax.lang.model.type.TypeMirror nestedSourceElementType = nestedSourceArgs.isEmpty() ? null : nestedSourceArgs.get(0);
            javax.lang.model.type.TypeMirror nestedTargetElementType = nestedTargetArgs.isEmpty() ? null : nestedTargetArgs.get(0);
            javax.lang.model.type.TypeMirror nestedDtoElementType = nestedDtoArgs.isEmpty() ? null : nestedDtoArgs.get(0);

            javax.lang.model.type.TypeMirror loopNestedMirror =
                    nestedSourceElementType != null ? nestedSourceElementType :
                            (nestedTargetElementType != null ? nestedTargetElementType : null);
            TypeName loopNestedType = loopNestedMirror != null
                    ? TypeName.get(loopNestedMirror)
                    : TypeName.get(Object.class);

            methodBuilder.beginControlFlow("for ($T nestedItem : nestedSource)", loopNestedType);

            if (nestedSourceElementType != null && TypeUtils.needsDeepCopy(nestedSourceElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else if (nestedTargetElementType != null && TypeUtils.needsDeepCopy(nestedTargetElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else {
                methodBuilder.addStatement("nestedTarget.add(nestedItem)");
            }

            methodBuilder.endControlFlow()
                    .addStatement("targetSet.add(nestedTarget)")
                    .endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("targetSet.add(null)")
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("targetSet.add(item)");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetSet)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }

    /**
     * 生成数组字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     */
    private void generateArrayDeepCopyCode(MethodSpec.Builder methodBuilder,
                                           String getterName,
                                           String setterName,
                                           javax.lang.model.type.TypeMirror sourceFieldType,
                                           javax.lang.model.type.TypeMirror targetFieldType,
                                           FieldMapping mapping,
                                           boolean reverse) {
        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceArray = source.$L()", TypeName.get(sourceFieldType), getterName);

        javax.lang.model.type.TypeMirror targetComponentType = TypeUtils.getArrayComponentType(targetFieldType);
        javax.lang.model.type.TypeMirror sourceComponentType = TypeUtils.getArrayComponentType(sourceFieldType);
        javax.lang.model.type.TypeMirror dtoComponentType = TypeUtils.getArrayComponentType(mapping.getTargetType());

        methodBuilder.addStatement("$T targetArray = new $T[sourceArray.length]",
                TypeName.get(targetFieldType),
                TypeName.get(targetComponentType));

        methodBuilder.beginControlFlow("for (int i = 0; i < sourceArray.length; i++)");

        if (sourceComponentType != null && TypeUtils.needsDeepCopy(sourceComponentType) && dtoComponentType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoComponentType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            // 先取出强类型元素，再交给 Copier，避免在生成代码中出现强制类型转换，并保证 null 元素安全
            methodBuilder.addStatement("$T element = sourceArray[i]", TypeName.get(sourceComponentType))
                    .beginControlFlow("if (element != null)")
                    .addStatement("targetArray[i] = $T.$L(element)", copierClass, methodName)
                    .nextControlFlow("else")
                    .addStatement("targetArray[i] = null")
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("targetArray[i] = sourceArray[i]");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetArray)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }

    /**
     * 生成 Map 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     */
    private void generateMapDeepCopyCode(MethodSpec.Builder methodBuilder,
                                         String getterName,
                                         String setterName,
                                         javax.lang.model.type.TypeMirror sourceFieldType,
                                         javax.lang.model.type.TypeMirror targetFieldType,
                                         FieldMapping mapping,
                                         boolean reverse) {
        javax.lang.model.type.TypeMirror sourceKeyType = TypeUtils.extractMapKeyType(sourceFieldType);
        javax.lang.model.type.TypeMirror targetKeyType = TypeUtils.extractMapKeyType(targetFieldType);

        javax.lang.model.type.TypeMirror sourceValueType = TypeUtils.extractMapValueType(sourceFieldType);
        javax.lang.model.type.TypeMirror targetValueType = TypeUtils.extractMapValueType(targetFieldType);
        javax.lang.model.type.TypeMirror dtoValueType = TypeUtils.extractMapValueType(mapping.getTargetType());

        // Key 类型优先使用 source 的泛型，其次是 target，最后退回 Object
        TypeName keyTypeName;
        if (sourceKeyType != null) {
            keyTypeName = TypeName.get(sourceKeyType);
        } else if (targetKeyType != null) {
            keyTypeName = TypeName.get(targetKeyType);
        } else {
            keyTypeName = TypeName.get(Object.class);
        }

        // Value 类型优先使用 source 的泛型，其次是 target，最后退回 Object
        javax.lang.model.type.TypeMirror loopValueMirror =
                sourceValueType != null ? sourceValueType :
                        (targetValueType != null ? targetValueType : null);
        TypeName loopValueTypeName = loopValueMirror != null
                ? TypeName.get(loopValueMirror)
                : TypeName.get(Object.class);

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceMap = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetMap = new java.util.HashMap(sourceMap.size())", TypeName.get(targetFieldType))
                // 使用带泛型的 Map.Entry<K, V>，避免 Object + 强制类型转换
                .beginControlFlow("for (java.util.Map.Entry<$T, $T> entry : sourceMap.entrySet())", keyTypeName, loopValueTypeName)
                .addStatement("$T key = entry.getKey()", keyTypeName)
                .addStatement("$T value = entry.getValue()", loopValueTypeName)
                .beginControlFlow("if (value != null)");

        if (sourceValueType != null && TypeUtils.needsDeepCopy(sourceValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.addStatement("targetMap.put(key, $T.$L(value))", copierClass, methodName);
        } else if (targetValueType != null && TypeUtils.needsDeepCopy(targetValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.addStatement("targetMap.put(key, $T.$L(value))", copierClass, methodName);
        } else if (sourceValueType != null && TypeUtils.isList(sourceValueType)) {
            // Map<K, List<V>> 场景：对 Value 中的 List 做深拷贝，生成代码中不出现强制类型转换
            TypeName nestedSourceListType = TypeName.get(sourceValueType);
            TypeName nestedTargetListType = targetValueType != null ? TypeName.get(targetValueType) : nestedSourceListType;

            methodBuilder.addStatement("$T nestedSource = value", nestedSourceListType)
                    .addStatement("$T nestedTarget = new java.util.ArrayList(nestedSource.size())", nestedTargetListType);

            java.util.List<javax.lang.model.type.TypeMirror> nestedSourceArgs = TypeUtils.extractTypeArguments(sourceValueType);
            java.util.List<javax.lang.model.type.TypeMirror> nestedTargetArgs = targetValueType != null
                    ? TypeUtils.extractTypeArguments(targetValueType) : java.util.Collections.emptyList();
            java.util.List<javax.lang.model.type.TypeMirror> nestedDtoArgs = dtoValueType != null
                    ? TypeUtils.extractTypeArguments(dtoValueType) : java.util.Collections.emptyList();

            javax.lang.model.type.TypeMirror nestedSourceElementType = nestedSourceArgs.isEmpty() ? null : nestedSourceArgs.get(0);
            javax.lang.model.type.TypeMirror nestedTargetElementType = nestedTargetArgs.isEmpty() ? null : nestedTargetArgs.get(0);
            javax.lang.model.type.TypeMirror nestedDtoElementType = nestedDtoArgs.isEmpty() ? null : nestedDtoArgs.get(0);

            javax.lang.model.type.TypeMirror loopNestedMirror =
                    nestedSourceElementType != null ? nestedSourceElementType :
                            (nestedTargetElementType != null ? nestedTargetElementType : null);
            TypeName loopNestedType = loopNestedMirror != null
                    ? TypeName.get(loopNestedMirror)
                    : TypeName.get(Object.class);

            methodBuilder.beginControlFlow("for ($T nestedItem : nestedSource)", loopNestedType);

            if (nestedSourceElementType != null && TypeUtils.needsDeepCopy(nestedSourceElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else if (nestedTargetElementType != null && TypeUtils.needsDeepCopy(nestedTargetElementType) && nestedDtoElementType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem))", copierClass, methodName);
            } else {
                methodBuilder.addStatement("nestedTarget.add(nestedItem)");
            }

            methodBuilder.endControlFlow()
                    .addStatement("targetMap.put(key, nestedTarget)");
        } else {
            // 普通 Map<K, V> 场景：直接使用强类型 value，避免 Object -> V 的不安全强转
            methodBuilder.addStatement("targetMap.put(key, value)");
        }

        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("targetMap.put(key, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("target.$L(targetMap)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }
}
