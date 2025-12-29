# Fast Bean Copier API 文档

> v1.2 新增：多字段映射（多对一、一对多）、TypeConverter 类型转换器、依赖注入支持、函数式定制拷贝。

## 注解

### @CopyTarget

标记目标 DTO 类，指定源类、要忽略的字段、自定义转换器和组件模型。

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
    
    /**
     * 自定义转换器类列表。可选。
     */
    Class<?>[] uses() default {};
    
    /**
     * 组件模型（依赖注入框架）。可选。
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `source` | `Class<?>` | 是 | 源类的类型 |
| `ignore` | `String[]` | 否 | 要忽略的字段名数组 |
| `uses` | `Class<?>[]` | 否 | 自定义转换器类列表（v1.2） |
| `componentModel` | `ComponentModel` | 否 | 依赖注入框架选择（v1.2） |

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

// 使用自定义转换器
@CopyTarget(source = User.class, uses = {StringToListConverter.class})
public class UserDto {
    // ...
}

// Spring 集成
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto {
    // ...
}
```

### @CopyField（v1.2 新增）

标记目标字段，指定字段级映射和转换规则。

#### 声明

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyField {
    /**
     * 源字段名（支持多个，用于多对一）
     */
    String[] source() default {};
    
    /**
     * 目标字段名（默认使用注解所在字段名）
     */
    String target() default "";
    
    /**
     * Java 表达式，用于复杂转换
     */
    String expression() default "";
    
    /**
     * 绑定具名转换方法（需配合 uses 使用）
     */
    String qualifiedByName() default "";
    
    /**
     * 指定 TypeConverter 实现类
     */
    Class<? extends TypeConverter<?, ?>> converter() default TypeConverter.None.class;
    
    /**
     * 转换器配置参数（格式字符串等）
     */
    String format() default "";
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `source` | `String[]` | 否 | 源字段名数组（支持多对一） |
| `target` | `String` | 否 | 目标字段名 |
| `expression` | `String` | 否 | Java 表达式 |
| `qualifiedByName` | `String` | 否 | 具名转换方法名 |
| `converter` | `Class<?>` | 否 | TypeConverter 实现类 |
| `format` | `String` | 否 | 格式字符串 |

#### 示例

```java
// 多对一映射
@CopyField(source = {"firstName", "lastName"}, 
           expression = "source.getFirstName() + \" \" + source.getLastName()")
private String fullName;

// 一对多映射
@CopyField(source = "fullName", 
           expression = "source.getFullName().split(\" \")[0]")
private String firstName;

// 使用 TypeConverter
@CopyField(converter = NumberFormatter.class, format = "#,##0.00")
private String priceText;

// 使用具名方法
@CopyField(qualifiedByName = "statusToName")
private String statusText;
```

### ComponentModel 枚举（v1.2 新增）

定义依赖注入框架的组件模型。

```java
public enum ComponentModel {
    /**
     * 无依赖注入，使用静态方法
     */
    DEFAULT,
    
    /**
     * Spring 框架，生成 @Component 注解
     */
    SPRING,
    
    /**
     * CDI 框架，生成 @ApplicationScoped 注解
     */
    CDI,
    
    /**
     * JSR-330 标准，生成 @Named 和 @Singleton 注解
     */
    JSR330
}
```

### TypeConverter 接口（v1.2 新增）

类型转换器接口，用于自定义类型转换。

```java
public interface TypeConverter<S, T> {
    /**
     * 将源类型转换为目标类型
     * 
     * @param source 源对象
     * @param format 格式字符串（可为空）
     * @return 转换后的对象
     */
    T convert(S source, String format);
    
    /**
     * 空实现占位符
     */
    final class None implements TypeConverter<Object, Object> {
        @Override
        public Object convert(Object source, String format) {
            throw new UnsupportedOperationException("No converter configured");
        }
    }
}
```

## 内置 TypeConverter

### NumberFormatter

将数字格式化为字符串。

```java
@CopyField(converter = NumberFormatter.class, format = "#,##0.00")
private String priceText;  // 1234.5 -> "1,234.50"
```

### NumberParser

将字符串解析为数字。

```java
@CopyField(converter = NumberParser.class, format = "#,##0.00")
private BigDecimal price;  // "1,234.50" -> 1234.50
```

### DateFormatter

将日期格式化为字符串。

```java
@CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
private String createTimeText;  // LocalDateTime -> "2025-12-29 10:30:00"
```

### DateParser

将字符串解析为日期。

```java
@CopyField(converter = DateParser.class, format = "yyyy-MM-dd")
private LocalDate createDate;  // "2025-12-29" -> LocalDate
```

### EnumStringConverter

枚举与字符串/整数互转。

```java
@CopyField(converter = EnumStringConverter.class)
private String statusText;  // Status.ACTIVE -> "ACTIVE"
```

### JsonConverter

对象与 JSON 字符串互转（依赖 Jackson）。

```java
@CopyField(converter = JsonConverter.class)
private String dataJson;  // Object -> JSON String
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

#### toDto(source, customizer)（v1.2 新增）

将源对象转换为目标 DTO 对象，并应用自定义逻辑。

**签名**：
```java
public static TargetType toDto(SourceType source, UnaryOperator<TargetType> customizer)
```

**参数**：
- `source` - 源对象
- `customizer` - 自定义函数

**返回值**：
- 经过自定义处理的目标 DTO 对象

**示例**：
```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
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

#### fromDto(source, customizer)（v1.2 新增）

将目标 DTO 对象转换回源对象，并应用自定义逻辑。

**签名**：
```java
public static SourceType fromDto(TargetType source, UnaryOperator<SourceType> customizer)
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

#### toDtoList(sources, customizer)（v1.2 新增）

将源对象列表转换为目标 DTO 对象列表，并对每个元素应用自定义逻辑。

**签名**：
```java
public static java.util.List<TargetType> toDtoList(
    java.util.List<SourceType> sources, 
    UnaryOperator<TargetType> customizer)
```

#### toDtoSet(sources)

将源对象集合转换为目标 DTO 对象集合。

**签名**：
```java
public static java.util.Set<TargetType> toDtoSet(java.util.Set<SourceType> sources)
```

#### toDtoSet(sources, customizer)（v1.2 新增）

将源对象集合转换为目标 DTO 对象集合，并对每个元素应用自定义逻辑。

#### fromDtoList(sources)

将目标 DTO 对象列表转换回源对象列表（反向拷贝）。

**签名**：
```java
public static java.util.List<SourceType> fromDtoList(java.util.List<TargetType> sources)
```

#### fromDtoList(sources, customizer)（v1.2 新增）

将目标 DTO 对象列表转换回源对象列表，并对每个元素应用自定义逻辑。

#### fromDtoSet(sources)

将目标 DTO 对象集合转换回源对象集合（反向拷贝）。

**签名**：
```java
public static java.util.Set<SourceType> fromDtoSet(java.util.Set<TargetType> sources)
```

#### fromDtoSet(sources, customizer)（v1.2 新增）

将目标 DTO 对象集合转换回源对象集合，并对每个元素应用自定义逻辑。

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

### 函数式定制的 null 处理（v1.2）

当源对象为 null 时，customizer 函数不会被调用：

```java
UserDto dto = UserDtoCopier.toDto(null, result -> {
    // 这里不会执行
    return result;
});
// dto 为 null
```

## 依赖注入模式（v1.2）

### DEFAULT 模式

生成静态方法，无依赖注入：

```java
public final class UserDtoCopier {
    private UserDtoCopier() {}
    
    public static UserDto toDto(User source) { ... }
}
```

### SPRING 模式

生成 Spring Bean：

```java
@Component
public final class UserDtoCopier {
    private final CustomConverter customConverter;
    
    public UserDtoCopier(CustomConverter customConverter) {
        this.customConverter = customConverter != null 
            ? customConverter : new CustomConverter();
    }
    
    public UserDtoCopier() {
        this(null);
    }
    
    public UserDto toDto(User source) { ... }
}
```

### CDI 模式

生成 CDI Bean：

```java
@ApplicationScoped
public final class UserDtoCopier {
    // 与 SPRING 模式类似
}
```

### JSR330 模式

生成 JSR-330 Bean：

```java
@Named
@Singleton
public final class UserDtoCopier {
    // 与 SPRING 模式类似
}
```

## 线程安全性

生成的 Copier 类是无状态的（DEFAULT 模式）或不可变的（DI 模式），可以安全地在多线程环境中使用。

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
- **TypeConverter 复用** - DEFAULT 模式使用静态实例，DI 模式使用单例

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
