# Fast Bean Copier 常见问题解答

## 许可证

本项目采用 Apache License 2.0 许可证。

## 版本特性

### Q: v1.5.0 有哪些新增功能？
**A**: v1.5.0 主要新增 Bean ↔ Map 转换支持，并清理了废弃 API：
- **@CopyToMap**：Bean → Map 转换，生成 `{Class}MapCopier` 的 toMap/toMapList/toMapSet 方法
- **@CopyFromMap**：Map → Bean 转换，生成 fromMap/fromMapList/fromMapSet 方法
- **MapKeyStrategy**：FIELD_NAME / CAMEL_CASE / SNAKE_CASE / CUSTOM 四种 key 命名策略
- **@CopyField.mapKey**：字段级自定义 Map key，优先级高于 keyStrategy
- **移除 beforeMapping**：使用 `preProcessor` 替代
- **移除单参数 customizer**：使用 `toDto(source, null, postProcessor)` 替代

### Q: v1.5.0 如何使用 Bean ↔ Map 转换？
**A**:
```java
// Bean → Map
@CopyToMap(keyStrategy = MapKeyStrategy.SNAKE_CASE)
public class UserDto {
    private Long id;
    private String firstName;          // key: "first_name"
    @CopyField(mapKey = "email_addr")
    private String email;              // key: "email_addr"（字段级优先）
}
Map<String, Object> map = UserDtoMapCopier.toMap(userDto);

// Map → Bean
@CopyFromMap
public class UserDto { ... }
UserDto dto = UserDtoMapCopier.fromMap(map);
```

### Q: v1.5.0 移除了哪些 API？如何迁移？
**A**: 移除了 v1.4.0 标记为废弃的两类 API：

**1. beforeMapping 属性**（使用 preProcessor 替代）：
```java
UserDto dto = UserDtoCopier.toDto(user,
    source -> { /* 验证逻辑 */ return source; },
    null
);
```

**2. 单参数 customizer 重载**（使用 postProcessor 替代）：
```java
UserDto dto = UserDtoCopier.toDto(user, null, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

## 基本问题

### Q: Fast Bean Copier 是什么？
**A**: Fast Bean Copier 是一个基于注解处理器的 Java Bean 拷贝工具，在编译期自动生成类型安全、高性能的 Bean 映射代码。

### Q: 与 MapStruct 有什么区别？
**A**: 
- Fast Bean Copier 更简洁，只需一个 `@CopyTarget` 注解
- Fast Bean Copier 自动生成 List/Set/Map/数组 映射方法
- Fast Bean Copier 自动处理基本类型与包装类型转换
- MapStruct 功能更强大，支持更多高级特性

### Q: 支持哪些 Java 版本？
**A**: Java 8 及以上版本。

### Q: 可以在 Gradle 项目中使用吗？
**A**: 可以。在 `build.gradle` 中添加：
```gradle
dependencies {
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.5.0'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.5.0'
}
```

## v1.4.0 新功能问题

### Q: 如何使用 preProcessor 和 postProcessor？
**A**: v1.4.0 提供了统一的双处理器 API：

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

// 集合转换
List<UserDto> dtos = UserDtoCopier.toDtoList(
    users,
    source -> { /* 预处理每个 source */ return source; },
    target -> { /* 后处理每个 target */ return target; }
);
```

### Q: preProcessor 和 postProcessor 的执行顺序是什么？
**A**: 执行顺序为：`preProcessor` → 字段拷贝 → `postProcessor`（Bean ↔ Bean 与 Bean ↔ Map 均适用）

### Q: 如何使用 deepCopy 控制深拷贝行为？
**A**: 通过 `@CopyField(deepCopy = true/false)` 控制：

```java
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    // 深拷贝（默认）：创建新的 AddressDto 对象
    @CopyField(deepCopy = true)
    private AddressDto address;
    
    // 浅拷贝：直接引用传递
    @CopyField(deepCopy = false)
    private DepartmentDto department;
    
    // 集合深拷贝（默认）：拷贝集合并深拷贝每个元素
    @CopyField(deepCopy = true)
    private List<ProjectDto> projects;
    
    // 集合浅拷贝：拷贝集合但元素直接引用
    @CopyField(deepCopy = false)
    private List<TagDto> tags;
}
```

### Q: deepCopy 的默认值是什么？
**A**: 默认值为 `true`，保持与之前版本一致的深拷贝行为。如果需要浅拷贝，显式设置 `deepCopy = false`。

### Q: 什么时候应该使用 deepCopy = false？
**A**: 以下场景适合使用浅拷贝：
- **性能优化**：对不需要深拷贝的字段使用浅拷贝，减少对象创建开销
- **共享引用**：多个对象需要共享同一个嵌套对象实例
- **不可变对象**：对不可变对象（如枚举、常量）使用浅拷贝
- **大对象**：对大型嵌套对象使用浅拷贝，避免内存占用

### Q: 旧的 customizer 方法还能用吗？
**A**: **不能**。v1.5.0 已完全移除单参数 `customizer` 重载，请使用双处理器 API：

```java
// 旧方法（v1.5.0 已移除）
UserDto dto = UserDtoCopier.toDto(user, result -> { ... return result; });

// 新方法
UserDto dto = UserDtoCopier.toDto(user, null, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

### Q: beforeMapping 还能用吗？
**A**: **不能**。v1.5.0 已完全移除 `@CopyTarget.beforeMapping()` 属性，请使用 `preProcessor` 替代：

```java
// 旧方法（v1.5.0 已移除）
@CopyTarget(source = User.class, beforeMapping = "validate")
public class UserDto { ... }

// 新方法
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

### Q: v1.3.2 有哪些新增功能？
**A**: v1.3.2 主要新增嵌套对象深拷贝支持：
- **自动深拷贝不同类型的嵌套对象**：支持 `Address` → `AddressDto` 等不同类型嵌套对象的自动转换
- **智能 Copier 检测**：自动检测嵌套对象是否有 Copier，有则使用 Copier（最优性能），无则使用字段拷贝（自动回退）
- **无限层级嵌套**：支持任意深度的对象图（A→B→C→D...）
- **混合模式支持**：在同一对象图中混合使用有 Copier 和无 Copier 的嵌套对象
- **正向和反向转换**：toDto() 和 fromDto() 都支持嵌套对象深拷贝

### Q: v1.3.1 有哪些新增功能？
**A**: v1.3.1 主要新增和改进：
- **统一 UnaryOperator 行为**：所有集合类型（List/Set/Map/Array）的 customizer 现在都操作整个集合，提供一致且强大的定制能力
- **List/Set UnaryOperator 增强**：从操作单个元素改为操作整个集合，支持过滤、排序、限制、不可变转换等高级操作
- **Map/Array UnaryOperator 重载**：新增 Map 和 Array 批量转换的 UnaryOperator 重载方法
- **Properties 配置文件支持**：通过 `fast-bean-copier.properties` 文件进行全局配置
- **逆向转换智能跳过**：自动跳过使用了特殊映射配置的字段（typeConverter、expression、qualifiedByName、constant）

### Q: v1.3 有哪些新增功能？
**A**: v1.3 主要新增：
- **更新现有对象**：updateDto/updateEntity 方法，支持更新已存在的对象而不是创建新对象
- **映射前回调**：beforeMapping 属性，在映射前执行验证、初始化等自定义逻辑
- **条件映射**：condition 属性，基于条件决定是否映射字段
- **默认值和常量**：defaultValue/constant 属性，设置字段的默认值和常量值
- **全局配置**：@CopyTargetConfig 注解，包级别配置减少重复配置
- **Null 值处理策略**：NullValueStrategy 枚举，IGNORE 或 REPLACE 策略

### Q: v1.2.1 有哪些变化？
**A**: v1.2.1 主要是处理器架构重构：
- **BeanCopierProcessor 重构**：从 ~500 行精简为 ~148 行，作为协调者角色
- **CodeGenerator 重构**：从 ~1900 行精简为 ~192 行，作为代码生成协调者
- **新增组件**：ProcessorContext、AnnotationExtractor、FieldMappingAnalyzer
- **新增生成器**：ClassStructureGenerator、BasicMethodGenerator、CollectionMethodGenerator、FieldCopyGenerator、DeepCopyGenerator
- **完全向后兼容**：生成的 Copier 类代码与 v1.2.0 完全一致

### Q: v1.2 有哪些新增功能？
**A**: v1.2 主要新增：
- **多字段映射**：多对一（字段合并）、一对多（字段拆分）
- **TypeConverter**：内置 6 个转换器（NumberFormatter、NumberParser、DateFormatter、DateParser、EnumStringConverter、JsonConverter）
- **表达式映射**：支持 Java 表达式进行复杂转换
- **依赖注入**：支持 Spring、CDI、JSR-330 框架集成
- **函数式定制**：支持 `UnaryOperator` 后处理

## 功能问题

### Q: 支持嵌套对象拷贝吗？（v1.3.2 增强）
**A**: 完全支持。v1.3.2 新增了不同类型嵌套对象的自动深拷贝：
- **同类型嵌套对象**：直接拷贝（引用传递）
- **不同类型嵌套对象**：自动深拷贝（如 `Address` → `AddressDto`）
- **有 Copier 的嵌套对象**：使用 Copier 进行拷贝（最优性能）
- **无 Copier 的嵌套对象**：使用字段拷贝（自动回退）
- **多层嵌套**：支持无限层级，每层独立选择最优策略

示例：
```java
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    private Long id;
    private AddressDto address;  // 自动深拷贝
}

@CopyTarget(source = Address.class)
public class AddressDto {
    private String city;
    private String street;
}
```

### Q: 嵌套对象必须添加 @CopyTarget 注解吗？
**A**: 不是必须的。v1.3.2 支持两种方式：
- **有 @CopyTarget**：生成 Copier 类，性能最优，推荐用于频繁使用的嵌套对象
- **无 @CopyTarget**：使用字段拷贝，适合简单的一次性嵌套对象

两种方式可以混合使用，框架会自动选择最优策略。

### Q: 支持多层嵌套吗？
**A**: 支持。v1.3.2 支持任意深度的嵌套（A→B→C→D...），并且可以混合使用有 Copier 和无 Copier 的嵌套对象。例如：
- Level1 有 @CopyTarget → 使用 Copier
- Level2 无 @CopyTarget → 使用字段拷贝
- Level3 有 @CopyTarget → 在字段拷贝中仍使用 Copier

### Q: 嵌套对象的字段不完全匹配怎么办？
**A**: 字段拷贝模式下，只拷贝同名且类型兼容的字段，其他字段保持默认值。如果需要自定义映射，建议为嵌套对象添加 @CopyTarget 注解并使用 @CopyField 配置。

### Q: 循环引用怎么处理？
**A**: 当前版本不支持循环引用（如 A 包含 B，B 又包含 A）。建议避免循环引用的设计，或使用 `@CopyTarget(ignore = {"fieldName"})` 忽略其中一个字段。

### Q: 有 Copier 和无 Copier 的性能差异？
**A**: 
- **有 Copier**：最优性能，直接方法调用，编译期生成
- **无 Copier（字段拷贝）**：性能略低但仍然很好，也是编译期生成，无反射开销
- **建议**：频繁使用的嵌套对象添加 @CopyTarget，简单的一次性嵌套对象可以不添加

### Q: 集合/数组字段会自动深拷贝吗？
**A**: 会。List/Set/Map/数组（含嵌套组合与多维数组）都会按元素深拷贝。

### Q: 原始类型或无界通配符的集合如何处理？
**A**: 会回退为浅拷贝并输出编译期警告，建议为集合添加明确泛型。

### Q: 支持自定义转换器吗？
**A**: v1.2 开始支持。通过 `@CopyTarget(uses = {CustomConverter.class})` 引入自定义转换器，使用 `@CopyField(qualifiedByName = "methodName")` 指定转换方法。

### Q: 支持 Enum 转换吗？
**A**: v1.2 开始支持。使用 `EnumStringConverter` 可以实现 Enum ↔ String/Integer 转换。

### Q: 支持 Map 转换吗？
**A**: 分两种场景：
- **Bean ↔ Map 转换（v1.5.0）**：使用 `@CopyToMap` / `@CopyFromMap`，生成 `MapCopier` 的 `toMap` / `fromMap` 方法，详见上文「v1.5.0 如何使用 Bean ↔ Map 转换」
- **Bean 中的 Map 字段**：`Map<K,V>` 类型字段在 Bean ↔ Bean 拷贝时，Key 通常直接拷贝，Value 按深拷贝规则处理

### Q: 支持 Builder 模式吗？
**A**: 当前版本不支持。Copier 类使用 setter 方法进行赋值。

## v1.2 新功能问题

### Q: 如何使用多对一映射？
**A**: 使用 `@CopyField` 注解的 `source` 数组和 `expression` 属性：
```java
@CopyField(source = {"firstName", "lastName"}, 
           expression = "java(source.getFirstName() + \" \" + source.getLastName())")
private String fullName;
```

### Q: 如何使用一对多映射？
**A**: 多个目标字段引用同一个源字段：
```java
@CopyField(source = "fullName", 
           expression = "java(source.getFullName().split(\" \")[0])")
private String firstName;

@CopyField(source = "fullName", 
           expression = "java(source.getFullName().split(\" \")[1])")
private String lastName;
```

### Q: 表达式中如何引用源对象？
**A**: 使用 `source` 变量，如 `source.getXxx()`。

### Q: 如何使用内置 TypeConverter？
**A**: 使用 `@CopyField` 的 `converter` 和 `format` 属性：
```java
@CopyField(converter = NumberFormatter.class, format = "#,##0.00")
private String priceText;

@CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd")
private String dateText;
```

### Q: 有哪些内置 TypeConverter？
**A**: 
- `NumberFormatter`：Number → String 格式化
- `NumberParser`：String → Number 解析
- `DateFormatter`：Date/LocalDate/LocalDateTime → String 格式化
- `DateParser`：String → 日期类型 解析
- `EnumStringConverter`：Enum ↔ String/Integer 转换
- `JsonConverter`：Object ↔ JSON String 转换

### Q: 如何与 Spring 集成？
**A**: 使用 `componentModel = ComponentModel.SPRING`：
```java
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { }

@Service
public class UserService {
    @Autowired
    private UserDtoCopier userDtoCopier;
}
```

### Q: 支持哪些依赖注入框架？
**A**: 
- `ComponentModel.DEFAULT`：无依赖注入，静态方法
- `ComponentModel.SPRING`：Spring 框架
- `ComponentModel.CDI`：CDI 框架
- `ComponentModel.JSR330`：JSR-330 标准

### Q: 如何使用函数式定制？
**A**: 使用 `preProcessor` + `postProcessor` 双处理器 API（v1.4+，v1.5.0 为唯一方式）：
```java
UserDto dto = UserDtoCopier.toDto(user,
    source -> { /* 拷贝前处理 source */ return source; },
    result -> {
        result.setDisplayName(result.getName().toUpperCase());
        return result;
    }
);
```

### Q: TypeConverter 在不同模式下如何注入？
**A**: 
- DEFAULT 模式：使用静态实例
- SPRING/CDI/JSR330 模式：通过构造器注入，如果容器中没有 Bean 则使用默认实例

## v1.3 新功能问题

### Q: 如何使用 updateDto/updateEntity 方法？
**A**: 这些方法用于更新已存在的对象：
```java
// 更新已存在的 DTO 对象
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");
UserDtoCopier.updateDto(existingDto, user);

// 更新已存在的实体对象
User existingUser = new User();
UserDtoCopier.updateEntity(existingUser, userDto);
```

### Q: updateDto 和 toDto 有什么区别？
**A**: 
- `toDto`：创建新的目标对象并拷贝字段
- `updateDto`：更新已存在的目标对象，不创建新对象

### Q: 如何使用映射前回调？
**A**: v1.5.0 已移除 `beforeMapping` 属性，请使用 `preProcessor` 在拷贝前处理源对象：
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

### Q: MapCopier 能与 BeanCopier 同时使用吗？
**A**: 可以。在同一类上同时标注 `@CopyTarget`、`@CopyToMap`、`@CopyFromMap`，会分别生成 `UserDtoCopier` 与 `UserDtoMapCopier`，互不干扰。

### Q: Map key 的命名优先级是什么？
**A**: `@CopyField(mapKey)` > `@CopyToMap(keyStrategy)` / `@CopyFromMap(keyStrategy)` > 字段名。例如 `keyStrategy = SNAKE_CASE` 时字段 `firstName` 的 key 为 `"first_name"`；若该字段设置了 `@CopyField(mapKey = "fn")`，则 key 为 `"fn"`。

### Q: 如何使用条件映射？
**A**: 使用 `@CopyField` 的 `condition` 属性：
```java
@CopyField(condition = "java(source.getName() != null)")
private String name;

@CopyField(condition = "java(source.getAge() > 18)")
private Integer age;
```

### Q: 条件表达式的格式是什么？
**A**: 使用 `java(...)` 格式包裹条件表达式，表达式中 `source` 变量代表源对象。

### Q: 如何设置默认值？
**A**: 使用 `@CopyField` 的 `defaultValue` 属性：
```java
@CopyField(defaultValue = "未知")
private String name;

@CopyField(defaultValue = "0")
private Integer count;
```

### Q: defaultValue 支持哪些类型？
**A**: 支持 String、Integer、Long、Double、Float、Short、Byte、Boolean、BigDecimal、BigInteger。

### Q: 如何设置常量值？
**A**: 使用 `@CopyField` 的 `constant` 属性：
```java
@CopyField(constant = "SYSTEM")
private String createdBy;
```

### Q: defaultValue 和 constant 有什么区别？
**A**: 
- `defaultValue`：当源字段为 null 时使用的默认值
- `constant`：直接设置常量值，不依赖源字段（与 defaultValue 互斥）

### Q: 如何使用全局配置？
**A**: 在 `package-info.java` 中使用 `@CopyTargetConfig`：
```java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

### Q: 全局配置的优先级是什么？
**A**: 类级别 > 包级别 > 默认值。类级别的配置会覆盖包级别的配置。

### Q: NullValueStrategy 有哪些选项？
**A**: 
- `IGNORE`：忽略 null 值，不更新目标字段（默认）
- `REPLACE`：替换 null 值，将目标字段设置为 null

### Q: NullValueStrategy 主要用于什么场景？
**A**: 主要用于 updateDto/updateEntity 方法，决定当源字段为 null 时是否更新目标字段。

## v1.3.2 新功能问题

### Q: 如何使用嵌套对象深拷贝？
**A**: 无需任何配置，自动生效。只需定义嵌套对象的 DTO 类：

```java
// 源实体
public class Employee {
    private Long id;
    private Address address;
}

// 目标 DTO
@CopyTarget(source = Employee.class)
public class EmployeeDto {
    private Long id;
    private AddressDto address;  // 自动深拷贝
}

// 嵌套对象 DTO（可选添加 @CopyTarget）
@CopyTarget(source = Address.class)  // 可选，添加后性能更优
public class AddressDto {
    private String city;
}

// 使用
EmployeeDto dto = EmployeeDtoCopier.toDto(employee);
```

### Q: 什么时候应该为嵌套对象添加 @CopyTarget 注解？
**A**: 建议根据使用频率决定：
- **频繁使用的嵌套对象**：添加 @CopyTarget，生成 Copier 类，性能最优
- **简单的一次性嵌套对象**：不添加注解，使用字段拷贝，减少代码量
- **复杂的嵌套对象（多字段）**：添加 @CopyTarget，便于维护和复用

### Q: 嵌套对象深拷贝支持反向转换吗？
**A**: 完全支持。`fromDto()` 方法也会自动深拷贝嵌套对象：

```java
// 正向转换
EmployeeDto dto = EmployeeDtoCopier.toDto(employee);

// 反向转换（也支持嵌套对象深拷贝）
Employee entity = EmployeeDtoCopier.fromDto(dto);
```

### Q: 如何处理嵌套对象的字段映射？
**A**: 如果嵌套对象需要自定义字段映射，为嵌套对象添加 @CopyTarget 注解并使用 @CopyField：

```java
@CopyTarget(source = Address.class)
public class AddressDto {
    @CopyField(source = "cityName")  // 自定义映射
    private String city;
    
    @CopyField(ignore = true)  // 忽略字段
    private String internalCode;
}
```

### Q: 嵌套对象深拷贝的性能如何？
**A**: 
- **有 Copier**：与手写代码性能相同，编译期生成，零运行时开销
- **无 Copier（字段拷贝）**：性能略低但仍然很好，也是编译期生成，无反射
- **建议**：性能敏感的场景为嵌套对象添加 @CopyTarget 注解

## v1.3.1 新功能问题

### Q: 如何使用 Map 批量转换的 UnaryOperator 重载？
**A**: 使用带 `UnaryOperator` 参数的重载方法：
```java
// 过滤 Map 条目
Map<String, UserDto> filteredMap = UserDtoCopier.toDtoMap(userMap, result -> {
    result.entrySet().removeIf(entry -> entry.getValue().getId() == null);
    return result;
});

// 转换为不可变 Map
Map<String, UserDto> immutableMap = UserDtoCopier.toDtoMap(userMap, 
    result -> Collections.unmodifiableMap(result));
```

### Q: 如何使用 Array 批量转换的 UnaryOperator 重载？
**A**: 使用带 `UnaryOperator` 参数的重载方法：
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

### Q: 如何使用 Properties 配置文件？
**A**: 在 `src/main/resources/` 目录下创建 `fast-bean-copier.properties` 文件：
```properties
# 组件模型：DEFAULT, SPRING, CDI, JSR330
fast.bean.copier.componentModel=SPRING

# 空值策略：IGNORE, REPLACE
fast.bean.copier.nullValueStrategy=IGNORE
```

### Q: Properties 配置文件的优先级是什么？
**A**: 配置优先级：类级别 > 包级别 > 配置文件 > 默认值。类级别的配置会覆盖配置文件中的配置。

### Q: 什么是逆向转换智能跳过？
**A**: 在 `fromDto/updateEntity` 方法中，使用了特殊映射配置的字段会自动跳过，因为这些映射是不可逆的：
- 使用了 `typeConverter` 的字段
- 使用了 `expression` 的字段
- 使用了 `qualifiedByName` 的字段
- 使用了 `constant` 的字段

生成的代码会包含中文注释说明跳过原因。

### Q: 为什么特殊字段在逆向转换时会被跳过？
**A**: 这些字段的映射是单向的、不可逆的：
- **typeConverter**：类型转换器通常是单向转换，逆向转换可能丢失信息
- **expression**：表达式映射是计算得出的，无法反向推导
- **qualifiedByName**：具名方法映射是自定义转换，通常不可逆
- **constant**：常量值不依赖源字段，无法反向映射

## 类型转换问题

### Q: 基本类型与包装类型如何转换？
**A**: Fast Bean Copier 自动支持基本类型与包装类型的转换，null 会转换为基本类型的默认值。

### Q: 支持 String 与其他类型的转换吗？
**A**: v1.2 开始支持。使用 TypeConverter 可以实现 String 与数字、日期等类型的转换。

## 字段问题

### Q: 如何忽略某些字段？
**A**: 使用 `@CopyTarget` 注解的 `ignore` 属性：
```java
@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserDto { }
```

### Q: 如何处理源类和目标类中字段名不同的情况？
**A**: v1.2 开始支持。使用 `@CopyField` 的 `source` 属性指定源字段名。

### Q: 如何处理源类中有但目标类中没有的字段？
**A**: 这些字段会被忽略，不会被拷贝。

### Q: 如何处理目标类中有但源类中没有的字段？
**A**: 这些字段不会被初始化，保持默认值。可以使用函数式定制来设置这些字段。

## 性能问题

### Q: Fast Bean Copier 的性能如何？
**A**: Fast Bean Copier 在编译期生成代码，运行时性能与手写代码相同。没有反射开销。

### Q: 生成的代码可以被 JIT 编译器优化吗？
**A**: 可以。生成的代码是普通 Java 代码，JIT 编译器可以进行内联等优化。

### Q: 生成的 Copier 类是否线程安全？
**A**: 是的。DEFAULT 模式生成的 Copier 类是无状态的，DI 模式生成的 Copier 类是不可变的。

## 开发问题

### Q: 生成的代码在哪里？
**A**: 在 `target/generated-sources/annotations/` 目录下。

### Q: 如何在 IDE 中查看生成的代码？
**A**: 大多数 IDE 都会自动识别生成的源代码。如果看不到，刷新项目或重新构建。

### Q: 支持 Lombok 吗？
**A**: 支持。Fast Bean Copier 可以与 Lombok 一起使用。

## 故障排除

### Q: 编译时出现 "找不到符号" 错误
**A**: 
1. 检查 `@CopyTarget` 注解是否正确使用
2. 检查源类是否存在
3. 运行 `mvn clean compile` 进行完整编译

### Q: 生成的 Copier 类未出现
**A**:
1. 确保使用了 `@CopyTarget` 注解
2. 确保源类和目标类都有 getter/setter 方法
3. 运行 `mvn clean compile` 进行完整编译

### Q: 字段未被拷贝
**A**:
1. 检查字段名是否完全相同（区分大小写）
2. 检查源类和目标类是否都有该字段的 getter/setter
3. 检查该字段是否在 `ignore` 属性中

### Q: 表达式编译错误
**A**:
1. 检查表达式语法是否正确
2. 确保使用 `source` 变量引用源对象
3. 添加 null 检查避免 NullPointerException

### Q: TypeConverter 未生效
**A**:
1. 确保 `converter` 属性指定了正确的类
2. 检查 `format` 参数是否正确
3. 确保泛型参数与字段类型匹配

## 获取帮助

### Q: 如何报告 Bug？
**A**: 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出问题。

### Q: 如何提出功能请求？
**A**: 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出。

### Q: 如何贡献代码？
**A**: 欢迎提交 Pull Request！
