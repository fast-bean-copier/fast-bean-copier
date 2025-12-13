# Fast Bean Copier 快速入门指南

## 5 分钟快速开始

### 步骤 1：添加依赖

在您的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-processor</artifactId>
    <version>1.0.0</version>
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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}
```

### 步骤 4：编译

```bash
mvn clean compile
```

编译完成后，Fast Bean Copier 会自动生成 `UserDtoCopier` 类。

### 步骤 5：使用

```java
// 创建源对象
User user = new User();
user.setId(1L);
user.setName("张三");
user.setEmail("zhangsan@example.com");
user.setAge(25);

// 转换为 DTO
UserDto userDto = UserDtoCopier.toDto(user);

// 打印结果
System.out.println("ID: " + userDto.getId());
System.out.println("Name: " + userDto.getName());
System.out.println("Email: " + userDto.getEmail());
System.out.println("Age: " + userDto.getAge());
```

## 常见场景

### 场景 1：忽略敏感字段

```java
@CopyTarget(source = User.class, ignore = {"password"})
public class UserDto {
    private Long id;
    private String name;
    private String email;
    // password 字段不会被拷贝
}
```

### 场景 2：批量转换

```java
List<User> users = userRepository.findAll();
List<UserDto> userDtos = UserDtoCopier.toDtoList(users);
```

### 场景 3：双向转换

```java
// 从 DTO 转换为实体
User user = UserDtoCopier.fromDto(userDto);

// 保存到数据库
userRepository.save(user);

// 再转换回 DTO
UserDto response = UserDtoCopier.toDto(user);
```

### 场景 4：集合转换

```java
// List 转换
List<UserDto> dtos = UserDtoCopier.toDtoList(users);

// Set 转换
Set<UserDto> dtoSet = UserDtoCopier.toDtoSet(users);

// 反向转换
List<User> users = UserDtoCopier.fromDtoList(dtos);
Set<User> userSet = UserDtoCopier.fromDtoSet(dtoSet);
```

## 故障排除

### 问题：生成的 Copier 类未出现

**解决方案**：
1. 确保使用了 `@CopyTarget` 注解
2. 确保源类和目标类都有 getter/setter 方法
3. 运行 `mvn clean compile` 进行完整编译

### 问题：字段未被拷贝

**解决方案**：
1. 检查字段名是否完全相同（区分大小写）
2. 检查源类和目标类是否都有该字段的 getter/setter
3. 检查该字段是否在 `ignore` 属性中

### 问题：IDE 中看不到生成的代码

**解决方案**：
1. 在 IDE 中刷新项目（F5 或右键 -> Refresh）
2. 重新构建项目（Clean -> Build）
3. 检查 IDE 是否启用了注解处理（通常默认启用）

## 下一步

- 查看 [参考文档](REFERENCE.md) 了解更多功能
- 查看 [示例代码](../fast-bean-copier-examples) 了解更多用法
- 在 [GitHub Issues](https://github.com/jackieonway/fast-bean-copier/issues) 中提出问题

## 提示

- 使用 Lombok 的 `@Data` 注解可以自动生成 getter/setter
- 生成的 Copier 类是无状态的，可以安全地在多线程环境中使用
- 生成的代码性能与手写代码相同

## 许可证

Fast Bean Copier 采用 Apache License 2.0 许可证。
