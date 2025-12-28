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
 * <h3>使用示例</h3>
 *
 * <h4>1. 多对一映射（多个源字段合并）</h4>
 * <pre>
 * &#64;CopyField(source = {"firstName", "lastName"}, expression = "source.getFirstName() + \" \" + source.getLastName()")
 * private String fullName;
 * </pre>
 *
 * <h4>2. 一对多映射（一个源字段拆分）</h4>
 * <pre>
 * &#64;CopyField(source = "fullName", expression = "source.getFullName().split(\" \")[0]")
 * private String firstName;
 *
 * &#64;CopyField(source = "fullName", expression = "source.getFullName().split(\" \")[1]")
 * private String lastName;
 * </pre>
 *
 * <h4>3. 使用类型转换器</h4>
 * <pre>
 * &#64;CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
 * private String birthDateStr;
 * </pre>
 *
 * <h4>4. 使用具名转换方法</h4>
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
     * <p>表达式中可以使用 {@code source} 变量引用源对象，
     * 通过调用源对象的 getter 方法获取字段值。
     *
     * <p>表达式示例：
     * <ul>
     *   <li>{@code source.getFirstName() + " " + source.getLastName()} - 字符串拼接</li>
     *   <li>{@code source.getAge() >= 18 ? "成年" : "未成年"} - 三元运算符</li>
     *   <li>{@code source.getPrice() * source.getQuantity()} - 数学运算</li>
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
}
