package com.github.jackieonway.copier.example.v150;

import com.github.jackieonway.copier.annotation.CopyFromMap;
import com.github.jackieonway.copier.annotation.CopyToMap;

/**
 * 测试 ignore 字段的 DTO。
 *
 * @author jackieonway
 * @since 1.5.0
 */
@CopyToMap(ignore = {"password"})
@CopyFromMap(ignore = {"password"})
public class IgnoreFieldDto {
    private Long id;
    private String username;
    private String password;

    public IgnoreFieldDto() {}

    public IgnoreFieldDto(Long id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
