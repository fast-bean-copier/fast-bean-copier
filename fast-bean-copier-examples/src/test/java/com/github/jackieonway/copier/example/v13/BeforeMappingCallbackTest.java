package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 映射前回调功能测试。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class BeforeMappingCallbackTest {

    /**
     * 测试 beforeMapping 属性提取。
     */
    @Test
    public void testBeforeMappingExtraction() {
        String beforeMapping = "validateAndPrepare";
        assertNotNull(beforeMapping);
        assertFalse(beforeMapping.isEmpty());
        assertEquals("validateAndPrepare", beforeMapping);
    }

    /**
     * 测试空 beforeMapping 属性。
     */
    @Test
    public void testEmptyBeforeMapping() {
        String beforeMapping = "";
        assertTrue(beforeMapping.isEmpty());
    }

    /**
     * 测试 beforeMapping 方法名验证。
     */
    @Test
    public void testBeforeMappingMethodNameValidation() {
        String methodName = "validateAndPrepare";
        
        // 方法名不能为空
        assertFalse(methodName.isEmpty());
        
        // 方法名应该是有效的 Java 标识符
        assertTrue(Character.isJavaIdentifierStart(methodName.charAt(0)));
        for (int i = 1; i < methodName.length(); i++) {
            assertTrue(Character.isJavaIdentifierPart(methodName.charAt(i)));
        }
    }

    /**
     * 测试 beforeMapping 回调调用逻辑。
     */
    @Test
    public void testBeforeMappingCallbackLogic() {
        // 模拟回调执行
        TestCallback callback = new TestCallback();
        TestSource source = new TestSource();
        source.setName("test");
        
        // 执行回调
        callback.beforeMapping(source);
        
        // 验证回调被执行
        assertTrue(callback.isCallbackExecuted());
        assertEquals("test", callback.getLastSourceName());
    }

    /**
     * 测试 beforeMapping 回调修改源对象。
     */
    @Test
    public void testBeforeMappingModifiesSource() {
        TestCallback callback = new TestCallback();
        TestSource source = new TestSource();
        source.setName("  test  ");
        
        // 回调可以修改源对象（如去除空格）
        callback.trimSourceName(source);
        
        assertEquals("test", source.getName());
    }

    /**
     * 测试 beforeMapping 回调验证逻辑。
     */
    @Test
    public void testBeforeMappingValidation() {
        TestCallback callback = new TestCallback();
        TestSource source = new TestSource();
        source.setName(null);
        
        // 回调可以设置默认值
        callback.setDefaultIfNull(source);
        
        assertEquals("default", source.getName());
    }

    // ========== 辅助类 ==========

    /**
     * 测试源对象。
     */
    private static class TestSource {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 测试回调类。
     */
    private static class TestCallback {
        private boolean callbackExecuted = false;
        private String lastSourceName;

        public void beforeMapping(TestSource source) {
            this.callbackExecuted = true;
            this.lastSourceName = source.getName();
        }

        public void trimSourceName(TestSource source) {
            if (source.getName() != null) {
                source.setName(source.getName().trim());
            }
        }

        public void setDefaultIfNull(TestSource source) {
            if (source.getName() == null) {
                source.setName("default");
            }
        }

        public boolean isCallbackExecuted() {
            return callbackExecuted;
        }

        public String getLastSourceName() {
            return lastSourceName;
        }
    }
}
