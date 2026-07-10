package com.github.jackieonway.copier.processor.generator;

import com.github.jackieonway.copier.processor.FieldMapping;
import com.github.jackieonway.copier.processor.TypeUtils;
import com.github.jackieonway.copier.processor.context.ProcessorContext;
import com.github.jackieonway.copier.annotation.CopyTarget;
import com.github.jackieonway.copier.annotation.CycleDetectionStrategy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import java.util.Collections;
import java.util.List;

/**
 * 深拷贝代码生成器，负责生成集合和嵌套对象的深拷贝代码。
 *
 * <p>该类负责生成：
 * <ul>
 *   <li>List 字段的深拷贝代码（包括嵌套 List 和 Map）</li>
 *   <li>Set 字段的深拷贝代码（包括嵌套 List）</li>
 *   <li>Map 字段的深拷贝代码（包括嵌套 List）</li>
 *   <li>Array 字段的深拷贝代码</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.1
 */
public class DeepCopyGenerator {

    /**
     * 处理器上下文。
     */
    private final ProcessorContext context;

    /**
     * 构造方法。
     *
     * @param context 处理器上下文
     */
    public DeepCopyGenerator(ProcessorContext context) {
        this.context = context;
    }

    /**
     * 生成 List 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @param useStaticMethods 是否使用静态方法（用于当前 Copier 类，嵌套 Copier 调用始终使用静态方法）
     */
    public void generateListDeepCopyCode(MethodSpec.Builder methodBuilder,
                                         String getterName,
                                         String setterName,
                                         TypeMirror sourceFieldType,
                                         TypeMirror targetFieldType,
                                         FieldMapping mapping,
                                         boolean reverse,
                                         boolean useStaticMethods) {
        List<TypeMirror> sourceArgs = TypeUtils.extractTypeArguments(sourceFieldType);
        List<TypeMirror> targetArgs = TypeUtils.extractTypeArguments(targetFieldType);
        List<TypeMirror> dtoArgs = TypeUtils.extractTypeArguments(mapping.getTargetType());

        TypeMirror sourceElementType = sourceArgs.isEmpty() ? null : sourceArgs.get(0);
        TypeMirror targetElementType = targetArgs.isEmpty() ? null : targetArgs.get(0);
        TypeMirror dtoElementType = dtoArgs.isEmpty() ? null : dtoArgs.get(0);


        // List 循环元素类型：优先使用源元素类型，其次目标元素类型，最后退回 Object
        TypeName loopElementType = sourceElementType != null
                ? safeTypeName(sourceElementType)
                : (targetElementType != null ? safeTypeName(targetElementType) : TypeName.get(Object.class));

        // 目标 List 使用具体元素类型声明，避免 extends 通配符导致 add 受限
        TypeName targetListType = targetElementType != null
                ? ParameterizedTypeName.get(ClassName.get(java.util.List.class), safeTypeName(targetElementType))
                : TypeName.get(targetFieldType);
        TypeName targetListImplType = targetElementType != null
                ? ParameterizedTypeName.get(ClassName.get(java.util.ArrayList.class), safeTypeName(targetElementType))
                : TypeName.get(targetFieldType);

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceList = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetList = new $T(sourceList.size())", targetListType, targetListImplType)
                .beginControlFlow("for ($T item : sourceList)", loopElementType);

        // 一层元素：基本类型 / 对象 / DTO 拷贝
        if (sourceElementType != null && TypeUtils.needsDeepCopy(sourceElementType) && dtoElementType != null) {
            generateElementDeepCopy(methodBuilder, dtoElementType, reverse, "targetList.add");
        } else if (targetElementType != null && TypeUtils.needsDeepCopy(targetElementType) && dtoElementType != null) {
            generateElementDeepCopy(methodBuilder, dtoElementType, reverse, "targetList.add");
        }
        // 嵌套 List：例如 List<List<User>>
        else if (sourceElementType != null && TypeUtils.isList(sourceElementType)) {
            generateNestedListCopyCode(methodBuilder, sourceElementType, targetElementType, dtoElementType, reverse, "targetList");
        }
        // 嵌套 Map：例如 List<Map<K, V>>
        else if (sourceElementType != null && TypeUtils.isMap(sourceElementType)) {
            generateNestedMapInListCopyCode(methodBuilder, sourceElementType, targetElementType, dtoElementType, reverse, "targetList");
        } else {
            methodBuilder.addStatement("targetList.add(item)");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetList)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }

    /**
     * 生成 Set 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @param useStaticMethods 是否使用静态方法
     */
    public void generateSetDeepCopyCode(MethodSpec.Builder methodBuilder,
                                        String getterName,
                                        String setterName,
                                        TypeMirror sourceFieldType,
                                        TypeMirror targetFieldType,
                                        FieldMapping mapping,
                                        boolean reverse,
                                        boolean useStaticMethods) {
        List<TypeMirror> sourceArgs = TypeUtils.extractTypeArguments(sourceFieldType);
        List<TypeMirror> targetArgs = TypeUtils.extractTypeArguments(targetFieldType);
        List<TypeMirror> dtoArgs = TypeUtils.extractTypeArguments(mapping.getTargetType());

        TypeMirror sourceElementType = sourceArgs.isEmpty() ? null : sourceArgs.get(0);
        TypeMirror targetElementType = targetArgs.isEmpty() ? null : targetArgs.get(0);
        TypeMirror dtoElementType = dtoArgs.isEmpty() ? null : dtoArgs.get(0);

        // Set 循环元素类型
        TypeName loopElementType = sourceElementType != null
                ? safeTypeName(sourceElementType)
                : (targetElementType != null ? safeTypeName(targetElementType) : TypeName.get(Object.class));

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceSet = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetSet = new java.util.LinkedHashSet($L)", TypeName.get(targetFieldType),
                        buildInitialCapacity("sourceSet.size()"))
                .beginControlFlow("for ($T item : sourceSet)", loopElementType);

        if (sourceElementType != null && TypeUtils.needsDeepCopy(sourceElementType) && dtoElementType != null) {
            generateElementDeepCopy(methodBuilder, dtoElementType, reverse, "targetSet.add");
        } else if (targetElementType != null && TypeUtils.needsDeepCopy(targetElementType) && dtoElementType != null) {
            generateElementDeepCopy(methodBuilder, dtoElementType, reverse, "targetSet.add");
        } else if (sourceElementType != null && TypeUtils.isList(sourceElementType)) {
            // Set<List<T>> 场景
            generateNestedListCopyCode(methodBuilder, sourceElementType, targetElementType, dtoElementType, reverse, "targetSet");
        } else {
            methodBuilder.addStatement("targetSet.add(item)");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetSet)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }


    /**
     * 生成数组字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @param useStaticMethods 是否使用静态方法
     */
    public void generateArrayDeepCopyCode(MethodSpec.Builder methodBuilder,
                                          String getterName,
                                          String setterName,
                                          TypeMirror sourceFieldType,
                                          TypeMirror targetFieldType,
                                          FieldMapping mapping,
                                          boolean reverse,
                                          boolean useStaticMethods) {
        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceArray = source.$L()", TypeName.get(sourceFieldType), getterName);

        TypeMirror targetComponentType = TypeUtils.getArrayComponentType(targetFieldType);
        TypeMirror sourceComponentType = TypeUtils.getArrayComponentType(sourceFieldType);
        TypeMirror dtoComponentType = TypeUtils.getArrayComponentType(mapping.getTargetType());

        methodBuilder.addStatement("$T targetArray = new $T[sourceArray.length]",
                TypeName.get(targetFieldType),
                TypeName.get(targetComponentType));

        methodBuilder.beginControlFlow("for (int i = 0; i < sourceArray.length; i++)");

        if (sourceComponentType != null && TypeUtils.needsDeepCopy(sourceComponentType) && dtoComponentType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoComponentType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("$T element = sourceArray[i]", TypeName.get(sourceComponentType))
                    .beginControlFlow("if (element != null)")
                    .addStatement("targetArray[i] = $T.$L(element$L)", copierClass, methodName, cacheArg(dtoComponentType))
                    .nextControlFlow("else")
                    .addStatement("targetArray[i] = null")
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("targetArray[i] = sourceArray[i]");
        }

        methodBuilder.endControlFlow()
                .addStatement("target.$L(targetArray)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }

    /**
     * 生成 Map 字段的深拷贝代码。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @param useStaticMethods 是否使用静态方法
     */
    public void generateMapDeepCopyCode(MethodSpec.Builder methodBuilder,
                                        String getterName,
                                        String setterName,
                                        TypeMirror sourceFieldType,
                                        TypeMirror targetFieldType,
                                        FieldMapping mapping,
                                        boolean reverse,
                                        boolean useStaticMethods) {
        TypeMirror sourceKeyArgument = getTypeArgument(sourceFieldType, 0);
        TypeMirror sourceValueArgument = getTypeArgument(sourceFieldType, 1);
        TypeMirror targetKeyArgument = getTypeArgument(targetFieldType, 0);
        TypeMirror targetValueArgument = getTypeArgument(targetFieldType, 1);

        TypeMirror sourceKeyType = TypeUtils.extractMapKeyType(sourceFieldType);
        TypeMirror targetKeyType = TypeUtils.extractMapKeyType(targetFieldType);

        TypeMirror sourceValueType = TypeUtils.extractMapValueType(sourceFieldType);
        TypeMirror targetValueType = TypeUtils.extractMapValueType(targetFieldType);
        TypeMirror dtoValueType = TypeUtils.extractMapValueType(mapping.getTargetType());
        TypeMirror dtoKeyType = TypeUtils.extractMapKeyType(mapping.getTargetType());

        // Key 类型
        TypeName keyTypeName = sourceKeyType != null ? safeTypeName(sourceKeyType)
                : (targetKeyType != null ? safeTypeName(targetKeyType) : TypeName.get(Object.class));

        // Value 类型
        TypeMirror loopValueMirror = sourceValueType != null ? sourceValueType
                : (targetValueType != null ? targetValueType : null);
        TypeName loopValueTypeName = loopValueMirror != null ? safeTypeName(loopValueMirror) : TypeName.get(Object.class);

        TypeMirror entryKeyMirror = sourceKeyArgument != null ? sourceKeyArgument : targetKeyArgument;
        TypeMirror entryValueMirror = sourceValueArgument != null ? sourceValueArgument : targetValueArgument;
        TypeName entryKeyTypeName = entryKeyMirror != null ? TypeName.get(entryKeyMirror) : keyTypeName;
        TypeName entryValueTypeName = entryValueMirror != null ? TypeName.get(entryValueMirror) : loopValueTypeName;
        TypeName targetKeyTypeName = targetKeyType != null ? safeTypeName(targetKeyType) : keyTypeName;
        TypeName targetValueTypeName = targetValueType != null ? safeTypeName(targetValueType) : loopValueTypeName;

        TypeName targetMapType = (targetKeyType != null || targetValueType != null)
                ? ParameterizedTypeName.get(ClassName.get(java.util.Map.class), targetKeyTypeName, targetValueTypeName)
                : TypeName.get(targetFieldType);
        TypeName targetMapImplType = (targetKeyType != null || targetValueType != null)
                ? ParameterizedTypeName.get(ClassName.get(java.util.HashMap.class), targetKeyTypeName, targetValueTypeName)
                : TypeName.get(targetFieldType);

        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName)
                .addStatement("$T sourceMap = source.$L()", TypeName.get(sourceFieldType), getterName)
                .addStatement("$T targetMap = new $T($L)", targetMapType, targetMapImplType,
                        buildInitialCapacity("sourceMap.size()"))
                .beginControlFlow("for (java.util.Map.Entry<$T, $T> entry : sourceMap.entrySet())", entryKeyTypeName, entryValueTypeName)
                .addStatement("$T key = entry.getKey()", keyTypeName)
                .addStatement("$T value = entry.getValue()", loopValueTypeName);

        // 处理 key 的深拷贝
        boolean needsKeyDeepCopy = (sourceKeyType != null && TypeUtils.needsDeepCopy(sourceKeyType) && dtoKeyType != null) ||
                (targetKeyType != null && TypeUtils.needsDeepCopy(targetKeyType) && dtoKeyType != null);

        if (needsKeyDeepCopy && dtoKeyType != null) {
            ClassName keyCopierClass = ClassName.bestGuess(dtoKeyType.toString() + "Copier");
            String keyMethodName = nestedCopierMethod(reverse);
            TypeName targetKeyTypeNameForCopy = targetKeyType != null ? safeTypeName(targetKeyType) : keyTypeName;
            methodBuilder.beginControlFlow("if (key != null)")
                    .addStatement("$T copiedKey = $T.$L(key$L)", targetKeyTypeNameForCopy, keyCopierClass, keyMethodName, cacheArg(dtoKeyType))
                    .nextControlFlow("else")
                    .addStatement("$T copiedKey = null", targetKeyTypeNameForCopy)
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("$T copiedKey = key", keyTypeName);
        }

        methodBuilder.beginControlFlow("if (value != null)");

        if (sourceValueType != null && TypeUtils.needsDeepCopy(sourceValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("targetMap.put(copiedKey, $T.$L(value$L))", copierClass, methodName, cacheArg(dtoValueType));
        } else if (targetValueType != null && TypeUtils.needsDeepCopy(targetValueType) && dtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(dtoValueType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("targetMap.put(copiedKey, $T.$L(value$L))", copierClass, methodName, cacheArg(dtoValueType));
        } else if (sourceValueType != null && TypeUtils.isList(sourceValueType)) {
            // Map<K, List<V>> 场景
            generateNestedListInMapCopyCode(methodBuilder, sourceValueType, targetValueType, dtoValueType, reverse);
        } else {
            methodBuilder.addStatement("targetMap.put(copiedKey, value)");
        }

        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("targetMap.put(copiedKey, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement("target.$L(targetMap)", setterName)
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement("target.$L(null)", setterName)
                .endControlFlow();
    }


    // ========== 私有辅助方法 ==========

    /**
     * 生成元素深拷贝代码（带 null 检查）。
     */
    private void generateElementDeepCopy(MethodSpec.Builder methodBuilder, TypeMirror dtoElementType,
                                         boolean reverse, String addStatement) {
        ClassName copierClass = ClassName.bestGuess(dtoElementType.toString() + "Copier");
        String methodName = nestedCopierMethod(reverse);
        methodBuilder.beginControlFlow("if (item != null)")
                .addStatement(addStatement + "($T.$L(item$L))", copierClass, methodName, cacheArg(dtoElementType))
                .nextControlFlow("else")
                .addStatement(addStatement + "(null)")
                .endControlFlow();
    }

    /**
     * 生成嵌套 List 的深拷贝代码（用于 List<List<T>> 或 Set<List<T>>）。
     */
    private void generateNestedListCopyCode(MethodSpec.Builder methodBuilder,
                                            TypeMirror sourceElementType,
                                            TypeMirror targetElementType,
                                            TypeMirror dtoElementType,
                                            boolean reverse,
                                            String targetCollectionName) {
        TypeName nestedSourceListType = safeTypeName(sourceElementType);
        TypeName nestedTargetListType = targetElementType != null ? safeTypeName(targetElementType) : nestedSourceListType;

        methodBuilder.beginControlFlow("if (item != null)")
                .addStatement("$T nestedSource = item", nestedSourceListType)
                .addStatement("$T nestedTarget = new java.util.ArrayList(nestedSource.size())", nestedTargetListType);

        List<TypeMirror> nestedSourceArgs = TypeUtils.extractTypeArguments(sourceElementType);
        List<TypeMirror> nestedTargetArgs = targetElementType != null
                ? TypeUtils.extractTypeArguments(targetElementType) : Collections.emptyList();
        List<TypeMirror> nestedDtoArgs = dtoElementType != null
                ? TypeUtils.extractTypeArguments(dtoElementType) : Collections.emptyList();

        TypeMirror nestedSourceElementType = nestedSourceArgs.isEmpty() ? null : nestedSourceArgs.get(0);
        TypeMirror nestedTargetElementType = nestedTargetArgs.isEmpty() ? null : nestedTargetArgs.get(0);
        TypeMirror nestedDtoElementType = nestedDtoArgs.isEmpty() ? null : nestedDtoArgs.get(0);

        TypeMirror loopNestedMirror = nestedSourceElementType != null ? nestedSourceElementType
                : (nestedTargetElementType != null ? nestedTargetElementType : null);
        TypeName loopNestedType = loopNestedMirror != null ? safeTypeName(loopNestedMirror) : TypeName.get(Object.class);

        methodBuilder.beginControlFlow("for ($T nestedItem : nestedSource)", loopNestedType);

        if (nestedSourceElementType != null && TypeUtils.needsDeepCopy(nestedSourceElementType) && nestedDtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem$L))", copierClass, methodName, cacheArg(nestedDtoElementType));
        } else if (nestedTargetElementType != null && TypeUtils.needsDeepCopy(nestedTargetElementType) && nestedDtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem$L))", copierClass, methodName, cacheArg(nestedDtoElementType));
        } else {
            methodBuilder.addStatement("nestedTarget.add(nestedItem)");
        }

        methodBuilder.endControlFlow()
                .addStatement(targetCollectionName + ".add(nestedTarget)")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement(targetCollectionName + ".add(null)")
                .endControlFlow();
    }

    /**
     * 生成嵌套 Map 的深拷贝代码（用于 List<Map<K, V>>）。
     */
    private void generateNestedMapInListCopyCode(MethodSpec.Builder methodBuilder,
                                                  TypeMirror sourceElementType,
                                                  TypeMirror targetElementType,
                                                  TypeMirror dtoElementType,
                                                  boolean reverse,
                                                  String targetCollectionName) {
        TypeName nestedSourceMapType = safeTypeName(sourceElementType);
        TypeName nestedTargetMapType = targetElementType != null ? safeTypeName(targetElementType) : nestedSourceMapType;

        TypeMirror nestedSourceKeyType = TypeUtils.extractMapKeyType(sourceElementType);
        TypeMirror nestedTargetKeyType = targetElementType != null ? TypeUtils.extractMapKeyType(targetElementType) : null;

        TypeMirror nestedSourceValueType = TypeUtils.extractMapValueType(sourceElementType);
        TypeMirror nestedTargetValueType = targetElementType != null ? TypeUtils.extractMapValueType(targetElementType) : null;
        TypeMirror nestedDtoValueType = dtoElementType != null ? TypeUtils.extractMapValueType(dtoElementType) : null;
        TypeMirror nestedDtoKeyType = dtoElementType != null ? TypeUtils.extractMapKeyType(dtoElementType) : null;

        TypeMirror nestedLoopKeyMirror = nestedSourceKeyType != null ? nestedSourceKeyType
                : (nestedTargetKeyType != null ? nestedTargetKeyType : null);
        TypeName nestedKeyTypeName = nestedLoopKeyMirror != null ? safeTypeName(nestedLoopKeyMirror) : TypeName.get(Object.class);

        TypeMirror nestedLoopValueMirror = nestedSourceValueType != null ? nestedSourceValueType
                : (nestedTargetValueType != null ? nestedTargetValueType : null);
        TypeName nestedValueTypeName = nestedLoopValueMirror != null ? safeTypeName(nestedLoopValueMirror) : TypeName.get(Object.class);

        methodBuilder.beginControlFlow("if (item != null)")
                .addStatement("$T nestedSource = item", nestedSourceMapType)
                .addStatement("$T nestedTarget = new java.util.HashMap($L)", nestedTargetMapType,
                        buildInitialCapacity("nestedSource.size()"))
                .beginControlFlow("for (java.util.Map.Entry<$T, $T> nestedEntry : nestedSource.entrySet())", nestedKeyTypeName, nestedValueTypeName)
                .addStatement("$T nestedKey = nestedEntry.getKey()", nestedKeyTypeName)
                .addStatement("$T nestedValue = nestedEntry.getValue()", nestedValueTypeName);

        // 处理嵌套 Map 中 key 的深拷贝
        boolean needsNestedKeyDeepCopy = (nestedSourceKeyType != null && TypeUtils.needsDeepCopy(nestedSourceKeyType) && nestedDtoKeyType != null) ||
                (nestedTargetKeyType != null && TypeUtils.needsDeepCopy(nestedTargetKeyType) && nestedDtoKeyType != null);

        if (needsNestedKeyDeepCopy && nestedDtoKeyType != null) {
            ClassName nestedKeyCopierClass = ClassName.bestGuess(nestedDtoKeyType.toString() + "Copier");
            String nestedKeyMethodName = nestedCopierMethod(reverse);
            TypeName nestedTargetKeyTypeNameForCopy = nestedTargetKeyType != null ? safeTypeName(nestedTargetKeyType) : nestedKeyTypeName;
            methodBuilder.beginControlFlow("if (nestedKey != null)")
                    .addStatement("$T nestedCopiedKey = $T.$L(nestedKey$L)", nestedTargetKeyTypeNameForCopy, nestedKeyCopierClass, nestedKeyMethodName, cacheArg(nestedDtoKeyType))
                    .nextControlFlow("else")
                    .addStatement("$T nestedCopiedKey = null", nestedTargetKeyTypeNameForCopy)
                    .endControlFlow();
        } else {
            methodBuilder.addStatement("$T nestedCopiedKey = nestedKey", nestedKeyTypeName);
        }

        methodBuilder.beginControlFlow("if (nestedValue != null)");

        if (nestedSourceValueType != null && TypeUtils.needsDeepCopy(nestedSourceValueType) && nestedDtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, $T.$L(nestedValue$L))", copierClass, methodName, cacheArg(nestedDtoValueType));
        } else if (nestedTargetValueType != null && TypeUtils.needsDeepCopy(nestedTargetValueType) && nestedDtoValueType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoValueType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, $T.$L(nestedValue$L))", copierClass, methodName, cacheArg(nestedDtoValueType));
        } else {
            methodBuilder.addStatement("nestedTarget.put(nestedCopiedKey, nestedValue)");
        }

        methodBuilder.endControlFlow()
                .beginControlFlow("else")
                .addStatement("nestedTarget.put(nestedCopiedKey, null)")
                .endControlFlow()
                .endControlFlow()
                .addStatement(targetCollectionName + ".add(nestedTarget)")
                .endControlFlow()
                .beginControlFlow("else")
                .addStatement(targetCollectionName + ".add(null)")
                .endControlFlow();
    }


    /**
     * 生成嵌套 List 的深拷贝代码（用于 Map<K, List<V>>）。
     */
    private void generateNestedListInMapCopyCode(MethodSpec.Builder methodBuilder,
                                                  TypeMirror sourceValueType,
                                                  TypeMirror targetValueType,
                                                  TypeMirror dtoValueType,
                                                  boolean reverse) {
        TypeName nestedSourceListType = safeTypeName(sourceValueType);
        TypeName nestedTargetListType = targetValueType != null ? safeTypeName(targetValueType) : nestedSourceListType;

        methodBuilder.addStatement("$T nestedSource = value", nestedSourceListType)
                .addStatement("$T nestedTarget = new java.util.ArrayList(nestedSource.size())", nestedTargetListType);

        List<TypeMirror> nestedSourceArgs = TypeUtils.extractTypeArguments(sourceValueType);
        List<TypeMirror> nestedTargetArgs = targetValueType != null
                ? TypeUtils.extractTypeArguments(targetValueType) : Collections.emptyList();
        List<TypeMirror> nestedDtoArgs = dtoValueType != null
                ? TypeUtils.extractTypeArguments(dtoValueType) : Collections.emptyList();

        TypeMirror nestedSourceElementType = nestedSourceArgs.isEmpty() ? null : nestedSourceArgs.get(0);
        TypeMirror nestedTargetElementType = nestedTargetArgs.isEmpty() ? null : nestedTargetArgs.get(0);
        TypeMirror nestedDtoElementType = nestedDtoArgs.isEmpty() ? null : nestedDtoArgs.get(0);

        TypeMirror loopNestedMirror = nestedSourceElementType != null ? nestedSourceElementType
                : (nestedTargetElementType != null ? nestedTargetElementType : null);
        TypeName loopNestedType = loopNestedMirror != null ? safeTypeName(loopNestedMirror) : TypeName.get(Object.class);

        methodBuilder.beginControlFlow("for ($T nestedItem : nestedSource)", loopNestedType);

        if (nestedSourceElementType != null && TypeUtils.needsDeepCopy(nestedSourceElementType) && nestedDtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem$L))", copierClass, methodName, cacheArg(nestedDtoElementType));
        } else if (nestedTargetElementType != null && TypeUtils.needsDeepCopy(nestedTargetElementType) && nestedDtoElementType != null) {
            ClassName copierClass = ClassName.bestGuess(nestedDtoElementType.toString() + "Copier");
            String methodName = nestedCopierMethod(reverse);
            methodBuilder.addStatement("nestedTarget.add($T.$L(nestedItem$L))", copierClass, methodName, cacheArg(nestedDtoElementType));
        } else {
            methodBuilder.addStatement("nestedTarget.add(nestedItem)");
        }

        methodBuilder.endControlFlow()
                .addStatement("targetMap.put(copiedKey, nestedTarget)");
    }

    /**
     * 安全地获取 TypeName，处理通配符类型。
     *
     * @param typeMirror 类型
     * @return TypeName
     */
    private TypeName safeTypeName(TypeMirror typeMirror) {
        if (typeMirror == null) {
            return TypeName.get(Object.class);
        }
        if (typeMirror instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) typeMirror;
            TypeMirror bound = wildcardType.getExtendsBound() != null
                    ? wildcardType.getExtendsBound()
                    : wildcardType.getSuperBound();
            return bound != null ? TypeName.get(bound) : TypeName.get(Object.class);
        }
        return TypeName.get(typeMirror);
    }

    private boolean isRuntimeCycleStrategy() {
        CycleDetectionStrategy strategy = context.getCycleDetectionStrategy();
        return strategy == CycleDetectionStrategy.RETURN_NULL
                || strategy == CycleDetectionStrategy.AUTOMATIC_CACHE;
    }

    private String nestedCopierMethod(boolean reverse) {
        return reverse ? "fromDto" : "toDto";
    }

    private String cacheArg(TypeMirror dtoType) {
        return shouldUseCycleCacheFor(dtoType) ? ", __cache" : "";
    }

    private boolean shouldUseCycleCacheFor(TypeMirror dtoType) {
        if (!isRuntimeCycleStrategy() || dtoType == null) {
            return false;
        }
        if (context.getTargetType() != null
                && context.getTargetType().asType().toString().equals(dtoType.toString())) {
            return true;
        }
        Element element = context.getTypeUtils().asElement(dtoType);
        if (!(element instanceof TypeElement)) {
            return false;
        }
        CopyTarget copyTarget = ((TypeElement) element).getAnnotation(CopyTarget.class);
        return copyTarget != null && isRuntimeCycleStrategy(copyTarget.cycleDetection());
    }

    private boolean isRuntimeCycleStrategy(CycleDetectionStrategy strategy) {
        return strategy == CycleDetectionStrategy.RETURN_NULL
                || strategy == CycleDetectionStrategy.AUTOMATIC_CACHE;
    }

    /**
     * 获取类型参数。
     *
     * @param typeMirror 类型
     * @param index      参数索引
     * @return 类型参数
     */
    private TypeMirror getTypeArgument(TypeMirror typeMirror, int index) {
        if (!(typeMirror instanceof DeclaredType)) {
            return null;
        }
        List<? extends TypeMirror> args = ((DeclaredType) typeMirror).getTypeArguments();
        if (args == null || args.size() <= index) {
            return null;
        }
        return args.get(index);
    }

    /**
     * 计算集合或 Map 的初始容量，减少扩容带来的开销。
     *
     * @param sizeExpression 大小表达式
     * @return 初始容量表达式
     */
    private String buildInitialCapacity(String sizeExpression) {
        return "Math.max((int)(" + sizeExpression + " / 0.75f) + 1, 16)";
    }

    // ========== v1.3 新增方法：更新现有对象嵌套处理 ==========

    /**
     * 生成嵌套对象更新代码。
     *
     * <p>当目标嵌套对象为 null 时创建新对象，否则递归更新嵌套对象的字段。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @since 1.3.0
     */
    public void generateNestedObjectUpdate(MethodSpec.Builder methodBuilder,
                                           String getterName,
                                           String setterName,
                                           TypeMirror sourceFieldType,
                                           TypeMirror targetFieldType,
                                           FieldMapping mapping,
                                           boolean reverse) {
        // 检查源字段是否为 null
        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName);
        
        // 检查目标嵌套对象是否为 null，如果是则创建新对象
        methodBuilder.beginControlFlow("if (target.$L() == null)", getterName);
        ClassName targetClassName = ClassName.bestGuess(targetFieldType.toString());
        methodBuilder.addStatement("target.$L(new $T())", setterName, targetClassName);
        methodBuilder.endControlFlow();
        
        // 如果目标类型有对应的 Copier，使用 updateDto/updateEntity 方法
        if (TypeUtils.needsDeepCopy(targetFieldType)) {
            TypeMirror dtoType = reverse ? sourceFieldType : targetFieldType;
            ClassName copierClass = ClassName.bestGuess(dtoType.toString() + "Copier");
            String updateMethodName = reverse ? "updateEntity" : "updateDto";
            methodBuilder.addStatement("$T.$L(target.$L(), source.$L())", 
                    copierClass, updateMethodName, getterName, getterName);
        } else {
            // 对于没有 Copier 的嵌套对象，直接赋值
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
        }
        
        methodBuilder.endControlFlow();
    }

    /**
     * 生成集合字段更新代码（替换策略）。
     *
     * <p>默认策略：替换整个集合。
     *
     * @param methodBuilder   方法构建器
     * @param getterName      源字段 getter 方法名
     * @param setterName      目标字段 setter 方法名
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     * @param reverse         是否为反向拷贝
     * @since 1.3.0
     */
    public void generateCollectionUpdate(MethodSpec.Builder methodBuilder,
                                         String getterName,
                                         String setterName,
                                         TypeMirror sourceFieldType,
                                         TypeMirror targetFieldType,
                                         FieldMapping mapping,
                                         boolean reverse) {
        // 检查源字段是否为 null
        methodBuilder.beginControlFlow("if (source.$L() != null)", getterName);
        
        // 根据集合类型生成不同的替换代码
        if (TypeUtils.isList(sourceFieldType)) {
            methodBuilder.addStatement("target.$L(new java.util.ArrayList<>(source.$L()))", 
                    setterName, getterName);
        } else if (TypeUtils.isSet(sourceFieldType)) {
            methodBuilder.addStatement("target.$L(new java.util.LinkedHashSet<>(source.$L()))", 
                    setterName, getterName);
        } else if (TypeUtils.isMap(sourceFieldType)) {
            methodBuilder.addStatement("target.$L(new java.util.HashMap<>(source.$L()))", 
                    setterName, getterName);
        } else if (TypeUtils.isArrayType(sourceFieldType)) {
            // 数组需要克隆
            methodBuilder.addStatement("target.$L(source.$L().clone())", setterName, getterName);
        } else {
            // 其他集合类型，直接赋值
            methodBuilder.addStatement("target.$L(source.$L())", setterName, getterName);
        }
        
        methodBuilder.endControlFlow();
    }

    /**
     * 输出原始类型或不支持通配符的警告。
     *
     * @param sourceFieldType 源字段类型
     * @param targetFieldType 目标字段类型
     * @param mapping         字段映射
     */
    public void warnUnsupportedGenerics(TypeMirror sourceFieldType, TypeMirror targetFieldType, FieldMapping mapping) {
        context.warning(
                "集合字段使用了原始类型或不受支持的通配符，已跳过深拷贝生成。请为字段添加明确的泛型参数。source="
                        + sourceFieldType + ", target=" + targetFieldType,
                mapping.getTargetField());
    }

    /**
     * 判断是否有不支持的泛型（原始类型或无界通配符）。
     *
     * @param typeMirror 类型
     * @return 是否有不支持的泛型
     */
    public boolean hasUnsupportedGenerics(TypeMirror typeMirror) {
        return TypeUtils.isRawType(typeMirror) || TypeUtils.hasUnboundedWildcard(typeMirror);
    }
}
