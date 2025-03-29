package org.harvey.compiler.io.source;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.common.util.BinaryCharset;
import org.harvey.compiler.common.util.ByteUtil;

/**
 * 源码中的每一个部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:25
 */
@Getter
@EqualsAndHashCode
public class SourceString implements SourcePositionSupplier {

    private final SourceType type;

    private final String value;
    private final SourcePosition position;

    public SourceString(SourceType type, String value, SourcePosition position) {
        this.type = type;
        this.value = value;
        this.position = (SourcePosition) position.clone();
    }


    @Override
    public String toString() {
        String string = value;
        byte[] bytes;
        switch (type) {
            case INT32:
                bytes = string.getBytes(BinaryCharset.INSTANCE);
                string = String.valueOf(ByteUtil.phaseRawBytes4(bytes));
                break;
            case INT64:
                bytes = string.getBytes(BinaryCharset.INSTANCE);
                string = String.valueOf(ByteUtil.phaseRawBytes8(bytes));
                break;
            case FLOAT32:
                bytes = string.getBytes(BinaryCharset.INSTANCE);
                string = String.valueOf(Float.intBitsToFloat(ByteUtil.phaseRawBytes4(bytes)));
                break;
            case FLOAT64:
                bytes = string.getBytes(BinaryCharset.INSTANCE);
                string = String.valueOf(Double.longBitsToDouble(ByteUtil.phaseRawBytes8(bytes)));
                break;
        }
        return position + " `" + string + '`';
    }
}
