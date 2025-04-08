package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 数组初始化的表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:50
 */
@Getter
public class ArrayInitExpression extends ComplexExpression {
    public static final String END_MSG = "__array__init__end__";
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
            ComplexExpression.Serializer.register(TYPE.ordinal(), new ArrayInitExpression.Serializer(),
                    ArrayInitExpression.class
            );
        }

        private Serializer() {
        }

        @Override
        public ArrayInitExpression in(InputStream is) {
            HeadMap[] head = StreamSerializerUtil.readHeads(is, 4, 1, 31);
            boolean start = head[0].getUnsignedValue() != 0;
            int otherSide = (int) head[1].getUnsignedValue();
            return new ArrayInitExpression(start, otherSide);
        }

        @Override
        public int out(OutputStream os, ArrayInitExpression src) {
            return StreamSerializerUtil.writeHeads(os, new HeadMap(src.start ? 1 : 0, 1),
                    new HeadMap(src.otherSide, 31).inRange(true, "array init other size reference")
            );
        }
    }
}
