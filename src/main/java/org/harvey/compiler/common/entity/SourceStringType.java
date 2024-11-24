package org.harvey.compiler.common.entity;

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
    INT32, // 整形
    INT64, // 整形
    FLOAT32, // 浮点数
    FLOAT64, // 浮点数
    IGNORE_IDENTIFIER,  // 用于忽略的字符传, 例如`_`
    SCIENTIFIC_NOTATION_FLOAT32, // 科学计数法
    SCIENTIFIC_NOTATION_FLOAT64, // 科学计数法
    IDENTIFIER, // 源码中是标识符
    KEYWORD,//关键字

}