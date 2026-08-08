package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v150.MapUserDto;
import com.github.jackieonway.copier.example.v150.MapUserDtoMapCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * v1.6.0 Bean <-> Map BiFunction postProcessor coverage.
 */
public class V160MapBiFunctionCallbackTest {

    @Test
    public void toMapPostProcessorReadsSourceAndResult() {
        MapUserDto source = user(1L, "Alice");
        BiFunction<MapUserDto, Map<String, Object>, Map<String, Object>> post = (s, result) -> {
            result.put("label", s.getName() + "_" + result.get("id"));
            return result;
        };

        Map<String, Object> result = MapUserDtoMapCopier.toMap(source, null, post);

        assertNotNull(result);
        assertEquals("Alice_1", result.get("label"));
    }

    @Test
    public void fromMapPostProcessorReadsSourceAndResult() {
        Map<String, Object> source = map(1L, "Alice");
        BiFunction<Map<String, Object>, MapUserDto, MapUserDto> post = (s, result) -> {
            result.setName(s.get("name") + "_" + result.getId());
            return result;
        };

        MapUserDto result = MapUserDtoMapCopier.fromMap(source, null, post);

        assertNotNull(result);
        assertEquals("Alice_1", result.getName());
    }

    @Test
    public void toMapListPostProcessorReadsSourcesAndResult() {
        List<MapUserDto> sources = Arrays.asList(user(1L, "Alice"), user(2L, "Bob"));
        BiFunction<List<MapUserDto>, List<Map<String, Object>>, List<Map<String, Object>>> post =
                (input, result) -> result.stream()
                        .filter(m -> input.get(1).getId().equals(m.get("id")))
                        .collect(Collectors.toList());

        List<Map<String, Object>> result = MapUserDtoMapCopier.toMapList(sources, null, post);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).get("id"));
    }

    @Test
    public void fromMapListPostProcessorReadsSourcesAndResult() {
        List<Map<String, Object>> sources = Arrays.asList(map(1L, "Alice"), map(2L, "Bob"));
        BiFunction<List<Map<String, Object>>, List<MapUserDto>, List<MapUserDto>> post =
                (input, result) -> result.stream()
                        .filter(dto -> input.get(1).get("id").equals(dto.getId()))
                        .collect(Collectors.toList());

        List<MapUserDto> result = MapUserDtoMapCopier.fromMapList(sources, null, post);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
    }

    @Test
    public void toMapSetPostProcessorReadsSourcesAndResult() {
        Set<MapUserDto> sources = new LinkedHashSet<>(Arrays.asList(user(1L, "Alice")));
        BiFunction<Set<MapUserDto>, Set<Map<String, Object>>, Set<Map<String, Object>>> post =
                (input, result) -> {
                    result.iterator().next().put("sourceSize", input.size());
                    return result;
                };

        Set<Map<String, Object>> result = MapUserDtoMapCopier.toMapSet(sources, null, post);

        assertNotNull(result);
        assertEquals(1, result.iterator().next().get("sourceSize"));
    }

    @Test
    public void fromMapSetPostProcessorReadsSourcesAndResult() {
        Set<Map<String, Object>> sources = new LinkedHashSet<>(Arrays.asList(map(1L, "Alice")));
        BiFunction<Set<Map<String, Object>>, Set<MapUserDto>, Set<MapUserDto>> post =
                (input, result) -> {
                    result.iterator().next().setName(result.iterator().next().getName() + "_" + input.size());
                    return result;
                };

        Set<MapUserDto> result = MapUserDtoMapCopier.fromMapSet(sources, null, post);

        assertNotNull(result);
        assertEquals("Alice_1", result.iterator().next().getName());
    }

    private static MapUserDto user(Long id, String name) {
        MapUserDto dto = new MapUserDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    private static Map<String, Object> map(Long id, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        return map;
    }
}
