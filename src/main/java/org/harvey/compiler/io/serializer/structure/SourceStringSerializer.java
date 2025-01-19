package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <ul>
 *     <li><p><span>源码分割设计? (48Bit+)</span></p>
 *         <ul>
 *             <li><span>raw ({@link CompileFileConstants#RAW_BC})</span></li>
 *             <li><span>col ({@link CompileFileConstants#COL_BC})</span></li>
 *             <li><span>TYPE ({@link CompileFileConstants#SOURCE_STRING_TYPE_ORDINAL_BC})</span></li>
 *             <li><span>字符串长 ({@link CompileFileConstants#MAX_SOURCE_STRING_LENGTH})</span></li>
 *             <li><span>字符串 (不定长)</span></li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 15:47
 */
public class SourceStringSerializer extends AbstractSerializer<SourceString> {

    private static final int RAW_BC = CompileFileConstants.RAW_BC;
    private static final int MAX_RAW = CompileFileConstants.MAX_RAW;
    private static final int COL_BC = CompileFileConstants.COL_BC;
    private static final int MAX_COL = CompileFileConstants.MAX_COL;
    private static final int TYPE_ORDINAL_BC = CompileFileConstants.SOURCE_STRING_TYPE_ORDINAL_BC;
    private static final int MAX_TYPE_ORDINAL = CompileFileConstants.MAX_SOURCE_STRING_TYPE_ORDINAL;
    private static final int STRING_LEN_BC = CompileFileConstants.SOURCE_STRING_LENGTH_BC;
    private static final int MAX_STRING_LEN = CompileFileConstants.MAX_SOURCE_STRING_LENGTH;
    private static final int HEAD_BC = RAW_BC + COL_BC + TYPE_ORDINAL_BC + STRING_LEN_BC;


    public SourceStringSerializer(InputStream is, OutputStream os) {
        super(is, os);
    }

    private static void isValidHead(SourcePosition position, int ordinal, int strLen) {
        Serializes.notTooMuch(position.getRaw(), "raw", MAX_RAW);
        Serializes.notTooMuch(position.getColumn(), "column", MAX_COL);
        Serializes.notTooMuch(ordinal, "type", MAX_TYPE_ORDINAL);
        Serializes.notTooLong(strLen, "source string length", MAX_STRING_LEN);
    }

    @Override
    public void serialize(SourceString origin) throws IOException {
        if (origin == null) {
            return;
        }
        SourcePosition position = origin.getPosition();
        int ordinal = origin.getType().ordinal();
        byte[] value = origin.getValue().getBytes(CompileProperties.SOURCE_FILE_CHARSET);
        int strLen = value.length;
        isValidHead(position, ordinal, strLen);
        // head制作
        SerializableData head = Serializes.makeHead(
                new HeadMap(position.getRaw(), RAW_BC),
                new HeadMap(position.getColumn(), COL_BC),
                new HeadMap(ordinal, TYPE_ORDINAL_BC),
                new HeadMap(strLen, STRING_LEN_BC)
        );
        os.write(new SerializableData(
                head, value).data());

    }

    @Override
    public SourceString deserialize() throws IOException {
        SerializableData head = new SerializableData(is.readNBytes(Serializes.bitCountToByteCount(HEAD_BC)));
        HeadMap[] headMaps = head.phaseHeader(RAW_BC, COL_BC, TYPE_ORDINAL_BC, STRING_LEN_BC);
        assert headMaps.length == 4;
        int raw = (int) headMaps[0].getValue();
        int col = (int) headMaps[1].getValue();
        int typeOrdinal = (int) headMaps[2].getValue();
        int stringLen = (int) headMaps[3].getValue();
        SerializableData read = new SerializableData(is.readNBytes(stringLen));
        String identifier = AbstractSerializer.getString(read);
        return new SourceString(
                SourceStringType.get(typeOrdinal),
                identifier, new SourcePosition(raw, col));
    }


}
