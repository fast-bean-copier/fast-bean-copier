package com.github.jackieonway.copier.converter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;

/**
 * 日期格式化转换器，将日期类型转换为格式化的字符串。
 *
 * <p>支持多种日期类型：
 * <ul>
 *   <li>{@link Date} - 使用 {@link SimpleDateFormat}</li>
 *   <li>{@link LocalDate} - 使用 {@link DateTimeFormatter}</li>
 *   <li>{@link LocalDateTime} - 使用 {@link DateTimeFormatter}</li>
 *   <li>{@link LocalTime} - 使用 {@link DateTimeFormatter}</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>
 * &#64;CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
 * private String birthDateStr;
 *
 * &#64;CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
 * private String createTimeStr;
 * </pre>
 *
 * <h3>常用格式模式</h3>
 * <ul>
 *   <li>{@code yyyy-MM-dd} - 日期：2024-01-15</li>
 *   <li>{@code yyyy-MM-dd HH:mm:ss} - 日期时间：2024-01-15 14:30:00</li>
 *   <li>{@code HH:mm:ss} - 时间：14:30:00</li>
 *   <li>{@code yyyy/MM/dd} - 日期（斜杠分隔）：2024/01/15</li>
 *   <li>{@code yyyyMMdd} - 紧凑日期：20240115</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 * @see SimpleDateFormat
 * @see DateTimeFormatter
 */
public class DateFormatter implements TypeConverter<Object, String> {

    /**
     * 默认日期时间格式。
     */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认日期格式。
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认时间格式。
     */
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 将日期对象转换为格式化的字符串。
     *
     * @param source 源日期对象，支持 Date、LocalDate、LocalDateTime、LocalTime
     * @param format 格式模式字符串，如果为空则使用默认格式
     * @return 格式化后的字符串，如果源对象为 null 则返回 null
     * @throws IllegalArgumentException 如果源对象类型不支持
     */
    @Override
    public String convert(Object source, String format) {
        if (source == null) {
            return null;
        }

        if (source instanceof Date) {
            return formatDate((Date) source, format);
        } else if (source instanceof LocalDateTime) {
            return formatLocalDateTime((LocalDateTime) source, format);
        } else if (source instanceof LocalDate) {
            return formatLocalDate((LocalDate) source, format);
        } else if (source instanceof LocalTime) {
            return formatLocalTime((LocalTime) source, format);
        } else if (source instanceof Temporal) {
            // 其他 Temporal 类型尝试使用 DateTimeFormatter
            return formatTemporal((Temporal) source, format);
        }

        throw new IllegalArgumentException("Unsupported date type: " + source.getClass().getName());
    }

    private String formatDate(Date date, String format) {
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    private String formatLocalDateTime(LocalDateTime dateTime, String format) {
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    private String formatLocalDate(LocalDate date, String format) {
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATE_FORMAT : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.format(formatter);
    }

    private String formatLocalTime(LocalTime time, String format) {
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_TIME_FORMAT : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return time.format(formatter);
    }

    private String formatTemporal(Temporal temporal, String format) {
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(temporal);
    }
}
