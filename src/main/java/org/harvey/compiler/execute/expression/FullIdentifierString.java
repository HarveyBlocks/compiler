package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.serializer.StringStreamSerializer;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;
import org.harvey.compiler.type.generic.RawType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 在表达式中的全称, a.b.c.d
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-25 21:48
 */
@Getter
public class FullIdentifierString implements SourcePositionSupplier, RawType {
    private final SourcePosition position;
    /**
     * fullname, separator is .
     */
    private final String[] fullname;
    private final SourcePosition[] positionList;

    /**
     * @param sp can not be empty
     */
    public FullIdentifierString(SourcePosition[] sp, String[] fullname) {
        position = sp[0];
        this.positionList = sp;
        this.fullname = fullname;
    }

    public FullIdentifierString(SourcePosition sp, String fullname) {
        position = sp;
        this.positionList = new SourcePosition[]{sp};
        this.fullname = new String[]{fullname};
    }

    @Override
    public String toString() {
        return joinFullnameString(Operator.GET_MEMBER.getName());
    }


    public String get(int index) {
        return fullname[index];
    }

    public boolean empty() {
        return fullname.length == 0;
    }

    public int length() {
        return fullname.length;
    }

    public String joinFullnameString(String delimiter) {
        return String.join(delimiter, fullname);
    }

    public SourcePosition getPositionAt(int index) {
        return positionList[index];
    }

    public boolean valueEquals(FullIdentifierString fullname) {
        if (fullname == null) {
            return false;
        }
        return Arrays.equals(this.getFullname(), fullname.getFullname());
    }

    public IdentifierString cast2Identifier() {
        if (empty()) {
            throw new CompilerException("empty full name can not cast to IdentifierString");
        } else if (length() == 1) {
            return new IdentifierString(this.getPosition(), fullname[0]);
        } else {
            throw new AnalysisExpressionException(positionList[0], positionList[length() - 1],
                    "expected a single identifier"
            );
        }

    }

    public String[] getRange(int start, int end) {
        if (start > end) {
            return null;
        }
        String[] splice = new String[end - start];
        System.arraycopy(fullname, start, splice, 0, end - start);
        return splice;
    }

    public FullIdentifierString getRangeWithPosition(int start, int end) {
        if (start > end) {
            return null;
        }
        String[] strings = new String[end - start];
        System.arraycopy(fullname, start, strings, 0, end - start);
        SourcePosition[] positions = new SourcePosition[end - start];
        System.arraycopy(positionList, start, positions, 0, end - start);
        return new FullIdentifierString(positions, strings);
    }

    /**
     * @return Math.min(this.length (), other.length()) if all equals
     */
    public int firstDifferenceIndex(FullIdentifierString other) {
        return ArrayUtil.firstDifferenceIndex(this.fullname, other.fullname);
    }

    public static class Serializer implements StreamSerializer<FullIdentifierString> {
        public static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);
        public static final StringStreamSerializer STRING_STREAM_SERIALIZER = StreamSerializerRegister.get(
                StringStreamSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public FullIdentifierString in(InputStream is) {
            SourcePosition[] sp = StreamSerializerUtil.collectionIn(is, SOURCE_POSITION_SERIALIZER)
                    .toArray(SourcePosition[]::new);
            String value = STRING_STREAM_SERIALIZER.in(is);
            return new FullIdentifierString(sp, StringUtil.simpleSplit(value, "."));
        }

        @Override
        public int out(OutputStream os, FullIdentifierString src) {
            return StreamSerializerUtil.collectionOut(os, ArrayUtil.toList(src.getPositionList()),
                    SOURCE_POSITION_SERIALIZER
            ) +
                   STRING_STREAM_SERIALIZER.out(os, String.join(".", src.getFullname()));
        }
    }
}