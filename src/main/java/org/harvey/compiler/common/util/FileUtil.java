package org.harvey.compiler.common.util;

import org.harvey.compiler.common.constant.CompileFileConstant;
import org.harvey.compiler.common.constant.SourceFileConstant;

import java.io.File;

/**
 * 对文件进行一些简单的辨别和操作的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 20:55
 */
public class FileUtil {
    private FileUtil() {
    }

    public static boolean inTarget(File file) {
        return file.getName().endsWith(CompileFileConstant.FILE_SUFFIX);
    }

    public static boolean inSource(File file) {
        return file.getName().endsWith(SourceFileConstant.FILE_SUFFIX);
    }

    public static boolean isCompiledStructure(File file) {
        return file.getName().contains(CompileFileConstant.STRUCTURE_SEPARATOR);
    }
}
