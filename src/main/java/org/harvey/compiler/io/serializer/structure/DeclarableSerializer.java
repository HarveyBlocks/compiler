package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.declare.EmbellishSourceString;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 13:06
 */
public class DeclarableSerializer extends AbstractSerializer<Declarable> {
    public DeclarableSerializer(InputStream is, OutputStream os) {
        super(is, os);
    }

    @Override
    public void serialize(Declarable origin) throws IOException {
        SourcePosition start = origin.getStart();
        SourceTextContext permissions = origin.getPermissions();
        EmbellishSourceString embellish = origin.getEmbellish();
        SourceTextContext type = origin.getType();
        SourceString identifier = origin.getIdentifier();
        byte embellishCode = embellishCode(embellish);
        Serializes.notTooMuch(start.getColumn(), "col", CompileFileConstants.MAX_COL);
        Serializes.notTooMuch(start.getRaw(), "raw", CompileFileConstants.MAX_RAW);
        Serializes.notTooMuch(permissions.size(), "permission size", 63);
        Serializes.notTooMuch(embellishCode, "embellish", 15);
        Serializes.notTooMuch(type.size(), "type source string", 4095);
        SerializableData head = Serializes.makeHead(
                new HeadMap(start.getColumn(), CompileFileConstants.COL_BC),
                new HeadMap(start.getRaw(), CompileFileConstants.RAW_BC),
                new HeadMap(permissions.size(), 8),
                new HeadMap(embellishCode, 4),
                new HeadMap(type.size(), 12)
        );
        // TODO
        // declare是可以检查的吗qwq
        // class, A, B,C都可以检查
        // identifier无法解析出expression 也没事的
        new SourceStringSerializer(is, os).serialize(identifier);
    }

    private byte embellishCode(EmbellishSourceString embellishSourceString) {
        return new Embellish(embellishSourceString).getCode();
    }

    @Override
    public Declarable deserialize() {
        // TODO

        return null;
    }
}
