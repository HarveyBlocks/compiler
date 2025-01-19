package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.Pair;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-29 21:52
 */
public class ConstTableElementSerializer extends EntrySerializer<String, Integer> {
    private static final int MAX_STRING_LEN = CompileFileConstants.MAX_CONSTANT_ELEMENT_STRING_LENGTH;
    private static final int STRING_LEN_BC = CompileFileConstants.CONSTANT_ELEMENT_STRING_LENGTH_BC;
    private static final int MAX_INDEX_LEN = CompileFileConstants.MAX_CONSTANT_ELEMENT_INDEX_LENGTH;
    private static final int INDEX_BC = CompileFileConstants.CONSTANT_ELEMENT_INDEX_LENGTH_BC;
    private static final int HEAD_BC = STRING_LEN_BC + INDEX_BC;

    public ConstTableElementSerializer(InputStream is, OutputStream os) {
        super(is, os);
    }

    public static Map<String, Integer> toMap(Map<String, ImportContext> importContext) {
        Map<String, Integer> result = new HashMap<>();
        for (ImportContext context : importContext.values()) {
            String key = context.getPackagePath();
            int length = key.length();
            Serializes.notTooLong(length, "import identifier", CompileFileConstants.CONSTANT_ELEMENT_STRING_LENGTH_BC);
            result.put(key, CompileFileConstants.IMPORT_IDENTIFIER_TYPE_CODE);
        }
        return result;
    }


    @Override
    protected void serialize(String identifier, Integer index) throws IOException {
        int length = identifier.length();
        Serializes.notTooMuch(index, "identifier statement", MAX_INDEX_LEN);
        Serializes.notTooLong(length, "constant identifier", MAX_STRING_LEN);
        SerializableData head = Serializes.makeHead(
                new HeadMap(index, INDEX_BC),
                new HeadMap(length, STRING_LEN_BC)
        );
        os.write(new SerializableData(head,
                identifier.getBytes(CompileProperties.SOURCE_FILE_CHARSET)).data());
    }

    @Override
    public Map.Entry<String, Integer> deserialize() throws IOException {
        SerializableData head = new SerializableData(is.readNBytes(Serializes.bitCountToByteCount(HEAD_BC)));
        HeadMap[] headMaps = head.phaseHeader(INDEX_BC, STRING_LEN_BC);
        assert headMaps.length == 2;
        int mapTarget = (int) headMaps[0].getValue();
        int stringLen = (int) headMaps[1].getValue();
        SerializableData read = new SerializableData(is.readNBytes(stringLen));
        String identifier = AbstractSerializer.getString(read);
        return new Pair<>(identifier, mapTarget);
    }


}
