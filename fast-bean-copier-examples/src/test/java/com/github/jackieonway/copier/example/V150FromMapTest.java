package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v150.IgnoreFieldDto;
import com.github.jackieonway.copier.example.v150.IgnoreFieldDtoMapCopier;
import com.github.jackieonway.copier.example.v150.MapUserDto;
import com.github.jackieonway.copier.example.v150.MapUserDtoMapCopier;
import com.github.jackieonway.copier.example.v150.SnakeCaseDto;
import com.github.jackieonway.copier.example.v150.SnakeCaseDtoMapCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * v1.5.0 Map → Bean 转换测试。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class V150FromMapTest {

    // ========== 基础 fromMap 转换 ==========

    @Test
    public void testFromMap_basicConversion() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "Alice");
        map.put("age", 30);
        map.put("userEmail", "alice@example.com");

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Alice", result.getName());
        assertEquals(Integer.valueOf(30), result.getAge());
        assertEquals("alice@example.com", result.getEmail());
    }

    @Test
    public void testFromMap_nullSource_returnsNull() {
        assertNull(MapUserDtoMapCopier.fromMap(null));
    }

    @Test
    public void testFromMap_missingKey_fieldIsNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 2L);
        // name, age, email 不在 map 中

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.getId());
        assertNull(result.getName());
        assertNull(result.getAge());
    }

    // ========== 类型转换 ==========

    @Test
    public void testFromMap_typeConversion_stringToLong() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "42");  // String -> Long
        map.put("name", "Bob");

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(42L), result.getId());
    }

    @Test
    public void testFromMap_typeConversion_stringToInteger() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("age", "25");  // String -> Integer

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Integer.valueOf(25), result.getAge());
    }

    // ========== mapKey 优先级 ==========

    @Test
    public void testFromMap_mapKey_usedAsKey() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("userEmail", "test@example.com");  // mapKey = "userEmail"

        MapUserDto result = MapUserDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    // ========== SNAKE_CASE keyStrategy ==========

    @Test
    public void testFromMap_snakeCaseStrategy() {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", 1L);
        map.put("first_name", "John");
        map.put("last_name", "Doe");

        SnakeCaseDto result = SnakeCaseDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
    }

    // ========== ignore 字段不参与转换 ==========

    @Test
    public void testFromMap_ignoreField_notSet() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("username", "bob");
        map.put("password", "should_be_ignored");

        IgnoreFieldDto result = IgnoreFieldDtoMapCopier.fromMap(map);

        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("bob", result.getUsername());
        assertNull("password 应被忽略", result.getPassword());
    }

    // ========== 函数式 preProcessor/postProcessor ==========

    @Test
    public void testFromMap_withPreProcessor_modifiesMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "original");

        UnaryOperator<Map<String, Object>> pre = m -> {
            m.put("name", "modified");
            return m;
        };

        MapUserDto result = MapUserDtoMapCopier.fromMap(map, pre, null);

        assertNotNull(result);
        assertEquals("modified", result.getName());
    }

    @Test
    public void testFromMap_withPostProcessor_modifiesBean() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);
        map.put("name", "Alice");

        UnaryOperator<MapUserDto> post = dto -> { dto.setName("post_" + dto.getName()); return dto; };

        MapUserDto result = MapUserDtoMapCopier.fromMap(map, null, post);

        assertNotNull(result);
        assertEquals("post_Alice", result.getName());
    }

    @Test
    public void testFromMap_preProcessorReturnsNull_returnsNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", 1L);

        MapUserDto result = MapUserDtoMapCopier.fromMap(map, m -> null, null);

        assertNull(result);
    }

    // ========== 批量 fromMapList ==========

    @Test
    public void testFromMapList_basicConversion() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1L); m1.put("name", "Alice");
        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2L); m2.put("name", "Bob");

        List<MapUserDto> result = MapUserDtoMapCopier.fromMapList(Arrays.asList(m1, m2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Long.valueOf(1L), result.get(0).getId());
        assertEquals(Long.valueOf(2L), result.get(1).getId());
    }

    @Test
    public void testFromMapList_nullSources_returnsNull() {
        assertNull(MapUserDtoMapCopier.fromMapList(null));
    }

    @Test
    public void testFromMapList_withProcessors_filtersResult() {
        Map<String, Object> m1 = new HashMap<>();
        m1.put("id", 1L); m1.put("name", "Alice");
        Map<String, Object> m2 = new HashMap<>();
        m2.put("id", 2L); m2.put("name", "Bob");

        UnaryOperator<List<MapUserDto>> post = list ->
                list.stream().filter(d -> Long.valueOf(2L).equals(d.getId())).collect(Collectors.toList());

        List<MapUserDto> result = MapUserDtoMapCopier.fromMapList(Arrays.asList(m1, m2), null, post);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
    }

    // ========== 批量 fromMapSet ==========

    @Test
    public void testFromMapSet_basicConversion() {
        Set<Map<String, Object>> sources = new LinkedHashSet<>();
        Map<String, Object> m = new HashMap<>();
        m.put("id", 1L); m.put("name", "Alice");
        sources.add(m);

        Set<MapUserDto> result = MapUserDtoMapCopier.fromMapSet(sources);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.iterator().next().getId());
    }

    @Test
    public void testFromMapSet_nullSources_returnsNull() {
        assertNull(MapUserDtoMapCopier.fromMapSet(null));
    }
}
