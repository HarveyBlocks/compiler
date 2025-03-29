package org.harvey.compiler;

import org.harvey.compiler.command.CompileCommandProperties;
import org.harvey.compiler.common.constant.CompileCommandConstant;
import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.exception.command.CommandException;
import org.harvey.compiler.exception.execution.PropertiesException;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.PackageMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * 应用启动
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 20:08
 */
public class Application {

    public static void main(String[] args) {
        Application.application(args);
    }

    public static void application(String[] args) {
        if (args.length == 0) {
            throw new CommandException("Need text file path");
        }
        // [命令] source_file compile_properties_file
        compileMain(CompileCommandProperties.loadProperty(args));
    }


    private static void globalExceptionHandler(Properties properties) {

    }

    private static void compileMain(Properties properties) {
        CoreCompiler coreCompiler = new CoreCompiler(properties);
        PackageMessageFactory factory = coreCompiler.getPackageMessageFactory();
        String packageString = properties.getProperty(CompileCommandConstant.PACKAGE_NAME_KEY);
        PackageMessage beforeFilePackage = factory.create(packageString);
        String filename = properties.getProperty(CompileCommandConstant.SOURCE_FILE_KEY);
        PackageMessage.PathBuilder builder = beforeFilePackage.getSource();
        if (filename != null && !filename.isEmpty()) {
            builder.appendFile(filename).suffix();
        }
        File source = builder.build();
        if (source.isDirectory()) {
            // 编译所有子目录文件
            coreCompiler.compileDirectoryDeclare(beforeFilePackage, source);
        } else if (source.isFile()) {
            coreCompiler.compileAndWriteOneFile(beforeFilePackage, source);
        } else {
            throw new PropertiesException(new FileNotFoundException(source.getAbsolutePath()));
        }
        // 连接
    }


}
