package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v131.SimpleProduct;
import com.github.jackieonway.copier.example.v131.SimpleProductDto;
import com.github.jackieonway.copier.example.v131.SimpleProductDtoCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
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
 * v1.5.0 API 清理回归测试。
 *
 * <p>验证 beforeMapping 移除和单参数 UnaryOperator 重载移除后，
 * Bean ↔ Bean 功能完全正常。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class V150ApiCleanupRegressionTest {

    // ========== Bean ↔ Bean 基础转换 ==========

    @Test
    public void testToDto_basicConversion_works() {
        SimpleProduct source = new SimpleProduct(1L, "Product 1", 99.9);
        SimpleProductDto result = SimpleProductDtoCopier.toDto(source);
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals("Product 1", result.getName());
        assertEquals(99.9, result.getPrice(), 0.001);
    }

    @Test
    public void testFromDto_basicConversion_works() {
        SimpleProductDto source = new SimpleProductDto();
        source.setId(2L);
        source.setName("Product 2");
        source.setPrice(49.9);
        SimpleProduct result = SimpleProductDtoCopier.fromDto(source);
        assertNotNull(result);
        assertEquals(Long.valueOf(2L), result.getId());
        assertEquals("Product 2", result.getName());
        assertEquals(49.9, result.getPrice(), 0.001);
    }

    @Test
    public void testToDto_nullSource_returnsNull() {
        assertNull(SimpleProductDtoCopier.toDto(null));
    }

    @Test
    public void testFromDto_nullSource_returnsNull() {
        assertNull(SimpleProductDtoCopier.fromDto(null));
    }

    // ========== 双参数 preProcessor/postProcessor ==========

    @Test
    public void testToDto_withPreProcessor_modifiesSource() {
        SimpleProduct source = new SimpleProduct(1L, "original", 10.0);
        UnaryOperator<SimpleProduct> pre = s -> { s.setName("modified"); return s; };
        SimpleProductDto result = SimpleProductDtoCopier.toDto(source, pre, null);
        assertNotNull(result);
        assertEquals("modified", result.getName());
    }

    @Test
    public void testToDto_withPostProcessor_modifiesTarget() {
        SimpleProduct source = new SimpleProduct(1L, "name", 10.0);
        UnaryOperator<SimpleProductDto> post = dto -> { dto.setName("post_name"); return dto; };
        SimpleProductDto result = SimpleProductDtoCopier.toDto(source, null, post);
        assertNotNull(result);
        assertEquals("post_name", result.getName());
    }

    @Test
    public void testFromDto_withPreAndPostProcessor_works() {
        SimpleProductDto source = new SimpleProductDto();
        source.setId(5L);
        source.setName("dto_name");
        source.setPrice(5.0);
        UnaryOperator<SimpleProductDto> pre = dto -> { dto.setName("pre_name"); return dto; };
        UnaryOperator<SimpleProduct> post = p -> { p.setName(p.getName() + "_post"); return p; };
        SimpleProduct result = SimpleProductDtoCopier.fromDto(source, pre, post);
        assertNotNull(result);
        assertEquals("pre_name_post", result.getName());
    }

    // ========== 集合批量方法（双参数版本）==========

    @Test
    public void testToDtoList_withProcessors_works() {
        List<SimpleProduct> sources = Arrays.asList(
                new SimpleProduct(1L, "P1", 10.0),
                new SimpleProduct(2L, "P2", 20.0)
        );
        UnaryOperator<List<SimpleProductDto>> post = list ->
                list.stream().filter(d -> d.getPrice() > 15.0).collect(Collectors.toList());
        List<SimpleProductDto> result = SimpleProductDtoCopier.toDtoList(sources, null, post);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
    }

    @Test
    public void testFromDtoList_withProcessors_works() {
        SimpleProductDto dto1 = new SimpleProductDto();
        dto1.setId(1L); dto1.setName("D1"); dto1.setPrice(10.0);
        SimpleProductDto dto2 = new SimpleProductDto();
        dto2.setId(2L); dto2.setName("D2"); dto2.setPrice(20.0);
        List<SimpleProductDto> sources = Arrays.asList(dto1, dto2);
        UnaryOperator<List<SimpleProduct>> post = list ->
                list.stream().filter(p -> p.getId() != null && p.getId() > 1L).collect(Collectors.toList());
        List<SimpleProduct> result = SimpleProductDtoCopier.fromDtoList(sources, null, post);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get(0).getId());
    }

    @Test
    public void testToDtoSet_withProcessors_works() {
        Set<SimpleProduct> sources = new LinkedHashSet<>();
        sources.add(new SimpleProduct(1L, "P1", 10.0));
        sources.add(new SimpleProduct(2L, "P2", 20.0));
        UnaryOperator<Set<SimpleProductDto>> post = set ->
                set.stream().filter(d -> d.getPrice() > 15.0)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<SimpleProductDto> result = SimpleProductDtoCopier.toDtoSet(sources, null, post);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.iterator().next().getId());
    }

    @Test
    public void testToDtoMap_withProcessors_works() {
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("a", new SimpleProduct(1L, "P1", 10.0));
        sources.put("b", new SimpleProduct(2L, "P2", 20.0));
        UnaryOperator<Map<String, SimpleProductDto>> post = map -> {
            Map<String, SimpleProductDto> filtered = new LinkedHashMap<>();
            map.forEach((k, v) -> { if (v.getPrice() > 15.0) filtered.put(k, v); });
            return filtered;
        };
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources, null, post);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(2L), result.get("b").getId());
    }

    @Test
    public void testToDtoArray_withProcessors_works() {
        SimpleProduct[] sources = {
                new SimpleProduct(1L, "P1", 10.0),
                new SimpleProduct(2L, "P2", 20.0)
        };
        UnaryOperator<SimpleProductDto[]> post = arr ->
                Arrays.stream(arr).filter(d -> d.getPrice() > 15.0).toArray(SimpleProductDto[]::new);
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources, null, post);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Long.valueOf(2L), result[0].getId());
    }

    // ========== 无处理器的基础集合方法 ==========

    @Test
    public void testToDtoList_noProcessors_works() {
        List<SimpleProduct> sources = Arrays.asList(
                new SimpleProduct(1L, "P1", 10.0),
                new SimpleProduct(2L, "P2", 20.0)
        );
        List<SimpleProductDto> result = SimpleProductDtoCopier.toDtoList(sources);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testToDtoSet_noProcessors_works() {
        Set<SimpleProduct> sources = new LinkedHashSet<>();
        sources.add(new SimpleProduct(1L, "P1", 10.0));
        Set<SimpleProductDto> result = SimpleProductDtoCopier.toDtoSet(sources);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testToDtoMap_noProcessors_works() {
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("k", new SimpleProduct(1L, "P1", 10.0));
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testToDtoArray_noProcessors_works() {
        SimpleProduct[] sources = { new SimpleProduct(1L, "P1", 10.0) };
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources);
        assertNotNull(result);
        assertEquals(1, result.length);
    }
}
