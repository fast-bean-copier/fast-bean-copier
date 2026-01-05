package com.github.jackieonway.copier.processor.extractor;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.converter.TypeConverter;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.model.CopyFieldConfig;

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
}
