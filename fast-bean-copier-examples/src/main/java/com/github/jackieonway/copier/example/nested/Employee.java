package com.github.jackieonway.copier.example.nested;

/**
 * 员工实体类。
 *
 * @author jackieonway
 * @since 1.3.2
 */
public class Employee {
    private Long id;
    private String name;
    private Address address;

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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
