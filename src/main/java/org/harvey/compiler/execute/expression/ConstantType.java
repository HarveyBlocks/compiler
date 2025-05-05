package org.harvey.compiler.execute.expression;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.io.source.SourceType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 01:11
 */
public enum ConstantType {
    STRING, INT32, INT64, FLOAT32, FLOAT64, BOOL, NULL, CHAR;

    public static ConstantType constantType(SourceType constantSourceType, String value) {
        switch (constantSourceType) {
            case STRING:
                return ConstantType.STRING;
            case CHAR:
                return ConstantType.CHAR;
            case BOOL:
                return ConstantType.BOOL;
            case INT32:
                return ConstantType.INT32;
            case INT64:
                return ConstantType.INT64;
            case FLOAT32:
                return ConstantType.FLOAT32;
            case FLOAT64:
                return ConstantType.FLOAT64;
            case KEYWORD:
                if (Keyword.NULL.equals(value)) {
                    return ConstantType.NULL;
                }
            default:
                return null;
        }
    }
}
