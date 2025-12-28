package com.github.jackieonway.copier.converter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * 数字解析转换器，将字符串解析为 Number 类型。
 *
 * <p>支持解析各种数字格式的字符串，包括带千分位分隔符的数字。
 *
 * <h3>使用示例</h3>
 * <pre>
 * &#64;CopyField(converter = NumberParser.class, format = "#,##0.00")
 * private BigDecimal price;
 * </pre>
 *
 * <h3>支持的目标类型</h3>
 * <ul>
 *   <li>{@link Integer} / {@code int}</li>
 *   <li>{@link Long} / {@code long}</li>
 *   <li>{@link Double} / {@code double}</li>
 *   <li>{@link Float} / {@code float}</li>
 *   <li>{@link Short} / {@code short}</li>
 *   <li>{@link Byte} / {@code byte}</li>
 *   <li>{@link BigDecimal}</li>
 *   <li>{@link BigInteger}</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 * @see DecimalFormat
 */
public class NumberParser implements TypeConverter<String, Number> {

    /**
     * 将字符串解析为数字。
     *
     * <p>默认返回 {@link BigDecimal} 类型，以保证精度。
     * 如果需要特定类型，可以在使用时进行类型转换。
     *
     * @param source 源字符串，可能为 null 或空字符串
     * @param format 格式模式字符串，用于解析带格式的数字（如千分位分隔符）
     * @return 解析后的数字，如果源字符串为 null 或空则返回 null
     * @throws NumberFormatException 如果字符串无法解析为数字
     */
    @Override
    public Number convert(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = source.trim();
        
        // 如果指定了格式，使用 DecimalFormat 解析
        if (format != null && !format.isEmpty()) {
            try {
                DecimalFormat decimalFormat = new DecimalFormat(format);
                decimalFormat.setParseBigDecimal(true);
                return (BigDecimal) decimalFormat.parse(trimmed);
            } catch (ParseException e) {
                throw new NumberFormatException("Cannot parse '" + source + "' with format '" + format + "'");
            }
        }
        
        // 没有指定格式，直接解析
        return new BigDecimal(trimmed);
    }

    /**
     * 将字符串解析为指定的数字类型。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @param targetType 目标数字类型
     * @param <T> 目标类型
     * @return 解析后的数字
     */
    @SuppressWarnings("unchecked")
    public <T extends Number> T convert(String source, String format, Class<T> targetType) {
        Number number = convert(source, format);
        if (number == null) {
            return null;
        }
        
        if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(number.intValue());
        } else if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(number.longValue());
        } else if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(number.doubleValue());
        } else if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf(number.floatValue());
        } else if (targetType == Short.class || targetType == short.class) {
            return (T) Short.valueOf(number.shortValue());
        } else if (targetType == Byte.class || targetType == byte.class) {
            return (T) Byte.valueOf(number.byteValue());
        } else if (targetType == BigDecimal.class) {
            return (T) (number instanceof BigDecimal ? number : new BigDecimal(number.toString()));
        } else if (targetType == BigInteger.class) {
            return (T) (number instanceof BigDecimal 
                    ? ((BigDecimal) number).toBigInteger() 
                    : BigInteger.valueOf(number.longValue()));
        }
        
        return (T) number;
    }
}
