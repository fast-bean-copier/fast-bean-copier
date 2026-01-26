package com.github.jackieonway.copier.example.v131.config;

/**
 * Spring 模式产品实体。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class SpringModelProduct {
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
