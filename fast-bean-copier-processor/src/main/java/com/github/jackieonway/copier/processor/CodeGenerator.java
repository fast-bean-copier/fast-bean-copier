package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.ArrayTypeName;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.tools.Diagnostic;

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
     * 用于输出编译期提示。
     */
    private final Messager messager;

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
     * v1.2: uses 类列表。
     */
    private List<TypeMirror> usesClasses = new ArrayList<>();

    /**
     * v1.2: 组件模型。
     */
    private ComponentModel componentModel = ComponentModel.DEFAULT;

    /**
     * v1.2: 需要的转换器类名集合。
     */
    private Set<String> requiredConverters = new HashSet<>();

    /**
     * 构造方法。
     *
     * @param processingEnv 处理环境
     * @param sourceType    源类型元素
     * @param targetType    目标类型元素
     */
    public CodeGenerator(ProcessingEnvironment processingEnv, TypeElement sourceType, TypeElement targetType) {
        this.processingEnv = processingEnv;
        this.messager = processingEnv.getMessager();
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
        // 收集需要的转换器
        collectRequiredConverters();
    }

    /**
     * 设置 uses 类列表。
     *
     * @param usesClasses uses 类列表
     * @since 1.2.0
     */
    public void setUsesClasses(List<TypeMirror> usesClasses) {
        this.usesClasses = usesClasses != null ? usesClasses : new ArrayList<>();
    }

    /**
     * 设置组件模型。
     *
     * @param componentModel 组件模型
     * @since 1.2.0
     */
    public void setComponentModel(ComponentModel componentModel) {
        this.componentModel = componentModel != null ? componentModel : ComponentModel.DEFAULT;
    }

    /**
     * 收集需要的转换器类。
     */
    private void collectRequiredConverters() {
        requiredConverters.clear();
        for (FieldMapping mapping : fieldMappings) {
            if (mapping.hasConverter()) {
                requiredConverters.add(mapping.getConverterClassName());
            }
        }
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
            
            // 创建类构建器
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(copierClassName)
                    .addModifiers(Modifier.PUBLIC);
            
            // 根据 ComponentModel 添加类注解和修饰符
            addClassAnnotationsAndModifiers(classBuilder);
            
            // 添加转换器字段（如果需要）
            addConverterFields(classBuilder);
            
            // 添加 uses 类字段（如果需要）
            addUsesFields(classBuilder);
            
            // 添加构造器
            addConstructors(classBuilder);
            
            // 创建 toDto 方法
            MethodSpec toDtoMethod = generateToDto();
            classBuilder.addMethod(toDtoMethod);
            
            // 创建 fromDto 方法
            MethodSpec fromDtoMethod = generateFromDto();
            classBuilder.addMethod(fromDtoMethod);
            
            // 创建集合方法
            classBuilder.addMethod(generateToDtoList());
            classBuilder.addMethod(generateToDtoSet());
            classBuilder.addMethod(generateToDtoMap());
            classBuilder.addMethod(generateToDtoArray());
            classBuilder.addMethod(generateFromDtoList());
            classBuilder.addMethod(generateFromDtoSet());
            classBuilder.addMethod(generateFromDtoMap());
            classBuilder.addMethod(generateFromDtoArray());
            
            // v1.2: 添加函数式重载方法
            classBuilder.addMethod(generateToDtoWithCustomizer());
            classBuilder.addMethod(generateFromDtoWithCustomizer());
            classBuilder.addMethod(generateToDtoListWithCustomizer());
            classBuilder.addMethod(generateToDtoSetWithCustomizer());
            classBuilder.addMethod(generateFromDtoListWithCustomizer());
            classBuilder.addMethod(generateFromDtoSetWithCustomizer());
            
            // 生成 Java 文件
            JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                    .build();
            
            // 写入文件
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException("生成 Copier 类失败", e);
        }
    }

    /**
     * 根据 ComponentModel 添加类注解和修饰符。
     */
    private void addClassAnnotationsAndModifiers(TypeSpec.Builder classBuilder) {
        switch (componentModel) {
            case SPRING:
                classBuilder.addAnnotation(ClassName.get("org.springframework.stereotype", "Component"));
                break;
            case CDI:
                classBuilder.addAnnotation(ClassName.get("javax.enterprise.context", "ApplicationScoped"));
                break;
            case JSR330:
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Named"));
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Singleton"));
                break;
            case DEFAULT:
            default:
                classBuilder.addModifiers(Modifier.FINAL);
                break;
        }
    }

    /**
     * 添加转换器字段。
     */
    private void addConverterFields(TypeSpec.Builder classBuilder) {
        for (String converterClassName : requiredConverters) {
            ClassName converterType = ClassName.bestGuess(converterClassName);
            String fieldName = getConverterFieldName(converterClassName);
            
            if (componentModel == ComponentModel.DEFAULT) {
                // 静态字段
                classBuilder.addField(FieldSpec.builder(converterType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", converterType)
                        .build());
            } else {
                // 实例字段
                classBuilder.addField(FieldSpec.builder(converterType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());
            }
        }
    }

    /**
     * 添加 uses 类字段。
     */
    private void addUsesFields(TypeSpec.Builder classBuilder) {
        for (TypeMirror usesClass : usesClasses) {
            ClassName usesType = ClassName.bestGuess(usesClass.toString());
            String fieldName = getUsesFieldName(usesClass.toString());
            
            if (componentModel == ComponentModel.DEFAULT) {
                // 静态字段
                classBuilder.addField(FieldSpec.builder(usesType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T()", usesType)
                        .build());
            } else {
                // 实例字段
                classBuilder.addField(FieldSpec.builder(usesType, fieldName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());
            }
        }
    }

    /**
     * 添加构造器。
     */
    private void addConstructors(TypeSpec.Builder classBuilder) {
        if (componentModel == ComponentModel.DEFAULT) {
            // 私有构造器，防止实例化
            classBuilder.addMethod(createPrivateConstructor());
        } else {
            // 无参构造器（使用默认实例）
            MethodSpec.Builder noArgConstructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            
            // 初始化转换器字段
            for (String converterClassName : requiredConverters) {
                ClassName converterType = ClassName.bestGuess(converterClassName);
                String fieldName = getConverterFieldName(converterClassName);
                noArgConstructor.addStatement("this.$L = new $T()", fieldName, converterType);
            }
            
            // 初始化 uses 字段
            for (TypeMirror usesClass : usesClasses) {
                ClassName usesType = ClassName.bestGuess(usesClass.toString());
                String fieldName = getUsesFieldName(usesClass.toString());
                noArgConstructor.addStatement("this.$L = new $T()", fieldName, usesType);
            }
            
            classBuilder.addMethod(noArgConstructor.build());
            
            // 如果有需要注入的依赖，添加带参数的构造器
            if (!requiredConverters.isEmpty() || !usesClasses.isEmpty()) {
                MethodSpec.Builder injectionConstructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC);
                
                // 添加 @Inject 注解（如果不是 SPRING）
                if (componentModel != ComponentModel.SPRING) {
                    injectionConstructor.addAnnotation(ClassName.get("javax.inject", "Inject"));
                } else {
                    injectionConstructor.addAnnotation(ClassName.get("org.springframework.beans.factory.annotation", "Autowired"));
                }
                
                // 添加转换器参数
                for (String converterClassName : requiredConverters) {
                    ClassName converterType = ClassName.bestGuess(converterClassName);
                    String fieldName = getConverterFieldName(converterClassName);
                    injectionConstructor.addParameter(converterType, fieldName);
                    injectionConstructor.addStatement("this.$L = $L", fieldName, fieldName);
                }
                
                // 添加 uses 参数
                for (TypeMirror usesClass : usesClasses) {
                    ClassName usesType = ClassName.bestGuess(usesClass.toString());
                    String fieldName = getUsesFieldName(usesClass.toString());
                    injectionConstructor.addParameter(usesType, fieldName);
                    injectionConstructor.addStatement("this.$L = $L", fieldName, fieldName);
                }
                
                classBuilder.addMethod(injectionConstructor.build());
            }
        }
    }

    /**
     * 获取转换器字段名。
     */
    private String getConverterFieldName(String converterClassName) {
        String simpleName = converterClassName.substring(converterClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 获取 uses 字段名。
     */
    private String getUsesFieldName(String usesClassName) {
        String simpleName = usesClassName.substring(usesClassName.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }

    /**
     * 判断是否使用静态方法。
     */
    private boolean useStaticMethods() {
        return componentModel == ComponentModel.DEFAULT;
    }

    /**
     * 生成 toDto 方法。
     *
     * 该方法将源对象拷贝到目标对象。
     * 方法签名：public static TargetType toDto(SourceType source)
     */
    private MethodSpec generateToDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(targetType))
                .addParameter(ClassName.get(sourceType), "source");
        
        // 根据 ComponentModel 添加 static 修饰符
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
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
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(sourceType))
                .addParameter(ClassName.get(targetType), "source");
        
        // 根据 ComponentModel 添加 static 修饰符
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
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

    // ========== v1.2: 函数式重载方法 ==========

    /**
     * 生成带 customizer 的 toDto 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateToDtoWithCustomizer() {
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetTypeName)
                .addParameter(ClassName.get(sourceType), "source")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("$T result = toDto(source)", targetTypeName);
        } else {
            methodBuilder.addStatement("$T result = this.toDto(source)", targetTypeName);
        }
        
        methodBuilder.beginControlFlow("if (customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDto 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateFromDtoWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceTypeName)
                .addParameter(ClassName.get(targetType), "source")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("$T result = fromDto(source)", sourceTypeName);
        } else {
            methodBuilder.addStatement("$T result = this.fromDto(source)", sourceTypeName);
        }
        
        methodBuilder.beginControlFlow("if (customizer != null)")
                .addStatement("result = customizer.apply(result)")
                .endControlFlow()
                .addStatement("return result");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 toDtoList 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateToDtoListWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfTarget, ClassName.get(java.util.ArrayList.class));
        
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(toDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source, customizer))");
        }
        methodBuilder.endControlFlow();
        
        methodBuilder.addStatement("return result");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 toDtoSet 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateToDtoSetWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfTarget, ClassName.get(java.util.LinkedHashSet.class));
        
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(toDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source, customizer))");
        }
        methodBuilder.endControlFlow();
        
        methodBuilder.addStatement("return result");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoList 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateFromDtoListWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName listOfSource = ParameterizedTypeName.get(ClassName.get(java.util.List.class), sourceTypeName);
        TypeName listOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.List.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfSource, ClassName.get(java.util.ArrayList.class));
        
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(fromDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source, customizer))");
        }
        methodBuilder.endControlFlow();
        
        methodBuilder.addStatement("return result");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 fromDtoSet 方法。
     *
     * @since 1.2.0
     */
    private MethodSpec generateFromDtoSetWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName setOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), sourceTypeName);
        TypeName setOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Set.class), targetTypeName);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();
        
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfSource, ClassName.get(java.util.LinkedHashSet.class));
        
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(fromDto(source, customizer))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source, customizer))");
        }
        methodBuilder.endControlFlow();
        
        methodBuilder.addStatement("return result");
        
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
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfTarget)
                .addParameter(listOfSource, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果列表：List<TargetType> result = new ArrayList<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfTarget,
                ClassName.get(java.util.ArrayList.class));

        // 遍历源列表并拷贝（无 Object，无强转）
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(toDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source))");
        }
        methodBuilder.endControlFlow();

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
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfTarget)
                .addParameter(setOfSource, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果集合：Set<TargetType> result = new LinkedHashSet<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfTarget,
                ClassName.get(java.util.LinkedHashSet.class));

        // 遍历源集合并拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", sourceTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(toDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.toDto(source))");
        }
        methodBuilder.endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 toDtoMap 方法。
     *
     * 该方法将 {@code Map<K, SourceType>} 拷贝为 {@code Map<K, TargetType>}。
     *
     * @return 生成的 {@code Map<K, TargetType>} 方法规范
     */
    public MethodSpec generateToDtoMap() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName mapImpl = ParameterizedTypeName.get(ClassName.get(java.util.LinkedHashMap.class), keyType, targetTypeName);
        TypeName entryType = ParameterizedTypeName.get(ClassName.get(java.util.Map.Entry.class), keyType, sourceTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfTarget)
                .addParameter(mapOfSource, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T($L)", mapOfTarget, mapImpl, buildInitialCapacity("sources.size()"))
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.put(key, toDto(entry.getValue()))");
        } else {
            methodBuilder.addStatement("result.put(key, this.toDto(entry.getValue()))");
        }
        
        methodBuilder.nextControlFlow("else")
                .addStatement("result.put(key, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 toDtoArray 方法。
     *
     * 该方法将 {@code SourceType[]} 拷贝为 {@code TargetType[]}。
     *
     * @return 生成的 {@code TargetType[]} 方法规范
     */
    public MethodSpec generateToDtoArray() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName sourceElementType = ClassName.get(sourceType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetArrayType)
                .addParameter(sourceArrayType, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T[sources.length]", targetArrayType, ClassName.get(targetType))
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", sourceElementType)
                .beginControlFlow("if (element != null)");
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("result[i] = toDto(element)");
        } else {
            methodBuilder.addStatement("result[i] = this.toDto(element)");
        }
        
        methodBuilder.nextControlFlow("else")
                .addStatement("result[i] = null")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

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
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfSource)
                .addParameter(listOfTarget, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果列表：List<SourceType> result = new ArrayList<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                listOfSource,
                ClassName.get(java.util.ArrayList.class));

        // 遍历源列表并反向拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
        }
        methodBuilder.endControlFlow();

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
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfSource)
                .addParameter(setOfTarget, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        // 添加 null 检查
        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        // 创建结果集合：Set<SourceType> result = new LinkedHashSet<>(sources.size());
        methodBuilder.addStatement("$T result = new $T<>(sources.size())",
                setOfSource,
                ClassName.get(java.util.LinkedHashSet.class));

        // 遍历源集合并反向拷贝
        methodBuilder.beginControlFlow("for ($T source : sources)", targetTypeName);
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.add(fromDto(source))");
        } else {
            methodBuilder.addStatement("result.add(this.fromDto(source))");
        }
        methodBuilder.endControlFlow();

        // 返回结果
        methodBuilder.addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoMap 方法。
     *
     * 该方法将 {@code Map<K, TargetType>} 拷贝回 {@code Map<K, SourceType>}。
     *
     * @return 生成的 {@code Map<K, SourceType>} 方法规范
     */
    public MethodSpec generateFromDtoMap() {
        TypeVariableName keyType = TypeVariableName.get("K");
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName mapOfSource = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, sourceTypeName);
        TypeName mapOfTarget = ParameterizedTypeName.get(ClassName.get(java.util.Map.class), keyType, targetTypeName);
        TypeName mapImpl = ParameterizedTypeName.get(ClassName.get(java.util.LinkedHashMap.class), keyType, sourceTypeName);
        TypeName entryType = ParameterizedTypeName.get(ClassName.get(java.util.Map.Entry.class), keyType, targetTypeName);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoMap")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(keyType)
                .returns(mapOfSource)
                .addParameter(mapOfTarget, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T($L)", mapOfSource, mapImpl, buildInitialCapacity("sources.size()"))
                .beginControlFlow("for ($T entry : sources.entrySet())", entryType)
                .addStatement("$T key = entry.getKey()", keyType)
                .beginControlFlow("if (entry.getValue() != null)");
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("result.put(key, fromDto(entry.getValue()))");
        } else {
            methodBuilder.addStatement("result.put(key, this.fromDto(entry.getValue()))");
        }
        
        methodBuilder.nextControlFlow("else")
                .addStatement("result.put(key, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        return methodBuilder.build();
    }

    /**
     * 生成 fromDtoArray 方法。
     *
     * 该方法将 {@code TargetType[]} 拷贝回 {@code SourceType[]}。
     *
     * @return 生成的 {@code SourceType[]} 方法规范
     */
    public MethodSpec generateFromDtoArray() {
        TypeName sourceArrayType = ArrayTypeName.of(ClassName.get(sourceType));
        TypeName targetArrayType = ArrayTypeName.of(ClassName.get(targetType));
        TypeName targetElementType = ClassName.get(targetType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDtoArray")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceArrayType)
                .addParameter(targetArrayType, "sources");

        if (useStaticMethods()) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }

        methodBuilder.beginControlFlow("if (sources == null)")
                .addStatement("return null")
                .endControlFlow();

        methodBuilder.addStatement("$T result = new $T[sources.length]", sourceArrayType, ClassName.get(sourceType))
                .beginControlFlow("for (int i = 0; i < sources.length; i++)")
                .addStatement("$T element = sources[i]", targetElementType)
                .beginControlFlow("if (element != null)");
        
        if (useStaticMethods()) {
            methodBuilder.addStatement("result[i] = fromDto(element)");
        } else {
            methodBuilder.addStatement("result[i] = this.fromDto(element)");
        }
        
        methodBuilder.nextControlFlow("else")
                .addStatement("result[i] = null")
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

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
     * 在存在原始类型或不受支持的通配符时，不生成深拷贝代码，直接回退为普通赋值。
     */
    private boolean hasUnsupportedGenerics(TypeMirror typeMirror) {
        return TypeUtils.isCollectionType(typeMirror) &&
                (TypeUtils.isRawType(typeMirror) || TypeUtils.hasUnboundedWildcard(typeMirror));
    }

    private void warnUnsupportedGenerics(FieldMapping mapping, TypeMirror sourceFieldType, TypeMirror targetFieldType) {
        messager.printMessage(Diagnostic.Kind.WARNING,
                "集合字段使用了原始类型或不受支持的通配符，已跳过深拷贝生成。请为字段添加明确的泛型参数。source="
                        + sourceFieldType + ", target=" + targetFieldType,
                mapping.getTargetField());
    }

    private TypeName safeTypeName(TypeMirror typeMirror) {
        if (typeMirror == null) {
            return TypeName.get(Object.class);
        }
        if (typeMirror instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) typeMirror;
            TypeMirror bound = wildcardType.getExtendsBound() != null
                    ? wildcardType.getExtendsBound()
                    : wildcardType.getSuperBound();
            return bound != null ? TypeName.get(bound) : TypeName.get(Object.class);
        }
        return TypeName.get(typeMirror);
    }

    private TypeMirror getTypeArgument(TypeMirror typeMirror, int index) {
        if (!(typeMirror instanceof javax.lang.model.type.DeclaredType)) {
            return null;
        }
        java.util.List<? extends TypeMirror> args = ((javax.lang.model.type.DeclaredType) typeMirror).getTypeArguments();
        if (args == null || args.size() <= index) {
            return null;
        }
        return args.get(index);
    }

    /**
     * 计算集合或 Map 的初始容量，减少扩容带来的开销。
     */
    private String buildInitialCapacity(String sizeExpression) {
        return "Math.max((int)(" + sizeExpression + " / 0.75f) + 1, 16)";
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
        // v1.2: 根据映射类型生成不同的代码
        switch (mapping.getMappingType()) {
            case EXPRESSION:
                generateExpressionFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case CONVERTER:
                generateConverterFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case QUALIFIED_BY_NAME:
                generateQualifiedByNameFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case MANY_TO_ONE:
                generateExpressionFieldCopyCode(methodBuilder, mapping, reverse);
                return;
            case SIMPLE:
            default:
                // 继续使用原有逻辑
                break;
        }
        
        // 原有的简单映射逻辑
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        if (sourceFieldName == null) {
            // 没有源字段，跳过
            return;
        }

        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);

        javax.lang.model.type.TypeMirror sourceFieldType = reverse ? mapping.getTargetType() : mapping.getSourceType();
        javax.lang.model.type.TypeMirror targetFieldType = reverse ? mapping.getSourceType() : mapping.getTargetType();

        if (hasUnsupportedGenerics(sourceFieldType) || hasUnsupportedGenerics(targetFieldType)) {
            warnUnsupportedGenerics(mapping, sourceFieldType, targetFieldType);
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
            return;
        }

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
     * 生成表达式字段拷贝代码。
     *
     * @since 1.2.0
     */
    private void generateExpressionFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                  FieldMapping mapping, boolean reverse) {
        String targetFieldName = mapping.getTargetFieldName();
        String setterName = "set" + capitalize(targetFieldName);
        String expression = mapping.getExpression();
        
        if (expression == null || expression.trim().isEmpty()) {
            return;
        }
        
        // 表达式只在正向拷贝时使用，反向拷贝时跳过（因为表达式通常是单向的）
        if (reverse) {
            // 反向拷贝时，尝试使用简单映射
            if (mapping.getSourceFieldNames() != null && mapping.getSourceFieldNames().size() == 1) {
                String sourceFieldName = mapping.getSourceFieldNames().get(0);
                String getterName = "get" + capitalize(sourceFieldName);
                methodBuilder.addStatement("target.$L(source.$L())", "set" + capitalize(sourceFieldName), 
                        "get" + capitalize(targetFieldName));
            }
            return;
        }
        
        // 生成表达式代码
        methodBuilder.addStatement("target.$L($L)", setterName, expression);
    }

    /**
     * 生成转换器字段拷贝代码。
     *
     * @since 1.2.0
     */
    private void generateConverterFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                 FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();
        
        if (sourceFieldName == null) {
            return;
        }
        
        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);
        String converterFieldName = getConverterFieldName(mapping.getConverterClassName());
        String format = mapping.getFormat() != null ? mapping.getFormat() : "";
        
        // 生成转换器调用代码
        if (useStaticMethods()) {
            methodBuilder.addStatement("target.$L($L.convert(source.$L(), $S))", 
                    setterName, converterFieldName, getterName, format);
        } else {
            methodBuilder.addStatement("target.$L(this.$L.convert(source.$L(), $S))", 
                    setterName, converterFieldName, getterName, format);
        }
    }

    /**
     * 生成具名转换方法字段拷贝代码。
     *
     * @since 1.2.0
     */
    private void generateQualifiedByNameFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                                       FieldMapping mapping, boolean reverse) {
        // qualifiedByName 映射通常是单向的，反向拷贝时跳过
        if (reverse) {
            return;
        }
        
        String sourceFieldName = mapping.getSourceFieldName();
        String targetFieldName = mapping.getTargetFieldName();
        
        if (sourceFieldName == null) {
            return;
        }
        
        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);
        String methodName = mapping.getQualifiedByName();
        
        // 查找包含该方法的 uses 类
        String usesFieldName = findUsesFieldForMethod(methodName);
        
        if (usesFieldName != null) {
            if (useStaticMethods()) {
                methodBuilder.addStatement("target.$L($L.$L(source.$L()))", 
                        setterName, usesFieldName, methodName, getterName);
            } else {
                methodBuilder.addStatement("target.$L(this.$L.$L(source.$L()))", 
                        setterName, usesFieldName, methodName, getterName);
            }
        } else {
            // 如果找不到 uses 类，生成警告并使用简单赋值
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "找不到包含方法 '" + methodName + "' 的 uses 类");
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
        }
    }

    /**
     * 查找包含指定方法的 uses 字段名。
     */
    private String findUsesFieldForMethod(String methodName) {
        // 简化实现：返回第一个 uses 类的字段名
        // 实际应该检查方法是否存在于该类中
        if (!usesClasses.isEmpty()) {
            return getUsesFieldName(usesClasses.get(0).toString());
        }
        return null;
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
                ? safeTypeName(sourceElementType)
                : (targetElementType != null ? safeTypeName(targetElementType) : TypeName.get(Object.class));

        // 目标 List 使用具体元素类型声明，避免 extends 通配符导致 add 受限
        TypeName targetListType = targetElementType != null
                ? ParameterizedTypeName.get(ClassName.get(java.util.List.class), safeTypeName(targetElementType))
                : TypeName.get(targetFieldType);
        TypeName targetListImplType = targetElementType != null
                ? ParameterizedTypeName.get(ClassName.get(java.util.ArrayList.class), safeTypeName(targetElementType))
                : TypeName.get(targetFieldType);

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceList = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetList = new $T(sourceList.size())", targetListType, targetListImplType)
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
            TypeName nestedSourceListType = safeTypeName(sourceElementType);
            TypeName nestedTargetListType = targetElementType != null ? safeTypeName(targetElementType) : nestedSourceListType;

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
                    ? safeTypeName(loopNestedMirror)
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
            TypeName nestedSourceMapType = safeTypeName(sourceElementType);
            TypeName nestedTargetMapType = targetElementType != null ? safeTypeName(targetElementType) : nestedSourceMapType;

            javax.lang.model.type.TypeMirror nestedSourceKeyType = TypeUtils.extractMapKeyType(sourceElementType);
            javax.lang.model.type.TypeMirror nestedTargetKeyType = targetElementType != null
                    ? TypeUtils.extractMapKeyType(targetElementType) : null;

            javax.lang.model.type.TypeMirror nestedSourceValueType = TypeUtils.extractMapValueType(sourceElementType);
            javax.lang.model.type.TypeMirror nestedTargetValueType = targetElementType != null
                    ? TypeUtils.extractMapValueType(targetElementType) : null;
            javax.lang.model.type.TypeMirror nestedDtoValueType = dtoElementType != null
                    ? TypeUtils.extractMapValueType(dtoElementType) : null;
            javax.lang.model.type.TypeMirror nestedDtoKeyType = dtoElementType != null
                    ? TypeUtils.extractMapKeyType(dtoElementType) : null;

            // key 的循环类型
            javax.lang.model.type.TypeMirror nestedLoopKeyMirror =
                    nestedSourceKeyType != null ? nestedSourceKeyType :
                            (nestedTargetKeyType != null ? nestedTargetKeyType : null);
            TypeName nestedKeyTypeName = nestedLoopKeyMirror != null
                    ? safeTypeName(nestedLoopKeyMirror)
                    : TypeName.get(Object.class);

            // value 的循环类型
            javax.lang.model.type.TypeMirror nestedLoopValueMirror =
                    nestedSourceValueType != null ? nestedSourceValueType :
                            (nestedTargetValueType != null ? nestedTargetValueType : null);
            TypeName nestedValueTypeName = nestedLoopValueMirror != null
                    ? safeTypeName(nestedLoopValueMirror)
                    : TypeName.get(Object.class);

            methodBuilder.beginControlFlow("if (item != null)")
                    .addStatement("$T nestedSource = item", nestedSourceMapType)
                    .addStatement("$T nestedTarget = new java.util.HashMap($L)", nestedTargetMapType,
                            buildInitialCapacity("nestedSource.size()"))
                    .beginControlFlow("for (java.util.Map.Entry<$T, $T> nestedEntry : nestedSource.entrySet())", nestedKeyTypeName, nestedValueTypeName)
                    .addStatement("$T nestedKey = nestedEntry.getKey()", nestedKeyTypeName)
                    .addStatement("$T nestedValue = nestedEntry.getValue()", nestedValueTypeName);
            
            // 处理嵌套 Map 中 key 的深拷贝
            boolean needsNestedKeyDeepCopy = (nestedSourceKeyType != null && TypeUtils.needsDeepCopy(nestedSourceKeyType) && nestedDtoKeyType != null) ||
                    (nestedTargetKeyType != null && TypeUtils.needsDeepCopy(nestedTargetKeyType) && nestedDtoKeyType != null);
            
            if (needsNestedKeyDeepCopy && nestedDtoKeyType != null) {
                ClassName nestedKeyCopierClass = ClassName.bestGuess(nestedDtoKeyType.toString() + "Copier");
                String nestedKeyMethodName = reverse ? "fromDto" : "toDto";
                TypeName nestedTargetKeyTypeNameForCopy = nestedTargetKeyType != null ? safeTypeName(nestedTargetKeyType) : nestedKeyTypeName;
                methodBuilder.beginControlFlow("if (nestedKey != null)")
                        .addStatement("$T nestedCopiedKey = $T.$L(nestedKey)", nestedTargetKeyTypeNameForCopy, nestedKeyCopierClass, nestedKeyMethodName)
                        .nextControlFlow("else")
                        .addStatement("$T nestedCopiedKey = null", nestedTargetKeyTypeNameForCopy)
                        .endControlFlow();
            } else {
                // key 不需要深拷贝，直接使用
                methodBuilder.addStatement("$T nestedCopiedKey = nestedKey", nestedKeyTypeName);
            }
            
            methodBuilder.beginControlFlow("if (nestedValue != null)");

            if (nestedSourceValueType != null && TypeUtils.needsDeepCopy(nestedSourceValueType) && nestedDtoValueType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, $T.$L(nestedValue))", copierClass, methodName);
            } else if (nestedTargetValueType != null && TypeUtils.needsDeepCopy(nestedTargetValueType) && nestedDtoValueType != null) {
                ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
                String methodName = reverse ? "fromDto" : "toDto";
                methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, $T.$L(nestedValue))", copierClass, methodName);
            } else {
                methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, nestedValue)");
            }

            methodBuilder.endControlFlow()
                    .beginControlFlow("else")
                    .addStatement("nestedTarget.put(nestedCopiedKey, null)")
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
                ? safeTypeName(sourceElementType)
                : (targetElementType != null ? safeTypeName(targetElementType) : TypeName.get(Object.class));

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceSet = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetSet = new java.util.LinkedHashSet($L)", TypeName.get(targetFieldType),
                        buildInitialCapacity("sourceSet.size()"))
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
            TypeName nestedSourceListType = safeTypeName(sourceElementType);
            TypeName nestedTargetListType = targetElementType != null ? safeTypeName(targetElementType) : nestedSourceListType;

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
                    ? safeTypeName(loopNestedMirror)
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
        TypeMirror sourceKeyArgument = getTypeArgument(sourceFieldType, 0);
        TypeMirror sourceValueArgument = getTypeArgument(sourceFieldType, 1);
        TypeMirror targetKeyArgument = getTypeArgument(targetFieldType, 0);
        TypeMirror targetValueArgument = getTypeArgument(targetFieldType, 1);

        javax.lang.model.type.TypeMirror sourceKeyType = TypeUtils.extractMapKeyType(sourceFieldType);
        javax.lang.model.type.TypeMirror targetKeyType = TypeUtils.extractMapKeyType(targetFieldType);

        javax.lang.model.type.TypeMirror sourceValueType = TypeUtils.extractMapValueType(sourceFieldType);
        javax.lang.model.type.TypeMirror targetValueType = TypeUtils.extractMapValueType(targetFieldType);
        javax.lang.model.type.TypeMirror dtoValueType = TypeUtils.extractMapValueType(mapping.getTargetType());
        javax.lang.model.type.TypeMirror dtoKeyType = TypeUtils.extractMapKeyType(mapping.getTargetType());

        // Key 类型优先使用 source 的泛型，其次是 target，最后退回 Object
        TypeName keyTypeName;
        if (sourceKeyType != null) {
            keyTypeName = safeTypeName(sourceKeyType);
        } else if (targetKeyType != null) {
            keyTypeName = safeTypeName(targetKeyType);
        } else {
            keyTypeName = TypeName.get(Object.class);
        }

        // Value 类型优先使用 source 的泛型，其次是 target，最后退回 Object
        javax.lang.model.type.TypeMirror loopValueMirror =
                sourceValueType != null ? sourceValueType :
                        (targetValueType != null ? targetValueType : null);
        TypeName loopValueTypeName = loopValueMirror != null
                ? safeTypeName(loopValueMirror)
                : TypeName.get(Object.class);
        TypeMirror entryKeyMirror = sourceKeyArgument != null ? sourceKeyArgument : targetKeyArgument;
        TypeMirror entryValueMirror = sourceValueArgument != null ? sourceValueArgument : targetValueArgument;
        TypeName entryKeyTypeName = entryKeyMirror != null ? TypeName.get(entryKeyMirror) : keyTypeName;
        TypeName entryValueTypeName = entryValueMirror != null ? TypeName.get(entryValueMirror) : loopValueTypeName;
        TypeName targetKeyTypeName = targetKeyType != null ? safeTypeName(targetKeyType) : keyTypeName;
        TypeName targetValueTypeName = targetValueType != null ? safeTypeName(targetValueType) : loopValueTypeName;
        TypeName targetMapType = (targetKeyType != null || targetValueType != null)
                ? ParameterizedTypeName.get(ClassName.get(java.util.Map.class), targetKeyTypeName, targetValueTypeName)
                : TypeName.get(targetFieldType);
        TypeName targetMapImplType = (targetKeyType != null || targetValueType != null)
                ? ParameterizedTypeName.get(ClassName.get(java.util.HashMap.class), targetKeyTypeName, targetValueTypeName)
                : TypeName.get(targetFieldType);

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceMap = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetMap = new $T($L)", targetMapType, targetMapImplType,
                        buildInitialCapacity("sourceMap.size()"))
                // 使用带泛型的 Map.Entry<K, V>，避免 Object + 强制类型转换
                .beginControlFlow("for (java.util.Map.Entry<$T, $T> entry : sourceMap.entrySet())", entryKeyTypeName, entryValueTypeName)
                .addStatement("$T key = entry.getKey()", keyTypeName)
                .addStatement("$T value = entry.getValue()", loopValueTypeName);
        
        // 处理 key 的深拷贝
        boolean needsKeyDeepCopy = (sourceKeyType != null && TypeUtils.needsDeepCopy(sourceKeyType) && dtoKeyType != null) ||
                (targetKeyType != null && TypeUtils.needsDeepCopy(targetKeyType) && dtoKeyType != null);
        
        if (needsKeyDeepCopy && dtoKeyType != null) {
            ClassName keyCopierClass = ClassName.bestGuess(dtoKeyType.toString() + "Copier");
            String keyMethodName = reverse ? "fromDto" : "toDto";
            TypeName targetKeyTypeNameForCopy = targetKeyType != null ? safeTypeName(targetKeyType) : keyTypeName;
            methodBuilder.beginControlFlow("if (key != null)")
                    .addStatement("$T copiedKey = $T.$L(key)", targetKeyTypeNameForCopy, keyCopierClass, keyMethodName)
                    .nextControlFlow("else")
                    .addStatement("$T copiedKey = null", targetKeyTypeNameForCopy)
                    .endControlFlow();
        } else {
            // key 不需要深拷贝，直接使用
            methodBuilder.addStatement("$T copiedKey = key", keyTypeName);
        }
        
        methodBuilder.beginControlFlow("if (value != null)");

        if (sourceValueType != null && TypeUtils.needsDeepCopy(sourceValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.addStatement("targetMap.put(copiedKey, $T.$L(value))", copierClass, methodName);
        } else if (targetValueType != null && TypeUtils.needsDeepCopy(targetValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = reverse ? "fromDto" : "toDto";
            methodBuilder.addStatement("targetMap.put(copiedKey, $T.$L(value))", copierClass, methodName);
        } else if (sourceValueType != null && TypeUtils.isList(sourceValueType)) {
            // Map<K, List<V>> 场景：对 Value 中的 List 做深拷贝，生成代码中不出现强制类型转换
            TypeName nestedSourceListType = safeTypeName(sourceValueType);
            TypeName nestedTargetListType = targetValueType != null ? safeTypeName(targetValueType) : nestedSourceListType;

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
                    ? safeTypeName(loopNestedMirror)
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
                    .addStatement("targetMap.put(copiedKey, nestedTarget)");
        } else {
            // 普通 Map<K, V> 场景：直接使用强类型 value，避免 Object -> V 的不安全强转
            methodBuilder.addStatement("targetMap.put(copiedKey, value)");
        }

        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("targetMap.put(copiedKey, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("target.$L(targetMap)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }
}
