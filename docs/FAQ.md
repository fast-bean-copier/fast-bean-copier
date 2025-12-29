# Fast Bean Copier 常见问题解答

## 许可证

本项目采用 Apache License 2.0 许可证。

## 基本问题

### Q: v1.2 有哪些新增功能？
**A**: v1.2 主要新增：
- **多字段映射**：多对一（字段合并）、一对多（字段拆分）
- **TypeConverter**：内置 6 个转换器（NumberFormatter、NumberParser、DateFormatter、DateParser、EnumStringConverter、JsonConverter）
- **表达式映射**：支持 Java 表达式进行复杂转换
- **依赖注入**：支持 Spring、CDI、JSR-330 框架集成
- **函数式定制**：支持 `UnaryOperator` 后处理

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
    implementation 'com.github.jackieonway:fast-bean-copier-annotations:1.2.0'
    annotationProcessor 'com.github.jackieonway:fast-bean-copier-processor:1.2.0'
}
```

## 功能问题

### Q: 支持嵌套对象拷贝吗？
**A**: 支持。同名字段会直接拷贝；当嵌套类型也使用 `@CopyTarget` 标注时，会自动递归深拷贝。

### Q: 集合/数组字段会自动深拷贝吗？
**A**: 会。List/Set/Map/数组（含嵌套组合与多维数组）都会按元素深拷贝。

### Q: 原始类型或无界通配符的集合如何处理？
**A**: 会回退为浅拷贝并输出编译期警告，建议为集合添加明确泛型。

### Q: 支持自定义转换器吗？
**A**: v1.2 开始支持。通过 `@CopyTarget(uses = {CustomConverter.class})` 引入自定义转换器，使用 `@CopyField(qualifiedByName = "methodName")` 指定转换方法。

### Q: 支持 Enum 转换吗？
**A**: v1.2 开始支持。使用 `EnumStringConverter` 可以实现 Enum ↔ String/Integer 转换。

### Q: 支持 Map 转换吗？
**A**: 支持。Key 通常直接拷贝，Value 会按深拷贝规则处理。

### Q: 支持 Builder 模式吗？
**A**: 当前版本不支持。Copier 类使用 setter 方法进行赋值。

## v1.2 新功能问题

### Q: 如何使用多对一映射？
**A**: 使用 `@CopyField` 注解的 `source` 数组和 `expression` 属性：
```java
@CopyField(source = {"firstName", "lastName"}, 
           expression = "source.getFirstName() + \" \" + source.getLastName()")
private String fullName;
```

### Q: 如何使用一对多映射？
**A**: 多个目标字段引用同一个源字段：
```java
@CopyField(source = "fullName", 
           expression = "source.getFullName().split(\" \")[0]")
private String firstName;

@CopyField(source = "fullName", 
           expression = "source.getFullName().split(\" \")[1]")
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
**A**: 使用带 `UnaryOperator` 参数的重载方法：
```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

### Q: TypeConverter 在不同模式下如何注入？
**A**: 
- DEFAULT 模式：使用静态实例
- SPRING/CDI/JSR330 模式：通过构造器注入，如果容器中没有 Bean 则使用默认实例

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
**A**: 在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中提出问题。

### Q: 如何提出功能请求？
**A**: 在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中提出。

### Q: 如何贡献代码？
**A**: 欢迎提交 Pull Request！
