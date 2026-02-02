# Fast Bean Copier

Fast Bean Copier 是一个高性能的 Java Bean 拷贝工具，使用 APT（注解处理工具）在编译期自动生成拷贝代码，实现零运行时开销。

> **v1.3.2 新特性**：嵌套对象深拷贝支持（自动深拷贝不同类型的嵌套对象、无限层级嵌套、混合模式支持）。
>
> **v1.3.1 新特性**：统一所有集合类型的 UnaryOperator 行为（List/Set/Map/Array）、Properties 配置文件支持、逆向转换特殊字段自动跳过。
>
> **v1.3 新特性**：更新现有对象（updateDto/updateEntity）、映射前回调、条件映射、默认值和常量、全局配置（@CopyTargetConfig）。
>
> **v1.2.1 重构**：处理器架构重构，BeanCopierProcessor 和 CodeGenerator 拆分为多个职责单一的组件，代码可维护性显著提升。
>
> **v1.2 新特性**：多字段映射（多对一、一对多）、TypeConverter 类型转换器、依赖注入支持（Spring/CDI/JSR-330）、函数式定制拷贝。

## 特性

- ✅ **编译期代码生成** - 使用 APT 在编译期生成拷贝代码，零运行时反射
- ✅ **类型安全** - 编译期类型检查，避免运行时错误
- ✅ **高性能** - 生成的代码直接调用 getter/setter，性能最优
- ✅ **易用** - 只需添加 `@CopyTarget` 注解即可
- ✅ **灵活** - 支持字段忽略、类型转换、集合处理
- ✅ **完整** - 支持双向拷贝、集合/Map/数组拷贝、嵌套对象深拷贝
- ✅ **多字段映射** - 支持多对一、一对多字段映射和表达式
- ✅ **类型转换器** - 内置数字、日期、枚举等转换器，支持自定义转换器
- ✅ **依赖注入** - 支持 Spring、CDI、JSR-330 等依赖注入框架
- ✅ **函数式定制** - 支持函数式后处理定制拷贝结果
- ✅ **更新现有对象** - 支持 updateDto/updateEntity 方法更新已存在的对象
- ✅ **映射前回调** - 支持在映射前执行验证、初始化等自定义逻辑
- ✅ **条件映射** - 支持基于条件决定是否映射字段
- ✅ **默认值和常量** - 支持设置字段的默认值和常量值
- ✅ **全局配置** - 支持包级别配置，减少重复配置
- 🆕 **嵌套对象深拷贝**（v1.3.2）- 自动深拷贝不同类型的嵌套对象，支持无限层级和混合模式
- 🆕 **统一集合定制**（v1.3.1）- 所有集合类型（List/Set/Map/Array）使用一致的 UnaryOperator 行为
- 🆕 **配置文件支持**（v1.3.1）- 支持通过 Properties 文件进行全局配置
- 🆕 **智能逆向转换**（v1.3.1）- 自动跳过不可逆的特殊字段映射

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.3.2</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.3.2</version>
    <scope>provided</scope>
</dependency>
```

### 2. 定义 DTO 类

```java
import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // getter/setter...
}
```

### 3. 编译并使用

```bash
mvn clean compile
```

```java
// 单个对象拷贝
UserDto userDto = UserDtoCopier.toDto(user);

// 反向拷贝
User converted = UserDtoCopier.fromDto(userDto);

// 集合拷贝
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

## v1.2 新功能

### 多对一映射（字段合并）

```java
@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
}
```

### 一对多映射（字段拆分）

```java
@CopyTarget(source = FullNameSource.class)
public class NameDto {
    @CopyField(source = "fullName", 
               expression = "java(source.getFullName().split(\" \")[0])")
    private String firstName;
    
    @CopyField(source = "fullName", 
               expression = "java(source.getFullName().split(\" \")[1])")
    private String lastName;
}
```

### 类型转换器

```java
@CopyTarget(source = Product.class)
public class ProductDto {
    // 数字格式化
    @CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
    private String priceText;
    
    // 日期格式化
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
    private String createTimeText;
}
```

### 自定义转换器

```java
public class PersonConverter {
    public String formatAge(Integer age) {
        return age + "岁";
    }
}

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
    
    public UserDto getUser(Long id) {
        return userDtoCopier.toDto(userRepository.findById(id));
    }
}
```

### 函数式定制

```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

## v1.3.1 新功能

### 统一 UnaryOperator 行为（重要变更）

v1.3.1 统一了所有集合类型的 UnaryOperator 行为，现在所有 customizer 都操作整个集合，提供一致且强大的定制能力：

**统一后的类型签名：**
- List: `UnaryOperator<List<T>>`
- Set: `UnaryOperator<Set<T>>`
- Map: `UnaryOperator<Map<K, T>>`
- Array: `UnaryOperator<T[]>`

**好处：**
- ✅ 一致的 API 设计，更容易理解和使用
- ✅ 更强大的灵活性：支持过滤、排序、限制、替换实现等操作
- ✅ 更直观：方法返回 `List<T>`，customizer 就操作 `List<T>`

```java
// List 过滤和排序
List<UserDto> result = UserDtoCopier.toDtoList(users, list -> 
    list.stream()
        .filter(dto -> dto.getPrice() >= 100)
        .sorted(Comparator.comparing(UserDto::getName))
        .limit(10)
        .collect(Collectors.toList())
);

// Set 转换为不可变集合
Set<UserDto> immutableSet = UserDtoCopier.toDtoSet(userSet, 
    Collections::unmodifiableSet);

// 如果需要修改每个元素，在 customizer 中遍历
List<UserDto> modified = UserDtoCopier.toDtoList(users, list -> {
    list.forEach(dto -> dto.setSource("batch"));
    return list;
});
```

### Map/Array UnaryOperator 重载（v1.3.1 新增）

新增 Map 和 Array 批量转换的 UnaryOperator 重载：

```java
// Map 过滤：移除 id 为 null 的条目
Map<String, UserDto> filteredMap = UserDtoCopier.toDtoMap(userMap, result -> {
    result.entrySet().removeIf(entry -> entry.getValue().getId() == null);
    return result;
});

// Map 转换为不可变集合
Map<String, UserDto> immutableMap = UserDtoCopier.toDtoMap(userMap, 
    Collections::unmodifiableMap);

// Array 过滤：移除 id 为 null 的元素
UserDto[] filteredArray = UserDtoCopier.toDtoArray(users, result -> 
    Arrays.stream(result)
        .filter(dto -> dto.getId() != null)
        .toArray(UserDto[]::new));

// Array 排序
UserDto[] sortedArray = UserDtoCopier.toDtoArray(users, result -> {
    Arrays.sort(result, Comparator.comparing(UserDto::getName));
    return result;
});
```

### Properties 配置文件支持

支持通过 `fast-bean-copier.properties` 文件进行全局配置，减少重复配置：

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

在 `fromDto/updateEntity` 方法中自动跳过使用了特殊映射配置的字段，避免逆向转换错误：

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
    
    // 映射前处理方法
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
    
    @CopyField(defaultValue = "0")
    private Integer count;
    
    // 设置常量值，不依赖源字段
    @CopyField(constant = "SYSTEM")
    private String createdBy;
}
```

### 全局配置（包级别）

```java
// package-info.java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

### Null 值处理策略

```java
// IGNORE 策略：只更新非 null 字段（默认）
// REPLACE 策略：更新所有字段，包括 null 值

@CopyTargetConfig(nullValueStrategy = NullValueStrategy.REPLACE)
package com.example.dto;
```

## 内置 TypeConverter

| 转换器 | 说明 | 示例 |
|--------|------|------|
| `NumberFormatter` | Number → String | `#,##0.00` |
| `NumberParser` | String → Number | `#,##0.00` |
| `DateFormatter` | Date → String | `yyyy-MM-dd` |
| `DateParser` | String → Date | `yyyy-MM-dd` |
| `EnumStringConverter` | Enum ↔ String | - |
| `JsonConverter` | Object ↔ JSON | - |

## 依赖注入支持

| 模式 | 注解 | 说明 |
|------|------|------|
| `DEFAULT` | 无 | 静态方法 |
| `SPRING` | `@Component` | Spring Bean |
| `CDI` | `@ApplicationScoped` | CDI Bean |
| `JSR330` | `@Named` + `@Singleton` | JSR-330 Bean |

## 文档

- [参考文档](docs/REFERENCE.md) - 完整的参考文档
- [快速入门指南](docs/GETTING_STARTED.md) - 5 分钟快速开始
- [API 文档](docs/API.md) - 详细的 API 文档
- [常见问题解答](docs/FAQ.md) - 常见问题和解答
- [更新日志](docs/CHANGELOG.md) - 版本更新记录
- [项目总结](docs/PROJECT_SUMMARY.md) - 项目完成情况总结

## 许可证

Apache License 2.0

## 作者

jackieonway

## 获取帮助

- 查看 [常见问题解答](docs/FAQ.md)
- 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出问题
