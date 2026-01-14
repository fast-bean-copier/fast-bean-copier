# Fast Bean Copier v1.3.0 Release Notes

**Release Date:** January 14, 2026

## What's New

### 1. Update Existing Objects

Update existing DTO/entity objects instead of creating new ones:

```java
@CopyTarget(source = User.class)
public class UserDto {
    private String name;
    private String email;
}

// Update existing object
UserCopier.updateDto(dto, user);
UserCopier.updateEntity(user, dto);
```

**Features:**
- `updateDto()` - Update DTO from entity
- `updateEntity()` - Update entity from DTO (reverse)
- Null-safe: source null → target unchanged
- Supports nested objects and collections
- Two strategies: IGNORE (default) or REPLACE null fields

### 2. Before Mapping Callback

Execute custom logic before mapping:

```java
@CopyTarget(source = User.class, beforeMapping = "validate")
public class UserDto {
    public default void validate(User source) {
        if (source == null) throw new IllegalArgumentException("Source required");
    }
}

UserDto dto = UserCopier.toDto(user);  // validate() called first
```

### 3. Conditional Mapping

Map fields based on conditions:

```java
@CopyTarget(source = User.class)
public class UserDto {
    @CopyField(condition = "java(source.getAge() >= 18)")
    private String adultStatus;
    
    @CopyField(condition = "java(source.getEmail() != null)")
    private String email;
}
```

### 4. Default Values & Constants

Set defaults when source is null, or use constants:

```java
@CopyTarget(source = User.class)
public class UserDto {
    @CopyField(defaultValue = "ACTIVE")
    private String status;
    
    @CopyField(constant = "v1.0")
    private String version;
}
```

**Supported types:** String, Integer, Long, Double, Float, Short, Byte, Boolean, BigDecimal, BigInteger

### 5. Global Configuration

Package-level configuration in `package-info.java`:

```java
@CopyTargetConfig(
    componentModel = ComponentModel.SPRING,
    nullValueStrategy = NullValueStrategy.REPLACE
)
package com.example.dto;
```

**Priority:** Class level > Package level > Default

---

## Testing & Quality

- **330+ test cases** - All v1.3 features covered
- **80%+ code coverage** - New components thoroughly tested
- **Backward compatible** - All v1.2 code works unchanged
- **Zero overhead** - Compile-time code generation, no reflection

---

## Quick Start

### Update Dependency

```xml
<dependency>
    <groupId>com.github.jackieonway</groupId>
    <artifactId>fast-bean-copier-annotations</artifactId>
    <version>1.3.0</version>
</dependency>
```

### Run Tests

```bash
cd fast-bean-copier
mvn install -DskipTests -q
cd fast-bean-copier-examples
mvn test
```

---

## Technical Details

- **Java**: 8+ (tested on 8, 11, 17, 21)
- **Build**: Maven 3.x+
- **Dependencies**: JavaPoet 1.13.0, Auto Service 1.0.1
- **Performance**: Zero runtime overhead, identical to hand-written code

---

## Documentation

- `docs/API.md` - API reference
- `docs/GETTING_STARTED.md` - Quick start guide
- `docs/REFERENCE.md` - Feature documentation
- `docs/FAQ.md` - Common questions
- `docs/CHANGELOG_EN.md` - Full changelog

---

## Verification

- ✅ All 330+ tests pass
- ✅ Code coverage > 80%
- ✅ Backward compatible with v1.2
- ✅ Performance benchmarks pass
- ✅ Maven build successful

---

**Version:** 1.3.0 | **License:** Apache 2.0 | **GitHub:** https://github.com/fast-bean-copier/fast-bean-copier
