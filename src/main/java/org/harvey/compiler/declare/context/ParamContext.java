package org.harvey.compiler.declare.context;

import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.serializer.SourceStringStreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 需要被序列化的的Callable的Param对象
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 13:48
 */
@Getter
public class ParamContext {
    private final LocalType localType;
    private final IdentifierString identifier;
    private final int defaultAssign;

    public ParamContext(LocalType localType, IdentifierString identifier, int defaultAssign) {
        this.localType = localType;
        this.identifier = identifier;
        this.defaultAssign = defaultAssign;
    }



    public static class Serializer implements StreamSerializer<ParamContext> {
        private static final SourceStringStreamSerializer SOURCE_SERIALIZER = StreamSerializerRegister.get(
                SourceStringStreamSerializer.class);
        private static final IdentifierString.Serializer IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
                IdentifierString.Serializer.class);
        private static final LocalType.Serializer LOCAL_TYPE_SERIALIZER = StreamSerializerRegister.get(
                LocalType.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ParamContext in(InputStream is) {
            LocalType type = LOCAL_TYPE_SERIALIZER.in(is);
            IdentifierString identifierString = IDENTIFIER_STRING_SERIALIZER.in(is);
            int defaultAssign = (int) StreamSerializerUtil.readNumber(is, 32, false);
            return new ParamContext(type, identifierString, defaultAssign);
        }

        @Override
        public int out(OutputStream os, ParamContext src) {
            return LOCAL_TYPE_SERIALIZER.out(os, src.localType) + IDENTIFIER_STRING_SERIALIZER.out(os, src.identifier) +
                   StreamSerializerUtil.writeNumber(os, src.defaultAssign, 32, false);
        }
    }

}
