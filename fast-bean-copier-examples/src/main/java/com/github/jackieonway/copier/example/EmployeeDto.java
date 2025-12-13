package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 员工数据传输对象，包含嵌套的地址对象。
 *
 * @author jackieonway
 * @since 1.0.0
 */
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    private Long id;
    private String name;
    private AddressDto address;

    public EmployeeDto() {
    }

    public EmployeeDto(Long id, String name, AddressDto address) {
        this.id = id;
        this.name = name;
        this.address = address;
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

    public AddressDto getAddress() {
        return address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address=" + address +
                '}';
    }
}
