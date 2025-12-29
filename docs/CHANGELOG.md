# 更新日志

本项目的所有重要变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.2.0] - 2025-12-29

### 新增

#### 多字段映射
- **多对一转换**：支持将多个源字段合并映射到一个目标字段
  - 使用 `@CopyField(source = {"field1", "field2"}, expression = "...")` 语法
  - 支持 Java 表达式进行复杂转换
  - 示例：`firstName + lastName -> fullName`
- **一对多转换**：支持将一个源字段拆分映射到多个目标字段
  - 多个目标字段可以引用同一个源字段
  - 支持表达式进行字段拆分
  - 示例：`fullName -> firstName + lastName`

#### 类型转换器（TypeConverter）
- **内置转换器**：
  - `NumberFormatter`：`Number` → `String` 格式化（支持 `DecimalFormat` 格式字符串）
  - `NumberParser`：`String` → `Number` 解析
  - `DateFormatter`：`Date`/`LocalDate`/`LocalDateTime` → `String` 格式化
  - `DateParser`：`String` → 日期类型 解析
  - `EnumStringConverter`：`Enum` ↔ `String`/`Integer` 转换
  - `JsonConverter`：对象 ↔ JSON 字符串 转换（依赖 Jackson）
- **格式化支持**：通过 `@CopyField(converter = Xxx.class, format = "...")` 传递格式字符串
- **自定义转换器**：通过 `@CopyTarget(uses = {CustomConverter.class})` 引入自定义转换器

#### 表达式映射
- 支持 Java 表达式进行复杂字段转换
- 表达式中 `source` 变量代表源对象
- 支持方法调用、链式调用、流操作、三元运算符等
- 编译期类型检查和错误提示

#### 依赖注入支持
- **ComponentModel 枚举**：
  - `DEFAULT`：无依赖注入，生成静态方法
  - `SPRING`：Spring 框架，生成 `@Component` 注解
  - `CDI`：CDI 框架，生成 `@ApplicationScoped` 注解
  - `JSR330`：JSR-330 标准，生成 `@Named` + `@Singleton` 注解
- **构造器注入**：TypeConverter 和自定义转换器通过构造器注入
- **字段不可变性**：依赖注入模式下字段使用 `final` 修饰
- **向后兼容**：提供无参构造器以兼容没有注册 Bean 的情况

#### 函数式定制拷贝
- 新增带 `UnaryOperator` 参数的重载方法
- 支持在拷贝完成后立即执行自定义逻辑
- 方法签名：`toDto(source, UnaryOperator<DTO> customizer)`
- 集合方法同样提供重载：`toDtoList(sources, customizer)` 等
- null 安全：当源对象为 null 时，函数不被调用

#### 注解扩展
- **@CopyTarget 扩展**：
  - 新增 `uses` 属性：自定义转换器类列表
  - 新增 `componentModel` 属性：依赖注入框架选择
- **@CopyField 注解**：
  - `source[]`：源字段名数组（支持多对一）
  - `target`：目标字段名
  - `expression`：Java 表达式
  - `qualifiedByName`：具名转换方法名
  - `converter`：TypeConverter 实现类
  - `format`：格式字符串

#### 测试与覆盖率
- 新增 `OneToManyMappingTest`：一对多映射测试
- 新增 `FormattingTest`：格式化转换器测试
- 新增 `ComponentModelTest`：依赖注入模式测试
- 示例模块指令覆盖率保持 93%+

### 改进
- TypeConverter 在不同 componentModel 下采用不同注入方式
- DEFAULT 模式使用静态实例，SPRING/CDI/JSR330 模式使用构造器注入
- 表达式解析器支持更复杂的 Java 表达式语法

### 兼容性
- Java 8+，Maven 构建
- 保持零运行时反射开销
- 完全向后兼容 v1.1

### 验证
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.1.0] - 2025-12-23

### 新增

#### 集合与数组深拷贝
- List/Set/Map/数组字段级深拷贝，支持双向拷贝（toDto/fromDto）
- 嵌套组合支持：`List<List<T>>`、`Map<K, List<V>>`、`List<Map<K,V>>`、多维数组
- null 集合与 null 元素安全处理；Map 的 null value 安全保留
- 原始类型或无界通配符集合自动降级为浅拷贝并给出编译期警告

#### 工具与代码生成
- TypeUtils：集合类型识别、泛型提取、数组元素提取、深拷贝判定
- CodeGenerator：针对 List/Set/Map/数组生成预分配容量的深拷贝代码，支持反向拷贝
- 集成递归深度处理，避免无限递归

#### 测试与覆盖率
- 新增集合深拷贝、反向拷贝、嵌套集合、原始/通配符集合等测试
- 新增 `PojoCoverageTest` 覆盖所有示例 Bean 与生成的 Copier，示例模块指令覆盖率 93%+
- 性能基准与集成测试覆盖集合场景

#### 文档
- 更新快速入门、参考文档、FAQ：补充集合/数组深拷贝、反向拷贝、通配符降级说明与示例
- 生成 JavaDoc

### 改进
- 集合容量预分配与循环类型推断，减少装箱与不安全强转
- Map/集合生成代码针对泛型缺失或不受支持的通配符回退为安全赋值并发出警告

### 兼容性
- Java 8+，Maven 构建；保持零运行时反射开销

### 验证
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.0.0] - 2025-12-13

### 新增

#### 核心功能
- **APT 编译期代码生成**
  - 使用 JavaPoet 自动生成 Copier 类
  - 零运行时反射开销
  - 编译期类型检查

- **同名字段自动拷贝**
  - 支持基本类型和对象类型
  - 使用 getter/setter 方法访问字段
  - 无缝处理私有字段

- **基本类型 ↔ 包装类型转换**
  - 自动装箱/拆箱支持
  - 安全的 null 值处理
  - 支持所有 8 种基本类型（byte, short, int, long, float, double, char, boolean）

- **字段忽略功能**
  - `@CopyTarget` 注解的 `ignore` 属性
  - 灵活的字段排除机制
  - 轻松排除敏感或不必要的字段

- **List/Set 集合拷贝**
  - `toDtoList()` 方法：将 List 集合转换为 DTO
  - `toDtoSet()` 方法：将 Set 集合转换为 DTO
  - `fromDtoList()` 方法：反向 List 转换
  - `fromDtoSet()` 方法：反向 Set 转换
  - 正确处理集合中的 null 元素

- **双向拷贝支持**
  - `toDto()` 方法：实体到 DTO 的转换
  - `fromDto()` 方法：DTO 到实体的转换
  - 完整的双向转换支持

- **嵌套对象支持**
  - 支持嵌套对象字段
  - 嵌套对象的安全 null 值处理
  - 嵌套对象中同名字段的直接拷贝

#### 测试与质量
- **完整的单元测试覆盖**
  - 21 个测试用例，覆盖所有主要功能
  - 100% 测试覆盖率
  - 测试类：
    - `SameNameFieldCopyTest`（5 个测试）
    - `PrimitiveWrapperConversionTest`（4 个测试）
    - `FieldIgnoreTest`（3 个测试）
    - `CollectionCopyTest`（5 个测试）
    - `NestedObjectCopyTest`（4 个测试）
    - `TypeUtilsTest`（5 个测试）
  - 所有测试通过 ✅

#### 文档
- **完整的文档套件**
  - 参考文档（MapStruct 风格格式）
  - 快速入门指南（5 分钟快速开始）
  - API 文档
  - 常见问题解答
  - 项目总结
  - 代码示例和使用模式

#### 技术基础设施
- **Maven 模块结构**
  - `fast-bean-copier-annotations` - 注解定义模块
  - `fast-bean-copier-processor` - APT 处理器模块
  - `fast-bean-copier-examples` - 示例和测试用例模块

- **依赖项**
  - JavaPoet 1.13.0 用于代码生成
  - Google Auto Service 1.0.1 用于 APT 自动注册
  - JUnit 4.13.2 用于单元测试

### 技术细节

- **Java 版本**：8+
- **构建工具**：Maven
- **许可证**：Apache License 2.0
- **性能**：零运行时开销，与手写代码性能相同
- **代码生成**：每个 Copier 类约 2KB
- **线程安全**：生成的代码是无状态的，线程安全

### 统计信息

- **源代码**：约 2000 行
- **测试代码**：约 1000 行
- **文档**：约 3000 行
- **总代码量**：约 6000 行
- **测试用例**：21 个
- **文档文件**：5 个

---

[1.2.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.2.0
[1.1.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.1.0
[1.0.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.0.0
