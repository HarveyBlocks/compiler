package org.harvey.compiler.common;

import org.harvey.compiler.common.util.BinaryCharset;

import java.nio.charset.Charset;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:10
 */
public class CompileProperties {

    public static final Charset COMPILED_FILE_CHARSET = Charset.defaultCharset();
    public static final Charset NUMBER_CHARSET = BinaryCharset.INSTANCE;
    public static final int FILE_READ_BUFFER_SIZE = 128;
    public static final String COMPILED_FILE_TARGET_DIR = "";
    public static final String DEFAULT_PACKAGE = "";
    public static final Charset SOURCE_FILE_CHARSET = Charset.defaultCharset();
}
