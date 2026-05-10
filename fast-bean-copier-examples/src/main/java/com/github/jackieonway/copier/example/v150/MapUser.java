package com.github.jackieonway.copier.example.v150;

/**
 * v1.5.0 Bean ↔ Map 转换测试用源对象。
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class MapUser {
    private Long id;
    private String name;
    private Integer age;
    private String email;

    public MapUser() {}

    public MapUser(Long id, String name, Integer age, String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
