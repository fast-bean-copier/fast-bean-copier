package com.github.jackieonway.copier.example.converter;

import com.github.jackieonway.copier.converter.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 字符串转日期转换器 - 用于反向转换。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class StringToDateConverter implements TypeConverter<String, Date> {

    @Override
    public Date convert(String source, String format) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: " + source, e);
        }
    }
}
