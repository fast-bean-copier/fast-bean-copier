package com.github.jackieonway.copier.processor.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CopyFieldConfig 单元测试。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class CopyFieldConfigTest {

    // ========== 构造器测试 ==========

    @Test
    public void constructor_withAllValues_shouldSetAllFields() {
        String[] sourceNames = {"field1", "field2"};
        CopyFieldConfig config = new CopyFieldConfig(
                sourceNames,
                "targetField",
                "source.getValue()",
                "convertMethod",
                "yyyy-MM-dd",
                "com.example.MyConverter"
        );

        assertArrayEquals(sourceNames, config.getSourceNames());
        assertEquals("targetField", config.getTarget());
        assertEquals("source.getValue()", config.getExpression());
        assertEquals("convertMethod", config.getQualifiedByName());
        assertEquals("yyyy-MM-dd", config.getFormat());
        assertEquals("com.example.MyConverter", config.getConverterClassName());
    }

    @Test
    public void constructor_withNullSourceNames_shouldSetEmptyArray() {
        CopyFieldConfig config = new CopyFieldConfig(
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertNotNull(config.getSourceNames());
        assertEquals(0, config.getSourceNames().length);
    }

    // ========== hasSourceNames 测试 ==========

    @Test
    public void hasSourceNames_withValues_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1"},
                null, null, null, null, null
        );

        assertTrue(config.hasSourceNames());
    }

    @Test
    public void hasSourceNames_withEmptyArray_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{},
                null, null, null, null, null
        );

        assertFalse(config.hasSourceNames());
    }

    @Test
    public void hasSourceNames_withNull_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null,
                null, null, null, null, null
        );

        assertFalse(config.hasSourceNames());
    }

    // ========== hasTarget 测试 ==========

    @Test
    public void hasTarget_withValue_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, "targetField", null, null, null, null
        );

        assertTrue(config.hasTarget());
    }

    @Test
    public void hasTarget_withEmpty_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, "", null, null, null, null
        );

        assertFalse(config.hasTarget());
    }

    @Test
    public void hasTarget_withBlank_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, "   ", null, null, null, null
        );

        assertFalse(config.hasTarget());
    }

    @Test
    public void hasTarget_withNull_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, null, null
        );

        assertFalse(config.hasTarget());
    }

    // ========== hasExpression 测试 ==========

    @Test
    public void hasExpression_withValue_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, "source.getValue()", null, null, null
        );

        assertTrue(config.hasExpression());
    }

    @Test
    public void hasExpression_withEmpty_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, "", null, null, null
        );

        assertFalse(config.hasExpression());
    }

    @Test
    public void hasExpression_withBlank_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, "   ", null, null, null
        );

        assertFalse(config.hasExpression());
    }

    // ========== hasQualifiedByName 测试 ==========

    @Test
    public void hasQualifiedByName_withValue_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, "convertMethod", null, null
        );

        assertTrue(config.hasQualifiedByName());
    }

    @Test
    public void hasQualifiedByName_withEmpty_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, "", null, null
        );

        assertFalse(config.hasQualifiedByName());
    }

    // ========== hasFormat 测试 ==========

    @Test
    public void hasFormat_withValue_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, "yyyy-MM-dd", null
        );

        assertTrue(config.hasFormat());
    }

    @Test
    public void hasFormat_withEmpty_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, "", null
        );

        assertFalse(config.hasFormat());
    }

    // ========== hasConverter 测试 ==========

    @Test
    public void hasConverter_withValue_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, null, "com.example.MyConverter"
        );

        assertTrue(config.hasConverter());
    }

    @Test
    public void hasConverter_withEmpty_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, null, ""
        );

        assertFalse(config.hasConverter());
    }

    @Test
    public void hasConverter_withNull_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                null, null, null, null, null, null
        );

        assertFalse(config.hasConverter());
    }

    // ========== isManyToOne 测试 ==========

    @Test
    public void isManyToOne_withMultipleSources_shouldReturnTrue() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1", "field2"},
                null, null, null, null, null
        );

        assertTrue(config.isManyToOne());
    }

    @Test
    public void isManyToOne_withSingleSource_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1"},
                null, null, null, null, null
        );

        assertFalse(config.isManyToOne());
    }

    @Test
    public void isManyToOne_withEmptyArray_shouldReturnFalse() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{},
                null, null, null, null, null
        );

        assertFalse(config.isManyToOne());
    }

    // ========== getFirstSourceName 测试 ==========

    @Test
    public void getFirstSourceName_withValues_shouldReturnFirst() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1", "field2"},
                null, null, null, null, null
        );

        assertEquals("field1", config.getFirstSourceName());
    }

    @Test
    public void getFirstSourceName_withEmptyArray_shouldReturnNull() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{},
                null, null, null, null, null
        );

        assertNull(config.getFirstSourceName());
    }

    @Test
    public void getFirstSourceName_withNull_shouldReturnNull() {
        CopyFieldConfig config = new CopyFieldConfig(
                null,
                null, null, null, null, null
        );

        assertNull(config.getFirstSourceName());
    }

    // ========== toString 测试 ==========

    @Test
    public void toString_shouldContainAllFields() {
        CopyFieldConfig config = new CopyFieldConfig(
                new String[]{"field1"},
                "targetField",
                "source.getValue()",
                "convertMethod",
                "yyyy-MM-dd",
                "com.example.MyConverter"
        );

        String result = config.toString();

        assertTrue(result.contains("field1"));
        assertTrue(result.contains("targetField"));
        assertTrue(result.contains("source.getValue()"));
        assertTrue(result.contains("convertMethod"));
        assertTrue(result.contains("yyyy-MM-dd"));
        assertTrue(result.contains("com.example.MyConverter"));
    }
}
