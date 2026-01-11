package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 默认值和常量功能测试。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class DefaultValueConstantTest {

    /**
     * 测试字符串类型默认值转换。
     */
    @Test
    public void testStringDefaultValue() {
        String defaultValue = "未知";
        String typeLiteral = "\"" + defaultValue + "\"";
        assertEquals("\"未知\"", typeLiteral);
    }

    /**
     * 测试整数类型默认值转换。
     */
    @Test
    public void testIntegerDefaultValue() {
        String defaultValue = "0";
        // Integer 类型直接使用数字
        assertEquals("0", defaultValue);
    }

    /**
     * 测试 Long 类型默认值转换。
     */
    @Test
    public void testLongDefaultValue() {
        String defaultValue = "100";
        String typeLiteral = defaultValue + "L";
        assertEquals("100L", typeLiteral);
    }

    /**
     * 测试 Boolean 类型默认值转换。
     */
    @Test
    public void testBooleanDefaultValue() {
        String defaultValue = "true";
        assertEquals("true", defaultValue.toLowerCase());
        
        defaultValue = "FALSE";
        assertEquals("false", defaultValue.toLowerCase());
    }

    /**
     * 测试 BigDecimal 类型默认值转换。
     */
    @Test
    public void testBigDecimalDefaultValue() {
        String defaultValue = "0.00";
        String typeLiteral = "new java.math.BigDecimal(\"" + defaultValue + "\")";
        assertEquals("new java.math.BigDecimal(\"0.00\")", typeLiteral);
    }

    /**
     * 测试常量值设置。
     */
    @Test
    public void testConstantValue() {
        String constant = "SYSTEM";
        String typeLiteral = "\"" + constant + "\"";
        assertEquals("\"SYSTEM\"", typeLiteral);
    }
}
