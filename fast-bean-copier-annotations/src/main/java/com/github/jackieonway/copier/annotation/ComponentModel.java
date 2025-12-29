package com.github.jackieonway.copier.annotation;

/**
 * 组件模型枚举，用于指定生成的 Copier 类的依赖注入框架。
 *
 * <p>不同的组件模型会影响生成代码的方式：
 * <ul>
 *   <li>{@link #DEFAULT}：生成静态方法，无依赖注入</li>
 *   <li>{@link #SPRING}：生成 Spring Bean，使用 {@code @Component} 注解</li>
 *   <li>{@link #CDI}：生成 CDI Bean，使用 {@code @ApplicationScoped} 注解</li>
 *   <li>{@link #JSR330}：生成 JSR-330 兼容 Bean，使用 {@code @Named} 和 {@code @Singleton} 注解</li>
 * </ul>
 *
 * <p><b>使用示例</b></p>
 * <pre>
 * // 默认模式，生成静态方法
 * &#64;CopyTarget(source = User.class)
 * public class UserDto { }
 *
 * // Spring 模式，生成 Spring Bean
 * &#64;CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
 * public class UserDto { }
 * </pre>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see CopyTarget#componentModel()
 */
public enum ComponentModel {

    /**
     * 默认模式，无依赖注入。
     *
     * <p>生成的 Copier 类使用静态方法，可以直接通过类名调用：
     * <pre>
     * UserDto dto = UserDtoCopier.toDto(user);
     * </pre>
     */
    DEFAULT,

    /**
     * Spring 框架模式。
     *
     * <p>生成的 Copier 类会添加 {@code @Component} 注解，
     * 可以通过 Spring 依赖注入使用：
     * <pre>
     * &#64;Autowired
     * private UserDtoCopier userDtoCopier;
     *
     * UserDto dto = userDtoCopier.toDto(user);
     * </pre>
     */
    SPRING,

    /**
     * CDI（Contexts and Dependency Injection）框架模式。
     *
     * <p>生成的 Copier 类会添加 {@code @ApplicationScoped} 注解，
     * 可以通过 CDI 依赖注入使用：
     * <pre>
     * &#64;Inject
     * private UserDtoCopier userDtoCopier;
     * </pre>
     */
    CDI,

    /**
     * JSR-330 标准模式。
     *
     * <p>生成的 Copier 类会添加 {@code @Named} 和 {@code @Singleton} 注解，
     * 兼容任何支持 JSR-330 的依赖注入框架：
     * <pre>
     * &#64;Inject
     * &#64;Named("userDtoCopier")
     * private UserDtoCopier userDtoCopier;
     * </pre>
     */
    JSR330
}
