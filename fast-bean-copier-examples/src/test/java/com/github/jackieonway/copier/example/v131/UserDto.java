package com.github.jackieonway.copier.example.v131;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 测试 DTO 类。
 *
 * @author jackieonway
 * @since 1.3.1
 */
@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;

    public UserDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
