# Fast Bean Copier API 文档

> v1.1 提示：新增 List/Set/Map/数组字段的深拷贝与反向拷贝，支持嵌套集合、多维数组，raw/无界通配符集合自动降级为浅拷贝并输出编译期警告。

## 注解

### @CopyTarget

标记目标 DTO 类，指定源类和要忽略的字段。

#### 声明

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyTarget {
    /**
     * 源类的类型。必需。
     */
    Class<?> source();
    
    /**
     * 要忽略的字段名数组。可选。
     */
    String[] ignore() default {};
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `source` | `Class<?>` | 是 | 源类的类型 |
| `ignore` | `String[]` | 否 | 要忽略的字段名数组 |

#### 示例

```java
// 基本用法
@CopyTarget(source = User.class)
public class UserDto {
    // ...
}

// 忽略字段
@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserResponse {
    // ...
}
```

## 生成的 Copier 类

### 方法

#### toDto(source)

将源对象转换为目标 DTO 对象。

**签名**：
```java
public static TargetType toDto(SourceType source)
```

**参数**：
- `source` - 源对象

**返回值**：
- 目标 DTO 对象，如果源对象为 null，返回 null

**示例**：
```java
User user = new User(1L, "张三", "zhangsan@example.com", 25);
UserDto userDto = UserDtoCopier.toDto(user);
```

#### fromDto(source)

将目标 DTO 对象转换回源对象（反向拷贝）。

**签名**：
```java
public static SourceType fromDto(TargetType source)
```

**参数**：
- `source` - 目标 DTO 对象

**返回值**：
- 源对象，如果目标对象为 null，返回 null

**示例**：
```java
UserDto userDto = new UserDto(1L, "张三", "zhangsan@example.com", 25);
User user = UserDtoCopier.fromDto(userDto);
```

#### toDtoList(sources)

将源对象列表转换为目标 DTO 对象列表。

**签名**：
```java
public static java.util.List<TargetType> toDtoList(java.util.List<SourceType> sources)
```

**参数**：
- `sources` - 源对象列表

**返回值**：
- 目标 DTO 对象列表，如果源列表为 null，返回 null

**示例**：
```java
List<User> users = userRepository.findAll();
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

#### toDtoSet(sources)

将源对象集合转换为目标 DTO 对象集合。

**签名**：
```java
public static java.util.Set<TargetType> toDtoSet(java.util.Set<SourceType> sources)
```

**参数**：
- `sources` - 源对象集合

**返回值**：
- 目标 DTO 对象集合，如果源集合为 null，返回 null

**示例**：
```java
Set<User> users = new HashSet<>(userRepository.findAll());
Set<UserDto> userDtos = UserDtoCopier.toDtoSet(users);
```

#### fromDtoList(sources)

将目标 DTO 对象列表转换回源对象列表（反向拷贝）。

**签名**：
```java
public static java.util.List<SourceType> fromDtoList(java.util.List<TargetType> sources)
```

**参数**：
- `sources` - 目标 DTO 对象列表

**返回值**：
- 源对象列表，如果目标列表为 null，返回 null

**示例**：
```java
List<UserDto> userDtos = request.getBody();
List<User> users = UserDtoCopier.fromDtoList(userDtos);
```

#### fromDtoSet(sources)

将目标 DTO 对象集合转换回源对象集合（反向拷贝）。

**签名**：
```java
public static java.util.Set<SourceType> fromDtoSet(java.util.Set<TargetType> sources)
```

#### toDtoMap(sources)

将源对象 Map 转换为目标 DTO Map（保留 Key，拷贝 Value）。

**签名**：
```java
public static <K> java.util.Map<K, TargetType> toDtoMap(java.util.Map<K, SourceType> sources)
```

#### fromDtoMap(sources)

将目标 DTO Map 反向转换为源对象 Map。

**签名**：
```java
public static <K> java.util.Map<K, SourceType> fromDtoMap(java.util.Map<K, TargetType> sources)
```

#### toDtoArray(sources)

将源对象数组转换为目标 DTO 数组。

**签名**：
```java
public static TargetType[] toDtoArray(SourceType[] sources)
```

#### fromDtoArray(sources)

将目标 DTO 数组转换回源对象数组。

**签名**：
```java
public static SourceType[] fromDtoArray(TargetType[] sources)
```

**参数**：
- `sources` - 目标 DTO 对象集合

**返回值**：
- 源对象集合，如果目标集合为 null，返回 null

**示例**：
```java
Set<UserDto> userDtos = request.getBody();
Set<User> users = UserDtoCopier.fromDtoSet(userDtos);
```

## 类型转换

### 支持的类型转换

Fast Bean Copier 自动支持以下类型转换：

#### 基本类型 ↔ 包装类型

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

#### 同名字段拷贝

对于同名字段，Fast Bean Copier 会自动拷贝，无论类型是否相同（只要兼容）。

**示例**：
```java
public class User {
    private String name;  // String 类型
}

@CopyTarget(source = User.class)
public class UserDto {
    private String name;  // 自动从 User.name 拷贝
}
```

## Null 值处理

### 对象级别的 null 处理

所有映射方法都支持 null 值处理：

```java
UserDto dto = UserDtoCopier.toDto(null);  // 返回 null
User user = UserDtoCopier.fromDto(null);  // 返回 null
List<UserDto> dtos = UserDtoCopier.toDtoList(null);  // 返回 null
```

### 字段级别的 null 处理

在字段映射时，null 值会被保留：

```java
User user = new User();
user.setId(1L);
user.setName(null);  // name 为 null

UserDto dto = UserDtoCopier.toDto(user);
// dto.name 也为 null
```

### 包装类型 null 转换为基本类型

当从包装类型转换为基本类型时，null 值会转换为默认值：

```java
@CopyTarget(source = UserDto.class)
public class User {
    private int age;  // 基本类型
}

UserDto userDto = new UserDto();
userDto.setAge(null);  // age 为 null

User user = UserDtoCopier.fromDto(userDto);
// user.age 为 0（int 的默认值）
```

## 字段忽略

### 使用 ignore 属性

使用 `@CopyTarget` 注解的 `ignore` 属性可以指定要忽略的字段：

```java
@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // password 和 token 不会被拷贝
}
```

### 多个字段忽略

```java
@CopyTarget(
    source = User.class,
    ignore = {"password", "token", "secretKey", "internalId"}
)
public class UserDto {
    // ...
}
```

## 集合映射

### List 映射

```java
// 转换为 DTO 列表
List<UserDto> dtos = UserDtoCopier.toDtoList(users);

// 反向转换
List<User> users = UserDtoCopier.fromDtoList(dtos);
```

### Set 映射

```java
// 转换为 DTO 集合
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);

// 反向转换
Set<User> userSet = UserDtoCopier.fromDtoSet(dtoSet);
```

### 集合中的 null 处理

```java
// 集合为 null 时返回 null
List<UserDto> dtos = UserDtoCopier.toDtoList(null);  // 返回 null

// 集合中的 null 元素被保留
List<UserDto> dtos = UserDtoCopier.toDtoList(
    Arrays.asList(user1, null, user2)
);
// 结果中包含 3 个元素，第二个为 null
```

### Map 与数组深拷贝（v1.1）

```java
Map<String, User> map = Collections.singletonMap("u1", new User(1L, "Tom", "t@test.com", 20));
Map<String, UserDto> dtoMap = UserDtoCopier.toDtoMap(map); // 由生成的 Copier 提供

User[] arr = new User[]{new User(1L, "Jerry", "j@test.com", 18)};
UserDto[] dtoArr = UserDtoCopier.toDtoArray(arr);
```

- Map：Key 通常直接拷贝，Value 按深拷贝规则处理；null Map/Value 保留。
- 数组：按元素深拷贝，支持多维数组与 null 元素。
- Raw/无界通配符集合会降级为浅拷贝并给出编译期警告。

## 线程安全性

生成的 Copier 类是无状态的，可以安全地在多线程环境中使用。

```java
// 在多线程环境中安全使用
ExecutorService executor = Executors.newFixedThreadPool(10);
for (User user : users) {
    executor.submit(() -> {
        UserDto dto = UserDtoCopier.toDto(user);
        // 处理 dto
    });
}
```

## 性能特性

- **编译期代码生成** - 无运行时开销
- **无反射** - 直接调用 getter/setter
- **无动态代理** - 生成的代码是普通 Java 代码
- **内联友好** - JIT 编译器可以内联生成的代码

## 异常处理

Fast Bean Copier 生成的代码不会抛出检查异常。如果在 getter/setter 中抛出异常，异常会直接传播给调用者。

```java
try {
    UserDto dto = UserDtoCopier.toDto(user);
} catch (RuntimeException e) {
    // 处理异常
}
```

## 许可证

Fast Bean Copier 采用 Apache License 2.0 许可证。
