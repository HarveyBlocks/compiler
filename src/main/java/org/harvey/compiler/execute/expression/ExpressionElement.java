package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.PolymorphismSerializable;
import org.harvey.compiler.io.ss.PolymorphismStreamSerializer;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @date 2025-01-08 16:45
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public abstract class ExpressionElement extends PolymorphismSerializable {

    private SourcePosition position;


    public abstract int out(OutputStream os);

    public static class Serializer extends PolymorphismStreamSerializer<ExpressionElement> {
        private static final Map<Integer, StreamSerializer<? extends ExpressionElement>> MAP = new HashMap<>();

        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        public static void register(int code, StreamSerializer<? extends ExpressionElement> serializer) {
            PolymorphismStreamSerializer.register(MAP, code, serializer);
        }

        @Override
        public ExpressionElement in(InputStream is) {
            return PolymorphismStreamSerializer.in(MAP, is);
        }
    }

//    public static class Serializer implements StreamSerializer<ExpressionElement> {}
//        private static final Map<Integer, StreamSerializer<? extends ExpressionElement>> MAP = new HashMap<>();
//
//        private Serializer() {
//        }
//
//        static {
//            StreamSerializer.register(new Serializer());
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
//        public ExpressionElement in(InputStream is) {
//            int code;
//            try {
//                code = is.readNBytes(1)[0];
//            } catch (IOException e) {
//                throw new CompilerFileReaderException(e);
//            }
//            ExpressionElement element = MAP.get(code).in(is);
//            if (element == null) {
//                throw new CompilerException("Unknown Expression Element Serializer");
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
