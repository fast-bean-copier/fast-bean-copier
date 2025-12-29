# Fast Bean Copier 快速入门指南

> v1.2 新特性：多字段映射（多对一、一对多）、TypeConverter 类型转换器、依赖注入支持、函数式定制拷贝。

## 5 分钟快速开始

### 步骤 1：添加依赖

在您的 `pom.xml` 中添加以下依赖：

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

## v1.2 新功能

### 多对一映射

```java
@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "source.getFirstName() + \" \" + source.getLastName()")
    private String fullName;
}
```

### 一对多映射

```java
@CopyTarget(source = FullNameSource.class)
public class NameDto {
    @CopyField(source = "fullName", 
               expression = "source.getFullName().split(\" \")[0]")
    private String firstName;
}
```

### 数字格式化

```java
@CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
private String priceText;
```

### 日期格式化

```java
@CopyField(converter = DateFormatter.class, format = "yyyy-MM-dd HH:mm:ss")
private String createTimeText;
```

### 自定义转换器

```java
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
}
```

### 函数式定制

```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

## 下一步

- 查看 [参考文档](REFERENCE.md) 了解更多功能
- 查看 [API 文档](API.md) 了解详细 API
- 查看 [FAQ](FAQ.md) 了解常见问题

## 许可证

Apache License 2.0
