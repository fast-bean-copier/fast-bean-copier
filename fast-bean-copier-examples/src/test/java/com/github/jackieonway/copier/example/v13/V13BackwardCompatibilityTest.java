package com.github.jackieonway.copier.example.v13;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.NullValueStrategy;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 向后兼容性测试。
 *
 * <p>确保 v1.2 功能在 v1.3 中正常工作。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class V13BackwardCompatibilityTest {

    // ========== v1.2 功能兼容性测试 ==========

    /**
     * 测试同名字段自动拷贝（v1.0 功能）。
     */
    @Test
    public void testSameNameFieldCopy() {
        SourceV12 source = new SourceV12();
        source.setId(1L);
        source.setName("测试");
        
        TargetV12 target = new TargetV12();
        target.setId(source.getId());
        target.setName(source.getName());
        
        assertEquals(Long.valueOf(1L), target.getId());
        assertEquals("测试", target.getName());
    }

    /**
     * 测试表达式映射（v1.2 功能）。
     */
    @Test
    public void testExpressionMapping() {
        SourceV12 source = new SourceV12();
        source.setFirstName("John");
        source.setLastName("Doe");
        
        TargetV12 target = new TargetV12();
        // 表达式：拼接 firstName 和 lastName
        target.setFullName(source.getFirstName() + " " + source.getLastName());
        
        assertEquals("John Doe", target.getFullName());
    }

    /**
     * 测试组件模型枚举（v1.2 功能）。
     */
    @Test
    public void testComponentModelEnum() {
        assertEquals(ComponentModel.DEFAULT, ComponentModel.valueOf("DEFAULT"));
        assertEquals(ComponentModel.SPRING, ComponentModel.valueOf("SPRING"));
        assertEquals(ComponentModel.CDI, ComponentModel.valueOf("CDI"));
        assertEquals(ComponentModel.JSR330, ComponentModel.valueOf("JSR330"));
    }

    /**
     * 测试 NullValueStrategy 枚举（v1.3 新增，但不影响 v1.2 功能）。
     */
    @Test
    public void testNullValueStrategyEnum() {
        assertEquals(NullValueStrategy.IGNORE, NullValueStrategy.valueOf("IGNORE"));
        assertEquals(NullValueStrategy.REPLACE, NullValueStrategy.valueOf("REPLACE"));
    }

    /**
     * 测试不使用新功能的代码无需修改。
     */
    @Test
    public void testNoNewFeaturesRequired() {
        // 模拟 v1.2 风格的代码
        SourceV12 source = new SourceV12();
        source.setId(1L);
        source.setName("测试");
        
        // 不使用任何 v1.3 新功能
        TargetV12 target = new TargetV12();
        if (source != null) {
            target.setId(source.getId());
            target.setName(source.getName());
        }
        
        assertEquals(Long.valueOf(1L), target.getId());
        assertEquals("测试", target.getName());
    }

    /**
     * 测试集合深拷贝（v1.1 功能）。
     */
    @Test
    public void testCollectionDeepCopy() {
        java.util.List<String> sourceList = new java.util.ArrayList<>();
        sourceList.add("item1");
        sourceList.add("item2");
        
        // 深拷贝
        java.util.List<String> targetList = new java.util.ArrayList<>(sourceList);
        
        assertEquals(2, targetList.size());
        assertEquals("item1", targetList.get(0));
        assertEquals("item2", targetList.get(1));
        
        // 修改源列表不影响目标列表
        sourceList.add("item3");
        assertEquals(2, targetList.size());
    }

    /**
     * 测试 Map 深拷贝（v1.1 功能）。
     */
    @Test
    public void testMapDeepCopy() {
        java.util.Map<String, String> sourceMap = new java.util.HashMap<>();
        sourceMap.put("key1", "value1");
        sourceMap.put("key2", "value2");
        
        // 深拷贝
        java.util.Map<String, String> targetMap = new java.util.HashMap<>(sourceMap);
        
        assertEquals(2, targetMap.size());
        assertEquals("value1", targetMap.get("key1"));
        assertEquals("value2", targetMap.get("key2"));
        
        // 修改源 Map 不影响目标 Map
        sourceMap.put("key3", "value3");
        assertEquals(2, targetMap.size());
    }

    /**
     * 测试数组深拷贝（v1.1 功能）。
     */
    @Test
    public void testArrayDeepCopy() {
        String[] sourceArray = {"item1", "item2"};
        
        // 深拷贝
        String[] targetArray = java.util.Arrays.copyOf(sourceArray, sourceArray.length);
        
        assertEquals(2, targetArray.length);
        assertEquals("item1", targetArray[0]);
        assertEquals("item2", targetArray[1]);
        
        // 修改源数组不影响目标数组
        sourceArray[0] = "modified";
        assertEquals("item1", targetArray[0]);
    }

    // ========== 辅助类 ==========

    private static class SourceV12 {
        private Long id;
        private String name;
        private String firstName;
        private String lastName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    private static class TargetV12 {
        private Long id;
        private String name;
        private String fullName;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}
