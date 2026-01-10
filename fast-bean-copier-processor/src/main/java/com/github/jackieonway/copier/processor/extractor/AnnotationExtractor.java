package com.github.jackieonway.copier.processor.extractor;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CopyTargetConfig;
import com.github.jackieonway.copier.annotation.NullValueStrategy;
import com.github.jackieonway.copier.converter.TypeConverter;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.model.CopyFieldConfig;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 注解提取器，负责从注解中提取配置信息。
 *
 * <p>该类封装了从 {@link CopyTarget} 和 {@link CopyField} 注解中
 * 提取配置信息的逻辑，包括处理 {@link MirroredTypeException} 等
 * 注解处理器特有的异常。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class AnnotationExtractor {

    /**
     * 处理器上下文。
     */
    private final ProcessorContext context;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public AnnotationExtractor(ProcessorContext context) {
        this.context = context;
    }

    /**
     * 从 @CopyTarget 注解中提取源类型。
     *
     * <p>由于注解处理器的特性，直接访问 Class 类型的注解属性会抛出
     * {@link MirroredTypeException}，需要通过捕获该异常来获取类型信息。
     *
     * @param targetType 目标类型元素
     * @param annotation CopyTarget 注解
     * @return 源类型元素，如果无法获取则返回 null
     */
    public TypeElement extractSourceType(TypeElement targetType, CopyTarget annotation) {
        try {
            // 尝试直接获取 source 属性，这会抛出 MirroredTypeException
            annotation.source();
        } catch (MirroredTypeException e) {
            // 从异常中获取 TypeMirror
            TypeMirror sourceTypeMirror = e.getTypeMirror();
            if (sourceTypeMirror != null) {
                return (TypeElement) context.getTypeUtils().asElement(sourceTypeMirror);
            }
        }
        return null;
    }

    /**
     * 从 @CopyTarget 注解中提取忽略字段列表。
     *
     * @param annotation CopyTarget 注解
     * @return 忽略的字段名集合
     */
    public Set<String> extractIgnoreFields(CopyTarget annotation) {
        Set<String> ignoreFields = new HashSet<>();
        String[] ignoreArray = annotation.ignore();
        if (ignoreArray != null) {
            for (String fieldName : ignoreArray) {
                if (fieldName != null && !fieldName.trim().isEmpty()) {
                    ignoreFields.add(fieldName);
                }
            }
        }
        return ignoreFields;
    }

    /**
     * 从 @CopyTarget 注解中提取 uses 类列表。
     *
     * <p>由于注解处理器的特性，直接访问 Class[] 类型的注解属性会抛出
     * {@link MirroredTypesException}，需要通过捕获该异常来获取类型信息。
     *
     * @param annotation CopyTarget 注解
     * @return uses 类的 TypeMirror 列表
     */
    public List<TypeMirror> extractUsesClasses(CopyTarget annotation) {
        List<TypeMirror> usesClasses = new ArrayList<>();
        try {
            Class<?>[] uses = annotation.uses();
            // 如果能直接获取，说明类在编译路径中
            for (Class<?> clazz : uses) {
                TypeElement element = context.getElementUtils().getTypeElement(clazz.getCanonicalName());
                if (element != null) {
                    usesClasses.add(element.asType());
                }
            }
        } catch (MirroredTypesException e) {
            // 从异常中获取 TypeMirror 列表
            usesClasses.addAll(e.getTypeMirrors());
        }
        return usesClasses;
    }

    /**
     * 从 @CopyTarget 注解中提取 componentModel 配置。
     *
     * @param annotation CopyTarget 注解
     * @return 组件模型配置
     */
    public ComponentModel extractComponentModel(CopyTarget annotation) {
        ComponentModel componentModel = annotation.componentModel();
        return componentModel != null ? componentModel : ComponentModel.DEFAULT;
    }

    /**
     * 从 @CopyField 注解中提取字段映射配置。
     *
     * @param annotation CopyField 注解
     * @return 字段映射配置对象
     */
    public CopyFieldConfig extractCopyFieldConfig(CopyField annotation) {
        if (annotation == null) {
            return null;
        }

        String[] sourceNames = annotation.source();
        String target = annotation.target();
        String expression = annotation.expression();
        String qualifiedByName = annotation.qualifiedByName();
        String format = annotation.format();
        String converterClassName = extractConverterClassName(annotation);

        return new CopyFieldConfig(
                sourceNames,
                target,
                expression,
                qualifiedByName,
                format,
                converterClassName
        );
    }

    /**
     * 从 @CopyField 注解中提取转换器类名。
     *
     * <p>由于注解处理器的特性，直接访问 Class 类型的注解属性会抛出
     * {@link MirroredTypeException}，需要通过捕获该异常来获取类型信息。
     *
     * @param annotation CopyField 注解
     * @return 转换器类的完全限定名，如果未指定或为默认值则返回 null
     */
    public String extractConverterClassName(CopyField annotation) {
        try {
            Class<? extends TypeConverter<?, ?>> converterClass = annotation.converter();
            String className = converterClass.getCanonicalName();
            // 检查是否为默认值（TypeConverter.None）
            if (isDefaultConverter(className)) {
                return null;
            }
            return className;
        } catch (MirroredTypeException e) {
            String className = e.getTypeMirror().toString();
            // 检查是否为默认值（TypeConverter.None）
            if (isDefaultConverter(className)) {
                return null;
            }
            return className;
        }
    }

    /**
     * 检查转换器类名是否为默认值（TypeConverter.None）。
     *
     * @param className 转换器类名
     * @return 如果是默认值返回 true
     */
    private boolean isDefaultConverter(String className) {
        return className == null
                || className.isEmpty()
                || className.endsWith("TypeConverter$None")
                || className.endsWith("TypeConverter.None");
    }

    /**
     * 验证 @CopyTarget 注解配置是否有效。
     *
     * @param targetType 目标类型元素
     * @param annotation CopyTarget 注解
     * @return 如果配置有效返回 true
     */
    public boolean validateCopyTargetAnnotation(TypeElement targetType, CopyTarget annotation) {
        // 检查源类型是否可以获取
        TypeElement sourceType = extractSourceType(targetType, annotation);
        if (sourceType == null) {
            context.error("无法获取源类型，请检查 @CopyTarget 注解的 source 属性", targetType);
            return false;
        }
        return true;
    }

    /**
     * 验证 @CopyField 注解配置是否有效。
     *
     * @param config 字段映射配置
     * @return 如果配置有效返回 true
     */
    public boolean validateCopyFieldConfig(CopyFieldConfig config) {
        if (config == null) {
            return true; // 没有注解是有效的
        }

        // 多对一映射必须有表达式
        if (config.isManyToOne() && !config.hasExpression()) {
            return false;
        }

        return true;
    }

    /**
     * 从包元素中提取 @CopyTargetConfig 注解配置。
     *
     * <p>读取 package-info.java 中的 @CopyTargetConfig 注解，
     * 如果注解不存在则返回默认配置。
     *
     * @param packageElement 包元素
     * @return 包级别配置，如果注解不存在则返回 null
     * @since 1.3.0
     */
    public PackageConfig extractPackageConfig(PackageElement packageElement) {
        if (packageElement == null) {
            return null;
        }

        CopyTargetConfig config = packageElement.getAnnotation(CopyTargetConfig.class);
        if (config == null) {
            return null;
        }

        ComponentModel componentModel = config.componentModel();
        NullValueStrategy nullValueStrategy = config.nullValueStrategy();

        return new PackageConfig(
                componentModel != null ? componentModel : ComponentModel.DEFAULT,
                nullValueStrategy != null ? nullValueStrategy : NullValueStrategy.IGNORE
        );
    }

    /**
     * 合并类级别配置和包级别配置。
     *
     * <p>配置优先级：类级别 > 包级别 > 默认值
     *
     * @param classComponentModel 类级别组件模型配置（可能为 null）
     * @param packageConfig 包级别配置（可能为 null）
     * @return 合并后的组件模型
     * @since 1.3.0
     */
    public ComponentModel mergeComponentModel(ComponentModel classComponentModel, PackageConfig packageConfig) {
        // 类级别配置优先
        if (classComponentModel != null && classComponentModel != ComponentModel.DEFAULT) {
            return classComponentModel;
        }

        // 包级别配置次之
        if (packageConfig != null && packageConfig.getComponentModel() != null) {
            return packageConfig.getComponentModel();
        }

        // 默认值
        return ComponentModel.DEFAULT;
    }

    /**
     * 获取有效的 null 值处理策略。
     *
     * <p>配置优先级：包级别 > 默认值（IGNORE）
     *
     * @param packageConfig 包级别配置（可能为 null）
     * @return 有效的 null 值处理策略
     * @since 1.3.0
     */
    public NullValueStrategy getEffectiveNullValueStrategy(PackageConfig packageConfig) {
        if (packageConfig != null && packageConfig.getNullValueStrategy() != null) {
            return packageConfig.getNullValueStrategy();
        }
        return NullValueStrategy.IGNORE;
    }

    /**
     * 从 @CopyTarget 注解中提取 beforeMapping 方法名。
     *
     * @param annotation CopyTarget 注解
     * @return beforeMapping 方法名，如果未指定返回空字符串
     * @since 1.3.0
     */
    public String extractBeforeMapping(CopyTarget annotation) {
        if (annotation == null) {
            return "";
        }
        String beforeMapping = annotation.beforeMapping();
        return beforeMapping != null ? beforeMapping : "";
    }

    /**
     * 验证 beforeMapping 方法签名是否正确。
     *
     * <p>beforeMapping 方法必须满足以下条件：
     * <ul>
     *   <li>方法存在于目标类中</li>
     *   <li>方法参数类型为源类型</li>
     *   <li>方法返回类型为 void</li>
     * </ul>
     *
     * @param targetType    目标类型元素
     * @param sourceType    源类型元素
     * @param beforeMapping beforeMapping 方法名
     * @return 如果方法签名正确返回 true，否则返回 false
     * @since 1.3.0
     */
    public boolean validateBeforeMappingMethod(TypeElement targetType, TypeElement sourceType, String beforeMapping) {
        if (beforeMapping == null || beforeMapping.trim().isEmpty()) {
            return true; // 没有指定 beforeMapping 方法，验证通过
        }

        // 查找目标类中的方法
        for (javax.lang.model.element.Element element : targetType.getEnclosedElements()) {
            if (element.getKind() == javax.lang.model.element.ElementKind.METHOD) {
                javax.lang.model.element.ExecutableElement method = (javax.lang.model.element.ExecutableElement) element;
                if (method.getSimpleName().toString().equals(beforeMapping)) {
                    // 验证返回类型为 void
                    if (method.getReturnType().getKind() != javax.lang.model.type.TypeKind.VOID) {
                        context.error("beforeMapping 方法 '" + beforeMapping + "' 必须返回 void", targetType);
                        return false;
                    }

                    // 验证参数类型
                    java.util.List<? extends javax.lang.model.element.VariableElement> params = method.getParameters();
                    if (params.size() != 1) {
                        context.error("beforeMapping 方法 '" + beforeMapping + "' 必须有且仅有一个参数", targetType);
                        return false;
                    }

                    javax.lang.model.type.TypeMirror paramType = params.get(0).asType();
                    if (!context.getTypeUtils().isSameType(paramType, sourceType.asType())) {
                        context.error("beforeMapping 方法 '" + beforeMapping + "' 的参数类型必须为源类型 " + sourceType.getSimpleName(), targetType);
                        return false;
                    }

                    return true;
                }
            }
        }

        context.error("在目标类中找不到 beforeMapping 方法 '" + beforeMapping + "'", targetType);
        return false;
    }

    /**
     * 从 @CopyField 注解中提取条件表达式。
     *
     * @param annotation CopyField 注解
     * @return 条件表达式，如果未指定返回空字符串
     * @since 1.3.0
     */
    public String extractCondition(CopyField annotation) {
        if (annotation == null) {
            return "";
        }
        String condition = annotation.condition();
        return condition != null ? condition : "";
    }

    /**
     * 从 @CopyField 注解中提取默认值。
     *
     * @param annotation CopyField 注解
     * @return 默认值，如果未指定返回空字符串
     * @since 1.3.0
     */
    public String extractDefaultValue(CopyField annotation) {
        if (annotation == null) {
            return "";
        }
        String defaultValue = annotation.defaultValue();
        return defaultValue != null ? defaultValue : "";
    }

    /**
     * 从 @CopyField 注解中提取常量值。
     *
     * @param annotation CopyField 注解
     * @return 常量值，如果未指定返回空字符串
     * @since 1.3.0
     */
    public String extractConstant(CopyField annotation) {
        if (annotation == null) {
            return "";
        }
        String constant = annotation.constant();
        return constant != null ? constant : "";
    }

    /**
     * 包级别配置数据类。
     *
     * @since 1.3.0
     */
    public static class PackageConfig {
        private final ComponentModel componentModel;
        private final NullValueStrategy nullValueStrategy;

        public PackageConfig(ComponentModel componentModel, NullValueStrategy nullValueStrategy) {
            this.componentModel = componentModel;
            this.nullValueStrategy = nullValueStrategy;
        }

        public ComponentModel getComponentModel() {
            return componentModel;
        }

        public NullValueStrategy getNullValueStrategy() {
            return nullValueStrategy;
        }
    }
}
