package org.harvey.compiler.type.generic.link;

import lombok.AllArgsConstructor;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-16 20:22
 */
@AllArgsConstructor
public class LocalParameterizedLink {
    private final SourcePosition constMark;
    private final SourcePosition finalMark;
    private final LinkedGenericDefine.Using type;

    public static class Serializer implements StreamSerializer<LocalParameterizedLink> {
        private static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        private static final LinkedGenericDefine.UsingSerializer USING_LINK_SERIALIZER = StreamSerializerRegister.get(
                LinkedGenericDefine.UsingSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }


        private Serializer() {
        }

        @Override
        public LocalParameterizedLink in(InputStream is) {
            long l = StreamSerializerUtil.readNumber(is, 8, false);
            SourcePosition constMark = (l & 1) == 1 ? SOURCE_POSITION_SERIALIZER.in(is) : null;
            SourcePosition finalMark = (l & 2) == 2 ? SOURCE_POSITION_SERIALIZER.in(is) : null;
            LinkedGenericDefine.Using type = USING_LINK_SERIALIZER.in(is);
            return new LocalParameterizedLink(constMark, finalMark, type);
        }

        @Override
        public int out(OutputStream os, LocalParameterizedLink src) {
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
            length += USING_LINK_SERIALIZER.out(os, src.type);
            return length;
        }
    }
}
