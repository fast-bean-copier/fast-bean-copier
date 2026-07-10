package com.github.jackieonway.copier.processor.config;

/**
 * 配置合并器。
 *
 * <p>用于合并不同来源的配置，按照优先级规则进行配置覆盖。
 *
 * <p>配置优先级（从高到低）：
 * <ol>
 *   <li>类级别配置 - {@code @CopyTarget} 注解中的配置</li>
 *   <li>包级别配置 - {@code @CopyTargetConfig} 注解中的配置</li>
 *   <li>配置文件配置 - {@code fast-bean-copier.properties} 中的配置</li>
 *   <li>默认值 - 系统默认配置</li>
 * </ol>
 *
 * <p>合并规则：
 * <ul>
 *   <li>高优先级的非空配置会覆盖低优先级的配置</li>
 *   <li>如果高优先级配置为 null 或空，则使用低优先级配置</li>
 *   <li>支持部分配置覆盖（例如只覆盖 componentModel，保留其他配置）</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.3.1
 */
public class ConfigMerger {

    /**
     * 默认组件模型。
     */
    private static final String DEFAULT_COMPONENT_MODEL = "DEFAULT";

    /**
     * 默认空值策略。
     */
    private static final String DEFAULT_NULL_VALUE_STRATEGY = "IGNORE";

    private static final String DEFAULT_CYCLE_DETECTION = "FAIL_FAST";

    /**
     * 合并组件模型配置。
     *
     * <p>按照优先级合并配置：类级别 > 包级别 > 配置文件 > 默认值。
     *
     * @param classLevel 类级别配置
     * @param packageLevel 包级别配置
     * @param fileLevel 配置文件配置
     * @return 合并后的组件模型，不会返回 null
     */
    public String mergeComponentModel(String classLevel, String packageLevel, String fileLevel) {
        // 类级别配置优先级最高
        if (isValidConfig(classLevel)) {
            return classLevel;
        }

        // 包级别配置次之
        if (isValidConfig(packageLevel)) {
            return packageLevel;
        }

        // 配置文件配置
        if (isValidConfig(fileLevel)) {
            return fileLevel;
        }

        // 使用默认值
        return DEFAULT_COMPONENT_MODEL;
    }

    /**
     * 合并空值策略配置。
     *
     * <p>按照优先级合并配置：类级别 > 包级别 > 配置文件 > 默认值。
     *
     * @param classLevel 类级别配置
     * @param packageLevel 包级别配置
     * @param fileLevel 配置文件配置
     * @return 合并后的空值策略，不会返回 null
     */
    public String mergeNullValueStrategy(String classLevel, String packageLevel, String fileLevel) {
        // 类级别配置优先级最高
        if (isValidConfig(classLevel)) {
            return classLevel;
        }

        // 包级别配置次之
        if (isValidConfig(packageLevel)) {
            return packageLevel;
        }

        // 配置文件配置
        if (isValidConfig(fileLevel)) {
            return fileLevel;
        }

        // 使用默认值
        return DEFAULT_NULL_VALUE_STRATEGY;
    }

    /**
     * 合并循环检测策略配置。
     *
     * @param classLevel 类级别配置
     * @param packageLevel 包级别配置
     * @param fileLevel 配置文件配置
     * @return 合并后的循环检测策略
     * @since 1.6.0
     */
    public String mergeCycleDetection(String classLevel, String packageLevel, String fileLevel) {
        if (isValidConfig(classLevel)) {
            return classLevel;
        }
        if (isValidConfig(packageLevel)) {
            return packageLevel;
        }
        if (isValidConfig(fileLevel)) {
            return fileLevel;
        }
        return DEFAULT_CYCLE_DETECTION;
    }

    /**
     * 判断配置是否有效（非 null 且非空字符串）。
     *
     * @param config 配置值
     * @return 如果配置有效返回 true，否则返回 false
     */
    private boolean isValidConfig(String config) {
        return config != null && !config.trim().isEmpty();
    }
}
