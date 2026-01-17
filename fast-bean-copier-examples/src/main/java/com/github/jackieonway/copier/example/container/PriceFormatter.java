package com.github.jackieonway.copier.example.container;

import com.github.jackieonway.copier.converter.TypeConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 价格格式化转换器 - 用于测试容器模式下的 TypeConverter。
 * 将 BigDecimal 价格转换为格式化的字符串。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class PriceFormatter implements TypeConverter<BigDecimal, String> {

    @Override
    public String convert(BigDecimal source, String format) {
        if (source == null) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "#,##0.00";
        }
        DecimalFormat df = new DecimalFormat(format);
        return df.format(source);
    }
}
