package com.github.jackieonway.copier.converter;

/**
 * 枚举与字符串/整数转换器。
 *
 * <p>支持以下转换：
 * <ul>
 *   <li>Enum → String：使用 {@link Enum#name()}</li>
 *   <li>Enum → Integer：使用 {@link Enum#ordinal()}</li>
 *   <li>String → Enum：通过 name 查找</li>
 *   <li>Integer → Enum：通过 ordinal 查找</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 枚举转字符串
 * &#64;CopyField(converter = EnumStringConverter.class)
 * private String statusStr;
 *
 * // 字符串转枚举（需要在 format 中指定枚举类全限定名）
 * &#64;CopyField(converter = EnumStringConverter.class, format = "com.example.Status")
 * private Status status;
 * </pre>
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 */
public class EnumStringConverter implements TypeConverter<Object, Object> {

    /**
     * 转换枚举与字符串/整数。
     *
     * <p>转换规则：
     * <ul>
     *   <li>如果源是枚举，转换为字符串（name）</li>
     *   <li>如果源是字符串且 format 指定了枚举类，转换为枚举</li>
     *   <li>如果源是整数且 format 指定了枚举类，通过 ordinal 转换为枚举</li>
     * </ul>
     *
     * @param source 源对象（Enum、String 或 Integer）
     * @param format 枚举类的全限定名（用于字符串/整数转枚举）
     * @return 转换后的对象
     */
    @Override
    public Object convert(Object source, String format) {
        if (source == null) {
            return null;
        }

        // 枚举转字符串
        if (source instanceof Enum) {
            return ((Enum<?>) source).name();
        }

        // 字符串或整数转枚举
        if (format != null && !format.isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) Class.forName(format);
                
                if (source instanceof String) {
                    return stringToEnum((String) source, enumClass);
                } else if (source instanceof Integer) {
                    return ordinalToEnum((Integer) source, enumClass);
                } else if (source instanceof Number) {
                    return ordinalToEnum(((Number) source).intValue(), enumClass);
                }
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Enum class not found: " + format, e);
            }
        }

        throw new IllegalArgumentException("Cannot convert " + source.getClass().getName() + 
                " without specifying enum class in format parameter");
    }

    /**
     * 将枚举转换为字符串（name）。
     *
     * @param source 源枚举
     * @return 枚举的 name
     */
    public String enumToString(Enum<?> source) {
        if (source == null) {
            return null;
        }
        return source.name();
    }

    /**
     * 将枚举转换为整数（ordinal）。
     *
     * @param source 源枚举
     * @return 枚举的 ordinal
     */
    public Integer enumToOrdinal(Enum<?> source) {
        if (source == null) {
            return null;
        }
        return source.ordinal();
    }

    /**
     * 将字符串转换为枚举。
     *
     * @param source 源字符串（枚举的 name）
     * @param enumClass 目标枚举类
     * @param <E> 枚举类型
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果找不到对应的枚举值
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends Enum<E>> E stringToEnum(String source, Class<? extends Enum<?>> enumClass) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        Class rawClass = enumClass;
        return (E) Enum.valueOf(rawClass, source.trim());
    }

    /**
     * 将整数（ordinal）转换为枚举。
     *
     * @param ordinal 枚举的 ordinal 值
     * @param enumClass 目标枚举类
     * @param <E> 枚举类型
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果 ordinal 超出范围
     */
    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> E ordinalToEnum(int ordinal, Class<? extends Enum<?>> enumClass) {
        Enum<?>[] constants = enumClass.getEnumConstants();
        if (ordinal < 0 || ordinal >= constants.length) {
            throw new IllegalArgumentException("Invalid ordinal " + ordinal + " for enum " + enumClass.getName());
        }
        return (E) constants[ordinal];
    }
}
