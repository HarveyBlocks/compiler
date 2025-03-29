package org.harvey.compiler.execute.local;

import lombok.Getter;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.ExpressionElementType;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-11 21:42
 */
@Getter
public class LocalTableElementDeclare extends ExpressionElement {

    public static final ExpressionElementType TYPE = ExpressionElementType.LOCAL_VARIABLE;
    public static final Serializer SERIALIZER = StreamSerializerRegister.get(
            Serializer.class);
    private final int typeReference;
    private int start;

    public LocalTableElementDeclare(SourcePosition position, int start, int typeReference) {
        super(position);
        this.start = start;
        this.typeReference = typeReference;
    }

    public void resetStart(LambdaVariableManager lambdaVariableManager) {
        this.start = lambdaVariableManager.resetStart(start);
    }

    public LocalVariableManager.LocalVariableType getLocalVariableType() {
        if (typeReference <= LocalVariableManager.LocalVariableType.REFERENCE.ordinal()) {
            return LocalVariableManager.LocalVariableType.values()[typeReference];
        }
        return LocalVariableManager.LocalVariableType.REFERENCE;
    }

    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }

    public static class Serializer implements StreamSerializer<LocalTableElementDeclare> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        public static final StringStreamSerializer STRING_STREAM_SERIALIZER = StreamSerializerRegister.get(
                StringStreamSerializer.class);

        static {
            ExpressionElement.Serializer.register(
                    TYPE.ordinal(), new Serializer(), LocalTableElementDeclare.class);
        }

        private Serializer() {
        }

        @Override
        public LocalTableElementDeclare in(InputStream is) {
            HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 4, 3, 12, 12);
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            return new LocalTableElementDeclare(sp, (int) headMaps[0].getUnsignedValue(),
                    (int) headMaps[1].getUnsignedValue()
            );
        }

        @Override
        public int out(OutputStream os, LocalTableElementDeclare src) {
            return StreamSerializerUtil.writeHeads(os, new HeadMap(src.start, 12).inRange(true, "start"),
                    new HeadMap(src.typeReference, 12).inRange(true, "type reference")
            ) + SOURCE_POSITION_SERIALIZER.out(os, src.getPosition());
        }
    }

}
