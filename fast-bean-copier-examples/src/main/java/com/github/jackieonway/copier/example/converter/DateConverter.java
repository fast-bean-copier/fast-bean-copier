package com.github.jackieonway.copier.example.converter;

import com.github.jackieonway.copier.converter.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转换器示例 - 实现 TypeConverter 接口。
 * 用于演示自定义 TypeConverter 的使用。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class DateConverter implements TypeConverter<Date, String> {

    @Override
    public String convert(Date source, String format) {
        if (source == null) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(source);
    }
}
