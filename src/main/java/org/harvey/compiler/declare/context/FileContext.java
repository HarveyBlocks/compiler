package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.execute.control.ExecutableBody;
import org.harvey.compiler.execute.control.ExecutableBodyFactory;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.ss.StreamSerializer;
import org.harvey.compiler.io.ss.StringStreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static org.harvey.compiler.io.ss.StreamSerializer.readElements;
import static org.harvey.compiler.io.ss.StreamSerializer.writeElements;

/**
 * 要便于序列化, 已经要有常量池的概念了...
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:36
 */
@Getter
@AllArgsConstructor
public class FileContext {
    // 包含enum, interface, struct
    private final Map<String, ImportContext> importTable;
    private final List<TypeAlias> aliasList;
    private final List<String> identifierTable;
    private final ExecutableBodyFactory executableBodyFactory;
    private final List<ComplexStructureContext> complexStructureTable;
    private final List<CallableContext> functionTable;


    public FileContext() {
        this(new HashMap<>(), new ArrayList<>(), new ArrayList<>(), new ExecutableBodyFactory(new ArrayList<>()),
                new ArrayList<>(), new ArrayList<>());
    }


    /**
     * @return Index 如果已经含有此元素, 返回该元素所在的位置的index
     */
    public int addIdentifier(String identifier) {
        int index = identifierTable.indexOf(identifier);
        if (index >= 0) {
            // 已经含有元素了
            return index;
        }
        index = identifierTable.size();
        identifierTable.add(identifier);
        return index;
    }

    public String getIdentifier(int index) {
        return identifierTable.get(index);
    }

    public CallableContext getFunction(int index) {
        return functionTable.get(index);
    }

    public void addFunction(CallableContext function) {
        functionTable.add(function);
    }

    public void addStructure(ComplexStructureContext structure) {
        complexStructureTable.add(structure);
    }


    public ComplexStructureContext getComplexStructure(int outerStructure) {
        return complexStructureTable.get(outerStructure);
    }

    public int executableBodyDepart(SourceTextContext body) {
        return executableBodyFactory.depart(body);
    }

    public void addImport(Map<String, ImportContext> importTable) {
        this.importTable.putAll(importTable);
    }

    public void addAliases(List<TypeAlias> typeAliases) {
        this.aliasList.addAll(typeAliases);
    }


    public static class Serializer implements StreamSerializer<FileContext> {
        public static final int[] HEAD_LENGTH_BITS = {12, 12, 12, 12, 12, 12, 12};
        public static final int HEAD_BYTE = Serializes.bitCountToByteCount(ArrayUtil.sum(HEAD_LENGTH_BITS));
        private static final ImportContext.Serializer IMPORT_CONTEXT_SERIALIZER = StreamSerializer.get(
                ImportContext.Serializer.class);
        private static final StringStreamSerializer STRING_SERIALIZER = StreamSerializer.get(
                StringStreamSerializer.class);
        private static final CallableContext.Serializer CALLABLE_CONTEXT_SERIALIZER = StreamSerializer.get(
                CallableContext.Serializer.class);
        private static final ComplexStructureContext.Serializer COMPLEX_STRUCTURE_CONTEXT_SERIALIZER = StreamSerializer.get(
                ComplexStructureContext.Serializer.class);
        private static final ExecutableBody.Serializer EXECUTABLE_BODY_SERIALIZER = StreamSerializer.get(
                ExecutableBody.Serializer.class);
        private static final TypeAlias.Serializer TYPE_ALIAS_SERIALIZER = StreamSerializer.get(
                TypeAlias.Serializer.class);

        static {
            StreamSerializer.register(new Serializer());
        }
        private Serializer() {
        }

        @Override
        public FileContext in(InputStream is) {
            byte[] head;
            try {
                head = is.readNBytes(HEAD_BYTE);
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            HeadMap[] headMap = new SerializableData(head).phaseHeader(HEAD_LENGTH_BITS);
            ArrayList<ImportContext> importTable = readElements(is, headMap[0].getValue(), IMPORT_CONTEXT_SERIALIZER);
            ArrayList<String> identifierTable = readElements(is, headMap[1].getValue(), STRING_SERIALIZER);
            ArrayList<CallableContext> functionTable = readElements(is, headMap[2].getValue(),
                    CALLABLE_CONTEXT_SERIALIZER);
            ArrayList<ComplexStructureContext> complexStructureTable = readElements(is, headMap[3].getValue(),
                    COMPLEX_STRUCTURE_CONTEXT_SERIALIZER);
            ArrayList<ExecutableBody> executablePool = readElements(is, headMap[4].getValue(),
                    EXECUTABLE_BODY_SERIALIZER);
            ArrayList<TypeAlias> aliasList = readElements(is, headMap[5].getValue(), TYPE_ALIAS_SERIALIZER);
            return new FileContext(importToMap(importTable), aliasList, identifierTable,
                    new ExecutableBodyFactory(executablePool), complexStructureTable, functionTable);
        }

        private Map<String, ImportContext> importToMap(List<ImportContext> importTable) {
            return importTable.stream().collect(Collectors.toMap(e -> e.getTarget().getValue(), e -> e));
        }


        @Override
        public int out(OutputStream os, FileContext src) {
            Collection<ImportContext> importTable = src.importTable.values();
            Collection<String> identifierTable = src.identifierTable;
            Collection<CallableContext> functionTable = src.functionTable;
            Collection<ComplexStructureContext> complexStructureTable = src.complexStructureTable;
            Collection<ExecutableBody> executablePool = src.executableBodyFactory.getPool();
            Collection<TypeAlias> aliasList = src.aliasList;
            SerializableData head = Serializes.makeHead(
                    new HeadMap(importTable.size(), HEAD_LENGTH_BITS[0]),
                    new HeadMap(identifierTable.size(), HEAD_LENGTH_BITS[1]),
                    new HeadMap(functionTable.size(), HEAD_LENGTH_BITS[2]),
                    new HeadMap(complexStructureTable.size(), HEAD_LENGTH_BITS[3]),
                    new HeadMap(executablePool.size(), HEAD_LENGTH_BITS[4]),
                    new HeadMap(aliasList.size(), HEAD_LENGTH_BITS[5]));
            try {
                os.write(head.data());
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }
            return head.length() + writeElements(os, importTable, IMPORT_CONTEXT_SERIALIZER) +
                    writeElements(os, identifierTable, STRING_SERIALIZER) +
                    writeElements(os, functionTable, CALLABLE_CONTEXT_SERIALIZER) +
                    writeElements(os, complexStructureTable, COMPLEX_STRUCTURE_CONTEXT_SERIALIZER) +
                    writeElements(os, executablePool, EXECUTABLE_BODY_SERIALIZER) +
                    writeElements(os, aliasList, TYPE_ALIAS_SERIALIZER);
        }


    }
}
