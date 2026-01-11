package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 组合功能测试。
 *
 * <p>测试多个 v1.3 功能组合使用的场景。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class V13CombinationTest {

    // ========== 条件映射 + 表达式组合 ==========

    /**
     * 测试条件映射与表达式组合。
     */
    @Test
    public void testConditionWithExpression() {
        TestSource source = new TestSource();
        source.setFirstName("John");
        source.setLastName("Doe");
        source.setActive(true);
        
        TestTarget target = new TestTarget();
        
        // 条件：只有当 active 为 true 时才执行表达式映射
        if (source.isActive()) {
            // 表达式：拼接 firstName 和 lastName
            target.setFullName(source.getFirstName() + " " + source.getLastName());
        }
        
        assertEquals("John Doe", target.getFullName());
    }

    /**
     * 测试条件不满足时表达式不执行。
     */
    @Test
    public void testConditionWithExpressionNotMet() {
        TestSource source = new TestSource();
        source.setFirstName("John");
        source.setLastName("Doe");
        source.setActive(false);
        
        TestTarget target = new TestTarget();
        target.setFullName("默认");
        
        if (source.isActive()) {
            target.setFullName(source.getFirstName() + " " + source.getLastName());
        }
        
        assertEquals("默认", target.getFullName());
    }

    // ========== 默认值 + 条件映射组合 ==========

    /**
     * 测试默认值与条件映射组合。
     */
    @Test
    public void testDefaultValueWithCondition() {
        TestSource source = new TestSource();
        source.setName(null);
        source.setActive(true);
        
        TestTarget target = new TestTarget();
        
        // 条件：只有当 active 为 true 时才映射
        if (source.isActive()) {
            // 使用默认值
            if (source.getName() != null) {
                target.setName(source.getName());
            } else {
                target.setName("未知");
            }
        }
        
        assertEquals("未知", target.getName());
    }

    /**
     * 测试条件不满足时默认值不生效。
     */
    @Test
    public void testDefaultValueWithConditionNotMet() {
        TestSource source = new TestSource();
        source.setName(null);
        source.setActive(false);
        
        TestTarget target = new TestTarget();
        target.setName("原始值");
        
        if (source.isActive()) {
            if (source.getName() != null) {
                target.setName(source.getName());
            } else {
                target.setName("未知");
            }
        }
        
        assertEquals("原始值", target.getName()); // 保持原值
    }

    // ========== 更新对象 + 嵌套对象组合 ==========

    /**
     * 测试更新对象与嵌套对象组合。
     */
    @Test
    public void testUpdateWithNestedObject() {
        TestSource source = new TestSource();
        source.setName("新名称");
        
        AddressSource addressSource = new AddressSource();
        addressSource.setCity("北京");
        addressSource.setStreet(null); // null 值
        source.setAddress(addressSource);
        
        TestTarget target = new TestTarget();
        target.setName("原始名称");
        
        AddressTarget addressTarget = new AddressTarget();
        addressTarget.setCity("上海");
        addressTarget.setStreet("原始街道");
        target.setAddress(addressTarget);
        
        // 更新主对象
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        
        // 更新嵌套对象（IGNORE 策略）
        if (source.getAddress() != null) {
            if (target.getAddress() == null) {
                target.setAddress(new AddressTarget());
            }
            if (source.getAddress().getCity() != null) {
                target.getAddress().setCity(source.getAddress().getCity());
            }
            // street 为 null，不更新
        }
        
        assertEquals("新名称", target.getName());
        assertEquals("北京", target.getAddress().getCity());
        assertEquals("原始街道", target.getAddress().getStreet()); // 保持原值
    }

    // ========== 映射前回调 + 条件映射组合 ==========

    /**
     * 测试映射前回调与条件映射组合。
     */
    @Test
    public void testBeforeMappingWithCondition() {
        TestSource source = new TestSource();
        source.setName("  test  ");
        source.setActive(true);
        
        TestTarget target = new TestTarget();
        
        // 映射前回调：去除空格
        if (source.getName() != null) {
            source.setName(source.getName().trim());
        }
        
        // 条件映射
        if (source.isActive()) {
            target.setName(source.getName());
        }
        
        assertEquals("test", target.getName());
    }

    // ========== 辅助类 ==========

    private static class TestSource {
        private String name;
        private String firstName;
        private String lastName;
        private boolean active;
        private AddressSource address;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public AddressSource getAddress() { return address; }
        public void setAddress(AddressSource address) { this.address = address; }
    }

    private static class TestTarget {
        private String name;
        private String fullName;
        private AddressTarget address;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public AddressTarget getAddress() { return address; }
        public void setAddress(AddressTarget address) { this.address = address; }
    }

    private static class AddressSource {
        private String city;
        private String street;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }

    private static class AddressTarget {
        private String city;
        private String street;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getStreet() { return street; }
        public void setStreet(String street) { this.street = street; }
    }
}
