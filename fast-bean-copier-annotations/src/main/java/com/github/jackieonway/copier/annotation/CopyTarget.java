package com.github.jackieonway.copier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记目标类为拷贝目标，用于自动生成 Copier 类。
 *
 * 使用示例：
 * <pre>
 * {@code
 * @CopyTarget(source = User.class, ignore = {"password"})
 * public class UserDto {
 *     private Long id;
 *     private String name;
 *     // password 字段被忽略，不会被拷贝
 * }
 * }
 * </pre>
 *
 * 编译后会自动生成 UserDtoCopier 类，包含以下方法：
 * - toDto(User source): 将 User 对象拷贝到 UserDto
 * - fromDto(UserDto source): 将 UserDto 对象拷贝回 User
 * - toDtoList(List<User> sources): 批量拷贝 List
 * - toDtoSet(Set<User> sources): 批量拷贝 Set
 * - fromDtoList(List<UserDto> sources): 反向批量拷贝 List
 * - fromDtoSet(Set<UserDto> sources): 反向批量拷贝 Set
 *
 * @author jackieonway
 * @since 1.0.0
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
     * 示例：
     * <pre>
     * {@code
     * @CopyTarget(source = User.class, ignore = {"password", "secretKey"})
     * public class UserDto {
     *     // password 和 secretKey 字段不会被拷贝
     * }
     * }
     * </pre>
     *
     * @return 要忽略的字段名数组
     */
    String[] ignore() default {};
}
