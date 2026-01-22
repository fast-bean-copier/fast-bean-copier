package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v131.SimpleProduct;
import com.github.jackieonway.copier.example.v131.SimpleProductDto;
import com.github.jackieonway.copier.example.v131.SimpleProductDtoCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * v1.3.1 UnaryOperator integration test.
 *
 * <p>Tests Map and Array UnaryOperator overload methods.
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class V131UnaryOperatorIntegrationTest {

    // ========== Map UnaryOperator Tests ==========

    @Test
    public void testToDtoMap_withCustomizer_filterNullIds() {
        // Prepare test data
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("p1", new SimpleProduct(1L, "Product 1", 100.0));
        sources.put("p2", new SimpleProduct(null, "Product 2", 200.0));
        sources.put("p3", new SimpleProduct(3L, "Product 3", 300.0));

        // Define customizer: filter out entries with null id
        UnaryOperator<Map<String, SimpleProductDto>> customizer = map -> {
            Map<String, SimpleProductDto> filtered = new LinkedHashMap<>();
            for (Map.Entry<String, SimpleProductDto> entry : map.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getId() != null) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        };

        // Execute conversion
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("p1"));
        assertTrue(result.containsKey("p3"));
        assertEquals(Long.valueOf(1L), result.get("p1").getId());
        assertEquals(Long.valueOf(3L), result.get("p3").getId());
    }

    @Test
    public void testToDtoMap_withCustomizer_unmodifiableMap() {
        // Prepare test data
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("p1", new SimpleProduct(1L, "Product 1", 100.0));

        // Define customizer: convert to unmodifiable map
        UnaryOperator<Map<String, SimpleProductDto>> customizer = Collections::unmodifiableMap;

        // Execute conversion
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Verify it's unmodifiable
        try {
            result.put("p2", new SimpleProductDto());
            assertTrue("Should throw UnsupportedOperationException", false);
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testToDtoMap_withNullCustomizer() {
        // Prepare test data
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("p1", new SimpleProduct(1L, "Product 1", 100.0));

        // Execute conversion with null customizer
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources, null);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get("p1").getId());
    }

    @Test
    public void testToDtoMap_withNullSources() {
        // Execute conversion with null sources
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(null, map -> map);

        // Verify result
        assertNull(result);
    }

    @Test
    public void testFromDtoMap_withCustomizer_filterNullIds() {
        // Prepare test data
        Map<String, SimpleProductDto> sources = new LinkedHashMap<>();
        SimpleProductDto dto1 = new SimpleProductDto();
        dto1.setId(1L);
        dto1.setName("Product 1");
        dto1.setPrice(100.0);
        
        SimpleProductDto dto2 = new SimpleProductDto();
        dto2.setId(null);
        dto2.setName("Product 2");
        dto2.setPrice(200.0);
        
        sources.put("p1", dto1);
        sources.put("p2", dto2);

        // Define customizer: filter out entries with null id
        UnaryOperator<Map<String, SimpleProduct>> customizer = map -> {
            Map<String, SimpleProduct> filtered = new LinkedHashMap<>();
            for (Map.Entry<String, SimpleProduct> entry : map.entrySet()) {
                if (entry.getValue() != null && entry.getValue().getId() != null) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        };

        // Execute conversion
        Map<String, SimpleProduct> result = SimpleProductDtoCopier.fromDtoMap(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("p1"));
        assertEquals(Long.valueOf(1L), result.get("p1").getId());
    }

    // ========== Array UnaryOperator Tests ==========

    @Test
    public void testToDtoArray_withCustomizer_filterNullIds() {
        // Prepare test data
        SimpleProduct[] sources = new SimpleProduct[]{
                new SimpleProduct(1L, "Product 1", 100.0),
                new SimpleProduct(null, "Product 2", 200.0),
                new SimpleProduct(3L, "Product 3", 300.0)
        };

        // Define customizer: filter out elements with null id
        UnaryOperator<SimpleProductDto[]> customizer = array -> {
            return Arrays.stream(array)
                    .filter(dto -> dto != null && dto.getId() != null)
                    .toArray(SimpleProductDto[]::new);
        };

        // Execute conversion
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(3L), result[1].getId());
    }

    @Test
    public void testToDtoArray_withCustomizer_sort() {
        // Prepare test data
        SimpleProduct[] sources = new SimpleProduct[]{
                new SimpleProduct(3L, "Product 3", 300.0),
                new SimpleProduct(1L, "Product 1", 100.0),
                new SimpleProduct(2L, "Product 2", 200.0)
        };

        // Define customizer: sort by id
        UnaryOperator<SimpleProductDto[]> customizer = array -> {
            Arrays.sort(array, Comparator.comparing(SimpleProductDto::getId));
            return array;
        };

        // Execute conversion
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(2L), result[1].getId());
        assertEquals(Long.valueOf(3L), result[2].getId());
    }

    @Test
    public void testToDtoArray_withCustomizer_limitSize() {
        // Prepare test data
        SimpleProduct[] sources = new SimpleProduct[]{
                new SimpleProduct(1L, "Product 1", 100.0),
                new SimpleProduct(2L, "Product 2", 200.0),
                new SimpleProduct(3L, "Product 3", 300.0)
        };

        // Define customizer: limit to first 2 elements
        UnaryOperator<SimpleProductDto[]> customizer = array -> {
            return Arrays.copyOf(array, Math.min(2, array.length));
        };

        // Execute conversion
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(2L), result[1].getId());
    }

    @Test
    public void testToDtoArray_withNullCustomizer() {
        // Prepare test data
        SimpleProduct[] sources = new SimpleProduct[]{
                new SimpleProduct(1L, "Product 1", 100.0)
        };

        // Execute conversion with null customizer
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources, null);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
    }

    @Test
    public void testToDtoArray_withNullSources() {
        // Execute conversion with null sources
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(null, array -> array);

        // Verify result
        assertNull(result);
    }

    @Test
    public void testFromDtoArray_withCustomizer_filterNullIds() {
        // Prepare test data
        SimpleProductDto dto1 = new SimpleProductDto();
        dto1.setId(1L);
        dto1.setName("Product 1");
        dto1.setPrice(100.0);
        
        SimpleProductDto dto2 = new SimpleProductDto();
        dto2.setId(null);
        dto2.setName("Product 2");
        dto2.setPrice(200.0);
        
        SimpleProductDto[] sources = new SimpleProductDto[]{dto1, dto2};

        // Define customizer: filter out elements with null id
        UnaryOperator<SimpleProduct[]> customizer = array -> {
            return Arrays.stream(array)
                    .filter(product -> product != null && product.getId() != null)
                    .toArray(SimpleProduct[]::new);
        };

        // Execute conversion
        SimpleProduct[] result = SimpleProductDtoCopier.fromDtoArray(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
    }

    @Test
    public void testFromDtoArray_withCustomizer_sort() {
        // Prepare test data
        SimpleProductDto dto1 = new SimpleProductDto();
        dto1.setId(3L);
        dto1.setName("Product 3");
        
        SimpleProductDto dto2 = new SimpleProductDto();
        dto2.setId(1L);
        dto2.setName("Product 1");
        
        SimpleProductDto[] sources = new SimpleProductDto[]{dto1, dto2};

        // Define customizer: sort by id
        UnaryOperator<SimpleProduct[]> customizer = array -> {
            Arrays.sort(array, Comparator.comparing(SimpleProduct::getId));
            return array;
        };

        // Execute conversion
        SimpleProduct[] result = SimpleProductDtoCopier.fromDtoArray(sources, customizer);

        // Verify result
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(3L), result[1].getId());
    }

    // ========== Backward Compatibility Tests ==========

    @Test
    public void testBasicMapMethods_stillWork() {
        // Prepare test data
        Map<String, SimpleProduct> sources = new LinkedHashMap<>();
        sources.put("p1", new SimpleProduct(1L, "Product 1", 100.0));

        // Execute conversion without customizer
        Map<String, SimpleProductDto> result = SimpleProductDtoCopier.toDtoMap(sources);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Long.valueOf(1L), result.get("p1").getId());
    }

    @Test
    public void testBasicArrayMethods_stillWork() {
        // Prepare test data
        SimpleProduct[] sources = new SimpleProduct[]{
                new SimpleProduct(1L, "Product 1", 100.0)
        };

        // Execute conversion without customizer
        SimpleProductDto[] result = SimpleProductDtoCopier.toDtoArray(sources);

        // Verify result
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
    }
}
