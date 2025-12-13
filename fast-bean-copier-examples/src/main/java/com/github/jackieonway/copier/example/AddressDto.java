package com.github.jackieonway.copier.example;

/**
 * 地址数据传输对象。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class AddressDto {
    private String province;
    private String city;
    private String street;

    public AddressDto() {
    }

    public AddressDto(String province, String city, String street) {
        this.province = province;
        this.city = city;
        this.street = street;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public String toString() {
        return "AddressDto{" +
                "province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                '}';
    }
}
