package org.harvey.compiler.common.constant;

import java.io.File;

/**
 * BC = Bit Count
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 01:36
 */
public final class CompileFileConstant {

    public static final String FILE_SUFFIX = ".hdsmn";
    // C'est la vie
    public static final long COMPILE_FILE_MAGIC = 0x00_CE_4D_5A_5EL;
    public static final int COMPILE_FILE_MAGIC_BC = 32;
    public static final int STAGE_BC = 8;
    public static final int MAX_STAGE = (1 << STAGE_BC) - 1;
    public static final int STRING_LEN_BC = 12;
    // 常量池个数上限
    public static final int CONSTANT_TABLE_SIZE_BC = 16;
    public static final int MAX_CONSTANT_TABLE_SIZE = (1 << CONSTANT_TABLE_SIZE_BC) - 1;
    public static final int CONSTANT_ELEMENT_STRING_LENGTH_BC = STRING_LEN_BC;
    public static final int MAX_CONSTANT_ELEMENT_STRING_LENGTH = (1 << CONSTANT_ELEMENT_STRING_LENGTH_BC) - 1;
    // 类型标识
    public static final int CONSTANT_ELEMENT_INDEX_LENGTH_BC = 12;
    public static final int IMPORT_IDENTIFIER_TYPE_CODE = (1 << CONSTANT_ELEMENT_INDEX_LENGTH_BC) - 1;
    public static final int BOOL_TYPE_CODE = IMPORT_IDENTIFIER_TYPE_CODE - 1;
    public static final int CHAR_TYPE_CODE = BOOL_TYPE_CODE - 1;
    public static final int FLOAT32_TYPE_CODE = CHAR_TYPE_CODE - 1;
    public static final int FLOAT64_TYPE_CODE = FLOAT32_TYPE_CODE - 1;
    public static final int INT8_TYPE_CODE = FLOAT64_TYPE_CODE - 1;
    public static final int INT16_TYPE_CODE = INT8_TYPE_CODE - 1;
    public static final int INT32_TYPE_CODE = INT16_TYPE_CODE - 1;
    public static final int INT64_TYPE_CODE = INT32_TYPE_CODE - 1;
    public static final int UNSIGNED_INT8_TYPE_CODE = INT64_TYPE_CODE - 1;
    public static final int UNSIGNED_INT16_TYPE_CODE = UNSIGNED_INT8_TYPE_CODE - 1;
    public static final int UNSIGNED_INT32_TYPE_CODE = UNSIGNED_INT16_TYPE_CODE - 1;
    public static final int UNSIGNED_INT64_TYPE_CODE = UNSIGNED_INT32_TYPE_CODE - 1;
    public static final int VAR_TYPE_CODE = UNSIGNED_INT64_TYPE_CODE - 1;
    public static final int NOT_DETERMINED_YET_TYPE_CODE = VAR_TYPE_CODE - 1;
    public static final int MAX_CONSTANT_ELEMENT_INDEX_LENGTH = NOT_DETERMINED_YET_TYPE_CODE - 1;
    // 源码转换上限
    public static final int DEPARTED_FILE_SIZE_BC = 16;
    public static final int MAX_DEPARTED_FILE_SIZE_LEN = (1 << DEPARTED_FILE_SIZE_BC) - 1;
    public static final int RAW_BC = 16;
    public static final int COL_BC = 14;
    public static final int MAX_RAW = (1 << RAW_BC) - 1;
    public static final int MAX_COL = (1 << COL_BC) - 1;
    public static final int SOURCE_STRING_TYPE_ORDINAL_BC = 6;
    public static final int MAX_SOURCE_STRING_TYPE_ORDINAL = (1 << SOURCE_STRING_TYPE_ORDINAL_BC) - 1;
    public static final int SOURCE_STRING_LENGTH_BC = STRING_LEN_BC;
    public static final int MAX_SOURCE_STRING_LENGTH = (1 << SOURCE_STRING_LENGTH_BC) - 1;
    public static final int DEPARTED_PART_STATEMENT_LENGTH_BC = 12;
    public static final int DEPARTED_PART_BODY_LENGTH_BC = 20;
    public static final int MAX_DEPARTED_PART_STATEMENT_LENGTH = (1 << DEPARTED_PART_STATEMENT_LENGTH_BC) - 1;
    public static final int MAX_DEPARTED_PART_BODY_LENGTH = (1 << DEPARTED_PART_BODY_LENGTH_BC) - 1;
    public static final String STRUCTURE_SEPARATOR = "$";
    public static final String FILE_SEPARATOR = File.separator;


    private CompileFileConstant() {
    }
}
