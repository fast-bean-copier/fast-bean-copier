package com.github.jackieonway.copier.converter;

import java.text.DecimalFormat;

/**
 * 数字格式化转换器，将 Number 类型转换为格式化的字符串。
 *
 * <p>使用 {@link DecimalFormat} 进行格式化，支持各种数字格式模式。
 *
 * <p><b>使用示例</b></p>
 * <pre>
 * &#64;CopyField(converter = NumberFormatter.class, format = "#,##0.00")
 * private String priceStr;
 * </pre>
 *
 * <p><b>常用格式模式</b></p>
 * <ul>
 *   <li>{@code #,##0.00} - 千分位分隔，保留两位小数：1,234.56</li>
 *   <li>{@code 0.00} - 保留两位小数：1234.56</li>
 *   <li>{@code #.##} - 最多两位小数，不补零：1234.5</li>
 *   <li>{@code 0%} - 百分比格式：12%</li>
 *   <li>{@code #.##E0} - 科学计数法：1.23E3</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 * @see DecimalFormat
 */
public class NumberFormatter implements TypeConverter<Number, String> {

    /**
     * 默认格式模式：保留两位小数。
     */
    private static final String DEFAULT_FORMAT = "0.##";

    /**
     * 将数字转换为格式化的字符串。
     *
     * @param source 源数字，可能为 null
     * @param format 格式模式字符串，如果为空则使用默认格式
     * @return 格式化后的字符串，如果源数字为 null 则返回 null
     */
    @Override
    public String convert(Number source, String format) {
        if (source == null) {
            return null;
        }
        
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_FORMAT : format;
        DecimalFormat decimalFormat = new DecimalFormat(pattern);
        return decimalFormat.format(source);
    }
}
