package com.github.jackieonway.copier.example.container;

import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.ComponentModel;

/**
 * 产品 DTO - JSR330 模式，使用 TypeConverter。
 *
 * @author jackieonway
 * @since 1.2.0
 */
@CopyTarget(source = Product.class, 
            componentModel = ComponentModel.JSR330,
            uses = StockStatusConverter.class)
public class Jsr330ProductDto {
    
    private Long id;
    private String name;
    
    @CopyField(expression = "java(source.getPrice() != null ? source.getPrice().toString() : null)")
    private String price;
    
    /**
     * 使用 StockStatusConverter 转换库存为状态描述
     */
    @CopyField(source = "stock", qualifiedByName = "toStatus")
    private String stockStatus;
    
    public Jsr330ProductDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}
