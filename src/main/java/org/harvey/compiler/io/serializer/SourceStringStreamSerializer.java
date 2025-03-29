package org.harvey.compiler.io.serializer;

import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用于将{@link SourceString}序列化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 15:18
 */
public class SourceStringStreamSerializer implements StreamSerializer<SourceString> {
    private static final StringStreamSerializer SSS = StreamSerializerRegister.get(StringStreamSerializer.class);
    private static final SourcePosition.Serializer SPS = StreamSerializerRegister.get(SourcePosition.Serializer.class);

    static {
        StreamSerializerRegister.register(new SourceStringStreamSerializer());
    }

    private SourceStringStreamSerializer() {
    }

    @Override
    public SourceString in(InputStream is) {
        SourceType type = SourceType.values()[(int) StreamSerializerUtil.readNumber(is, 8, false)];
        String value = SSS.in(is);
        SourcePosition position = SPS.in(is);
        return new SourceString(type, value, position);
    }

    @Override
    public int out(OutputStream os, SourceString src) {
        return StreamSerializerUtil.writeNumber(os, src.getType().ordinal(), 8, false) + SSS.out(os, src.getValue()) +
               SPS.out(os, src.getPosition());
    }
}
