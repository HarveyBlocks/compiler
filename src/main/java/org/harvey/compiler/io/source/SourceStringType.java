package org.harvey.compiler.io.source;

/**
 * 源码中的每一个部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:26
 */
public enum SourceStringType {
    SIGN,// 符号, 不包含运算符的一些特殊符号, 仅包含`{`, `}`, `;`
    STRING, // 源码中是字符串
    SINGLE_LINE_COMMENTS, // 源码中是单行注释
    LINE_SEPARATOR, MULTI_LINE_COMMENTS,// 源码中是多行注释
    MIXED,// 多种类型混合, 不包含字符串和注释
    OPERATOR, // 完成分解的独立运算符
    ITEM, // 包含CHAR, Number, Word
    CHAR, // 源码中是常量

    BOOL,// 源码中是常量, 是关键字
    INT32, // 整形
    INT64, // 整形
    FLOAT32, // 浮点数
    FLOAT64, // 浮点数
    IGNORE_IDENTIFIER,  // 用于忽略的字符传, 例如`_`
    SCIENTIFIC_NOTATION_FLOAT32, // 科学计数法
    SCIENTIFIC_NOTATION_FLOAT64, // 科学计数法
    IDENTIFIER, // 源码中是标识符
    // VARIABLE_IDENTIFIER, // 变量
    // CLASS_IDENTIFIER,  // 从import中获取到的? 后面紧跟<>, 是为Class?怎么区分类和变量?
    // CALLABLE_IDENTIFIER, // 函数名[<Generic0,Generic1....>](Arg0,[..., ArgN])
    // 1. 先判断是不是有<>
    //  有:
    //  1. 判断是不是有()有->函数
    //                 无->类
    //  无:
    //  2. 判断是不是有()有->函数
    //                  无->
    // 如果是变量, 是指向局部变量的?
    //           是指向成员字段的?
    //           是指向成员方法的(不带括号, 也就要求成员字段方法不重名)?
    //           是指向文件变量的? 是指向文件函数的? 是指向外部类的?
    // 指向什么只能通过上下文确认
    KEYWORD,//关键字


    // 暂时的, 可能删除
    GOTO,//关键字
    LABEL, // 关键字
    ASSIGN_TEMP,
    ;// 关键字

    public static SourceStringType get(int ordinal) {
        return values()[ordinal];
    }
}