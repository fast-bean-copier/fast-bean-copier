package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.annotation.CopyTarget;

/**
 * 账户数据传输对象，忽略password字段。
 *
 * @author jackieonway
 * @since 1.0.0
 */
@CopyTarget(source = Account.class, ignore = {"password"})
public class AccountDto {
    private Long id;
    private String username;
    private String password;
    private String email;

    public AccountDto() {
    }

    public AccountDto(Long id, String username, String password, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "AccountDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
