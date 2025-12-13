package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 产品数据传输对象，包含包装类型字段。
 *
 * @author jackieonway
 * @since 1.0.0
 */
@CopyTarget(source = Product.class)
public class ProductDto {
    private Long id;
    private String name;
    private Double price;
    private Integer quantity;

    public ProductDto() {
    }

    public ProductDto(Long id, String name, Double price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProductDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
