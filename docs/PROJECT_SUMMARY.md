# Fast Bean Copier 项目总结

## 项目概述

**Fast Bean Copier** 是一个高性能的 Java Bean 拷贝工具，使用 APT（注解处理工具）在编译期自动生成拷贝代码，实现零运行时开销。

**项目状态**：✅ 已完成，生产就绪（v1.4.0 新增函数式处理增强和深拷贝控制）

## 项目信息

- **项目名称**：Fast Bean Copier
- **版本**：1.4.0
- **发布日期**：2026-03-29
- **Java 版本**：Java 8+
- **构建工具**：Maven
- **许可证**：Apache License 2.0

## 项目结构

```
fast-bean-copier/
├── fast-bean-copier-annotations/      # 注解定义模块
│   ├── CopyTarget.java               # 目标类注解
│   ├── CopyField.java                # 字段映射注解（v1.2）
│   ├── ComponentModel.java           # 组件模型枚举（v1.2）
│   └── TypeConverter.java            # 类型转换器接口（v1.2）
├── fast-bean-copier-processor/        # APT 处理器模块
│   ├── BeanCopierProcessor.java      # 注解处理器（协调者）
│   ├── CodeGenerator.java            # 代码生成器（协调者）
│   ├── context/                      # 上下文包（v1.2.1）
│   │   └── ProcessorContext.java     # 处理器上下文
│   ├── extractor/                    # 提取器包（v1.2.1）
│   │   └── AnnotationExtractor.java  # 注解提取器
│   ├── analyzer/                     # 分析器包（v1.2.1）
│   │   └── FieldMappingAnalyzer.java # 字段映射分析器
│   ├── generator/                    # 生成器包（v1.2.1）
│   │   ├── ClassStructureGenerator.java  # 类结构生成器
│   │   ├── BasicMethodGenerator.java     # 基础方法生成器
│   │   ├── CollectionMethodGenerator.java # 集合方法生成器
│   │   ├── FieldCopyGenerator.java       # 字段拷贝生成器
│   │   └── DeepCopyGenerator.java        # 深拷贝生成器
│   ├── model/                        # 模型包
│   │   ├── FieldMapping.java         # 字段映射模型
│   │   └── CopyFieldConfig.java      # 字段配置数据类（v1.2.1）
│   ├── ConverterAnalyzer.java        # 转换器分析器（v1.2）
│   ├── ExpressionParser.java         # 表达式解析器（v1.2）
│   ├── ExpressionUtils.java          # 表达式工具
│   └── TypeUtils.java                # 类型工具
├── fast-bean-copier-examples/         # 示例与测试
│   ├── v10/                          # v1.0 示例
│   ├── v11/                          # v1.1 示例
│   └── v12/                          # v1.2 示例
├── docs/                              # 文档
└── pom.xml                            # 父 POM
```

## 核心功能

### ✅ v1.0 功能

1. **APT 编译期代码生成** - 使用 JavaPoet 生成 Copier 类
2. **同名字段自动拷贝** - 支持基本类型和对象类型
3. **基本类型 ↔ 包装类型转换** - 自动装箱/拆箱
4. **字段忽略功能** - @CopyTarget 注解的 ignore 属性
5. **List/Set 集合拷贝** - 自动生成集合映射方法
6. **双向拷贝支持** - toDto/fromDto 方法

### ✅ v1.1 功能

7. **集合深拷贝** - List/Set/Map/数组字段级深拷贝
8. **嵌套集合支持** - List<List<T>>、Map<K, List<V>> 等
9. **多维数组支持** - 支持多维数组深拷贝
10. **Raw/通配符降级** - 自动降级为浅拷贝并警告

### ✅ v1.2 功能

11. **多对一转换** - 多个源字段合并到一个目标字段
12. **一对多转换** - 一个源字段拆分到多个目标字段
13. **表达式映射** - 支持 Java 表达式进行复杂转换
14. **TypeConverter** - 6 个内置类型转换器
15. **自定义转换器** - 通过 uses 机制引入自定义转换器
16. **依赖注入支持** - Spring、CDI、JSR-330 框架集成
17. **函数式定制** - UnaryOperator 后处理支持

### ✅ v1.2.1 重构

18. **处理器架构重构** - BeanCopierProcessor 和 CodeGenerator 拆分为多个职责单一的组件
19. **ProcessorContext** - 处理器上下文，封装共享状态和工具
20. **AnnotationExtractor** - 注解提取器，从注解中提取配置信息
21. **FieldMappingAnalyzer** - 字段映射分析器，分析字段映射关系
22. **生成器组件** - ClassStructureGenerator、BasicMethodGenerator、CollectionMethodGenerator、FieldCopyGenerator、DeepCopyGenerator

### ✅ v1.3.0 功能

23. **更新现有对象** - updateDto/updateEntity 方法，支持更新已存在的对象
24. **映射前回调** - beforeMapping 属性，在映射前执行验证、初始化等自定义逻辑
25. **条件映射** - condition 属性，基于条件决定是否映射字段
26. **默认值和常量** - defaultValue/constant 属性，设置字段的默认值和常量值
27. **全局配置** - @CopyTargetConfig 注解，包级别配置减少重复配置
28. **Null 值处理策略** - NullValueStrategy 枚举，IGNORE 或 REPLACE 策略

### ✅ v1.3.1 功能

29. **Map 批量转换 UnaryOperator 重载** - toDtoMap/fromDtoMap 支持函数式后处理
30. **Array 批量转换 UnaryOperator 重载** - toDtoArray/fromDtoArray 支持函数式后处理
31. **Properties 配置文件支持** - 通过 fast-bean-copier.properties 进行全局配置
32. **配置优先级合并** - 类级别 > 包级别 > 配置文件 > 默认值
33. **逆向转换智能跳过** - 自动跳过不可逆的特殊字段映射（typeConverter、expression、qualifiedByName、constant）

### ✅ v1.4.0 功能

34. **函数式处理增强** - preProcessor + postProcessor 双处理器 API
35. **双处理器方法** - toDto/fromDto 支持预处理和后处理
36. **集合双处理器** - 所有集合方法支持双处理器
37. **深拷贝控制** - @CopyField.deepCopy 属性，字段级控制深拷贝行为
38. **嵌套对象深拷贝控制** - 控制嵌套对象是否深拷贝
39. **集合深拷贝控制** - 控制集合元素是否深拷贝
40. **数组深拷贝控制** - 控制数组元素是否深拷贝
41. **beforeMapping 废弃** - 标记为 @Deprecated，建议使用 preProcessor
42. **customizer 废弃** - 标记为 @Deprecated，建议使用 postProcessor

## 技术栈

- **Java 8** - 编程语言
- **Maven** - 项目构建工具
- **APT** - 注解处理工具
- **JavaPoet 1.13.0** - 代码生成库
- **Google Auto Service 1.0.1** - APT 自动注册
- **JUnit 4.13.2** - 单元测试框架
- **Jackson** - JSON 处理（JsonConverter 依赖）

## 测试覆盖

- **测试用例**：471+（涵盖所有功能）
- **示例模块指令覆盖率**：95%+（Jacoco）
- **处理器模块覆盖率**：80%+（Jacoco）
- **所有测试通过** ✅

### v1.4.0 测试类

- `V140ProcessorIntegrationTest` - 双处理器功能测试（10 个测试用例）
- `V140BeforeMappingCompatibilityTest` - beforeMapping 兼容性测试（5 个测试用例）
- `V140DeepCopyControlTest` - 深拷贝控制功能测试（10 个测试用例）

### v1.3.2 测试类

- `ReverseSkipFieldTest` - 逆向转换跳过字段测试（7 个测试用例）
- `PropertiesConfigLoaderTest` - 配置文件读取测试（19 个测试用例）
- `ConfigMergerTest` - 配置优先级合并测试（20 个测试用例）
- `V131UnaryOperatorIntegrationTest` - UnaryOperator 集成测试（14 个测试用例）
- `PropertiesConfigIntegrationTest` - Properties 配置集成测试（6 个测试用例）

### v1.3.1 测试类

- `ReverseSkipFieldTest` - 逆向转换跳过字段测试（7 个测试用例）
- `PropertiesConfigLoaderTest` - 配置文件读取测试（19 个测试用例）
- `ConfigMergerTest` - 配置优先级合并测试（20 个测试用例）
- `V131UnaryOperatorIntegrationTest` - UnaryOperator 集成测试（14 个测试用例）
- `PropertiesConfigIntegrationTest` - Properties 配置集成测试（6 个测试用例）

### v1.3.0 测试类

- `PackageConfigTest` - 全局配置测试
- `ConditionalMappingTest` - 条件映射测试
- `DefaultValueConstantTest` - 默认值和常量测试
- `UpdateExistingObjectTest` - 更新现有对象基础测试
- `UpdateNestedObjectTest` - 更新现有对象嵌套处理测试
- `BeforeMappingCallbackTest` - 映射前回调测试
- `V13IntegrationTest` - v1.3 功能集成测试
- `V13CombinationTest` - 组合功能测试
- `V13BackwardCompatibilityTest` - 向后兼容性测试
- `V13PerformanceBenchmarkTest` - 性能基准测试

### v1.2.1 测试类

- `ProcessorContextTest` - 处理器上下文测试
- `AnnotationExtractorTest` - 注解提取器测试
- `FieldMappingAnalyzerTest` - 字段映射分析器测试
- `ClassStructureGeneratorTest` - 类结构生成器测试
- `BasicMethodGeneratorTest` - 基础方法生成器测试
- `CollectionMethodGeneratorTest` - 集合方法生成器测试
- `FieldCopyGeneratorTest` - 字段拷贝生成器测试
- `DeepCopyGeneratorTest` - 深拷贝生成器测试
- `CodeGeneratorIntegrationTest` - 代码生成器集成测试
- `BeanCopierProcessorIntegrationTest` - 处理器集成测试

### v1.2 测试类

- `OneToManyMappingTest` - 一对多映射测试
- `FormattingTest` - 格式化转换器测试
- `ComponentModelTest` - 依赖注入模式测试

## 代码质量

- **编译**：✅ 无错误
- **代码规范**：✅ UTF-8 编码，中文注释
- **文档完整**：✅ 详细的参考文档和 API 文档
- **线程安全**：✅ 生成的代码是无状态/不可变的
- **性能**：✅ 与手写代码性能相同

## 项目亮点

### 1. 简洁易用
只需 `@CopyTarget` 注解，自动生成所有拷贝方法。

### 2. 高性能
编译期代码生成，零运行时反射，性能与手写代码相同。

### 3. 类型安全
编译期类型检查，避免运行时错误。

### 4. 功能丰富
支持字段忽略、类型转换、集合处理、双向拷贝、多字段映射、依赖注入等。

### 5. 零依赖
生成的代码不依赖任何外部库（JsonConverter 除外）。

### 6. 框架集成
支持 Spring、CDI、JSR-330 等主流依赖注入框架。

## 使用示例

### 基本用法

```java
@CopyTarget(source = User.class)
public class UserDto {
    private Long id;
    private String name;
}

// 使用
UserDto dto = UserDtoCopier.toDto(user);
```

### 多字段映射（v1.2）

```java
@CopyTarget(source = Person.class)
public class PersonDto {
    @CopyField(source = {"firstName", "lastName"}, 
               expression = "java(source.getFirstName() + \" \" + source.getLastName())")
    private String fullName;
}
```

### 类型转换（v1.2）

```java
@CopyTarget(source = Product.class)
public class ProductDto {
    @CopyField(converter = NumberFormatter.class, format = "#,##0.00元")
    private String priceText;
}
```

### Spring 集成（v1.2）

```java
@CopyTarget(source = User.class, componentModel = ComponentModel.SPRING)
public class UserDto { }

@Service
public class UserService {
    @Autowired
    private UserDtoCopier userDtoCopier;
}
```

### 函数式定制（v1.2）

```java
UserDto dto = UserDtoCopier.toDto(user, result -> {
    result.setDisplayName(result.getName().toUpperCase());
    return result;
});
```

### 更新现有对象（v1.3）

```java
// 更新已存在的 DTO 对象
UserDto existingDto = new UserDto();
existingDto.setName("原始名称");
UserDtoCopier.updateDto(existingDto, user);

// 更新已存在的实体对象
User existingUser = new User();
UserDtoCopier.updateEntity(existingUser, userDto);
```

### Map 批量转换定制（v1.3.1）

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

### Properties 配置文件（v1.3.1）

```properties
# fast-bean-copier.properties
fast.bean.copier.componentModel=SPRING
fast.bean.copier.nullValueStrategy=IGNORE
```

### 函数式处理（v1.4）

```java
// 预处理和后处理
UserDto dto = UserDtoCopier.toDto(
    user,
    source -> {
        source.setName(source.getName().trim());
        return source;
    },
    target -> {
        target.setDisplayName(target.getName().toUpperCase());
        return target;
    }
);
```

### 深拷贝控制（v1.4）

```java
@CopyTarget(source = Order.class)
public class OrderDto {
    // 深拷贝：完全独立的副本
    @CopyField(deepCopy = true)
    private CustomerDto customer;
    
    // 浅拷贝：共享引用，性能优化
    @CopyField(deepCopy = false)
    private StatusDto status;
}
```

### 条件映射和默认值（v1.3）

```java
@CopyTarget(source = User.class)
public class UserDto {
    // 条件映射
    @CopyField(condition = "java(source.getName() != null)")
    private String name;
    
    // 默认值
    @CopyField(defaultValue = "未知")
    private String status;
    
    // 常量值
    @CopyField(constant = "SYSTEM")
    private String createdBy;
}
```

### 全局配置（v1.3）

```java
// package-info.java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.IGNORE
)
package com.example.dto;

import com.github.jackieonway.copier.annotation.*;
```

## 构建和部署

### 本地构建

```bash
mvn clean install
```

### 运行测试

```bash
mvn clean test
```

### 生成覆盖率报告

```bash
mvn jacoco:report
```

## 文档

- **README.md** - 项目说明和快速开始
- **docs/REFERENCE.md** - 完整的参考文档
- **docs/GETTING_STARTED.md** - 快速入门指南
- **docs/API.md** - 详细的 API 文档
- **docs/FAQ.md** - 常见问题解答
- **docs/CHANGELOG.md** - 更新日志
- **docs/PROJECT_SUMMARY.md** - 项目总结（本文件）

## 性能指标

- **编译时间**：< 1 秒（增量编译）
- **运行时性能**：与手写代码相同
- **代码生成大小**：~2-5KB 每个 Copier 类
- **内存占用**：最小化，无额外开销

## 兼容性

- **Java 版本**：8+
- **IDE**：IntelliJ IDEA、Eclipse、VS Code 等
- **构建工具**：Maven、Gradle
- **框架**：Spring、CDI、JSR-330 等

## 版本历史

### 1.4.0（2026-03-29）
- 函数式处理增强：preProcessor + postProcessor 双处理器 API
- 深拷贝控制：@CopyField.deepCopy 属性
- beforeMapping 和 customizer 方法标记为 @Deprecated
- 所有集合方法支持双处理器
- 471+ 测试用例，覆盖率 95%+

### 1.3.2（2026-02-03）
- 嵌套对象深拷贝支持
- 自动深拷贝不同类型的嵌套对象
- 无限层级嵌套和混合模式支持
- 440+ 测试用例

### 1.3.1（2026-01-28）
- Map/Array 批量转换的 UnaryOperator 重载
- Properties 配置文件支持
- 逆向转换智能跳过特殊字段
- 配置优先级合并器
- 66+ 新增测试用例，总计 396+ 测试用例

### 1.3.0（2026-01-14）
- 更新现有对象：updateDto/updateEntity 方法
- 映射前回调：beforeMapping 属性
- 条件映射：condition 属性
- 默认值和常量：defaultValue/constant 属性
- 全局配置：@CopyTargetConfig 注解
- Null 值处理策略：NullValueStrategy 枚举
- 330+ 测试用例，覆盖率 80%+

### 1.2.1（2026-01-08）
- 处理器架构重构：BeanCopierProcessor 和 CodeGenerator 拆分
- 新增组件：ProcessorContext、AnnotationExtractor、FieldMappingAnalyzer
- 新增生成器：ClassStructureGenerator、BasicMethodGenerator、CollectionMethodGenerator、FieldCopyGenerator、DeepCopyGenerator
- 代码可维护性显著提升
- 275+ 测试用例，覆盖率 80%+

### 1.2.0（2025-12-29）
- 多字段映射：多对一、一对多转换
- TypeConverter：6 个内置类型转换器
- 表达式映射：支持 Java 表达式
- 依赖注入：Spring、CDI、JSR-330 支持
- 函数式定制：UnaryOperator 后处理

### 1.1.0（2025-12-23）
- 集合深拷贝：List/Set/Map/数组
- 嵌套集合与多维数组支持
- Raw/通配符集合降级处理

### 1.0.0（2025-12-13）
- 初始版本发布
- 同名字段自动拷贝
- 基本类型与包装类型转换
- 字段忽略、集合拷贝、双向拷贝

## 项目统计

- **源代码行数**：~12000 行
- **测试代码行数**：~7000 行
- **文档行数**：~8000 行
- **总代码行数**：~27000 行
- **测试用例数**：471+ 个
- **文档文件数**：8 个

## 贡献指南

欢迎贡献代码、报告 Bug 或提出功能建议！

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 开启 Pull Request

## 许可证

本项目采用 Apache License 2.0 许可证。

## 作者

- **jackieonway** - 项目创建者和维护者

## 联系方式

- **GitHub Issues**：[https://github.com/fast-bean-copier/fast-bean-copier/issues](https://github.com/fast-bean-copier/fast-bean-copier/issues)

---

感谢使用 Fast Bean Copier！如果您喜欢这个项目，请给我们一个 Star ⭐！
