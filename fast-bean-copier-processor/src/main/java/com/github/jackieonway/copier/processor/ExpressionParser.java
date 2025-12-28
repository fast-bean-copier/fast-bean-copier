package com.github.jackieonway.copier.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 表达式解析器，用于解析和验证 Java 表达式。
 *
 * <p>支持的表达式类型：
 * <ul>
 *   <li>方法调用：{@code source.getXxx()}</li>
 *   <li>链式调用：{@code source.getXxx().getYyy()}</li>
 *   <li>基本运算符：{@code +}、{@code -}、{@code *}、{@code /} 等</li>
 *   <li>三元运算符：{@code condition ? value1 : value2}</li>
 *   <li>字符串连接：{@code "str1" + "str2"}</li>
 * </ul>
 *
 * @author jackieonway
 * @since 1.2.0
 */
public class ExpressionParser {

    private final Elements elementUtils;
    private final Types typeUtils;
    private final Messager messager;

    /**
     * 创建表达式解析器。
     *
     * @param elementUtils 元素工具
     * @param typeUtils 类型工具
     * @param messager 消息输出器
     */
    public ExpressionParser(Elements elementUtils, Types typeUtils, Messager messager) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
        this.messager = messager;
    }

    /**
     * 解析表达式结果。
     */
    public static class ParseResult {
        private final boolean valid;
        private final String errorMessage;
        private final String codeSnippet;
        private final Set<String> requiredVariables;

        private ParseResult(boolean valid, String errorMessage, String codeSnippet, Set<String> requiredVariables) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.codeSnippet = codeSnippet;
            this.requiredVariables = requiredVariables;
        }

        public static ParseResult success(String codeSnippet, Set<String> requiredVariables) {
            return new ParseResult(true, null, codeSnippet, requiredVariables);
        }

        public static ParseResult error(String errorMessage) {
            return new ParseResult(false, errorMessage, null, null);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getCodeSnippet() {
            return codeSnippet;
        }

        public Set<String> getRequiredVariables() {
            return requiredVariables;
        }
    }

    /**
     * 解析表达式。
     *
     * @param expression 表达式字符串
     * @param sourceType 源对象类型
     * @param targetType 目标字段类型
     * @param element 关联的元素（用于错误报告）
     * @return 解析结果
     */
    public ParseResult parseExpression(String expression, TypeMirror sourceType, 
                                       TypeMirror targetType, Element element) {
        if (expression == null || expression.trim().isEmpty()) {
            return ParseResult.error("Expression cannot be null or empty");
        }

        String trimmed = expression.trim();

        // 语法验证
        String syntaxError = ExpressionUtils.validateSyntax(trimmed);
        if (syntaxError != null) {
            reportError(element, "Expression syntax error: " + syntaxError);
            return ParseResult.error(syntaxError);
        }

        // 提取变量
        List<String> variables = ExpressionUtils.extractVariables(trimmed);
        Set<String> requiredVariables = new HashSet<>(variables);

        // 验证变量（确保 source 变量存在于表达式中，如果表达式引用了它）
        if (requiredVariables.contains("source") && sourceType == null) {
            String error = "Expression references 'source' but source type is not available";
            reportError(element, error);
            return ParseResult.error(error);
        }

        // 生成代码片段
        String codeSnippet = generateCodeSnippet(trimmed, sourceType, targetType);

        return ParseResult.success(codeSnippet, requiredVariables);
    }

    /**
     * 验证表达式中的方法调用是否有效。
     *
     * @param expression 表达式
     * @param sourceType 源类型
     * @param element 关联的元素
     * @return 如果有效返回 true
     */
    public boolean validateMethodCalls(String expression, TypeMirror sourceType, Element element) {
        if (expression == null || sourceType == null) {
            return true; // 无法验证，假设有效
        }

        // 提取 source.getXxx() 形式的方法调用
        String[] parts = expression.split("\\.");
        if (parts.length < 2) {
            return true;
        }

        // 获取源类型的元素
        TypeElement sourceElement = (TypeElement) typeUtils.asElement(sourceType);
        if (sourceElement == null) {
            return true; // 无法获取类型信息，假设有效
        }

        // 检查方法是否存在
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            // 提取方法名
            int parenIndex = part.indexOf('(');
            if (parenIndex > 0) {
                String methodName = part.substring(0, parenIndex);
                if (!hasMethod(sourceElement, methodName)) {
                    reportWarning(element, "Method '" + methodName + "' may not exist in " + sourceType);
                }
            }
        }

        return true;
    }

    /**
     * 检查类型是否有指定的方法。
     */
    private boolean hasMethod(TypeElement typeElement, String methodName) {
        for (Element enclosed : typeElement.getEnclosedElements()) {
            if (enclosed instanceof ExecutableElement) {
                if (enclosed.getSimpleName().toString().equals(methodName)) {
                    return true;
                }
            }
        }
        // 检查父类
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !superclass.toString().equals("java.lang.Object")) {
            TypeElement superElement = (TypeElement) typeUtils.asElement(superclass);
            if (superElement != null) {
                return hasMethod(superElement, methodName);
            }
        }
        return false;
    }

    /**
     * 生成代码片段。
     *
     * <p>将表达式转换为可以直接嵌入生成代码的片段。
     */
    private String generateCodeSnippet(String expression, TypeMirror sourceType, TypeMirror targetType) {
        // 表达式可以直接使用，因为它已经是有效的 Java 代码
        // 只需要确保变量名正确
        return expression;
    }

    /**
     * 推断表达式的返回类型。
     *
     * @param expression 表达式
     * @param sourceType 源类型
     * @return 推断的类型描述
     */
    public String inferReturnType(String expression, TypeMirror sourceType) {
        return ExpressionUtils.inferType(expression, sourceType);
    }

    /**
     * 检查表达式是否需要类型转换。
     *
     * @param expression 表达式
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果需要类型转换返回 true
     */
    public boolean needsTypeCast(String expression, TypeMirror sourceType, TypeMirror targetType) {
        if (targetType == null) {
            return false;
        }

        String inferredType = inferReturnType(expression, sourceType);
        String targetTypeName = targetType.toString();

        // 简单比较类型名
        return !inferredType.equals(targetTypeName) && !"Object".equals(inferredType);
    }

    /**
     * 报告错误。
     */
    private void reportError(Element element, String message) {
        if (messager != null && element != null) {
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
        }
    }

    /**
     * 报告警告。
     */
    private void reportWarning(Element element, String message) {
        if (messager != null && element != null) {
            messager.printMessage(Diagnostic.Kind.WARNING, message, element);
        }
    }
}
