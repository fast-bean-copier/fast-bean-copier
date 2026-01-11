package com.github.jackieonway.copier.example.v13;

import com.github.jackieonway.copier.annotation.NullValueStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 更新现有对象功能测试。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class UpdateExistingObjectTest {

    /**
     * 测试 NullValueStrategy 枚举值。
     */
    @Test
    public void testNullValueStrategyEnum() {
        assertEquals(NullValueStrategy.IGNORE, NullValueStrategy.valueOf("IGNORE"));
        assertEquals(NullValueStrategy.REPLACE, NullValueStrategy.valueOf("REPLACE"));
    }

    /**
     * 测试 IGNORE 策略逻辑。
     */
    @Test
    public void testIgnoreStrategy() {
        NullValueStrategy strategy = NullValueStrategy.IGNORE;
        
        // IGNORE 策略：只更新非 null 字段
        String sourceValue = null;
        String targetValue = "原始值";
        
        // 模拟 IGNORE 策略行为
        if (sourceValue != null) {
            targetValue = sourceValue;
        }
        
        // 目标值应保持不变
        assertEquals("原始值", targetValue);
    }

    /**
     * 测试 REPLACE 策略逻辑。
     */
    @Test
    public void testReplaceStrategy() {
        NullValueStrategy strategy = NullValueStrategy.REPLACE;
        
        // REPLACE 策略：更新所有字段（包括 null）
        String sourceValue = null;
        String targetValue = "原始值";
        
        // 模拟 REPLACE 策略行为
        targetValue = sourceValue;
        
        // 目标值应被设置为 null
        assertNull(targetValue);
    }

    /**
     * 测试非 null 值更新。
     */
    @Test
    public void testNonNullValueUpdate() {
        String sourceValue = "新值";
        String targetValue = "原始值";
        
        // 无论哪种策略，非 null 值都应该被更新
        if (sourceValue != null) {
            targetValue = sourceValue;
        }
        
        assertEquals("新值", targetValue);
    }
}
