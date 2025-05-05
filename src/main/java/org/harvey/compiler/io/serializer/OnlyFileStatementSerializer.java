package org.harvey.compiler.io.serializer;

import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.ByteUtil;
import org.harvey.compiler.declare.context.CallableContext;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.declare.identifier.DeprecatedIdentifierManager;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.DequeueOutputStream;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.harvey.compiler.io.serializer.StreamSerializerUtil.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 20:31
 */

@Getter
public class OnlyFileStatementSerializer implements StatementFileSerializer {
    public static final int[] HEAD_LENGTH_BITS = {16, 16, 8, 8, 16, 16, 16, 16, 24};
    public static final int HEAD_BYTE = Serializes.bitCountToByteCount(ArrayUtil.sum(HEAD_LENGTH_BITS));
    private static final FullIdentifierString.Serializer FULL_IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
            FullIdentifierString.Serializer.class);
    private static final ImportString.Serializer IMPORT_STRING_SERIALIZER = StreamSerializerRegister.get(
            ImportString.Serializer.class);
    private static final TypeAlias.Serializer TYPE_ALIAS_SERIALIZER = StreamSerializerRegister.get(
            TypeAlias.Serializer.class);

    private static final ReferenceElement.Serializer REFERENCE_ELEMENT_SERIALIZER = StreamSerializerRegister.get(
            ReferenceElement.Serializer.class);

    private static final CallableContext.Serializer CALLABLE_CONTEXT_SERIALIZER = StreamSerializerRegister.get(
            CallableContext.Serializer.class);
    private final File file;
    private HeadMap[] headMap;
    @Getter
    private CompileStage stage;
    @Getter
    private FileContext resource;

    public OnlyFileStatementSerializer(File file) {
        this.file = file;
        this.resource = null;
        this.headMap = null;
        this.stage = CompileStage.SOURCE;
    }

    public static void out(DequeueOutputStream os, FileContext src) {
        DIdentifierManager identifierManager = src.getIdentifierManager();
        int importReferenceAfterIndex = identifierManager.getImportReferenceAfterIndex();
        List<FullIdentifierString> allIdentifierTable = identifierManager.getAllIdentifierTable();
        Set<Integer> disableSet = identifierManager.getDisableSet();
        Collection<ImportString> importStrings = identifierManager.getImportTable().values();
        Collection<CallableContext> functionTable = src.getFunctionTable();
        Collection<TypeAlias> aliasList = src.getAliasList();
        Collection<ReferenceElement> complexStructureTable = src.getComplexStructureTable();
        List<? extends List<SourceString>> executablePool = src.getExecutablePool();
        int headLength = StreamSerializerUtil.writeHeads(os,
                new HeadMap(importReferenceAfterIndex, HEAD_LENGTH_BITS[0]).inRange(
                        true,
                        "import reference after index"
                ),
                new HeadMap(allIdentifierTable.size(), HEAD_LENGTH_BITS[1]).inRange(true, "identifier table size"),
                new HeadMap(identifierManager.getPreLength(), HEAD_LENGTH_BITS[2]).inRange(
                        true,
                        "complex structure table size"
                ),
                new HeadMap(disableSet.size(), HEAD_LENGTH_BITS[3]).inRange(
                        true,
                        "identifier disable set"
                ), new HeadMap(importStrings.size(), HEAD_LENGTH_BITS[4]).inRange(true, "import table size"),
                new HeadMap(aliasList.size(), HEAD_LENGTH_BITS[5]).inRange(true, "alias table size"),
                new HeadMap(complexStructureTable.size(), HEAD_LENGTH_BITS[6]).inRange(
                        true,
                        "complex structure table size"
                ), new HeadMap(functionTable.size(), HEAD_LENGTH_BITS[7]).inRange(true, "function table size"),
                new HeadMap(executablePool.size(), HEAD_LENGTH_BITS[8]).inRange(
                        true,
                        "complex structure table size"
                )
        );
        int len1 = headLength +
                   writeElements(os, allIdentifierTable, FULL_IDENTIFIER_STRING_SERIALIZER) +
                   writeNumbers(os, disableSet, 32, false) +
                   writeElements(os, importStrings, IMPORT_STRING_SERIALIZER) +
                   writeElements(os, aliasList, TYPE_ALIAS_SERIALIZER) +
                   writeElements(os, complexStructureTable, REFERENCE_ELEMENT_SERIALIZER);
        int len2 = len1 + writeElements(os, functionTable, CALLABLE_CONTEXT_SERIALIZER);
        int len3 = len2 + StatementFileSerializer.writePool(os, src.getExecutablePool());
        os.writeFirst(ByteUtil.toRawBytes(len3));
        os.writeFirst(ByteUtil.toRawBytes(len2));
        os.writeFirst(ByteUtil.toRawBytes(len1));
    }


    @Override
    public void updateStage(CompileStage stage) {
        this.stage = stage;
    }

    public Object in(InputStream is) {
        int len1;
        int len2;
        int len3;
        try {
            len1 = ByteUtil.phaseRawBytes4(is.readNBytes(4));
            len2 = ByteUtil.phaseRawBytes4(is.readNBytes(4));
            len3 = ByteUtil.phaseRawBytes4(is.readNBytes(4));
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
        if (stage == CompileStage.STATEMENT) {
            // 读取类型声明
            if (headMap != null) {
                return resource;
            }
            this.headMap = StreamSerializerUtil.readHeads(is, HEAD_BYTE, HEAD_LENGTH_BITS);
            int importReferenceAfterIndex = (int) headMap[0].getUnsignedValue();
            long allIdentifierTableSize = headMap[1].getUnsignedValue();
            int preLength = (int) headMap[2].getUnsignedValue();
            int disableSetSize = (int) headMap[3].getUnsignedValue();
            long importStringsSize = headMap[4].getUnsignedValue();
            long aliasListSize = headMap[5].getUnsignedValue();
            long complexStructureTableSize = headMap[6].getUnsignedValue();
            ArrayList<FullIdentifierString> allIdentifierTable = readElements(
                    is, allIdentifierTableSize, FULL_IDENTIFIER_STRING_SERIALIZER);
            Set<Integer> disableSet = new HashSet<>(readNumbers(
                    is, disableSetSize, 32, false));
            ArrayList<ImportString> importStrings = readElements(is, importStringsSize, IMPORT_STRING_SERIALIZER);
            ArrayList<TypeAlias> typeAliases = readElements(is, aliasListSize, TYPE_ALIAS_SERIALIZER);
            ArrayList<ReferenceElement> complexStructureTable = readElements(
                    is, complexStructureTableSize, REFERENCE_ELEMENT_SERIALIZER);
            return resource = new FileContext(typeAliases, complexStructureTable,
                    new DeprecatedIdentifierManager(importStrings, importReferenceAfterIndex, preLength,
                            allIdentifierTable
                    ), null, null, null
            );
        } else if (stage == CompileStage.LINKING) {
            if (resource.getFunctionTable() != null) {
                return resource;
            }
            StatementFileSerializer.skip(is, len1);
            // 读取成员声明
            long functionTableSize = headMap[6].getUnsignedValue();
            ArrayList<CallableContext> functionTable = readElements(
                    is, functionTableSize, CALLABLE_CONTEXT_SERIALIZER);
            return resource = new FileContext(resource.getAliasList(), resource.getComplexStructureTable(),
                    resource.getIdentifierManager(), functionTable, null, null
            );
        } else if (stage == CompileStage.COMPILED) {
            if (resource.getExecutablePool() != null) {
                return resource;
            }
            StatementFileSerializer.skip(is, len2);
            // 读取executable
            int executablePoolSize = (int) headMap[7].getUnsignedValue();
            List<ArrayList<SourceString>> executablePool = StatementFileSerializer.readPool(
                    is, executablePoolSize);
            return resource = new FileContext(resource.getAliasList(), resource.getComplexStructureTable(),
                    resource.getIdentifierManager(), resource.getFunctionTable(), executablePool, null
            );
        } else {
            throw new CompilerException("Unknown stage");
        }

    }

}
