package com.github.jackieonway.copier.example.v13;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * v1.3 性能基准测试。
 *
 * <p>测试 v1.3 新功能的性能表现。
 *
 * @author jackieonway
 * @since 1.3.0
 */
public class V13PerformanceBenchmarkTest {

    private static final int ITERATIONS = 10000;
    private static final int WARMUP_ITERATIONS = 1000;

    // ========== 更新现有对象性能测试 ==========

    /**
     * 测试 updateDto 方法性能。
     */
    @Test
    public void testUpdateDtoPerformance() {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performUpdate();
        }

        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            performUpdate();
        }
        long endTime = System.nanoTime();

        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeNs = (double) (endTime - startTime) / ITERATIONS;

        System.out.println("Update performance: " + ITERATIONS + " iterations in " + totalTimeMs + "ms");
        System.out.println("Average time per update: " + avgTimeNs + "ns");

        // 性能断言：平均每次更新应小于 1ms
        assertTrue("Update should be fast", avgTimeNs < 1_000_000);
    }

    /**
     * 测试 toDto 方法性能（对比基准）。
     */
    @Test
    public void testToDtoPerformance() {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performToDto();
        }

        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            performToDto();
        }
        long endTime = System.nanoTime();

        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeNs = (double) (endTime - startTime) / ITERATIONS;

        System.out.println("ToDto performance: " + ITERATIONS + " iterations in " + totalTimeMs + "ms");
        System.out.println("Average time per toDto: " + avgTimeNs + "ns");

        // 性能断言：平均每次转换应小于 1ms
        assertTrue("ToDto should be fast", avgTimeNs < 1_000_000);
    }

    // ========== 条件映射性能测试 ==========

    /**
     * 测试条件映射性能。
     */
    @Test
    public void testConditionalMappingPerformance() {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performConditionalMapping();
        }

        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            performConditionalMapping();
        }
        long endTime = System.nanoTime();

        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeNs = (double) (endTime - startTime) / ITERATIONS;

        System.out.println("Conditional mapping performance: " + ITERATIONS + " iterations in " + totalTimeMs + "ms");
        System.out.println("Average time per conditional mapping: " + avgTimeNs + "ns");

        // 性能断言：条件映射应该很快
        assertTrue("Conditional mapping should be fast", avgTimeNs < 1_000_000);
    }

    /**
     * 测试无条件映射性能（对比基准）。
     */
    @Test
    public void testUnconditionalMappingPerformance() {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performUnconditionalMapping();
        }

        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            performUnconditionalMapping();
        }
        long endTime = System.nanoTime();

        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeNs = (double) (endTime - startTime) / ITERATIONS;

        System.out.println("Unconditional mapping performance: " + ITERATIONS + " iterations in " + totalTimeMs + "ms");
        System.out.println("Average time per unconditional mapping: " + avgTimeNs + "ns");

        // 性能断言
        assertTrue("Unconditional mapping should be fast", avgTimeNs < 1_000_000);
    }

    // ========== 大量字段更新性能测试 ==========

    /**
     * 测试大量字段更新性能。
     */
    @Test
    public void testManyFieldsUpdatePerformance() {
        // 预热
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            performManyFieldsUpdate();
        }

        // 性能测试
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            performManyFieldsUpdate();
        }
        long endTime = System.nanoTime();

        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeNs = (double) (endTime - startTime) / ITERATIONS;

        System.out.println("Many fields update performance: " + ITERATIONS + " iterations in " + totalTimeMs + "ms");
        System.out.println("Average time per many fields update: " + avgTimeNs + "ns");

        // 性能断言
        assertTrue("Many fields update should be fast", avgTimeNs < 1_000_000);
    }

    // ========== 辅助方法 ==========

    private void performUpdate() {
        TestTarget target = new TestTarget();
        target.setName("原始");
        target.setAge(25);

        TestSource source = new TestSource();
        source.setName("新值");
        source.setAge(30);

        // 模拟 updateDto
        if (source.getName() != null) {
            target.setName(source.getName());
        }
        if (source.getAge() != null) {
            target.setAge(source.getAge());
        }
    }

    private void performToDto() {
        TestSource source = new TestSource();
        source.setName("测试");
        source.setAge(25);

        // 模拟 toDto
        TestTarget target = new TestTarget();
        target.setName(source.getName());
        target.setAge(source.getAge());
    }

    private void performConditionalMapping() {
        TestSource source = new TestSource();
        source.setName("测试");
        source.setAge(25);

        TestTarget target = new TestTarget();

        // 条件映射
        if (source.getAge() != null && source.getAge() >= 18) {
            target.setName(source.getName());
        }
        target.setAge(source.getAge());
    }

    private void performUnconditionalMapping() {
        TestSource source = new TestSource();
        source.setName("测试");
        source.setAge(25);

        TestTarget target = new TestTarget();
        target.setName(source.getName());
        target.setAge(source.getAge());
    }

    private void performManyFieldsUpdate() {
        ManyFieldsTarget target = new ManyFieldsTarget();
        target.setField1("原始1");
        target.setField2("原始2");
        target.setField3("原始3");
        target.setField4("原始4");
        target.setField5("原始5");

        ManyFieldsSource source = new ManyFieldsSource();
        source.setField1("新值1");
        source.setField2(null);
        source.setField3("新值3");
        source.setField4(null);
        source.setField5("新值5");

        // 模拟 IGNORE 策略更新
        if (source.getField1() != null) target.setField1(source.getField1());
        if (source.getField2() != null) target.setField2(source.getField2());
        if (source.getField3() != null) target.setField3(source.getField3());
        if (source.getField4() != null) target.setField4(source.getField4());
        if (source.getField5() != null) target.setField5(source.getField5());
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

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    private static class ManyFieldsSource {
        private String field1, field2, field3, field4, field5;

        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public String getField2() { return field2; }
        public void setField2(String field2) { this.field2 = field2; }
        public String getField3() { return field3; }
        public void setField3(String field3) { this.field3 = field3; }
        public String getField4() { return field4; }
        public void setField4(String field4) { this.field4 = field4; }
        public String getField5() { return field5; }
        public void setField5(String field5) { this.field5 = field5; }
    }

    private static class ManyFieldsTarget {
        private String field1, field2, field3, field4, field5;

        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public String getField2() { return field2; }
        public void setField2(String field2) { this.field2 = field2; }
        public String getField3() { return field3; }
        public void setField3(String field3) { this.field3 = field3; }
        public String getField4() { return field4; }
        public void setField4(String field4) { this.field4 = field4; }
        public String getField5() { return field5; }
        public void setField5(String field5) { this.field5 = field5; }
    }
}
