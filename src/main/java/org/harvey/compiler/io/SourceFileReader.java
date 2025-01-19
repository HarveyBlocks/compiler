package org.harvey.compiler.io;

import org.harvey.compiler.analysis.text.SourceFileManager;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.SystemConstant;
import org.harvey.compiler.common.reflect.VieConstructor;

import java.io.IOException;
import java.io.Reader;
import java.util.function.BiConsumer;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 16:01
 */
public class SourceFileReader<R extends Reader> {
    private final VieConstructor<R> constructor;

    public SourceFileReader(Class<R> readerType) {
        this.constructor = new VieConstructor<>(readerType, String.class);
    }

    private static boolean incompleteLineSeparator(String buff) {
        for (int i = SystemConstant.LINE_SEPARATOR.length() - 1; i > 0; i--) {
            if (buff.endsWith(SystemConstant.LINE_SEPARATOR.substring(0, i))) {
                return true;
            }
        }
        return false;
    }

    public SourceTextContext read(String filename, SourceFileManager manager) throws IOException {
        BiConsumer<Boolean, String> consumer = manager::appendDecomposed;
        int size = CompileProperties.FILE_READ_BUFFER_SIZE;
        char[] buffer = new char[size];
        try (R reader = constructor.instance(filename)) {
            int read;
            String lastBuffer = "";
            while ((read = reader.read(buffer, 0, size)) > 0) {
                if (manager.completePhaseFile()) {
                    break;
                }
                lastBuffer = apartThisBuff(consumer, lastBuffer + new String(buffer, 0, read));
            }
            if (!lastBuffer.isEmpty() && !manager.completePhaseFile()) {
                consumer.accept(false, lastBuffer);
            }
        }
        return manager.get();
    }

    /**
     * 以行分解一个字符串, 如果行的末尾的换行标识符不全, 将返回哪个不全的部分<br>
     * 给consumer的参数中, 如果是完整的一行, 那么末尾一定是换行符
     */
    private String apartThisBuff(BiConsumer<Boolean, String> consumer, String buff) {
        int index;
        int fromIndex = 0;
        while (true) {
            index = buff.indexOf(SystemConstant.LINE_SEPARATOR, fromIndex);
            if (index >= 0) {
                // 有换行的
                consumer.accept(true, buff.substring(fromIndex, index + SystemConstant.LINE_SEPARATOR.length()));
                fromIndex = index + SystemConstant.LINE_SEPARATOR.length();
                continue;
            }
            // 没有换行的
            if (incompleteLineSeparator(buff)) {
                return buff.substring(fromIndex);
            } else {
                consumer.accept(false, buff.substring(fromIndex));
                return "";
            }
        }
    }

}
