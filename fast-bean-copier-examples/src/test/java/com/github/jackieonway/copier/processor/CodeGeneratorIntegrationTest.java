package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import com.github.jackieonway.copier.example.UserDtoCopier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * CodeGenerator 集成测试。
 *
 * <p>验证重构后的 CodeGenerator 生成的代码与重构前一致。
 * 通过检查生成的 Copier 类的方法和行为来验证。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class CodeGeneratorIntegrationTest {

    // ========== 基础方法测试 ==========

    @Test
    public void testToDtoMethodExists() {
        assertNotNull("UserDtoCopier should exist", UserDtoCopier.class);
    }

    @Test
    public void testToDtoWithNullInput() {
        assertNull("toDto(null) should return null", UserDtoCopier.toDto(null));
    }

    @Test
    public void testFromDtoWithNullInput() {
        assertNull("fromDto(null) should return null", UserDtoCopier.fromDto(null));
    }

    // ========== 集合方法测试 ==========

    @Test
    public void testToDtoListWithNullInput() {
        assertNull("toDtoList(null) should return null", UserDtoCopier.toDtoList(null));
    }

    @Test
    public void testFromDtoListWithNullInput() {
        assertNull("fromDtoList(null) should return null", UserDtoCopier.fromDtoList(null));
    }

    @Test
    public void testToDtoSetWithNullInput() {
        assertNull("toDtoSet(null) should return null", UserDtoCopier.toDtoSet(null));
    }

    @Test
    public void testFromDtoSetWithNullInput() {
        assertNull("fromDtoSet(null) should return null", UserDtoCopier.fromDtoSet(null));
    }

    @Test
    public void testToDtoMapWithNullInput() {
        assertNull("toDtoMap(null) should return null", UserDtoCopier.toDtoMap(null));
    }

    @Test
    public void testFromDtoMapWithNullInput() {
        assertNull("fromDtoMap(null) should return null", UserDtoCopier.fromDtoMap(null));
    }

    @Test
    public void testToDtoArrayWithNullInput() {
        assertNull("toDtoArray(null) should return null", UserDtoCopier.toDtoArray(null));
    }

    @Test
    public void testFromDtoArrayWithNullInput() {
        assertNull("fromDtoArray(null) should return null", UserDtoCopier.fromDtoArray(null));
    }

    // ========== Processors 方法测试 ==========

    @Test
    public void testToDtoWithProcessorsNullInput() {
        assertNull("toDto(null, pre, post) should return null",
                UserDtoCopier.toDto(null, null, null));
    }

    @Test
    public void testFromDtoWithProcessorsNullInput() {
        assertNull("fromDto(null, pre, post) should return null",
                UserDtoCopier.fromDto(null, null, null));
    }

    @Test
    public void testToDtoListWithProcessorsNullInput() {
        assertNull("toDtoList(null, pre, post) should return null",
                UserDtoCopier.toDtoList(null, null, null));
    }

    @Test
    public void testFromDtoListWithProcessorsNullInput() {
        assertNull("fromDtoList(null, pre, post) should return null",
                UserDtoCopier.fromDtoList(null, null, null));
    }

    @Test
    public void testToDtoSetWithProcessorsNullInput() {
        assertNull("toDtoSet(null, pre, post) should return null",
                UserDtoCopier.toDtoSet(null, null, null));
    }

    @Test
    public void testFromDtoSetWithProcessorsNullInput() {
        assertNull("fromDtoSet(null, pre, post) should return null",
                UserDtoCopier.fromDtoSet(null, null, null));
    }

    // ========== 功能性测试 ==========

    @Test
    public void testToDtoBasicConversion() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserDto dto = UserDtoCopier.toDto(user);

        assertNotNull("Converted DTO should not be null", dto);
        assertEquals("ID should be copied", Long.valueOf(1L), dto.getId());
        assertEquals("Name should be copied", "Test User", dto.getName());
        assertEquals("Email should be copied", "test@example.com", dto.getEmail());
    }

    @Test
    public void testFromDtoBasicConversion() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setName("DTO User");
        dto.setEmail("dto@example.com");

        User user = UserDtoCopier.fromDto(dto);

        assertNotNull("Converted entity should not be null", user);
        assertEquals("ID should be copied", Long.valueOf(2L), user.getId());
        assertEquals("Name should be copied", "DTO User", user.getName());
        assertEquals("Email should be copied", "dto@example.com", user.getEmail());
    }

    @Test
    public void testToDtoWithPostProcessor() {
        User user = new User();
        user.setId(3L);
        user.setName("Original");

        UserDto dto = UserDtoCopier.toDto(user, null, d -> {
            d.setName("Modified");
            return d;
        });

        assertNotNull("Converted DTO should not be null", dto);
        assertEquals("Name should be modified by postProcessor", "Modified", dto.getName());
    }

    @Test
    public void testToDtoListConversion() {
        List<User> users = new ArrayList<>();

        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        users.add(user1);

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");
        users.add(user2);

        List<UserDto> dtos = UserDtoCopier.toDtoList(users);

        assertNotNull("Converted list should not be null", dtos);
        assertEquals("List size should match", 2, dtos.size());
        assertEquals("First user ID should match", Long.valueOf(1L), dtos.get(0).getId());
        assertEquals("Second user ID should match", Long.valueOf(2L), dtos.get(1).getId());
    }

    @Test
    public void testToDtoSetConversion() {
        Set<User> users = new LinkedHashSet<>();

        User user = new User();
        user.setId(1L);
        user.setName("Set User");
        users.add(user);

        Set<UserDto> dtos = UserDtoCopier.toDtoSet(users);

        assertNotNull("Converted set should not be null", dtos);
        assertEquals("Set size should match", 1, dtos.size());
    }

    @Test
    public void testToDtoArrayConversion() {
        User[] users = new User[2];

        users[0] = new User();
        users[0].setId(1L);
        users[0].setName("Array User 1");

        users[1] = new User();
        users[1].setId(2L);
        users[1].setName("Array User 2");

        UserDto[] dtos = UserDtoCopier.toDtoArray(users);

        assertNotNull("Converted array should not be null", dtos);
        assertEquals("Array length should match", 2, dtos.length);
        assertEquals("First user ID should match", Long.valueOf(1L), dtos[0].getId());
        assertEquals("Second user ID should match", Long.valueOf(2L), dtos[1].getId());
    }

    @Test
    public void testToDtoMapConversion() {
        Map<String, User> users = new LinkedHashMap<>();

        User user = new User();
        user.setId(1L);
        user.setName("Map User");
        users.put("key1", user);

        Map<String, UserDto> dtos = UserDtoCopier.toDtoMap(users);

        assertNotNull("Converted map should not be null", dtos);
        assertEquals("Map size should match", 1, dtos.size());
        assertTrue("Map should contain key", dtos.containsKey("key1"));
        assertEquals("User ID should match", Long.valueOf(1L), dtos.get("key1").getId());
    }
}
