package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.CopyTarget;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.Set;

/**
 * Bean 拷贝代码生成的 APT 处理器。
 *
 * 该处理器会扫描所有被 @CopyTarget 注解标记的类，
 * 并自动生成对应的 Copier 类，包含 toDto、fromDto 等方法。
 *
 * @author jackieonway
 * @since 1.0.0
 */
@AutoService(Processor.class)
public class BeanCopierProcessor extends AbstractProcessor {

    /**
     * 处理环境，用于访问编译时的各种信息。
     */
    private ProcessingEnvironment processingEnv;

    /**
     * 初始化处理器。
     *
     * @param processingEnv 处理环境
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }

    /**
     * 返回该处理器支持的注解类型。
     *
     * @return 支持的注解类型集合
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(CopyTarget.class.getCanonicalName());
    }

    /**
     * 返回该处理器支持的源代码版本。
     *
     * @return 支持的源代码版本
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    /**
     * 处理注解。
     *
     * @param annotations 在此轮中要处理的注解类型
     * @param roundEnv    有关当前和上一轮的信息
     * @return 如果此处理器处理了这些注解，则返回 true；否则返回 false
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 暂时只返回 false，后续实现具体的处理逻辑
        return false;
    }
}
