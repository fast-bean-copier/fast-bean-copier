package com.github.jackieonway.copier.example.v131;

import java.util.Date;

/**
 * 产品实体类，用于测试逆向转换跳过字段功能。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class Product {
    private Long id;
    private String name;
    private Double price;
    private Date createdDate;
    private String category;

    public Product() {}

    public Product(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Product(Long id, String name, Double price, Date createdDate, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.createdDate = createdDate;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
