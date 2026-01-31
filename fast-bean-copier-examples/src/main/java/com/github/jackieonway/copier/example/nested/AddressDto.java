package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 地址 DTO 类（有 @CopyTarget 注解）。
 *
 * @author jackieonway
 * @since 1.3.2
 */
@CopyTarget(source = Address.class)
public class AddressDto {
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
