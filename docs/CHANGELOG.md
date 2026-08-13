# 更新日志

本项目的所有重要变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/),
本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.6.0] - 2026-06-23

### 变更

- **postProcessor 改为 BiFunction**：Bean ↔ Bean 与 Bean ↔ Map 的函数式方法中，`postProcessor` 从 `UnaryOperator<Result>` 升级为 `BiFunction<Source, Result, Result>`，回调可同时读取源输入和转换结果。
- **迁移方式**：将 `target -> { ... }` / `result -> { ... }` 改为 `(source, target) -> { ... }` / `(sources, result) -> { ... }`；`preProcessor` 仍保持 `UnaryOperator` 不变。

### 新增

- **CycleDetectionStrategy 枚举**：新增 `FAIL_FAST`、`RETURN_NULL`、`AUTOMATIC_CACHE` 三个策略值。
- **@CopyTarget.cycleDetection**：新增循环检测策略配置，默认 `FAIL_FAST`。
- **BiFunction 专项测试**：新增 `V160BiFunctionCallbackTest`，覆盖单对象、集合、Map、数组、空回调、返回新实例、原地修改、preProcessor 组合等场景。

### 验证

- `mvn clean compile -DskipTests`
- `mvn clean test`（在 `fast-bean-copier-examples` 模块目录执行，556 tests）

## [1.5.0] - 2026-06-03

### 新增

#### Bean ↔ Map 转换支持
- **@CopyToMap 注解**：标记类支持 Bean → Map 转换，生成 `{Class}MapCopier` 类
  - `toMap(T source)` - 基础转换方法
  - `toMap(T source, preProcessor, postProcessor)` - 函数式方法
  - `toMapList(List<T> sources)` / `toMapList(sources, pre, post)` - 批量 List 转换
  - `toMapSet(Set<T> sources)` / `toMapSet(sources, pre, post)` - 批量 Set 转换
- **@CopyFromMap 注解**：标记类支持 Map → Bean 转换
  - `fromMap(Map<String, Object> source)` - 基础转换方法
  - `fromMap(source, preProcessor, postProcessor)` - 函数式方法
  - `fromMapList` / `fromMapSet` 批量方法
- **MapKeyStrategy 枚举**：Map key 命名策略
  - `FIELD_NAME`（默认）：使用字段名
  - `CAMEL_CASE`：驼峰命名
  - `SNAKE_CASE`：下划线命名（如 `firstName` → `first_name`）
  - `CUSTOM`：配合 `@CopyField(mapKey)` 自定义
- **@CopyField.mapKey 属性**：字段级自定义 Map key，优先级高于 keyStrategy
- **双向转换**：同一类可同时标注 `@CopyToMap` + `@CopyFromMap`，生成双向方法
- **与 @CopyTarget 共存**：独立生成 BeanCopier 和 MapCopier，互不影响

### 移除

#### API 清理（移除废弃 API）
- **移除 @CopyTarget.beforeMapping()** 属性（v1.4.0 已废弃）
  - 请使用 `preProcessor` 参数替代
- **移除单参数 customizer 重载**（v1.4.0 已废弃）
  - `toDto(source, customizer)` → 使用 `toDto(source, null, postProcessor)`
  - `fromDto(source, customizer)` → 使用 `fromDto(source, null, postProcessor)`
  - 集合方法同理：`toDtoList(sources, customizer)` → `toDtoList(sources, null, postProcessor)`

### 改进

- **CopyFieldConfig**：新增 `mapKey` 字段支持
- **FieldMapping**：新增 `mapKey` 字段及 getter/setter
- **AnnotationExtractor**：新增 `extractMapKey()` 方法
- **MapCopierProcessor / MapCodeGenerator**：独立 Bean ↔ Map 代码生成体系，与 Bean ↔ Bean 互不影响

### 测试

- 新增 55+ 个 v1.5.0 专项测试用例
- 新增 `V150ToMapTest`：Bean → Map 转换测试
- 新增 `V150FromMapTest`：Map → Bean 转换测试
- 新增 `V150MapAndBeanCopierCoexistenceTest`：MapCopier 与 BeanCopier 共存测试
- 新增 `V150MapComponentModelTest`：MapCopier 依赖注入模式测试
- 新增 `V150ApiCleanupRegressionTest`：API 清理回归测试
- 代码覆盖率：95%+

### 兼容性

- **破坏性变更**：`beforeMapping` 属性和单参数 `customizer` 重载已完全移除
- **迁移要求**：从 v1.4.x 升级前，需将使用废弃 API 的代码迁移到 `preProcessor`/`postProcessor`
- **新增功能**：Bean ↔ Map 转换为可选能力，不影响现有 Bean ↔ Bean 代码

## [1.4.0] - 2026-03-29

### 新增

#### 函数式处理增强：preProcessor + postProcessor 双处理器
- **双处理器 API**：统一的 `preProcessor + postProcessor` 函数式接口
  - `toDto(source, preProcessor, postProcessor)`：拷贝前对 source 预处理，拷贝后对 target 后处理
  - `fromDto(source, preProcessor, postProcessor)`：拷贝前对 DTO 预处理，拷贝后对 Entity 后处理
  - 执行顺序：`preProcessor` → 字段拷贝 → `postProcessor`
- **集合方法双处理器支持**：所有集合方法支持双处理器
  - `toDtoList(sources, preProcessor, postProcessor)`
  - `toDtoSet(sources, preProcessor, postProcessor)`
  - `toDtoMap(sources, preProcessor, postProcessor)`
  - `toDtoArray(sources, preProcessor, postProcessor)`
  - 对应的 `fromDto*` 方法同样支持
- **处理器作用域**：与 v1.3.1 保持一致，对整个集合结果应用处理器

#### 深拷贝控制：@CopyField.deepCopy 属性
- **字段级深拷贝控制**：通过 `@CopyField(deepCopy = true/false)` 控制深拷贝行为
  - `deepCopy = true`（默认）：嵌套对象/集合元素深拷贝（创建新对象）
  - `deepCopy = false`：浅拷贝（直接引用传递）
- **支持类型**：
  - 嵌套对象：控制是否调用 Copier 或字段拷贝
  - 集合（List/Set/Map）：控制集合元素是否深拷贝
  - 数组：控制数组元素是否深拷贝
- **使用场景**：
  - 性能优化：对不需要深拷贝的字段使用浅拷贝
  - 共享引用：多个对象共享同一个嵌套对象实例
  - 不可变对象：对不可变对象使用浅拷贝

### 废弃

#### beforeMapping 方法废弃
- **@CopyTarget.beforeMapping()** 标记为 `@Deprecated`
  - 建议使用 `preProcessor` 替代
  - 过渡期保留，仍可正常使用
- **执行顺序兼容**：当 `preProcessor` 和 `beforeMapping` 同时存在时
  - 执行顺序：`preProcessor` → `beforeMapping` → 字段拷贝
  - 确保向后兼容

#### customizer 方法废弃
- **toDto(source, customizer)** 和 **fromDto(source, customizer)** 标记为 `@Deprecated`
  - 建议使用 `toDto(source, null, postProcessor)` 替代
  - 内部委托到新方法实现，行为保持一致
- **集合方法 customizer** 同样标记为 `@Deprecated`
  - 委托到 `(..., null, customizer)` 实现

### 改进

#### 代码生成优化
- **FieldCopyGenerator 增强**：支持 `deepCopy` 控制
  - 读取 `mapping.isDeepCopy()` 配置
  - `deepCopy=false` 时生成直接赋值代码
  - `deepCopy=true` 时保持原有深拷贝逻辑
- **BasicMethodGenerator 增强**：生成双处理器方法
  - 生成 `preProcessor` 和 `postProcessor` 参数的方法
  - 旧方法委托到新方法实现
- **CollectionMethodGenerator 增强**：生成集合双处理器方法
  - 所有集合类型统一支持双处理器
  - 旧方法委托到新方法实现

### 测试
- 新增 471+ 测试用例（+10 个新测试）
- 新增 `V140ProcessorIntegrationTest`：双处理器功能测试
- 新增 `V140BeforeMappingCompatibilityTest`：beforeMapping 兼容性测试
- 新增 `V140DeepCopyControlTest`：深拷贝控制功能测试
- 代码覆盖率：95%+

### 兼容性
- **向后兼容**：所有 v1.3.x 代码无需修改即可升级
- **废弃方法保留**：`beforeMapping` 和 `customizer` 方法仍可使用
- **默认行为不变**：`deepCopy` 默认为 `true`，保持原有深拷贝行为

---

## [1.3.2] - 2026-02-03

### 新增

#### 嵌套对象深拷贝支持
- **自动深拷贝不同类型的嵌套对象**：支持 `Address` → `AddressDto` 等不同类型嵌套对象的自动转换
  - 智能检测嵌套对象是否有对应的 Copier 类
  - 有 Copier 时使用 Copier 进行拷贝（最优性能）
  - 无 Copier 时使用字段拷贝（自动回退）
- **无限层级嵌套支持**：支持任意深度的对象图（A→B→C→D...）
  - 递归处理所有嵌套层级
  - 每层独立选择最优拷贝策略
- **混合模式支持**：在同一对象图中混合使用有 Copier 和无 Copier 的嵌套对象
  - Level1 有 `@CopyTarget` → 使用 Copier
  - Level2 无 `@CopyTarget` → 使用字段拷贝
  - Level3 有 `@CopyTarget` → 在字段拷贝中仍使用 Copier
- **正向和反向转换支持**：`toDto()` 和 `fromDto()` 都支持嵌套对象深拷贝
  - 正向转换：调用 `TargetDtoCopier.toDto()`
  - 反向转换：调用 `TargetDtoCopier.fromDto()`

### 改进

#### 类型兼容性增强
- **TypeUtils.isTypeCompatible() 增强**：识别自定义对象类型为兼容
  - 之前：不同类型的自定义对象被判定为不兼容，字段被跳过
  - 现在：自定义对象类型被判定为兼容，触发深拷贝逻辑
  - 保持对基本类型、包装类型、集合类型的原有判断逻辑

#### 代码生成优化
- **FieldCopyGenerator 增强**：新增嵌套对象处理逻辑
  - `checkCopierExists()`：通过 `@CopyTarget` 注解检测 Copier 是否存在
  - `generateNestedObjectCopyCode()`：生成优化的嵌套对象拷贝代码
  - `generateFieldBasedCopy()`：递归生成字段拷贝代码，支持嵌套 Copier 检测
  - `buildFieldMap()`：构建字段映射表，提高字段查找效率

### 测试
- 新增 440+ 测试用例（+4 个新测试）
- 新增 `SimpleNestedObjectTest`：基本嵌套对象场景测试
- 新增 `MultilevelNestedObjectTest`：多层嵌套混合模式测试
- 新增 `TypeUtilsNestedObjectTest`：类型兼容性增强测试
- 代码覆盖率：95%+

### 兼容性
- ✅ 完全向后兼容 v1.3.1
- ✅ 无需修改现有代码
- ✅ 现有嵌套对象（同类型）继续使用引用传递
- ✅ 新的嵌套对象（不同类型）自动深拷贝

---

## [1.3.1] - 2026-01-28

### 新增

#### Map/Array 批量转换的 UnaryOperator 重载
- **toDtoMap/fromDtoMap UnaryOperator 重载**：支持在 Map 批量转换后立即进行后处理
  - 方法签名：`<K> Map<K, TargetDto> toDtoMap(Map<K, Source> sources, UnaryOperator<Map<K, TargetDto>> customizer)`
  - 支持过滤、转换为不可变集合、添加额外条目等操作
  - null 安全：customizer 为 null 时直接返回转换结果
- **toDtoArray/fromDtoArray UnaryOperator 重载**：支持在 Array 批量转换后立即进行后处理
  - 方法签名：`TargetDto[] toDtoArray(Source[] sources, UnaryOperator<TargetDto[]> customizer)`
  - 支持过滤、排序、去重、限制数量等操作
  - null 安全：customizer 为 null 时直接返回转换结果

#### Properties 配置文件支持
- **PropertiesConfigLoader**：配置文件读取器
  - 支持从 `fast-bean-copier.properties` 或 `META-INF/fast-bean-copier.properties` 读取配置
  - 支持配置项：`fast.bean.copier.componentModel`、`fast.bean.copier.nullValueStrategy`
  - 配置项值验证和错误处理
- **ConfigMerger**：配置优先级合并器
  - 配置优先级：类级别 > 包级别 > 配置文件 > 默认值
  - 支持部分配置覆盖
- **全局配置**：通过配置文件为所有 Copier 提供默认配置
  - 减少重复配置
  - 统一项目配置风格

#### 逆向转换智能跳过
- **特殊字段自动跳过**：在 `fromDto/updateEntity` 方法中自动跳过不可逆的字段
  - 跳过使用了 `typeConverter` 的字段
  - 跳过使用了 `expression` 的字段
  - 跳过使用了 `qualifiedByName` 的字段
  - 跳过使用了 `constant` 的字段
- **跳过原因注释**：生成中文注释说明跳过原因
  - 注释格式：`// {映射类型} '{字段名}' 不可逆，在 fromDto() 中跳过`
  - 提高生成代码的可读性

### 改进
- UnaryOperator 方法支持链式调用和函数式编程风格
- 配置文件读取支持多路径查找，提高灵活性
- 配置优先级合并逻辑清晰，易于理解和维护
- 逆向转换跳过逻辑自动化，避免手动配置

### 修复
- 修复类级别 `ComponentModel.DEFAULT` 被配置文件覆盖的问题
  - 确保类级别配置优先级最高
  - 即使配置文件中设置了其他值，类级别的 DEFAULT 也不会被覆盖

### 测试
- 新增 `ReverseSkipFieldTest`：逆向转换跳过字段测试（7 个测试用例）
- 新增 `PropertiesConfigLoaderTest`：配置文件读取测试（19 个测试用例）
- 新增 `ConfigMergerTest`：配置优先级合并测试（20 个测试用例）
- 新增 `V131UnaryOperatorIntegrationTest`：UnaryOperator 集成测试（14 个测试用例）
- 新增 `PropertiesConfigIntegrationTest`：配置文件集成测试（6 个测试用例）
- 所有 66 个新增测试用例通过
- 所有现有测试通过，确保向后兼容

### 兼容性
- Java 8+，Maven 构建
- 完全向后兼容 v1.3.0
- 新功能为可选功能，不影响现有代码
- 保持零运行时反射开销

### 验证
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.3.0] - 2026-01-14

### 新增

#### 更新现有对象
- **updateDto 方法**：更新已存在的 DTO 对象，而不是创建新对象
  - 方法签名：`void updateDto(TargetDto target, Source source)`
  - 源对象为 null 时直接返回，不修改目标对象
- **updateEntity 方法**：更新已存在的实体对象
  - 方法签名：`void updateEntity(Source target, TargetDto source)`
  - 支持反向更新
- **嵌套对象更新**：支持嵌套对象的递归更新
  - 目标嵌套对象为 null 时自动创建新对象
  - 支持有 @CopyTarget 注解和无注解的嵌套对象
- **集合字段更新**：支持 List、Set、Map、数组字段的更新
  - 默认策略：替换整个集合

#### 映射前回调
- **beforeMapping 属性**：在 @CopyTarget 中指定映射前处理方法名
- **方法签名要求**：
  - 必须是目标类中的默认方法（default method）
  - 参数类型为源类类型
  - 返回类型为 void
- **调用时机**：在映射逻辑执行之前调用
- **用途**：验证、初始化、日志记录等前置操作

#### 条件映射
- **condition 属性**：在 @CopyField 中指定条件表达式
- **表达式格式**：`java(source.getXxx() != null)` 格式
- **条件为 true 时执行映射**，否则跳过该字段
- **支持组合使用**：可与 expression、converter 等属性组合

#### 默认值和常量
- **defaultValue 属性**：当源字段为 null 时使用的默认值
  - 支持类型：String、Integer、Long、Double、Float、Short、Byte、Boolean、BigDecimal、BigInteger
  - 自动类型转换
- **constant 属性**：直接设置常量值，不依赖源字段
  - 与 defaultValue 互斥
  - 使用时 source 属性被忽略

#### 全局配置
- **@CopyTargetConfig 注解**：包级别配置注解
  - 应用于 package-info.java 文件
  - 为包内所有 @CopyTarget 提供默认配置
- **componentModel 属性**：默认组件模型
- **nullValueStrategy 属性**：默认 null 值处理策略
- **配置优先级**：类级别 > 包级别 > 默认值

#### NullValueStrategy 枚举
- **IGNORE**：忽略 null 值，不更新目标字段（默认）
- **REPLACE**：替换 null 值，将目标字段设置为 null

### 改进
- 更新方法支持基本类型字段（跳过 null 检查）
- 条件表达式解析器支持复杂 Java 表达式
- 默认值和常量支持多种数据类型自动转换

### 测试
- 新增 `PackageConfigTest`：全局配置测试
- 新增 `ConditionalMappingTest`：条件映射测试
- 新增 `DefaultValueConstantTest`：默认值和常量测试
- 新增 `UpdateExistingObjectTest`：更新现有对象基础测试
- 新增 `UpdateNestedObjectTest`：更新现有对象嵌套处理测试
- 新增 `BeforeMappingCallbackTest`：映射前回调测试
- 新增 `V13IntegrationTest`：v1.3 功能集成测试
- 新增 `V13CombinationTest`：组合功能测试
- 新增 `V13BackwardCompatibilityTest`：向后兼容性测试
- 新增 `V13PerformanceBenchmarkTest`：性能基准测试
- 所有 330 个测试用例通过

### 兼容性
- Java 8+，Maven 构建
- 完全向后兼容 v1.2.x
- 保持零运行时反射开销

### 验证
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.2.1] - 2026-01-08

### 重构

#### 处理器架构重构
- **BeanCopierProcessor 重构**：从 ~500 行精简为 ~148 行，作为协调者角色
- **CodeGenerator 重构**：从 ~1900 行精简为 ~192 行，作为代码生成协调者
- **新增组件**：
  - `ProcessorContext`：处理器上下文，封装共享状态和工具
  - `AnnotationExtractor`：注解提取器，从注解中提取配置信息
  - `FieldMappingAnalyzer`：字段映射分析器，分析源类和目标类之间的字段映射
  - `ClassStructureGenerator`：类结构生成器，生成 Copier 类的结构
  - `BasicMethodGenerator`：基础方法生成器，生成 toDto/fromDto 等方法
  - `CollectionMethodGenerator`：集合方法生成器，生成集合类型转换方法
  - `FieldCopyGenerator`：字段拷贝生成器，生成字段拷贝代码
  - `DeepCopyGenerator`：深拷贝生成器，生成集合和嵌套对象的深拷贝代码
  - `CopyFieldConfig`：字段配置数据类，封装 @CopyField 注解配置

#### 设计原则
- **单一职责原则**：每个组件只负责一个功能
- **开闭原则**：对扩展开放，对修改关闭
- **依赖倒置原则**：高层模块不依赖低层模块，都依赖抽象

### 改进
- 代码可维护性显著提升
- 单元测试更容易编写
- 新功能开发效率提高
- 代码审查效率提升

### 测试
- 新增 275 个测试用例
- 所有现有测试通过
- 代码覆盖率达到 80%+

### 兼容性
- 生成的 Copier 类代码与 v1.2.0 完全一致
- 完全向后兼容，无需修改任何用户代码

### 验证
- `mvn clean install`
- `mvn jacoco:report`

## [1.2.0] - 2025-12-29

### 新增

#### 多字段映射
- **多对一转换**：支持将多个源字段合并映射到一个目标字段
  - 使用 `@CopyField(source = {"field1", "field2"}, expression = "java(...)")` 语法
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

[1.6.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.6.0
[1.5.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.5.0
[1.4.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.4.0
[1.3.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.3.0
[1.2.1]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.2.1
[1.2.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.2.0
[1.1.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.1.0
[1.0.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.0.0
