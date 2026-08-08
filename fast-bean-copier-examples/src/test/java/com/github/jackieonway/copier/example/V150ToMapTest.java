package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v150.IgnoreFieldDto;
import com.github.jackieonway.copier.example.v150.IgnoreFieldDtoMapCopier;
import com.github.jackieonway.copier.example.v150.MapUserDto;
import com.github.jackieonway.copier.example.v150.MapUserDtoMapCopier;
import com.github.jackieonway.copier.example.v150.SnakeCaseDto;
import com.github.jackieonway.copier.example.v150.SnakeCaseDtoMapCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * v1.5.0 Bean → Map 转换测试。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class V150ToMapTest {

    // ========== 基础 toMap 转换 ==========

    @Test
    public void testToMap_basicConversion() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("Alice");
        dto.setAge(30);
        dto.setEmail("alice@example.com");

        Map<String, Object> map = MapUserDtoMapCopier.toMap(dto);

        assertNotNull(map);
        assertEquals(1L, map.get("id"));
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
        // mapKey = "userEmail"
        assertEquals("alice@example.com", map.get("userEmail"));
        assertFalse("原字段名 email 不应存在", map.containsKey("email"));
    }

    @Test
    public void testToMap_nullSource_returnsNull() {
        assertNull(MapUserDtoMapCopier.toMap(null));
    }

    // ========== mapKey 优先级最高 ==========

    @Test
    public void testToMap_mapKey_overridesFieldName() {
        MapUserDto dto = new MapUserDto();
        dto.setEmail("test@example.com");

        Map<String, Object> map = MapUserDtoMapCopier.toMap(dto);

        assertTrue("mapKey 'userEmail' 应存在", map.containsKey("userEmail"));
        assertFalse("原字段名 'email' 不应存在", map.containsKey("email"));
    }

    // ========== SNAKE_CASE keyStrategy ==========

    @Test
    public void testToMap_snakeCaseStrategy() {
        SnakeCaseDto dto = new SnakeCaseDto(1L, "John", "Doe");

        Map<String, Object> map = SnakeCaseDtoMapCopier.toMap(dto);

        assertNotNull(map);
        assertEquals(1L, map.get("user_id"));
        assertEquals("John", map.get("first_name"));
        assertEquals("Doe", map.get("last_name"));
    }

    // ========== ignore 字段不参与转换 ==========

    @Test
    public void testToMap_ignoreField_notInMap() {
        IgnoreFieldDto dto = new IgnoreFieldDto(1L, "bob", "secret123");

        Map<String, Object> map = IgnoreFieldDtoMapCopier.toMap(dto);

        assertNotNull(map);
        assertEquals(1L, map.get("id"));
        assertEquals("bob", map.get("username"));
        assertFalse("password 应被忽略", map.containsKey("password"));
    }

    // ========== 函数式 preProcessor/postProcessor ==========

    @Test
    public void testToMap_withPostProcessor_modifiesMap() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("Alice");

        BiFunction<MapUserDto, Map<String, Object>, Map<String, Object>> post = (source, map) -> {
            map.put("extra", "added");
            map.put("sourceName", source.getName());
            return map;
        };

        Map<String, Object> result = MapUserDtoMapCopier.toMap(dto, null, post);

        assertNotNull(result);
        assertEquals("added", result.get("extra"));
        assertEquals("Alice", result.get("sourceName"));
        assertEquals(1L, result.get("id"));
    }

    @Test
    public void testToMap_withPreProcessor_modifiesSource() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("original");

        UnaryOperator<MapUserDto> pre = d -> { d.setName("modified"); return d; };

        Map<String, Object> result = MapUserDtoMapCopier.toMap(dto, pre, null);

        assertNotNull(result);
        assertEquals("modified", result.get("name"));
    }

    @Test
    public void testToMap_preProcessorReturnsNull_returnsNull() {
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);

        Map<String, Object> result = MapUserDtoMapCopier.toMap(dto, d -> null, null);

        assertNull(result);
    }

    // ========== 批量 toMapList ==========

    @Test
    public void testToMapList_basicConversion() {
        MapUserDto dto1 = new MapUserDto();
        dto1.setId(1L); dto1.setName("Alice");
        MapUserDto dto2 = new MapUserDto();
        dto2.setId(2L); dto2.setName("Bob");

        List<Map<String, Object>> result = MapUserDtoMapCopier.toMapList(Arrays.asList(dto1, dto2));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals(2L, result.get(1).get("id"));
    }

    @Test
    public void testToMapList_nullSources_returnsNull() {
        assertNull(MapUserDtoMapCopier.toMapList(null));
    }

    @Test
    public void testToMapList_withProcessors_filtersResult() {
        MapUserDto dto1 = new MapUserDto();
        dto1.setId(1L); dto1.setName("Alice");
        MapUserDto dto2 = new MapUserDto();
        dto2.setId(2L); dto2.setName("Bob");

        BiFunction<List<MapUserDto>, List<Map<String, Object>>, List<Map<String, Object>>> post = (sources, list) ->
                list.stream()
                        .filter(m -> Long.valueOf(sources.get(1).getId()).equals(m.get("id")))
                        .collect(Collectors.toList());

        List<Map<String, Object>> result = MapUserDtoMapCopier.toMapList(
                Arrays.asList(dto1, dto2), null, post);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).get("id"));
    }

    // ========== 批量 toMapSet ==========

    @Test
    public void testToMapSet_basicConversion() {
        Set<MapUserDto> sources = new LinkedHashSet<>();
        MapUserDto dto = new MapUserDto();
        dto.setId(1L); dto.setName("Alice");
        sources.add(dto);

        Set<Map<String, Object>> result = MapUserDtoMapCopier.toMapSet(sources);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.iterator().next().get("id"));
    }

    @Test
    public void testToMapSet_nullSources_returnsNull() {
        assertNull(MapUserDtoMapCopier.toMapSet(null));
    }

    @Test
    public void testToMapSet_withProcessors_readsSourcesAndResult() {
        Set<MapUserDto> sources = new LinkedHashSet<>();
        MapUserDto dto = new MapUserDto();
        dto.setId(1L);
        dto.setName("Alice");
        sources.add(dto);

        BiFunction<Set<MapUserDto>, Set<Map<String, Object>>, Set<Map<String, Object>>> post = (input, result) -> {
            result.iterator().next().put("sourceSize", input.size());
            return result;
        };

        Set<Map<String, Object>> result = MapUserDtoMapCopier.toMapSet(sources, null, post);

        assertNotNull(result);
        assertEquals(1, result.iterator().next().get("sourceSize"));
    }
}
