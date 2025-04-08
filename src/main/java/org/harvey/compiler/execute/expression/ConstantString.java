package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 在表达式中的常量
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:48
 */
@Getter
public class ConstantString extends ExpressionElement implements ItemString {

    // 大端
    private final byte[] data;
    private final ConstantType type;

    public ConstantString(SourcePosition sp, byte[] data, ConstantType type) {
        super(sp);
        this.data = data;
        this.type = type;
        legalDataLength();
    }

    private void legalDataLength() {
        int length = data.length;
        switch (type) {
            case CHAR:
            case STRING:
                break;
            case BOOL:
                if (length != 1) {
                    throw new CompilerException("Illegal data length of " + type);
                }
                break;
            case FLOAT32:
            case INT32:
                if (length != 4) {
                    throw new CompilerException("Illegal data length of " + type);
                }
                break;
            case FLOAT64:
            case INT64:
                if (length != 8) {
                    throw new CompilerException("Illegal data length of " + type);
                }
                break;
        }
    }

    public enum ConstantType {
        STRING, INT32, INT64, FLOAT32, FLOAT64, BOOL, CHAR
    }


}
