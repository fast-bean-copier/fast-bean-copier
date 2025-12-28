package com.github.jackieonway.copier.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JSON 转换器，用于对象与 JSON 字符串之间的转换。
 *
 * <p>使用 Jackson 进行序列化和反序列化。
 *
 * <h3>使用示例</h3>
 * <pre>
 * // 对象转 JSON 字符串
 * &#64;CopyField(converter = JsonConverter.class)
 * private String addressJson;
 *
 * // JSON 字符串转对象（需要在 format 中指定目标类全限定名）
 * &#64;CopyField(converter = JsonConverter.class, format = "com.example.Address")
 * private Address address;
 * </pre>
 *
 * <p><b>注意：</b>使用此转换器需要在项目中添加 Jackson 依赖。
 *
 * @author jackieonway
 * @since 1.2.0
 * @see TypeConverter
 * @see ObjectMapper
 */
public class JsonConverter implements TypeConverter<Object, Object> {

    /**
     * 共享的 ObjectMapper 实例。
     * ObjectMapper 是线程安全的，可以共享使用。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 转换对象与 JSON 字符串。
     *
     * <p>转换规则：
     * <ul>
     *   <li>如果源是字符串且 format 指定了目标类，将 JSON 反序列化为对象</li>
     *   <li>否则，将对象序列化为 JSON 字符串</li>
     * </ul>
     *
     * @param source 源对象
     * @param format 目标类的全限定名（用于 JSON 反序列化）
     * @return 转换后的对象（JSON 字符串或反序列化后的对象）
     * @throws RuntimeException 如果转换失败
     */
    @Override
    public Object convert(Object source, String format) {
        if (source == null) {
            return null;
        }

        // 如果源是字符串且指定了目标类，进行反序列化
        if (source instanceof String && format != null && !format.isEmpty()) {
            return fromJson((String) source, format);
        }

        // 否则序列化为 JSON 字符串
        return toJson(source);
    }

    /**
     * 将对象序列化为 JSON 字符串。
     *
     * @param source 源对象
     * @return JSON 字符串
     * @throws RuntimeException 如果序列化失败
     */
    public String toJson(Object source) {
        if (source == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为对象。
     *
     * @param json JSON 字符串
     * @param targetClassName 目标类的全限定名
     * @return 反序列化后的对象
     * @throws RuntimeException 如果反序列化失败
     */
    public Object fromJson(String json, String targetClassName) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            Class<?> targetClass = Class.forName(targetClassName);
            return OBJECT_MAPPER.readValue(json, targetClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Target class not found: " + targetClassName, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * 将 JSON 字符串反序列化为指定类型的对象。
     *
     * @param json JSON 字符串
     * @param targetClass 目标类
     * @param <T> 目标类型
     * @return 反序列化后的对象
     * @throws RuntimeException 如果反序列化失败
     */
    public <T> T fromJson(String json, Class<T> targetClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + targetClass.getName(), e);
        }
    }
}
