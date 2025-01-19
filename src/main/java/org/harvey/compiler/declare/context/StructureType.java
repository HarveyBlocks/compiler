package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-05 21:40
 */
@Getter
@AllArgsConstructor
public enum StructureType {
    ENUM(Keyword.ENUM, Environment.ENUM),
    CLASS(Keyword.CLASS, Environment.CLASS),
    STRUCT(Keyword.STRUCT, Environment.STRUCT),
    ABSTRACT_CLASS(Keyword.CLASS, Environment.ABSTRACT_CLASS),
    ABSTRACT_STRUCT(Keyword.STRUCT, Environment.ABSTRACT_STRUCT), /* ? abstract 必须是const, 方法必须是const,*/
    INTERFACE(Keyword.INTERFACE, Environment.INTERFACE);
    private final Keyword keyword;
    private final Environment environment;

    public static StructureType get(Keyword keyword) {
        switch (keyword) {
            case CLASS:
                return CLASS;
            case ENUM:
                return ENUM;
            case INTERFACE:
                return INTERFACE;
            case STRUCT:
                return STRUCT;
            default:
                return null;
        }
    }

    public static StructureType get(SourceTextContext type) {
        if (type == null || type.size() != 1) {
            return null;
        }
        return StructureType.get(Keyword.get(type.getFirst().getValue()));
    }


    public static StructureType embellishAbstract(StructureType type) {
        switch (type) {
            case CLASS:
            case ABSTRACT_CLASS:
                return ABSTRACT_CLASS;
            case STRUCT:
            case ABSTRACT_STRUCT:
                return ABSTRACT_STRUCT;
            case INTERFACE:
                return INTERFACE;
            default:
                throw new CompilerException(type.getKeyword().getValue() + " can not embellish with abstract");
        }
    }
}

