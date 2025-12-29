# Fast Bean Copier 1.2.0 参考文档

## 前言

本文档是 Fast Bean Copier 的参考文档，Fast Bean Copier 是一个基于注解处理器的 Java Bean 拷贝工具，用于生成类型安全、高性能且零依赖的 Bean 映射代码。

本指南涵盖了 Fast Bean Copier 提供的所有功能。如果本指南无法回答您的所有问题，请在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中提出问题以获得帮助。

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

## 2. 设置

### 2.1. Apache Maven

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.2.0</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.2.0</version>
    <scope>provided</scope>
</dependency>
```

### 2.2. Gradle

```gradle
dependencies {
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.2.0'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.2.0'
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
               expression = "source.getFirstName() + \" \" + source.getLastName()")
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
               expression = "source.getFullName() != null ? source.getFullName().split(\" \")[0] : null")
    private String firstName;
    
    @CopyField(source = "fullName", 
               expression = "source.getFullName() != null && source.getFullName().contains(\" \") ? source.getFullName().split(\" \")[1] : null")
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

## 10. 数据类型转换

### 10.1. 基本类型 ↔ 包装类型

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

### 10.2. 同名字段自动拷贝

对于同名字段，Fast Bean Copier 会自动拷贝。

## 11. 集合映射

### 11.1. List/Set 映射

```java
List<UserDto> dtos = UserDtoCopier.toDtoList(users);
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);

List<User> users = UserDtoCopier.fromDtoList(dtos);
Set<User> userSet = UserDtoCopier.fromDtoSet(dtoSet);
```

### 11.2. Map 映射

```java
Map<String, UserDto> dtoMap = UserDtoCopier.toDtoMap(userMap);
Map<String, User> userMap = UserDtoCopier.fromDtoMap(dtoMap);
```

### 11.3. 数组映射

```java
UserDto[] dtoArr = UserDtoCopier.toDtoArray(userArr);
User[] userArr = UserDtoCopier.fromDtoArray(dtoArr);
```

### 11.4. 深拷贝

List/Set/Map/数组字段会自动深拷贝，包括嵌套集合和多维数组。

### 11.5. Raw/通配符处理

Raw 类型或无界通配符集合会降级为浅拷贝并给出编译期警告。

## 12. Null 值处理

### 12.1. 对象级别

```java
UserDto dto = UserDtoCopier.toDto(null);  // 返回 null
```

### 12.2. 字段级别

null 值会被保留：

```java
User user = new User();
user.setName(null);
UserDto dto = UserDtoCopier.toDto(user);
// dto.name 也为 null
```

## 13. 生成的代码示例

### 13.1. DEFAULT 模式

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

### 13.2. SPRING 模式

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

## 14. 常见用例

### 14.1. API 响应 DTO

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserResponse {
    private Long id;
    private String username;
    private String email;
}
```

### 14.2. 批量转换

```java
List<User> users = userRepository.findAll();
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

### 14.3. 复杂字段映射

```java
@CopyTarget(source = Order.class, uses = OrderConverter.class)
public class OrderDto {
    @CopyField(source = {"items"}, 
               expression = "source.getItems().stream().mapToDouble(Item::getPrice).sum()")
    private double totalPrice;
    
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
    private String orderDate;
    
    @CopyField(qualifiedByName = "formatStatus")
    private String statusText;
}
```

## 15. 故障排除

### 15.1. 生成的代码未出现

1. 确保使用了 `@CopyTarget` 注解
2. 确保有 getter/setter 方法
3. 运行 `mvn clean compile`

### 15.2. 字段未被拷贝

1. 检查字段名是否相同
2. 检查是否有 getter/setter
3. 检查是否在 `ignore` 中

### 15.3. 表达式编译错误

1. 检查表达式语法
2. 使用 `source` 变量引用源对象
3. 添加 null 检查

## 16. 性能考虑

- 编译期代码生成，无运行时反射
- 直接调用 getter/setter
- TypeConverter 复用（静态实例或单例）
- 集合容量预分配

## 17. 最佳实践

1. 为每个 DTO 定义一个 `@CopyTarget`
2. 使用 `ignore` 排除敏感字段
3. 使用 TypeConverter 进行格式化
4. 使用函数式定制添加额外逻辑
5. 在 Spring 项目中使用 `ComponentModel.SPRING`

## 18. 版本历史

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

## 19. 许可证

Fast Bean Copier 采用 Apache License 2.0 许可证。

## 20. 获取帮助

- 查看本参考文档
- 在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中搜索或提问
