package org.harvey.compiler.common.constant;

import org.harvey.compiler.execute.calculate.Operator;

import java.util.Set;

/**
 * 编译器命令的有关常量
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 19:46
 */
public class CompileCommandConstant {
    public static final String PACKAGE_SEPARATOR = Operator.GET_MEMBER.getName();
    public static final String PACKAGE_NAME_KEY = "package";
    public static final String PACKAGE_NAME_DEFAULT = "";
    public static final Set<Character> ILLEGAL_PACKAGE_CHARACTER_SET = Set.of('$', '.');
    public static final String COMPILED_FILE_TARGET_DICTIONARY_KEY = "target";
    public static final String COMPILED_FILE_TARGET_DICTIONARY_DEFAULT = "target";
    public static final String SOURCE_FILE_KEY = "file";
    public static final String SOURCE_PATH_KEY = "source";
    public static final String PROPERTIES_PATH_KEY = "properties_file_path";
}
