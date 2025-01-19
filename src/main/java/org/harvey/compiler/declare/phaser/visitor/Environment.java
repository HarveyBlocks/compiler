package org.harvey.compiler.declare.phaser.visitor;

import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 22:31
 */
// 描述环境
public enum Environment {
    FILE,
    ENUM,
    CLASS,
    INTERFACE,
    ABSTRACT_CLASS,
    ABSTRACT_STRUCT,
    STRUCT;// 要第一行的, 不是第一行的, 还不行(生气);

    public boolean isComplexStructure() {
        switch (this) {
            case FILE:
                return false;
            case ENUM:
            case ABSTRACT_CLASS:
            case CLASS:
            case INTERFACE:
            case STRUCT:
                return true;
            default:
                throw new CompilerException("Unknown environment");
        }
    }
}