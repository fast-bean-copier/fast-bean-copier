package com.github.jackieonway.copier.converter;

/**
 * 类型转换器接口，用于在 Bean 拷贝过程中进行类型转换。
 *
 * <p>实现此接口可以自定义类型转换逻辑，如：
 * <ul>
 *   <li>日期与字符串之间的转换</li>
 *   <li>数字与字符串之间的转换</li>
 *   <li>枚举与字符串/整数之间的转换</li>
 *   <li>对象与 JSON 字符串之间的转换</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>
 * public class DateToStringConverter implements TypeConverter&lt;Date, String&gt; {
 *     &#64;Override
 *     public String convert(Date source, String format) {
 *         if (source == null) {
 *             return null;
 *         }
 *         SimpleDateFormat sdf = new SimpleDateFormat(format);
 *         return sdf.format(source);
 *     }
 * }
 * </pre>
 *
 * <h3>在注解中使用</h3>
 * <pre>
 * &#64;CopyField(converter = DateToStringConverter.class, format = "yyyy-MM-dd")
 * private String birthDateStr;
 * </pre>
 *
 * @param <S> 源类型
 * @param <T> 目标类型
 * @author jackieonway
 * @since 1.2.0
 */
public interface TypeConverter<S, T> {

    /**
     * 将源对象转换为目标类型。
     *
     * @param source 源对象，可能为 null
     * @param format 格式字符串，用于指定转换格式（如日期格式、数字格式等），可能为空字符串
     * @return 转换后的目标对象，如果源对象为 null，通常返回 null
     */
    T convert(S source, String format);

    /**
     * 空实现占位符，用于注解的默认值。
     *
     * <p>当 {@link com.github.jackieonway.copier.annotation.CopyField#converter()}
     * 未指定转换器时，使用此类作为默认值。
     *
     * @since 1.2.0
     */
    final class None implements TypeConverter<Object, Object> {
        
        private None() {
            // 私有构造器，防止实例化
        }

        @Override
        public Object convert(Object source, String format) {
            throw new UnsupportedOperationException("None converter should not be used");
        }
    }
}
