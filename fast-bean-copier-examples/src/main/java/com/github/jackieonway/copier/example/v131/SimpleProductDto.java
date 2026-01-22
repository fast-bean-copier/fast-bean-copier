package com.github.jackieonway.copier.example.v131;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 简单产品 DTO 类，用于集成测试。
 *
 * @author jackieonway
 * @since 1.3.1
 */
@CopyTarget(source = SimpleProduct.class)
public class SimpleProductDto {
    private Long id;
    private String name;
    private Double price;

    public SimpleProductDto() {
    }

    public SimpleProductDto(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
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
}
