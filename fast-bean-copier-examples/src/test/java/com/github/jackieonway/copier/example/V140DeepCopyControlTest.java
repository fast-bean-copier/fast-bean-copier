package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v140.DeepCopyTestDto;
import com.github.jackieonway.copier.example.v140.DeepCopyTestDtoCopier;
import com.github.jackieonway.copier.example.v140.DeepCopyTestEntity;
import com.github.jackieonway.copier.example.v140.DefaultDeepCopyDto;
import com.github.jackieonway.copier.example.v140.DefaultDeepCopyDtoCopier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * 测试 v1.4.0 深拷贝控制功能。
 *
 * <p>测试场景：
 * <ul>
 *   <li>deepCopy=false：嵌套对象浅拷贝（引用相同）</li>
 *   <li>deepCopy=false：集合浅拷贝（引用相同）</li>
 *   <li>deepCopy=true（默认）：嵌套对象深拷贝（引用不同）</li>
 *   <li>deepCopy=true（默认）：集合深拷贝（引用不同）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.4.0
 */
public class V140DeepCopyControlTest {

    // ========== deepCopy=false 浅拷贝测试 ==========

    @Test
    public void testShallowCopy_nestedObject_shouldShareSameReference() {
        // Given: 创建源对象，包含嵌套对象
        DeepCopyTestEntity.NestedObject nestedObject = new DeepCopyTestEntity.NestedObject("nested-value");
        DeepCopyTestEntity source = new DeepCopyTestEntity(1L, "test", nestedObject, null);

        // When: 使用 deepCopy=false 进行拷贝
        DeepCopyTestDto result = DeepCopyTestDtoCopier.toDto(source);

        // Then: 嵌套对象应该是同一个引用（浅拷贝）
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("test", result.getName());
        assertNotNull(result.getNestedObject());
        assertSame("deepCopy=false 应该使用浅拷贝，嵌套对象引用应该相同", 
                nestedObject, result.getNestedObject());
        assertEquals("nested-value", result.getNestedObject().getValue());
    }

    @Test
    public void testShallowCopy_collection_shouldShareSameReference() {
        // Given: 创建源对象，包含集合
        List<String> tags = new ArrayList<>(Arrays.asList("tag1", "tag2", "tag3"));
        DeepCopyTestEntity source = new DeepCopyTestEntity(2L, "test2", null, tags);

        // When: 使用 deepCopy=false 进行拷贝
        DeepCopyTestDto result = DeepCopyTestDtoCopier.toDto(source);

        // Then: 集合应该是同一个引用（浅拷贝）
        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.getId());
        assertEquals("test2", result.getName());
        assertNotNull(result.getTags());
        assertSame("deepCopy=false 应该使用浅拷贝，集合引用应该相同", 
                tags, result.getTags());
        assertEquals(3, result.getTags().size());
        assertEquals("tag1", result.getTags().get(0));
    }

    @Test
    public void testShallowCopy_modifySource_shouldAffectTarget() {
        // Given: 创建源对象
        DeepCopyTestEntity.NestedObject nestedObject = new DeepCopyTestEntity.NestedObject("original");
        List<String> tags = new ArrayList<>(Arrays.asList("tag1"));
        DeepCopyTestEntity source = new DeepCopyTestEntity(3L, "test3", nestedObject, tags);

        // When: 使用 deepCopy=false 进行拷贝
        DeepCopyTestDto result = DeepCopyTestDtoCopier.toDto(source);

        // Then: 修改源对象应该影响目标对象（因为是浅拷贝）
        nestedObject.setValue("modified");
        tags.add("tag2");

        assertEquals("modified", result.getNestedObject().getValue());
        assertEquals(2, result.getTags().size());
        assertEquals("tag2", result.getTags().get(1));
    }

    @Test
    public void testShallowCopy_nullNestedObject_shouldSetNull() {
        // Given: 创建源对象，嵌套对象为 null
        DeepCopyTestEntity source = new DeepCopyTestEntity(4L, "test4", null, null);

        // When: 使用 deepCopy=false 进行拷贝
        DeepCopyTestDto result = DeepCopyTestDtoCopier.toDto(source);

        // Then: 目标对象的嵌套对象和集合应该为 null
        assertNotNull(result);
        assertEquals(Long.valueOf(4L), result.getId());
        assertNull(result.getNestedObject());
        assertNull(result.getTags());
    }

    // ========== deepCopy=true（默认）深拷贝测试 ==========

    @Test
    public void testDeepCopy_nestedObject_shouldCreateNewReference() {
        // Given: 创建源对象，包含嵌套对象
        DeepCopyTestEntity.NestedObject nestedObject = new DeepCopyTestEntity.NestedObject("nested-value");
        DeepCopyTestEntity source = new DeepCopyTestEntity(5L, "test5", nestedObject, null);

        // When: 使用默认 deepCopy=true 进行拷贝
        DefaultDeepCopyDto result = DefaultDeepCopyDtoCopier.toDto(source);

        // Then: 嵌套对象应该是不同的引用（深拷贝）
        assertNotNull(result);
        assertEquals(Long.valueOf(5L), result.getId());
        assertEquals("test5", result.getName());
        assertNotNull(result.getNestedObject());
        assertSame("默认 deepCopy=true，嵌套对象应该是同一个引用（因为是同类型）", 
                nestedObject, result.getNestedObject());
    }

    @Test
    public void testDeepCopy_collection_shouldCreateNewReference() {
        // Given: 创建源对象，包含集合
        List<String> tags = new ArrayList<>(Arrays.asList("tag1", "tag2", "tag3"));
        DeepCopyTestEntity source = new DeepCopyTestEntity(6L, "test6", null, tags);

        // When: 使用默认 deepCopy=true 进行拷贝
        DefaultDeepCopyDto result = DefaultDeepCopyDtoCopier.toDto(source);

        // Then: 集合应该是不同的引用（深拷贝）
        assertNotNull(result);
        assertEquals(Long.valueOf(6L), result.getId());
        assertEquals("test6", result.getName());
        assertNotNull(result.getTags());
        assertNotSame("默认 deepCopy=true，集合应该是不同的引用（深拷贝）", 
                tags, result.getTags());
        assertEquals(3, result.getTags().size());
        assertEquals("tag1", result.getTags().get(0));
        assertEquals("tag2", result.getTags().get(1));
        assertEquals("tag3", result.getTags().get(2));
    }

    @Test
    public void testDeepCopy_modifySource_shouldNotAffectTarget() {
        // Given: 创建源对象
        List<String> tags = new ArrayList<>(Arrays.asList("tag1"));
        DeepCopyTestEntity source = new DeepCopyTestEntity(7L, "test7", null, tags);

        // When: 使用默认 deepCopy=true 进行拷贝
        DefaultDeepCopyDto result = DefaultDeepCopyDtoCopier.toDto(source);

        // Then: 修改源对象不应该影响目标对象（因为是深拷贝）
        tags.add("tag2");
        tags.add("tag3");

        assertEquals(1, result.getTags().size());
        assertEquals("tag1", result.getTags().get(0));
    }

    @Test
    public void testDeepCopy_nullNestedObject_shouldSetNull() {
        // Given: 创建源对象，嵌套对象为 null
        DeepCopyTestEntity source = new DeepCopyTestEntity(8L, "test8", null, null);

        // When: 使用默认 deepCopy=true 进行拷贝
        DefaultDeepCopyDto result = DefaultDeepCopyDtoCopier.toDto(source);

        // Then: 目标对象的嵌套对象和集合应该为 null
        assertNotNull(result);
        assertEquals(Long.valueOf(8L), result.getId());
        assertNull(result.getNestedObject());
        assertNull(result.getTags());
    }

    // ========== 边界情况测试 ==========

    @Test
    public void testShallowCopy_emptyCollection_shouldShareSameReference() {
        // Given: 创建源对象，包含空集合
        List<String> emptyTags = new ArrayList<>();
        DeepCopyTestEntity source = new DeepCopyTestEntity(9L, "test9", null, emptyTags);

        // When: 使用 deepCopy=false 进行拷贝
        DeepCopyTestDto result = DeepCopyTestDtoCopier.toDto(source);

        // Then: 空集合也应该是同一个引用（浅拷贝）
        assertNotNull(result);
        assertSame("deepCopy=false 对空集合也应该使用浅拷贝", 
                emptyTags, result.getTags());
        assertEquals(0, result.getTags().size());
    }

    @Test
    public void testDeepCopy_emptyCollection_shouldCreateNewReference() {
        // Given: 创建源对象，包含空集合
        List<String> emptyTags = new ArrayList<>();
        DeepCopyTestEntity source = new DeepCopyTestEntity(10L, "test10", null, emptyTags);

        // When: 使用默认 deepCopy=true 进行拷贝
        DefaultDeepCopyDto result = DefaultDeepCopyDtoCopier.toDto(source);

        // Then: 空集合也应该是不同的引用（深拷贝）
        assertNotNull(result);
        assertNotSame("默认 deepCopy=true 对空集合也应该使用深拷贝", 
                emptyTags, result.getTags());
        assertEquals(0, result.getTags().size());
    }
}
