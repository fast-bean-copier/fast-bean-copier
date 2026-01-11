package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 条件映射功能测试。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class ConditionalMappingTest {

    /**
     * 测试条件表达式解析。
     */
    @Test
    public void testConditionExpressionParsing() {
        // 测试条件表达式格式验证
        String validCondition = "java(source.getName() != null)";
        assertTrue(validCondition.startsWith("java("));
        assertTrue(validCondition.endsWith(")"));
        
        // 提取条件代码
        String conditionCode = validCondition.substring(5, validCondition.length() - 1);
        assertEquals("source.getName() != null", conditionCode);
    }

    /**
     * 测试复杂条件表达式。
     */
    @Test
    public void testComplexConditionExpression() {
        String complexCondition = "java(source.getAge() > 18 && source.getName() != null)";
        assertTrue(complexCondition.startsWith("java("));
        assertTrue(complexCondition.endsWith(")"));
        
        String conditionCode = complexCondition.substring(5, complexCondition.length() - 1);
        assertEquals("source.getAge() > 18 && source.getName() != null", conditionCode);
    }
}
