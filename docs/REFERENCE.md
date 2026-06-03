# Fast Bean Copier 1.5.0 参考文档

## 前言

本文档是 Fast Bean Copier 的参考文档，Fast Bean Copier 是一个基于注解处理器的 Java Bean 拷贝工具，用于生成类型安全、高性能且零依赖的 Bean 映射代码。

本指南涵盖了 Fast Bean Copier 提供的所有功能。如果本指南无法回答您的所有问题，请在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出问题以获得帮助。

本项目采用 Apache License 2.0 许可证。

## 1. 简介

Fast Bean Copier 是一个 Java 注解处理器，用于生成类型安全的 Bean 拷贝类。

您只需要定义一个目标 DTO 类并使用 `@CopyTarget` 注解标记它。在编译期间，Fast Bean Copier 会自动生成一个 Copier 类，该类包含 `toDto()` 和 `fromDto()` 等方法。

与动态映射框架相比，Fast Bean Copier 提供以下优势：

- **快速执行** - 使用普通方法调用而不是反射
- **编译期类型安全** - 编译期检查类型匹配
- **清晰的编译时错误报告** - 映射不完整或不正确时报错
- **零运行时依赖** - 生成的代码不依赖任何外部库
- **丰富的映射功能** - 支持多字段映射、类型转换、依赖注入等
- **Bean ↔ Map 转换** - @CopyToMap/@CopyFromMap 注解，生成 MapCopier 类（v1.5.0）
- **函数式处理** - preProcessor + postProcessor 双处理器 API（v1.5.0）
- **深拷贝控制** - @CopyField.deepCopy 字段级控制（v1.5.0）
- **更新现有对象** - updateDto/updateEntity 方法（v1.3）
- **条件映射和默认值** - condition/defaultValue/constant（v1.3）
- **全局配置** - 包级别配置 + Properties 文件（v1.3/v1.3.1）

## 2. 设置

### 2.1. Apache Maven

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.5.0</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.5.0</version>
    <scope>provided</scope>
</dependency>
```

### 2.2. Gradle

```gradle
dependencies {
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.5.0'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.5.0'
}
```

### 2.3. Java 版本要求

Fast Bean Copier 需要 Java 8 或更高版本。

## 3. 基本使用

### 3.1. 定义 DTO 类

```java
// 源类
public class User {
    private Long id;
    private String name;
    private String email;
    // getter/setter...
}

// 目标 DTO 类
@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // getter/setter...
}
```

### 3.2. 使用生成的 Copier 类

```java
// 单个对象拷贝
UserDto userDto = UserDtoCopier.toDto(user);

// 反向拷贝
User converted = UserDtoCopier.fromDto(userDto);

// 集合拷贝
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

## 4. @CopyTarget 注解

### 4.1. 注解属性

```java
@CopyTarget(
    source = User.class,           // 必需：指定源类
    ignore = {"password"},         // 可选：忽略的字段
    uses = {CustomConverter.class},// 可选：自定义转换器（v1.2）
    componentModel = ComponentModel.DEFAULT // 可选：组件模型（v1.2）
)
public class UserDto { }
```

### 4.2. 字段忽略

```java
@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserDto { }
```

## 5. @CopyField 注解（v1.2）

`@CopyField` 注解用于字段级映射和转换配置。

### 5.1. 多对一映射

将多个源字段合并到一个目标字段：

```java
public class Person {
    private String firstName;
    private String lastName;
}

@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
}
```

### 5.2. 一对多映射

将一个源字段拆分到多个目标字段：

```java
public class FullNameSource {
    private String fullName;
}

@CopyTarget(source = FullNameSource.class)
public class NameDto {
    @CopyField(source = "fullName", 
               expression = "java(source.getFullName() != null ? source.getFullName().split(\" \")[0] : null)")
    private String firstName;
    
    @CopyField(source = "fullName", 
               expression = "java(source.getFullName() != null && source.getFullName().contains(\" \") ? source.getFullName().split(\" \")[1] : null)")
    private String lastName;
}
```

### 5.3. 表达式语法

表达式是标准的 Java 代码，支持：
- 方法调用：`source.getXxx()`
- 链式调用：`source.getXxx().getYyy()`
- 流操作：`source.getList().stream().map(...).collect(...)`
- 三元运算符：`source.getXxx() != null ? source.getXxx() : defaultValue`
- 静态方法调用：`String.valueOf(source.getXxx())`

## 6. TypeConverter（v1.2）

### 6.1. 内置转换器

| 转换器 | 说明 | format 参数 |
|--------|------|-------------|
| `NumberFormatter` | Number → String 格式化 | DecimalFormat 格式字符串 |
| `NumberParser` | String → Number 解析 | DecimalFormat 格式字符串 |
| `DateFormatter` | Date/LocalDate/LocalDateTime → String | 日期格式字符串 |
| `DateParser` | String → 日期类型 | 日期格式字符串 |
| `EnumStringConverter` | Enum ↔ String/Integer | 枚举类全限定名 |
| `JsonConverter` | Object ↔ JSON String | 目标类全限定名 |

### 6.2. 数字格式化

```java
@CopyTarget(source = Product.class)
public class ProductDto {
    @CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
    private String priceText;
    
    @CopyField(converter = NumberFormatter.class, format = "0.00%")
    private String discountText;
}
```

### 6.3. 日期格式化

```java
@CopyTarget(source = Order.class)
public class OrderDto {
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
    private String createTimeText;
    
    @CopyField(converter = DateFormatter.class, format = "yyyy年MM月dd日")
    private String deliveryDateText;
}
```

### 6.4. 枚举转换

```java
@CopyTarget(source = User.class)
public class UserDto {
    @CopyField(converter = EnumStringConverter.class)
    private String statusText;  // Status.ACTIVE -> "ACTIVE"
}
```

## 7. 自定义转换器（v1.2）

### 7.1. 定义转换器

```java
public class PersonConverter {
    public String formatAge(Integer age) {
        return age != null ? age + "岁" : "未知";
    }
    
    public String formatStatus(Boolean active) {
        return active != null && active ? "活跃" : "非活跃";
    }
    
    public List<String> stringToList(String str) {
        if (str == null) return null;
        return Arrays.asList(str.split(","));
    }
}
```

### 7.2. 使用转换器

```java
@CopyTarget(source = Person.class, uses = PersonConverter.class)
public class PersonDto {
    @CopyField(qualifiedByName = "formatAge")
    private String ageText;
    
    @CopyField(qualifiedByName = "formatStatus")
    private String statusText;
    
    @CopyField(source = "tags", qualifiedByName = "stringToList")
    private List<String> tagList;
}
```

## 8. 依赖注入支持（v1.2）

### 8.1. ComponentModel 枚举

```java
public enum ComponentModel {
    DEFAULT,  // 无依赖注入，静态方法
    SPRING,   // Spring 框架，@Component
    CDI,      // CDI 框架，@ApplicationScoped
    JSR330    // JSR-330 标准，@Named + @Singleton
}
```

### 8.2. DEFAULT 模式

```java
@CopyTarget(source = User.class)
public class UserDto { }

// 生成的代码
public final class UserDtoCopier {
    private UserDtoCopier() {}
    public static UserDto toDto(User source) { ... }
}

// 使用
UserDto dto = UserDtoCopier.toDto(user);
```

### 8.3. SPRING 模式

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { }

// 生成的代码
@Component
public final class UserDtoCopier {
    private final CustomConverter customConverter;
    
    public UserDtoCopier(CustomConverter customConverter) {
        this.customConverter = customConverter != null 
            ? customConverter : new CustomConverter();
    }
    
    public UserDtoCopier() { this(null); }
    
    public UserDto toDto(User source) { ... }
}

// 使用
@Service
public class UserService {
    @Autowired
    private UserDtoCopier userDtoCopier;
    
    public UserDto getUser(Long id) {
        return userDtoCopier.toDto(userRepository.findById(id));
    }
}
```

### 8.4. CDI 模式

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.CDI)
public class UserDto { }

// 生成的代码
@ApplicationScoped
public final class UserDtoCopier { ... }
```

### 8.5. JSR330 模式

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.JSR330)
public class UserDto { }

// 生成的代码
@Named
@Singleton
public final class UserDtoCopier { ... }
```

## 9. 函数式定制拷贝（v1.2）

### 9.1. 基本用法

```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    result.setProcessedAt(LocalDateTime.now());
    return result;
});
```

### 9.2. 集合拷贝

```java
List<UserDto> dtos = UserDtoCopier.toDtoList(users, result -> {
    result.setSource("BATCH_IMPORT");
    return result;
});
```

### 9.3. 反向拷贝

```java
User entity = UserDtoCopier.fromDto(dto, result -> {
    result.setLastModified(Instant.now());
    return result;
});
```

### 9.4. Null 安全

当源对象为 null 时，customizer 函数不会被调用：

```java
UserDto dto = UserDtoCopier.toDto(null, result -> {
    // 这里不会执行
    return result;
});
// dto 为 null
```

## 10. 更新现有对象（v1.3）

### 10.1. updateDto 方法

更新已存在的 DTO 对象，而不是创建新对象：

```java
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");
existingDto.setAge(25);

User user = new User();
user.setName("新名称");
user.setAge(30);

UserDtoCopier.updateDto(existingDto, user);
// existingDto.name = "新名称", existingDto.age = 30
```

### 10.2. updateEntity 方法

更新已存在的实体对象（反向更新）：

```java
User existingUser = new User();
UserDtoCopier.updateEntity(existingUser, userDto);
```

### 10.3. 嵌套对象更新

支持嵌套对象的递归更新：

```java
@CopyTarget(source = Order.class)
public class OrderDto {
    private Long id;
    private AddressDto address;  // 嵌套对象
}

// 更新时，如果目标嵌套对象为 null，会自动创建新对象
OrderDto existingDto = new OrderDto();
existingDto.setAddress(null);
OrderDtoCopier.updateDto(existingDto, order);
// existingDto.address 会被创建并填充
```

### 10.4. 集合字段更新

支持 List、Set、Map、数组字段的更新：

```java
@CopyTarget(source = User.class)
public class UserDto {
    private List<String> roles;
}

// 默认策略：替换整个集合
UserDto existingDto = new UserDto();
existingDto.setRoles(Arrays.asList("USER"));
UserDtoCopier.updateDto(existingDto, user);
// existingDto.roles 被替换为 user.roles
```

## 11. 映射前回调（v1.3，v1.5 已移除）

> **v1.5.0 已移除** `beforeMapping` 属性。请使用 `preProcessor` 替代：
> ```java
> UserDto dto = UserDtoCopier.toDto(user,
>     source -> { /* 验证逻辑 */ return source; },
>     null
> );
> ```

### 11.1. beforeMapping 属性（已移除）

v1.5.0 已完全移除此属性。历史用法参考：

```java
// 旧用法（v1.5.0 已移除）
// @CopyTarget(source = User.class, beforeMapping = "validateAndPrepare")
// public class UserDto {
//     default void validateAndPrepare(User source) { ... }
// }
```

### 11.2. 迁移到 preProcessor

```java
UserDto dto = UserDtoCopier.toDto(user,
    source -> {
        if (source.getName() == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        return source;
    },
    null
);
```

## 12. 条件映射（v1.3）

### 12.1. condition 属性

在 @CopyField 中指定条件表达式：

```java
@CopyTarget(source = User.class)
public class UserDto {
    // 仅当源字段不为 null 时才映射
    @CopyField(condition = "java(source.getName() != null)")
    private String name;
    
    // 仅当年龄大于 18 时才映射
    @CopyField(condition = "java(source.getAge() != null && source.getAge() > 18)")
    private Integer age;
    
    // 复杂条件
    @CopyField(condition = "java(source.getStatus() != null && source.getStatus().equals(\"ACTIVE\"))")
    private String status;
}
```

### 12.2. 表达式格式

- 使用 `java(...)` 格式包裹条件表达式
- 表达式中 `source` 变量代表源对象
- 条件为 true 时执行映射，否则跳过该字段

### 12.3. 与其他属性组合

可与 expression、converter 等属性组合使用：

```java
@CopyField(
    condition = "java(source.getPrice() != null)",
    converter = NumberFormatter.class, 
    format = "#,##0.00"
)
private String priceText;
```

## 13. 默认值和常量（v1.3）

### 13.1. defaultValue 属性

当源字段为 null 时使用的默认值：

```java
@CopyTarget(source = User.class)
public class UserDto {
    @CopyField(defaultValue = "未知")
    private String name;
    
    @CopyField(defaultValue = "0")
    private Integer count;
    
    @CopyField(defaultValue = "0.0")
    private Double price;
    
    @CopyField(defaultValue = "false")
    private Boolean active;
}
```

### 13.2. 支持的类型

- String
- Integer、Long、Short、Byte
- Double、Float
- Boolean
- BigDecimal、BigInteger

### 13.3. constant 属性

直接设置常量值，不依赖源字段：

```java
@CopyTarget(source = User.class)
public class UserDto {
    @CopyField(constant = "SYSTEM")
    private String createdBy;
    
    @CopyField(constant = "1")
    private Integer version;
}
```

### 13.4. 注意事项

- `constant` 与 `defaultValue` 互斥
- 使用 `constant` 时，`source` 属性被忽略

## 14. 全局配置（v1.3）

### 14.1. @CopyTargetConfig 注解

包级别配置注解，为包内所有 @CopyTarget 提供默认配置：

```java
// package-info.java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

### 14.2. 配置属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `componentModel` | `ComponentModel` | `DEFAULT` | 默认组件模型 |
| `nullValueStrategy` | `NullValueStrategy` | `IGNORE` | 默认 null 值处理策略 |

### 14.3. 配置优先级

类级别 > 包级别 > 默认值

```java
// 包级别配置 SPRING
@CopyTargetConfig(componentModel = ComponentModel.SPRING)
package com.example.dto;

// 类级别覆盖为 DEFAULT
@CopyTarget(source = User.class, componentModel = ComponentModel.DEFAULT)
public class UserDto { }
```

## 15. NullValueStrategy（v1.3）

### 15.1. 枚举值

```java
public enum NullValueStrategy {
    IGNORE,   // 忽略 null 值，不更新目标字段（默认）
    REPLACE   // 替换 null 值，将目标字段设置为 null
}
```

### 15.2. 使用场景

主要用于 updateDto/updateEntity 方法：

```java
// IGNORE 策略（默认）
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");

User user = new User();
user.setName(null);

UserDtoCopier.updateDto(existingDto, user);
// existingDto.name 保持 "原始名称"（null 被忽略）

// REPLACE 策略
// existingDto.name 会被设置为 null
```

## 16. Map/Array 批量转换的 UnaryOperator 重载（v1.3.1）

### 16.1. Map 批量转换定制

Map 批量转换方法支持 UnaryOperator 重载，用于立即后处理：

```java
import java.util.function.UnaryOperator;
import java.util.Collections;

// 过滤 Map 条目 - 移除 id 为 null 的条目
Map<String, UserDto> filteredMap = UserDtoCopier.toDtoMap(userMap, result -> {
    result.entrySet().removeIf(entry -> entry.getValue().getId() == null);
    return result;
});

// 转换为不可变 Map
Map<String, UserDto> immutableMap = UserDtoCopier.toDtoMap(userMap, 
    result -> Collections.unmodifiableMap(result));

// 添加额外条目
Map<String, UserDto> enrichedMap = UserDtoCopier.toDtoMap(userMap, result -> {
    result.put("default", createDefaultUserDto());
    return result;
});

// 反向转换定制
Map<Long, User> customizedUsers = UserDtoCopier.fromDtoMap(dtoMap, result -> {
    // 自定义后处理逻辑
    return result;
});
```

**生成的方法签名**：
- `<K> Map<K, TargetDto> toDtoMap(Map<K, Source> sources, UnaryOperator<Map<K, TargetDto>> customizer)`
- `<K> Map<K, Source> fromDtoMap(Map<K, TargetDto> sources, UnaryOperator<Map<K, Source>> customizer)`

**Null 安全**：
- 当 `sources` 为 null 时，返回 null 而不调用 customizer
- 当 `customizer` 为 null 时，直接返回转换结果
- 当转换结果为 null 时，返回 null 而不调用 customizer

### 16.2. Array 批量转换定制

Array 批量转换方法支持 UnaryOperator 重载，用于立即后处理：

```java
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.UnaryOperator;

// 过滤数组元素 - 移除 id 为 null 的元素
UserDto[] filteredArray = UserDtoCopier.toDtoArray(users, result -> 
    Arrays.stream(result)
        .filter(dto -> dto.getId() != null)
        .toArray(UserDto[]::new));

// 排序数组
UserDto[] sortedArray = UserDtoCopier.toDtoArray(users, result -> {
    Arrays.sort(result, Comparator.comparing(UserDto::getName));
    return result;
});

// 限制数组大小 - 取前 10 个元素
UserDto[] limitedArray = UserDtoCopier.toDtoArray(users, result -> 
    Arrays.stream(result)
        .limit(10)
        .toArray(UserDto[]::new));

// 反向转换定制
User[] customizedUsers = UserDtoCopier.fromDtoArray(dtoArray, result -> {
    // 自定义后处理逻辑
    return result;
});
```

**生成的方法签名**：
- `TargetDto[] toDtoArray(Source[] sources, UnaryOperator<TargetDto[]> customizer)`
- `Source[] fromDtoArray(TargetDto[] sources, UnaryOperator<Source[]> customizer)`

**Null 安全**：
- 当 `sources` 为 null 时，返回 null 而不调用 customizer
- 当 `customizer` 为 null 时，直接返回转换结果
- 当转换结果为 null 时，返回 null 而不调用 customizer

## 17. Properties 配置文件支持（v1.3.1）

### 17.1. 配置文件格式

在 `src/main/resources/` 目录下创建 `fast-bean-copier.properties` 文件：

```properties
# 组件模型：DEFAULT, SPRING, CDI, JSR330
fast.bean.copier.componentModel=SPRING

# 空值策略：IGNORE, REPLACE
fast.bean.copier.nullValueStrategy=IGNORE
```

**支持的配置文件路径**：
1. `fast-bean-copier.properties`（类路径根目录）
2. `META-INF/fast-bean-copier.properties`

### 17.2. 配置优先级

```
类级别配置 (@CopyTarget)
    ↓ 覆盖
包级别配置 (@CopyTargetConfig)
    ↓ 覆盖
配置文件配置 (fast-bean-copier.properties)
    ↓ 覆盖
默认值
```

### 17.3. 使用示例

```java
// 类级别配置优先级最高
@CopyTarget(source = User.class, componentModel = ComponentModel.DEFAULT)
public class UserDto { }  // 使用 DEFAULT（静态方法）

// 未配置时使用配置文件中的设置
@CopyTarget(source = Product.class)
public class ProductDto { }  // 使用配置文件中的 SPRING

// 包级别配置
// package-info.java
@CopyTargetConfig(componentModel = ComponentModel.CDI)
package com.example.dto;
```

### 17.4. 配置优势

- **集中管理**：统一管理项目配置
- **减少重复**：避免在每个 DTO 上重复配置
- **易于修改**：修改配置文件即可影响所有 Copier
- **团队协作**：统一团队配置风格

## 18. 逆向转换智能跳过（v1.3.1）

### 18.1. 自动跳过特殊字段

在 `fromDto()` 和 `updateEntity()` 方法中，使用了特殊映射配置的字段会自动跳过：

```java
@CopyTarget(source = User.class)
public class UserDto {
    // TypeConverter 字段 - 自动跳过
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
    private String createTimeText;
    
    // Expression 字段 - 自动跳过
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
    
    // QualifiedByName 字段 - 自动跳过
    @CopyField(qualifiedByName = "formatAge")
    private String ageText;
    
    // Constant 字段 - 自动跳过
    @CopyField(constant = "SYSTEM")
    private String createdBy;
}
```

### 18.2. 生成的代码示例

```java
public static User fromDto(UserDto source) {
    if (source == null) {
        return null;
    }
    User target = new User();
    target.setId(source.getId());
    target.setName(source.getName());
    // 类型转换器映射 'createTimeText' 不可逆，在 fromDto() 中跳过
    // 表达式映射 'fullName' 不可逆，在 fromDto() 中跳过
    // 具名方法映射 'ageText' 不可逆，在 fromDto() 中跳过
    // 常量映射 'createdBy' 不可逆，在 fromDto() 中跳过
    return target;
}
```

### 18.3. 跳过原因

| 映射类型 | 跳过原因 | 注释文本 |
|---------|---------|---------|
| `typeConverter` | 类型转换器通常是单向的 | "类型转换器映射 '{字段名}' 不可逆，在 fromDto() 中跳过" |
| `expression` | 表达式是计算得出的 | "表达式映射 '{字段名}' 不可逆，在 fromDto() 中跳过" |
| `qualifiedByName` | 具名方法是自定义转换 | "具名方法映射 '{字段名}' 不可逆，在 fromDto() 中跳过" |
| `constant` | 常量值不依赖源字段 | "常量映射 '{字段名}' 不可逆，在 fromDto() 中跳过" |

## 19. Bean ↔ Map 转换（v1.5）

### 19.1. @CopyToMap 注解

标记类支持 Bean → Map 转换，生成 `{Class}MapCopier` 类：

```java
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;
    private String firstName;          // key: "first_name"
    @CopyField(mapKey = "email_addr")
    private String email;              // key: "email_addr"（字段级优先）
    private LocalDateTime createTime;  // key: "create_time"
}
```

**生成的 MapCopier 类方法**：
- `toMap(T source)` - 基础转换方法
- `toMap(T source, preProcessor, postProcessor)` - 函数式方法
- `toMapList(List<T> sources)` / `toMapList(sources, pre, post)` - 批量 List 转换
- `toMapSet(Set<T> sources)` / `toMapSet(sources, pre, post)` - 批量 Set 转换

### 19.2. @CopyFromMap 注解

标记类支持 Map → Bean 转换：

```java
@CopyFromMap
public class UserDto {
    private Long id;
    private String name;
    private String email;
}
```

**生成的 MapCopier 类方法**：
- `fromMap(Map<String, Object> source)` - 基础转换方法
- `fromMap(source, preProcessor, postProcessor)` - 函数式方法
- `fromMapList` / `fromMapSet` 批量方法

### 19.3. MapKeyStrategy 枚举

Map key 命名策略：

```java
public enum MapKeyStrategy {
    /** 使用字段名作为 Map key（默认策略） */
    FIELD_NAME,
    
    /** 使用驼峰命名作为 Map key */
    CAMEL_CASE,
    
    /** 使用下划线命名（snake_case）作为 Map key */
    SNAKE_CASE,
    
    /** 使用自定义 key，需配合 @CopyField.mapKey() 指定具体 key 值 */
    CUSTOM
}
```

**使用示例**：
```java
// 字段名策略（默认）
@CopyToMap(keyStrategy = MapKeyStrategy.FIELD_NAME)
public class UserDto {
    private String firstName;  // key: "firstName"
}

// 下划线策略
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private String firstName;  // key: "first_name"
}

// 自定义策略
@CopyToMap(keyStrategy = MapKeyStrategy.CUSTOM)
public class UserDto {
    @CopyField(mapKey = "user_id")
    private Long id;           // key: "user_id"
}
```

### 19.4. @CopyField.mapKey 属性

字段级自定义 Map key，优先级高于 keyStrategy：

```java
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;                    // key: "id"
    private String firstName;           // key: "first_name"
    @CopyField(mapKey = "email_address")
    private String email;               // key: "email_address"（字段级优先）
    @CopyField(mapKey = "created_at")
    private LocalDateTime createTime;   // key: "created_at"（字段级优先）
}
```

### 19.5. 双向转换

同一类可同时标注 `@CopyToMap` + `@CopyFromMap`，生成双向方法：

```java
@CopyToMap
@CopyFromMap
public class UserDto {
    private Long id;
    private String name;
    private String email;
}

// 使用
Map<String, Object> map = UserDtoMapCopier.toMap(userDto);
UserDto dto = UserDtoMapCopier.fromMap(map);
```

### 19.6. 与 @CopyTarget 共存

独立生成 BeanCopier 和 MapCopier，互不影响：

```java
@CopyTarget(source = User.class)  // 生成 UserDtoCopier
@CopyToMap                         // 生成 UserDtoMapCopier（toMap）
@CopyFromMap                       // 生成 UserDtoMapCopier（fromMap）
public class UserDto {
    private Long id;
    private String name;
}

// 使用
UserDto dto = UserDtoCopier.toDto(user);          // Bean 转换
Map<String, Object> map = UserDtoMapCopier.toMap(dto);  // Map 转换
UserDto dto2 = UserDtoMapCopier.fromMap(map);     // Map 转 Bean
```

### 19.7. 函数式处理器

支持 preProcessor 和 postProcessor 双处理器：

```java
// Bean → Map 带处理器
Map<String, Object> map = UserDtoMapCopier.toMap(
    userDto,
    src -> { 
        src.setName(src.getName().trim()); 
        return src; 
    },
    result -> { 
        result.put("_ts", System.currentTimeMillis()); 
        return result; 
    }
);

// Map → Bean 带处理器
UserDto dto = UserDtoMapCopier.fromMap(
    map,
    src -> { 
        src.remove("_ts"); 
        return src; 
    },
    result -> { 
        result.setDisplayName(result.getName().toUpperCase()); 
        return result; 
    }
);
```

### 19.8. 类型转换支持

支持 TypeConverter 和 @CopyField 配置：

```java
@CopyToMap
public class ProductDto {
    @CopyField(converter = NumberFormatter.class, format = "#,##0.00")
    private String priceText;  // 转换器生效
    
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;   // 表达式生效
}
```

### 19.9. 使用场景

| 场景 | 推荐配置 | 说明 |
|------|----------|------|
| REST API 响应 | `@CopyToMap` | 将 DTO 转换为 Map 作为 JSON 响应 |
| 配置文件读取 | `@CopyFromMap` | 将 Map 配置转换为 Bean |
| 数据库结果集 | `@CopyFromMap` | 将数据库查询结果转换为 Bean |
| 缓存存储 | `@CopyToMap` + `@CopyFromMap` | 双向转换，适合缓存场景 |
| 外部系统集成 | `@CopyToMap` | 转换为通用 Map 格式进行传输 |

## 21. 函数式处理增强（v1.4）

### 21.1. preProcessor + postProcessor 双处理器

v1.4.0 引入了统一的双处理器 API，提供更灵活的函数式处理能力：

```java
// 单对象转换
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

// 反向转换
User entity = UserDtoCopier.fromDto(
    dto,
    source -> { /* 预处理 DTO */ return source; },
    target -> { /* 后处理 Entity */ return target; }
);
```

### 21.2. 集合方法双处理器支持

所有集合方法都支持双处理器：

```java
// List 转换
List<UserDto> dtos = UserDtoCopier.toDtoList(
    users,
    source -> { /* 预处理每个 source */ return source; },
    target -> { /* 后处理每个 target */ return target; }
);

// Set 转换
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(
    userSet,
    source -> { /* 预处理 */ return source; },
    target -> { /* 后处理 */ return target; }
);

// Map 转换
Map<String, UserDto> dtoMap = UserDtoCopier.toDtoMap(
    userMap,
    source -> { /* 预处理 */ return source; },
    target -> { /* 后处理 */ return target; }
);

// Array 转换
UserDto[] dtoArray = UserDtoCopier.toDtoArray(
    userArray,
    source -> { /* 预处理 */ return source; },
    target -> { /* 后处理 */ return target; }
);
```

### 21.3. 执行顺序

处理器的执行顺序为：

```
preProcessor → 字段拷贝 → postProcessor
```

### 21.4. Null 安全

- 当 `source` 为 null 时，不调用任何处理器，直接返回 null
- 当 `preProcessor` 为 null 时，跳过预处理
- 当 `postProcessor` 为 null 时，跳过后处理

### 21.5. 废弃方法迁移（v1.5.0 已移除）

单参数 `customizer` 重载已在 v1.5.0 中完全移除，请使用双处理器 API：

```java
// 旧方法（v1.5.0 已移除）
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});

// 新方法
UserDto dto = UserDtoCopier.toDto(user, null, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

## 22. 深拷贝控制（v1.4）

### 22.1. @CopyField.deepCopy 属性

v1.4.0 新增 `deepCopy` 属性，用于字段级控制深拷贝行为：

```java
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    // 深拷贝（默认）：创建新的 AddressDto 对象
    @CopyField(deepCopy = true)
    private AddressDto address;
    
    // 浅拷贝：直接引用传递
    @CopyField(deepCopy = false)
    private DepartmentDto department;
}
```

### 20.2. 嵌套对象深拷贝控制

```java
@CopyTarget(source = Order.class)
public class OrderDto {
    // 深拷贝：调用 CustomerDtoCopier.toDto() 创建新对象
    @CopyField(deepCopy = true)
    private CustomerDto customer;
    
    // 浅拷贝：直接赋值引用
    @CopyField(deepCopy = false)
    private StatusDto status;
}
```

生成的代码：

```java
public static OrderDto toDto(Order source) {
    if (source == null) return null;
    OrderDto target = new OrderDto();
    
    // 深拷贝
    target.setCustomer(source.getCustomer() != null 
        ? CustomerDtoCopier.toDto(source.getCustomer()) 
        : null);
    
    // 浅拷贝
    target.setStatus(source.getStatus());
    
    return target;
}
```

### 20.3. 集合深拷贝控制

```java
@CopyTarget(source = Project.class)
public class ProjectDto {
    // 集合深拷贝（默认）：拷贝集合并深拷贝每个元素
    @CopyField(deepCopy = true)
    private List<TaskDto> tasks;
    
    // 集合浅拷贝：拷贝集合但元素直接引用
    @CopyField(deepCopy = false)
    private List<TagDto> tags;
}
```

生成的代码：

```java
// 深拷贝：拷贝集合并深拷贝元素
if (source.getTasks() != null) {
    List<TaskDto> taskList = new ArrayList<>(source.getTasks().size());
    for (Task item : source.getTasks()) {
        taskList.add(item != null ? TaskDtoCopier.toDto(item) : null);
    }
    target.setTasks(taskList);
}

// 浅拷贝：拷贝集合但元素直接引用
if (source.getTags() != null) {
    target.setTags(new ArrayList<>(source.getTags()));
}
```

### 20.4. 数组深拷贝控制

```java
@CopyTarget(source = Document.class)
public class DocumentDto {
    // 数组深拷贝（默认）
    @CopyField(deepCopy = true)
    private AttachmentDto[] attachments;
    
    // 数组浅拷贝
    @CopyField(deepCopy = false)
    private String[] tags;
}
```

### 20.5. 使用场景

| 场景 | deepCopy 设置 | 说明 |
|------|--------------|------|
| 性能优化 | `false` | 对不需要深拷贝的字段使用浅拷贝 |
| 共享引用 | `false` | 多个对象共享同一个嵌套对象实例 |
| 不可变对象 | `false` | 对不可变对象（如枚举）使用浅拷贝 |
| 大对象 | `false` | 避免大型嵌套对象的内存占用 |
| 独立副本 | `true` | 需要完全独立的对象副本 |
| 修改安全 | `true` | 修改副本不影响原对象 |

### 20.6. 默认行为

`deepCopy` 的默认值为 `true`，保持与之前版本一致的深拷贝行为。如果需要浅拷贝，显式设置 `deepCopy = false`。

## 21. 数据类型转换

### 21.1. 基本类型 ↔ 包装类型

Fast Bean Copier 自动支持基本类型与包装类型之间的转换：

| 源类型 | 目标类型 | null 处理 |
|--------|--------|-----------|
| `int` | `Integer` | 自动装箱 |
| `Integer` | `int` | null → 0 |
| `long` | `Long` | 自动装箱 |
| `Long` | `long` | null → 0L |
| `double` | `Double` | 自动装箱 |
| `Double` | `double` | null → 0.0 |
| `boolean` | `Boolean` | 自动装箱 |
| `Boolean` | `boolean` | null → false |

### 21.2. 同名字段自动拷贝

对于同名字段，Fast Bean Copier 会自动拷贝。

## 22. 集合映射

### 22.1. List/Set 映射

```java
List<UserDto> dtos = UserDtoCopier.toDtoList(users);
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);

List<User> users = UserDtoCopier.fromDtoList(dtos);
Set<User> userSet = UserDtoCopier.fromDtoSet(dtoSet);
```

### 22.2. Map 映射

```java
Map<String, UserDto> dtoMap = UserDtoCopier.toDtoMap(userMap);
Map<String, User> userMap = UserDtoCopier.fromDtoMap(dtoMap);
```

### 22.3. 数组映射

```java
UserDto[] dtoArr = UserDtoCopier.toDtoArray(userArr);
User[] userArr = UserDtoCopier.fromDtoArray(dtoArr);
```

### 22.4. 深拷贝

List/Set/Map/数组字段会自动深拷贝，包括嵌套集合和多维数组。

### 22.5. Raw/通配符处理

Raw 类型或无界通配符集合会降级为浅拷贝并给出编译期警告。

## 23. Null 值处理

### 23.1. 对象级别

```java
UserDto dto = UserDtoCopier.toDto(null);  // 返回 null
```

### 23.2. 字段级别

null 值会被保留：

```java
User user = new User();
user.setName(null);
UserDto dto = UserDtoCopier.toDto(user);
// dto.name 也为 null
```

## 24. 生成的代码示例

### 24.1. DEFAULT 模式

```java
public final class UserDtoCopier {
    private UserDtoCopier() {
        throw new AssertionError("No instances");
    }
    
    public static UserDto toDto(User source) {
        if (source == null) return null;
        UserDto target = new UserDto();
        target.setId(source.getId());
        target.setName(source.getName());
        return target;
    }
    
    public static UserDto toDto(User source, UnaryOperator<UserDto> customizer) {
        UserDto result = toDto(source);
        if (result != null && customizer != null) {
            result = customizer.apply(result);
        }
        return result;
    }
    
    public static User fromDto(UserDto source) { ... }
    public static List<UserDto> toDtoList(List<User> sources) { ... }
    public static List<UserDto> toDtoList(List<User> sources, UnaryOperator<UserDto> customizer) { ... }
    // ... 其他方法
}
```

### 24.2. SPRING 模式

```java
@Component
public final class UserDtoCopier {
    private final NumberFormatter numberFormatter;
    
    public UserDtoCopier(NumberFormatter numberFormatter) {
        this.numberFormatter = numberFormatter != null 
            ? numberFormatter : new NumberFormatter();
    }
    
    public UserDtoCopier() { this(null); }
    
    public UserDto toDto(User source) {
        if (source == null) return null;
        UserDto target = new UserDto();
        target.setId(source.getId());
        target.setPriceText(numberFormatter.convert(source.getPrice(), "#,##0.00"));
        return target;
    }
    
    // ... 其他方法
}
```

## 25. 常见用例

### 25.1. API 响应 DTO

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserResponse {
    private Long id;
    private String username;
    private String email;
}
```

### 25.2. 批量转换

```java
List<User> users = userRepository.findAll();
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

### 25.3. 复杂字段映射

```java
@CopyTarget(source = Order.class, uses = OrderConverter.class)
public class OrderDto {
    @CopyField(source = {"items"}, 
               expression = "java(source.getItems().stream().mapToDouble(Item::getPrice).sum())")
    private double totalPrice;
    
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
    private String orderDate;
    
    @CopyField(qualifiedByName = "formatStatus")
    private String statusText;
}
```

### 25.4. 更新现有对象（v1.3）

```java
// 部分更新场景
UserDto existingDto = userService.getUser(id);
UserDtoCopier.updateDto(existingDto, partialUser);
```

### 25.5. 函数式处理（v1.4）

```java
// 预处理和后处理
UserDto dto = UserDtoCopier.toDto(
    user,
    source -> {
        source.setName(source.getName().trim());
        return source;
    },
    target -> {
        target.setDisplayName(target.getName().toUpperCase());
        return target;
    }
);
```

### 25.6. 深拷贝控制（v1.4）

```java
@CopyTarget(source = Order.class)
public class OrderDto {
    // 深拷贝：完全独立的副本
    @CopyField(deepCopy = true)
    private CustomerDto customer;
    
    // 浅拷贝：共享引用，性能优化
    @CopyField(deepCopy = false)
    private StatusDto status;
}
```

## 26. 故障排除

### 26.1. 生成的代码未出现

1. 确保使用了 `@CopyTarget` 注解
2. 确保有 getter/setter 方法
3. 运行 `mvn clean compile`

### 26.2. 字段未被拷贝

1. 检查字段名是否相同
2. 检查是否有 getter/setter
3. 检查是否在 `ignore` 中

### 26.3. 表达式编译错误

1. 检查表达式语法
2. 使用 `source` 变量引用源对象
3. 添加 null 检查

## 27. 性能考虑

- 编译期代码生成，无运行时反射
- 直接调用 getter/setter
- TypeConverter 复用（静态实例或单例）
- 集合容量预分配

## 28. 最佳实践

1. 为每个 DTO 定义一个 `@CopyTarget`
2. 使用 `ignore` 排除敏感字段
3. 使用 TypeConverter 进行格式化
4. 使用函数式定制添加额外逻辑
5. 在 Spring 项目中使用 `ComponentModel.SPRING`
6. 使用 `@CopyTargetConfig` 减少重复配置（v1.3）
7. 使用 `updateDto/updateEntity` 进行部分更新（v1.3）
8. 使用 `UnaryOperator` 重载进行批量转换定制（v1.3.1）
9. 使用 Properties 配置文件统一管理配置（v1.3.1）
10. 使用 `preProcessor` 和 `postProcessor` 进行函数式处理（v1.4）
11. 使用 `deepCopy` 属性优化性能和控制拷贝行为（v1.4）
12. 使用 `@CopyToMap`/`@CopyFromMap` 进行 Bean ↔ Map 转换（v1.5）
13. 使用 `MapKeyStrategy` 统一控制 Map key 命名风格（v1.5）

## 29. 版本历史

### 1.5.0（2026-06-03）
- Bean ↔ Map 转换：@CopyToMap/@CopyFromMap 注解
- MapKeyStrategy 枚举：FIELD_NAME / CAMEL_CASE / SNAKE_CASE / CUSTOM
- @CopyField.mapKey 属性：字段级自定义 Map key
- API 清理：完全移除 beforeMapping 和单参数 customizer 重载
- 481 测试用例，覆盖率 95%+

### 1.4.0（2026-03-29）
- 函数式处理增强：preProcessor + postProcessor 双处理器 API
- 深拷贝控制：@CopyField.deepCopy 属性
- 移除废弃 API：beforeMapping 和单参数 customizer 重载
- 所有集合方法支持双处理器
- 461 测试用例，覆盖率 95%+

### 1.3.2（2026-02-03）
- 嵌套对象深拷贝支持
- 自动深拷贝不同类型的嵌套对象
- 无限层级嵌套和混合模式支持
- 440+ 测试用例

### 1.3.1（2026-01-28）
- Map/Array 批量转换的 UnaryOperator 重载
- Properties 配置文件支持
- 逆向转换智能跳过特殊字段
- 配置优先级合并器
- 66+ 新增测试用例

### 1.3.0（2026-01-14）
- 更新现有对象：updateDto/updateEntity 方法
- 条件映射：condition 属性
- 默认值和常量：defaultValue/constant 属性
- 全局配置：@CopyTargetConfig 注解
- Null 值处理策略：NullValueStrategy 枚举

### 1.2.1（2026-01-08）
- 处理器架构重构
- 新增组件：ProcessorContext、AnnotationExtractor、FieldMappingAnalyzer
- 新增生成器：ClassStructureGenerator、BasicMethodGenerator、CollectionMethodGenerator、FieldCopyGenerator、DeepCopyGenerator
- 代码可维护性显著提升

### 1.2.0（2025-12-29）
- 多字段映射（多对一、一对多）
- TypeConverter 类型转换器
- 表达式映射
- 依赖注入支持
- 函数式定制拷贝

### 1.1.0（2025-12-23）
- 集合深拷贝
- 嵌套集合与多维数组
- Raw/通配符降级

### 1.0.0（2025-12-13）
- 初始版本

## 30. 许可证

Fast Bean Copier 采用 Apache License 2.0 许可证。

## 31. 获取帮助

- 查看本参考文档
- 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中搜索或提问
