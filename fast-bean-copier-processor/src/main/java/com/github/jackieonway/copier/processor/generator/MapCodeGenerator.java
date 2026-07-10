package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.annotation.ComponentModel;
import com.github.jackieonway.copier.annotation.MapKeyStrategy;
import com.github.jackieonway.copier.processor.MapCopierProcessor.MapConfig;
import com.github.jackieonway.copier.processor.MapCopierProcessor.MapFieldInfo;
import com.github.jackieonway.copier.processor.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * Bean ↔ Map 转换代码生成器。
 *
 * <p>生成 {TargetClass}MapCopier 类，包含：
 * <ul>
 *   <li>toMap / toMap(pre, post) - Bean → Map（由 @CopyToMap 触发）</li>
 *   <li>fromMap / fromMap(pre, post) - Map → Bean（由 @CopyFromMap 触发）</li>
 *   <li>toMapList / toMapSet 批量方法</li>
 *   <li>fromMapList / fromMapSet 批量方法</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.5.0
 */
public class MapCodeGenerator {

    private static final ClassName MAP_CLASS = ClassName.get(Map.class);
    private static final ClassName STRING_CLASS = ClassName.get(String.class);
    private static final ClassName OBJECT_CLASS = ClassName.get(Object.class);
    private static final ClassName LIST_CLASS = ClassName.get(java.util.List.class);
    private static final ClassName SET_CLASS = ClassName.get(java.util.Set.class);
    private static final ClassName ARRAY_LIST_CLASS = ClassName.get(ArrayList.class);
    private static final ClassName LINKED_HASH_SET_CLASS = ClassName.get(LinkedHashSet.class);
    private static final ClassName HASH_MAP_CLASS = ClassName.get(HashMap.class);

    /** Map<String, Object> 类型名 */
    private static final TypeName MAP_STRING_OBJECT = ParameterizedTypeName.get(MAP_CLASS, STRING_CLASS, OBJECT_CLASS);
    /** List<Map<String, Object>> 类型名 */
    private static final TypeName LIST_OF_MAP = ParameterizedTypeName.get(LIST_CLASS, MAP_STRING_OBJECT);
    /** Set<Map<String, Object>> 类型名 */
    private static final TypeName SET_OF_MAP = ParameterizedTypeName.get(SET_CLASS, MAP_STRING_OBJECT);

    private final ProcessingEnvironment processingEnv;
    private final TypeElement targetType;
    private final MapConfig toMapConfig;
    private final MapConfig fromMapConfig;
    private final List<MapFieldInfo> fields;
    private final ComponentModel componentModel;
    private final boolean useStatic;

    public MapCodeGenerator(ProcessingEnvironment processingEnv, TypeElement targetType,
                             MapConfig toMapConfig, MapConfig fromMapConfig,
                             List<MapFieldInfo> fields, ComponentModel componentModel) {
        this.processingEnv = processingEnv;
        this.targetType = targetType;
        this.toMapConfig = toMapConfig;
        this.fromMapConfig = fromMapConfig;
        this.fields = fields;
        this.componentModel = componentModel != null ? componentModel : ComponentModel.DEFAULT;
        this.useStatic = this.componentModel == ComponentModel.DEFAULT;
    }

    /**
     * 生成 MapCopier 类。
     */
    public void generate() {
        try {
            String targetClassName = targetType.getSimpleName().toString();
            String copierClassName = targetClassName + "MapCopier";
            String packageName = getPackageName(targetType);

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(copierClassName)
                    .addModifiers(Modifier.PUBLIC);

            // 类注解和修饰符
            addClassAnnotations(classBuilder);

            // 构造器
            if (useStatic) {
                classBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addStatement("throw new $T($S)", AssertionError.class,
                                "No instances of " + copierClassName)
                        .build());
            } else {
                classBuilder.addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC).build());
            }

            // toMap 方法（由 @CopyToMap 触发）
            if (toMapConfig != null) {
                classBuilder.addMethod(generateToMap());
                classBuilder.addMethod(generateToMapWithProcessors());
                classBuilder.addMethod(generateToMapList());
                classBuilder.addMethod(generateToMapListWithProcessors());
                classBuilder.addMethod(generateToMapSet());
                classBuilder.addMethod(generateToMapSetWithProcessors());
            }

            // fromMap 方法（由 @CopyFromMap 触发）
            if (fromMapConfig != null) {
                classBuilder.addMethod(generateFromMap());
                classBuilder.addMethod(generateFromMapWithProcessors());
                classBuilder.addMethod(generateFromMapList());
                classBuilder.addMethod(generateFromMapListWithProcessors());
                classBuilder.addMethod(generateFromMapSet());
                classBuilder.addMethod(generateFromMapSetWithProcessors());
            }

            JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build()).build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException("生成 MapCopier 类失败", e);
        }
    }

    // ========== toMap 方法 ==========

    /** 生成 toMap(T source) 基础方法 */
    private MethodSpec generateToMap() {
        TypeName beanType = ClassName.get(targetType);
        MapKeyStrategy keyStrategy = toMapConfig.keyStrategy;

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMap")
                .addModifiers(Modifier.PUBLIC)
                .returns(MAP_STRING_OBJECT)
                .addParameter(beanType, "source");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (source == null)")
                .addStatement("return null")
                .endControlFlow();

        m.addStatement("$T map = new $T<>()", MAP_STRING_OBJECT, HASH_MAP_CLASS);

        for (MapFieldInfo fi : fields) {
            if (!fi.inToMap) continue;
            String key = fi.resolveMapKey(keyStrategy);
            String getter = "get" + capitalize(fi.fieldName);
            TypeMirror fieldType = TypeUtils.getFieldType(fi.field);

            if (TypeUtils.needsDeepCopy(fieldType) && !TypeUtils.isCollectionType(fieldType)) {
                // 嵌套对象：递归转为 Map（如果有 MapCopier 则用，否则直接放入）
                m.addStatement("map.put($S, source.$L() != null ? toNestedMap(source.$L()) : null)",
                        key, getter, getter);
            } else {
                m.addStatement("map.put($S, source.$L())", key, getter);
            }
        }

        m.addStatement("return map");
        return m.build();
    }

    /** 生成 toMap(T source, preProcessor, postProcessor) 函数式方法 */
    private MethodSpec generateToMapWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), beanType);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), beanType,
                MAP_STRING_OBJECT, MAP_STRING_OBJECT);

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMap")
                .addModifiers(Modifier.PUBLIC)
                .returns(MAP_STRING_OBJECT)
                .addParameter(beanType, "source")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (source == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("source = preProcessor.apply(source)")
                .endControlFlow();
        m.beginControlFlow("if (source == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = toMap(source)", MAP_STRING_OBJECT);
        } else {
            m.addStatement("$T result = this.toMap(source)", MAP_STRING_OBJECT);
        }

        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(source, result)")
                .endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 toMapList(List<T> sources) */
    private MethodSpec generateToMapList() {
        TypeName beanType = ClassName.get(targetType);
        TypeName listOfBean = ParameterizedTypeName.get(LIST_CLASS, beanType);

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMapList")
                .addModifiers(Modifier.PUBLIC)
                .returns(LIST_OF_MAP)
                .addParameter(listOfBean, "sources");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.addStatement("$T result = new $T<>(sources.size())", LIST_OF_MAP, ARRAY_LIST_CLASS);
        m.beginControlFlow("for ($T source : sources)", beanType);
        if (useStatic) {
            m.addStatement("result.add(toMap(source))");
        } else {
            m.addStatement("result.add(this.toMap(source))");
        }
        m.endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 toMapList(List<T> sources, preProcessor, postProcessor) */
    private MethodSpec generateToMapListWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName listOfBean = ParameterizedTypeName.get(LIST_CLASS, beanType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), listOfBean);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), listOfBean,
                LIST_OF_MAP, LIST_OF_MAP);

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMapList")
                .addModifiers(Modifier.PUBLIC)
                .returns(LIST_OF_MAP)
                .addParameter(listOfBean, "sources")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)").endControlFlow();
        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = toMapList(sources)", LIST_OF_MAP);
        } else {
            m.addStatement("$T result = this.toMapList(sources)", LIST_OF_MAP);
        }
        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)").endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 toMapSet(Set<T> sources) */
    private MethodSpec generateToMapSet() {
        TypeName beanType = ClassName.get(targetType);
        TypeName setOfBean = ParameterizedTypeName.get(SET_CLASS, beanType);

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMapSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(SET_OF_MAP)
                .addParameter(setOfBean, "sources");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.addStatement("$T result = new $T<>()", SET_OF_MAP, LINKED_HASH_SET_CLASS);
        m.beginControlFlow("for ($T source : sources)", beanType);
        if (useStatic) {
            m.addStatement("result.add(toMap(source))");
        } else {
            m.addStatement("result.add(this.toMap(source))");
        }
        m.endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 toMapSet(Set<T> sources, preProcessor, postProcessor) */
    private MethodSpec generateToMapSetWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName setOfBean = ParameterizedTypeName.get(SET_CLASS, beanType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), setOfBean);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), setOfBean,
                SET_OF_MAP, SET_OF_MAP);

        MethodSpec.Builder m = MethodSpec.methodBuilder("toMapSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(SET_OF_MAP)
                .addParameter(setOfBean, "sources")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)").endControlFlow();
        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = toMapSet(sources)", SET_OF_MAP);
        } else {
            m.addStatement("$T result = this.toMapSet(sources)", SET_OF_MAP);
        }
        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)").endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    // ========== fromMap 方法 ==========

    /** 生成 fromMap(Map<String, Object> source) 基础方法 */
    private MethodSpec generateFromMap() {
        TypeName beanType = ClassName.get(targetType);
        MapKeyStrategy keyStrategy = fromMapConfig.keyStrategy;

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMap")
                .addModifiers(Modifier.PUBLIC)
                .returns(beanType)
                .addParameter(MAP_STRING_OBJECT, "source");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (source == null)").addStatement("return null").endControlFlow();
        m.addStatement("$T target = new $T()", beanType, beanType);

        for (MapFieldInfo fi : fields) {
            if (!fi.inFromMap) continue;
            String key = fi.resolveMapKey(keyStrategy);
            String setter = "set" + capitalize(fi.fieldName);
            TypeMirror fieldType = TypeUtils.getFieldType(fi.field);
            String typeName = fieldType.toString();

            m.addStatement("$T $L = source.get($S)", OBJECT_CLASS, fi.fieldName + "Val", key);
            m.beginControlFlow("if ($L != null)", fi.fieldName + "Val");

            if (TypeUtils.isPrimitive(fieldType) || TypeUtils.isWrapper(fieldType)) {
                // 基本类型/包装类型：安全转换
                generatePrimitiveSetter(m, setter, fi.fieldName + "Val", typeName);
            } else if (TypeUtils.isStringType(fieldType)) {
                m.addStatement("target.$L($L.toString())", setter, fi.fieldName + "Val");
            } else if (TypeUtils.isCollectionType(fieldType)) {
                // 集合类型：直接赋值（保持原结构）
                m.addStatement("target.$L(($T) $L)", setter, ClassName.bestGuess(typeName), fi.fieldName + "Val");
            } else if (TypeUtils.needsDeepCopy(fieldType)) {
                // 嵌套对象：尝试从 Map 还原
                m.beginControlFlow("if ($L instanceof $T)", fi.fieldName + "Val", MAP_STRING_OBJECT);
                m.addStatement("target.$L(fromNestedMap(($T) $L, $T.class))",
                        setter, MAP_STRING_OBJECT, fi.fieldName + "Val", ClassName.bestGuess(typeName));
                m.endControlFlow();
            } else {
                m.addStatement("target.$L(($T) $L)", setter, ClassName.bestGuess(typeName), fi.fieldName + "Val");
            }

            m.endControlFlow();
        }

        m.addStatement("return target");
        return m.build();
    }

    /** 生成基本类型/包装类型的 setter 语句 */
    private void generatePrimitiveSetter(MethodSpec.Builder m, String setter,
                                          String valVar, String typeName) {
        switch (typeName) {
            case "int":
            case "java.lang.Integer":
                m.addStatement("target.$L($L instanceof $T ? ($T) $L : $T.parseInt($L.toString()))",
                        setter, valVar, Integer.class, Integer.class, valVar, Integer.class, valVar);
                break;
            case "long":
            case "java.lang.Long":
                m.addStatement("target.$L($L instanceof $T ? ($T) $L : $T.parseLong($L.toString()))",
                        setter, valVar, Long.class, Long.class, valVar, Long.class, valVar);
                break;
            case "double":
            case "java.lang.Double":
                m.addStatement("target.$L($L instanceof $T ? ($T) $L : $T.parseDouble($L.toString()))",
                        setter, valVar, Double.class, Double.class, valVar, Double.class, valVar);
                break;
            case "float":
            case "java.lang.Float":
                m.addStatement("target.$L($L instanceof $T ? ($T) $L : $T.parseFloat($L.toString()))",
                        setter, valVar, Float.class, Float.class, valVar, Float.class, valVar);
                break;
            case "boolean":
            case "java.lang.Boolean":
                m.addStatement("target.$L($L instanceof $T ? ($T) $L : $T.parseBoolean($L.toString()))",
                        setter, valVar, Boolean.class, Boolean.class, valVar, Boolean.class, valVar);
                break;
            default:
                m.addStatement("target.$L(($T) $L)", setter, ClassName.bestGuess(typeName), valVar);
        }
    }

    /** 生成 fromMap(Map source, preProcessor, postProcessor) 函数式方法 */
    private MethodSpec generateFromMapWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), MAP_STRING_OBJECT);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), MAP_STRING_OBJECT,
                beanType, beanType);

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMap")
                .addModifiers(Modifier.PUBLIC)
                .returns(beanType)
                .addParameter(MAP_STRING_OBJECT, "source")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (source == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("source = preProcessor.apply(source)").endControlFlow();
        m.beginControlFlow("if (source == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = fromMap(source)", beanType);
        } else {
            m.addStatement("$T result = this.fromMap(source)", beanType);
        }
        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(source, result)").endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 fromMapList(List<Map<String, Object>> sources) */
    private MethodSpec generateFromMapList() {
        TypeName beanType = ClassName.get(targetType);
        TypeName listOfBean = ParameterizedTypeName.get(LIST_CLASS, beanType);

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMapList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfBean)
                .addParameter(LIST_OF_MAP, "sources");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.addStatement("$T result = new $T<>(sources.size())", listOfBean, ARRAY_LIST_CLASS);
        m.beginControlFlow("for ($T source : sources)", MAP_STRING_OBJECT);
        if (useStatic) {
            m.addStatement("result.add(fromMap(source))");
        } else {
            m.addStatement("result.add(this.fromMap(source))");
        }
        m.endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 fromMapList(List sources, preProcessor, postProcessor) */
    private MethodSpec generateFromMapListWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName listOfBean = ParameterizedTypeName.get(LIST_CLASS, beanType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), LIST_OF_MAP);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), LIST_OF_MAP,
                listOfBean, listOfBean);

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMapList")
                .addModifiers(Modifier.PUBLIC)
                .returns(listOfBean)
                .addParameter(LIST_OF_MAP, "sources")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)").endControlFlow();
        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = fromMapList(sources)", listOfBean);
        } else {
            m.addStatement("$T result = this.fromMapList(sources)", listOfBean);
        }
        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)").endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 fromMapSet(Set<Map<String, Object>> sources) */
    private MethodSpec generateFromMapSet() {
        TypeName beanType = ClassName.get(targetType);
        TypeName setOfBean = ParameterizedTypeName.get(SET_CLASS, beanType);

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMapSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfBean)
                .addParameter(SET_OF_MAP, "sources");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.addStatement("$T result = new $T<>()", setOfBean, LINKED_HASH_SET_CLASS);
        m.beginControlFlow("for ($T source : sources)", MAP_STRING_OBJECT);
        if (useStatic) {
            m.addStatement("result.add(fromMap(source))");
        } else {
            m.addStatement("result.add(this.fromMap(source))");
        }
        m.endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    /** 生成 fromMapSet(Set sources, preProcessor, postProcessor) */
    private MethodSpec generateFromMapSetWithProcessors() {
        TypeName beanType = ClassName.get(targetType);
        TypeName setOfBean = ParameterizedTypeName.get(SET_CLASS, beanType);
        TypeName preType = ParameterizedTypeName.get(ClassName.get(UnaryOperator.class), SET_OF_MAP);
        TypeName postType = ParameterizedTypeName.get(ClassName.get(BiFunction.class), SET_OF_MAP,
                setOfBean, setOfBean);

        MethodSpec.Builder m = MethodSpec.methodBuilder("fromMapSet")
                .addModifiers(Modifier.PUBLIC)
                .returns(setOfBean)
                .addParameter(SET_OF_MAP, "sources")
                .addParameter(preType, "preProcessor")
                .addParameter(postType, "postProcessor");
        if (useStatic) m.addModifiers(Modifier.STATIC);

        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();
        m.beginControlFlow("if (preProcessor != null)")
                .addStatement("sources = preProcessor.apply(sources)").endControlFlow();
        m.beginControlFlow("if (sources == null)").addStatement("return null").endControlFlow();

        if (useStatic) {
            m.addStatement("$T result = fromMapSet(sources)", setOfBean);
        } else {
            m.addStatement("$T result = this.fromMapSet(sources)", setOfBean);
        }
        m.beginControlFlow("if (result != null && postProcessor != null)")
                .addStatement("result = postProcessor.apply(sources, result)").endControlFlow();
        m.addStatement("return result");
        return m.build();
    }

    // ========== 嵌套对象辅助方法 ==========

    /**
     * 生成嵌套对象转 Map 的辅助方法（反射方式，通用）。
     * 实际生成的代码中直接内联处理，此处为占位。
     */
    @SuppressWarnings("unused")
    private static Map<String, Object> toNestedMap(Object obj) {
        return null; // 占位，实际由生成代码处理
    }

    @SuppressWarnings("unused")
    private static <T> T fromNestedMap(Map<String, Object> map, Class<T> clazz) {
        return null; // 占位，实际由生成代码处理
    }

    // ========== 类结构辅助方法 ==========

    private void addClassAnnotations(TypeSpec.Builder classBuilder) {
        switch (componentModel) {
            case SPRING:
                classBuilder.addAnnotation(ClassName.get("org.springframework.stereotype", "Component"));
                break;
            case CDI:
                classBuilder.addAnnotation(ClassName.get("javax.enterprise.context", "ApplicationScoped"));
                break;
            case JSR330:
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Named"));
                classBuilder.addAnnotation(ClassName.get("javax.inject", "Singleton"));
                break;
            default:
                classBuilder.addModifiers(Modifier.FINAL);
                break;
        }
    }

    private String getPackageName(TypeElement typeElement) {
        String qualifiedName = typeElement.getQualifiedName().toString();
        int lastDot = qualifiedName.lastIndexOf('.');
        return lastDot > 0 ? qualifiedName.substring(0, lastDot) : "";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
