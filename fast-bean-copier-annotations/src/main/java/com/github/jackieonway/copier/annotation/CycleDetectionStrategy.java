package com.github.jackieonway.copier.annotation;

/**
 * 循环检测策略枚举。
 *
 * <p>定义在处理嵌套对象时检测到循环引用时的行为。
 *
 * @author jackieonway
 * @since 1.6.0
 */
public enum CycleDetectionStrategy {

    /**
     * 编译期检测到循环直接报错终止（默认策略）。
     *
     * <p>如果检测到 A→B→A 类型的循环引用，会在编译阶段报错，
     * 提示循环路径信息。这是最安全的策略，确保不会有循环引用被静默处理。
     */
    FAIL_FAST,

    /**
     * 运行期循环字段置 null。
     *
     * <p>当运行时检测到循环引用时，将该字段设置为 null 而不是继续递归。
     * 适用于允许循环引用但不需要完整深拷贝的场景。
     */
    RETURN_NULL,

    /**
     * 运行期维护缓存打破循环。
     *
     * <p>在运行时维护一个 IdentityHashMap 缓存，记录已处理过的对象引用。
     * 当检测到循环时，直接返回缓存中的引用，确保引用关系一致性。
     * 适用于需要保持对象引用关系的场景。
     */
    AUTOMATIC_CACHE
}