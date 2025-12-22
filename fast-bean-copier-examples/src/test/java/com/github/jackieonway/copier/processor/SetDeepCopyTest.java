package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.Group;
import com.github.jackieonway.copier.example.GroupDto;
import com.github.jackieonway.copier.example.GroupDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Set 深拷贝功能测试。
 */
public class SetDeepCopyTest {

    @Test
    public void shouldCopyPrimitiveSet() {
        Set<String> tags = new LinkedHashSet<>();
        tags.add("a");
        tags.add("b");
        Group group = new Group(1L, tags, null);

        GroupDto dto = GroupDtoCopier.toDto(group);

        assertNotNull(dto.getTags());
        assertEquals(tags.size(), dto.getTags().size());
        assertTrue(dto.getTags().containsAll(tags));
        assertNotSame("目标 Set 应为新实例", tags, dto.getTags());
    }

    @Test
    public void shouldDeepCopyNestedObjectSet() {
        User user = new User(1L, "Tom", "t@e.com", 20);
        Set<User> members = new LinkedHashSet<>();
        members.add(user);
        Group group = new Group(2L, null, members);

        GroupDto dto = GroupDtoCopier.toDto(group);

        assertNotNull(dto.getMembers());
        assertEquals(1, dto.getMembers().size());

        UserDto copied = dto.getMembers().iterator().next();
        assertNotSame("嵌套对象应深拷贝", user, copied);
        assertEquals(user.getId(), copied.getId());
        assertEquals(user.getName(), copied.getName());
        assertEquals(user.getEmail(), copied.getEmail());
        assertEquals(user.getAge(), copied.getAge());
    }

    @Test
    public void shouldHandleNullAndEmptySet() {
        Group nullGroup = new Group(3L, null, null);
        GroupDto nullDto = GroupDtoCopier.toDto(nullGroup);
        assertNull(nullDto.getTags());
        assertNull(nullDto.getMembers());

        Group emptyGroup = new Group(4L, new LinkedHashSet<>(), new LinkedHashSet<>());
        GroupDto emptyDto = GroupDtoCopier.toDto(emptyGroup);

        assertNotNull(emptyDto.getTags());
        assertTrue(emptyDto.getTags().isEmpty());

        assertNotNull(emptyDto.getMembers());
        assertTrue(emptyDto.getMembers().isEmpty());
    }

    /**
     * 测试 Set 中包含 null 元素时的处理。
     */
    @Test
    public void shouldHandleNullElementInSet() {
        Set<User> members = new LinkedHashSet<>();
        members.add(null);
        members.add(new User(1L, "Tom", "t@e.com", 20));

        Group group = new Group(5L, null, members);
        GroupDto dto = GroupDtoCopier.toDto(group);

        assertNotNull(dto.getMembers());
        assertEquals(2, dto.getMembers().size());
        assertTrue("应包含一个 null 元素", dto.getMembers().contains(null));
    }
}


