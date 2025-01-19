package org.harvey.compiler.io.serializer.collection;

import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.io.serializer.Serializer;
import org.harvey.compiler.io.serializer.structure.ImportContextSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-30 17:46
 */
public class ImportTableSerializer implements Serializer<Map<String, ImportContext>> {
    private final CollectionSerializer<ImportContext> ic;

    public ImportTableSerializer(InputStream is, OutputStream os) {
        // TODO 魔数
        ic = new CollectionSerializer<>(
                is, os, new ImportContextSerializer(is, os),
                8, "import table element");
    }

    @Override
    public void serialize(Map<String, ImportContext> origin) throws IOException {
        ic.serialize(origin.values());
    }

    @Override
    public Map<String, ImportContext> deserialize() throws IOException {
        return ic.deserialize().stream()
                .collect(Collectors.toMap(e -> e.getTarget().getValue(), e -> e));
    }

}
