package org.harvey.compiler.io.source;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;

import java.io.InputStream;
import java.io.OutputStream;

import static org.harvey.compiler.io.serializer.StreamSerializerUtil.readNumber;
import static org.harvey.compiler.io.serializer.StreamSerializerUtil.writeNumber;

/**
 * 源码中的行列
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 12:22
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class SourcePosition implements Cloneable, Comparable<SourcePosition> {
    public static final SourcePosition UNKNOWN = new SourcePosition(-1, -1);
    public static final SourcePosition PROPERTY = new SourcePosition(-2, -2);
    public static final SourcePosition NOT_EXIST = new SourcePosition(-3, -3);
    /**
     * 行, 第几行
     */
    private int raw;
    /**
     * 列, 行中的第几个字
     */
    private int column;

    /**
     * @return 移动后的新值
     */
    public static SourcePosition moveToEnd(SourcePosition start, String value) {
        int lineCounter = 0;
        int fromIndex = 0;
        int index;
        while (true) {
            index = value.indexOf(SourceFileConstant.LINE_SEPARATOR, fromIndex);
            if (index < 0) {
                break;
            }
            fromIndex = index + SourceFileConstant.LINE_SEPARATOR.length();
            lineCounter++;
        }
        return new SourcePosition(
                start.getRaw() + lineCounter,
                value.length() + (lineCounter == 0 ? start.getColumn() : -fromIndex)
        );
    }

    public void rawIncreasing() {
        raw++;
        column = 0;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return clone(0, 0);
    }

    public void columnIncreasing() {
        column++;
    }

    public void columnDecreasing() {
        column--;
    }

    public void columnAdding(int delta) {
        column += delta;
    }

    public SourcePosition clone(int rawDelta, int columnDelta) {
        return new SourcePosition(raw + rawDelta, column + columnDelta);
    }

    @Override
    public String toString() {
        return "[" + raw + ":" + column + "]";
    }

    @Override
    public int compareTo(SourcePosition sp) {
        if (sp.unknown() || this.unknown()) {
            throw new CompilerException("can not compare");
        }
        if (this.raw == sp.raw) {
            return this.column - sp.column;
        }
        return this.raw - sp.raw;
    }

    public long code() {
        return this.unknown() ? -1L : (long) raw << 32 | column;
    }

    public boolean before(SourcePosition sp) {
        return this.compareTo(sp) < 0;
    }

    public boolean after(SourcePosition sp) {
        return this.compareTo(sp) > 0;
    }

    public boolean unknown() {
        return this.raw == UNKNOWN.raw || this.column == UNKNOWN.column;
    }

    public static class Serializer implements StreamSerializer<SourcePosition> {
        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public SourcePosition in(InputStream is) {
            int col = (int) readNumber(is, 32, false);
            int raw = (int) readNumber(is, 32, false);
            return new SourcePosition(raw, col);
        }

        @Override
        public int out(OutputStream os, SourcePosition src) {
            return writeNumber(os, src.getColumn(), 32, false) +
                   writeNumber(os, src.getRaw(), 32, false);
        }
    }
}
