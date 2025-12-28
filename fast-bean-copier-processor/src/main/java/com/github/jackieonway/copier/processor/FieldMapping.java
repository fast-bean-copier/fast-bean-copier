package com.github.jackieonway.copier.processor;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

/**
 * 字段映射关系的数据类。
 *
 * 用于表示源对象和目标对象之间的字段映射关系，
 * 包含源字段、目标字段及其类型信息。
 *
 * <p>v1.2 新增支持：
 * <ul>
 *   <li>多对一映射：多个源字段映射到一个目标字段</li>
 *   <li>一对多映射：一个源字段映射到多个目标字段</li>
 *   <li>表达式映射：使用 Java 表达式进行转换</li>
 *   <li>类型转换器：使用 TypeConverter 进行类型转换</li>
 *   <li>具名转换方法：使用自定义转换器中的具名方法</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class FieldMapping {

    /**
     * 映射类型枚举。
     *
     * @since 1.2.0
     */
    public enum MappingType {
        /** 简单的一对一映射 */
        SIMPLE,
        /** 多对一映射（多个源字段合并到一个目标字段） */
        MANY_TO_ONE,
        /** 一对多映射（一个源字段拆分到多个目标字段） */
        ONE_TO_MANY,
        /** 使用表达式的映射 */
        EXPRESSION,
        /** 使用类型转换器的映射 */
        CONVERTER,
        /** 使用具名转换方法的映射 */
        QUALIFIED_BY_NAME
    }

    /**
     * 源字段。
     */
    private final VariableElement sourceField;

    /**
     * 目标字段。
     */
    private final VariableElement targetField;

    /**
     * 源字段的类型。
     */
    private final TypeMirror sourceType;

    /**
     * 目标字段的类型。
     */
    private final TypeMirror targetType;

    // ========== v1.2 新增字段 ==========

    /**
     * 映射类型。
     *
     * @since 1.2.0
     */
    private MappingType mappingType = MappingType.SIMPLE;

    /**
     * 多对一映射时的源字段名列表。
     *
     * @since 1.2.0
     */
    private List<String> sourceFieldNames = new ArrayList<>();

    /**
     * 表达式字符串。
     *
     * @since 1.2.0
     */
    private String expression;

    /**
     * 类型转换器类名。
     *
     * @since 1.2.0
     */
    private String converterClassName;

    /**
     * 格式字符串（用于类型转换器）。
     *
     * @since 1.2.0
     */
    private String format;

    /**
     * 具名转换方法名。
     *
     * @since 1.2.0
     */
    private String qualifiedByName;

    /**
     * 构造方法。
     *
     * @param sourceField 源字段
     * @param targetField 目标字段
     * @param sourceType  源字段的类型
     * @param targetType  目标字段的类型
     */
    public FieldMapping(VariableElement sourceField, VariableElement targetField,
                        TypeMirror sourceType, TypeMirror targetType) {
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    /**
     * 获取源字段。
     *
     * @return 源字段
     */
    public VariableElement getSourceField() {
        return sourceField;
    }

    /**
     * 获取目标字段。
     *
     * @return 目标字段
     */
    public VariableElement getTargetField() {
        return targetField;
    }

    /**
     * 获取源字段的类型。
     *
     * @return 源字段的类型
     */
    public TypeMirror getSourceType() {
        return sourceType;
    }

    /**
     * 获取目标字段的类型。
     *
     * @return 目标字段的类型
     */
    public TypeMirror getTargetType() {
        return targetType;
    }

    /**
     * 获取源字段的名称。
     *
     * @return 源字段的名称
     */
    public String getSourceFieldName() {
        return sourceField != null ? sourceField.getSimpleName().toString() : null;
    }

    /**
     * 获取目标字段的名称。
     *
     * @return 目标字段的名称
     */
    public String getTargetFieldName() {
        return targetField.getSimpleName().toString();
    }

    // ========== v1.2 新增方法 ==========

    /**
     * 获取映射类型。
     *
     * @return 映射类型
     * @since 1.2.0
     */
    public MappingType getMappingType() {
        return mappingType;
    }

    /**
     * 设置映射类型。
     *
     * @param mappingType 映射类型
     * @since 1.2.0
     */
    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    /**
     * 获取多对一映射时的源字段名列表。
     *
     * @return 源字段名列表
     * @since 1.2.0
     */
    public List<String> getSourceFieldNames() {
        return sourceFieldNames;
    }

    /**
     * 设置多对一映射时的源字段名列表。
     *
     * @param sourceFieldNames 源字段名列表
     * @since 1.2.0
     */
    public void setSourceFieldNames(List<String> sourceFieldNames) {
        this.sourceFieldNames = sourceFieldNames;
    }

    /**
     * 获取表达式字符串。
     *
     * @return 表达式字符串
     * @since 1.2.0
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 设置表达式字符串。
     *
     * @param expression 表达式字符串
     * @since 1.2.0
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
     * 获取类型转换器类名。
     *
     * @return 类型转换器类名
     * @since 1.2.0
     */
    public String getConverterClassName() {
        return converterClassName;
    }

    /**
     * 设置类型转换器类名。
     *
     * @param converterClassName 类型转换器类名
     * @since 1.2.0
     */
    public void setConverterClassName(String converterClassName) {
        this.converterClassName = converterClassName;
    }

    /**
     * 获取格式字符串。
     *
     * @return 格式字符串
     * @since 1.2.0
     */
    public String getFormat() {
        return format;
    }

    /**
     * 设置格式字符串。
     *
     * @param format 格式字符串
     * @since 1.2.0
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * 获取具名转换方法名。
     *
     * @return 具名转换方法名
     * @since 1.2.0
     */
    public String getQualifiedByName() {
        return qualifiedByName;
    }

    /**
     * 设置具名转换方法名。
     *
     * @param qualifiedByName 具名转换方法名
     * @since 1.2.0
     */
    public void setQualifiedByName(String qualifiedByName) {
        this.qualifiedByName = qualifiedByName;
    }

    /**
     * 判断是否有表达式。
     *
     * @return 如果有表达式返回 true
     * @since 1.2.0
     */
    public boolean hasExpression() {
        return expression != null && !expression.trim().isEmpty();
    }

    /**
     * 判断是否有类型转换器。
     *
     * @return 如果有类型转换器返回 true
     * @since 1.2.0
     */
    public boolean hasConverter() {
        return converterClassName != null && !converterClassName.isEmpty()
                && !converterClassName.endsWith("TypeConverter$None")
                && !converterClassName.endsWith("TypeConverter.None");
    }

    /**
     * 判断是否有具名转换方法。
     *
     * @return 如果有具名转换方法返回 true
     * @since 1.2.0
     */
    public boolean hasQualifiedByName() {
        return qualifiedByName != null && !qualifiedByName.trim().isEmpty();
    }

    /**
     * 判断是否是多对一映射。
     *
     * @return 如果是多对一映射返回 true
     * @since 1.2.0
     */
    public boolean isManyToOne() {
        return mappingType == MappingType.MANY_TO_ONE || 
               (sourceFieldNames != null && sourceFieldNames.size() > 1);
    }

    /**
     * 返回字段映射的字符串表示。
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "FieldMapping{" +
                "sourceField='" + getSourceFieldName() + '\'' +
                ", targetField='" + getTargetFieldName() + '\'' +
                ", sourceType=" + sourceType +
                ", targetType=" + targetType +
                ", mappingType=" + mappingType +
                ", expression='" + expression + '\'' +
                ", converterClassName='" + converterClassName + '\'' +
                '}';
    }
}
