package com.github.jackieonway.copier.example;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 同名字段拷贝的集成测试。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class SameNameFieldCopyTest {

    /**
     * 测试 toDto 方法。
     */
    @Test
    public void testToDto() {
        // 创建源对象
        User user = new User(1L, "张三", "zhangsan@example.com", 25);
        
        // 调用生成的 toDto 方法
        UserDto userDto = UserDtoCopier.toDto(user);
        
        // 验证拷贝结果
        assertNotNull(userDto);
        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getAge(), userDto.getAge());
    }

    /**
     * 测试 toDto 方法处理 null 值。
     */
    @Test
    public void testToDtoWithNull() {
        // 调用生成的 toDto 方法，传入 null
        UserDto userDto = UserDtoCopier.toDto(null);
        
        // 验证返回 null
        assertNull(userDto);
    }

    /**
     * 测试 fromDto 方法。
     */
    @Test
    public void testFromDto() {
        // 创建目标对象
        UserDto userDto = new UserDto(2L, "李四", "lisi@example.com", 30);
        
        // 调用生成的 fromDto 方法
        User user = UserDtoCopier.fromDto(userDto);
        
        // 验证反向拷贝结果
        assertNotNull(user);
        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
        assertEquals(userDto.getAge(), user.getAge());
    }

    /**
     * 测试 fromDto 方法处理 null 值。
     */
    @Test
    public void testFromDtoWithNull() {
        // 调用生成的 fromDto 方法，传入 null
        User user = UserDtoCopier.fromDto(null);
        
        // 验证返回 null
        assertNull(user);
    }

    /**
     * 测试往返拷贝。
     */
    @Test
    public void testRoundTrip() {
        // 创建源对象
        User originalUser = new User(3L, "王五", "wangwu@example.com", 35);
        
        // 转换为 DTO
        UserDto userDto = UserDtoCopier.toDto(originalUser);
        
        // 再转换回 User
        User convertedUser = UserDtoCopier.fromDto(userDto);
        
        // 验证往返拷贝结果
        assertNotNull(convertedUser);
        assertEquals(originalUser.getId(), convertedUser.getId());
        assertEquals(originalUser.getName(), convertedUser.getName());
        assertEquals(originalUser.getEmail(), convertedUser.getEmail());
        assertEquals(originalUser.getAge(), convertedUser.getAge());
    }
}
