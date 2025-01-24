package org.harvey.compiler.io.serializer.structure;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.depart.DeclaredDepartedPart;
import org.harvey.compiler.depart.SimpleComplexStructure;
import org.harvey.compiler.io.serializer.AbstractSerializer;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.serializer.collection.CollectionSerializer;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.stage.TypeStatementMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 第一阶段序列化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 17:42
 */
public class TypeStatementMessageSerializer extends AbstractSerializer<TypeStatementMessage> {

    public TypeStatementMessageSerializer(InputStream is, OutputStream os) {
        super(is, os);
        ImportContextSerializer ics = new ImportContextSerializer(is, os);
        StringSerializer ss = new StringSerializer(is, os, 16);
        CollectionSerializer<SourceString> collectionSerializer = new CollectionSerializer<>(
                is, os, new SourceStringSerializer(is, os), 16, "source string"
        );
        DeclarableSerializer ds = new DeclarableSerializer(is, os);
        // new SimpleCallableSerializer
        // new SimpleComplexStructureSerializer
    }

    @Override
    public void serialize(TypeStatementMessage origin) throws IOException {
        Collection<ImportContext> importTable = origin.getImportTable().values(); // 12
        Set<String> identifierSet = origin.getIdentifierSet(); // 16
        List<DeclaredDepartedPart> callableList = origin.getFunctionTable();     // 12
        List<SimpleComplexStructure> structureList = origin.getStructureTable(); // 12
        int importTableSize = importTable.size();
        int identifierSetSize = identifierSet.size();
        int callableListSize = callableList.size();
        int structureListSize = structureList.size();
        // assert
        Serializes.notTooMuch(importTableSize, "import table size", Serializes.unsignedMaxValue(12));
        Serializes.notTooMuch(identifierSetSize, "identifier set size", Serializes.unsignedMaxValue(16));
        Serializes.notTooMuch(callableListSize, "callable list size", Serializes.unsignedMaxValue(12));
        Serializes.notTooMuch(structureListSize, "structure list size", Serializes.unsignedMaxValue(12));
        // head
        SerializableData head = Serializes.makeHead(
                new HeadMap(importTableSize, 12),
                new HeadMap(identifierSetSize, 16),
                new HeadMap(callableListSize, 12),
                new HeadMap(structureListSize, 12)
        );
        os.write(head.data());
        // TODO body
        for (int i = 0; i < importTableSize; i++) {
            os.write(null);
        }
    }


    @Override
    public TypeStatementMessage deserialize() throws IOException {
        return null;
    }
}
