package org.harvey.compiler.io;

import org.harvey.compiler.command.CompileProperties;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.common.reflect.VieConstructor;
import org.harvey.compiler.text.SourceFileManager;
import org.harvey.compiler.text.context.SourceTextContext;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 16:01
 */
public class SourceFileReader<R extends Reader> {
    public static final String LINE_SEPARATOR = SourceFileConstant.LINE_SEPARATOR;
    private final VieConstructor<R> constructor;

    public SourceFileReader(Class<R> readerType) {
        this.constructor = new VieConstructor<>(readerType, String.class);
    }

    private static boolean incompleteLineSeparator(String buff) {
        for (int i = LINE_SEPARATOR.length() - 1; i > 0; i--) {
            if (buff.endsWith(LINE_SEPARATOR.substring(0, i))) {
                return true;
            }
        }
        return false;
    }

    public SourceTextContext read(String filename, SourceFileManager manager) throws IOException {
        char[] buffer = new char[CompileProperties.FILE_READ_BUFFER_SIZE];
        try (R reader = constructor.instance(filename)) {
            read0(manager, reader, buffer);
        }
        return manager.get();
    }

    public SourceTextContext read(File file, SourceFileManager manager) throws IOException {
        char[] buffer = new char[CompileProperties.FILE_READ_BUFFER_SIZE];
        try (R reader = constructor.instance(file)) {
            read0(manager, reader, buffer);
        }
        return manager.get();
    }

    private void read0(SourceFileManager manager, R reader, char[] buffer) throws IOException {
        int read;
        String lastBuffer = "";
        while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
            if (manager.completePhaseFile()) {
                break;
            }
            lastBuffer = apartThisBuff(manager, lastBuffer + new String(buffer, 0, read));
        }
        if (!lastBuffer.isEmpty() && !manager.completePhaseFile()) {
            manager.appendDecomposed(false, lastBuffer);
        }
    }

    /**
     * 以行分解一个字符串, 如果行的末尾的换行标识符不全, 将返回哪个不全的部分<br>
     * 给consumer的参数中, 如果是完整的一行, 那么末尾一定是换行符
     */
    private String apartThisBuff(SourceFileManager manager, String buff) {
        int index;
        int fromIndex = 0;
        while (true) {
            index = buff.indexOf(LINE_SEPARATOR, fromIndex);
            if (index >= 0) {
                // 有换行的
                manager.appendDecomposed(
                        true,
                        buff.substring(fromIndex, index + LINE_SEPARATOR.length())
                );
                fromIndex = index + LINE_SEPARATOR.length();
                continue;
            }
            // 没有换行的
            if (incompleteLineSeparator(buff)) {
                return buff.substring(fromIndex);
            } else {
                manager.appendDecomposed(false, buff.substring(fromIndex));
                return "";
            }
        }
    }

}
