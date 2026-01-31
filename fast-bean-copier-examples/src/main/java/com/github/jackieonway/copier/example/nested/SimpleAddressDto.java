package com.github.jackieonway.copier.example.nested;

// 注意：这个类没有 @CopyTarget 注解，用于测试字段拷贝回退机制
public class SimpleAddressDto {
    private String city;
    private String street;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}
