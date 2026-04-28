package com.github.jackieonway.copier.processor;

import com.google.auto.service.AutoService;
import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.CopyField;
import com.github.jackieonway.copier.annotation.CopyFromMap;
import com.github.jackieonway.copier.annotation.CopyToMap;
import com.github.jackieonway.copier.annotation.MapKeyStrategy;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.processor.generator.MapCodeGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean ↔ Map 转换代码生成的 APT 处理器。
 *
 * <p>该处理器扫描所有被 {@link CopyToMap} 或 {@link CopyFromMap} 注解标记的类，
 * 并自动生成对应的 MapCopier 类，包含 toMap、fromMap 等方法。
 *
 * <p>同一个类可以同时标注两个注解，生成双向转换方法。
 * 与 {@link BeanCopierProcessor} 独立运行，互不影响。
 *
 * @author jackieonway
 * @since 1.5.0
 */
@AutoService(Processor.class)
public class MapCopierProcessor extends AbstractProcessor {

    /** 处理器上下文 */
    private ProcessorContext context;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.context = new ProcessorContext(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(CopyToMap.class.getCanonicalName());
        types.add(CopyFromMap.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 收集所有需要处理的类（去重）
        Set<TypeElement> toProcess = new LinkedHashSet<>();
        for (Element e : roundEnv.getElementsAnnotatedWith(CopyToMap.class)) {
            if (e instanceof TypeElement) toProcess.add((TypeElement) e);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(CopyFromMap.class)) {
            if (e instanceof TypeElement) toProcess.add((TypeElement) e);
        }

        for (TypeElement typeElement : toProcess) {
            processType(typeElement);
        }
        return true;
    }

    /**
     * 处理单个类型，生成 MapCopier 类。
     *
     * @param typeElement 目标类型元素
     */
    private void processType(TypeElement typeElement) {
        CopyToMap toMapAnnotation = typeElement.getAnnotation(CopyToMap.class);
        CopyFromMap fromMapAnnotation = typeElement.getAnnotation(CopyFromMap.class);

        // 提取配置
        MapConfig toMapConfig = toMapAnnotation != null ? extractToMapConfig(toMapAnnotation) : null;
        MapConfig fromMapConfig = fromMapAnnotation != null ? extractFromMapConfig(fromMapAnnotation) : null;

        // 分析字段
        List<MapFieldInfo> fields = analyzeFields(typeElement,
                toMapConfig != null ? toMapConfig.ignoreFields : new HashSet<>(),
                fromMapConfig != null ? fromMapConfig.ignoreFields : new HashSet<>());

        // 生成 MapCopier 类
        ComponentModel componentModel = resolveComponentModel(toMapConfig, fromMapConfig);
        MapCodeGenerator generator = new MapCodeGenerator(
                context.getProcessingEnv(), typeElement,
                toMapConfig, fromMapConfig, fields, componentModel);
        generator.generate();
    }

    /**
     * 从 @CopyToMap 提取配置。
     */
    private MapConfig extractToMapConfig(CopyToMap annotation) {
        Set<String> ignoreFields = new HashSet<>(Arrays.asList(annotation.ignore()));
        List<TypeMirror> usesClasses = extractUsesClasses(annotation);
        ComponentModel componentModel = annotation.componentModel();
        MapKeyStrategy keyStrategy = annotation.keyStrategy();
        return new MapConfig(ignoreFields, usesClasses, componentModel, keyStrategy);
    }

    /**
     * 从 @CopyFromMap 提取配置。
     */
    private MapConfig extractFromMapConfig(CopyFromMap annotation) {
        Set<String> ignoreFields = new HashSet<>(Arrays.asList(annotation.ignore()));
        List<TypeMirror> usesClasses = extractUsesClasses(annotation);
        ComponentModel componentModel = annotation.componentModel();
        MapKeyStrategy keyStrategy = annotation.keyStrategy();
        return new MapConfig(ignoreFields, usesClasses, componentModel, keyStrategy);
    }

    /**
     * 从 @CopyToMap 提取 uses 类列表。
     */
    private List<TypeMirror> extractUsesClasses(CopyToMap annotation) {
        try {
            annotation.uses();
            return new ArrayList<>();
        } catch (MirroredTypesException e) {
            return new ArrayList<>(e.getTypeMirrors());
        }
    }

    /**
     * 从 @CopyFromMap 提取 uses 类列表。
     */
    private List<TypeMirror> extractUsesClasses(CopyFromMap annotation) {
        try {
            annotation.uses();
            return new ArrayList<>();
        } catch (MirroredTypesException e) {
            return new ArrayList<>(e.getTypeMirrors());
        }
    }

    /**
     * 分析类型的字段，构建 MapFieldInfo 列表。
     *
     * @param typeElement     目标类型
     * @param toIgnoreFields  toMap 忽略字段
     * @param fromIgnoreFields fromMap 忽略字段
     * @return 字段信息列表
     */
    private List<MapFieldInfo> analyzeFields(TypeElement typeElement,
                                              Set<String> toIgnoreFields,
                                              Set<String> fromIgnoreFields) {
        List<VariableElement> allFields = TypeUtils.getAllFields(typeElement);
        List<MapFieldInfo> result = new ArrayList<>();

        for (VariableElement field : allFields) {
            String fieldName = field.getSimpleName().toString();
            CopyField copyField = field.getAnnotation(CopyField.class);

            String mapKey = "";
            if (copyField != null && copyField.mapKey() != null && !copyField.mapKey().isEmpty()) {
                mapKey = copyField.mapKey();
            }

            boolean inToMap = !toIgnoreFields.contains(fieldName);
            boolean inFromMap = !fromIgnoreFields.contains(fieldName);

            result.add(new MapFieldInfo(field, fieldName, mapKey, inToMap, inFromMap));
        }
        return result;
    }

    /**
     * 解析有效的 ComponentModel（toMap 优先，其次 fromMap）。
     */
    private ComponentModel resolveComponentModel(MapConfig toMapConfig, MapConfig fromMapConfig) {
        if (toMapConfig != null && toMapConfig.componentModel != ComponentModel.DEFAULT) {
            return toMapConfig.componentModel;
        }
        if (fromMapConfig != null && fromMapConfig.componentModel != ComponentModel.DEFAULT) {
            return fromMapConfig.componentModel;
        }
        return ComponentModel.DEFAULT;
    }

    // ========== 内部数据类 ==========

    /**
     * Map 注解配置数据类。
     */
    public static class MapConfig {
        public final Set<String> ignoreFields;
        public final List<TypeMirror> usesClasses;
        public final ComponentModel componentModel;
        public final MapKeyStrategy keyStrategy;

        public MapConfig(Set<String> ignoreFields, List<TypeMirror> usesClasses,
                         ComponentModel componentModel, MapKeyStrategy keyStrategy) {
            this.ignoreFields = ignoreFields;
            this.usesClasses = usesClasses;
            this.componentModel = componentModel != null ? componentModel : ComponentModel.DEFAULT;
            this.keyStrategy = keyStrategy != null ? keyStrategy : MapKeyStrategy.FIELD_NAME;
        }
    }

    /**
     * Map 字段信息数据类。
     */
    public static class MapFieldInfo {
        public final VariableElement field;
        public final String fieldName;
        public final String mapKey;
        public final boolean inToMap;
        public final boolean inFromMap;

        public MapFieldInfo(VariableElement field, String fieldName, String mapKey,
                            boolean inToMap, boolean inFromMap) {
            this.field = field;
            this.fieldName = fieldName;
            this.mapKey = mapKey;
            this.inToMap = inToMap;
            this.inFromMap = inFromMap;
        }

        /**
         * 根据 keyStrategy 确定实际使用的 Map key。
         *
         * @param keyStrategy key 命名策略
         * @return 实际 Map key
         */
        public String resolveMapKey(MapKeyStrategy keyStrategy) {
            // mapKey 优先级最高
            if (mapKey != null && !mapKey.isEmpty()) {
                return mapKey;
            }
            if (keyStrategy == MapKeyStrategy.SNAKE_CASE) {
                return toSnakeCase(fieldName);
            }
            // FIELD_NAME / CAMEL_CASE / CUSTOM（无 mapKey 时回退到字段名）
            return fieldName;
        }

        /**
         * 驼峰转下划线。
         */
        private String toSnakeCase(String camelCase) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < camelCase.length(); i++) {
                char c = camelCase.charAt(i);
                if (Character.isUpperCase(c) && i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            }
            return sb.toString();
        }
    }
}
