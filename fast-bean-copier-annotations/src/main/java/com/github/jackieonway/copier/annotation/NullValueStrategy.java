package com.github.jackieonway.copier.annotation;

/**
 * null 值处理策略枚举，用于指定在更新现有对象时如何处理 null 值。
 *
 * <p>此枚举主要用于 {@link CopyTargetConfig#nullValueStrategy()} 配置，
 * 影响 {@code updateDto} 和 {@code updateEntity} 方法的行为。
 *
 * <p><b>使用示例</b></p>
 *
 * <p><b>1. 在包级别配置中使用</b></p>
 * <pre>
 * // package-info.java
 * &#64;CopyTargetConfig(nullValueStrategy = NullValueStrategy.IGNORE)
 * package com.example.dto;
 *
 * import com.github.jackieonway.copier.annotation.CopyTargetConfig;
 * import com.github.jackieonway.copier.annotation.NullValueStrategy;
 * </pre>
 *
 * <p><b>2. IGNORE 策略效果</b></p>
 * <pre>
 * // 源对象
 * User source = new User();
 * source.setName(null);  // null 值
 * source.setAge(25);     // 非 null 值
 *
 * // 目标对象
 * UserDto target = new UserDto();
 * target.setName("原始名称");
 * target.setAge(20);
 *
 * // 使用 IGNORE 策略更新后
 * // target.name = "原始名称"  (保持不变，因为 source.name 为 null)
 * // target.age = 25           (被更新，因为 source.age 不为 null)
 * </pre>
 *
 * <p><b>3. REPLACE 策略效果</b></p>
 * <pre>
 * // 使用 REPLACE 策略更新后
 * // target.name = null  (被设置为 null)
 * // target.age = 25     (被更新)
 * </pre>
 *
 * @author jackieonway
 * @since 1.3.0
 * @see CopyTargetConfig#nullValueStrategy()
 */
public enum NullValueStrategy {

    /**
     * 忽略 null 值，不更新目标字段。
     *
     * <p>当源字段值为 null 时，目标字段保持原值不变。
     * 这是更新现有对象时的推荐策略，可以避免意外覆盖已有数据。
     *
     * <p>生成的代码示例：
     * <pre>
     * if (source.getName() != null) {
     *     target.setName(source.getName());
     * }
     * // 如果 source.getName() 为 null，则不执行任何操作
     * </pre>
     */
    IGNORE,

    /**
     * 替换 null 值，将目标字段设置为 null。
     *
     * <p>当源字段值为 null 时，目标字段也会被设置为 null。
     * 这种策略适用于需要完全同步源对象状态的场景。
     *
     * <p>生成的代码示例：
     * <pre>
     * target.setName(source.getName());
     * // 无论 source.getName() 是否为 null，都会执行赋值
     * </pre>
     */
    REPLACE
}
