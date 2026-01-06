package com.github.jackieonway.copier.processor.generator;

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
}
