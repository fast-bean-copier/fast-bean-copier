package com.github.jackieonway.copier.example.config;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 配置测试 DTO 类。
 *
 * <p>用于测试配置文件读取功能。
 *
 * @author jackieonway
 * @since 1.3.1
 */
@CopyTarget(source = ConfigEntity.class)
public class ConfigDto {
    private Long id;
    private String name;

    public ConfigDto() {
    }

    public ConfigDto(Long id, String name) {
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
