package com.github.jackieonway.copier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 包级别配置注解，用于为整个包内的所有 {@link CopyTarget} 提供默认配置。
 *
 * <p>此注解应用于 {@code package-info.java} 文件，为包内所有使用 {@link CopyTarget}
 * 注解的类提供统一的默认配置，减少重复配置。
 *
 * <p><b>配置优先级</b></p>
 * <p>配置的优先级从高到低为：
 * <ol>
 *   <li>类级别配置（{@link CopyTarget} 注解属性）</li>
 *   <li>包级别配置（{@link CopyTargetConfig} 注解属性）</li>
 *   <li>默认值</li>
 * </ol>
 *
 * <p><b>使用示例</b></p>
 *
 * <p><b>1. 创建 package-info.java 文件</b></p>
 * <pre>
 * // 文件路径：src/main/java/com/example/dto/package-info.java
 * &#64;CopyTargetConfig(
 *     componentModel = ComponentModel.SPRING,
 *     nullValueStrategy = NullValueStrategy.IGNORE
 * )
 * package com.example.dto;
 *
 * import com.github.jackieonway.copier.annotation.CopyTargetConfig;
 * import com.github.jackieonway.copier.annotation.ComponentModel;
 * import com.github.jackieonway.copier.annotation.NullValueStrategy;
 * </pre>
 *
 * <p><b>2. 包内的 DTO 类自动继承配置</b></p>
 * <pre>
 * // 文件路径：src/main/java/com/example/dto/UserDto.java
 * package com.example.dto;
 *
 * &#64;CopyTarget(source = User.class)
 * public class UserDto {
 *     // 自动使用包级别配置：
 *     // - componentModel = SPRING
 *     // - nullValueStrategy = IGNORE
 * }
 * </pre>
 *
 * <p><b>3. 类级别覆盖包级别配置</b></p>
 * <pre>
 * // 文件路径：src/main/java/com/example/dto/OrderDto.java
 * package com.example.dto;
 *
 * &#64;CopyTarget(source = Order.class, componentModel = ComponentModel.DEFAULT)
 * public class OrderDto {
 *     // componentModel 被覆盖为 DEFAULT
 *     // nullValueStrategy 仍然使用包级别配置 IGNORE
 * }
 * </pre>
 *
 * @author jackieonway
 * @since 1.3.0
 * @see CopyTarget
 * @see ComponentModel
 * @see NullValueStrategy
 */
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyTargetConfig {

    /**
     * 默认组件模型，用于指定依赖注入框架。
     *
     * <p>此配置会被包内所有 {@link CopyTarget} 注解的类继承，
     * 除非类级别显式指定了不同的 {@link CopyTarget#componentModel()}。
     *
     * @return 组件模型，默认为 {@link ComponentModel#DEFAULT}
     * @see ComponentModel
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;

    /**
     * 默认 null 值处理策略。
     *
     * <p>此配置影响 {@code updateDto} 和 {@code updateEntity} 方法的行为，
     * 决定当源字段值为 null 时如何处理目标字段。
     *
     * @return null 值处理策略，默认为 {@link NullValueStrategy#IGNORE}
     * @see NullValueStrategy
     */
    NullValueStrategy nullValueStrategy() default NullValueStrategy.IGNORE;
}
