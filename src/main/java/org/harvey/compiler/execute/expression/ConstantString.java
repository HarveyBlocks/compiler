package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.command.CompileProperties;
import org.harvey.compiler.common.util.ByteUtil;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

import java.util.Arrays;

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

    public static ConstantString constantString(SourceString source) {
        String value = source.getValue();
        SourceType type = source.getType();
        ConstantType constantType = ConstantType.constantType(type, value);
        if (constantType == null) {
            return null;
        }
        return new ConstantString(source.getPosition(), stringToBytes(value, constantType), constantType);
    }

    private static byte[] boolToBytes(String value) {
        byte[] bytes = new byte[1];
        boolean isTrue = Keyword.TRUE.equals(value);
        boolean isFalse = Keyword.FALSE.equals(value);
        if (isTrue == isFalse) {
            throw new CompilerException(value + " is not a bool");
        }
        bytes[0] = (byte) (isTrue ? 0 : 1);
        return bytes;
    }

    private static byte[] stringToBytes(
            String value, ConstantType type) {
        switch (type) {
            case STRING:
                return LiterallyConstantUtil.stringData(value);
            case CHAR:
                return LiterallyConstantUtil.charData(value);
            case BOOL:
                return boolToBytes(value);
            case INT32:
            case INT64:
            case FLOAT32:
            case FLOAT64:
                return numberToBytes(value);
            case NULL:
                return new byte[0];
            default:
                return null;
        }
    }

    private static byte[] numberToBytes(String value) {
        return value.getBytes(CompileProperties.NUMBER_CHARSET);
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

    @Override
    public String show() {
        if (type == ConstantType.STRING) {
            return '"' + new String(this.data) + '"';
        } else if (type == ConstantType.CHAR) {
            return '\'' + new String(this.data) + '\'';
        } else if (type == ConstantType.BOOL) {
            return (this.data[0] != 0) + "";
        } else if (type == ConstantType.FLOAT64) {
            return "float " + Arrays.toString(this.data);
        } else {
            return ByteUtil.phaseRawBytes(this.data) + "";
        }
    }

}
