package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.Department;
import com.github.jackieonway.copier.example.DepartmentDto;
import com.github.jackieonway.copier.example.DepartmentDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * 嵌套集合深拷贝测试。
 */
public class NestedCollectionTest {

    @Test
    public void shouldDeepCopyListOfList() {
        User user = new User(1L, "Tom", "t@example.com", 20);
        List<User> inner = new ArrayList<>();
        inner.add(user);
        List<List<User>> groups = new ArrayList<>();
        groups.add(inner);

        Department department = new Department(groups, null, null);

        DepartmentDto dto = DepartmentDtoCopier.toDto(department);
        assertNotNull(dto);
        assertNotNull(dto.getUserGroups());
        assertEquals(1, dto.getUserGroups().size());

        List<UserDto> copiedInner = dto.getUserGroups().get(0);
        assertNotNull(copiedInner);
        assertEquals(1, copiedInner.size());
        UserDto copiedUser = copiedInner.get(0);

        assertNotSame(user, copiedUser);
        assertEquals(user.getId(), copiedUser.getId());
        assertEquals(user.getName(), copiedUser.getName());
        assertEquals(user.getEmail(), copiedUser.getEmail());
        assertEquals(user.getAge(), copiedUser.getAge());
    }

    @Test
    public void shouldDeepCopyMapWithListValue() {
        User user = new User(2L, "Jack", "j@example.com", 30);
        List<User> list = new ArrayList<>();
        list.add(user);
        Map<String, List<User>> map = new HashMap<>();
        map.put("g1", list);

        Department department = new Department(null, map, null);

        DepartmentDto dto = DepartmentDtoCopier.toDto(department);
        assertNotNull(dto.getUserGroupMap());
        assertTrue(dto.getUserGroupMap().containsKey("g1"));

        List<UserDto> copiedList = dto.getUserGroupMap().get("g1");
        assertNotNull(copiedList);
        assertEquals(1, copiedList.size());
        UserDto copiedUser = copiedList.get(0);

        assertNotSame(user, copiedUser);
        assertEquals(user.getId(), copiedUser.getId());
    }

    @Test
    public void shouldDeepCopyListOfMap() {
        User user = new User(3L, "Lucy", "l@example.com", 25);
        Map<String, User> map = new HashMap<>();
        map.put("u1", user);
        List<Map<String, User>> list = new ArrayList<>();
        list.add(map);

        Department department = new Department(null, null, list);

        DepartmentDto dto = DepartmentDtoCopier.toDto(department);
        assertNotNull(dto.getUserGroupList());
        assertEquals(1, dto.getUserGroupList().size());

        Map<String, UserDto> copiedMap = dto.getUserGroupList().get(0);
        assertNotNull(copiedMap);
        assertTrue(copiedMap.containsKey("u1"));
        UserDto copiedUser = copiedMap.get("u1");

        assertNotSame(user, copiedUser);
        assertEquals(user.getId(), copiedUser.getId());
    }

    @Test
    public void shouldHandleNullNestedCollections() {
        Department department = new Department(null, null, null);
        DepartmentDto dto = DepartmentDtoCopier.toDto(department);

        assertNull(dto.getUserGroups());
        assertNull(dto.getUserGroupMap());
        assertNull(dto.getUserGroupList());
    }
}


