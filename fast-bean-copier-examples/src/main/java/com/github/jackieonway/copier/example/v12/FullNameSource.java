package com.github.jackieonway.copier.example.v12;

/**
 * 一对多测试用源实体类 - 包含需要拆分的字段。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class FullNameSource {
    private Long id;
    private String fullName;
    private String address;
    private String email;

    public FullNameSource() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}