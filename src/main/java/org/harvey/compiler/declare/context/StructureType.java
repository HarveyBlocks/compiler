package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.Environment;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;


/**
 * {@link StructureContext}的类型信息, 也用于RawType的构建
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-05 21:40
 */
@Getter
@AllArgsConstructor
public enum StructureType {
    ENUM(Keyword.ENUM, Environment.ENUM), CLASS(Keyword.CLASS, Environment.CLASS), STRUCT(
            Keyword.STRUCT, Environment.STRUCT), ABSTRACT_CLASS(
            Keyword.CLASS, Environment.ABSTRACT_CLASS), ABSTRACT_STRUCT(
            Keyword.STRUCT, Environment.ABSTRACT_STRUCT), /* ? abstract 必须是const, 方法必须是const,*/

    INTERFACE(Keyword.INTERFACE, Environment.INTERFACE),
    KEYWORD_BASIC(null, null), ALIAS(
            Keyword.ALIAS, null), GENERIC_DEFINE(null, null);
    private final Keyword keyword;
    private final Environment environment;

    public static StructureType get(Keyword keyword) {
        if (Keywords.isBasicType(keyword)) {
            return StructureType.KEYWORD_BASIC;
        }
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


}

