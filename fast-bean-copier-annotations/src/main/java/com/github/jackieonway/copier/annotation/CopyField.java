package com.github.jackieonway.copier.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.jackieonway.copier.converter.TypeConverter;

/**
 * 字段级映射和转换配置注解。
 *
 * <p>用于在目标类的字段上指定自定义的映射规则，支持：
 * <ul>
 *   <li>多对一映射：多个源字段合并到一个目标字段</li>
 *   <li>一对多映射：一个源字段拆分到多个目标字段</li>
 *   <li>表达式转换：使用 Java 表达式进行字段转换</li>
 *   <li>类型转换器：使用 TypeConverter 进行类型转换</li>
 *   <li>具名转换方法：绑定自定义转换器中的具名方法</li>
 * </ul>
 *
 * <p><b>使用示例</b></p>
 *
 * <p><b>1. 多对一映射（多个源字段合并）</b></p>
 * <pre>
 * &#64;CopyField(source = {"firstName", "lastName"}, expression = "java(source.getFirstName() + \" \" + source.getLastName())")
 * private String fullName;
 * </pre>
 *
 * <p><b>2. 一对多映射（一个源字段拆分）</b></p>
 * <pre>
 * &#64;CopyField(source = "fullName", expression = "java(source.getFullName().split(\" \")[0])")
 * private String firstName;
 *
 * &#64;CopyField(source = "fullName", expression = "java(source.getFullName().split(\" \")[1])")
 * private String lastName;
 * </pre>
 *
 * <p><b>3. 使用类型转换器</b></p>
 * <pre>
 * &#64;CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
 * private String birthDateStr;
 * </pre>
 *
 * <p><b>4. 使用具名转换方法</b></p>
 * <pre>
 * &#64;CopyField(qualifiedByName = "formatPrice")
 * private String priceStr;
 * </pre>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see CopyTarget
 * @see TypeConverter
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyField {

    /**
     * 源字段名数组，支持多对一映射。
     *
     * <p>当指定多个源字段时，通常需要配合 {@link #expression()} 使用，
     * 通过表达式将多个源字段的值合并为一个目标值。
     *
     * <p>如果为空数组（默认值），则使用注解所在字段的名称作为源字段名。
     *
     * @return 源字段名数组
     */
    String[] source() default {};

    /**
     * 目标字段名。
     *
     * <p>默认为空字符串，表示使用注解所在字段的名称作为目标字段名。
     * 通常不需要指定，除非需要映射到不同名称的字段。
     *
     * @return 目标字段名
     */
    String target() default "";

    /**
     * Java 表达式，用于字段值转换。
     *
     * <p>表达式必须使用 {@code java(...)} 包裹，括号内为有效的 Java 表达式。
     * 表达式中可以使用 {@code source} 变量引用源对象，
     * 通过调用源对象的 getter 方法获取字段值。
     *
     * <p>表达式示例：
     * <ul>
     *   <li>{@code java(source.getFirstName() + " " + source.getLastName())} - 字符串拼接</li>
     *   <li>{@code java(source.getAge() >= 18 ? "成年" : "未成年")} - 三元运算符</li>
     *   <li>{@code java(source.getPrice() * source.getQuantity())} - 数学运算</li>
     * </ul>
     *
     * @return Java 表达式字符串
     */
    String expression() default "";

    /**
     * 绑定具名转换方法。
     *
     * <p>指定在 {@link CopyTarget#uses()} 中声明的转换器类中的方法名。
     * 处理器会根据方法名和参数类型自动匹配合适的转换方法。
     *
     * @return 转换方法名
     */
    String qualifiedByName() default "";

    /**
     * 指定 TypeConverter 实现类。
     *
     * <p>用于类型转换，如日期格式化、数字格式化等。
     * 配合 {@link #format()} 使用可以传递格式化参数。
     *
     * <p>默认值 {@link TypeConverter.None} 表示不使用类型转换器。
     *
     * @return TypeConverter 实现类
     */
    Class<? extends TypeConverter<?, ?>> converter() default TypeConverter.None.class;

    boolean deepCopy() default true;

    /**
     * 自定义 Map key，仅在 Bean ↔ Map 转换时生效。
     *
     * <p>当使用 {@link CopyToMap} 或 {@link CopyFromMap} 进行转换时，
     * 此属性指定该字段对应的 Map key 名称。
     *
     * <p>优先级高于 {@link CopyToMap#keyStrategy()} 和 {@link CopyFromMap#keyStrategy()}。
     * 默认为空字符串，表示不指定自定义 key，使用 keyStrategy 策略确定 key。
     *
     * <p>使用示例：
     * <pre>
     * &#64;CopyToMap
     * public class UserDto {
     *     &#64;CopyField(mapKey = "user_name")
     *     private String name;  // Map 中的 key 为 "user_name"
     * }
     * </pre>
     *
     * @return 自定义 Map key，默认为空字符串
     * @since 1.5.0
     */
    String mapKey() default "";

    /**
     * 转换器配置参数（格式字符串）。
     *
     * <p>传递给 {@link TypeConverter#convert(Object, String)} 方法的第二个参数。
     * 常用于日期格式化模式、数字格式化模式等。
     *
     * <p>示例：
     * <ul>
     *   <li>{@code "yyyy-MM-dd"} - 日期格式</li>
     *   <li>{@code "#,##0.00"} - 数字格式</li>
     * </ul>
     *
     * @return 格式字符串
     */
    String format() default "";

    /**
     * 条件表达式，用于决定是否映射该字段。
     *
     * <p>当条件表达式的结果为 {@code true} 时，才会执行字段映射；
     * 否则跳过该字段的映射。
     *
     * <p>表达式格式必须以 {@code java(} 开头和 {@code )} 结尾，
     * 括号内为有效的 Java 布尔表达式。表达式中可以使用 {@code source} 变量引用源对象。
     *
     * <p>使用示例：
     * <pre>
     * // 仅当源字段不为 null 时才映射
     * &#64;CopyField(condition = "java(source.getName() != null)")
     * private String name;
     *
     * // 仅当年龄大于 18 时才映射
     * &#64;CopyField(condition = "java(source.getAge() > 18)")
     * private Integer age;
     *
     * // 组合条件
     * &#64;CopyField(condition = "java(source.getStatus() != null && source.getStatus().equals(\"ACTIVE\"))")
     * private String status;
     * </pre>
     *
     * @return 条件表达式字符串，默认为空表示无条件映射
     * @since 1.3.0
     */
    String condition() default "";

    /**
     * 默认值，当源字段为 null 时使用。
     *
     * <p>默认值以字符串形式指定，处理器会根据目标字段类型自动进行类型转换。
     * 支持的类型包括：String、Integer、Long、Double、Float、Short、Byte、
     * Boolean、BigDecimal、BigInteger 等。
     *
     * <p><b>注意：</b>此属性与 {@link #constant()} 互斥，不能同时使用。
     *
     * <p>使用示例：
     * <pre>
     * // String 类型默认值
     * &#64;CopyField(defaultValue = "未知")
     * private String name;
     *
     * // Integer 类型默认值
     * &#64;CopyField(defaultValue = "0")
     * private Integer count;
     *
     * // Boolean 类型默认值
     * &#64;CopyField(defaultValue = "false")
     * private Boolean active;
     *
     * // BigDecimal 类型默认值
     * &#64;CopyField(defaultValue = "0.00")
     * private BigDecimal price;
     * </pre>
     *
     * @return 默认值字符串，默认为空表示不使用默认值
     * @since 1.3.0
     */
    String defaultValue() default "";

    /**
     * 常量值，直接设置目标字段为指定的常量，不依赖源字段。
     *
     * <p>常量值以字符串形式指定，处理器会根据目标字段类型自动进行类型转换。
     * 支持的类型包括：String、Integer、Long、Double、Float、Short、Byte、
     * Boolean、BigDecimal、BigInteger 等。
     *
     * <p><b>注意：</b>
     * <ul>
     *   <li>此属性与 {@link #defaultValue()} 互斥，不能同时使用</li>
     *   <li>使用此属性时，{@link #source()} 属性将被忽略</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>
     * // 设置常量字符串
     * &#64;CopyField(constant = "SYSTEM")
     * private String createdBy;
     *
     * // 设置常量数字
     * &#64;CopyField(constant = "1")
     * private Integer version;
     *
     * // 设置常量布尔值
     * &#64;CopyField(constant = "true")
     * private Boolean enabled;
     * </pre>
     *
     * @return 常量值字符串，默认为空表示不使用常量值
     * @since 1.3.0
     */
    String constant() default "";
}
