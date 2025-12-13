package com.github.jackieonway.copier.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 基本类型与包装类型转换的集成测试。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class PrimitiveWrapperConversionTest {

    /**
     * 测试基本类型转换为包装类型。
     */
    @Test
    public void testPrimitiveToWrapper() {
        // 创建源对象（基本类型）
        Product product = new Product(1L, "手机", 3999.99, 100);
        
        // 调用生成的 toDto 方法
        ProductDto productDto = ProductDtoCopier.toDto(product);
        
        // 验证转换结果
        assertNotNull(productDto);
        assertEquals(Long.valueOf(1L), productDto.getId());
        assertEquals("手机", productDto.getName());
        assertEquals(Double.valueOf(3999.99), productDto.getPrice());
        assertEquals(Integer.valueOf(100), productDto.getQuantity());
    }

    /**
     * 测试包装类型转换为基本类型。
     */
    @Test
    public void testWrapperToPrimitive() {
        // 创建目标对象（包装类型）
        ProductDto productDto = new ProductDto(2L, "平板", 2999.99, 50);
        
        // 调用生成的 fromDto 方法
        Product product = ProductDtoCopier.fromDto(productDto);
        
        // 验证转换结果
        assertNotNull(product);
        assertEquals(2L, product.getId());
        assertEquals("平板", product.getName());
        assertEquals(2999.99, product.getPrice(), 0.01);
        assertEquals(50, product.getQuantity());
    }

    /**
     * 测试 null 值处理（包装类型转换为基本类型）。
     */
    @Test
    public void testNullValueHandling() {
        // 创建目标对象，某些字段为 null
        ProductDto productDto = new ProductDto(null, "笔记本", null, null);
        
        // 调用生成的 fromDto 方法
        Product product = ProductDtoCopier.fromDto(productDto);
        
        // 验证 null 值被转换为默认值
        assertNotNull(product);
        assertEquals(0L, product.getId());
        assertEquals("笔记本", product.getName());
        assertEquals(0.0, product.getPrice(), 0.01);
        assertEquals(0, product.getQuantity());
    }

    /**
     * 测试往返转换。
     */
    @Test
    public void testRoundTrip() {
        // 创建源对象
        Product originalProduct = new Product(3L, "键盘", 199.99, 200);
        
        // 转换为 DTO
        ProductDto productDto = ProductDtoCopier.toDto(originalProduct);
        
        // 再转换回 Product
        Product convertedProduct = ProductDtoCopier.fromDto(productDto);
        
        // 验证往返转换结果
        assertNotNull(convertedProduct);
        assertEquals(originalProduct.getId(), convertedProduct.getId());
        assertEquals(originalProduct.getName(), convertedProduct.getName());
        assertEquals(originalProduct.getPrice(), convertedProduct.getPrice(), 0.01);
        assertEquals(originalProduct.getQuantity(), convertedProduct.getQuantity());
    }
}
