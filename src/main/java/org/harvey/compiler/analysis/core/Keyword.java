package org.harvey.compiler.analysis.core;

import lombok.Getter;

import java.util.Objects;

/**
 * 关键字表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-19 16:16
 */
@Getter
public enum Keyword {
    // 模块和包
    IMPORT("import"),
    STRUCT("struct"),
    CLASS("class"),
    ENUM("enum"),
    INTERFACE("interface"),
    // 继承
    EXTENDS("extends"),
    IMPLEMENTS("implements"),
    ABSTRACT("abstract"),
    SUPER("super"),
    THIS("this"),
    // 访问控制权限
    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    INTERNAL("internal"),
    FILE("file"),
    PACKAGE("package"),
    // package+internal/internal+package表包及其子包可访问
    // 类成员
    STATIC("static"),
    // 不可变,
    //  方法加const表只读方法,
    //      只能调用本类成员的只读(const)方法
    //      能改变成员引用的对象或值
    //  属性/参数加const
    //      只能调用本属性/参数的const的方法
    //      成员可以被写
    CONST("const"),
    // 不可变
    // 属性加final
    //  只能调用本属性不可写, 可以调用其写方法
    //  不能改变成员的值
    // 类加final
    //   可以继承但方法不可重写, 也不可写父类成员变量(只能在构造函数初始化)
    FINAL("final"),
    // 类不可继承/方法不可重写
    SEALED("sealed"),

    // 类成员-不好说
    FIELD("field"),
    METHOD("method"),
    // 重写运算符, TODO, 以实现接口的形式调用函数式方程的形式, 还是以实现Cpp重写运算符的形式的形式
    OPERATOR("operator"),
    CONSTRUCTOR("constructor"),
    // 基本数据类型
    BOOL("bool"),
    CHAR("char"),
    FLOAT32("float32"),
    FLOAT64("float64"),
    INT8("int8"),
    INT16("int16"),
    INT32("int32"),
    INT64("int64"),
    SIGNED("signed"),
    UNSIGNED("unsigned"),
    // 方法
    VOID("void"),
    RETURN("return"),
    // 声明方法
    CALLABLE("callable"),
    // 控制结构
    IF("if"),
    ELSE("else"),
    SWITCH("switch"),
    CASE("case"),
    DEFAULT("default"),
    WHILE("while"),
    DO("do"),
    FOR("for"),
    BREAK("break"),
    CONTINUE("continue"),
    // GOTO("goto"),, 不支持
    // 直接量
    TRUE("true"),
    FALSE("false"),
    NULL("null"),
    // 自动变量
    VAR("var"),
    // 实例化对象
    NEW("new"),
    // 不好搞-反射
    // instance of
    IS("is"),
    // 不好搞-异常
    THROW("throw"),
    THROWS("throws"),
    TRY("try"),
    CATCH("catch"),
    //try-with-resource
    WITH("with"),
    FINALLY("finally"),
    // TODO 不好搞-多线程
    // 其他
    NATIVE("native");
    private final String value;

    Keyword(String value) {
        this.value = value;
    }

    public static Keyword get(String keyword) {
        for (Keyword value : Keyword.values()) {
            if (Objects.equals(value.getValue(), keyword)) {
                return value;
            }
        }
        return null;
    }

    public String value() {
        return value;
    }

    public boolean equals(String value) {
        if (this.value == null && value == null) {
            return true;
        }
        if (this.value == null || value == null) {
            return false;
        }
        return this.value.equals(value);
    }
}
