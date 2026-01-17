package com.github.jackieonway.copier.example.container;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.*;

/**
 * 容器模式下 TypeConverter 集成测试。
 * 测试 uses + qualifiedByName 在 Spring、CDI、JSR330 容器模式下的使用。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class ContainerTypeConverterTest {

    @Test
    public void testSpringMode_withQualifiedByName() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setStock(50);
        
        StockStatusConverter converter = new StockStatusConverter();
        SpringProductDtoCopier copier = new SpringProductDtoCopier(converter);
        SpringProductDto dto = copier.toDto(product);
        
        assertNotNull(dto);
        assertEquals("IN_STOCK", dto.getStockStatus());
    }

    @Test
    public void testSpringMode_differentStockStatus() {
        StockStatusConverter converter = new StockStatusConverter();
        SpringProductDtoCopier copier = new SpringProductDtoCopier(converter);
        
        Product p1 = new Product();
        p1.setStock(0);
        assertEquals("OUT_OF_STOCK", copier.toDto(p1).getStockStatus());
        
        Product p2 = new Product();
        p2.setStock(5);
        assertEquals("LOW_STOCK", copier.toDto(p2).getStockStatus());
        
        Product p3 = new Product();
        p3.setStock(50);
        assertEquals("IN_STOCK", copier.toDto(p3).getStockStatus());
        
        Product p4 = new Product();
        p4.setStock(150);
        assertEquals("HIGH_STOCK", copier.toDto(p4).getStockStatus());
    }

    @Test
    public void testCdiMode_withQualifiedByName() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Phone");
        product.setStock(120);
        
        StockStatusConverter converter = new StockStatusConverter();
        CdiProductDtoCopier copier = new CdiProductDtoCopier(converter);
        CdiProductDto dto = copier.toDto(product);
        
        assertNotNull(dto);
        assertEquals("HIGH_STOCK", dto.getStockStatus());
    }

    @Test
    public void testJsr330Mode_withQualifiedByName() {
        Product product = new Product();
        product.setId(3L);
        product.setName("Tablet");
        product.setStock(8);
        
        StockStatusConverter converter = new StockStatusConverter();
        Jsr330ProductDtoCopier copier = new Jsr330ProductDtoCopier(converter);
        Jsr330ProductDto dto = copier.toDto(product);
        
        assertNotNull(dto);
        assertEquals("LOW_STOCK", dto.getStockStatus());
    }

    @Test
    public void testAllContainerModes() {
        Product product = new Product();
        product.setStock(25);
        
        StockStatusConverter converter = new StockStatusConverter();
        
        assertEquals("IN_STOCK", new SpringProductDtoCopier(converter).toDto(product).getStockStatus());
        assertEquals("IN_STOCK", new CdiProductDtoCopier(converter).toDto(product).getStockStatus());
        assertEquals("IN_STOCK", new Jsr330ProductDtoCopier(converter).toDto(product).getStockStatus());
    }
}
