package com.github.jackieonway.copier.example.container;

import java.math.BigDecimal;

/**
 * 产品实体类 - 用于测试容器模式下的 TypeConverter。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class Product {
    
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    
    public Product() {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
