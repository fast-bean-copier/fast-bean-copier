package com.github.jackieonway.copier.processor;

import com.github.jackieonway.copier.example.User;
import com.github.jackieonway.copier.example.UserDto;
import com.github.jackieonway.copier.example.UserDtoCopier;
import com.github.jackieonway.copier.example.Product;
import com.github.jackieonway.copier.example.ProductDto;
import com.github.jackieonway.copier.example.ProductDtoCopier;
import com.github.jackieonway.copier.example.Employee;
import com.github.jackieonway.copier.example.EmployeeDto;
import com.github.jackieonway.copier.example.EmployeeDtoCopier;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * BeanCopierProcessor 集成测试。
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class BeanCopierProcessorIntegrationTest {

    @Test
    public void testUserCopierGenerated() {
        assertNotNull("UserDtoCopier should be generated", UserDtoCopier.class);
    }

    @Test
    public void testUserToDto() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserDto dto = UserDtoCopier.toDto(user);

        assertNotNull("DTO should not be null", dto);
        assertEquals("ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Name should match", "Test User", dto.getName());
        assertEquals("Email should match", "test@example.com", dto.getEmail());
    }

    @Test
    public void testUserFromDto() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setName("DTO User");
        dto.setEmail("dto@example.com");

        User user = UserDtoCopier.fromDto(dto);

        assertNotNull("User should not be null", user);
        assertEquals("ID should match", Long.valueOf(2L), user.getId());
        assertEquals("Name should match", "DTO User", user.getName());
        assertEquals("Email should match", "dto@example.com", user.getEmail());
    }

    @Test
    public void testNullHandling() {
        assertNull("toDto(null) should return null", UserDtoCopier.toDto(null));
        assertNull("fromDto(null) should return null", UserDtoCopier.fromDto(null));
        assertNull("toDtoList(null) should return null", UserDtoCopier.toDtoList(null));
        assertNull("fromDtoList(null) should return null", UserDtoCopier.fromDtoList(null));
    }

    @Test
    public void testListConversion() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");
        users.add(user1);

        List<UserDto> dtos = UserDtoCopier.toDtoList(users);

        assertNotNull("DTO list should not be null", dtos);
        assertEquals("List size should match", 1, dtos.size());
        assertEquals("First user ID should match", Long.valueOf(1L), dtos.get(0).getId());
    }

    @Test
    public void testCustomizer() {
        User user = new User();
        user.setId(1L);
        user.setName("Original Name");

        UserDto dto = UserDtoCopier.toDto(user, d -> {
            d.setName("Modified Name");
            return d;
        });

        assertNotNull("DTO should not be null", dto);
        assertEquals("Name should be modified", "Modified Name", dto.getName());
    }

    @Test
    public void testRoundTrip() {
        User original = new User();
        original.setId(100L);
        original.setName("Round Trip User");
        original.setEmail("roundtrip@example.com");

        UserDto dto = UserDtoCopier.toDto(original);
        User restored = UserDtoCopier.fromDto(dto);

        assertNotNull("Restored user should not be null", restored);
        assertEquals("ID should match after round trip", original.getId(), restored.getId());
        assertEquals("Name should match after round trip", original.getName(), restored.getName());
    }

    @Test
    public void testProductToDto() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");

        ProductDto dto = ProductDtoCopier.toDto(product);

        assertNotNull("ProductDto should not be null", dto);
        assertEquals("Product ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Product name should match", "Test Product", dto.getName());
    }

    @Test
    public void testEmployeeToDto() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");

        EmployeeDto dto = EmployeeDtoCopier.toDto(employee);

        assertNotNull("EmployeeDto should not be null", dto);
        assertEquals("Employee ID should match", Long.valueOf(1L), dto.getId());
        assertEquals("Employee name should match", "John Doe", dto.getName());
    }
}
