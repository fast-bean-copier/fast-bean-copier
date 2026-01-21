package com.github.jackieonway.copier.example.v131;

import com.github.jackieonway.copier.converter.TypeConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期转字符串转换器，用于测试 converter 跳过逻辑。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class DateToStringConverter implements TypeConverter<Date, String> {
    
    @Override
    public String convert(Date source, String format) {
        if (source == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format != null && !format.isEmpty() ? format : "yyyy-MM-dd");
        return sdf.format(source);
    }
}
