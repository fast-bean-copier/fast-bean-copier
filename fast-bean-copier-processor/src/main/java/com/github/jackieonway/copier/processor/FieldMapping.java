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

    // ========== v1.3 新增字段 ==========

    /**
     * 条件表达式。
     *
     * @since 1.3.0
     */
    private String condition;

    /**
     * 默认值。
     *
     * @since 1.3.0
     */
    private String defaultValue;

    /**
     * 常量值。
     *
     * @since 1.3.0
     */
    private String constant;

    // ========== v1.3.1 新增字段 ==========

    /**
     * 逆向转换跳过标记。
     *
     * @since 1.3.1
     */
    private boolean skipInReverseMapping = false;

    /**
     * 逆向转换跳过原因。
     *
     * @since 1.3.1
     */
    private String reverseSkipReason;

    // ========== v1.4 新增字段 ==========

    /**
     * 深拷贝控制标记。
     *
     * @since 1.4.0
     */
    private boolean deepCopy = true;

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

    // ========== v1.3 新增方法 ==========

    /**
     * 获取条件表达式。
     *
     * @return 条件表达式
     * @since 1.3.0
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 设置条件表达式。
     *
     * @param condition 条件表达式
     * @since 1.3.0
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * 判断是否有条件表达式。
     *
     * @return 如果有条件表达式返回 true
     * @since 1.3.0
     */
    public boolean hasCondition() {
        return condition != null && !condition.trim().isEmpty();
    }

    /**
     * 获取解析后的条件表达式（去除 java() 包装）。
     *
     * @return 解析后的条件表达式
     * @since 1.3.0
     */
    public String getConditionExpression() {
        if (condition == null || condition.trim().isEmpty()) {
            return null;
        }
        String trimmed = condition.trim();
        if (trimmed.startsWith("java(") && trimmed.endsWith(")")) {
            return trimmed.substring(5, trimmed.length() - 1);
        }
        return trimmed;
    }

    /**
     * 获取默认值。
     *
     * @return 默认值
     * @since 1.3.0
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * 设置默认值。
     *
     * @param defaultValue 默认值
     * @since 1.3.0
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * 判断是否有默认值。
     *
     * @return 如果有默认值返回 true
     * @since 1.3.0
     */
    public boolean hasDefaultValue() {
        return defaultValue != null && !defaultValue.trim().isEmpty();
    }

    /**
     * 获取常量值。
     *
     * @return 常量值
     * @since 1.3.0
     */
    public String getConstant() {
        return constant;
    }

    /**
     * 设置常量值。
     *
     * @param constant 常量值
     * @since 1.3.0
     */
    public void setConstant(String constant) {
        this.constant = constant;
    }

    /**
     * 判断是否有常量值。
     *
     * @return 如果有常量值返回 true
     * @since 1.3.0
     */
    public boolean hasConstant() {
        return constant != null && !constant.trim().isEmpty();
    }

    /**
     * 判断是否为常量映射。
     *
     * @return 如果是常量映射返回 true
     * @since 1.3.0
     */
    public boolean isConstantMapping() {
        return hasConstant();
    }

    // ========== v1.3.1 新增方法 ==========

    /**
     * 判断是否在逆向转换中跳过。
     *
     * @return 如果需要跳过返回 true
     * @since 1.3.1
     */
    public boolean isSkipInReverseMapping() {
        return skipInReverseMapping;
    }

    /**
     * 设置逆向转换跳过标记。
     *
     * @param skipInReverseMapping 是否跳过
     * @since 1.3.1
     */
    public void setSkipInReverseMapping(boolean skipInReverseMapping) {
        this.skipInReverseMapping = skipInReverseMapping;
    }

    /**
     * 获取逆向转换跳过原因。
     *
     * @return 跳过原因
     * @since 1.3.1
     */
    public String getReverseSkipReason() {
        return reverseSkipReason;
    }

    /**
     * 设置逆向转换跳过原因。
     *
     * @param reverseSkipReason 跳过原因
     * @since 1.3.1
     */
    public void setReverseSkipReason(String reverseSkipReason) {
        this.reverseSkipReason = reverseSkipReason;
    }

    // ========== v1.4 新增方法 ==========

    /**
     * 判断是否启用深拷贝。
     *
     * @return 如果启用深拷贝返回 true
     * @since 1.4.0
     */
    public boolean isDeepCopy() {
        return deepCopy;
    }

    /**
     * 设置深拷贝控制标记。
     *
     * @param deepCopy 是否启用深拷贝
     * @since 1.4.0
     */
    public void setDeepCopy(boolean deepCopy) {
        this.deepCopy = deepCopy;
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
