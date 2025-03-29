package org.harvey.compiler.type.generic.using;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.define.GenericDefine;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 区别于{@link ParameterizedType}, 特别地, 有final和const修饰共同组成的类型
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 20:44
 */
@AllArgsConstructor
@Getter
public class LocalParameterizedType {
    private final SourcePosition constMark;
    // 不重要
    private final SourcePosition finalMark;
    private final ParameterizedType<ReferenceElement> type;

    public boolean isMarkConst() {
        return constMark != null;
    }

    public boolean isMarkFinal() {
        return finalMark != null;
    }

    public static class Serializer implements StreamSerializer<LocalParameterizedType> {
        private static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        private static final GenericDefine.GenericUsingSerializer PARAMETERIZED_TYPE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.GenericUsingSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public LocalParameterizedType in(InputStream is) {
            long l = StreamSerializerUtil.readNumber(is, 8, false);
            SourcePosition constMark = (l & 1) == 1 ? SOURCE_POSITION_SERIALIZER.in(is) : null;
            SourcePosition finalMark = (l & 2) == 2 ? SOURCE_POSITION_SERIALIZER.in(is) : null;
            ParameterizedType<ReferenceElement> type = PARAMETERIZED_TYPE_SERIALIZER.in(is);
            return new LocalParameterizedType(constMark, finalMark, type);
        }

        @Override
        public int out(OutputStream os, LocalParameterizedType src) {
            int code = 0;
            if (src.constMark != null) {
                code |= 1;
            }
            if (src.finalMark != null) {
                code |= 2;
            }
            int length = StreamSerializerUtil.writeNumber(os, code, 8, false);
            if (src.constMark != null) {
                SOURCE_POSITION_SERIALIZER.out(os, src.constMark);
            }
            if (src.finalMark != null) {
                SOURCE_POSITION_SERIALIZER.out(os, src.finalMark);
            }
            length += PARAMETERIZED_TYPE_SERIALIZER.out(os, src.type);
            return length;
        }
    }
}
