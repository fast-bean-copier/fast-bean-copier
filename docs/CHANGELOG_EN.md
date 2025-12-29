# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.2.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.2.0
[1.1.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.1.0
[1.0.0]: https://github.com/jackieonway/fast-bean-copier/releases/tag/v1.0.0
