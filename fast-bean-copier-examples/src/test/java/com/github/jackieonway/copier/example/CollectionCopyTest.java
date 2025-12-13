package com.github.jackieonway.copier.example;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * 集合拷贝的集成测试。
 *
 * @author jackieonway
 * @since 1.0.0
 */
public class CollectionCopyTest {

    /**
     * 测试 List 拷贝。
     */
    @Test
    public void testToDtoList() {
        // 创建源列表
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "张三", "zhangsan@example.com", 25));
        users.add(new User(2L, "李四", "lisi@example.com", 30));
        users.add(new User(3L, "王五", "wangwu@example.com", 35));
        
        // 调用生成的 toDtoList 方法
        List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
        
        // 验证结果
        assertNotNull(userDtos);
        assertEquals(3, userDtos.size());
        
        assertEquals(Long.valueOf(1L), userDtos.get(0).getId());
        assertEquals("张三", userDtos.get(0).getName());
        
        assertEquals(Long.valueOf(2L), userDtos.get(1).getId());
        assertEquals("李四", userDtos.get(1).getName());
        
        assertEquals(Long.valueOf(3L), userDtos.get(2).getId());
        assertEquals("王五", userDtos.get(2).getName());
    }

    /**
     * 测试 Set 拷贝。
     */
    @Test
    public void testToDtoSet() {
        // 创建源集合
        Set<User> users = new HashSet<>();
        users.add(new User(1L, "张三", "zhangsan@example.com", 25));
        users.add(new User(2L, "李四", "lisi@example.com", 30));
        
        // 调用生成的 toDtoSet 方法
        Set<UserDto> userDtos = UserDtoCopier.toDtoSet(users);
        
        // 验证结果
        assertNotNull(userDtos);
        assertEquals(2, userDtos.size());
    }

    /**
     * 测试 null 列表处理。
     */
    @Test
    public void testToDtoListWithNull() {
        // 调用生成的 toDtoList 方法，传入 null
        List<UserDto> userDtos = UserDtoCopier.toDtoList(null);
        
        // 验证返回 null
        assertNull(userDtos);
    }

    /**
     * 测试反向 List 拷贝。
     */
    @Test
    public void testFromDtoList() {
        // 创建目标列表
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(new UserDto(1L, "张三", "zhangsan@example.com", 25));
        userDtos.add(new UserDto(2L, "李四", "lisi@example.com", 30));
        
        // 调用生成的 fromDtoList 方法
        List<User> users = UserDtoCopier.fromDtoList(userDtos);
        
        // 验证结果
        assertNotNull(users);
        assertEquals(2, users.size());
        
        assertEquals(Long.valueOf(1L), users.get(0).getId());
        assertEquals("张三", users.get(0).getName());
        
        assertEquals(Long.valueOf(2L), users.get(1).getId());
        assertEquals("李四", users.get(1).getName());
    }

    /**
     * 测试反向 Set 拷贝。
     */
    @Test
    public void testFromDtoSet() {
        // 创建目标集合
        Set<UserDto> userDtos = new HashSet<>();
        userDtos.add(new UserDto(1L, "张三", "zhangsan@example.com", 25));
        userDtos.add(new UserDto(2L, "李四", "lisi@example.com", 30));
        
        // 调用生成的 fromDtoSet 方法
        Set<User> users = UserDtoCopier.fromDtoSet(userDtos);
        
        // 验证结果
        assertNotNull(users);
        assertEquals(2, users.size());
    }
}
