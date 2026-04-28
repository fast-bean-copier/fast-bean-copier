package com.github.jackieonway.copier.annotation;

/**
 * Map key 命名策略枚举，用于 Bean ↔ Map 转换时确定 Map 的 key。
 *
 * <p>当使用 {@link CopyToMap} 或 {@link CopyFromMap} 注解时，
 * 通过此枚举指定字段名到 Map key 的转换策略。
 *
 * <p>优先级：{@link CopyField#mapKey()} > {@code MapKeyStrategy} > 字段名
 *
 * @author jackieonway
 * @since 1.5.0
 * @see CopyToMap
 * @see CopyFromMap
 */
public enum MapKeyStrategy {

    /**
     * 使用字段名作为 Map key（默认策略）。
     *
     * <p>示例：字段 {@code firstName} → key {@code "firstName"}
     */
    FIELD_NAME,

    /**
     * 使用驼峰命名作为 Map key。
     *
     * <p>与 {@link #FIELD_NAME} 相同，字段名本身即为驼峰格式。
     * 示例：字段 {@code firstName} → key {@code "firstName"}
     */
    CAMEL_CASE,

    /**
     * 使用下划线命名（snake_case）作为 Map key。
     *
     * <p>示例：字段 {@code firstName} → key {@code "first_name"}
     */
    SNAKE_CASE,

    /**
     * 使用自定义 key，需配合 {@link CopyField#mapKey()} 指定具体 key 值。
     *
     * <p>当策略为 {@code CUSTOM} 时，必须在每个字段上通过
     * {@link CopyField#mapKey()} 指定 key；未指定时回退到字段名。
     */
    CUSTOM
}
