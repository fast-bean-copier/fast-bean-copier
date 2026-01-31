package com.github.jackieonway.copier.example.nested;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 员工 DTO 类。
 *
 * @author jackieonway
 * @since 1.3.2
 */
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    private Long id;
    private String name;
    private AddressDto address;

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
}
