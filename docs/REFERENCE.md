# Fast Bean Copier 1.1.0-SNAPSHOT 参考文档

## 前言

本文档是 Fast Bean Copier 的参考文档，Fast Bean Copier 是一个基于注解处理器的 Java Bean 拷贝工具，用于生成类型安全、高性能且零依赖的 Bean 映射代码。

本指南涵盖了 Fast Bean Copier 提供的所有功能。如果本指南无法回答您的所有问题，请在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出问题以获得帮助。

如果您在本指南中发现了错别字或其他错误，请通过在 GitHub 仓库中开启 Issue 或发送 Pull Request 来帮助我们改进。

本项目采用 Apache License 2.0 许可证。

## 1. 简介

Fast Bean Copier 是一个 Java [注解处理器](http://docs.oracle.com/javase/6/docs/technotes/guides/apt/index.html)，用于生成类型安全的 Bean 拷贝类。

您只需要定义一个目标 DTO 类并使用 `@CopyTarget` 注解标记它。在编译期间，Fast Bean Copier 会自动生成一个 Copier 类，该类包含 `toDto()` 和 `fromDto()` 等方法。这些方法使用普通的 Java 方法调用进行映射，不使用反射或类似的机制。

与手写映射代码相比，Fast Bean Copier 通过生成繁琐且容易出错的代码来节省时间。遵循"约定优于配置"的方法，Fast Bean Copier 使用合理的默认值，但在配置或实现特殊行为时不会妨碍您。

与动态映射框架相比，Fast Bean Copier 提供以下优势：

- **快速执行** - 使用普通方法调用而不是反射
- **编译期类型安全** - 只有对象和属性能够相互映射，不会意外地将一个订单实体映射到客户 DTO
- **清晰的编译时错误报告** - 如果：
  - 映射不完整（并非所有目标属性都被映射）
  - 映射不正确（找不到合适的映射方法或类型转换）
- **零运行时依赖** - 生成的代码不依赖任何外部库
- **支持集合映射** - 自动生成 List/Set 的映射方法

## 2. 设置

Fast Bean Copier 是基于 [JSR 269](http://www.jcp.org/en/jsr/detail?id=269) 的 Java 注解处理器，可以在命令行构建（javac、Ant、Maven 等）以及 IDE 中使用。

它包含以下工件：

- `com.github.jackieonway:fast-bean-copier-annotations` - 包含必需的注解，如 `@CopyTarget`
- `com.github.jackieonway:fast-bean-copier-processor` - 包含生成 Copier 实现的注解处理器

### 2.1. Apache Maven

要在 Maven 项目中使用 Fast Bean Copier，请在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.1.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

注意：处理器依赖应该使用 `provided` 作用域，因为它只在编译时需要。

### 2.2. Gradle

要在 Gradle 项目中使用 Fast Bean Copier，请在您的 `build.gradle` 中添加以下依赖：

```gradle
dependencies {
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.1.0-SNAPSHOT'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.1.0-SNAPSHOT'
}
```

### 2.3. Java 版本要求

Fast Bean Copier 需要 Java 8 或更高版本。

## 3. 基本使用

### 3.1. 定义 DTO 类

首先，定义您的源类和目标 DTO 类：

```java
// 源类
public class User {
    private Long id;
    private String name;
    private String email;
    private Integer age;
    
    // getter/setter...
}

// 目标 DTO 类
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

### 3.2. 编译

在编译时，Fast Bean Copier 会自动生成 `UserDtoCopier` 类：

```bash
mvn clean compile
```

生成的 Copier 类位于 `target/generated-sources/annotations/` 目录下。

### 3.3. 使用生成的 Copier 类

编译后，您可以使用生成的 Copier 类进行对象转换：

```java
// 单个对象拷贝
User user = new User(1L, "张三", "zhangsan@example.com", 25);
UserDto userDto = UserDtoCopier.toDto(user);

// 反向拷贝
User converted = UserDtoCopier.fromDto(userDto);

// 集合拷贝
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
Set<UserDto> userDtoSet = UserDtoCopier.toDtoSet(users);

// 反向集合拷贝
List<User> convertedUsers = UserDtoCopier.fromDtoList(userDtos);
Set<User> convertedUserSet = UserDtoCopier.fromDtoSet(userDtoSet);
```

## 4. @CopyTarget 注解

`@CopyTarget` 注解用于标记目标 DTO 类，指定源类和要忽略的字段。

### 4.1. 注解属性

```java
@CopyTarget(
    source = User.class,           // 必需：指定源类
    ignore = {"password", "token"} // 可选：指定要忽略的字段
)
public class UserDto {
    // ...
}
```

- **source** - 源类的类型。必需。
- **ignore** - 要忽略的字段名数组。可选。这些字段不会被拷贝。

### 4.2. 字段忽略示例

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // password 字段不会被拷贝
}
```

## 5. 数据类型转换

### 5.1. 同名字段自动拷贝

Fast Bean Copier 自动拷贝源类和目标类中同名的字段：

```java
public class User {
    private Long id;
    private String name;
    private String email;
}

@CopyTarget(source = User.class)
public class UserDto {
    private Long id;        // 自动从 User.id 拷贝
    private String name;    // 自动从 User.name 拷贝
    private String email;   // 自动从 User.email 拷贝
}
```

### 5.2. 基本类型与包装类型转换

Fast Bean Copier 自动支持基本类型与包装类型之间的转换：

```java
// 源类使用基本类型
public class User {
    private long id;
    private int age;
    private double salary;
}

// 目标 DTO 使用包装类型
@CopyTarget(source = User.class)
public class UserDto {
    private Long id;        // long -> Long（自动装箱）
    private Integer age;    // int -> Integer
    private Double salary;  // double -> Double
}
```

反向转换时，Fast Bean Copier 会自动处理 null 值：

```java
User user = UserDtoCopier.fromDto(userDto);
// 如果 userDto.age 为 null，则 user.age 为 0（基本类型的默认值）
```

### 5.3. 支持的类型转换

Fast Bean Copier 支持以下类型转换：

| 源类型 | 目标类型 | 说明 |
|--------|--------|------|
| `int` | `Integer` | 自动装箱 |
| `Integer` | `int` | 自动拆箱，null 转换为 0 |
| `long` | `Long` | 自动装箱 |
| `Long` | `long` | 自动拆箱，null 转换为 0L |
| `double` | `Double` | 自动装箱 |
| `Double` | `double` | 自动拆箱，null 转换为 0.0 |
| `float` | `Float` | 自动装箱 |
| `Float` | `float` | 自动拆箱，null 转换为 0.0f |
| `short` | `Short` | 自动装箱 |
| `Short` | `short` | 自动拆箱，null 转换为 0 |
| `byte` | `Byte` | 自动装箱 |
| `Byte` | `byte` | 自动拆箱，null 转换为 0 |
| `char` | `Character` | 自动装箱 |
| `Character` | `char` | 自动拆箱，null 转换为 '\u0000' |
| `boolean` | `Boolean` | 自动装箱 |
| `Boolean` | `boolean` | 自动拆箱，null 转换为 false |

## 6. 集合映射

### 6.1. List 映射

Fast Bean Copier 自动生成 List 映射方法：

```java
// 转换为 DTO 列表
List<UserDto> dtos = UserDtoCopier.toDtoList(users);

// 反向转换
List<User> users = UserDtoCopier.fromDtoList(dtos);
```

### 6.2. Set 映射

Fast Bean Copier 自动生成 Set 映射方法：

```java
// 转换为 DTO 集合
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);

// 反向转换
Set<User> userSet = UserDtoCopier.fromDtoSet(dtoSet);
```

### 6.3. 集合中的 null 处理

集合映射方法支持 null 值处理：

```java
// 传入 null 返回 null
List<UserDto> dtos = UserDtoCopier.toDtoList(null);  // 返回 null

// 集合中的 null 元素被保留
List<UserDto> dtos = UserDtoCopier.toDtoList(Arrays.asList(user1, null, user2));
// 结果中包含 3 个元素，第二个为 null
```

### 6.4. Map 与数组深拷贝（v1.1）

- Map：Key 通常直接拷贝（如 String/基本类型）；Value 按深拷贝规则处理，null Map/Value 保留；嵌套集合/数组递归处理。
- 数组：按元素深拷贝，支持多维数组与 null 元素。
- Raw/无界通配符集合将降级为浅拷贝并给出编译期警告，建议为集合声明明确泛型。
- 生成的顶层方法：`toDtoMap/fromDtoMap` 保留 Key 并对 Value 做深拷贝；`toDtoArray/fromDtoArray` 支持数组双向转换。

## 7. Null 值处理

### 7.1. 对象级别的 null 处理

所有映射方法都支持 null 值处理：

```java
// 传入 null 返回 null
UserDto dto = UserDtoCopier.toDto(null);  // 返回 null
User user = UserDtoCopier.fromDto(null);  // 返回 null
```

### 7.2. 字段级别的 null 处理

在字段映射时，null 值会被保留：

```java
User user = new User();
user.setId(1L);
user.setName(null);  // name 为 null

UserDto dto = UserDtoCopier.toDto(user);
// dto.name 也为 null
```

## 8. 生成的代码示例

### 8.1. 基本 Copier 类

编译后自动生成的 `UserDtoCopier` 类示例：

```java
public final class UserDtoCopier {
    private UserDtoCopier() {
        throw new AssertionError("No instances of UserDtoCopier");
    }
    
    public static UserDto toDto(User source) {
        if (source == null) return null;
        UserDto target = new UserDto();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setAge(source.getAge());
        return target;
    }
    
    public static User fromDto(UserDto source) {
        if (source == null) return null;
        User target = new User();
        target.setId(source.getId());
        target.setName(source.getName());
        target.setEmail(source.getEmail());
        target.setAge(source.getAge());
        return target;
    }
    
    public static List toDtoList(List sources) {
        if (sources == null) return null;
        List result = new ArrayList(sources.size());
        for (Object source : sources) {
            result.add(toDto((User) source));
        }
        return result;
    }
    
    public static Set toDtoSet(Set sources) {
        if (sources == null) return null;
        Set result = new HashSet();
        for (Object source : sources) {
            result.add(toDto((User) source));
        }
        return result;
    }
    
    public static List fromDtoList(List sources) {
        if (sources == null) return null;
        List result = new ArrayList(sources.size());
        for (Object source : sources) {
            result.add(fromDto((UserDto) source));
        }
        return result;
    }
    
    public static Set fromDtoSet(Set sources) {
        if (sources == null) return null;
        Set result = new HashSet();
        for (Object source : sources) {
            result.add(fromDto((UserDto) source));
        }
        return result;
    }
}
```

### 8.2. 带字段忽略的 Copier 类

当使用 `ignore` 属性时，被忽略的字段不会被拷贝：

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto {
    // ...
}

// 生成的代码中，password 字段不会被拷贝
public static UserDto toDto(User source) {
    if (source == null) return null;
    UserDto target = new UserDto();
    target.setId(source.getId());
    target.setName(source.getName());
    target.setEmail(source.getEmail());
    target.setAge(source.getAge());
    // password 不被拷贝
    return target;
}
```

## 9. 常见用例

### 9.1. API 响应 DTO

```java
// 数据库实体
public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
}

// API 响应 DTO（忽略敏感信息）
@CopyTarget(source = User.class, ignore = {"password"})
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
}

// 使用
User user = userRepository.findById(1L);
UserResponse response = UserResponseCopier.toDto(user);
```

### 9.2. 批量转换

```java
// 从数据库查询用户列表
List<User> users = userRepository.findAll();

// 转换为 DTO 列表
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);

// 返回给客户端
return ResponseEntity.ok(userDtos);
```

### 9.3. 双向转换

```java
// 从客户端接收 DTO
UserDto userDto = request.getBody();

// 转换为实体
User user = UserDtoCopier.fromDto(userDto);

// 保存到数据库
userRepository.save(user);

// 再转换回 DTO 返回
UserDto response = UserDtoCopier.toDto(user);
return ResponseEntity.ok(response);
```

## 10. 故障排除

### 10.1. 生成的代码未出现

**问题**：编译后在 `target/generated-sources/annotations/` 中找不到生成的 Copier 类。

**解决方案**：
1. 确保使用了 `@CopyTarget` 注解
2. 确保源类和目标类都有 getter/setter 方法
3. 运行 `mvn clean compile` 进行完整编译

### 10.2. 字段未被拷贝

**问题**：某些字段在拷贝后为 null。

**解决方案**：
1. 检查字段名是否完全相同（区分大小写）
2. 检查源类和目标类是否都有该字段的 getter/setter
3. 检查该字段是否在 `ignore` 属性中

### 10.3. 类型转换错误

**问题**：编译时出现类型不兼容的错误。

**解决方案**：
1. 检查源类和目标类的字段类型是否兼容
2. 对于不兼容的类型，在应用层手动处理

## 11. 性能考虑

### 11.1. 编译期代码生成

Fast Bean Copier 在编译期生成代码，因此：

- 运行时性能与手写代码相同
- 无反射开销
- 无动态代理开销
- 直接调用 getter/setter

### 11.2. 内存使用

生成的 Copier 类是无状态的，可以安全地在多线程环境中使用。

## 12. 最佳实践

### 12.1. 为每个 DTO 定义一个 @CopyTarget

```java
// ✅ 推荐
@CopyTarget(source = User.class)
public class UserDto { }

@CopyTarget(source = User.class)
public class UserResponse { }

// ❌ 不推荐
// 不要尝试在一个类中处理多个源类
```

### 12.2. 使用 ignore 属性排除敏感字段

```java
// ✅ 推荐
@CopyTarget(source = User.class, ignore = {"password", "secretKey"})
public class UserDto { }

// ❌ 不推荐
// 不要在 DTO 中包含敏感字段
```

### 12.3. 保持源类和 DTO 的字段一致

```java
// ✅ 推荐
public class User {
    private Long id;
    private String name;
    private String email;
}

@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;
    private String email;
}

// ❌ 不推荐
// 不要在 DTO 中添加源类中不存在的字段（除非有特殊原因）
```

## 13. 常见问题

### Q: 支持哪些 Java 版本？
**A**: Java 8 及以上版本。

### Q: 支持嵌套对象拷贝吗？
**A**: 支持。同名字段会直接拷贝。对于不同类型的嵌套对象，需要在应用层手动处理或为嵌套对象也定义 `@CopyTarget`。

### Q: 集合与数组的深拷贝支持到什么程度？
**A**: 1.1 版本内置 List/Set/Map/数组的双向深拷贝，支持嵌套组合（例如 `List<List<User>>`、`Map<String, List<User>>`、多维数组），并对原始类型或无界通配符自动降级为安全的浅拷贝。

### Q: 有哪些性能与使用建议？
**A**: 生成代码已为集合预分配容量并避免反射。实际使用中建议：  
1) 始终为集合声明明确的泛型参数，避免原始类型；  
2) 需要双向转换时同时定义 DTO 与源类的 `@CopyTarget`；  
3) 发布前执行 `mvn clean compile`, `mvn test`, `mvn jacoco:report` 验证兼容性与覆盖率（示例模块覆盖率可达 90%+）。

### Q: 支持自定义转换器吗？
**A**: 当前版本不支持。可以在应用层手动处理特殊字段的转换。

### Q: 生成的代码在哪里？
**A**: 在 `target/generated-sources/annotations/` 目录下。

### Q: 可以在 IDE 中看到生成的代码吗？
**A**: 可以。大多数 IDE（如 IntelliJ IDEA、Eclipse）都会自动识别生成的源代码。

### Q: 生成的代码是否线程安全？
**A**: 是的。生成的 Copier 类是无状态的，可以安全地在多线程环境中使用。

### Q: 支持 Lombok 吗？
**A**: 支持。Fast Bean Copier 可以与 Lombok 一起使用，Lombok 会生成 getter/setter 方法。

### Q: 支持 Builder 模式吗？
**A**: 当前版本不支持。Copier 类使用 setter 方法进行赋值。

## 14. 许可证

Fast Bean Copier 采用 Apache License 2.0 许可证。

## 15. 获取帮助

如有问题，请：

1. 查看本参考文档
2. 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中搜索相关问题
3. 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出新问题

## 16. 版本历史

### 1.0.0（2025-12-13）
- 初始版本发布
- 支持同名字段自动拷贝
- 支持基本类型与包装类型转换
- 支持字段忽略
- 支持 List/Set 集合拷贝
- 支持双向拷贝
- 完整的单元测试覆盖
