# Fast Bean Copier 常见问题解答

## 许可证

本项目采用 Apache License 2.0 许可证。

## 基本问题

### Q: v1.1 有哪些新增/改进？
**A**: 主要新增集合与数组字段的深拷贝，涵盖 List/Set/Map/数组及其嵌套组合，支持双向拷贝、null 元素安全处理、容量预分配，以及对原始类型/无界通配符集合的安全降级（回退浅拷贝并给出编译期告警）。

### Q: Fast Bean Copier 是什么？
**A**: Fast Bean Copier 是一个基于注解处理器的 Java Bean 拷贝工具，在编译期自动生成类型安全、高性能的 Bean 映射代码。

### Q: 与 MapStruct 有什么区别？
**A**: 
- Fast Bean Copier 更简洁，只需一个 `@CopyTarget` 注解
- Fast Bean Copier 自动生成 List/Set 映射方法
- Fast Bean Copier 自动处理基本类型与包装类型转换
- MapStruct 功能更强大，支持更多高级特性

### Q: 支持哪些 Java 版本？
**A**: Java 8 及以上版本。

### Q: 可以在 Gradle 项目中使用吗？
**A**: 可以。在 `build.gradle` 中添加：
```gradle
dependencies {
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.1.0-SNAPSHOT'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.1.0-SNAPSHOT'
}
```

## 功能问题

### Q: 支持嵌套对象拷贝吗？
**A**: 支持。同名字段会直接拷贝；当嵌套类型也使用 `@CopyTarget` 标注时，会自动递归深拷贝，无需手动补齐：

```java
@CopyTarget(source = User.class)
public class UserDto {
    private AddressDto address; // 与源类 Address 对应
}
```

### Q: 集合/数组字段会自动深拷贝吗？
**A**: 会。List/Set/Map/数组（含嵌套组合与多维数组）都会按元素深拷贝，基本类型/String 直接赋值，带 `@CopyTarget` 的对象调用对应 Copier，其余按类型匹配递归处理。null 集合/元素会被安全保留，容量预分配避免多次扩容。

### Q: 原始类型或无界通配符的集合如何处理？
**A**: 遇到 `List`、`Set`、`Map` 的 raw type 或 `?`/`? super` 等无法安全解析的通配符时，会回退为浅拷贝并输出编译期警告，建议为集合添加明确泛型。

### Q: 支持自定义转换器吗？
**A**: 当前版本不支持。可以在应用层手动处理特殊字段的转换。

### Q: 支持 Enum 转换吗？
**A**: 当前版本不支持 Enum 的自动转换。可以在应用层手动处理。

### Q: 支持 Map 转换吗？
**A**: 支持。Key 通常直接拷贝（如 String/基本类型），Value 会按深拷贝规则处理；Map 为 null 时保持 null，Value 为 null 时保留；嵌套集合/数组的 Map 也会递归处理。

### Q: 支持 Stream 转换吗？
**A**: 当前版本不支持 Stream 的自动转换。可以使用 `toDtoList()` 或 `toDtoSet()` 后再转换为 Stream。

### Q: Raw/无界通配符集合如何处理？
**A**: 会降级为浅拷贝，并在编译期输出警告。建议为集合声明明确泛型参数以启用深拷贝。

### Q: 支持 Builder 模式吗？
**A**: 当前版本不支持。Copier 类使用 setter 方法进行赋值。

## 类型转换问题

### Q: 基本类型与包装类型如何转换？
**A**: Fast Bean Copier 自动支持基本类型与包装类型的转换：

```java
public class User {
    private int age;  // 基本类型
}

@CopyTarget(source = User.class)
public class UserDto {
    private Integer age;  // 包装类型，自动装箱
}

UserDto userDto = UserDtoCopier.toDto(user);
// userDto.age 为 Integer，值为 user.age
```

### Q: 包装类型转换为基本类型时，null 如何处理？
**A**: null 会转换为基本类型的默认值：

```java
UserDto userDto = new UserDto();
userDto.setAge(null);

User user = UserDtoCopier.fromDto(userDto);
// user.age 为 0（int 的默认值）
```

### Q: 支持 String 与其他类型的转换吗？
**A**: 当前版本不支持。可以在应用层手动处理。

## 字段问题

### Q: 如何忽略某些字段？
**A**: 使用 `@CopyTarget` 注解的 `ignore` 属性：

```java
@CopyTarget(source = User.class, ignore = {"password", "token"})
public class UserDto {
    // password 和 token 不会被拷贝
}
```

### Q: 如何处理源类和目标类中字段名不同的情况？
**A**: 当前版本不支持字段名映射。可以在应用层手动处理。

### Q: 如何处理源类中有但目标类中没有的字段？
**A**: 这些字段会被忽略，不会被拷贝。

### Q: 如何处理目标类中有但源类中没有的字段？
**A**: 这些字段不会被初始化，保持默认值。

## 集合问题

### Q: 支持哪些集合类型？
**A**: 当前版本支持 List 和 Set。

### Q: 支持 Map 吗？
**A**: 当前版本不支持 Map。

### Q: 集合中的 null 元素如何处理？
**A**: null 元素会被保留：

```java
List<User> users = Arrays.asList(user1, null, user2);
List<UserDto> dtos = UserDtoCopier.toDtoList(users);
// dtos 中第二个元素为 null
```

### Q: 集合为 null 时如何处理？
**A**: 返回 null：

```java
List<UserDto> dtos = UserDtoCopier.toDtoList(null);
// dtos 为 null
```

## 性能问题

### Q: Fast Bean Copier 的性能如何？
**A**: Fast Bean Copier 在编译期生成代码，运行时性能与手写代码相同。没有反射开销，直接调用 getter/setter。

### Q: 生成的代码可以被 JIT 编译器优化吗？
**A**: 可以。生成的代码是普通 Java 代码，JIT 编译器可以进行内联等优化。

### Q: 生成的 Copier 类是否线程安全？
**A**: 是的。生成的 Copier 类是无状态的，可以安全地在多线程环境中使用。

## 开发问题

### Q: 生成的代码在哪里？
**A**: 在 `target/generated-sources/annotations/` 目录下。

### Q: 如何在 IDE 中查看生成的代码？
**A**: 大多数 IDE（如 IntelliJ IDEA、Eclipse）都会自动识别生成的源代码。如果看不到，可以：
1. 刷新项目（F5 或右键 -> Refresh）
2. 重新构建项目（Clean -> Build）

### Q: 可以修改生成的代码吗？
**A**: 不推荐。生成的代码会在每次编译时被覆盖。如果需要自定义，应该在应用层处理。

### Q: 如何调试生成的代码？
**A**: 可以在 IDE 中设置断点，调试器会进入生成的代码。

### Q: 支持 Lombok 吗？
**A**: 支持。Fast Bean Copier 可以与 Lombok 一起使用，Lombok 会生成 getter/setter 方法。

```java
@Data
public class User {
    private Long id;
    private String name;
}

@Data
@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;
}
```

### Q: 支持 Gradle 的增量编译吗？
**A**: 支持。Fast Bean Copier 与 Gradle 的增量编译兼容。

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
4. 检查编译日志中是否有错误

### Q: 字段未被拷贝
**A**:
1. 检查字段名是否完全相同（区分大小写）
2. 检查源类和目标类是否都有该字段的 getter/setter
3. 检查该字段是否在 `ignore` 属性中

### Q: IDE 中看不到生成的代码
**A**:
1. 刷新项目（F5 或右键 -> Refresh）
2. 重新构建项目（Clean -> Build）
3. 检查 IDE 是否启用了注解处理（通常默认启用）

### Q: 编译时出现 "不兼容的类型" 错误
**A**:
1. 检查源类和目标类的字段类型是否兼容
2. 对于不兼容的类型，在应用层手动处理

## 最佳实践

### Q: 如何组织代码？
**A**: 推荐的结构：
```
src/
├── main/
│   ├── java/
│   │   ├── entity/      # 实体类
│   │   ├── dto/         # DTO 类
│   │   ├── service/     # 业务逻辑
│   │   └── controller/  # 控制器
│   └── resources/
└── test/
```

### Q: 何时使用 Fast Bean Copier？
**A**: 适用于：
- API 响应 DTO 转换
- 数据库实体与 DTO 转换
- 微服务间的数据转换
- 批量数据转换

### Q: 何时不使用 Fast Bean Copier？
**A**: 不适用于：
- 需要复杂的字段映射逻辑
- 需要条件转换
- 需要自定义转换器

## 获取帮助

### Q: 如何报告 Bug？
**A**: 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出问题，包括：
1. 问题描述
2. 复现步骤
3. 期望行为
4. 实际行为
5. 环境信息（Java 版本、Maven 版本等）

### Q: 如何提出功能请求？
**A**: 在 [GitHub Issues](https://github.com/fast-bean-copier/fast-bean-copier/issues) 中提出，描述：
1. 功能描述
2. 使用场景
3. 期望的 API

### Q: 如何贡献代码？
**A**: 欢迎提交 Pull Request！请确保：
1. 代码遵循项目的代码风格
2. 添加相应的单元测试
3. 更新文档
4. 提交信息清晰明了
