package com.github.jackieonway.copier.example.v131.config;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * Spring 模式产品 DTO（显式配置 SPRING）。
 *
 * @author jackieonway
 * @since 1.3.1
 */
@CopyTarget(source = SpringModelProduct.class, componentModel = ComponentModel.SPRING)
public class SpringModelProductDto {
    private Long id;
    private String name;
    private Double price;

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
