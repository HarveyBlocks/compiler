package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.io.serializer.PolymorphismSerializable;
import org.harvey.compiler.io.serializer.PolymorphismStreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 在表达式中的从成员
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:45
 */
@AllArgsConstructor
@Getter
public abstract class ExpressionElement extends PolymorphismSerializable implements SourcePositionSupplier {

    private SourcePosition position;

    public static class Serializer extends PolymorphismStreamSerializer<ExpressionElement> {
        private static final Map<Byte, StreamSerializer<? extends ExpressionElement>> SERIALIZER_MAP = new HashMap<>();
        private static final Map<String, Byte> CODE_MAP = new HashMap<>();

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        public static <T extends ExpressionElement> void register(
                int code, StreamSerializer<T> serializer,
                Class<T> type) {
            PolymorphismStreamSerializer.register(SERIALIZER_MAP, (byte) code, serializer, CODE_MAP, type.getName());
        }

        @Override
        public ExpressionElement in(InputStream is) {
            return PolymorphismStreamSerializer.in(SERIALIZER_MAP, is);
        }

        @Override
        public int out(OutputStream os, ExpressionElement src) {
            return PolymorphismStreamSerializer.out(CODE_MAP, os, src);
        }
    }

//    public static class OnlyFileStatementSerializer implements StreamSerializer<ExpressionElement> {}
//        private static final Map<Integer, StreamSerializer<? extends ExpressionElement>> MAP = new HashMap<>();
//
//        private OnlyFileStatementSerializer() {
//        }
//
//        static {
//            StreamSerializer.register(new OnlyFileStatementSerializer());
//        }
//
//
//        /**
//         *  同时完成 {@link StreamSerializer#register(StreamSerializer)}的任务
//         */
//        public static void register(int code, StreamSerializer<? extends ExpressionElement> serializer) {
//            if (MAP.containsKey(code)) {
//                throw new CompilerException("code of " + code + " is repeated with" + MAP.get(code));
//            }
//            MAP.put(code, serializer);
//            StreamSerializer.register(serializer);
//        }
//
//        @Override
//        public ExpressionElement collectionIn(InputStream is) {
//            int code;
//            try {
//                code = is.readNBytes(1)[0];
//            } catch (IOException e) {
//                throw new CompilerFileReadException(e);
//            }
//            ExpressionElement element = MAP.get(code).collectionIn(is);
//            if (element == null) {
//                throw new CompilerException("Unknown Expression Element OnlyFileStatementSerializer");
//            }
//            return element;
//        }
//
//        @Override
//        public int out(OutputStream os, ExpressionElement src) {
//            return src.out(os);
//        }
//    }
}
