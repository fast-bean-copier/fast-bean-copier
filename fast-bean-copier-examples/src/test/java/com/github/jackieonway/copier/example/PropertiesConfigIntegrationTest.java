package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v131.config.DefaultModelProduct;
import com.github.jackieonway.copier.example.v131.config.DefaultModelProductDto;
import com.github.jackieonway.copier.example.v131.config.DefaultModelProductDtoCopier;
import com.github.jackieonway.copier.example.v131.config.SpringModelProduct;
import com.github.jackieonway.copier.example.v131.config.SpringModelProductDto;
import com.github.jackieonway.copier.example.v131.config.SpringModelProductDtoCopier;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Properties 配置集成测试。
 *
 * <p>测试配置优先级合并功能。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class PropertiesConfigIntegrationTest {

    // ========== 类级别配置测试 ==========

    @Test
    public void testDefaultComponentModel_usesStaticMethods() {
        // 类级别显式配置 DEFAULT 应该生成静态方法
        DefaultModelProduct source = new DefaultModelProduct();
        source.setId(1L);
        source.setName("Default Model Product");
        source.setPrice(99.99);

        // 使用静态方法（DEFAULT 模式）
        DefaultModelProductDto dto = DefaultModelProductDtoCopier.toDto(source);

        assertNotNull(dto);
        assertEquals(Long.valueOf(1L), dto.getId());
        assertEquals("Default Model Product", dto.getName());
        assertEquals(Double.valueOf(99.99), dto.getPrice());
    }

    @Test
    public void testSpringComponentModel_usesInstanceMethods() {
        // 类级别显式配置 SPRING 应该生成实例方法
        SpringModelProduct source = new SpringModelProduct();
        source.setId(2L);
        source.setName("Spring Model Product");
        source.setPrice(199.99);

        // 使用实例方法（SPRING 模式）
        SpringModelProductDtoCopier copier = new SpringModelProductDtoCopier();
        SpringModelProductDto dto = copier.toDto(source);

        assertNotNull(dto);
        assertEquals(Long.valueOf(2L), dto.getId());
        assertEquals("Spring Model Product", dto.getName());
        assertEquals(Double.valueOf(199.99), dto.getPrice());
    }

    // ========== 逆向转换测试 ==========

    @Test
    public void testDefaultModel_reverseConversion() {
        // 测试 DEFAULT 模式的逆向转换
        DefaultModelProductDto dto = new DefaultModelProductDto();
        dto.setId(3L);
        dto.setName("Default Reverse");
        dto.setPrice(299.99);

        DefaultModelProduct product = DefaultModelProductDtoCopier.fromDto(dto);

        assertNotNull(product);
        assertEquals(Long.valueOf(3L), product.getId());
        assertEquals("Default Reverse", product.getName());
        assertEquals(Double.valueOf(299.99), product.getPrice());
    }

    @Test
    public void testSpringModel_reverseConversion() {
        // 测试 SPRING 模式的逆向转换
        SpringModelProductDto dto = new SpringModelProductDto();
        dto.setId(4L);
        dto.setName("Spring Reverse");
        dto.setPrice(399.99);

        SpringModelProductDtoCopier copier = new SpringModelProductDtoCopier();
        SpringModelProduct product = copier.fromDto(dto);

        assertNotNull(product);
        assertEquals(Long.valueOf(4L), product.getId());
        assertEquals("Spring Reverse", product.getName());
        assertEquals(Double.valueOf(399.99), product.getPrice());
    }

    // ========== 批量转换测试 ==========

    @Test
    public void testDefaultModel_batchConversion() {
        // 测试 DEFAULT 模式的批量转换
        DefaultModelProduct[] sources = new DefaultModelProduct[]{
                createDefaultProduct(5L, "Product 5", 500.0),
                createDefaultProduct(6L, "Product 6", 600.0)
        };

        DefaultModelProductDto[] dtos = DefaultModelProductDtoCopier.toDtoArray(sources);

        assertNotNull(dtos);
        assertEquals(2, dtos.length);
        assertEquals(Long.valueOf(5L), dtos[0].getId());
        assertEquals(Long.valueOf(6L), dtos[1].getId());
    }

    @Test
    public void testSpringModel_batchConversion() {
        // 测试 SPRING 模式的批量转换
        SpringModelProduct[] sources = new SpringModelProduct[]{
                createSpringProduct(7L, "Product 7", 700.0),
                createSpringProduct(8L, "Product 8", 800.0)
        };

        SpringModelProductDtoCopier copier = new SpringModelProductDtoCopier();
        SpringModelProductDto[] dtos = copier.toDtoArray(sources);

        assertNotNull(dtos);
        assertEquals(2, dtos.length);
        assertEquals(Long.valueOf(7L), dtos[0].getId());
        assertEquals(Long.valueOf(8L), dtos[1].getId());
    }

    // ========== 辅助方法 ==========

    private DefaultModelProduct createDefaultProduct(Long id, String name, Double price) {
        DefaultModelProduct product = new DefaultModelProduct();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }

    private SpringModelProduct createSpringProduct(Long id, String name, Double price) {
        SpringModelProduct product = new SpringModelProduct();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        return product;
    }
}
