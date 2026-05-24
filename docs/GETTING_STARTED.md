# Fast Bean Copier 快速入门指南

> **v1.5.0 新特性**：Bean ↔ Map 转换支持（@CopyToMap/@CopyFromMap 注解、MapKeyStrategy 枚举、@CopyField.mapKey 属性）；API 清理（移除 beforeMapping 和单参数 customizer 废弃 API）。

## 5 分钟快速开始

### 步骤 1：添加依赖

在您的 `pom.xml` 中添加以下依赖：

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

## v1.5.0 新功能

### Bean → Map 转换

```java
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;
    private String firstName;          // key: "first_name"
    @CopyField(mapKey = "email_addr")
    private String email;              // key: "email_addr"（字段级优先）
}

Map<String, Object> map = UserDtoMapCopier.toMap(userDto);
List<Map<String, Object>> mapList = UserDtoMapCopier.toMapList(userDtos);
Set<Map<String, Object>> mapSet = UserDtoMapCopier.toMapSet(userDtoSet);
```

### Map → Bean 转换

```java
@CopyFromMap
public class UserDto {
    private Long id;
    private String name;
}

UserDto dto = UserDtoMapCopier.fromMap(map);
List<UserDto> dtos = UserDtoMapCopier.fromMapList(mapList);
```

### 函数式处理器（Bean ↔ Map）

```java
// Bean → Map 带处理器
Map<String, Object> map = UserDtoMapCopier.toMap(
    userDto,
    src -> { src.setName(src.getName().trim()); return src; },
    result -> { result.put("_ts", System.currentTimeMillis()); return result; }
);

// Map → Bean 带处理器
UserDto dto = UserDtoMapCopier.fromMap(
    map,
    src -> { src.remove("_ts"); return src; },
    result -> { result.setDisplayName(result.getName().toUpperCase()); return result; }
);
```

### 与 @CopyTarget 共存

```java
@CopyTarget(source = User.class)  // 生成 UserDtoCopier
@CopyToMap                         // 生成 UserDtoMapCopier（toMap）
@CopyFromMap                       // 生成 UserDtoMapCopier（fromMap）
public class UserDto { ... }
```

### 从 v1.4.x 迁移到 v1.5.0

v1.5.0 移除了 v1.4.0 中已废弃的 API，升级前请完成以下替换：

| 旧用法（v1.5.0 已移除） | 新用法 |
|------------------------|--------|
| `@CopyTarget(beforeMapping = "validate")` | `toDto(source, preProcessor, null)` |
| `toDto(source, customizer)` | `toDto(source, null, postProcessor)` |
| `fromDto(source, customizer)` | `fromDto(source, null, postProcessor)` |
| `toDtoList(sources, customizer)` | `toDtoList(sources, null, postProcessor)` |

Bean ↔ Map 转换为纯新增能力，现有 Bean ↔ Bean 代码在迁移废弃 API 后无需其他修改。

## v1.4.0 功能

### 函数式处理增强（preProcessor + postProcessor）

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

## 下一步

- 查看 [参考文档](REFERENCE.md) 了解更多功能
- 查看 [API 文档](API.md) 了解详细 API
- 查看 [FAQ](FAQ.md) 了解常见问题

## 许可证

Apache License 2.0
