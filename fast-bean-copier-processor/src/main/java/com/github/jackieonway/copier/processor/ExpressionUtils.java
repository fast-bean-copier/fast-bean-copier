package com.github.jackieonway.copier.processor;

import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式工具类，提供表达式语法验证和变量提取功能。
 *
 * <p>用于处理 {@link com.github.jackieonway.copier.annotation.CopyField#expression()} 中的 Java 表达式。
 *
 * @author jackieonway
 * @since 1.2.0
 */
public final class ExpressionUtils {

    /**
     * 匹配标识符的正则表达式。
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

    /**
     * 匹配方法调用的正则表达式。
     */
    private static final Pattern METHOD_CALL_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    /**
     * 匹配字符串字面量的正则表达式。
     */
    private static final Pattern STRING_LITERAL_PATTERN = Pattern.compile("\"(?:[^\"\\\\]|\\\\.)*\"");

    /**
     * Java 关键字集合。
     */
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>();

    static {
        // Java 关键字
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
        };
        for (String keyword : keywords) {
            JAVA_KEYWORDS.add(keyword);
        }
    }

    private ExpressionUtils() {
        // 私有构造器，防止实例化
    }

    /**
     * 验证表达式语法。
     *
     * <p>进行基本的语法检查：
     * <ul>
     *   <li>括号匹配检查</li>
     *   <li>引号匹配检查</li>
     *   <li>基本语法结构检查</li>
     * </ul>
     *
     * @param expression 要验证的表达式
     * @return 验证结果，如果有效返回 null，否则返回错误信息
     */
    public static String validateSyntax(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "Expression cannot be null or empty";
        }

        String trimmed = expression.trim();

        // 检查括号匹配
        String bracketError = checkBrackets(trimmed);
        if (bracketError != null) {
            return bracketError;
        }

        // 检查引号匹配
        String quoteError = checkQuotes(trimmed);
        if (quoteError != null) {
            return quoteError;
        }

        // 检查是否以运算符结尾（不包括后缀运算符）
        if (trimmed.matches(".*[+\\-*/=<>!&|^%]$") && !trimmed.matches(".*[+\\-]{2}$")) {
            return "Expression cannot end with an operator";
        }

        return null; // 验证通过
    }

    /**
     * 提取表达式中的变量。
     *
     * <p>识别表达式中使用的变量名（如 {@code source}），
     * 排除方法名、字符串字面量和 Java 关键字。
     *
     * @param expression 表达式
     * @return 变量名列表
     */
    public static List<String> extractVariables(String expression) {
        List<String> variables = new ArrayList<>();
        if (expression == null || expression.trim().isEmpty()) {
            return variables;
        }

        // 移除字符串字面量，避免误识别
        String cleaned = STRING_LITERAL_PATTERN.matcher(expression).replaceAll("\"\"");

        // 收集方法名，用于排除
        Set<String> methodNames = new HashSet<>();
        Matcher methodMatcher = METHOD_CALL_PATTERN.matcher(cleaned);
        while (methodMatcher.find()) {
            methodNames.add(methodMatcher.group(1));
        }

        // 提取标识符
        Set<String> seen = new HashSet<>();
        Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(cleaned);
        while (identifierMatcher.find()) {
            String identifier = identifierMatcher.group(1);
            // 排除方法名、关键字和已添加的变量
            if (!methodNames.contains(identifier) 
                    && !JAVA_KEYWORDS.contains(identifier)
                    && !seen.contains(identifier)) {
                // 检查是否是方法调用的一部分（前面有点号）
                int start = identifierMatcher.start();
                if (start > 0 && cleaned.charAt(start - 1) == '.') {
                    continue; // 跳过方法调用或字段访问
                }
                variables.add(identifier);
                seen.add(identifier);
            }
        }

        return variables;
    }

    /**
     * 推断表达式的返回类型。
     *
     * <p>基于表达式结构和源类型进行简单的类型推断。
     * 注意：这是一个简化的实现，复杂表达式可能无法准确推断。
     *
     * @param expression 表达式
     * @param sourceType 源对象类型
     * @return 推断的返回类型描述，如果无法推断返回 "Object"
     */
    public static String inferType(String expression, TypeMirror sourceType) {
        if (expression == null || expression.trim().isEmpty()) {
            return "Object";
        }

        String trimmed = expression.trim();

        // 字符串字面量
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return "String";
        }

        // 数字字面量
        if (trimmed.matches("-?\\d+")) {
            return "int";
        }
        if (trimmed.matches("-?\\d+L")) {
            return "long";
        }
        if (trimmed.matches("-?\\d+\\.\\d+")) {
            return "double";
        }
        if (trimmed.matches("-?\\d+\\.\\d+[fF]")) {
            return "float";
        }

        // 布尔字面量
        if ("true".equals(trimmed) || "false".equals(trimmed)) {
            return "boolean";
        }

        // 字符串连接表达式
        if (trimmed.contains("+ \"") || trimmed.contains("\" +")) {
            return "String";
        }

        // 三元运算符 - 尝试推断结果类型
        if (trimmed.contains("?") && trimmed.contains(":")) {
            // 简化处理，返回 Object
            return "Object";
        }

        // 默认返回 Object
        return "Object";
    }

    /**
     * 检查表达式是否包含指定的变量。
     *
     * @param expression 表达式
     * @param variableName 变量名
     * @return 如果包含返回 true
     */
    public static boolean containsVariable(String expression, String variableName) {
        List<String> variables = extractVariables(expression);
        return variables.contains(variableName);
    }

    /**
     * 检查括号是否匹配。
     */
    private static String checkBrackets(String expression) {
        int parentheses = 0;  // ()
        int brackets = 0;     // []
        int braces = 0;       // {}
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // 处理字符串
            if ((c == '"' || c == '\'') && (i == 0 || expression.charAt(i - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            if (inString) {
                continue;
            }

            switch (c) {
                case '(':
                    parentheses++;
                    break;
                case ')':
                    parentheses--;
                    if (parentheses < 0) {
                        return "Unmatched closing parenthesis at position " + i;
                    }
                    break;
                case '[':
                    brackets++;
                    break;
                case ']':
                    brackets--;
                    if (brackets < 0) {
                        return "Unmatched closing bracket at position " + i;
                    }
                    break;
                case '{':
                    braces++;
                    break;
                case '}':
                    braces--;
                    if (braces < 0) {
                        return "Unmatched closing brace at position " + i;
                    }
                    break;
            }
        }

        if (parentheses != 0) {
            return "Unmatched parentheses";
        }
        if (brackets != 0) {
            return "Unmatched brackets";
        }
        if (braces != 0) {
            return "Unmatched braces";
        }

        return null;
    }

    /**
     * 检查引号是否匹配。
     */
    private static String checkQuotes(String expression) {
        boolean inDoubleQuote = false;
        boolean inSingleQuote = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            // 跳过转义字符
            if (c == '\\' && i + 1 < expression.length()) {
                i++;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            }
        }

        if (inDoubleQuote) {
            return "Unmatched double quote";
        }
        if (inSingleQuote) {
            return "Unmatched single quote";
        }

        return null;
    }
}
