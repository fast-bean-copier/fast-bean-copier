package com.github.jackieonway.copier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记类支持 Map → Bean 转换，用于自动生成 MapCopier 类的 fromMap 方法。
 *
 * <p>使用示例：
 * <pre>
 * &#64;CopyFromMap
 * public class UserDto {
 *     private Long id;
 *     private String name;
 * }
 * </pre>
 *
 * <p>编译后会自动生成 UserDtoMapCopier 类，包含以下方法：
 * <ul>
 *   <li>fromMap(Map&lt;String, Object&gt; source): 将 Map 转换为 UserDto 对象</li>
 *   <li>fromMap(Map&lt;String, Object&gt; source, preProcessor, postProcessor): 带处理器的转换</li>
 *   <li>fromMapList(List&lt;Map&lt;String, Object&gt;&gt; sources): 批量转换 List</li>
 *   <li>fromMapSet(Set&lt;Map&lt;String, Object&gt;&gt; sources): 批量转换 Set</li>
 * </ul>
 *
 * <p>可与 {@link CopyToMap} 同时标注在同一个类上，生成双向转换方法。
 * 可与 {@link CopyTarget} 同时标注，生成独立的 BeanCopier 和 MapCopier 类。
 *
 * @author jackieonway
 * @since 1.5.0
 * @see CopyToMap
 * @see MapKeyStrategy
 * @see CopyField#mapKey()
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyFromMap {

    /**
     * 忽略的字段列表，可选，默认为空。
     *
     * <p>指定哪些字段不参与 Map → Bean 转换。
     *
     * @return 要忽略的字段名数组
     */
    String[] ignore() default {};

    /**
     * 自定义转换器类列表，可选，默认为空。
     *
     * <p>指定包含转换方法的类，用于 Map 值到字段的自定义转换。
     *
     * @return 转换器类数组
     */
    Class<?>[] uses() default {};

    /**
     * 组件模型，用于指定依赖注入框架，可选，默认为 {@link ComponentModel#DEFAULT}。
     *
     * @return 组件模型
     * @see ComponentModel
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;

    /**
     * Map key 命名策略，可选，默认为 {@link MapKeyStrategy#FIELD_NAME}。
     *
     * <p>字段级别的 {@link CopyField#mapKey()} 优先级高于此策略。
     *
     * @return Map key 命名策略
     * @see MapKeyStrategy
     */
    MapKeyStrategy keyStrategy() default MapKeyStrategy.FIELD_NAME;
}
