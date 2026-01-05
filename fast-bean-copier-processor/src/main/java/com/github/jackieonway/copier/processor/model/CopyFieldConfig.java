package com.github.jackieonway.copier.processor.model;

import com.github.jackieonway.copier.annotation.CopyField;

/**
 * @CopyField 注解配置的数据类。
 *
 * <p>封装从 {@link CopyField} 注解中提取的所有配置项，
 * 提供便捷的访问方法和判断方法。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class CopyFieldConfig {

    /**
     * 源字段名数组。
     */
    private final String[] sourceNames;

    /**
     * 目标字段名。
     */
    private final String target;

    /**
     * 表达式字符串。
     */
    private final String expression;

    /**
     * 具名转换方法名。
     */
    private final String qualifiedByName;

    /**
     * 格式字符串。
     */
    private final String format;

    /**
     * 转换器类名。
     */
    private final String converterClassName;

    /**
     * 构造方法。
     *
     * @param sourceNames       源字段名数组
     * @param target            目标字段名
     * @param expression        表达式字符串
     * @param qualifiedByName   具名转换方法名
     * @param format            格式字符串
     * @param converterClassName 转换器类名
     */
    public CopyFieldConfig(String[] sourceNames, String target, String expression,
                           String qualifiedByName, String format, String converterClassName) {
        this.sourceNames = sourceNames != null ? sourceNames : new String[0];
        this.target = target;
        this.expression = expression;
        this.qualifiedByName = qualifiedByName;
        this.format = format;
        this.converterClassName = converterClassName;
    }

    /**
     * 获取源字段名数组。
     *
     * @return 源字段名数组
     */
    public String[] getSourceNames() {
        return sourceNames;
    }

    /**
     * 获取目标字段名。
     *
     * @return 目标字段名
     */
    public String getTarget() {
        return target;
    }

    /**
     * 获取表达式字符串。
     *
     * @return 表达式字符串
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 获取具名转换方法名。
     *
     * @return 具名转换方法名
     */
    public String getQualifiedByName() {
        return qualifiedByName;
    }

    /**
     * 获取格式字符串。
     *
     * @return 格式字符串
     */
    public String getFormat() {
        return format;
    }

    /**
     * 获取转换器类名。
     *
     * @return 转换器类名
     */
    public String getConverterClassName() {
        return converterClassName;
    }

    /**
     * 判断是否有源字段名配置。
     *
     * @return 如果有源字段名配置返回 true
     */
    public boolean hasSourceNames() {
        return sourceNames != null && sourceNames.length > 0;
    }

    /**
     * 判断是否有目标字段名配置。
     *
     * @return 如果有目标字段名配置返回 true
     */
    public boolean hasTarget() {
        return target != null && !target.trim().isEmpty();
    }

    /**
     * 判断是否有表达式配置。
     *
     * @return 如果有表达式配置返回 true
     */
    public boolean hasExpression() {
        return expression != null && !expression.trim().isEmpty();
    }

    /**
     * 判断是否有具名转换方法配置。
     *
     * @return 如果有具名转换方法配置返回 true
     */
    public boolean hasQualifiedByName() {
        return qualifiedByName != null && !qualifiedByName.trim().isEmpty();
    }

    /**
     * 判断是否有格式字符串配置。
     *
     * @return 如果有格式字符串配置返回 true
     */
    public boolean hasFormat() {
        return format != null && !format.trim().isEmpty();
    }

    /**
     * 判断是否有转换器配置。
     *
     * @return 如果有转换器配置返回 true
     */
    public boolean hasConverter() {
        return converterClassName != null && !converterClassName.isEmpty();
    }

    /**
     * 判断是否是多对一映射（多个源字段映射到一个目标字段）。
     *
     * @return 如果是多对一映射返回 true
     */
    public boolean isManyToOne() {
        return sourceNames != null && sourceNames.length > 1;
    }

    /**
     * 获取第一个源字段名。
     *
     * @return 第一个源字段名，如果没有则返回 null
     */
    public String getFirstSourceName() {
        return hasSourceNames() ? sourceNames[0] : null;
    }

    @Override
    public String toString() {
        return "CopyFieldConfig{" +
                "sourceNames=" + java.util.Arrays.toString(sourceNames) +
                ", target='" + target + '\'' +
                ", expression='" + expression + '\'' +
                ", qualifiedByName='" + qualifiedByName + '\'' +
                ", format='" + format + '\'' +
                ", converterClassName='" + converterClassName + '\'' +
                '}';
    }
}
