package com.github.jackieonway.copier.processor.config;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties 配置文件读取器。
 *
 * <p>用于在编译期读取 {@code fast-bean-copier.properties} 配置文件，
 * 支持全局配置项的读取和解析。
 *
 * <p>配置文件查找路径（按优先级）：
 * <ol>
 *   <li>{@code fast-bean-copier.properties} - 项目根目录</li>
 *   <li>{@code META-INF/fast-bean-copier.properties} - META-INF 目录</li>
 * </ol>
 *
 * <p>支持的配置项：
 * <ul>
 *   <li>{@code fast.bean.copier.componentModel} - 组件模型（DEFAULT/SPRING/CDI/JSR330）</li>
 *   <li>{@code fast.bean.copier.nullValueStrategy} - 空值策略（IGNORE/SET_NULL/THROW_EXCEPTION）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class PropertiesConfigLoader {

    /**
     * 主配置文件路径。
     */
    private static final String PRIMARY_CONFIG_PATH = "fast-bean-copier.properties";

    /**
     * 备选配置文件路径。
     */
    private static final String FALLBACK_CONFIG_PATH = "META-INF/fast-bean-copier.properties";

    /**
     * 组件模型配置键。
     */
    private static final String KEY_COMPONENT_MODEL = "fast.bean.copier.componentModel";

    /**
     * 空值策略配置键。
     */
    private static final String KEY_NULL_VALUE_STRATEGY = "fast.bean.copier.nullValueStrategy";

    private static final String KEY_CYCLE_DETECTION = "fast.bean.copier.cycleDetection";

    /**
     * Filer 用于读取资源文件。
     */
    private final Filer filer;

    /**
     * 构造方法。
     *
     * @param filer Filer 实例
     */
    public PropertiesConfigLoader(Filer filer) {
        this.filer = filer;
    }

    /**
     * 加载配置文件。
     *
     * <p>按照优先级尝试读取配置文件：
     * <ol>
     *   <li>主路径：{@code fast-bean-copier.properties}</li>
     *   <li>备选路径：{@code META-INF/fast-bean-copier.properties}</li>
     * </ol>
     *
     * <p>如果所有路径都不存在，返回空的 Properties 对象。
     *
     * @return Properties 对象，如果文件不存在则返回空对象
     */
    public Properties loadConfig() {
        Properties properties = new Properties();

        // 尝试主路径
        if (tryLoadFromPath(PRIMARY_CONFIG_PATH, properties)) {
            return properties;
        }

        // 尝试备选路径
        if (tryLoadFromPath(FALLBACK_CONFIG_PATH, properties)) {
            return properties;
        }

        // 所有路径都不存在，返回空配置
        return properties;
    }

    /**
     * 尝试从指定路径加载配置文件。
     *
     * @param path 配置文件路径
     * @param properties Properties 对象
     * @return 如果成功加载返回 true，否则返回 false
     */
    private boolean tryLoadFromPath(String path, Properties properties) {
        try {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
            try (InputStream inputStream = resource.openInputStream()) {
                properties.load(inputStream);
                return true;
            }
        } catch (IOException e) {
            // 文件不存在或读取失败，继续尝试其他路径
            return false;
        }
    }

    /**
     * 解析组件模型配置。
     *
     * <p>从 Properties 中读取 {@code fast.bean.copier.componentModel} 配置项。
     *
     * <p>有效值：
     * <ul>
     *   <li>DEFAULT - 默认模式（无依赖注入）</li>
     *   <li>SPRING - Spring 框架</li>
     *   <li>CDI - CDI 容器</li>
     *   <li>JSR330 - JSR-330 标准</li>
     * </ul>
     *
     * @param props Properties 对象
     * @return 组件模型字符串，如果未配置或无效则返回 null
     */
    public String parseComponentModel(Properties props) {
        String value = props.getProperty(KEY_COMPONENT_MODEL);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim().toUpperCase();

        // 验证配置值
        if ("DEFAULT".equals(value) || "SPRING".equals(value) || 
            "CDI".equals(value) || "JSR330".equals(value)) {
            return value;
        }

        // 无效的配置值
        System.err.println("警告: 无效的 componentModel 配置值: " + value + 
                         "，有效值为: DEFAULT, SPRING, CDI, JSR330");
        return null;
    }

    /**
     * 解析空值策略配置。
     *
     * <p>从 Properties 中读取 {@code fast.bean.copier.nullValueStrategy} 配置项。
     *
     * <p>有效值：
     * <ul>
     *   <li>IGNORE - 忽略 null 值（默认）</li>
     *   <li>SET_NULL - 设置为 null</li>
     *   <li>THROW_EXCEPTION - 抛出异常</li>
     * </ul>
     *
     * @param props Properties 对象
     * @return 空值策略字符串，如果未配置或无效则返回 null
     */
    public String parseNullValueStrategy(Properties props) {
        String value = props.getProperty(KEY_NULL_VALUE_STRATEGY);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim().toUpperCase();

        // 验证配置值
        if ("IGNORE".equals(value) || "SET_NULL".equals(value) || 
            "THROW_EXCEPTION".equals(value)) {
            return value;
        }

        // 无效的配置值
        System.err.println("警告: 无效的 nullValueStrategy 配置值: " + value + 
                         "，有效值为: IGNORE, SET_NULL, THROW_EXCEPTION");
        return null;
    }

    /**
     * 解析循环检测策略配置。
     *
     * @param props Properties 对象
     * @return 循环检测策略字符串，如果未配置或无效则返回 null
     * @since 1.6.0
     */
    public String parseCycleDetection(Properties props) {
        String value = props.getProperty(KEY_CYCLE_DETECTION);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim().toUpperCase();

        if ("FAIL_FAST".equals(value) || "RETURN_NULL".equals(value)
                || "AUTOMATIC_CACHE".equals(value)) {
            return value;
        }

        System.err.println("Warning: invalid cycleDetection configuration value: " + value
                + ". Supported values: FAIL_FAST, RETURN_NULL, AUTOMATIC_CACHE");
        return null;
    }
}
