package org.harvey.compiler.declare.analysis;

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
    IMPORT("import"), STRUCT("struct"), CLASS("class"), ENUM("enum"), INTERFACE("interface"), ALIAS("alias"), // 继承
    EXTENDS("extends"), IMPLEMENTS("implements"), // 方法没有函数体就是abstract
    // 类有抽象方法就是abstract
    // 如果用一个方法没有函数体, 就是抽象方法, 一个类如果有抽象方法就是抽象类
    // 那么, 继承一个抽象类的类, 就无法在编译阶段知道自己继承的是抽象类
    // 由于没有实现父类的抽象方法而自己也变抽象了
    // 这样不好, 所以ABSTRACT的关键字指示是有必要的
    // 只是...这个关键字或许不一定要序列化吧?
    ABSTRACT("abstract"), SUPER("super"), THIS("this"), // 访问控制权限
    PUBLIC("public"), PROTECTED("protected"), PRIVATE("private"), INTERNAL("internal"), FILE("file"), PACKAGE(
            "package"), // package+internal/internal+package表包及其子包可访问
    // 类成员
    STATIC("static"), // 不可变,
    //  方法加const表只读方法,
    //      只能调用本类成员的只读(const)方法
    //      能改变成员引用的对象或值
    //  属性/参数加const
    //      只能调用本属性/参数的const的方法
    //      成员可以被写
    CONST("const"), // 不可变
    // 属性加final
    //  只能调用本属性不可写, 可以调用其写方法
    //  不能改变成员的值
    // 类加final
    //   可以继承但方法不可重写, 也不可写父类成员变量(只能在构造函数初始化), 父类成员只能读, 可以调用非const方法
    FINAL("final"), // 类不可继承/方法不可重写
    SEALED("sealed"),

    // 类成员-不好说
    // FIELD("field"),
    // METHOD("method"),
    // 重写运算符, 以实现接口的形式调用函数式方程的形式, 还是以实现Cpp重写运算符的形式的形式
    // 重载接口
    //  缺点: 涉及自举(自举, 不久意味着要写一堆库了吗?! 好累, 不想写...)
    //  优点: 方便编译(不过也要检查是否继承XX类, 然后再进行转换啦)
    // import system.util.function.operator.DigitalCalculable;
    // import system.util.function.operator.AdditionCalculable;
    // import system.util.function.operator.SubscriptionCalculable;
    // import system.util.function.operator.MultiplyCalculable;
    // import system.util.function.operator.DivideCalculable;
    // import system.util.function.operator.ReminderCalculable;

    // import system.util.function.operator.MinusCalculable;
    // import system.util.function.operator.PositionCalculable;

    // import system.util.function.operator.IncreasingCalculable;
    // import system.util.function.operator.DecreasingCalculable;

    // import system.util.function.operator.Assignable;
    // ...

    // import system.util.function.operator.BitCalculable;
    // import system.util.function.operator.OrBitCalculable;
    // import system.util.function.operator.AndBitCalculable;
    // import system.util.function.operator.XorBitCalculable;
    // import system.util.function.operator.CounterBitCalculable;

    // import system.util.function.operator.BitMovable;
    // import system.util.function.operator.LeftBitMovable;
    // import system.util.function.operator.SignedRightBitMovable;
    // import system.util.function.operator.UnsignedRightBitMovable;

    // import system.util.function.operator.Comparable;
    // import system.util.function.operator.EqualsComparable;
    // import system.util.function.operator.LargeEqualsComparable;
    // import system.util.function.operator.LessEqualsComparable;
    // import system.util.function.operator.LargeComparable;
    // import system.util.function.operator.LessComparable;

    // import system.util.function.operator.Cast<TargetType>;
    // operator方法
    //  优点: 声明方便, version2 便可完成
    //  缺点: 难以编译
    OPERATOR("operator"), // CONSTRUCTOR("constructor"),
    // 基本数据类型
    BOOL("bool"), CHAR("char"), INT8("int8"), UINT8("uint8"), INT16("int16"), UINT16("uint16"), INT32("int32"), UINT32(
            "uint32"), INT64("int64"), UINT64("uint64"), FLOAT32("float32"), FLOAT64("float64"),

    // SIGNED("signed"),
    // UNSIGNED("unsigned"),
    // 方法
    VOID("void"), RETURN("return"), // 声明函数/方法, 也可以是基础类型, 表示函数/方法类变量
    // Object#static int aaa(float)即callable<int,float>
    // Object#int aaa(float)即callable<int,float>
    // int aaa(float)即callable<int,float>
    // CALLABLE("callable"),
    // 控制结构
    IF("if"), ELSE("else"), SWITCH("switch"), CASE("case"), DEFAULT("default"), WHILE("while"), DO("do"), FOR(
            "for"), // 用于for-each
    // for(i collectionIn list){
    // }
    // for(i collectionIn Arrays.range(1,2)){
    //
    // }
    // collectionIn 也可以作为运算符被重载吧?
    // if(i collectionIn set){}
    IN("collectionIn"), BREAK("break"), CONTINUE("continue"), // GOTO("goto"),, 不支持
    // 直接量
    TRUE("true"), FALSE("false"), NULL("null"), // 自动变量
    VAR("var"), // 实例化对象
    NEW("new"), // 不好搞-反射
    // instance of
    IS("is"), // 不好搞-异常
    THROW("throw"), THROWS("throws"), TRY("try"), CATCH("catch"), //try-with-resource
    WITH("with"), FINALLY("finally"), // TODO 不好搞-多线程 为确定
    // 其他
    NATIVE("native");
    private final String value;

    Keyword(String value) {
        this.value = value;
    }

    public static Keyword get(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        for (Keyword value : Keyword.values()) {
            if (Objects.equals(value.getValue(), name)) {
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
