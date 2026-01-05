package com.github.jackieonway.copier.processor.context;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.processor.FieldMapping;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理器上下文，封装处理过程中的共享状态和工具。
 *
 * <p>该类作为各组件之间的数据传递桥梁，提供：
 * <ul>
 *   <li>对 ProcessingEnvironment 及其工具类的访问</li>
 *   <li>当前处理的源类型和目标类型信息</li>
 *   <li>字段映射列表</li>
 *   <li>uses 类列表和 componentModel 配置</li>
 *   <li>统一的消息输出接口</li>
 * </ul>
 *
 * @author jackieonway
 * @since 2.0.0
 */
public class ProcessorContext {

    /**
     * 处理环境。
     */
    private final ProcessingEnvironment processingEnv;

    /**
     * 元素工具。
     */
    private final Elements elementUtils;

    /**
     * 类型工具。
     */
    private final Types typeUtils;

    /**
     * 消息工具。
     */
    private final Messager messager;

    // ========== 当前处理的类型信息 ==========

    /**
     * 源类型。
     */
    private TypeElement sourceType;

    /**
     * 目标类型。
     */
    private TypeElement targetType;

    // ========== 配置信息 ==========

    /**
     * 忽略的字段名集合。
     */
    private Set<String> ignoreFields = new HashSet<>();

    /**
     * uses 类的 TypeMirror 列表。
     */
    private List<TypeMirror> usesClasses = new ArrayList<>();

    /**
     * 组件模型配置。
     */
    private ComponentModel componentModel = ComponentModel.DEFAULT;

    // ========== 分析结果 ==========

    /**
     * 字段映射列表。
     */
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /**
     * 构造方法。
     *
     * @param processingEnv 处理环境
     */
    public ProcessorContext(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
    }

    // ========== 工具类访问方法 ==========

    /**
     * 获取处理环境。
     *
     * @return 处理环境
     */
    public ProcessingEnvironment getProcessingEnv() {
        return processingEnv;
    }

    /**
     * 获取元素工具。
     *
     * @return 元素工具
     */
    public Elements getElementUtils() {
        return elementUtils;
    }

    /**
     * 获取类型工具。
     *
     * @return 类型工具
     */
    public Types getTypeUtils() {
        return typeUtils;
    }

    /**
     * 获取消息工具。
     *
     * @return 消息工具
     */
    public Messager getMessager() {
        return messager;
    }

    // ========== 类型信息访问方法 ==========

    /**
     * 获取源类型。
     *
     * @return 源类型
     */
    public TypeElement getSourceType() {
        return sourceType;
    }

    /**
     * 设置源类型。
     *
     * @param sourceType 源类型
     */
    public void setSourceType(TypeElement sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * 获取目标类型。
     *
     * @return 目标类型
     */
    public TypeElement getTargetType() {
        return targetType;
    }

    /**
     * 设置目标类型。
     *
     * @param targetType 目标类型
     */
    public void setTargetType(TypeElement targetType) {
        this.targetType = targetType;
    }

    // ========== 配置信息访问方法 ==========

    /**
     * 获取忽略的字段名集合。
     *
     * @return 忽略的字段名集合（不可修改）
     */
    public Set<String> getIgnoreFields() {
        return Collections.unmodifiableSet(ignoreFields);
    }

    /**
     * 设置忽略的字段名集合。
     *
     * @param ignoreFields 忽略的字段名集合
     */
    public void setIgnoreFields(Set<String> ignoreFields) {
        this.ignoreFields = ignoreFields != null ? new HashSet<>(ignoreFields) : new HashSet<>();
    }

    /**
     * 获取 uses 类列表。
     *
     * @return uses 类的 TypeMirror 列表（不可修改）
     */
    public List<TypeMirror> getUsesClasses() {
        return Collections.unmodifiableList(usesClasses);
    }

    /**
     * 设置 uses 类列表。
     *
     * @param usesClasses uses 类的 TypeMirror 列表
     */
    public void setUsesClasses(List<TypeMirror> usesClasses) {
        this.usesClasses = usesClasses != null ? new ArrayList<>(usesClasses) : new ArrayList<>();
    }

    /**
     * 获取组件模型配置。
     *
     * @return 组件模型配置
     */
    public ComponentModel getComponentModel() {
        return componentModel;
    }

    /**
     * 设置组件模型配置。
     *
     * @param componentModel 组件模型配置
     */
    public void setComponentModel(ComponentModel componentModel) {
        this.componentModel = componentModel != null ? componentModel : ComponentModel.DEFAULT;
    }

    // ========== 分析结果访问方法 ==========

    /**
     * 获取字段映射列表。
     *
     * @return 字段映射列表（不可修改）
     */
    public List<FieldMapping> getFieldMappings() {
        return Collections.unmodifiableList(fieldMappings);
    }

    /**
     * 设置字段映射列表。
     *
     * @param fieldMappings 字段映射列表
     */
    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings != null ? new ArrayList<>(fieldMappings) : new ArrayList<>();
    }

    // ========== 消息输出方法 ==========

    /**
     * 输出错误消息。
     *
     * @param message 消息内容
     * @param element 相关元素
     */
    public void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * 输出错误消息（无关联元素）。
     *
     * @param message 消息内容
     */
    public void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    /**
     * 输出警告消息。
     *
     * @param message 消息内容
     * @param element 相关元素
     */
    public void warning(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    /**
     * 输出警告消息（无关联元素）。
     *
     * @param message 消息内容
     */
    public void warning(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message);
    }

    /**
     * 输出提示消息。
     *
     * @param message 消息内容
     */
    public void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }

    /**
     * 输出提示消息。
     *
     * @param message 消息内容
     * @param element 相关元素
     */
    public void note(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    // ========== 辅助方法 ==========

    /**
     * 判断是否使用静态方法模式。
     *
     * @return 如果是 DEFAULT 模式返回 true
     */
    public boolean isStaticMode() {
        return componentModel == ComponentModel.DEFAULT;
    }

    /**
     * 判断是否使用 Spring 模式。
     *
     * @return 如果是 SPRING 模式返回 true
     */
    public boolean isSpringMode() {
        return componentModel == ComponentModel.SPRING;
    }

    /**
     * 判断是否使用 CDI 模式。
     *
     * @return 如果是 CDI 模式返回 true
     */
    public boolean isCdiMode() {
        return componentModel == ComponentModel.CDI;
    }

    /**
     * 判断是否使用 JSR330 模式。
     *
     * @return 如果是 JSR330 模式返回 true
     */
    public boolean isJsr330Mode() {
        return componentModel == ComponentModel.JSR330;
    }

    /**
     * 重置上下文状态，准备处理下一个类。
     */
    public void reset() {
        this.sourceType = null;
        this.targetType = null;
        this.ignoreFields = new HashSet<>();
        this.usesClasses = new ArrayList<>();
        this.componentModel = ComponentModel.DEFAULT;
        this.fieldMappings = new ArrayList<>();
    }

    /**
     * 获取源类型的简单名称。
     *
     * @return 源类型的简单名称，如果未设置返回 null
     */
    public String getSourceTypeName() {
        return sourceType != null ? sourceType.getSimpleName().toString() : null;
    }

    /**
     * 获取目标类型的简单名称。
     *
     * @return 目标类型的简单名称，如果未设置返回 null
     */
    public String getTargetTypeName() {
        return targetType != null ? targetType.getSimpleName().toString() : null;
    }

    /**
     * 获取源类型的完全限定名。
     *
     * @return 源类型的完全限定名，如果未设置返回 null
     */
    public String getSourceTypeQualifiedName() {
        return sourceType != null ? sourceType.getQualifiedName().toString() : null;
    }

    /**
     * 获取目标类型的完全限定名。
     *
     * @return 目标类型的完全限定名，如果未设置返回 null
     */
    public String getTargetTypeQualifiedName() {
        return targetType != null ? targetType.getQualifiedName().toString() : null;
    }
}
