package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 在表达式中的常量
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:48
 */
@Getter
public class ConstantString extends ExpressionElement {
    public static final ExpressionElementType TYPE = ExpressionElementType.CONSTANT;
    private static final StreamSerializer<ConstantString> SERIALIZER = StreamSerializerRegister.get(
            ConstantString.Serializer.class);
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

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public enum ConstantType {
        STRING, INT32, INT64, FLOAT32, FLOAT64, BOOL, CHAR
    }

    public static class Serializer implements StreamSerializer<ConstantString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);

        static {
            ExpressionElement.Serializer.register(TYPE.ordinal(), new ConstantString.Serializer(),
                    ConstantString.class
            );
        }

        private Serializer() {
        }

        @Override
        public ConstantString in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            ConstantType type;
            byte[] data;
            try {
                type = ConstantType.values()[is.readNBytes(1)[0]];
                long size = StreamSerializerUtil.readNumber(is, 16, false);
                data = is.readNBytes((int) size);
            } catch (IOException e) {
                throw new CompilerFileReadException(e);
            }
            return new ConstantString(sp, data, type);
        }

        @Override
        public int out(OutputStream os, ConstantString src) {
            return SOURCE_POSITION_SERIALIZER.out(os, src.getPosition()) +
                   StreamSerializerUtil.writeOneByte(os, (byte) src.getType().ordinal()) +
                   StreamSerializerUtil.writeNumber(os, src.getData().length, 16, false) +
                   StreamSerializerUtil.writeElements(os, src.getData());
        }


    }
}
