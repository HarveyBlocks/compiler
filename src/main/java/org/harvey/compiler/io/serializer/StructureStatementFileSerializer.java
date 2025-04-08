package org.harvey.compiler.io.serializer;

import lombok.Getter;
import org.harvey.compiler.common.util.ByteUtil;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.context.*;
import org.harvey.compiler.declare.identifier.DefaultIdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.DequeueOutputStream;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.stage.CompileStage;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.harvey.compiler.io.serializer.StreamSerializerUtil.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 20:29
 */
@Getter
public class StructureStatementFileSerializer implements StatementFileSerializer {
    public static final int[] HEAD_LENGTH_BITS = new int[]{
            // 0  1  2  3  4   5   6   7   8
            16, 8, 8, 8, 8, 12, 12, 24, 16,
            //  9   10  11  12  13  14 15  16  17  18  19,20
            32, 16, 16, 16, 16, 8, 16, 32, 16, 16, 24, 16};
    public static final int HEAD_BYTE = StreamSerializerUtil.headByte(HEAD_LENGTH_BITS);
    public static final ReferenceElement.Serializer REFERENCE_ELEMENT_SERIALIZER = StreamSerializerRegister.get(
            ReferenceElement.Serializer.class);
    private static final FullIdentifierString.Serializer FULL_IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
            FullIdentifierString.Serializer.class);
    private static final GenericDefine.Serializer GENERIC_DEFINE_SERIALIZER = StreamSerializerRegister.get(
            GenericDefine.Serializer.class);
    private static final GenericDefine.GenericUsingSerializer GENERIC_USING_SERIALIZER = StreamSerializerRegister.get(
            GenericDefine.GenericUsingSerializer.class);
    private static final TypeAlias.Serializer TYPE_ALIAS_SERIALIZER = StreamSerializerRegister.get(
            TypeAlias.Serializer.class);
    private static final CallableContext.Serializer CALLABLE_CONTEXT_SERIALIZER = StreamSerializerRegister.get(
            CallableContext.Serializer.class);
    private static final ConstructorContext.Serializer CONSTRUCTOR_CONTEXT_SERIALIZER = StreamSerializerRegister.get(
            ConstructorContext.Serializer.class);
    private static final ValueContext.Serializer VALUE_CONTEXT_SERIALIZER = StreamSerializerRegister.get(
            ValueContext.Serializer.class);
    private static final ImportString.Serializer IMPORT_STRING_SERIALIZER = StreamSerializerRegister.get(
            ImportString.Serializer.class);
    private final File file;
    private HeadMap[] headMap;
    @Getter
    private CompileStage stage;
    @Getter
    private StructureContext resource;


    public StructureStatementFileSerializer(File file) {
        this.file = file;
        this.resource = null;
        this.headMap = null;
        this.stage = CompileStage.SOURCE;
    }

    public static void out(DequeueOutputStream os, StructureContext src) {
        // 第一阶段
        int outerStructure = src.getOuterStructure();
        int depth = src.getDepth();
        byte permission = src.getAccessControl().getPermission();
        byte embellish = src.getEmbellish().getCode();
        int type = src.getType().ordinal();
        ReferenceElement identifierReference = src.getIdentifierReference();
        GenericDefine[] genericMessage = src.getGenericMessage();
        List<ParameterizedType<ReferenceElement>> interfaceList = src.getInterfaceList();
        IdentifierManager manager = src.getManager();
        int importReferenceAfterIndex = manager.getImportReferenceAfterIndex();
        int preLength = manager.getPreLength();
        List<FullIdentifierString> allIdentifierTable = manager.getAllIdentifierTable();
        Collection<ImportString> importStrings = manager.getImportTable().values();
        List<TypeAlias> typeAliases = src.getTypeAliases();
        List<Integer> innerStructureReferences = src.getInnerStructureReferences();
        List<ConstructorContext> constructors = src.getConstructors();
        // 第二阶段
        List<EnumConstantContext> enumConstants = src.getEnumConstants();
        List<ValueContext> fieldTable = src.getFieldTable();
        List<CallableContext> methodTable = src.getMethodTable();
        // 第三阶段
        List<Integer> blocks = src.getBlocks();
        List<Integer> staticBlocks = src.getStaticBlocks();
        List<? extends List<SourceString>> executablePool = src.getExecutablePool();
        int headLength = StreamSerializerUtil.writeHeads(os,
                // 第一阶段
                new HeadMap(outerStructure, HEAD_LENGTH_BITS[0]).inRange(false, "outer structure"),
                new HeadMap(depth, HEAD_LENGTH_BITS[1]).inRange(true, "depth"),
                new HeadMap(permission, HEAD_LENGTH_BITS[2]).inRange(true, "permission"),
                new HeadMap(embellish, HEAD_LENGTH_BITS[3]).inRange(true, "embellish"),
                new HeadMap(type, HEAD_LENGTH_BITS[4]).inRange(true, "type"),
                new HeadMap(genericMessage.length, HEAD_LENGTH_BITS[5]).inRange(true, "generic message size"),
                new HeadMap(interfaceList.size(), HEAD_LENGTH_BITS[6]).inRange(true, "interface list size"),
                new HeadMap(importReferenceAfterIndex, HEAD_LENGTH_BITS[7]).inRange(
                        true,
                        "import reference after index"
                ), new HeadMap(preLength, HEAD_LENGTH_BITS[8]).inRange(true, "preLength"),
                new HeadMap(allIdentifierTable.size(), HEAD_LENGTH_BITS[9]).inRange(true, "all identifier table size"),
                new HeadMap(importStrings.size(), HEAD_LENGTH_BITS[10]).inRange(true, "import strings size"),
                new HeadMap(typeAliases.size(), HEAD_LENGTH_BITS[11]).inRange(true, "type aliases size"),
                new HeadMap(innerStructureReferences.size(), HEAD_LENGTH_BITS[12]).inRange(
                        true,
                        "inner structure references size"
                ), new HeadMap(constructors.size(), HEAD_LENGTH_BITS[13]).inRange(true, "constructors size"),
                // 第二阶段
                new HeadMap(enumConstants.size(), HEAD_LENGTH_BITS[14]).inRange(true, "enum constants size"),
                new HeadMap(fieldTable.size(), HEAD_LENGTH_BITS[15]).inRange(true, "field table size"),
                new HeadMap(methodTable.size(), HEAD_LENGTH_BITS[16]).inRange(true, "method table size"),
                // 第三阶段
                new HeadMap(blocks.size(), HEAD_LENGTH_BITS[17]).inRange(true, "blocks size"),
                new HeadMap(staticBlocks.size(), HEAD_LENGTH_BITS[18]).inRange(true, "static blocks size"),
                new HeadMap(executablePool.size(), HEAD_LENGTH_BITS[19]).inRange(true, "executable pool size"),
                new HeadMap(manager.getDisableSet().size(), HEAD_LENGTH_BITS[20]).inRange(true, "disable set size")
        );
        int len1 = headLength +
                   REFERENCE_ELEMENT_SERIALIZER.out(os, identifierReference) +
                   writeElements(os, genericMessage, GENERIC_DEFINE_SERIALIZER) +
                   GENERIC_USING_SERIALIZER.out(os, src.getSuperStructure()) +
                   writeElements(os, interfaceList, GENERIC_USING_SERIALIZER) +
                   writeElements(os, allIdentifierTable, FULL_IDENTIFIER_STRING_SERIALIZER) +
                   writeElements(os, importStrings, IMPORT_STRING_SERIALIZER) +
                   writeElements(os, typeAliases, TYPE_ALIAS_SERIALIZER) +
                   writeNumbers(os, innerStructureReferences, 16, false) +
                   writeElements(os, constructors, CONSTRUCTOR_CONTEXT_SERIALIZER) +
                   writeNumbers(os, manager.getDisableSet(), 32, false);
        int len2 = len1 +
                   writeEnum(os, enumConstants) +
                   writeElements(os, fieldTable, VALUE_CONTEXT_SERIALIZER) +
                   writeElements(os, methodTable, CALLABLE_CONTEXT_SERIALIZER);
        int len3 = len2 +
                   writeNumbers(os, blocks, 32, false) +
                   writeNumbers(os, staticBlocks, 32, false) +
                   StatementFileSerializer.writePool(os, executablePool);
        os.writeFirst(ByteUtil.toRawBytes(len3));
        os.writeFirst(ByteUtil.toRawBytes(len2));
        os.writeFirst(ByteUtil.toRawBytes(len1));
    }

    private static int writeEnum(OutputStream os, List<EnumConstantContext> enumConstants) {
        int length = 0;
        for (EnumConstantContext each : enumConstants) {
            length += REFERENCE_ELEMENT_SERIALIZER.out(os, each.getIdentifier()) +
                      writeNumber(os, each.getArguments().size(), 16, false) +
                      writeNumbers(os, each.getArguments(), 32, false);
        }
        return length;
    }

    private static List<EnumConstantContext> readEnum(InputStream is, int length) {
        List<EnumConstantContext> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            ReferenceElement identifier = REFERENCE_ELEMENT_SERIALIZER.in(is);
            int size = (int) readNumber(is, 16, false);
            ArrayList<Integer> arguments = readNumbers(is, size, 32, false);
            result.add(new EnumConstantContext(identifier, arguments));
        }
        return result;
    }

    @Override
    public void updateStage(CompileStage stage) {
        this.stage = stage;
    }

    public Object in(InputStream is) {
        int len1;
        int len2;
        try {
            len1 = ByteUtil.phaseRawBytes4(is.readNBytes(4));
            len2 = ByteUtil.phaseRawBytes4(is.readNBytes(4));
            int ignore = ByteUtil.phaseRawBytes4(is.readNBytes(4));
        } catch (IOException e) {
            throw new CompilerFileReadException(e);
        }
        if (stage == CompileStage.STATEMENT) {
            if (headMap != null) {
                return resource;
            }
            // 读取类型声明
            this.headMap = StreamSerializerUtil.readHeads(is, HEAD_BYTE, HEAD_LENGTH_BITS);
            int outerStructure = (int) headMap[0].getSignedValue();
            int depth = (int) headMap[1].getUnsignedValue();
            AccessControl accessControl = new AccessControl((byte) headMap[2].getUnsignedValue());
            Embellish embellish = new Embellish((byte) headMap[3].getUnsignedValue());
            StructureType type = StructureType.values()[(int) headMap[4].getUnsignedValue()];
            long genericMessageSize = headMap[5].getUnsignedValue();
            long interfaceListSize = headMap[6].getUnsignedValue();
            int importReferenceAfterIndex = (int) headMap[7].getUnsignedValue();
            int preLength = (int) headMap[8].getUnsignedValue();
            long allIdentifierTableSize = headMap[9].getUnsignedValue();
            long importStringsSize = headMap[10].getUnsignedValue();
            long typeAliasesSize = headMap[11].getUnsignedValue();
            int innerStructureReferencesSize = (int) headMap[12].getUnsignedValue();
            int constructorsSize = (int) headMap[13].getUnsignedValue();
            ReferenceElement identifierReference = REFERENCE_ELEMENT_SERIALIZER.in(is);
            GenericDefine[] genericMessage = StreamSerializerUtil.readArray(
                    is, new GenericDefine[(int) genericMessageSize], GENERIC_DEFINE_SERIALIZER);
            ParameterizedType<ReferenceElement> superComplexStructure = GENERIC_USING_SERIALIZER.in(is);
            List<ParameterizedType<ReferenceElement>> interfaceList = readElements(is, interfaceListSize,
                    GENERIC_USING_SERIALIZER
            );
            ArrayList<FullIdentifierString> allIdentifierTable = readElements(is, allIdentifierTableSize,
                    FULL_IDENTIFIER_STRING_SERIALIZER
            );
            ArrayList<ImportString> importStrings = readElements(is, importStringsSize, IMPORT_STRING_SERIALIZER);
            int disableSetSize = (int) headMap[20].getUnsignedValue();
            HashSet<Integer> disableSet = new HashSet<>(readNumbers(is, disableSetSize, 32, false));
            IdentifierManager manager = new DefaultIdentifierManager(importStrings, importReferenceAfterIndex,
                    preLength, allIdentifierTable,disableSet
            );
            List<TypeAlias> typeAliases = readElements(is, typeAliasesSize, TYPE_ALIAS_SERIALIZER);
            List<Integer> innerStructureReferences = readNumbers(is, innerStructureReferencesSize, 16, false);
            List<ConstructorContext> constructors = readElements(is, constructorsSize, CONSTRUCTOR_CONTEXT_SERIALIZER);
            return resource = new StructureContext(outerStructure, depth, accessControl, embellish, type,
                    identifierReference, genericMessage, superComplexStructure, constructors, interfaceList, manager,
                    typeAliases, innerStructureReferences, null, null, null, null, null, null, null
            );
        } else if (stage == CompileStage.LINKING) {
            if (resource.getFieldTable() != null) {
                return resource;
            }
            StatementFileSerializer.skip(is, len1);
            // 第二阶段
            int enumConstantsSize = (int) headMap[14].getUnsignedValue();
            long fieldTableSize = headMap[15].getUnsignedValue();
            long methodTableSize = headMap[16].getUnsignedValue();
            // 读取成员声明
            List<EnumConstantContext> enumConstants = readEnum(is, enumConstantsSize);
            List<ValueContext> fieldTable = readElements(is, fieldTableSize, VALUE_CONTEXT_SERIALIZER);
            List<CallableContext> methodTable = readElements(is, methodTableSize, CALLABLE_CONTEXT_SERIALIZER);
            return resource = new StructureContext(resource.getOuterStructure(), resource.getDepth(),
                    resource.getAccessControl(), resource.getEmbellish(), resource.getType(),
                    resource.getIdentifierReference(), resource.getGenericMessage(), resource.getSuperStructure(),
                    resource.getConstructors(), resource.getInterfaceList(), resource.getManager(),
                    resource.getTypeAliases(), resource.getInnerStructureReferences(), enumConstants, fieldTable,
                    methodTable, null, null, null, null
            );
        } else if (stage == CompileStage.COMPILED) {
            if (resource.getExecutablePool() != null) {
                return resource;
            }
            StatementFileSerializer.skip(is, len2);
            // 第三阶段
            int blocksSize = (int) headMap[17].getUnsignedValue();
            int staticBlocksSize = (int) headMap[18].getUnsignedValue();
            int executablePoolSize = (int) headMap[19].getUnsignedValue();
            // 读取executable
            List<Integer> blocks = readNumbers(is, blocksSize, 32, false);
            List<Integer> staticBlocks = readNumbers(is, staticBlocksSize, 32, false);
            List<ArrayList<SourceString>> executablePool = StatementFileSerializer.readPool(is, executablePoolSize);
            return resource = new StructureContext(resource.getOuterStructure(), resource.getDepth(),
                    resource.getAccessControl(), resource.getEmbellish(), resource.getType(),
                    resource.getIdentifierReference(), resource.getGenericMessage(), resource.getSuperStructure(),
                    resource.getConstructors(), resource.getInterfaceList(), resource.getManager(),
                    resource.getTypeAliases(), resource.getInnerStructureReferences(), resource.getEnumConstants(),
                    resource.getFieldTable(), resource.getMethodTable(), blocks, staticBlocks, executablePool, null
            );
        } else {
            throw new CompilerException("Unknown stage");
        }

    }


}
