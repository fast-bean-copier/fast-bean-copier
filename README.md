# Fast Bean Copier

Fast Bean Copier 是一个高性能的 Java Bean 拷贝工具，使用 APT（注解处理工具）在编译期自动生成拷贝代码，实现零运行时开销。

> **v1.2.1 重构**：处理器架构重构，BeanCopierProcessor 和 CodeGenerator 拆分为多个职责单一的组件，代码可维护性显著提升。
>
> **v1.2 新特性**：多字段映射（多对一、一对多）、TypeConverter 类型转换器、依赖注入支持（Spring/CDI/JSR-330）、函数式定制拷贝。

## 特性

- ✅ **编译期代码生成** - 使用 APT 在编译期生成拷贝代码，零运行时反射
- ✅ **类型安全** - 编译期类型检查，避免运行时错误
- ✅ **高性能** - 生成的代码直接调用 getter/setter，性能最优
- ✅ **易用** - 只需添加 `@CopyTarget` 注解即可
- ✅ **灵活** - 支持字段忽略、类型转换、集合处理
- ✅ **完整** - 支持双向拷贝、集合/Map/数组拷贝、嵌套对象
- 🆕 **多字段映射** - 支持多对一、一对多字段映射和表达式
- 🆕 **类型转换器** - 内置数字、日期、枚举等转换器，支持自定义转换器
- 🆕 **依赖注入** - 支持 Spring、CDI、JSR-330 等依赖注入框架
- 🆕 **函数式定制** - 支持函数式后处理定制拷贝结果

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.2.1</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.2.1</version>
    <scope>provided</scope>
</dependency>
```

### 2. 定义 DTO 类

```java
import com.github.jackieonway.copier.annotation.CopyTarget;

@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // getter/setter...
}
```

### 3. 编译并使用

```bash
mvn clean compile
```

```java
// 单个对象拷贝
UserDto userDto = UserDtoCopier.toDto(user);

// 反向拷贝
User converted = UserDtoCopier.fromDto(userDto);

// 集合拷贝
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

## v1.2 新功能

### 多对一映射（字段合并）

```java
@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "source.getFirstName() + \" \" + source.getLastName()")
    private String fullName;
}
```

### 一对多映射（字段拆分）

```java
@CopyTarget(source = FullNameSource.class)
public class NameDto {
    @CopyField(source = "fullName", 
               expression = "source.getFullName().split(\" \")[0]")
    private String firstName;
    
    @CopyField(source = "fullName", 
               expression = "source.getFullName().split(\" \")[1]")
    private String lastName;
}
```

### 类型转换器

```java
@CopyTarget(source = Product.class)
public class ProductDto {
    // 数字格式化
    @CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
    private String priceText;
    
    // 日期格式化
    @CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
    private String createTimeText;
}
```

### 自定义转换器

```java
public class PersonConverter {
    public String formatAge(Integer age) {
        return age + "岁";
    }
}

@CopyTarget(source = Person.class, uses = PersonConverter.class)
public class PersonDto {
    @CopyField(qualifiedByName = "formatAge")
    private String ageText;
}
```

### Spring 集成

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { }

@Service
public class UserService {
    @Autowired
    private UserDtoCopier userDtoCopier;
    
    public UserDto getUser(Long id) {
        return userDtoCopier.toDto(userRepository.findById(id));
    }
}
```

### 函数式定制

```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

## 内置 TypeConverter

| 转换器 | 说明 | 示例 |
|--------|------|------|
| `NumberFormatter` | Number → String | `#,##0.00` |
| `NumberParser` | String → Number | `#,##0.00` |
| `DateFormatter` | Date → String | `yyyy-MM-dd` |
| `DateParser` | String → Date | `yyyy-MM-dd` |
| `EnumStringConverter` | Enum ↔ String | - |
| `JsonConverter` | Object ↔ JSON | - |

## 依赖注入支持

| 模式 | 注解 | 说明 |
|------|------|------|
| `DEFAULT` | 无 | 静态方法 |
| `SPRING` | `@Component` | Spring Bean |
| `CDI` | `@ApplicationScoped` | CDI Bean |
| `JSR330` | `@Named` + `@Singleton` | JSR-330 Bean |

## 文档

- [参考文档](docs/REFERENCE.md) - 完整的参考文档
- [快速入门指南](docs/GETTING_STARTED.md) - 5 分钟快速开始
- [API 文档](docs/API.md) - 详细的 API 文档
- [常见问题解答](docs/FAQ.md) - 常见问题和解答
- [更新日志](docs/CHANGELOG.md) - 版本更新记录
- [项目总结](docs/PROJECT_SUMMARY.md) - 项目完成情况总结

## 许可证

Apache License 2.0

## 作者

jackieonway

## 获取帮助

- 查看 [常见问题解答](docs/FAQ.md)
- 在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中提出问题
