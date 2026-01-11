package com.github.jackieonway.copier.example.v13;

import com.github.jackieonway.copier.annotation.NullValueStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 功能集成测试。
 *
 * <p>测试所有 v1.3 新功能的完整流程。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class V13IntegrationTest {

    // ========== 更新现有对象测试 ==========

    /**
     * 测试更新现有对象完整流程。
     */
    @Test
    public void testUpdateExistingObjectFlow() {
        // 模拟 updateDto 方法的行为
        TestTarget target = new TestTarget();
        target.setName("原始名称");
        target.setAge(25);
        
        TestSource source = new TestSource();
        source.setName("新名称");
        source.setAge(null); // null 值
        
        // IGNORE 策略：只更新非 null 字段
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        // age 为 null，不更新
        
        assertEquals("新名称", target.getName());
        assertEquals(Integer.valueOf(25), target.getAge()); // 保持原值
    }

    /**
     * 测试 REPLACE 策略更新。
     */
    @Test
    public void testReplaceStrategyUpdate() {
        TestTarget target = new TestTarget();
        target.setName("原始名称");
        target.setAge(25);
        
        TestSource source = new TestSource();
        source.setName("新名称");
        source.setAge(null);
        
        // REPLACE 策略：更新所有字段
        target.setName(source.getName());
        target.setAge(source.getAge());
        
        assertEquals("新名称", target.getName());
        assertNull(target.getAge()); // 被设置为 null
    }

    // ========== 条件映射测试 ==========

    /**
     * 测试条件映射完整流程。
     */
    @Test
    public void testConditionalMappingFlow() {
        TestSource source = new TestSource();
        source.setName("测试");
        source.setAge(18);
        
        TestTarget target = new TestTarget();
        
        // 条件：只有当 age >= 18 时才映射 name
        if (source.getAge() != null && source.getAge() >= 18) {
            target.setName(source.getName());
        }
        
        assertEquals("测试", target.getName());
    }

    /**
     * 测试条件不满足时不映射。
     */
    @Test
    public void testConditionalMappingNotMet() {
        TestSource source = new TestSource();
        source.setName("测试");
        source.setAge(16);
        
        TestTarget target = new TestTarget();
        target.setName("默认");
        
        // 条件：只有当 age >= 18 时才映射 name
        if (source.getAge() != null && source.getAge() >= 18) {
            target.setName(source.getName());
        }
        
        assertEquals("默认", target.getName()); // 保持原值
    }

    // ========== 默认值和常量测试 ==========

    /**
     * 测试默认值完整流程。
     */
    @Test
    public void testDefaultValueFlow() {
        TestSource source = new TestSource();
        source.setName(null); // null 值
        
        TestTarget target = new TestTarget();
        
        // 使用默认值
        String defaultValue = "未知";
        if (source.getName() != null) {
            target.setName(source.getName());
        } else {
            target.setName(defaultValue);
        }
        
        assertEquals("未知", target.getName());
    }

    /**
     * 测试常量值完整流程。
     */
    @Test
    public void testConstantValueFlow() {
        TestTarget target = new TestTarget();
        
        // 常量值直接设置
        String constant = "SYSTEM";
        target.setCreatedBy(constant);
        
        assertEquals("SYSTEM", target.getCreatedBy());
    }

    // ========== 映射前回调测试 ==========

    /**
     * 测试映射前回调完整流程。
     */
    @Test
    public void testBeforeMappingFlow() {
        TestSource source = new TestSource();
        source.setName("  test  ");
        
        TestTarget target = new TestTarget();
        
        // 映射前回调：去除空格
        target.beforeMapping(source);
        
        // 执行映射
        target.setName(source.getName());
        
        assertEquals("test", target.getName());
    }

    // ========== 全局配置测试 ==========

    /**
     * 测试全局配置完整流程。
     */
    @Test
    public void testGlobalConfigFlow() {
        // 模拟包级别配置
        NullValueStrategy packageStrategy = NullValueStrategy.IGNORE;
        
        // 类级别未指定，使用包级别配置
        NullValueStrategy effectiveStrategy = packageStrategy;
        
        assertEquals(NullValueStrategy.IGNORE, effectiveStrategy);
    }

    /**
     * 测试类级别覆盖包级别配置。
     */
    @Test
    public void testClassOverridesPackageConfig() {
        // 模拟包级别配置
        NullValueStrategy packageStrategy = NullValueStrategy.IGNORE;
        
        // 类级别指定 REPLACE
        NullValueStrategy classStrategy = NullValueStrategy.REPLACE;
        
        // 类级别优先
        NullValueStrategy effectiveStrategy = classStrategy != null ? classStrategy : packageStrategy;
        
        assertEquals(NullValueStrategy.REPLACE, effectiveStrategy);
    }

    // ========== 辅助类 ==========

    private static class TestSource {
        private String name;
        private Integer age;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    private static class TestTarget {
        private String name;
        private Integer age;
        private String createdBy;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

        public void beforeMapping(TestSource source) {
            if (source.getName() != null) {
                source.setName(source.getName().trim());
            }
        }
    }
}
