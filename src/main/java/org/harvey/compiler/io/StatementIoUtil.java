package org.harvey.compiler.io;

import org.harvey.compiler.common.constant.CompileFileConstant;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.context.StructureContext;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.*;

/**
 * {@link CompileStage#STATEMENT}阶段的IO工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 00:21
 */
public class StatementIoUtil {

    public static void write(String path, FileContext context) throws IOException {
        File file = createNotExistsFile(path);
        try (FileOutputStream fileOutputStream = new FileOutputStream(
                file); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream); DequeueOutputStream os = new DequeueOutputStream(bufferedOutputStream)) {
            writeCompileHeader(os, CompileStage.STATEMENT);
            OnlyFileStatementSerializer.out(os, context);
            os.flush();
        }
    }

    private static File createNotExistsFile(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                boolean ignore = parentFile.mkdirs();
            }
            boolean ignore = file.createNewFile();
        }
        return file;
    }

    public static void writeCompileHeader(OutputStream os, CompileStage stage) {
        StreamSerializerUtil.writeHeads(
                os,
                new HeadMap(CompileFileConstant.COMPILE_FILE_MAGIC, 32).inRange(true, "magic"),
                new HeadMap(stage.ordinal(), 8).inRange(true, "stage")
        );
    }

    private static CompileStage readCompileHeader(InputStream is) throws IOException {
        HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 5, 32, 8);
        if (headMaps[0].getUnsignedValue() != CompileFileConstant.COMPILE_FILE_MAGIC) {
            throw new IOException("not compile file");
        }
        return CompileStage.values()[(int) headMaps[0].getUnsignedValue()];
    }

    public static void write(String path, StructureContext context) throws IOException {
        File file = createNotExistsFile(path);
        try (FileOutputStream fileOutputStream = new FileOutputStream(
                file); BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
                fileOutputStream); DequeueOutputStream os = new DequeueOutputStream(bufferedOutputStream)) {
            writeCompileHeader(os, CompileStage.STATEMENT);
            StructureStatementFileSerializer.out(os, context);
            os.flush();
        }
    }


    /**
     * @return {@link StructureContext},...
     */
    public static StatementFileSerializer readStructure(File compiledFile) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(compiledFile))) {
            CompileStage stage = readCompileHeader(is);
            switch (stage) {
                case STATEMENT:
                    StatementFileSerializer serializer = new StructureStatementFileSerializer(compiledFile);
                    serializer.updateStage(CompileStage.STATEMENT);
                    serializer.in(is);
                    return  serializer;
                case LINKING:
                case COMPILED:
                    break;
                case PACKAGE:
                default:
                    throw new IllegalStateException("Unexpected value: " + stage);
            }
        }
        throw new IllegalStateException("unexpected");
    }

    /**
     * @return {@link FileContext},... 以后再说
     */
    public static StatementFileSerializer readFile(File compiledFile) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(compiledFile))) {
            CompileStage stage = readCompileHeader(is);
            switch (stage) {
                case STATEMENT:
                    // TODO
                    StatementFileSerializer serializer = new OnlyFileStatementSerializer(compiledFile);
                    serializer.updateStage(CompileStage.STATEMENT);
                    serializer.in(is);
                    return  serializer;
                case LINKING:
                case COMPILED:
                    break;
                case PACKAGE:
                default:
                    throw new IllegalStateException("Unexpected value: " + stage);
            }
        }
        throw new IllegalStateException("unexpected");
    }
}
