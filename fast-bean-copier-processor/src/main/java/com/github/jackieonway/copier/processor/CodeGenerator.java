package com.github.jackieonway.copier.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

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
        String sourceClassName = sourceType.getSimpleName().toString();
        String targetClassName = targetType.getSimpleName().toString();
        
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
            String sourceFieldName = mapping.getSourceFieldName();
            String targetFieldName = mapping.getTargetFieldName();
            
            // 使用 getter/setter 方法
            String getterName = "get" + capitalize(sourceFieldName);
            String setterName = "set" + capitalize(targetFieldName);
            
            // 检查是否需要类型转换
            if (needsTypeConversion(mapping.getSourceType(), mapping.getTargetType())) {
                // 需要类型转换
                String conversionCode = generateConversionCode(mapping.getSourceType(), mapping.getTargetType(), "source." + getterName + "()");
                methodBuilder.addStatement("target.$L($L)", setterName, conversionCode);
            } else {
                // 直接拷贝
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
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
            String sourceFieldName = mapping.getSourceFieldName();
            String targetFieldName = mapping.getTargetFieldName();
            
            // 反向拷贝：从目标字段拷贝到源字段
            String setterName = "set" + capitalize(sourceFieldName);
            String getterName = "get" + capitalize(targetFieldName);
            
            // 检查是否需要类型转换（反向转换）
            if (needsTypeConversion(mapping.getTargetType(), mapping.getSourceType())) {
                // 需要反向类型转换
                String conversionCode = generateConversionCode(mapping.getTargetType(), mapping.getSourceType(), "source." + getterName + "()");
                methodBuilder.addStatement("target.$L($L)", setterName, conversionCode);
            } else {
                // 直接拷贝
                methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            }
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
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(java.util.List.class)
                .addParameter(java.util.List.class, "sources");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建结果列表
        methodBuilder.addStatement("java.util.List result = new java.util.ArrayList(sources.size())");
        
        // 遍历源列表并拷贝
        methodBuilder.beginControlFlow("for (Object source : sources)")
                .addStatement("result.add(toDto(($T) source))", ClassName.get(sourceType))
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
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(java.util.Set.class)
                .addParameter(java.util.Set.class, "sources");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建结果集合
        methodBuilder.addStatement("java.util.Set result = new java.util.HashSet()");
        
        // 遍历源集合并拷贝
        methodBuilder.beginControlFlow("for (Object source : sources)")
                .addStatement("result.add(toDto(($T) source))", ClassName.get(sourceType))
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
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(java.util.List.class)
                .addParameter(java.util.List.class, "sources");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建结果列表
        methodBuilder.addStatement("java.util.List result = new java.util.ArrayList(sources.size())");
        
        // 遍历源列表并反向拷贝
        methodBuilder.beginControlFlow("for (Object source : sources)")
                .addStatement("result.add(fromDto(($T) source))", ClassName.get(targetType))
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
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(java.util.Set.class)
                .addParameter(java.util.Set.class, "sources");
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建结果集合
        methodBuilder.addStatement("java.util.Set result = new java.util.HashSet()");
        
        // 遍历源集合并反向拷贝
        methodBuilder.beginControlFlow("for (Object source : sources)")
                .addStatement("result.add(fromDto(($T) source))", ClassName.get(targetType))
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
}
