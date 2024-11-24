package org.harvey.compiler.analysis.core;

import org.harvey.compiler.common.util.CollectionUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-19 16:58
 */
public class Keywords {
    private Keywords() {
    }

    public static final Map<String, Keyword> KEYWORD_TABLE;

    static {
        KEYWORD_TABLE = Arrays.stream(Keyword.values()).collect(Collectors.toMap(
                Keyword::getValue, v -> v
        ));
    }

    public static final Set<String> KEYWORD_SET = KEYWORD_TABLE.keySet();


    public static final Set<Keyword> BASIC_TYPE;


    static {
        BASIC_TYPE = Set.of(
                Keyword.BOOL,
                Keyword.CHAR,
                Keyword.FLOAT32,
                Keyword.FLOAT64,
                Keyword.INT8,
                Keyword.INT16,
                Keyword.INT32,
                Keyword.INT64,
                Keyword.VOID
        );
    }

    public static final Set<Keyword> BASIC_TYPE_EMBELLISH;

    static {
        BASIC_TYPE_EMBELLISH = Set.of(
                Keyword.SIGNED,
                Keyword.UNSIGNED
        );
    }

    public static final Set<Keyword> BASIC_EMBELLISH_ABLE_TYPE;

    static {
        // 可以被修饰的
        BASIC_EMBELLISH_ABLE_TYPE = Set.of(
                Keyword.INT8,
                Keyword.INT16,
                Keyword.INT32,
                Keyword.INT64
        );
    }

    public static final Set<Keyword> ACCESS_CONTROL;
    public static final Keyword ACCESS_CONTROL_EMBELLISH = Keyword.INTERNAL;

    static {
        ACCESS_CONTROL = Set.of(
                Keyword.PUBLIC,
                Keyword.PROTECTED,
                Keyword.PRIVATE,
                Keyword.FILE,
                Keyword.PACKAGE
        );
    }

    public static final Set<Keyword> INTERNAL_ABLE_ACCESS_CONTROL;

    static {
        INTERNAL_ABLE_ACCESS_CONTROL = Set.of(
                Keyword.PRIVATE,
                Keyword.FILE,
                Keyword.PACKAGE
        );
    }

    public static int size() {
        return KEYWORD_SET.size();
    }

    public static boolean isKeyword(String s) {
        return KEYWORD_SET.contains(s);
    }

    public static boolean isBasicType(String name) {
        return CollectionUtil.contains(BASIC_TYPE, kw -> kw.equals(name));
    }

    public static boolean isBasicEmbellishAbleType(String name) {
        return CollectionUtil.contains(BASIC_EMBELLISH_ABLE_TYPE, kw -> kw.equals(name));
    }

    public static boolean isBasicTypeEmbellish(String name) {
        return CollectionUtil.contains(BASIC_TYPE_EMBELLISH, kw -> kw.equals(name));
    }

    public static boolean isBasicType(Keyword keyword) {
        return CollectionUtil.contains(BASIC_TYPE, kw -> kw == keyword);
    }

    public static boolean isBasicEmbellishAbleType(Keyword keyword) {
        return CollectionUtil.contains(BASIC_EMBELLISH_ABLE_TYPE, kw -> kw == keyword);
    }

    public static boolean isBasicTypeEmbellish(Keyword keyword) {
        return CollectionUtil.contains(BASIC_TYPE_EMBELLISH, kw -> kw == keyword);
    }

    public static boolean isAccessControl(Keyword keyword) {
        return CollectionUtil.contains(ACCESS_CONTROL, kw -> kw == keyword);
    }
}
