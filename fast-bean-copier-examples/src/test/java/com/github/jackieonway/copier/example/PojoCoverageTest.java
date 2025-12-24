package com.github.jackieonway.copier.example;

import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 覆盖示例中的简单 JavaBean，避免覆盖率因未访问 getter/setter 过低。
 */
public class PojoCoverageTest {

    private static final Class<?>[] BEAN_CLASSES = new Class<?>[]{
            Account.class, AccountDto.class,
            Address.class, AddressDto.class,
            ArrayHolder.class, ArrayHolderDto.class,
            Department.class, DepartmentDto.class,
            Employee.class, EmployeeDto.class,
            Group.class, GroupDto.class,
            MapHolder.class, MapHolderDto.class,
            Order.class, OrderDto.class,
            Product.class, ProductDto.class,
            User.class, UserDto.class
    };

    @Test
    public void shouldCoverAllPojoGettersAndSetters() throws Exception {
        for (Class<?> beanClass : BEAN_CLASSES) {
            Object instance = beanClass.getDeclaredConstructor().newInstance();

            // 调用所有 setter 并赋予可用的样例值
            for (Method method : beanClass.getMethods()) {
                if (isSetter(method)) {
                    Object sample = buildSampleValue(method.getParameterTypes()[0]);
                    if (sample != SkipValue.INSTANCE) {
                        method.invoke(instance, sample);
                    }
                }
            }

            // 调用所有 getter 覆盖访问
            for (Method method : beanClass.getMethods()) {
                if (isGetter(method)) {
                    method.invoke(instance);
                }
            }

            instance.toString();
        }
    }

    @Test
    public void shouldExerciseGeneratedCopiers() throws Exception {
        User user = new User(1L, "Tom", "tom@test.com", 20);
        UserDto userDto = new UserDto(1L, "Tom", "tom@test.com", 20);

        Address address = new Address("ZJ", "HZ", "WestLake");
        AddressDto addressDto = new AddressDto("ZJ", "HZ", "WestLake");

        Product product = new Product(1L, "p1", 9.9, 2);
        ProductDto productDto = new ProductDto(1L, "p1", 9.9, 2);

        Order order = new Order(1L, Collections.singletonList("tag"), Collections.singletonList(user));
        OrderDto orderDto = new OrderDto(1L, Collections.singletonList("tag"), Collections.singletonList(userDto));

        Group group = new Group(1L, new LinkedHashSet<>(Collections.singleton("g")), new LinkedHashSet<>(Collections.singleton(user)));
        GroupDto groupDto = new GroupDto(1L, new LinkedHashSet<>(Collections.singleton("g")), new LinkedHashSet<>(Collections.singleton(userDto)));

        Map<String, User> userMap = new HashMap<>();
        userMap.put("u1", user);
        Map<String, UserDto> userDtoMap = new HashMap<>();
        userDtoMap.put("u1", userDto);

        MapHolder mapHolder = new MapHolder(1L, Collections.singletonMap("k", "v"), userMap);
        MapHolderDto mapHolderDto = new MapHolderDto(1L, Collections.singletonMap("k", "v"), userDtoMap);

        ArrayHolder arrayHolder = new ArrayHolder(1L, new String[]{"a"}, new int[]{1}, new User[]{user});
        ArrayHolderDto arrayHolderDto = new ArrayHolderDto(1L, new String[]{"a"}, new int[]{1}, new UserDto[]{userDto});

        List<List<User>> userGroups = Collections.singletonList(Collections.singletonList(user));
        Map<String, List<User>> userGroupMap = Collections.singletonMap("d1", Collections.singletonList(user));
        List<Map<String, User>> userGroupList = Collections.singletonList(Collections.singletonMap("d1", user));
        Department department = new Department(userGroups, userGroupMap, userGroupList);

        List<List<UserDto>> userGroupsDto = Collections.singletonList(Collections.singletonList(userDto));
        Map<String, List<UserDto>> userGroupMapDto = Collections.singletonMap("d1", Collections.singletonList(userDto));
        List<Map<String, UserDto>> userGroupListDto = Collections.singletonList(Collections.singletonMap("d1", userDto));
        DepartmentDto departmentDto = new DepartmentDto(userGroupsDto, userGroupMapDto, userGroupListDto);

        Account account = new Account(1L, "u", "p", "e");
        AccountDto accountDto = new AccountDto(1L, "u", "p", "e");

        coverCopier(com.github.jackieonway.copier.example.AccountDtoCopier.class, account);
        coverCopier(com.github.jackieonway.copier.example.ArrayHolderDtoCopier.class, arrayHolder);
        coverCopier(com.github.jackieonway.copier.example.DepartmentDtoCopier.class, department);
        coverCopier(com.github.jackieonway.copier.example.EmployeeDtoCopier.class,
                new Employee(1L, "emp", address));
        coverCopier(com.github.jackieonway.copier.example.GroupDtoCopier.class, group);
        coverCopier(com.github.jackieonway.copier.example.MapHolderDtoCopier.class, mapHolder);
        coverCopier(com.github.jackieonway.copier.example.OrderDtoCopier.class, order);
        coverCopier(com.github.jackieonway.copier.example.ProductDtoCopier.class, product);
        coverCopier(com.github.jackieonway.copier.example.UserDtoCopier.class, user);

        // 覆盖 DTO -> domain 的方向
        coverCopierReverse(com.github.jackieonway.copier.example.AccountDtoCopier.class, accountDto);
        coverCopierReverse(com.github.jackieonway.copier.example.ArrayHolderDtoCopier.class, arrayHolderDto);
        coverCopierReverse(com.github.jackieonway.copier.example.DepartmentDtoCopier.class, departmentDto);
        coverCopierReverse(com.github.jackieonway.copier.example.EmployeeDtoCopier.class,
                new EmployeeDto(1L, "emp", addressDto));
        coverCopierReverse(com.github.jackieonway.copier.example.GroupDtoCopier.class, groupDto);
        coverCopierReverse(com.github.jackieonway.copier.example.MapHolderDtoCopier.class, mapHolderDto);
        coverCopierReverse(com.github.jackieonway.copier.example.OrderDtoCopier.class, orderDto);
        coverCopierReverse(com.github.jackieonway.copier.example.ProductDtoCopier.class, productDto);
        coverCopierReverse(com.github.jackieonway.copier.example.UserDtoCopier.class, userDto);
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterCount() == 1;
    }

    private boolean isGetter(Method method) {
        if (method.getParameterCount() > 0) {
            return false;
        }
        return method.getName().startsWith("get") || method.getName().startsWith("is");
    }

    private Object buildSampleValue(Class<?> type) throws Exception {
        if (type == String.class) {
            return "sample";
        }
        if (type == int.class || type == Integer.class) {
            return 1;
        }
        if (type == long.class || type == Long.class) {
            return 1L;
        }
        if (type == boolean.class || type == Boolean.class) {
            return true;
        }
        if (type == double.class || type == Double.class) {
            return 1.0d;
        }
        if (type == float.class || type == Float.class) {
            return 1.0f;
        }
        if (type == short.class || type == Short.class) {
            return (short) 1;
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) 1;
        }
        if (type == char.class || type == Character.class) {
            return 'a';
        }
        if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        }
        if (Set.class.isAssignableFrom(type)) {
            return new LinkedHashSet<>();
        }
        if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>();
        }
        if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        }
        if (type.isInterface()) {
            return SkipValue.INSTANCE;
        }
        return type.getDeclaredConstructor().newInstance();
    }

    private <S> void coverCopier(Class<?> copierClass, S source) throws Exception {
        Method toDto = copierClass.getMethod("toDto", source.getClass());
        Object dto = toDto.invoke(null, source);

        copierClass.getMethod("fromDto", dto.getClass()).invoke(null, dto);
        copierClass.getMethod("toDtoList", List.class).invoke(null, Arrays.asList(source));
        copierClass.getMethod("fromDtoList", List.class).invoke(null, Arrays.asList(dto));
        copierClass.getMethod("toDtoSet", Set.class).invoke(null, new LinkedHashSet<>(Collections.singleton(source)));
        copierClass.getMethod("fromDtoSet", Set.class).invoke(null, new LinkedHashSet<>(Collections.singleton(dto)));
    }

    private <D> void coverCopierReverse(Class<?> copierClass, D dto) throws Exception {
        Method fromDto = copierClass.getMethod("fromDto", dto.getClass());
        Object entity = fromDto.invoke(null, dto);

        copierClass.getMethod("toDto", entity.getClass()).invoke(null, entity);
        copierClass.getMethod("fromDtoList", List.class).invoke(null, Arrays.asList(dto));
        copierClass.getMethod("toDtoList", List.class).invoke(null, Arrays.asList(entity));
        copierClass.getMethod("fromDtoSet", Set.class).invoke(null, new LinkedHashSet<>(Collections.singleton(dto)));
        copierClass.getMethod("toDtoSet", Set.class).invoke(null, new LinkedHashSet<>(Collections.singleton(entity)));
    }

    /**
     * 标记跳过无法构造的类型。
     */
    private enum SkipValue {
        INSTANCE
    }
}

