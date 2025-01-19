package org.harvey.compiler.io.serializer.structure;

import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.depart.DepartedPart;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 21:22
 */
@Getter
public class DepartedPartSerializer extends AbstractSerializer<DepartedPart> {
    private static final int STATEMENT_LENGTH_BC = CompileFileConstants.DEPARTED_PART_STATEMENT_LENGTH_BC;
    private static final int BODY_LENGTH_BC = CompileFileConstants.DEPARTED_PART_BODY_LENGTH_BC;
    private static final int MAX_STATEMENT_LENGTH = CompileFileConstants.MAX_DEPARTED_PART_STATEMENT_LENGTH;
    private static final int MAX_BODY_LENGTH = CompileFileConstants.MAX_DEPARTED_PART_BODY_LENGTH;
    private static final int HEAD_BC = STATEMENT_LENGTH_BC + BODY_LENGTH_BC;
    private final SourceStringSerializer sss;

    public DepartedPartSerializer(InputStream is, OutputStream os) {
        super(is, os);
        sss = new SourceStringSerializer(is, os);
    }

    @Override
    public void serialize(DepartedPart origin) throws IOException {
        if (origin == null) {
            return;
        }
        final int statementSize;
        final int bodySize;
        SourceTextContext statement = origin.getStatement();
        SourceTextContext body = origin.getBody();
        statementSize = statement.size();
        bodySize = body.size();
        Serializes.notTooMuch(statementSize, "departed part statement size", MAX_STATEMENT_LENGTH);
        Serializes.notTooMuch(bodySize, "departed part body size", MAX_BODY_LENGTH);
        SerializableData head = Serializes.makeHead(
                new HeadMap(statementSize, STATEMENT_LENGTH_BC),
                new HeadMap(bodySize, BODY_LENGTH_BC)
        );
        os.write(head.data());
        statement.forEach(ss -> {
            try {
                sss.serialize(ss);
            } catch (IOException e) {
                throw new CompilerException(e);
            }
        });
        body.forEach(ss -> {
            try {
                sss.serialize(ss);
            } catch (IOException e) {
                throw new CompilerException(e);
            }
        });
    }

    @Override
    public DepartedPart deserialize() throws IOException {
        SourceTextContext statement = new SourceTextContext();
        SourceTextContext body = new SourceTextContext();
        SerializableData head = new SerializableData(is.readNBytes(Serializes.bitCountToByteCount(HEAD_BC)));
        HeadMap[] headMaps = head.phaseHeader(STATEMENT_LENGTH_BC, BODY_LENGTH_BC);
        assert headMaps.length == 2;
        int statementSize = (int) headMaps[0].getValue();
        while (statementSize-- > 0) {
            statement.add(sss.deserialize());
        }
        int bodySize = (int) headMaps[1].getValue();
        while (bodySize-- > 0) {
            body.add(sss.deserialize());
        }
        return new DepartedPart(statement, body);
    }
}
