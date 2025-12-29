package com.github.jackieonway.copier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记目标类为拷贝目标，用于自动生成 Copier 类。
 *
 * <p>使用示例：
 * <pre>
 * &#64;CopyTarget(source = User.class, ignore = {"password"})
 * public class UserDto {
 *     private Long id;
 *     private String name;
 *     // password 字段被忽略，不会被拷贝
 * }
 * </pre>
 *
 * <p>编译后会自动生成 UserDtoCopier 类，包含以下方法：
 * <ul>
 *   <li>toDto(User source): 将 User 对象拷贝到 UserDto</li>
 *   <li>fromDto(UserDto source): 将 UserDto 对象拷贝回 User</li>
 *   <li>toDtoList(List&lt;User&gt; sources): 批量拷贝 List</li>
 *   <li>toDtoSet(Set&lt;User&gt; sources): 批量拷贝 Set</li>
 *   <li>fromDtoList(List&lt;UserDto&gt; sources): 反向批量拷贝 List</li>
 *   <li>fromDtoSet(Set&lt;UserDto&gt; sources): 反向批量拷贝 Set</li>
 * </ul>
 *
 * <p><b>v1.2 新功能</b></p>
 *
 * <p><b>使用自定义转换器</b></p>
 * <pre>
 * &#64;CopyTarget(source = User.class, uses = {DateConverter.class, PriceConverter.class})
 * public class UserDto {
 *     &#64;CopyField(qualifiedByName = "formatDate")
 *     private String birthDateStr;
 * }
 * </pre>
 *
 * <p><b>使用 Spring 依赖注入</b></p>
 * <pre>
 * &#64;CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
 * public class UserDto { }
 * </pre>
 *
 * @author jackieonway
 * @since 1.0.0
 * @see CopyField
 * @see ComponentModel
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyTarget {

    /**
     * 源类，必需。
     * 指定要从哪个类拷贝数据。
     *
     * @return 源类的 Class 对象
     */
    Class<?> source();

    /**
     * 忽略的字段列表，可选，默认为空。
     * 指定哪些字段不需要拷贝。
     *
     * <p>示例：
     * <pre>
     * &#64;CopyTarget(source = User.class, ignore = {"password", "secretKey"})
     * public class UserDto {
     *     // password 和 secretKey 字段不会被拷贝
     * }
     * </pre>
     *
     * @return 要忽略的字段名数组
     */
    String[] ignore() default {};

    /**
     * 自定义转换器类列表，可选，默认为空。
     *
     * <p>指定包含转换方法的类，这些类中的公共方法可以通过
     * {@link CopyField#qualifiedByName()} 引用。
     *
     * <p>转换器类中的方法会根据方法名和参数类型自动匹配。
     *
     * <p>示例：
     * <pre>
     * // 定义转换器类
     * public class DateConverter {
     *     public String formatDate(Date date) {
     *         return new SimpleDateFormat("yyyy-MM-dd").format(date);
     *     }
     * }
     *
     * // 使用转换器
     * &#64;CopyTarget(source = User.class, uses = {DateConverter.class})
     * public class UserDto {
     *     &#64;CopyField(qualifiedByName = "formatDate")
     *     private String birthDateStr;
     * }
     * </pre>
     *
     * @return 转换器类数组
     * @since 1.2.0
     */
    Class<?>[] uses() default {};

    /**
     * 组件模型，用于指定依赖注入框架，可选，默认为 {@link ComponentModel#DEFAULT}。
     *
     * <p>不同的组件模型会影响生成代码的方式：
     * <ul>
     *   <li>{@link ComponentModel#DEFAULT}：生成静态方法，无依赖注入</li>
     *   <li>{@link ComponentModel#SPRING}：生成 Spring Bean</li>
     *   <li>{@link ComponentModel#CDI}：生成 CDI Bean</li>
     *   <li>{@link ComponentModel#JSR330}：生成 JSR-330 兼容 Bean</li>
     * </ul>
     *
     * <p>示例：
     * <pre>
     * &#64;CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
     * public class UserDto { }
     * </pre>
     *
     * @return 组件模型
     * @since 1.2.0
     * @see ComponentModel
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;
}
