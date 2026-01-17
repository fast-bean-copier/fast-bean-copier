package com.github.jackieonway.copier.example.container;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.example.User;

/**
 * Spring 容器模式的 UserDto 示例。
 * 生成的 Copier 类将使用 Spring 的 @Component 注解。
 *
 * @author jackieonway
 * @since 1.2.0
 */
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class SpringUserDto {
    private Long id;
    private String name;
    private String email;
    private Integer age;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
