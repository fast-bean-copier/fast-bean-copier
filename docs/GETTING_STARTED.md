# Fast Bean Copier 快速入门指南

> v1.4.0 新特性：函数式处理增强（preProcessor + postProcessor 双处理器）、深拷贝控制（@CopyField.deepCopy 属性）。
>
> v1.3.2 新特性：嵌套对象深拷贝支持（自动深拷贝不同类型的嵌套对象、无限层级嵌套、混合模式支持）。
>
> v1.3.1 新特性：Map/Array 批量转换的 UnaryOperator 重载、Properties 配置文件支持、逆向转换智能跳过。
>
> v1.3 新特性：更新现有对象（updateDto/updateEntity）、映射前回调、条件映射、默认值和常量、全局配置（@CopyTargetConfig）。
>
> v1.2.1 重构：处理器架构重构，代码可维护性显著提升。
>
> v1.2 新特性：多字段映射（多对一、一对多）、TypeConverter 类型转换器、依赖注入支持、函数式定制拷贝。

## 5 分钟快速开始

### 步骤 1：添加依赖

在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.4.0</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.4.0</version>
    <scope>provided</scope>
</dependency>
```

### 步骤 2：定义源类

```java
public class User {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    
    // getter/setter...
}
```

### 步骤 3：定义 DTO 类

```java
import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    
    // getter/setter...
}
```

### 步骤 4：编译

```bash
mvn clean compile
```

### 步骤 5：使用

```java
User user = new User(1L, "张三", "zhangsan@example.com", 25);
UserDto userDto = UserDtoCopier.toDto(user);
```

## 常见场景

### 场景 1：忽略敏感字段

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto { }
```

### 场景 2：批量转换

```java
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

### 场景 3：双向转换

```java
User user = UserDtoCopier.fromDto(userDto);
```

### 场景 4：集合转换

```java
List<UserDto> dtos = UserDtoCopier.toDtoList(users);
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);
```

## v1.4.0 新功能

### 函数式处理增强

```java
// 预处理和后处理
UserDto dto = UserDtoCopier.toDto(
    user,
    source -> {
        // preProcessor: 拷贝前对 source 预处理
        source.setName(source.getName().trim());
        return source;
    },
    target -> {
        // postProcessor: 拷贝后对 target 后处理
        target.setDisplayName(target.getName().toUpperCase());
        return target;
    }
);

// 集合转换
List<UserDto> dtos = UserDtoCopier.toDtoList(
    users,
    source -> { /* 预处理每个 source */ return source; },
    target -> { /* 后处理每个 target */ return target; }
);
```

### 深拷贝控制

```java
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    // 深拷贝（默认）：创建新的 AddressDto 对象
    @CopyField(deepCopy = true)
    private AddressDto address;
    
    // 浅拷贝：直接引用传递
    @CopyField(deepCopy = false)
    private DepartmentDto department;
    
    // 集合深拷贝（默认）
    @CopyField(deepCopy = true)
    private List<ProjectDto> projects;
    
    // 集合浅拷贝：拷贝集合但元素直接引用
    @CopyField(deepCopy = false)
    private List<TagDto> tags;
}
```

## v1.2 新功能

### 多对一映射

```java
@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
}
```

### 一对多映射

```java
@CopyTarget(source = FullNameSource.class)
public class NameDto {
    @CopyField(source = "fullName", 
               expression = "java(source.getFullName().split(\" \")[0])")
    private String firstName;
}
```

### 数字格式化

```java
@CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
private String priceText;
```

### 日期格式化

```java
@CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
private String createTimeText;
```

### 自定义转换器

```java
@CopyTarget(source = Person.class, uses = PersonConverter.class)
public class PersonDto {
    @CopyField(qualifiedByName = "formatAge")
    private String ageText;
}
```

### Spring 集成

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { }

@Service
public class UserService {
    @Autowired
    private UserDtoCopier userDtoCopier;
}
```

### 函数式定制（v1.3.1 统一行为）

> **v1.3.1 重要变更**：所有集合类型的 customizer 现在都操作整个集合，提供一致且强大的定制能力。

```java
// 单对象定制
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});

// List 定制 - 过滤和排序（v1.3.1 统一行为）
List<UserDto> result = UserDtoCopier.toDtoList(users, list -> 
    list.stream()
        .filter(dto -> dto.getPrice() >= 100)
        .sorted(Comparator.comparing(UserDto::getName))
        .collect(Collectors.toList())
);

// Set 定制 - 不可变集合（v1.3.1 统一行为）
Set<UserDto> immutableSet = UserDtoCopier.toDtoSet(userSet, 
    Collections::unmodifiableSet);
```

## v1.3.1 新功能

### 统一 UnaryOperator 行为

v1.3.1 统一了所有集合类型的 UnaryOperator 行为：

- List: `UnaryOperator<List<T>>` - 操作整个列表
- Set: `UnaryOperator<Set<T>>` - 操作整个集合
- Map: `UnaryOperator<Map<K, T>>` - 操作整个 Map
- Array: `UnaryOperator<T[]>` - 操作整个数组

### Map 批量转换的 UnaryOperator 重载

```java
// 过滤 Map 条目
Map<String, UserDto> filteredMap = UserDtoCopier.toDtoMap(userMap, result -> {
    result.entrySet().removeIf(entry -> entry.getValue().getId() == null);
    return result;
});

// 转换为不可变 Map
Map<String, UserDto> immutableMap = UserDtoCopier.toDtoMap(userMap, 
    Collections::unmodifiableMap);
```

### Array 批量转换的 UnaryOperator 重载

```java
// 过滤数组元素
UserDto[] filteredArray = UserDtoCopier.toDtoArray(users, result -> 
    Arrays.stream(result)
        .filter(dto -> dto.getId() != null)
        .toArray(UserDto[]::new));

// 排序数组
UserDto[] sortedArray = UserDtoCopier.toDtoArray(users, result -> {
    Arrays.sort(result, Comparator.comparing(UserDto::getName));
    return result;
});
```

### Properties 配置文件支持

```properties
# 在 src/main/resources/fast-bean-copier.properties 中配置
fast.bean.copier.componentModel=SPRING
fast.bean.copier.nullValueStrategy=IGNORE
```

配置优先级：类级别 > 包级别 > 配置文件 > 默认值

```java
// 类级别配置优先级最高
@CopyTarget(source = User.class, componentModel = ComponentModel.DEFAULT)
public class UserDto { }

// 未配置时使用配置文件中的设置
@CopyTarget(source = Product.class)
public class ProductDto { }  // 使用配置文件中的 SPRING 模式
```

### 逆向转换智能跳过

```java
@CopyTarget(source = User.class)
public class UserDto {
    // 使用类型转换器的字段在逆向转换时自动跳过
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
    private String createTimeText;
    
    // 使用表达式的字段在逆向转换时自动跳过
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
}

// 生成的 fromDto 方法会自动跳过这些字段并添加注释
// 注释格式：// 类型转换器映射 'createTimeText' 不可逆，在 fromDto() 中跳过
```

## v1.3 新功能

### 更新现有对象

```java
// 更新已存在的 DTO 对象
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");
UserDtoCopier.updateDto(existingDto, user);

// 更新已存在的实体对象
User existingUser = new User();
UserDtoCopier.updateEntity(existingUser, userDto);
```

### 映射前回调

```java
@CopyTarget(source = User.class, beforeMapping = "validateAndPrepare")
public class UserDto {
    private String name;
    
    default void validateAndPrepare(User source) {
        if (source.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
    }
}
```

### 条件映射

```java
@CopyTarget(source = User.class)
public class UserDto {
    // 仅当源字段不为 null 时才映射
    @CopyField(condition = "java(source.getName() != null)")
    private String name;
    
    // 仅当年龄大于 18 时才映射
    @CopyField(condition = "java(source.getAge() > 18)")
    private Integer age;
}
```

### 默认值和常量

```java
@CopyTarget(source = User.class)
public class UserDto {
    // 当源字段为 null 时使用默认值
    @CopyField(defaultValue = "未知")
    private String name;
    
    // 设置常量值，不依赖源字段
    @CopyField(constant = "SYSTEM")
    private String createdBy;
}
```

### 全局配置

```java
// package-info.java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

## 下一步

- 查看 [参考文档](REFERENCE.md) 了解更多功能
- 查看 [API 文档](API.md) 了解详细 API
- 查看 [FAQ](FAQ.md) 了解常见问题

## 许可证

Apache License 2.0
