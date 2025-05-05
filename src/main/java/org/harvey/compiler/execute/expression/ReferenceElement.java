package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.RawType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 表达式中的引用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-27 00:06
 */
@Getter
public class ReferenceElement extends ExpressionElement implements RawType, ItemString {
    private final ReferenceType type;
    private final int reference;

    public ReferenceElement(SourcePosition position, ReferenceType type, int reference) {
        super(position);
        this.reference = reference;
        if (reference < 0) {
            throw new CompilerException("reference can not be minus", new IllegalArgumentException());
        }
        this.type = type;
    }

    public static ReferenceElement of(KeywordString keywordString) {
        return new ReferenceElement(keywordString.getPosition(), ReferenceType.KEYWORD,
                keywordString.getKeyword().ordinal()
        );
    }

    public static ReferenceElement of(NormalOperatorString operatorString) {
        return new ReferenceElement(operatorString.getPosition(), ReferenceType.OPERATOR,
                operatorString.getValue().ordinal()
        );
    }

    public static ReferenceElement ofConstructor(SourcePosition position) {
        return new ReferenceElement(position, ReferenceType.CONSTRUCTOR, 0);
    }

    public static ReferenceElement ofCast(SourcePosition position) {
        return new ReferenceElement(position, ReferenceType.CAST_OPERATOR, 0);
    }

    public static boolean equals(ReferenceElement a, ReferenceElement b) {
        return a.type == b.type && a.reference == b.reference;
    }


    public Keyword keyword() {
        if (type != ReferenceType.KEYWORD) {
            return null;
        }
        Keyword[] values = Keyword.values();
        return reference >= values.length ? null : values[reference];
    }

    public Operator operator() {
        if (type != ReferenceType.OPERATOR) {
            return null;
        }
        Operator[] values = Operator.values();
        return reference >= values.length ? null : values[reference];
    }

    @Override
    public String toString() {
        return getPosition() + "" + getReference();
    }


    public static class Serializer implements StreamSerializer<ReferenceElement> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ReferenceElement in(InputStream is) {
            HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 3, 4, 20);
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            ReferenceType type = ReferenceType.values()[(int) headMaps[0].getUnsignedValue()];
            int reference = (int) headMaps[1].getUnsignedValue();
            return new ReferenceElement(sp, type, reference);
        }

        @Override
        public int out(OutputStream os, ReferenceElement src) {
            return StreamSerializerUtil.writeHeads(
                    os,
                    new HeadMap(src.type.ordinal(), 4).inRange(true, "reference type"),
                    new HeadMap(src.reference, 20).inRange(true, "reference")
            ) + SOURCE_POSITION_SERIALIZER.out(os, src.getPosition());
        }
    }
}
