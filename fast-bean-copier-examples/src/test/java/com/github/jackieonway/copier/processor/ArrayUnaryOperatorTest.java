package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.v131.User;
import com.github.jackieonway.copier.example.v131.UserDto;
import com.github.jackieonway.copier.example.v131.UserDtoCopier;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Array UnaryOperator 重载功能测试。
 *
 * <p>测试 toDtoArray 和 fromDtoArray 方法的 UnaryOperator 重载版本。
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class ArrayUnaryOperatorTest {

    // ========== toDtoArray UnaryOperator 测试 ==========

    @Test
    public void testToDtoArray_whenCustomizerProvided_shouldApplyCustomizer() {
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
        assertEquals("Alice", result[0].getName());
        assertEquals(Long.valueOf(3L), result[1].getId());
        assertEquals("Charlie", result[1].getName());
    }

    @Test
    public void testToDtoArray_whenCustomizerIsNull_shouldReturnBasicResult() {
        // 准备测试数据
        User[] users = {new User(1L, "Alice")};

        // customizer 为 null
        UserDto[] result = UserDtoCopier.toDtoArray(users, null);

        // 验证结果 - 应该返回基础转换结果
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Alice", result[0].getName());
    }

    @Test
    public void testToDtoArray_whenSourcesIsNull_shouldReturnNull() {
        // sources 为 null
        UserDto[] result = UserDtoCopier.toDtoArray(null, array -> array);

        // 验证结果
        assertNull(result);
    }

    @Test
    public void testToDtoArray_whenCustomizerSorts_shouldReturnSortedArray() {
        // 准备测试数据
        User[] users = {
                new User(3L, "Charlie"),
                new User(1L, "Alice"),
                new User(2L, "Bob")
        };

        // 使用 customizer 排序
        UserDto[] result = UserDtoCopier.toDtoArray(users, array -> {
            Arrays.sort(array, (a, b) -> a.getId().compareTo(b.getId()));
            return array;
        });

        // 验证结果 - 应该按 id 升序排列
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(2L), result[1].getId());
        assertEquals(Long.valueOf(3L), result[2].getId());
    }

    // ========== fromDtoArray UnaryOperator 测试 ==========

    @Test
    public void testFromDtoArray_whenCustomizerProvided_shouldApplyCustomizer() {
        // 准备测试数据
        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        dto1.setName("Alice");

        UserDto dto2 = new UserDto();
        dto2.setId(null);
        dto2.setName("Bob");

        UserDto dto3 = new UserDto();
        dto3.setId(3L);
        dto3.setName("Charlie");

        UserDto[] dtos = {dto1, dto2, dto3};

        // 使用 customizer 过滤掉 id 为 null 的元素
        User[] result = UserDtoCopier.fromDtoArray(dtos, array -> {
            return Arrays.stream(array)
                    .filter(user -> user.getId() != null)
                    .toArray(User[]::new);
        });

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals("Alice", result[0].getName());
        assertEquals(Long.valueOf(3L), result[1].getId());
        assertEquals("Charlie", result[1].getName());
    }

    @Test
    public void testFromDtoArray_whenCustomizerIsNull_shouldReturnBasicResult() {
        // 准备测试数据
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Alice");
        UserDto[] dtos = {dto};

        // customizer 为 null
        User[] result = UserDtoCopier.fromDtoArray(dtos, null);

        // 验证结果 - 应该返回基础转换结果
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("Alice", result[0].getName());
    }

    @Test
    public void testFromDtoArray_whenSourcesIsNull_shouldReturnNull() {
        // sources 为 null
        User[] result = UserDtoCopier.fromDtoArray(null, array -> array);

        // 验证结果
        assertNull(result);
    }

    @Test
    public void testFromDtoArray_whenCustomizerSorts_shouldReturnSortedArray() {
        // 准备测试数据
        UserDto dto1 = new UserDto();
        dto1.setId(3L);
        dto1.setName("Charlie");

        UserDto dto2 = new UserDto();
        dto2.setId(1L);
        dto2.setName("Alice");

        UserDto dto3 = new UserDto();
        dto3.setId(2L);
        dto3.setName("Bob");

        UserDto[] dtos = {dto1, dto2, dto3};

        // 使用 customizer 降序排序
        User[] result = UserDtoCopier.fromDtoArray(dtos, array -> {
            Arrays.sort(array, (a, b) -> b.getId().compareTo(a.getId()));
            return array;
        });

        // 验证结果 - 应该按 id 降序排列
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(Long.valueOf(3L), result[0].getId());
        assertEquals(Long.valueOf(2L), result[1].getId());
        assertEquals(Long.valueOf(1L), result[2].getId());
    }

    @Test
    public void testFromDtoArray_whenCustomizerLimitsSize_shouldReturnLimitedArray() {
        // 准备测试数据
        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        dto1.setName("Alice");

        UserDto dto2 = new UserDto();
        dto2.setId(2L);
        dto2.setName("Bob");

        UserDto dto3 = new UserDto();
        dto3.setId(3L);
        dto3.setName("Charlie");

        UserDto[] dtos = {dto1, dto2, dto3};

        // 使用 customizer 限制数量为前 2 个
        User[] result = UserDtoCopier.fromDtoArray(dtos, array -> {
            return Arrays.copyOf(array, Math.min(2, array.length));
        });

        // 验证结果 - 应该只有前 2 个元素
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Long.valueOf(1L), result[0].getId());
        assertEquals(Long.valueOf(2L), result[1].getId());
    }
}
