package com.github.jackieonway.copier.processor.config;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ConfigMerger 单元测试。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class ConfigMergerTest {

    private ConfigMerger merger;

    @Before
    public void setUp() {
        merger = new ConfigMerger();
    }

    // ========== componentModel 合并测试 ==========

    @Test
    public void testMergeComponentModel_classLevelOverridesAll() {
        // 类级别配置优先级最高
        String result = merger.mergeComponentModel("SPRING", "CDI", "JSR330");

        assertEquals("SPRING", result);
    }

    @Test
    public void testMergeComponentModel_packageLevelOverridesFile() {
        // 包级别配置覆盖配置文件
        String result = merger.mergeComponentModel(null, "CDI", "JSR330");

        assertEquals("CDI", result);
    }

    @Test
    public void testMergeComponentModel_fileOverridesDefault() {
        // 配置文件配置覆盖默认值
        String result = merger.mergeComponentModel(null, null, "JSR330");

        assertEquals("JSR330", result);
    }

    @Test
    public void testMergeComponentModel_allNull_usesDefault() {
        // 所有配置为 null 时使用默认值
        String result = merger.mergeComponentModel(null, null, null);

        assertEquals("DEFAULT", result);
    }

    @Test
    public void testMergeComponentModel_classLevelDefault_overridesFile() {
        // 类级别显式配置 DEFAULT 应该覆盖配置文件
        String result = merger.mergeComponentModel("DEFAULT", null, "SPRING");

        assertEquals("DEFAULT", result);
    }

    @Test
    public void testMergeComponentModel_emptyStrings_usesDefault() {
        // 空字符串应该被视为无效配置
        String result = merger.mergeComponentModel("", "", "");

        assertEquals("DEFAULT", result);
    }

    @Test
    public void testMergeComponentModel_partialOverride_classOnly() {
        // 只有类级别配置
        String result = merger.mergeComponentModel("CDI", null, null);

        assertEquals("CDI", result);
    }

    @Test
    public void testMergeComponentModel_partialOverride_packageOnly() {
        // 只有包级别配置
        String result = merger.mergeComponentModel(null, "SPRING", null);

        assertEquals("SPRING", result);
    }

    // ========== nullValueStrategy 合并测试 ==========

    @Test
    public void testMergeNullValueStrategy_classLevelOverridesAll() {
        // 类级别配置优先级最高
        String result = merger.mergeNullValueStrategy("IGNORE", "SET_NULL", "THROW_EXCEPTION");

        assertEquals("IGNORE", result);
    }

    @Test
    public void testMergeNullValueStrategy_packageLevelOverridesFile() {
        // 包级别配置覆盖配置文件
        String result = merger.mergeNullValueStrategy(null, "SET_NULL", "THROW_EXCEPTION");

        assertEquals("SET_NULL", result);
    }

    @Test
    public void testMergeNullValueStrategy_fileOverridesDefault() {
        // 配置文件配置覆盖默认值
        String result = merger.mergeNullValueStrategy(null, null, "THROW_EXCEPTION");

        assertEquals("THROW_EXCEPTION", result);
    }

    @Test
    public void testMergeNullValueStrategy_allNull_usesDefault() {
        // 所有配置为 null 时使用默认值
        String result = merger.mergeNullValueStrategy(null, null, null);

        assertEquals("IGNORE", result);
    }

    @Test
    public void testMergeNullValueStrategy_emptyStrings_usesDefault() {
        // 空字符串应该被视为无效配置
        String result = merger.mergeNullValueStrategy("", "", "");

        assertEquals("IGNORE", result);
    }

    @Test
    public void testMergeNullValueStrategy_partialOverride_classOnly() {
        // 只有类级别配置
        String result = merger.mergeNullValueStrategy("SET_NULL", null, null);

        assertEquals("SET_NULL", result);
    }

    @Test
    public void testMergeNullValueStrategy_partialOverride_packageOnly() {
        // 只有包级别配置
        String result = merger.mergeNullValueStrategy(null, "THROW_EXCEPTION", null);

        assertEquals("THROW_EXCEPTION", result);
    }

    // ========== 配置优先级验证测试 ==========

    @Test
    public void testConfigPriority_classBeatsPackage() {
        // 验证类级别 > 包级别
        String componentModel = merger.mergeComponentModel("SPRING", "CDI", null);
        String nullValueStrategy = merger.mergeNullValueStrategy("IGNORE", "SET_NULL", null);

        assertEquals("SPRING", componentModel);
        assertEquals("IGNORE", nullValueStrategy);
    }

    @Test
    public void testConfigPriority_packageBeatsFile() {
        // 验证包级别 > 配置文件
        String componentModel = merger.mergeComponentModel(null, "CDI", "JSR330");
        String nullValueStrategy = merger.mergeNullValueStrategy(null, "SET_NULL", "THROW_EXCEPTION");

        assertEquals("CDI", componentModel);
        assertEquals("SET_NULL", nullValueStrategy);
    }

    @Test
    public void testConfigPriority_fileBeatsDefault() {
        // 验证配置文件 > 默认值
        String componentModel = merger.mergeComponentModel(null, null, "JSR330");
        String nullValueStrategy = merger.mergeNullValueStrategy(null, null, "THROW_EXCEPTION");

        assertEquals("JSR330", componentModel);
        assertEquals("THROW_EXCEPTION", nullValueStrategy);
    }

    // ========== 混合场景测试 ==========

    @Test
    public void testMixedScenario_differentLevels() {
        // 测试不同配置项使用不同级别的配置
        // componentModel 使用类级别，nullValueStrategy 使用包级别
        String componentModel = merger.mergeComponentModel("SPRING", "CDI", "JSR330");
        String nullValueStrategy = merger.mergeNullValueStrategy(null, "SET_NULL", "THROW_EXCEPTION");

        assertEquals("SPRING", componentModel);
        assertEquals("SET_NULL", nullValueStrategy);
    }

    @Test
    public void testMixedScenario_partialConfiguration() {
        // 测试部分配置覆盖场景
        // componentModel 有类级别和文件级别，nullValueStrategy 只有文件级别
        String componentModel = merger.mergeComponentModel("DEFAULT", null, "SPRING");
        String nullValueStrategy = merger.mergeNullValueStrategy(null, null, "IGNORE");

        assertEquals("DEFAULT", componentModel);
        assertEquals("IGNORE", nullValueStrategy);
    }
    @Test
    public void testMergeCycleDetection_classLevelOverridesAll() {
        String result = merger.mergeCycleDetection("AUTOMATIC_CACHE", "RETURN_NULL", "FAIL_FAST");

        assertEquals("AUTOMATIC_CACHE", result);
    }

    @Test
    public void testMergeCycleDetection_packageLevelOverridesFile() {
        String result = merger.mergeCycleDetection(null, "RETURN_NULL", "FAIL_FAST");

        assertEquals("RETURN_NULL", result);
    }

    @Test
    public void testMergeCycleDetection_fileOverridesDefault() {
        String result = merger.mergeCycleDetection(null, null, "AUTOMATIC_CACHE");

        assertEquals("AUTOMATIC_CACHE", result);
    }

    @Test
    public void testMergeCycleDetection_allNull_usesDefault() {
        String result = merger.mergeCycleDetection(null, null, null);

        assertEquals("FAIL_FAST", result);
    }
}
