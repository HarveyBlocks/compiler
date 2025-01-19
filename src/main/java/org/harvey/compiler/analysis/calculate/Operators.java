package org.harvey.compiler.analysis.calculate;

import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 存储运算符表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:04
 */
public class Operators {

    public static final Map<String, Operator> ENUM_NAME_MAP;
    public static final Set<Operator> SET;
    public static final Set<Operator> TRADITIONAL_SET;
    public static final Set<String> NAME_SET;
    public static final Set<Operator> ASSIGNS;
    public static final Set<String> TRADITIONAL_NAME_SET;
    public static final List<Map<String, Integer>> TRADITIONAL_OPERATOR_LENGTH_LIST_SUB_NAME_MAP;
    public static final Set<Character> OPERATOR_SIGN_SET;
    private static final Map<Operator, Operator> PRE_POST;
    private static final Map<Operator, Operator> POST_PRE;

    static {
        ENUM_NAME_MAP = Arrays.stream(Operator.values()).collect(Collectors.toMap(Enum::name, op -> op));
    }

    static {
        SET = ENUM_NAME_MAP.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    static {
        HashSet<Operator> traditionalSet = new HashSet<>(SET);
        traditionalSet.remove(Operator.IN);
        traditionalSet.remove(Operator.IS);
        TRADITIONAL_SET = Collections.unmodifiableSet(traditionalSet);
    }

    static {
        NAME_SET = SET.stream().map(Operator::getName).collect(Collectors.toUnmodifiableSet());
    }

    static {
        ASSIGNS = Set.of(Operator.ASSIGN, Operator.DIVIDE_ASSIGN, Operator.MULTIPLY_ASSIGN, Operator.REMAINDER_ASSIGN,
                Operator.ADD_ASSIGN, Operator.SUBTRACT_ASSIGN, Operator.BITWISE_LEFT_MOVE_ASSIGN,
                Operator.BITWISE_RIGHT_MOVE_ASSIGN, Operator.BITWISE_AND_ASSIGN, Operator.BITWISE_XOR_ASSIGN,
                Operator.BITWISE_OR_ASSIGN);
    }

    static {
        TRADITIONAL_NAME_SET = TRADITIONAL_SET.stream().map(Operator::getName).collect(Collectors.toUnmodifiableSet());
    }

    static {
        List<Map<String, Integer>> maps = new ArrayList<>();
        boolean has = true;
        for (int i = 0; has; i++) {
            maps.add(new HashMap<>());
            Map<String, Integer> map = maps.get(i);
            has = false;
            for (String s : TRADITIONAL_NAME_SET) {
                if (i >= s.length()) {
                    continue;
                }
                has = true;
                String c = s.substring(0, i + 1);
                if (map.containsKey(c)) {
                    map.put(c, map.get(c) + 1);
                } else {
                    map.put(c, 1);
                }
            }
        }

        List<Map<String, Integer>> unmodifiableMaps = new ArrayList<>();
        for (Map<String, Integer> map : maps) {
            unmodifiableMaps.add(Collections.unmodifiableMap(map));
        }
        TRADITIONAL_OPERATOR_LENGTH_LIST_SUB_NAME_MAP = Collections.unmodifiableList(unmodifiableMaps);
    }

    static {
        final Set<Character> operatorSet = new HashSet<>();
        Operators.TRADITIONAL_NAME_SET.stream().map(String::toCharArray).forEach(cs -> {
            for (char c : cs) {
                operatorSet.add(c);
            }
        });
        OPERATOR_SIGN_SET = Collections.unmodifiableSet(operatorSet);
    }

    static {
        PRE_POST = Map.of(Operator.AT_INDEX_PRE, Operator.AT_INDEX_POST, Operator.BRACKET_PRE, Operator.BRACKET_POST,
                Operator.GENERIC_LIST_PRE, Operator.GENERIC_LIST_POST);
        POST_PRE = Map.of(Operator.AT_INDEX_POST, Operator.AT_INDEX_PRE, Operator.BRACKET_POST, Operator.BRACKET_PRE,
                Operator.GENERIC_LIST_POST, Operator.GENERIC_LIST_PRE);
    }

    public static Operator getByEnumName(String value) {
        return ENUM_NAME_MAP.get(value);
    }

    public static boolean isAssign(String value) {
        return CollectionUtil.contains(Operators.ASSIGNS, operator -> operator.nameEquals(value));
    }

    public static List<String> trySplit(String source, SourcePosition position) {
        try {
            return trySplit(source);
        } catch (IllegalArgumentException e) {
            throw new AnalysisExpressionException(position, e.getMessage());
        }
    }

    /**
     * 从左向右遍历, 长的Operator优先匹配, 长的Operator无法匹配, 重新退回尝试匹配
     */
    private static List<String> trySplit(String string) {
        int start = 0;
        List<String> result = new ArrayList<>();
        int end = 1;
        for (; end <= string.length(); end++) {
            String substring = string.substring(start, end);
            if (substring.length() - 1 >= TRADITIONAL_OPERATOR_LENGTH_LIST_SUB_NAME_MAP.size()) {
                throw new IllegalArgumentException("Unknown: " + substring);
            }
            Map<String, Integer> map = TRADITIONAL_OPERATOR_LENGTH_LIST_SUB_NAME_MAP.get(substring.length() - 1);
            if (map.containsKey(substring)) {
                if (map.get(substring) == 1) {
                    result.add(substring);
                    start = end;
                }
                continue;
            }
            boolean find = false;
            for (int i = 1; i < substring.length(); i++) {
                substring = StringUtil.substring(substring, -i);
                if (!NAME_SET.contains(substring)) {
                    continue;
                }
                find = true;
                end -= i;
                start = end;
                break;
            }
            if (find) {
                result.add(substring);
            } else {
                throw new IllegalArgumentException("Unknown: " + substring);
            }
        }

        end--;
        if (start != end) {
            String substring = string.substring(start, end);
            if (NAME_SET.contains(substring)) {
                result.add(substring);
            } else {
                throw new IllegalArgumentException("Unknown: " + substring);
            }
        }
        return result;

    }

    public static boolean is(String s) {
        return NAME_SET.contains(s);
    }

    /**
     * () 前Identifier->函数调用
     *      前Operator->括号
     * <>前类型, Operator(先要注册类型)->是类型, 否则括号
     * []
     */
    public static Operator[] get(String s) {
        List<Operator> result = new LinkedList<>();
        for (Operator operator : SET) {
            if (operator.nameEquals(s)) {
                result.add(operator);
            }
        }
        return result.toArray(new Operator[]{});
    }

    public static boolean isPre(Operator oper) {
        return PRE_POST.containsKey(oper);
    }

    public static boolean isPost(Operator oper) {
        return POST_PRE.containsKey(oper);
    }

    public static Operator pair(Operator oper) {
        if (isPre(oper)) {
            return PRE_POST.get(oper);
        } else if (isPost(oper)) {
            return POST_PRE.get(oper);
        } else {
            return null;
        }
    }

}
