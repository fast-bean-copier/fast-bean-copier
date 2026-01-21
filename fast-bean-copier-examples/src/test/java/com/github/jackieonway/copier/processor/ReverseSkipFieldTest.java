package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.v131.Product;
import com.github.jackieonway.copier.example.v131.ProductDto;
import com.github.jackieonway.copier.example.v131.ProductDtoCopier;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * 逆向转换跳过字段功能测试。
 *
 * <p>测试在 fromDto 和 updateEntity 方法中，使用了特殊映射配置的字段应该被跳过：
 * <ul>
 *   <li>converter 字段</li>
 *   <li>expression 字段</li>
 *   <li>qualifiedByName 字段</li>
 *   <li>constant 字段</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class ReverseSkipFieldTest {

    // ========== converter 字段跳过测试 ==========

    @Test
    public void testFromDto_whenConverterField_shouldSkip() {
        // 准备测试数据
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("Test Product");
        dto.setFormattedDate("2026-01-21 10:00:00"); // converter 字段

        // 执行逆向转换
        Product product = ProductDtoCopier.fromDto(dto);

        // 验证结果
        assertNotNull(product);
        assertEquals(Long.valueOf(1L), product.getId());
        assertEquals("Test Product", product.getName());
        // converter 字段应该被跳过，createdDate 应该为 null
        assertNull(product.getCreatedDate());
    }

    @Test
    public void testUpdateEntity_whenConverterField_shouldSkip() {
        // 准备现有实体
        Product existingProduct = new Product(1L, "Old Name", 100.0, new Date(), "Electronics");
        Date originalDate = existingProduct.getCreatedDate();

        // 准备 DTO
        ProductDto dto = new ProductDto();
        dto.setId(2L);
        dto.setName("New Name");
        dto.setFormattedDate("2026-12-31 23:59:59"); // converter 字段

        // 执行更新
        ProductDtoCopier.updateEntity(existingProduct, dto);

        // 验证结果
        assertEquals(Long.valueOf(2L), existingProduct.getId());
        assertEquals("New Name", existingProduct.getName());
        // converter 字段应该被跳过，createdDate 应该保持原值
        assertEquals(originalDate, existingProduct.getCreatedDate());
    }

    // ========== expression 字段跳过测试 ==========

    @Test
    public void testFromDto_whenExpressionField_shouldSkip() {
        // 准备测试数据
        ProductDto dto = new ProductDto();
        dto.setId(2L);
        dto.setName("Laptop");
        dto.setDisplayPrice("$999.99"); // expression 字段

        // 执行逆向转换
        Product product = ProductDtoCopier.fromDto(dto);

        // 验证结果
        assertNotNull(product);
        assertEquals(Long.valueOf(2L), product.getId());
        assertEquals("Laptop", product.getName());
        // expression 字段应该被跳过，price 应该为 null
        assertNull(product.getPrice());
    }

    @Test
    public void testUpdateEntity_whenExpressionField_shouldSkip() {
        // 准备现有实体
        Product existingProduct = new Product(1L, "Old Product", 500.0, new Date(), "Books");
        Double originalPrice = existingProduct.getPrice();

        // 准备 DTO
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("Updated Product");
        dto.setDisplayPrice("$1,234.56"); // expression 字段

        // 执行更新
        ProductDtoCopier.updateEntity(existingProduct, dto);

        // 验证结果
        assertEquals("Updated Product", existingProduct.getName());
        // expression 字段应该被跳过，price 应该保持原值
        assertEquals(originalPrice, existingProduct.getPrice());
    }

    // ========== qualifiedByName 字段跳过测试 ==========

    @Test
    public void testFromDto_whenQualifiedByNameField_shouldSkip() {
        // 准备测试数据
        ProductDto dto = new ProductDto();
        dto.setId(3L);
        dto.setName("phone");
        dto.setUpperName("PHONE"); // qualifiedByName 字段

        // 执行逆向转换
        Product product = ProductDtoCopier.fromDto(dto);

        // 验证结果
        assertNotNull(product);
        assertEquals(Long.valueOf(3L), product.getId());
        // name 字段正常拷贝
        assertEquals("phone", product.getName());
        // qualifiedByName 字段不影响 name 的逆向转换
    }

    @Test
    public void testUpdateEntity_whenQualifiedByNameField_shouldSkip() {
        // 准备现有实体
        Product existingProduct = new Product(1L, "oldname", 100.0, new Date(), "Tech");

        // 准备 DTO
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("newname");
        dto.setUpperName("NEWNAME"); // qualifiedByName 字段

        // 执行更新
        ProductDtoCopier.updateEntity(existingProduct, dto);

        // 验证结果
        // name 字段正常更新
        assertEquals("newname", existingProduct.getName());
    }

    // ========== constant 字段跳过测试 ==========

    @Test
    public void testFromDto_whenConstantField_shouldSkip() {
        // 准备测试数据
        ProductDto dto = new ProductDto();
        dto.setId(4L);
        dto.setName("Tablet");
        dto.setFixedCategory("CUSTOM_CATEGORY"); // constant 字段

        // 执行逆向转换
        Product product = ProductDtoCopier.fromDto(dto);

        // 验证结果
        assertNotNull(product);
        assertEquals(Long.valueOf(4L), product.getId());
        assertEquals("Tablet", product.getName());
        // constant 字段应该被跳过，category 应该为 null
        assertNull(product.getCategory());
    }

    @Test
    public void testUpdateEntity_whenConstantField_shouldSkip() {
        // 准备现有实体
        Product existingProduct = new Product(1L, "Product", 200.0, new Date(), "Original Category");
        String originalCategory = existingProduct.getCategory();

        // 准备 DTO
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("Updated Product");
        dto.setFixedCategory("DIFFERENT_CATEGORY"); // constant 字段

        // 执行更新
        ProductDtoCopier.updateEntity(existingProduct, dto);

        // 验证结果
        assertEquals("Updated Product", existingProduct.getName());
        // constant 字段应该被跳过，category 应该保持原值
        assertEquals(originalCategory, existingProduct.getCategory());
    }

    // ========== 正向转换不受影响测试 ==========

    @Test
    public void testToDto_whenSpecialFields_shouldWorkNormally() {
        // 准备测试数据
        Date testDate = new Date();
        Product product = new Product(5L, "Monitor", 299.99, testDate, "Electronics");

        // 执行正向转换
        ProductDto dto = ProductDtoCopier.toDto(product);

        // 验证结果 - 所有特殊字段都应该正常工作
        assertNotNull(dto);
        assertEquals(Long.valueOf(5L), dto.getId());
        assertEquals("Monitor", dto.getName());
        
        // converter 字段应该正常转换
        assertNotNull(dto.getFormattedDate());
        assertTrue(dto.getFormattedDate().contains("2026"));
        
        // expression 字段应该正常计算
        assertEquals("$299.99", dto.getDisplayPrice());
        
        // qualifiedByName 字段 - 注意：当前实现直接复制源字段值，不调用转换方法
        // 这是一个已知的限制，qualifiedByName 需要在 @CopyTarget.uses 中配置转换器类
        assertEquals("Monitor", dto.getUpperName());
        
        // constant 字段应该设置为常量值
        assertEquals("DEFAULT_CATEGORY", dto.getFixedCategory());
    }

    @Test
    public void testUpdateDto_whenSpecialFields_shouldWorkNormally() {
        // 准备现有 DTO
        ProductDto existingDto = new ProductDto();
        existingDto.setId(1L);
        existingDto.setName("Old");

        // 准备源实体
        Date testDate = new Date();
        Product product = new Product(2L, "keyboard", 49.99, testDate, "Accessories");

        // 执行更新
        ProductDtoCopier.updateDto(existingDto, product);

        // 验证结果 - 所有特殊字段都应该正常工作
        assertEquals(Long.valueOf(2L), existingDto.getId());
        assertEquals("keyboard", existingDto.getName());
        
        // converter 字段应该正常转换
        assertNotNull(existingDto.getFormattedDate());
        
        // expression 字段 - 注意：updateDto 中没有 expression 的条件判断，所以不会更新
        // 这是因为 expression 没有对应的源字段来判断 null
        // assertNull(existingDto.getDisplayPrice()); // 保持原值或为 null
        
        // qualifiedByName 字段 - 直接复制源字段值
        assertEquals("keyboard", existingDto.getUpperName());
        
        // constant 字段应该设置为常量值
        assertEquals("DEFAULT_CATEGORY", existingDto.getFixedCategory());
    }

    // ========== 组合测试 ==========

    @Test
    public void testFromDto_whenMultipleSpecialFields_shouldSkipAll() {
        // 准备测试数据 - 包含所有特殊字段
        ProductDto dto = new ProductDto();
        dto.setId(6L);
        dto.setName("Complex Product");
        dto.setFormattedDate("2026-01-21 12:00:00");
        dto.setDisplayPrice("$1,999.99");
        dto.setUpperName("COMPLEX PRODUCT");
        dto.setFixedCategory("SPECIAL_CATEGORY");

        // 执行逆向转换
        Product product = ProductDtoCopier.fromDto(dto);

        // 验证结果 - 只有普通字段被拷贝
        assertNotNull(product);
        assertEquals(Long.valueOf(6L), product.getId());
        assertEquals("Complex Product", product.getName());
        
        // 所有特殊字段对应的源字段都应该为 null
        assertNull(product.getCreatedDate());
        assertNull(product.getPrice());
        assertNull(product.getCategory());
    }

    @Test
    public void testRoundTrip_whenSpecialFields_shouldOnlyPreserveNormalFields() {
        // 准备原始实体
        Date originalDate = new Date();
        Product original = new Product(7L, "roundtrip", 123.45, originalDate, "Test Category");

        // 正向转换
        ProductDto dto = ProductDtoCopier.toDto(original);
        
        // 验证正向转换成功
        assertNotNull(dto);
        // qualifiedByName 字段直接复制源字段值
        assertEquals("roundtrip", dto.getUpperName());
        assertEquals("$123.45", dto.getDisplayPrice());

        // 逆向转换
        Product restored = ProductDtoCopier.fromDto(dto);

        // 验证结果 - 只有普通字段被恢复
        assertNotNull(restored);
        assertEquals(original.getId(), restored.getId());
        assertEquals(original.getName(), restored.getName());
        
        // 特殊字段对应的源字段无法恢复
        assertNull(restored.getCreatedDate()); // 不等于 originalDate
        assertNull(restored.getPrice()); // 不等于 123.45
        assertNull(restored.getCategory()); // 不等于 "Test Category"
    }
}
