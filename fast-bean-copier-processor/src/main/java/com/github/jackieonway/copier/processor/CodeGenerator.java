package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.NullValueStrategy;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.generator.BasicMethodGenerator;
import com.github.jackieonway.copier.processor.generator.ClassStructureGenerator;
import com.github.jackieonway.copier.processor.generator.CollectionMethodGenerator;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 代码生成器（协调者）。
 *
 * <p>使用 JavaPoet 库生成 Copier 类的代码。
 * 该类作为协调者，将具体的代码生成逻辑委托给各个专门的生成器：
 * <ul>
 *   <li>{@link ClassStructureGenerator} - 类结构生成（注解、字段、构造器）</li>
 *   <li>{@link BasicMethodGenerator} - 基础方法生成（toDto、fromDto）</li>
 *   <li>{@link CollectionMethodGenerator} - 集合方法生成（List、Set、Map、Array）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.0.0
 */
public final class CodeGenerator {

    /** 处理环境 */
    private final ProcessingEnvironment processingEnv;

    /** 处理器上下文 */
    private final ProcessorContext context;

    /** 源类型元素 */
    private final TypeElement sourceType;

    /** 目标类型元素 */
    private final TypeElement targetType;

    /** 字段映射列表 */
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /** uses 类列表 */
    private List<TypeMirror> usesClasses = new ArrayList<>();

    /** 组件模型 */
    private ComponentModel componentModel = ComponentModel.DEFAULT;

    /** null 值处理策略 */
    private NullValueStrategy nullValueStrategy = NullValueStrategy.IGNORE;

    /** 映射前处理方法名 */
    private String beforeMapping = "";

    /** 需要的转换器类名集合 */
    private Set<String> requiredConverters = new HashSet<>();

    // ========== 生成器组件 ==========
    private final ClassStructureGenerator classStructureGenerator;
    private final BasicMethodGenerator basicMethodGenerator;
    private final CollectionMethodGenerator collectionMethodGenerator;

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

        // 创建处理器上下文
        this.context = new ProcessorContext(processingEnv);
        this.context.setSourceType(sourceType);
        this.context.setTargetType(targetType);

        // 初始化生成器组件
        this.classStructureGenerator = new ClassStructureGenerator(context);
        this.basicMethodGenerator = new BasicMethodGenerator(context);
        this.collectionMethodGenerator = new CollectionMethodGenerator(context);
    }

    /**
     * 设置字段映射列表。
     *
     * @param fieldMappings 字段映射列表
     */
    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
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
        this.context.setComponentModel(this.componentModel);
    }

    /**
     * 设置 null 值处理策略。
     *
     * @param nullValueStrategy null 值处理策略
     * @since 1.3.0
     */
    public void setNullValueStrategy(NullValueStrategy nullValueStrategy) {
        this.nullValueStrategy = nullValueStrategy != null ? nullValueStrategy : NullValueStrategy.IGNORE;
        this.context.setNullValueStrategy(this.nullValueStrategy);
    }

    /**
     * 设置映射前处理方法名。
     *
     * @param beforeMapping 映射前处理方法名
     * @since 1.3.0
     */
    public void setBeforeMapping(String beforeMapping) {
        this.beforeMapping = beforeMapping != null ? beforeMapping : "";
        this.context.setBeforeMapping(this.beforeMapping);
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
     * 判断是否使用静态方法。
     */
    private boolean useStaticMethods() {
        return componentModel == ComponentModel.DEFAULT;
    }

    /**
     * 生成 Copier 类。
     *
     * <p>生成的类名为 {TargetClassName}Copier，
     * 包含 toDto、fromDto、toDtoList、toDtoSet 等方法。
     */
    public void generateCopierClass() {
        try {
            String targetClassName = targetType.getSimpleName().toString();
            String copierClassName = targetClassName + "Copier";
            String packageName = getPackageName(targetType);

            // 创建类构建器
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(copierClassName)
                    .addModifiers(Modifier.PUBLIC);

            // 配置生成器
            configureGenerators();

            // 1. 生成类结构（注解、字段、构造器）
            classStructureGenerator.addClassAnnotations(classBuilder);
            classStructureGenerator.addConverterFields(classBuilder, requiredConverters);
            classStructureGenerator.addUsesFields(classBuilder, usesClasses);
            classStructureGenerator.addConstructors(classBuilder, requiredConverters, usesClasses);

            // 2. 生成基础方法（toDto、fromDto 及其 customizer 重载）
            classBuilder.addMethod(basicMethodGenerator.generateToDto());
            classBuilder.addMethod(basicMethodGenerator.generateFromDto());
            classBuilder.addMethod(basicMethodGenerator.generateToDtoWithCustomizer());
            classBuilder.addMethod(basicMethodGenerator.generateFromDtoWithCustomizer());

            // 2.1 生成更新方法（v1.3 新增）
            classBuilder.addMethod(basicMethodGenerator.generateUpdateDto());
            classBuilder.addMethod(basicMethodGenerator.generateUpdateEntity());

            // 3. 生成集合方法（List、Set、Map、Array）
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoList());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoList());
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoSet());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoSet());
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoMap());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoMap());
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoArray());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoArray());

            // 4. 生成带 customizer 的集合方法
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoListWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoListWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoSetWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoSetWithCustomizer());

            // 4.1 生成带 customizer 的 Map/Array 方法（v1.3.1 新增）
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoMapWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoMapWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateToDtoArrayWithCustomizer());
            classBuilder.addMethod(collectionMethodGenerator.generateFromDtoArrayWithCustomizer());

            // 生成 Java 文件并写入
            JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException("生成 Copier 类失败", e);
        }
    }

    /**
     * 配置各个生成器组件。
     */
    private void configureGenerators() {
        boolean useStatic = useStaticMethods();

        // 配置 BasicMethodGenerator
        basicMethodGenerator.setSourceType(sourceType);
        basicMethodGenerator.setTargetType(targetType);
        basicMethodGenerator.setFieldMappings(fieldMappings);
        basicMethodGenerator.setUseStaticMethods(useStatic);
        basicMethodGenerator.setUsesClasses(usesClasses);
        basicMethodGenerator.setNullValueStrategy(nullValueStrategy);
        basicMethodGenerator.setBeforeMapping(beforeMapping);

        // 配置 CollectionMethodGenerator
        collectionMethodGenerator.setSourceType(sourceType);
        collectionMethodGenerator.setTargetType(targetType);
        collectionMethodGenerator.setUseStaticMethods(useStatic);
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
        return lastDot > 0 ? qualifiedName.substring(0, lastDot) : "";
    }
}
