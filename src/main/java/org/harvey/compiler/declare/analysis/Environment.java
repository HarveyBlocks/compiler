package org.harvey.compiler.declare.analysis;

/**
 * 在解析一个成员时, 需要直到其外部的信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-19 22:30
 */
public enum Environment {
    FILE, INTERFACE, CLASS, STRUCT, ENUM, ABSTRACT_CLASS, ABSTRACT_STRUCT
}
