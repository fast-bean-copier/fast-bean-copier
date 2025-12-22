package com.github.jackieonway.copier.example;

import com.github.jackieonway.copier.example.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * 集合反向拷贝功能测试。
 *
 * 覆盖 List、Set、数组、Map 及嵌套对象的 fromDto 反向拷贝。
 */
public class CollectionReverseCopyTest {

    @Test
    public void shouldReverseCopyList() {
        List<UserDto> dtos = new ArrayList<>();
        dtos.add(new UserDto(1L, "Tom", "t@e.com", 20));
        dtos.add(new UserDto(2L, "Jerry", "j@e.com", 18));

        List users = UserDtoCopier.fromDtoList(dtos);

        assertNotNull(users);
        assertEquals(2, users.size());
        Object first = users.get(0);
        assertTrue(first instanceof User);
        User user = (User) first;
        assertEquals(Long.valueOf(1L), user.getId());
        assertEquals("Tom", user.getName());
    }

    @Test
    public void shouldReverseCopySet() {
        Set<UserDto> dtoSet = new LinkedHashSet<>();
        dtoSet.add(new UserDto(1L, "Tom", "t@e.com", 20));

        GroupDto groupDto = new GroupDto();
        groupDto.setMembers(dtoSet);

        Group group = GroupDtoCopier.fromDto(groupDto);

        assertNotNull(group.getMembers());
        assertEquals(1, group.getMembers().size());
        User member = group.getMembers().iterator().next();
        assertEquals(Long.valueOf(1L), member.getId());
        assertEquals("Tom", member.getName());
    }

    @Test
    public void shouldReverseCopyArray() {
        UserDto[] dtoArray = new UserDto[]{
                new UserDto(1L, "Tom", "t@e.com", 20),
                new UserDto(2L, "Jerry", "j@e.com", 18)
        };
        ArrayHolderDto holderDto = new ArrayHolderDto();
        holderDto.setUsers(dtoArray);

        ArrayHolder holder = ArrayHolderDtoCopier.fromDto(holderDto);

        assertNotNull(holder.getUsers());
        assertEquals(2, holder.getUsers().length);
        assertEquals("Tom", holder.getUsers()[0].getName());
        assertEquals("Jerry", holder.getUsers()[1].getName());
    }

    @Test
    public void shouldReverseCopyMap() {
        Map<String, UserDto> dtoMap = new HashMap<>();
        dtoMap.put("u1", new UserDto(1L, "Tom", "t@e.com", 20));

        MapHolderDto holderDto = new MapHolderDto();
        holderDto.setUserMap(dtoMap);

        MapHolder holder = MapHolderDtoCopier.fromDto(holderDto);

        assertNotNull(holder.getUserMap());
        assertEquals(1, holder.getUserMap().size());
        assertTrue(holder.getUserMap().containsKey("u1"));
        User user = holder.getUserMap().get("u1");
        assertEquals("Tom", user.getName());
    }
}


