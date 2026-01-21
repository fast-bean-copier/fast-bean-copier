package com.github.jackieonway.copier.example.v131;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;

import java.util.Date;

/**
 * 产品 DTO 类，用于测试逆向转换跳过字段功能。
 *
 * <p>包含以下特殊字段映射：
 * <ul>
 *   <li>formattedDate: 使用 converter</li>
 *   <li>displayPrice: 使用 expression</li>
 *   <li>upperName: 使用 qualifiedByName</li>
 *   <li>fixedCategory: 使用 constant</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.3.1
 */
@CopyTarget(source = Product.class)
public class ProductDto {
    private Long id;
    private String name;
    
    // 使用 converter - 应该在逆向转换中跳过
    @CopyField(source = "createdDate", converter = DateToStringConverter.class, format = "yyyy-MM-dd HH:mm:ss")
    private String formattedDate;
    
    // 使用 expression - 应该在逆向转换中跳过
    @CopyField(expression = "source.getPrice() != null ? String.format(\"$%.2f\", source.getPrice()) : \"N/A\"")
    private String displayPrice;
    
    // 使用 qualifiedByName - 应该在逆向转换中跳过
    @CopyField(source = "name", qualifiedByName = "toUpperCase")
    private String upperName;
    
    // 使用 constant - 应该在逆向转换中跳过
    @CopyField(constant = "DEFAULT_CATEGORY")
    private String fixedCategory;

    public ProductDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
    public String getDisplayPrice() { return displayPrice; }
    public void setDisplayPrice(String displayPrice) { this.displayPrice = displayPrice; }
    public String getUpperName() { return upperName; }
    public void setUpperName(String upperName) { this.upperName = upperName; }
    public String getFixedCategory() { return fixedCategory; }
    public void setFixedCategory(String fixedCategory) { this.fixedCategory = fixedCategory; }
    
    /**
     * 具名方法：将字符串转换为大写。
     *
     * @param value 输入字符串
     * @return 大写字符串
     */
    public static String toUpperCase(String value) {
        return value != null ? value.toUpperCase() : null;
    }
}
