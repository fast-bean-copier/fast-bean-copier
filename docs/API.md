# Fast Bean Copier API 文档

> **v1.6.0 新增**：Bean ↔ Bean 与 Bean ↔ Map 的 `postProcessor` 从单参数 `UnaryOperator<Result>` 升级为 `BiFunction<Source, Result, Result>`，并新增 `CycleDetectionStrategy` 与 `@CopyTarget(cycleDetection = ...)`。

## 注解

### @CopyTarget

标记目标 DTO 类，指定源类、要忽略的字段、自定义转换器和组件模型。

#### 声明

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyTarget {
    Class<?> source();
    String[] ignore() default {};
    Class<?>[] uses() default {};
    ComponentModel componentModel() default ComponentModel.DEFAULT;
    CycleDetectionStrategy cycleDetection() default CycleDetectionStrategy.FAIL_FAST;
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `source` | `Class<?>` | 是 | 源类的类型 |
| `ignore` | `String[]` | 否 | 要忽略的字段名数组 |
| `uses` | `Class<?>[]` | 否 | 自定义转换器类列表 |
| `componentModel` | `ComponentModel` | 否 | 依赖注入框架选择 |
| `cycleDetection` | `CycleDetectionStrategy` | 否 | 循环引用检测策略，默认 `FAIL_FAST` |

#### 示例

```java
@CopyTarget(source = User.class)
public class UserDto { ... }

@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserResponse { ... }

@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { ... }
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
    
    /**
     * 条件表达式，决定是否执行映射（v1.3）
     */
    String condition() default "";
    
    /**
     * 默认值，当源字段为 null 时使用（v1.3）
     */
    String defaultValue() default "";
    
    /**
     * 常量值，直接设置，不依赖源字段（v1.3）
     */
    String constant() default "";
    
    /**
     * 深拷贝控制，决定嵌套对象/集合是否深拷贝（v1.4）
     */
    boolean deepCopy() default true;
    
    /**
     * 自定义 Map key，仅 Bean ↔ Map 转换时生效（v1.5）
     */
    String mapKey() default "";
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
| `condition` | `String` | 否 | 条件表达式（v1.3） |
| `defaultValue` | `String` | 否 | 默认值（v1.3） |
| `constant` | `String` | 否 | 常量值（v1.3） |
| `deepCopy` | `boolean` | 否 | 深拷贝控制，默认 true（v1.4） |
| `mapKey` | `String` | 否 | 自定义 Map key，仅 Bean ↔ Map 转换时生效（v1.5） |

#### 示例

```java
// 多对一映射
@CopyField(source = {"firstName", "lastName"}, 
           expression = "java(source.getFirstName() + \" \" + source.getLastName())")
private String fullName;

// 一对多映射
@CopyField(source = "fullName", 
           expression = "java(source.getFullName().split(\" \")[0])")
private String firstName;

// 使用 TypeConverter
@CopyField(converter = NumberFormatter.class, format = "#,##0.00")
private String priceText;

// 使用具名方法
@CopyField(qualifiedByName = "statusToName")
private String statusText;

// 条件映射（v1.3）
@CopyField(condition = "java(source.getName() != null)")
private String name;

// 默认值（v1.3）
@CopyField(defaultValue = "未知")
private String name;

// 常量值（v1.3）
@CopyField(constant = "SYSTEM")
private String createdBy;

// 深拷贝控制（v1.4）
@CopyField(deepCopy = false)  // 浅拷贝，直接引用
private Address address;

@CopyField(deepCopy = true)   // 深拷贝（默认行为）
private List<String> tags;
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

### CycleDetectionStrategy 枚举（v1.6 新增）

定义嵌套对象深拷贝遇到循环引用时的处理策略。

```java
public enum CycleDetectionStrategy {
    FAIL_FAST,        // 默认：编译期检测到循环引用时报错
    RETURN_NULL,      // 运行期检测到循环引用时，将循环字段置为 null
    AUTOMATIC_CACHE   // 运行期维护引用缓存，复用已拷贝的目标对象
}
```

示例：

```java
@CopyTarget(
    source = Node.class,
    cycleDetection = CycleDetectionStrategy.FAIL_FAST
)
public class NodeDto {
    private NodeDto parent;
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

### @CopyTargetConfig（v1.3 新增）

包级别配置注解，为包内所有 @CopyTarget 提供默认配置。

#### 声明

```java
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyTargetConfig {
    /**
     * 默认组件模型
     */
    ComponentModel componentModel() default ComponentModel.DEFAULT;
    
    /**
     * 默认 null 值处理策略
     */
    NullValueStrategy nullValueStrategy() default NullValueStrategy.IGNORE;

    /**
     * 默认循环检测策略
     */
    CycleDetectionStrategy cycleDetection() default CycleDetectionStrategy.FAIL_FAST;
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `componentModel` | `ComponentModel` | 否 | 默认组件模型 |
| `nullValueStrategy` | `NullValueStrategy` | 否 | 默认 null 值处理策略 |
| `cycleDetection` | `CycleDetectionStrategy` | 否 | 默认循环检测策略 |

#### 示例

```java
// package-info.java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE,
    cycleDetection = CycleDetectionStrategy.RETURN_NULL
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

### NullValueStrategy 枚举（v1.3 新增）

定义 null 值处理策略。

```java
public enum NullValueStrategy {
    /**
     * 忽略 null 值，不更新目标字段（默认）
     */
    IGNORE,
    
    /**
     * 替换 null 值，将目标字段设置为 null
     */
    REPLACE
}
```

### @CopyToMap（v1.5 新增）

标记类支持 Bean → Map 转换，编译期生成 `{Class}MapCopier` 类。

#### 声明

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyToMap {
    String[] ignore() default {};
    Class<?>[] uses() default {};
    ComponentModel componentModel() default ComponentModel.DEFAULT;
    MapKeyStrategy keyStrategy() default MapKeyStrategy.FIELD_NAME;
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `ignore` | `String[]` | 否 | 不参与转换的字段名 |
| `uses` | `Class<?>[]` | 否 | 自定义转换器类列表 |
| `componentModel` | `ComponentModel` | 否 | 依赖注入框架（与 Bean Copier 相同） |
| `keyStrategy` | `MapKeyStrategy` | 否 | Map key 命名策略，默认 `FIELD_NAME` |

#### 示例

```java
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;
    private String firstName;          // key: "first_name"
    @CopyField(mapKey = "email_addr")
    private String email;              // key: "email_addr"（字段级优先）
}
```

可与 `@CopyFromMap`、`@CopyTarget` 同时标注在同一类上，分别生成 MapCopier 与 BeanCopier，互不影响。

### @CopyFromMap（v1.5 新增）

标记类支持 Map → Bean 转换，与 `@CopyToMap` 共用 `{Class}MapCopier` 类名。

#### 声明

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CopyFromMap {
    String[] ignore() default {};
    Class<?>[] uses() default {};
    ComponentModel componentModel() default ComponentModel.DEFAULT;
    MapKeyStrategy keyStrategy() default MapKeyStrategy.FIELD_NAME;
}
```

#### 属性

| 属性 | 类型 | 必需 | 说明 |
|------|------|------|------|
| `ignore` | `String[]` | 否 | 不参与转换的字段名 |
| `uses` | `Class<?>[]` | 否 | 自定义转换器类列表 |
| `componentModel` | `ComponentModel` | 否 | 依赖注入框架 |
| `keyStrategy` | `MapKeyStrategy` | 否 | 解析 Map key 时的命名策略 |

#### 示例

```java
@CopyFromMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;
    private String firstName;
}
```

### MapKeyStrategy 枚举（v1.5 新增）

Bean ↔ Map 转换时，字段名到 Map key 的命名策略。

**优先级**：`@CopyField(mapKey)` > `keyStrategy` > 字段名

| 枚举值 | 说明 | 示例（字段 `firstName`） |
|--------|------|--------------------------|
| `FIELD_NAME` | 使用字段名（默认） | `"firstName"` |
| `CAMEL_CASE` | 驼峰（与字段名相同） | `"firstName"` |
| `SNAKE_CASE` | 下划线命名 | `"first_name"` |
| `CUSTOM` | 配合 `@CopyField(mapKey)` 逐字段指定 | 由 `mapKey` 决定 |

```java
@CopyToMap(keyStrategy = MapKeyStrategy.CUSTOM)
public class UserDto {
    @CopyField(mapKey = "user_id")
    private Long id;
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

### Bean Copier 方法

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

#### toDto(source, customizer)（v1.2 新增，v1.5 已移除）

单参数 customizer 重载已在 v1.5.0 中完全移除。请使用双处理器 API：

```java
// 新方法
UserDto dto = UserDtoCopier.toDto(user, null, (source, result) -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

#### toDto(source, preProcessor, postProcessor)（v1.4 新增）

将源对象转换为目标 DTO 对象，支持拷贝前和拷贝后的双重处理。

**签名**：
```java
public static TargetType toDto(SourceType source, 
                                UnaryOperator<SourceType> preProcessor, 
                                BiFunction<SourceType, TargetType, TargetType> postProcessor)
```

**参数**：
- `source` - 源对象
- `preProcessor` - 拷贝前处理器，对源对象进行预处理
- `postProcessor` - 拷贝后处理器，对结果对象进行后处理

**返回值**：
- 经过双重处理的目标 DTO 对象

**执行顺序**：
1. 执行 `preProcessor`（如果不为 null）
2. 执行字段拷贝
3. 执行 `postProcessor`（如果不为 null）

**示例**：
```java
// 拷贝前规范化源数据，拷贝后添加额外信息
UserDto dto = UserDtoCopier.toDto(user, 
    source -> {
        // 预处理：规范化邮箱
        source.setEmail(source.getEmail().toLowerCase());
        return source;
    },
    (source, result) -> {
        // 后处理：添加显示名称
        result.setDisplayName(result.getName().toUpperCase());
        return result;
    }
);
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

#### fromDto(source, customizer)（v1.2 新增，v1.5 已移除）

单参数 customizer 重载已在 v1.5.0 中完全移除。请使用 `fromDto(source, null, postProcessor)` 替代。

#### fromDto(source, preProcessor, postProcessor)（v1.4 新增）

将目标 DTO 对象转换回源对象，支持拷贝前和拷贝后的双重处理。

**签名**：
```java
public static SourceType fromDto(TargetType source, 
                                  UnaryOperator<TargetType> preProcessor, 
                                  BiFunction<TargetType, SourceType, SourceType> postProcessor)
```

**参数**：
- `source` - 目标 DTO 对象
- `preProcessor` - 拷贝前处理器，对 DTO 对象进行预处理
- `postProcessor` - 拷贝后处理器，对结果对象进行后处理

**返回值**：
- 经过双重处理的源对象

**示例**：
```java
User user = UserDtoCopier.fromDto(userDto, 
    dto -> {
        // 预处理：验证 DTO
        if (dto.getEmail() == null) {
            throw new IllegalArgumentException("Email is required");
        }
        return dto;
    },
    (dto, result) -> {
        // 后处理：设置默认值
        if (result.getStatus() == null) {
            result.setStatus("ACTIVE");
        }
        return result;
    }
);
```

#### updateDto(target, source)

更新已存在的目标 DTO 对象，而不是创建新对象。

**签名**：
```java
public static void updateDto(TargetType target, SourceType source)
```

**参数**：
- `target` - 要更新的目标 DTO 对象
- `source` - 源对象

**说明**：
- 源对象为 null 时直接返回，不修改目标对象
- 支持嵌套对象的递归更新
- 支持 List、Set、Map、数组字段的更新

**示例**：
```java
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");
UserDtoCopier.updateDto(existingDto, user);
// existingDto 的字段被更新为 user 的值
```

#### updateEntity(target, source)（v1.3 新增）

更新已存在的实体对象（反向更新）。

**签名**：
```java
public static void updateEntity(SourceType target, TargetType source)
```

**参数**：
- `target` - 要更新的实体对象
- `source` - 源 DTO 对象

**示例**：
```java
User existingUser = new User();
UserDtoCopier.updateEntity(existingUser, userDto);
// existingUser 的字段被更新为 userDto 的值
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

#### toDtoList(sources, preProcessor, postProcessor)（v1.6.0 更新）

将源对象列表转换为目标 DTO 对象列表，并应用预处理和后处理逻辑。

**v1.6.0 变更**：`postProcessor` 使用 `BiFunction<List<SourceType>, List<TargetType>, List<TargetType>>`，可同时访问源列表和转换结果。

**签名**：
```java
public static java.util.List<TargetType> toDtoList(
    java.util.List<SourceType> sources, 
    UnaryOperator<SourceType> preProcessor,
    BiFunction<java.util.List<SourceType>, java.util.List<TargetType>, java.util.List<TargetType>> postProcessor)
```

**示例**：
```java
// 过滤列表
List<UserDto> filtered = UserDtoCopier.toDtoList(users, null, (sources, result) -> 
    result.stream()
        .filter(dto -> dto.getPrice() >= 100)
        .collect(Collectors.toList())
);

// 排序列表
List<UserDto> sorted = UserDtoCopier.toDtoList(users, null, (sources, result) -> {
    result.sort(Comparator.comparing(UserDto::getName));
    return result;
});
```

#### toDtoSet(sources)

将源对象集合转换为目标 DTO 对象集合。

**签名**：
```java
public static java.util.Set<TargetType> toDtoSet(java.util.Set<SourceType> sources)
```

#### toDtoSet(sources, preProcessor, postProcessor)（v1.6.0 更新）

将源对象集合转换为目标 DTO 对象集合，并应用预处理和后处理逻辑。

**v1.6.0 变更**：`postProcessor` 使用 `BiFunction<Set<SourceType>, Set<TargetType>, Set<TargetType>>`。

**签名**：
```java
public static java.util.Set<TargetType> toDtoSet(
    java.util.Set<SourceType> sources, 
    UnaryOperator<SourceType> preProcessor,
    BiFunction<java.util.Set<SourceType>, java.util.Set<TargetType>, java.util.Set<TargetType>> postProcessor)
```

#### fromDtoList(sources)

将目标 DTO 对象列表转换回源对象列表（反向拷贝）。

**签名**：
```java
public static java.util.List<SourceType> fromDtoList(java.util.List<TargetType> sources)
```

#### fromDtoList(sources, preProcessor, postProcessor)（v1.6.0 更新）

将目标 DTO 对象列表转换回源对象列表，并应用预处理和后处理逻辑。

**签名**：
```java
public static java.util.List<SourceType> fromDtoList(
    java.util.List<TargetType> sources, 
    UnaryOperator<TargetType> preProcessor,
    BiFunction<java.util.List<TargetType>, java.util.List<SourceType>, java.util.List<SourceType>> postProcessor)
```

#### fromDtoSet(sources)

将目标 DTO 对象集合转换回源对象集合（反向拷贝）。

**签名**：
```java
public static java.util.Set<SourceType> fromDtoSet(java.util.Set<TargetType> sources)
```

#### fromDtoSet(sources, preProcessor, postProcessor)（v1.6.0 更新）

将目标 DTO 对象集合转换回源对象集合，并应用预处理和后处理逻辑。

**签名**：
```java
public static java.util.Set<SourceType> fromDtoSet(
    java.util.Set<TargetType> sources, 
    UnaryOperator<TargetType> preProcessor,
    BiFunction<java.util.Set<TargetType>, java.util.Set<SourceType>, java.util.Set<SourceType>> postProcessor)
```

#### toDtoMap(sources)

将源对象 Map 转换为目标 DTO Map（保留 Key，拷贝 Value）。

**签名**：
```java
public static <K> java.util.Map<K, TargetType> toDtoMap(java.util.Map<K, SourceType> sources)
```

#### toDtoMap(sources, preProcessor, postProcessor)（v1.6.0 更新）

将源对象 Map 转换为目标 DTO Map，并应用预处理和后处理逻辑。

**签名**：
```java
public static <K> java.util.Map<K, TargetType> toDtoMap(
    java.util.Map<K, SourceType> sources, 
    UnaryOperator<SourceType> preProcessor,
    BiFunction<java.util.Map<K, SourceType>, java.util.Map<K, TargetType>, java.util.Map<K, TargetType>> postProcessor)
```

**参数**：
- `sources` - 源对象 Map
- `preProcessor` - 单个源对象预处理器
- `postProcessor` - Map 转换结果后处理器，可同时访问原始 sources 和 result

**返回值**：
- 经过处理器处理的目标 DTO Map

**示例**：
```java
// 过滤 Map 条目
Map<String, UserDto> filteredMap = UserDtoCopier.toDtoMap(userMap, null, (sources, result) -> {
    result.entrySet().removeIf(entry -> entry.getValue().getId() == null);
    return result;
});

// 转换为不可变 Map
Map<String, UserDto> immutableMap = UserDtoCopier.toDtoMap(userMap, null,
    (sources, result) -> Collections.unmodifiableMap(result));
```

#### fromDtoMap(sources)

将目标 DTO Map 反向转换为源对象 Map。

**签名**：
```java
public static <K> java.util.Map<K, SourceType> fromDtoMap(java.util.Map<K, TargetType> sources)
```

#### fromDtoMap(sources, preProcessor, postProcessor)（v1.6.0 更新）

将目标 DTO Map 反向转换为源对象 Map，并应用自定义逻辑。

**签名**：
```java
public static <K> java.util.Map<K, SourceType> fromDtoMap(
    java.util.Map<K, TargetType> sources, 
    UnaryOperator<TargetType> preProcessor,
    BiFunction<java.util.Map<K, TargetType>, java.util.Map<K, SourceType>, java.util.Map<K, SourceType>> postProcessor)
```

#### toDtoArray(sources)

将源对象数组转换为目标 DTO 数组。

**签名**：
```java
public static TargetType[] toDtoArray(SourceType[] sources)
```

#### toDtoArray(sources, preProcessor, postProcessor)（v1.6.0 更新）

将源对象数组转换为目标 DTO 数组，并应用预处理和后处理逻辑。

**签名**：
```java
public static TargetType[] toDtoArray(
    SourceType[] sources,
    UnaryOperator<SourceType> preProcessor,
    BiFunction<SourceType[], TargetType[], TargetType[]> postProcessor)
```

**参数**：
- `sources` - 源对象数组
- `preProcessor` - 单个源对象预处理器
- `postProcessor` - 数组转换结果后处理器，可同时访问原始 sources 和 result

**返回值**：
- 经过处理器处理的目标 DTO 数组

**示例**：
```java
// 过滤数组元素
UserDto[] filteredArray = UserDtoCopier.toDtoArray(users, null, (sources, result) ->
    Arrays.stream(result)
        .filter(dto -> dto.getId() != null)
        .toArray(UserDto[]::new));

// 排序数组
UserDto[] sortedArray = UserDtoCopier.toDtoArray(users, null, (sources, result) -> {
    Arrays.sort(result, Comparator.comparing(UserDto::getName));
    return result;
});
```

#### fromDtoArray(sources)

将目标 DTO 数组转换回源对象数组。

**签名**：
```java
public static SourceType[] fromDtoArray(TargetType[] sources)
```

#### fromDtoArray(sources, preProcessor, postProcessor)（v1.6.0 更新）

将目标 DTO 数组转换回源对象数组，并应用预处理和后处理逻辑。

**签名**：
```java
public static SourceType[] fromDtoArray(
    TargetType[] sources,
    UnaryOperator<TargetType> preProcessor,
    BiFunction<TargetType[], SourceType[], SourceType[]> postProcessor)
```

## 嵌套对象深拷贝（v1.3.2 新增）

Fast Bean Copier 自动支持不同类型嵌套对象的深拷贝。当源对象和目标对象包含不同类型的嵌套对象时，框架会智能选择最优的拷贝策略。

### 自动深拷贝

无需任何配置，嵌套对象会自动深拷贝：

```java
// 源实体
public class Employee {
    private Long id;
    private String name;
    private Address address;  // 嵌套对象
}

public class Address {
    private String city;
    private String street;
}

// 目标 DTO
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    private Long id;
    private String name;
    private AddressDto address;  // 不同类型的嵌套对象
}

@CopyTarget(source = Address.class)
public class AddressDto {
    private String city;
    private String street;
}

// 使用
Employee employee = new Employee();
employee.setId(1L);
employee.setName("John");

Address address = new Address();
address.setCity("Beijing");
address.setStreet("Main Street");
employee.setAddress(address);

// 自动深拷贝嵌套对象
EmployeeDto dto = EmployeeDtoCopier.toDto(employee);
// dto.getAddress() 是一个新的 AddressDto 对象
```

### 两种实现方式

#### 方式 1：使用 Copier（推荐）

当嵌套对象的 DTO 类有 `@CopyTarget` 注解时，使用对应的 Copier 进行拷贝：

```java
@CopyTarget(source = Address.class)
public class AddressDto {
    private String city;
    private String street;
}

// 生成的代码使用 AddressDtoCopier
if (source.getAddress() != null) {
    target.setAddress(AddressDtoCopier.toDto(source.getAddress()));
} else {
    target.setAddress(null);
}
```

**优点：**
- 最优性能（直接方法调用）
- 类型安全
- 可复用 Copier 类
- 支持所有 Copier 特性（回调、条件映射等）

#### 方式 2：字段拷贝（自动回退）

当嵌套对象的 DTO 类没有 `@CopyTarget` 注解时，自动使用字段拷贝：

```java
// 没有 @CopyTarget 注解
public class SimpleAddressDto {
    private String city;
    private String street;
}

// 生成的代码使用字段拷贝
if (source.getAddress() != null) {
    SimpleAddressDto nestedAddress = new SimpleAddressDto();
    nestedAddress.setCity(source.getAddress().getCity());
    nestedAddress.setStreet(source.getAddress().getStreet());
    target.setAddress(nestedAddress);
} else {
    target.setAddress(null);
}
```

**优点：**
- 无需添加注解
- 适合简单的一次性嵌套对象
- 仍然是编译期生成（无反射）
- 自动处理递归嵌套

### 多层嵌套支持

支持任意深度的嵌套对象，并且可以混合使用两种方式：

```java
// Level 1: 有 @CopyTarget
@CopyTarget(source = Company.class)
public class CompanyDto {
    private Long id;
    private DepartmentDto department;  // Level 2
}

// Level 2: 无 @CopyTarget（使用字段拷贝）
public class DepartmentDto {
    private String name;
    private ManagerDto manager;  // Level 3
}

// Level 3: 有 @CopyTarget（在字段拷贝中仍使用 Copier）
@CopyTarget(source = Manager.class)
public class ManagerDto {
    private String name;
    private AddressDto address;  // Level 4
}

// Level 4: 有 @CopyTarget
@CopyTarget(source = Address.class)
public class AddressDto {
    private String city;
    private String street;
}

// 使用
Company company = createCompany();  // 创建完整的对象图
CompanyDto dto = CompanyDtoCopier.toDto(company);
// 所有层级都正确深拷贝
```

### Null 值处理

嵌套对象为 null 时，目标对象也设置为 null：

```java
Employee employee = new Employee();
employee.setId(1L);
employee.setAddress(null);  // 嵌套对象为 null

EmployeeDto dto = EmployeeDtoCopier.toDto(employee);
// dto.getAddress() == null
```

### 性能建议

| 场景 | 建议 |
|------|------|
| 频繁使用的嵌套对象 | 添加 `@CopyTarget` 注解，使用 Copier |
| 简单的一次性嵌套对象 | 不添加注解，使用字段拷贝 |
| 复杂的嵌套对象（多字段） | 添加 `@CopyTarget` 注解，使用 Copier |
| 嵌套深度 | 建议不超过 5 层 |

### 注意事项

1. **循环引用需配置策略**：默认 `FAIL_FAST` 会在检测到循环引用时快速失败；如需按 null 截断或保持引用一致，可显式使用 `RETURN_NULL` 或 `AUTOMATIC_CACHE`
2. **字段名必须匹配**：字段拷贝模式下，只拷贝同名字段
3. **类型必须兼容**：字段类型必须兼容（基本类型、包装类型、自定义对象）
4. **同类型引用传递**：如果嵌套对象类型相同（如 `Address` → `Address`），使用引用传递而非深拷贝

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

### 函数式处理的 null 处理（v1.4+）

当源对象为 null 时，`preProcessor` 和 `postProcessor` 均不会被调用：

```java
UserDto dto = UserDtoCopier.toDto(null, 
    source -> { return source; },
    (source, result) -> { return result; }
);
// dto 为 null，处理器不执行
```

## 生成的 MapCopier 类（v1.5 新增）

标注 `@CopyToMap` 和/或 `@CopyFromMap` 后，生成 `{Class}MapCopier`（如 `UserDtoMapCopier`）。

### Bean → Map 方法

| 方法 | 说明 |
|------|------|
| `toMap(T source)` | 单对象转 `Map<String, Object>` |
| `toMap(T source, preProcessor, postProcessor)` | 拷贝前处理 Bean，拷贝后用 `BiFunction<T, Map<String,Object>, Map<String,Object>>` 处理 Map |
| `toMapList(List<T> sources)` | 批量 List 转换 |
| `toMapList(sources, preProcessor, postProcessor)` | postProcessor 可同时访问原始 List 和转换后的 Map List |
| `toMapSet(Set<T> sources)` | 批量 Set 转换 |
| `toMapSet(sources, preProcessor, postProcessor)` | postProcessor 可同时访问原始 Set 和转换后的 Map Set |

**执行顺序**：`preProcessor` → 字段写入 Map → `postProcessor`

```java
Map<String, Object> map = UserDtoMapCopier.toMap(userDto);
List<Map<String, Object>> maps = UserDtoMapCopier.toMapList(userDtos);
```

### Map → Bean 方法

| 方法 | 说明 |
|------|------|
| `fromMap(Map<String, Object> source)` | 单 Map 转 Bean |
| `fromMap(source, preProcessor, postProcessor)` | 拷贝前处理 Map，拷贝后用 `BiFunction<Map<String,Object>, T, T>` 处理 Bean |
| `fromMapList(List<Map<String, Object>> sources)` | 批量 List 转换 |
| `fromMapList(sources, preProcessor, postProcessor)` | postProcessor 可同时访问原始 Map List 和转换后的 Bean List |
| `fromMapSet(Set<Map<String, Object>> sources)` | 批量 Set 转换 |
| `fromMapSet(sources, preProcessor, postProcessor)` | postProcessor 可同时访问原始 Map Set 和转换后的 Bean Set |

```java
UserDto dto = UserDtoMapCopier.fromMap(map);
List<UserDto> dtos = UserDtoMapCopier.fromMapList(mapList);
```

### 与 Bean Copier 共存

```java
@CopyTarget(source = User.class)
@CopyToMap
@CopyFromMap
public class UserDto { ... }

UserDto dto = UserDtoCopier.toDto(user);
Map<String, Object> map = UserDtoMapCopier.toMap(dto);
UserDto restored = UserDtoMapCopier.fromMap(map);
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
