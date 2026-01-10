package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.annotation.NullValueStrategy;
import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * 基础方法生成器，负责生成 toDto、fromDto 及其带 customizer 的重载方法。
 *
 * <p>该类负责生成：
 * <ul>
 *   <li>toDto(SourceType source) - 将源对象转换为目标对象</li>
 *   <li>fromDto(TargetType source) - 将目标对象转换回源对象</li>
 *   <li>toDto(SourceType source, UnaryOperator customizer) - 带自定义处理的转换</li>
 *   <li>fromDto(TargetType source, UnaryOperator customizer) - 带自定义处理的反向转换</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class BasicMethodGenerator {

    private final ProcessorContext context;
    private final FieldCopyGenerator fieldCopyGenerator;
    
    /** 源类型元素 */
    private TypeElement sourceType;
    
    /** 目标类型元素 */
    private TypeElement targetType;
    
    /** 字段映射列表 */
    private List<FieldMapping> fieldMappings;
    
    /** 是否使用静态方法 */
    private boolean useStaticMethods = true;

    /** null 值处理策略 */
    private NullValueStrategy nullValueStrategy = NullValueStrategy.IGNORE;

    /** 映射前处理方法名 */
    private String beforeMapping = "";

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public BasicMethodGenerator(ProcessorContext context) {
        this.context = context;
        this.fieldCopyGenerator = new FieldCopyGenerator(context);
    }

    /**
     * 设置源类型。
     *
     * @param sourceType 源类型元素
     */
    public void setSourceType(TypeElement sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 设置目标类型。
     *
     * @param targetType 目标类型元素
     */
    public void setTargetType(TypeElement targetType) {
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
     * 设置是否使用静态方法。
     *
     * @param useStaticMethods 是否使用静态方法
     */
    public void setUseStaticMethods(boolean useStaticMethods) {
        this.useStaticMethods = useStaticMethods;
        this.fieldCopyGenerator.setUseStaticMethods(useStaticMethods);
    }

    /**
     * 设置 uses 类列表。
     *
     * @param usesClasses uses 类列表
     */
    public void setUsesClasses(List<TypeMirror> usesClasses) {
        this.fieldCopyGenerator.setUsesClasses(usesClasses);
    }

    /**
     * 设置 null 值处理策略。
     *
     * @param nullValueStrategy null 值处理策略
     * @since 1.3.0
     */
    public void setNullValueStrategy(NullValueStrategy nullValueStrategy) {
        this.nullValueStrategy = nullValueStrategy != null ? nullValueStrategy : NullValueStrategy.IGNORE;
    }

    /**
     * 设置映射前处理方法名。
     *
     * @param beforeMapping 映射前处理方法名
     * @since 1.3.0
     */
    public void setBeforeMapping(String beforeMapping) {
        this.beforeMapping = beforeMapping != null ? beforeMapping : "";
    }

    /**
     * 判断是否有映射前处理方法。
     *
     * @return 如果有映射前处理方法返回 true
     * @since 1.3.0
     */
    private boolean hasBeforeMapping() {
        return beforeMapping != null && !beforeMapping.trim().isEmpty();
    }

    /**
     * 生成 toDto 方法。
     *
     * <p>将源对象拷贝到目标对象。
     * 方法签名：public [static] TargetType toDto(SourceType source)
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateToDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(targetType))
                .addParameter(ClassName.get(sourceType), "source");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建目标对象
        methodBuilder.addStatement("$T target = new $T()", 
                ClassName.get(targetType), ClassName.get(targetType));
        
        // 生成映射前回调调用（v1.3 新增）
        if (hasBeforeMapping()) {
            methodBuilder.addStatement("target.$L(source)", beforeMapping);
        }
        
        // 生成字段拷贝代码
        for (FieldMapping mapping : fieldMappings) {
            fieldCopyGenerator.generateFieldCopyCode(methodBuilder, mapping, false);
        }
        
        // 返回目标对象
        methodBuilder.addStatement("return target");
        
        return methodBuilder.build();
    }

    /**
     * 生成 fromDto 方法。
     *
     * <p>将目标对象拷贝回源对象（反向拷贝）。
     * 方法签名：public [static] SourceType fromDto(TargetType source)
     *
     * @return 生成的方法规范
     */
    public MethodSpec generateFromDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(sourceType))
                .addParameter(ClassName.get(targetType), "source");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        // 创建源对象
        methodBuilder.addStatement("$T target = new $T()", 
                ClassName.get(sourceType), ClassName.get(sourceType));
        
        // 生成反向字段拷贝代码
        for (FieldMapping mapping : fieldMappings) {
            fieldCopyGenerator.generateFieldCopyCode(methodBuilder, mapping, true);
        }
        
        // 返回源对象
        methodBuilder.addStatement("return target");
        
        return methodBuilder.build();
    }

    /**
     * 生成带 customizer 的 toDto 方法。
     *
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateToDtoWithCustomizer() {
        TypeName targetTypeName = ClassName.get(targetType);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), targetTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(targetTypeName)
                .addParameter(ClassName.get(sourceType), "source")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        if (useStaticMethods) {
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
     * @return 生成的方法规范
     * @since 1.2.0
     */
    public MethodSpec generateFromDtoWithCustomizer() {
        TypeName sourceTypeName = ClassName.get(sourceType);
        TypeName customizerType = ParameterizedTypeName.get(
                ClassName.get(UnaryOperator.class), sourceTypeName);
        
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("fromDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(sourceTypeName)
                .addParameter(ClassName.get(targetType), "source")
                .addParameter(customizerType, "customizer");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        methodBuilder.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();
        
        if (useStaticMethods) {
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

    // ========== v1.3 新增方法 ==========

    /**
     * 生成 updateDto 方法。
     *
     * <p>更新已存在的目标对象，而不是创建新对象。
     * 方法签名：public [static] void updateDto(TargetType target, SourceType source)
     *
     * @return 生成的方法规范
     * @since 1.3.0
     */
    public MethodSpec generateUpdateDto() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("updateDto")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get(targetType), "target")
                .addParameter(ClassName.get(sourceType), "source");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null || target == null)")
                .addStatement("return")
                .endControlFlow();
        
        // 生成字段更新代码
        for (FieldMapping mapping : fieldMappings) {
            generateUpdateFieldCopyCode(methodBuilder, mapping, false);
        }
        
        return methodBuilder.build();
    }

    /**
     * 生成 updateEntity 方法。
     *
     * <p>更新已存在的源对象（反向更新）。
     * 方法签名：public [static] void updateEntity(SourceType target, TargetType source)
     *
     * @return 生成的方法规范
     * @since 1.3.0
     */
    public MethodSpec generateUpdateEntity() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("updateEntity")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addParameter(ClassName.get(sourceType), "target")
                .addParameter(ClassName.get(targetType), "source");
        
        if (useStaticMethods) {
            methodBuilder.addModifiers(Modifier.STATIC);
        }
        
        // 添加 null 检查
        methodBuilder.beginControlFlow("if (source == null || target == null)")
                .addStatement("return")
                .endControlFlow();
        
        // 生成反向字段更新代码
        for (FieldMapping mapping : fieldMappings) {
            generateUpdateFieldCopyCode(methodBuilder, mapping, true);
        }
        
        return methodBuilder.build();
    }

    /**
     * 生成更新字段拷贝代码。
     *
     * <p>根据 NullValueStrategy 决定是否更新 null 字段。
     *
     * @param methodBuilder 方法构建器
     * @param mapping       字段映射
     * @param reverse       是否反向拷贝
     * @since 1.3.0
     */
    private void generateUpdateFieldCopyCode(MethodSpec.Builder methodBuilder, 
                                              FieldMapping mapping, boolean reverse) {
        String sourceFieldName = reverse ? mapping.getTargetFieldName() : mapping.getSourceFieldName();
        String targetFieldName = reverse ? mapping.getSourceFieldName() : mapping.getTargetFieldName();

        if (sourceFieldName == null) {
            // 常量映射等没有源字段的情况
            if (mapping.isConstantMapping() && !reverse) {
                fieldCopyGenerator.generateFieldCopyCode(methodBuilder, mapping, reverse);
            }
            return;
        }

        String getterName = "get" + capitalize(sourceFieldName);
        String setterName = "set" + capitalize(targetFieldName);

        if (nullValueStrategy == NullValueStrategy.IGNORE) {
            // IGNORE 策略：只更新非 null 字段
            methodBuilder.beginControlFlow("if (source.$L() != null)", getterName);
            fieldCopyGenerator.generateFieldCopyCode(methodBuilder, mapping, reverse);
            methodBuilder.endControlFlow();
        } else {
            // REPLACE 策略：更新所有字段（包括 null）
            fieldCopyGenerator.generateFieldCopyCode(methodBuilder, mapping, reverse);
        }
    }

    /**
     * 首字母大写。
     *
     * @param str 原始字符串
     * @return 首字母大写的字符串
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
