# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-01-14

### Added

#### Update Existing Objects
- **updateDto Method**: Update existing DTO objects instead of creating new ones
  - Method signature: `void updateDto(TargetDto target, Source source)`
  - Returns immediately when source object is null, without modifying target
- **updateEntity Method**: Update existing entity objects (reverse update)
  - Method signature: `void updateEntity(Source target, TargetDto source)`
  - Supports reverse update
- **Nested Object Update**: Supports recursive update of nested objects
  - Automatically creates new object when target nested object is null
  - Supports nested objects with and without @CopyTarget annotation
- **Collection Field Update**: Supports List, Set, Map, array field updates
  - Default strategy: replace entire collection

#### Before Mapping Callback
- **beforeMapping Attribute**: Specify pre-mapping handler method name in @CopyTarget
- **Method Signature Requirements**:
  - Must be a default method in the target class
  - Parameter type must be the source class type
  - Return type must be void
- **Invocation Timing**: Called before mapping logic executes
- **Use Cases**: Validation, initialization, logging, and other pre-processing operations

#### Conditional Mapping
- **condition Attribute**: Specify condition expression in @CopyField
- **Expression Format**: `java(source.getXxx() != null)` format
- **Mapping executes when condition is true**, otherwise field is skipped
- **Combination Support**: Can be combined with expression, converter, and other attributes

#### Default Values and Constants
- **defaultValue Attribute**: Default value used when source field is null
  - Supported types: String, Integer, Long, Double, Float, Short, Byte, Boolean, BigDecimal, BigInteger
  - Automatic type conversion
- **constant Attribute**: Set constant value directly, independent of source field
  - Mutually exclusive with defaultValue
  - source attribute is ignored when used

#### Global Configuration
- **@CopyTargetConfig Annotation**: Package-level configuration annotation
  - Applied to package-info.java files
  - Provides default configuration for all @CopyTarget in the package
- **componentModel Attribute**: Default component model
- **nullValueStrategy Attribute**: Default null value handling strategy
- **Configuration Priority**: Class level > Package level > Default value

#### NullValueStrategy Enum
- **IGNORE**: Ignore null values, don't update target field (default)
- **REPLACE**: Replace null values, set target field to null

### Improved
- Update methods support primitive type fields (skip null check)
- Condition expression parser supports complex Java expressions
- Default values and constants support automatic conversion for multiple data types

### Testing
- Added `PackageConfigTest`: Global configuration tests
- Added `ConditionalMappingTest`: Conditional mapping tests
- Added `DefaultValueConstantTest`: Default value and constant tests
- Added `UpdateExistingObjectTest`: Update existing object basic tests
- Added `UpdateNestedObjectTest`: Update existing object nested handling tests
- Added `BeforeMappingCallbackTest`: Before mapping callback tests
- Added `V13IntegrationTest`: v1.3 feature integration tests
- Added `V13CombinationTest`: Combination feature tests
- Added `V13BackwardCompatibilityTest`: Backward compatibility tests
- Added `V13PerformanceBenchmarkTest`: Performance benchmark tests
- All 330 test cases pass

### Compatibility
- Java 8+, Maven build
- Fully backward compatible with v1.2.x
- Maintains zero runtime reflection overhead

### Verification
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.2.1] - 2026-01-08

### Refactored

#### Processor Architecture Refactoring
- **BeanCopierProcessor Refactoring**: Reduced from ~500 lines to ~148 lines, acting as coordinator
- **CodeGenerator Refactoring**: Reduced from ~1900 lines to ~192 lines, acting as code generation coordinator
- **New Components**:
  - `ProcessorContext`: Processor context, encapsulates shared state and utilities
  - `AnnotationExtractor`: Annotation extractor, extracts configuration from annotations
  - `FieldMappingAnalyzer`: Field mapping analyzer, analyzes field mappings between source and target classes
  - `ClassStructureGenerator`: Class structure generator, generates Copier class structure
  - `BasicMethodGenerator`: Basic method generator, generates toDto/fromDto methods
  - `CollectionMethodGenerator`: Collection method generator, generates collection type conversion methods
  - `FieldCopyGenerator`: Field copy generator, generates field copy code
  - `DeepCopyGenerator`: Deep copy generator, generates deep copy code for collections and nested objects
  - `CopyFieldConfig`: Field configuration data class, encapsulates @CopyField annotation configuration

#### Design Principles
- **Single Responsibility Principle**: Each component is responsible for only one function
- **Open-Closed Principle**: Open for extension, closed for modification
- **Dependency Inversion Principle**: High-level modules do not depend on low-level modules, both depend on abstractions

### Improved
- Significantly improved code maintainability
- Easier to write unit tests
- Increased efficiency for new feature development
- Improved code review efficiency

### Testing
- Added 275 new test cases
- All existing tests pass
- Code coverage reaches 80%+

### Compatibility
- Generated Copier class code is identical to v1.2.0
- Fully backward compatible, no user code changes required

### Verification
- `mvn clean install`
- `mvn jacoco:report`

## [1.2.0] - 2025-12-29

### Added

#### Multi-Field Mapping
- **Many-to-One Conversion**: Support merging multiple source fields into one target field
  - Syntax: `@CopyField(source = {"field1", "field2"}, expression = "...")`
  - Support Java expressions for complex transformations
  - Example: `firstName + lastName -> fullName`
- **One-to-Many Conversion**: Support splitting one source field into multiple target fields
  - Multiple target fields can reference the same source field
  - Support expressions for field splitting
  - Example: `fullName -> firstName + lastName`

#### Type Converters (TypeConverter)
- **Built-in Converters**:
  - `NumberFormatter`: `Number` → `String` formatting (supports `DecimalFormat` patterns)
  - `NumberParser`: `String` → `Number` parsing
  - `DateFormatter`: `Date`/`LocalDate`/`LocalDateTime` → `String` formatting
  - `DateParser`: `String` → Date types parsing
  - `EnumStringConverter`: `Enum` ↔ `String`/`Integer` conversion
  - `JsonConverter`: Object ↔ JSON String conversion (requires Jackson)
- **Format Support**: Pass format strings via `@CopyField(converter = Xxx.class, format = "...")`
- **Custom Converters**: Import custom converters via `@CopyTarget(uses = {CustomConverter.class})`

#### Expression Mapping
- Support Java expressions for complex field transformations
- `source` variable in expressions represents the source object
- Support method calls, chained calls, stream operations, ternary operators, etc.
- Compile-time type checking and error reporting

#### Dependency Injection Support
- **ComponentModel Enum**:
  - `DEFAULT`: No DI, generates static methods
  - `SPRING`: Spring Framework, generates `@Component` annotation
  - `CDI`: CDI Framework, generates `@ApplicationScoped` annotation
  - `JSR330`: JSR-330 Standard, generates `@Named` + `@Singleton` annotations
- **Constructor Injection**: TypeConverters and custom converters injected via constructor
- **Field Immutability**: Fields use `final` modifier in DI modes
- **Backward Compatibility**: Provides no-arg constructor for cases without registered beans

#### Functional Customization
- New overloaded methods with `UnaryOperator` parameter
- Support executing custom logic immediately after copying
- Method signature: `toDto(source, UnaryOperator<DTO> customizer)`
- Collection methods also provide overloads: `toDtoList(sources, customizer)`, etc.
- Null-safe: Function not called when source object is null

#### Annotation Extensions
- **@CopyTarget Extensions**:
  - New `uses` attribute: Custom converter class list
  - New `componentModel` attribute: DI framework selection
- **@CopyField Annotation**:
  - `source[]`: Source field name array (for many-to-one)
  - `target`: Target field name
  - `expression`: Java expression
  - `qualifiedByName`: Named conversion method
  - `converter`: TypeConverter implementation class
  - `format`: Format string

#### Testing & Coverage
- New `OneToManyMappingTest`: One-to-many mapping tests
- New `FormattingTest`: Formatting converter tests
- New `ComponentModelTest`: DI mode tests
- Examples module maintains 93%+ instruction coverage

### Improved
- TypeConverter uses different injection strategies based on componentModel
- DEFAULT mode uses static instances, SPRING/CDI/JSR330 modes use constructor injection
- Expression parser supports more complex Java expression syntax

### Compatibility
- Java 8+, Maven build
- Maintains zero runtime reflection overhead
- Fully backward compatible with v1.1

### Verification
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.1.0] - 2025-12-23

### Added

#### Collection & Array Deep Copy
- Deep copy for List/Set/Map/array fields with bidirectional support (toDto/fromDto)
- Nested combinations: `List<List<T>>`, `Map<K, List<V>>`, `List<Map<K,V>>`, multi-dimensional arrays
- Null-safe handling for collections and elements; Map null values are preserved
- Raw types or unbounded wildcards automatically downgrade to shallow copy with compile-time warnings

#### Utilities & Code Generation
- TypeUtils: collection type detection, generic extraction, array component extraction, deep-copy decision
- CodeGenerator: capacity-preallocated deep-copy code for List/Set/Map/arrays, including reverse copy
- Recursive depth handling to avoid infinite recursion

#### Testing & Coverage
- New tests for collection deep copy, reverse copy, nested collections, raw/wildcard collections
- `PojoCoverageTest` covers all sample beans and generated Copiers; examples module reaches 93%+ instruction coverage
- Performance and integration tests now include collection scenarios

#### Documentation
- Updated Getting Started, Reference, and FAQ with collection/array deep copy, reverse copy, wildcard downgrade notes and examples
- JavaDoc generation enabled

### Improved
- Preallocated capacities and safer loop typing to reduce boxing and unsafe casts
- Collection/Map generation falls back to safe assignment with warnings when generics are missing or unsupported

### Compatibility
- Java 8+, Maven build; still zero runtime reflection overhead

### Verification
- `mvn clean install`
- `mvn jacoco:report`
- `mvn javadoc:javadoc`

## [1.0.0] - 2025-12-13

### Added

#### Core Features
- **APT Compile-time Code Generation**
  - Automatic generation of Copier classes using JavaPoet
  - Zero runtime reflection overhead
  - Compile-time type checking

- **Automatic Same-Name Field Copying**
  - Support for both primitive and object types
  - Uses getter/setter methods for field access
  - Handles private fields seamlessly

- **Primitive ↔ Wrapper Type Conversion**
  - Automatic boxing/unboxing support
  - Safe null value handling
  - Supports all 8 primitive types (byte, short, int, long, float, double, char, boolean)

- **Field Ignoring**
  - `@CopyTarget` annotation with `ignore` attribute
  - Flexible field exclusion mechanism
  - Easy to exclude sensitive or unnecessary fields

- **List/Set Collection Copying**
  - `toDtoList()` method for converting List collections to DTOs
  - `toDtoSet()` method for converting Set collections to DTOs
  - `fromDtoList()` method for reverse List conversion
  - `fromDtoSet()` method for reverse Set conversion
  - Proper null element handling in collections

- **Bidirectional Copying**
  - `toDto()` method for entity to DTO conversion
  - `fromDto()` method for DTO to entity conversion
  - Complete two-way transformation support

- **Nested Object Support**
  - Support for nested object fields
  - Safe null value handling for nested objects
  - Direct copying of same-name fields in nested objects

#### Testing & Quality
- **Comprehensive Unit Test Coverage**
  - 21 test cases covering all major features
  - 100% test coverage
  - Test classes:
    - `SameNameFieldCopyTest` (5 tests)
    - `PrimitiveWrapperConversionTest` (4 tests)
    - `FieldIgnoreTest` (3 tests)
    - `CollectionCopyTest` (5 tests)
    - `NestedObjectCopyTest` (4 tests)
    - `TypeUtilsTest` (5 tests)
  - All tests passing ✅

#### Documentation
- **Complete Documentation Suite**
  - Reference documentation (MapStruct-style format)
  - Getting started guide (5-minute quick start)
  - API documentation
  - FAQ section
  - Project summary
  - Code examples and usage patterns

#### Technical Infrastructure
- **Maven Module Structure**
  - `fast-bean-copier-annotations` - Annotation definitions module
  - `fast-bean-copier-processor` - APT processor module
  - `fast-bean-copier-examples` - Examples and test cases module

- **Dependencies**
  - JavaPoet 1.13.0 for code generation
  - Google Auto Service 1.0.1 for APT auto-registration
  - JUnit 4.13.2 for unit testing

### Technical Details

- **Java Version**: 8+
- **Build Tool**: Maven
- **License**: Apache License 2.0
- **Performance**: Zero runtime overhead, identical to hand-written code
- **Code Generation**: ~2KB per Copier class
- **Thread Safety**: Generated code is stateless and thread-safe

### Statistics

- **Source Code**: ~2000 lines
- **Test Code**: ~1000 lines
- **Documentation**: ~3000 lines
- **Total Code**: ~6000 lines
- **Test Cases**: 21
- **Documentation Files**: 5

---

[1.3.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.3.0
[1.2.1]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.2.1
[1.2.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.2.0
[1.1.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.1.0
[1.0.0]: https://github.com/fast-bean-copier/fast-bean-copier/releases/tag/v1.0.0
