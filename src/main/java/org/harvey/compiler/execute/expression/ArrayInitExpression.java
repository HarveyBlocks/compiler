package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @date 2025-01-08 16:50
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class ArrayInitExpression extends ComplexExpression {
    public static final String END_MSG = "__struct__clone__end__";
    public static final int UNSURE_OTHER_SIDE = 0x7f_ff_ff_ff;
    public static final ComplexExpression.Type TYPE = ComplexExpression.Type.ARRAY_INIT;
    private final boolean start;//不是start, 就是end
    @Setter
    private int otherSide;


    public ArrayInitExpression(boolean start) {
        this(start, UNSURE_OTHER_SIDE);
    }

    private ArrayInitExpression(boolean start, int otherSide) {
        this.start = start;
        this.otherSide = otherSide;
    }

    public static class Serializer implements StreamSerializer<ArrayInitExpression> {


        static {
            ComplexExpression.Serializer.register(TYPE.ordinal(), new ArrayInitExpression.Serializer());
        }

        private Serializer() {
        }

        @Override
        public ArrayInitExpression in(InputStream is) {
            HeadMap[] head;
            try {
                head = new SerializableData(is.readNBytes(1)[0]).phaseHeader(1, 7);
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            boolean start = head[0].getValue() != 0;
            int otherSide = (int) head[1].getValue();
            return new ArrayInitExpression(start, otherSide);
        }

        @Override
        public int out(OutputStream os, ArrayInitExpression src) {
            try {
                os.write(Serializes.makeHead(new HeadMap(src.start ? 1 : 0, 1),
                        new HeadMap(src.otherSide, 7).inRange(true, "array init other size reference")).data());
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }
            return 1;
        }
    }
}
