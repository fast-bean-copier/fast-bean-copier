package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.Order;
import com.github.jackieonway.copier.example.OrderDto;
import com.github.jackieonway.copier.example.OrderDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * List 深拷贝功能测试。
 */
public class ListDeepCopyTest {

    @Test
    public void shouldCopyPrimitiveList() {
        List<String> tags = new ArrayList<>();
        tags.add("a");
        tags.add("b");
        Order order = new Order(1L, tags, null);

        OrderDto dto = OrderDtoCopier.toDto(order);

        assertNotNull(dto.getTags());
        assertEquals(tags.size(), dto.getTags().size());
        assertEquals(tags, dto.getTags());
        assertNotSame("目标列表应为新实例", tags, dto.getTags());
    }

    @Test
    public void shouldDeepCopyNestedObjectList() {
        User user = new User(1L, "Tom", "t@e.com", 20);
        List<User> users = new ArrayList<>();
        users.add(user);
        Order order = new Order(2L, Collections.singletonList("u"), users);

        OrderDto dto = OrderDtoCopier.toDto(order);

        assertNotNull(dto.getUsers());
        assertEquals(1, dto.getUsers().size());

        UserDto copied = dto.getUsers().get(0);
        assertNotSame("嵌套对象应深拷贝", user, copied);
        assertEquals(user.getId(), copied.getId());
        assertEquals(user.getName(), copied.getName());
        assertEquals(user.getEmail(), copied.getEmail());
        assertEquals(user.getAge(), copied.getAge());
    }

    @Test
    public void shouldHandleNullAndEmptyList() {
        Order nullOrder = new Order(3L, null, null);
        OrderDto nullDto = OrderDtoCopier.toDto(nullOrder);
        assertNull(nullDto.getTags());
        assertNull(nullDto.getUsers());

        Order emptyOrder = new Order(4L, new ArrayList<>(), new ArrayList<>());
        OrderDto emptyDto = OrderDtoCopier.toDto(emptyOrder);
        assertNotNull(emptyDto.getTags());
        assertTrue(emptyDto.getTags().isEmpty());
        assertNotNull(emptyDto.getUsers());
        assertTrue(emptyDto.getUsers().isEmpty());
    }
}

