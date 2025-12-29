package com.github.jackieonway.copier.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 日期解析转换器，将字符串解析为日期类型。
 *
 * <p>默认返回 {@link LocalDateTime} 类型，也可以通过重载方法指定目标类型。
 *
 * <p><b>使用示例</b></p>
 * <pre>
 * &#64;CopyField(converter = DateParser.class, format = "yyyy-MM-dd")
 * private LocalDate birthDate;
 *
 * &#64;CopyField(converter = DateParser.class, format = "yyyy-MM-dd HH:mm:ss")
 * private LocalDateTime createTime;
 * </pre>
 *
 * <p><b>支持的目标类型</b></p>
 * <ul>
 *   <li>{@link Date}</li>
 *   <li>{@link LocalDate}</li>
 *   <li>{@link LocalDateTime}</li>
 *   <li>{@link LocalTime}</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 * @see SimpleDateFormat
 * @see DateTimeFormatter
 */
public class DateParser implements TypeConverter<String, Object> {

    /**
     * 默认日期时间格式。
     */
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将字符串解析为日期对象。
     *
     * <p>默认返回 {@link LocalDateTime} 类型。
     *
     * @param source 源字符串，可能为 null 或空字符串
     * @param format 格式模式字符串，如果为空则使用默认格式
     * @return 解析后的日期对象，如果源字符串为 null 或空则返回 null
     * @throws DateTimeParseException 如果字符串无法解析
     */
    @Override
    public Object convert(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        return parseToLocalDateTime(source.trim(), format);
    }

    /**
     * 将字符串解析为 {@link LocalDateTime}。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @return 解析后的 LocalDateTime
     */
    public LocalDateTime parseToLocalDateTime(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(source.trim(), formatter);
    }

    /**
     * 将字符串解析为 {@link LocalDate}。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @return 解析后的 LocalDate
     */
    public LocalDate parseToLocalDate(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String pattern = (format == null || format.isEmpty()) ? "yyyy-MM-dd" : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(source.trim(), formatter);
    }

    /**
     * 将字符串解析为 {@link LocalTime}。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @return 解析后的 LocalTime
     */
    public LocalTime parseToLocalTime(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String pattern = (format == null || format.isEmpty()) ? "HH:mm:ss" : format;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalTime.parse(source.trim(), formatter);
    }

    /**
     * 将字符串解析为 {@link Date}。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @return 解析后的 Date
     * @throws IllegalArgumentException 如果字符串无法解析
     */
    public Date parseToDate(String source, String format) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String pattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(source.trim());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse '" + source + "' with format '" + pattern + "'", e);
        }
    }

    /**
     * 将字符串解析为指定的日期类型。
     *
     * @param source 源字符串
     * @param format 格式模式字符串
     * @param targetType 目标日期类型
     * @param <T> 目标类型
     * @return 解析后的日期对象
     * @throws IllegalArgumentException 如果目标类型不支持
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(String source, String format, Class<T> targetType) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }

        if (targetType == LocalDateTime.class) {
            return (T) parseToLocalDateTime(source, format);
        } else if (targetType == LocalDate.class) {
            return (T) parseToLocalDate(source, format);
        } else if (targetType == LocalTime.class) {
            return (T) parseToLocalTime(source, format);
        } else if (targetType == Date.class) {
            return (T) parseToDate(source, format);
        }

        throw new IllegalArgumentException("Unsupported target date type: " + targetType.getName());
    }
}
