package org.harvey.compiler.command;

import org.harvey.compiler.common.constant.CompileCommandConstant;
import org.harvey.compiler.exception.command.CommandException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * 编译器命令的配置, key
 * command 形式
 * vie_file.vie source=source_package_pre \ #必须给出, 地址
 * package=Package \ # 默认就是编译source下所有文件
 * file=source \ # 不给就是编译整个包, 如果只给出file而不给出package就只一层文件, 不要给出文件后缀
 * target="" \ # 默认就是source.getParent()+"target"
 * properties_file="" \ # 配置文件地址, 可选
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 19:49
 */
public class CompileCommandProperties {
    private static final Map<String, String> DEFAULT_MAP = new HashMap<>();
    private static final List<String> MUST_PROPERTIES = new LinkedList<>();

    static {
        DEFAULT_MAP.put(CompileCommandConstant.PACKAGE_NAME_KEY, CompileCommandConstant.PACKAGE_NAME_DEFAULT);
        DEFAULT_MAP.put(
                CompileCommandConstant.COMPILED_FILE_TARGET_DICTIONARY_KEY,
                CompileCommandConstant.COMPILED_FILE_TARGET_DICTIONARY_DEFAULT
        );
        MUST_PROPERTIES.add(CompileCommandConstant.SOURCE_PATH_KEY);
    }

    public static Properties loadDefault() {
        Properties properties = new Properties();
        properties.putAll(DEFAULT_MAP);
        return properties;
    }

    public static Properties loadProperty(String[] args) {
        Properties properties = CompileCommandProperties.loadDefault();
        Map<String, String> argMaps = departArgs(args);
        if (!properties.containsKey(CompileCommandConstant.PROPERTIES_PATH_KEY)) {
            properties.putAll(argMaps);
            return checkHasMust(properties);
        }
        loadPropertiesFile(argMaps, properties);
        properties.putAll(argMaps);
        return checkHasMust(properties);
    }

    private static Properties checkHasMust(Properties properties) {
        for (String mustProperty : MUST_PROPERTIES) {
            if (!properties.containsKey(mustProperty)) {
                throw new CommandException(mustProperty + " is a must for compile");
            }
        }
        return properties;
    }

    private static void loadPropertiesFile(Map<String, String> argMaps, Properties properties) {
        String propertiesFile = argMaps.get(CompileCommandConstant.PROPERTIES_PATH_KEY);
        argMaps.remove(CompileCommandConstant.PROPERTIES_PATH_KEY);
        File file = new File(propertiesFile);
        try {
            properties.load(new FileReader(file.getAbsolutePath()));
        } catch (IOException ignore) {
            System.err.println("Not find property file: " + file.getAbsolutePath() + ", ignored");
        }
    }

    private static Map<String, String> departArgs(String[] args) {
        Map<String, String> argMaps = new HashMap<>();
        for (String arg : args) {
            int depart = arg.indexOf('=');
            if (depart == -1) {
                throw new CommandException("Illegal command argument:" + arg);
            }
            argMaps.put(arg.substring(0, depart), arg.substring(depart + 1));
        }
        return argMaps;
    }
}
