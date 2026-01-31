package com.github.jackieonway.copier.example.nested;

/**
 * 地址实体类。
 *
 * @author jackieonway
 * @since 1.3.2
 */
public class Address {
    private String province;
    private String city;
    private String district;
    private String street;

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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
