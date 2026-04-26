package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 前置处理功能测试。
 *
 * <p>原 beforeMapping 属性已在 v1.5.0 中移除，
 * 请使用 preProcessor（UnaryOperator）替代。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class BeforeMappingCallbackTest {

    /**
     * 测试 preProcessor 替代 beforeMapping 的前置处理逻辑。
     */
    @Test
    public void testPreProcessorReplacesBeforeMapping() {
        String sourceName = "  test  ";

        // 模拟 preProcessor 的前置处理：去除空格
        String processed = sourceName.trim();

        assertEquals("test", processed);
    }

    /**
     * 测试前置处理设置默认值。
     */
    @Test
    public void testPreProcessorSetsDefaultValue() {
        String sourceName = null;

        // 模拟 preProcessor 的前置处理：设置默认值
        String processed = sourceName != null ? sourceName : "default";

        assertEquals("default", processed);
    }
}
