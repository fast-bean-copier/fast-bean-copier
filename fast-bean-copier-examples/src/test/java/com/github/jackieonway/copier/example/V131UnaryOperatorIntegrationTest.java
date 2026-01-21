package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.v131.User;
import com.github.jackieonway.copier.example.v131.UserDto;
import com.github.jackieonway.copier.example.v131.UserDtoCopier;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * v1.3.1 UnaryOperator 集成测试。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class V131UnaryOperatorIntegrationTest {

    @Test
    public void testToDtoMapWithCustomizer() {
        // 准备测试数据
        Map<String, User> userMap = new HashMap<>();
        userMap.put("user1", new User(1L, "Alice"));
        userMap.put("user2", new User(null, "Bob"));
        userMap.put("user3", new User(3L, "Charlie"));

        // 使用 customizer 过滤掉 id 为 null 的条目
        Map<String, UserDto> result = UserDtoCopier.toDtoMap(userMap, map -> {
            Map<String, UserDto> filtered = new HashMap<>();
            for (Map.Entry<String, UserDto> entry : map.entrySet()) {
                if (entry.getValue().getId() != null) {
                    filtered.put(entry.getKey(), entry.getValue());
                }
            }
            return filtered;
        });

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("user1"));
        assertFalse(result.containsKey("user2")); // 被过滤掉
        assertTrue(result.containsKey("user3"));
    }

    @Test
    public void testFromDtoMapWithCustomizer() {
        // 准备测试数据
        Map<String, UserDto> dtoMap = new HashMap<>();
        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        dto1.setName("Alice");
        dtoMap.put("user1", dto1);

        UserDto dto2 = new UserDto();
        dto2.setId(2L);
        dto2.setName("Bob");
        dtoMap.put("user2", dto2);

        // 使用 customizer 添加额外条目
        Map<String, User> result = UserDtoCopier.fromDtoMap(dtoMap, map -> {
            User defaultUser = new User(999L, "Default");
            map.put("default", defaultUser);
            return map;
        });

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsKey("user1"));
        assertTrue(result.containsKey("user2"));
        assertTrue(result.containsKey("default"));
        assertEquals(Long.valueOf(999L), result.get("default").getId());
    }

    @Test
    public void testToDtoArrayWithCustomizer() {
        // 准备测试数据
        User[] users = {
                new User(1L, "Alice"),
                new User(null, "Bob"),
                new User(3L, "Charlie")
        };

        // 使用 customizer 过滤掉 id 为 null 的元素
        UserDto[] result = UserDtoCopier.toDtoArray(users, array -> {
            return Arrays.stream(array)
                    .filter(dto -> dto.getId() != null)
                    .toArray(UserDto[]::new);
        });

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(3L), result[1].getId());
    }

    @Test
    public void testFromDtoArrayWithCustomizer() {
        // 准备测试数据
        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        dto1.setName("Alice");

        UserDto dto2 = new UserDto();
        dto2.setId(2L);
        dto2.setName("Bob");

        UserDto[] dtos = {dto1, dto2};

        // 使用 customizer 排序
        User[] result = UserDtoCopier.fromDtoArray(dtos, array -> {
            Arrays.sort(array, (a, b) -> b.getId().compareTo(a.getId())); // 降序
            return array;
        });

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(2L), result[0].getId()); // Bob 在前
        assertEquals(Long.valueOf(1L), result[1].getId()); // Alice 在后
    }

    @Test
    public void testMapCustomizerWithNull() {
        // 测试 customizer 为 null 的情况
        Map<String, User> userMap = new HashMap<>();
        userMap.put("user1", new User(1L, "Alice"));

        Map<String, UserDto> result = UserDtoCopier.toDtoMap(userMap, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Alice", result.get("user1").getName());
    }

    @Test
    public void testArrayCustomizerWithNull() {
        // 测试 customizer 为 null 的情况
        User[] users = {new User(1L, "Alice")};

        UserDto[] result = UserDtoCopier.toDtoArray(users, null);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Alice", result[0].getName());
    }
}
