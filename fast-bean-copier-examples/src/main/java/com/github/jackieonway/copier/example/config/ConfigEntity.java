package com.github.jackieonway.copier.example.config;

/**
 * 配置测试实体类。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class ConfigEntity {
    private Long id;
    private String name;

    public ConfigEntity() {
    }

    public ConfigEntity(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
