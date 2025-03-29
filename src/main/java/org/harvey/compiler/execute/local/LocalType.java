package org.harvey.compiler.execute.local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.define.LocalTypeDefinition;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ListIterator;

/**
 * 可序列化的局部变量
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-26 11:04
 */
@AllArgsConstructor
@Getter
public class LocalType {
    private final SourcePosition constPosition;
    private final SourcePosition finalPosition;
    private final ParameterizedType<ReferenceElement> sourceType;

    public LocalType(LocalTypeDefinition definition, IdentifierManager manager) {
        this.constPosition = definition.getMarkConst();
        this.finalPosition = definition.getMarkFinal();
        ListIterator<SourceString> iterator = definition.getTypeParameter().listIterator();
        ReferenceElement reference = GenericFactory.rawType2Reference(definition.getRawType(), manager);
        this.sourceType = GenericFactory.parameterizedType(reference, iterator, manager);
    }

    public static SourcePosition getPositionIfNotNull(SourceString source) {
        return source == null ? null : source.getPosition();
    }

    public boolean isConst() {
        return constPosition != null;
    }

    public boolean isFinal() {
        return finalPosition != null;
    }

    public static class Serializer implements StreamSerializer<LocalType> {
        private static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        private static final GenericDefine.GenericUsingSerializer TYPE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.GenericUsingSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {

        }

        private static SourcePosition outEmbellishPosition(SourcePosition embellishPosition) {
            return embellishPosition == null ? SourcePosition.UNKNOWN : embellishPosition;
        }

        private static SourcePosition inEmbellishPosition(SourcePosition embellishPosition) {
            return SourcePosition.UNKNOWN.equals(embellishPosition) ? null : embellishPosition;
        }

        @Override
        public LocalType in(InputStream is) {
            SourcePosition constPosition = inEmbellishPosition(SOURCE_POSITION_SERIALIZER.in(is));
            SourcePosition finalPosition = inEmbellishPosition(SOURCE_POSITION_SERIALIZER.in(is));
            ParameterizedType<ReferenceElement> type = TYPE_SERIALIZER.in(is);
            return new LocalType(constPosition, finalPosition, type);
        }

        @Override
        public int out(OutputStream os, LocalType src) {
            return SOURCE_POSITION_SERIALIZER.out(os, outEmbellishPosition(src.constPosition)) +
                   SOURCE_POSITION_SERIALIZER.out(os, outEmbellishPosition(src.finalPosition)) +
                   TYPE_SERIALIZER.out(os, src.sourceType);
        }
    }
}
