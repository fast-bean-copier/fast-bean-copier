package com.github.jackieonway.copier.example.v150;

import com.github.jackieonway.copier.annotation.CopyFromMap;
import com.github.jackieonway.copier.annotation.CopyToMap;
import com.github.jackieonway.copier.annotation.MapKeyStrategy;

/**
 * 使用 SNAKE_CASE key 策略的 DTO。
 *
 * @author jackieonway
 * @since 1.5.0
 */
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
@CopyFromMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class SnakeCaseDto {
    private Long userId;
    private String firstName;
    private String lastName;

    public SnakeCaseDto() {}

    public SnakeCaseDto(Long userId, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
