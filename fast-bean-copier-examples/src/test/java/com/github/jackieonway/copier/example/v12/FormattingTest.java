package com.github.jackieonway.copier.example.v12;

import com.github.jackieonway.copier.converter.NumberFormatter;
import com.github.jackieonway.copier.converter.DateFormatter;
import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 格式化功能集成测试。
 * 直接测试 TypeConverter 的格式化功能。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class FormattingTest {

    /**
     * 测试数字格式化（NumberFormatter）
     */
    @Test
    public void testNumberFormatting() {
        NumberFormatter formatter = new NumberFormatter();
        
        // 测试正数格式化
        String result1 = formatter.convert(new BigDecimal("1234.56"), "#,##0.00");
        assertEquals("1,234.56", result1);
        
        // 测试负数格式化
        String result2 = formatter.convert(new BigDecimal("-1234.56"), "#,##0.00");
        assertEquals("-1,234.56", result2);
        
        // 测试零值格式化
        String result3 = formatter.convert(BigDecimal.ZERO, "#,##0.00");
        assertEquals("0.00", result3);
        
        // 测试整数格式化
        String result4 = formatter.convert(1000, "#,##0");
        assertEquals("1,000", result4);
    }

    /**
     * 测试日期格式化（DateFormatter）
     */
    @Test
    public void testDateFormatting() {
        DateFormatter formatter = new DateFormatter();
        
        // 测试 LocalDate 格式化
        LocalDate date = LocalDate.of(2023, 12, 25);
        String result1 = formatter.convert(date, "yyyy-MM-dd");
        assertEquals("2023-12-25", result1);
        
        // 测试 LocalDateTime 格式化
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 0);
        String result2 = formatter.convert(dateTime, "yyyy-MM-dd HH:mm:ss");
        assertEquals("2023-12-25 14:30:00", result2);
    }

    /**
     * 测试 null 值处理
     */
    @Test
    public void testNullHandling() {
        NumberFormatter numberFormatter = new NumberFormatter();
        DateFormatter dateFormatter = new DateFormatter();
        
        // 测试 null 数字
        String result1 = numberFormatter.convert(null, "#,##0.00");
        assertNull(result1);
        
        // 测试 null 日期
        String result2 = dateFormatter.convert(null, "yyyy-MM-dd");
        assertNull(result2);
    }

    /**
     * 测试格式字符串参数传递
     */
    @Test
    public void testFormatParameterPassing() {
        NumberFormatter formatter = new NumberFormatter();
        BigDecimal value = new BigDecimal("1234.567");
        
        // 测试不同的格式字符串
        String result1 = formatter.convert(value, "#,##0.00");
        assertEquals("1,234.57", result1);
        
        String result2 = formatter.convert(value, "#,##0.000");
        assertEquals("1,234.567", result2);
        
        String result3 = formatter.convert(value, "#,##0");
        assertEquals("1,235", result3);
    }

    /**
     * 测试类型匹配检查 - 通过编译即表示类型匹配正确
     */
    @Test
    public void testTypeMatching() {
        NumberFormatter numberFormatter = new NumberFormatter();
        DateFormatter dateFormatter = new DateFormatter();
        
        // 这些调用能够编译通过，说明类型匹配检查正确
        assertNotNull(numberFormatter.convert(new BigDecimal("123"), "#,##0.00"));
        assertNotNull(numberFormatter.convert(123, "#,##0"));
        assertNotNull(numberFormatter.convert(123L, "#,##0"));
        assertNotNull(numberFormatter.convert(123.45, "#,##0.00"));
        
        assertNotNull(dateFormatter.convert(LocalDate.now(), "yyyy-MM-dd"));
        assertNotNull(dateFormatter.convert(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"));
    }
}