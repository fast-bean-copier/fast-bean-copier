package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.ArrayHolder;
import com.github.jackieonway.copier.example.ArrayHolderDto;
import com.github.jackieonway.copier.example.ArrayHolderDtoCopier;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 性能基准的轻量化回归测试，确保常见集合拷贝在可接受时间内完成。
 * 不是严格基准，仅作为性能回退的快速报警。
 */
public class CollectionPerformanceTest {

    private static final int LIST_SIZE = 2000;
    private static final int SET_SIZE = 2000;
    private static final int MAP_SIZE = 1500;
    private static final int ARRAY_SIZE = 2000;
    private static final long MAX_MS = 2_000; // 保守阈值，避免不同环境下波动

    @Test
    public void listCopyShouldBeFast() {
        List<User> users = buildUserList(LIST_SIZE);
        long ms = measure(() -> {
            List<UserDto> dtos = UserDtoCopier.toDtoList(users);
            assertEquals(LIST_SIZE, dtos.size());
        });
        assertTrue("List copy too slow: " + ms + "ms", ms < MAX_MS);
    }

    @Test
    public void setCopyShouldBeFast() {
        Set<User> users = new HashSet<>(buildUserList(SET_SIZE));
        long ms = measure(() -> {
            Set<UserDto> dtos = UserDtoCopier.toDtoSet(users);
            assertEquals(SET_SIZE, dtos.size());
        });
        assertTrue("Set copy too slow: " + ms + "ms", ms < MAX_MS);
    }

    @Test
    public void mapCopyShouldBeFast() {
        Map<String, User> map = new HashMap<>();
        for (int i = 0; i < MAP_SIZE; i++) {
            map.put("k" + i, new User((long) i, "U" + i, "u" + i + "@e.com", 20));
        }
        MapHolder holder = new MapHolder(1L, new HashMap<>(), map);
        long ms = measure(() -> {
            MapHolderDto dto = MapHolderDtoCopier.toDto(holder);
            assertEquals(MAP_SIZE, dto.getUserMap().size());
        });
        assertTrue("Map copy too slow: " + ms + "ms", ms < MAX_MS);
    }

    @Test
    public void arrayCopyShouldBeFast() {
        User[] users = new User[ARRAY_SIZE];
        for (int i = 0; i < ARRAY_SIZE; i++) {
            users[i] = new User((long) i, "U" + i, "u" + i + "@e.com", 18);
        }
        ArrayHolder holder = new ArrayHolder(2L, new String[]{"a", "b"}, new int[]{1, 2, 3}, users);
        long ms = measure(() -> {
            ArrayHolderDto dto = ArrayHolderDtoCopier.toDto(holder);
            assertEquals(ARRAY_SIZE, dto.getUsers().length);
        });
        assertTrue("Array copy too slow: " + ms + "ms", ms < MAX_MS);
    }

    @Test
    public void mixedOrderCopyShouldBeFast() {
        List<String> tags = new ArrayList<>();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < LIST_SIZE; i++) {
            tags.add("t" + i);
            users.add(new User((long) i, "U" + i, "u" + i + "@e.com", 19));
        }
        Order order = new Order(3L, tags, users);
        long ms = measure(() -> {
            OrderDto dto = OrderDtoCopier.toDto(order);
            assertEquals(LIST_SIZE, dto.getUsers().size());
        });
        assertTrue("Order copy too slow: " + ms + "ms", ms < MAX_MS);
    }

    private List<User> buildUserList(int size) {
        List<User> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(new User((long) i, "U" + i, "u" + i + "@e.com", 18 + (i % 5)));
        }
        return list;
    }

    private long measure(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
}

