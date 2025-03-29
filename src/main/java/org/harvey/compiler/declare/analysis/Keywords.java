package org.harvey.compiler.declare.analysis;

import org.harvey.compiler.execute.calculate.Operator;

import java.util.Map;
import java.util.Set;

/**
 * 关键字的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-19 16:58
 */
public class Keywords {
    /**
     * 包括VOID和VAR
     */
    public static final Set<Keyword> BASIC_TYPE;
    public static final Set<Keyword> BASIC_NUMBER_TYPE;
    public static final Set<Keyword> ACCESS_CONTROL;
    public static final Keyword ACCESS_CONTROL_EMBELLISH = Keyword.INTERNAL;
    public static final Set<Keyword> CONTROL_STRUCTURE;
    public static final Set<Keyword> COMPLEX_STRUCTURE;
    public static final Map<Keyword, Operator> OPERATOR_KEYWORD_MAP;

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
                Keyword.UINT8,
                Keyword.UINT16,
                Keyword.UINT32,
                Keyword.UINT64,
                Keyword.VOID,
                Keyword.VAR
        );
        BASIC_NUMBER_TYPE = Set.of(
                Keyword.FLOAT32,
                Keyword.FLOAT64,
                Keyword.INT8,
                Keyword.INT16,
                Keyword.INT32,
                Keyword.INT64,
                Keyword.UINT8,
                Keyword.UINT16,
                Keyword.UINT32,
                Keyword.UINT64
        );
    }

    static {
        ACCESS_CONTROL = Set.of(
                Keyword.PUBLIC,
                Keyword.PROTECTED,
                Keyword.PRIVATE,
                Keyword.FILE,
                Keyword.PACKAGE
        );
    }

    static {
        CONTROL_STRUCTURE = Set.of(
                Keyword.IF,
                Keyword.ELSE,
                Keyword.SWITCH,
                Keyword.CASE,
                Keyword.DEFAULT,
                Keyword.DO,
                Keyword.WHILE,
                Keyword.FOR,
                Keyword.BREAK,
                Keyword.CONTINUE,
                Keyword.TRY,
                Keyword.CATCH,
                Keyword.FINALLY
        );
    }

    static {
        COMPLEX_STRUCTURE = Set.of(
                Keyword.STRUCT,
                Keyword.CLASS,
                Keyword.ENUM,
                Keyword.INTERFACE
        );
        // Keyword.CALLABLE
    }

    static {
        OPERATOR_KEYWORD_MAP = Map.of(Keyword.IS, Operator.IS, Keyword.IN, Operator.IN);
    }

    private Keywords() {
    }

    public static boolean isKeyword(String s) {
        return Keyword.get(s) != null;
    }

    /**
     * @see #BASIC_TYPE
     */
    public static boolean isBasicType(String name) {
        return isBasicType(Keyword.get(name));
    }

    /**
     * @see #BASIC_TYPE
     */
    public static boolean isBasicType(Keyword keyword) {
        return keyword != null && BASIC_TYPE.contains(keyword);
    }

    public static boolean isNumberBasicType(Keyword keyword) {
        return BASIC_NUMBER_TYPE.contains(keyword);
    }

    public static boolean isAccessControl(Keyword keyword) {
        return keyword != null && ACCESS_CONTROL.contains(keyword);
    }

    public static boolean isControlStructure(Keyword keyword) {
        return keyword != null && CONTROL_STRUCTURE.contains(keyword);
    }

    public static boolean isControlStructure(String name) {
        return isControlStructure(Keyword.get(name));
    }

    public static boolean isBoolConstant(String source) {
        return Keyword.TRUE.equals(source) || Keyword.FALSE.equals(source);
    }

    public static boolean isBoolConstant(Keyword source) {
        return Keyword.TRUE == source || Keyword.FALSE == source;
    }

    public static boolean isStructure(String name) {
        return isStructure(Keyword.get(name));
    }

    public static boolean isStructure(Keyword keyword) {
        return keyword != null && COMPLEX_STRUCTURE.contains(keyword);
    }

    /**
     * @return true if {@link #OPERATOR_KEYWORD_MAP}
     */
    public static boolean isOperator(Keyword keyword) {
        /*
         如何获取字节码对象?
         Type.instance<int>();->Type<int>
         Type.instance<Object>();->Type<int>
         Type.instance<List<String>>();->Type<List<String>>
         public class Type<T>{
              public static native Type<T> instance<T>();
              public static native Type<T> of(Reference r);
         }
         List<String> l = new().clone();// 报错
         var l = new ArrayList<String>();
         Type.of(l);
        */

        return keyword != null && OPERATOR_KEYWORD_MAP.containsKey(keyword) /*||
                // 类型转换
                (Keyword.VOID != keyword && BASIC_TYPE.contains(keyword))*/;
    }

    public static boolean callable(Keyword keyword) {
        // 形如super(), this(), typeof(), sizeof()
        return keyword == Keyword.SUPER || keyword == Keyword.THIS;
    }


}
