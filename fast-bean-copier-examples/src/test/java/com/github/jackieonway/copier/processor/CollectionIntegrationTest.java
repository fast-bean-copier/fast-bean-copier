package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.ArrayHolder;
import com.github.jackieonway.copier.example.ArrayHolderDto;
import com.github.jackieonway.copier.example.ArrayHolderDtoCopier;
import com.github.jackieonway.copier.example.Department;
import com.github.jackieonway.copier.example.DepartmentDto;
import com.github.jackieonway.copier.example.DepartmentDtoCopier;
import com.github.jackieonway.copier.example.MapHolder;
import com.github.jackieonway.copier.example.MapHolderDto;
import com.github.jackieonway.copier.example.MapHolderDtoCopier;
import com.github.jackieonway.copier.example.Order;
import com.github.jackieonway.copier.example.OrderDto;
import com.github.jackieonway.copier.example.OrderDtoCopier;
import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import com.github.jackieonway.copier.example.UserDtoCopier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 集合深拷贝的集成验证，覆盖嵌套、多类型混合和大规模场景。
 */
public class CollectionIntegrationTest {

    @Test
    public void shouldDeepCopyNestedDepartmentCollections() {
        User u1 = new User(1L, "Tom", "t@e.com", 20);
        List<User> group1 = new ArrayList<>(Arrays.asList(u1, null));
        List<List<User>> userGroups = new ArrayList<>();
        userGroups.add(group1);

        Map<String, List<User>> userGroupMap = new HashMap<>();
        userGroupMap.put("g1", group1);

        Map<String, User> mapEntry = new HashMap<>();
        mapEntry.put("u1", u1);
        List<Map<String, User>> userGroupList = new ArrayList<>();
        userGroupList.add(mapEntry);

        Department department = new Department(userGroups, userGroupMap, userGroupList);

        DepartmentDto dto = DepartmentDtoCopier.toDto(department);

        assertNotSame(department.getUserGroups(), dto.getUserGroups());
        assertEquals(1, dto.getUserGroups().size());
        assertNotSame(group1, dto.getUserGroups().get(0));
        UserDto copied = dto.getUserGroups().get(0).get(0);
        assertNotSame(u1, copied);
        assertEquals(u1.getName(), copied.getName());
        assertNull(dto.getUserGroups().get(0).get(1));

        List<UserDto> mappedList = dto.getUserGroupMap().get("g1");
        assertNotSame(userGroupMap.get("g1"), mappedList);
        assertEquals(2, mappedList.size());

        Map<String, UserDto> mappedEntry = dto.getUserGroupList().get(0);
        assertNotSame(mapEntry, mappedEntry);
        assertNotSame(u1, mappedEntry.get("u1"));
    }

    @Test
    public void shouldHandleMixedCollectionsAndArraysTogether() {
        User u1 = new User(2L, "Jerry", "j@e.com", 21);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("k1", "v1");
        Map<String, User> userMap = new HashMap<>();
        userMap.put("u", u1);
        MapHolder mapHolder = new MapHolder(10L, attributes, userMap);
        MapHolderDto mapHolderDto = MapHolderDtoCopier.toDto(mapHolder);

        assertNotSame(attributes, mapHolderDto.getAttributes());
        assertEquals(attributes, mapHolderDto.getAttributes());
        assertNotSame(userMap.get("u"), mapHolderDto.getUserMap().get("u"));

        ArrayHolder arrayHolder = new ArrayHolder(11L,
                new String[]{"a", "b"},
                new int[]{1, 2, 3},
                new User[]{u1, null});
        ArrayHolderDto arrayHolderDto = ArrayHolderDtoCopier.toDto(arrayHolder);

        assertNotSame(arrayHolder.getUsers(), arrayHolderDto.getUsers());
        assertNotSame(arrayHolder.getUsers()[0], arrayHolderDto.getUsers()[0]);
        assertNull(arrayHolderDto.getUsers()[1]);
        assertArrayEquals(arrayHolder.getScores(), arrayHolderDto.getScores());

        Order order = new Order(12L,
                Arrays.asList("p1", "p2"),
                Arrays.asList(u1));
        OrderDto orderDto = OrderDtoCopier.toDto(order);

        assertEquals(order.getTags(), orderDto.getTags());
        assertNotSame(order.getUsers().get(0), orderDto.getUsers().get(0));
    }

    @Test
    public void shouldCopyLargeListEfficiently() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            users.add(new User((long) i, "U" + i, "u" + i + "@e.com", 18 + (i % 5)));
        }

        List<UserDto> dtos = UserDtoCopier.toDtoList(users);

        assertEquals(1000, dtos.size());
        assertNotSame(users.get(0), dtos.get(0));
        assertEquals(users.get(999).getName(), dtos.get(999).getName());
    }
}

