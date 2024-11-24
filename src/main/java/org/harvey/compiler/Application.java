package org.harvey.compiler;

import org.harvey.compiler.analysis.stmt.phaser.ImportPhaser;
import org.harvey.compiler.analysis.stmt.phaser.StatementContextBuilder;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedFileBody;
import org.harvey.compiler.analysis.stmt.phaser.variable.FileVariablePhaser;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.decomposer.CommitClearChecker;
import org.harvey.compiler.analysis.text.decomposer.StringDecomposer;
import org.harvey.compiler.analysis.text.decomposer.TextDecomposerChain;
import org.harvey.compiler.analysis.text.mixed.MixedTextDecomposer;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.command.CommandException;
import org.harvey.compiler.io.SourceFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
        if (args.length == 0) {
            throw new CommandException("Need text file path");
        }
        String filename = args[0];
        Properties properties = new Properties();
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            int depart = arg.indexOf('=');
            if (depart == -1) {
                throw new CommandException("Illegal command argument:" + arg);
            }
            properties.setProperty(arg.substring(0, depart), arg.substring(depart + 1));
        }
        globalExceptionHandler(new File(filename));
        // HBC text target=targetPath base=BasePath
        //
    }

    private static void globalExceptionHandler(File file) {
        try {
            if (file.isDirectory()) {
                // 编译所有子目录文件
                compileDirectory(file.getAbsolutePath());
            } else if (file.isFile()) {
                compileFile(file.getAbsolutePath());
            } else {
                throw new FileNotFoundException(file.getAbsolutePath());
            }
        } catch (IOException | CompileException ce) {
            System.err.println("[ERROR] " + ce.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("编译器内部异常, 建议上报[mailto:harvey.blocks@outlook.com]", e);
        }
    }

    private static void compileDirectory(String absolutePath) {
        // TODO
    }

    private static void compileFile(String filename) throws Exception {
        TextDecomposerChain decomposerChain = registerChain();
        SourceTextContext context = decomposerChain.execute(new SourceFileReader<>(FileReader.class).read(filename));
        DepartedFileBody departedFileBody = DepartedFileBody.depart(context);
        StatementContextBuilder builder = new StatementContextBuilder();
        builder.registerPhaser(ImportPhaser::new)
                .registerPhaser(FileVariablePhaser::new);
        builder.build(departedFileBody);
        System.out.println("end");
        // TODO PackagePhaser pp = new PackagePhaser()
        //      Package nowP = pp.phase(context);
        //      SourceFile sf = new SourceFile(nowP,filename);
        //      ImportTablePhaser itp =  new ImportTablePhaser();
        //      ImportTable it = itp.phase(context);
        //      SourceContext srcContext = new sc(bool checked = false,sf,it,context);
        //      StatementContext statContext = StatementContextAdapter.adapt(sc);
        //      StatementFileWriter.write(statContext);
        //      it.map(new Function{
        //          public Queue taskQueue = new Queue();
        //          public Set<String> checkedFile;
        //          public R apply(){
        //              f->taskQueue.pull(new SourceFileReader<>(FileReader.class).read(f.getFilename(),(Checked File)sf)
        //          }
        //      });

    }

    private static TextDecomposerChain registerChain() {
        TextDecomposerChain chain = new TextDecomposerChain();
        chain.register(new CommitClearChecker())
                //.register(new SourceFileRebuilder())
                .register(new MixedTextDecomposer())
                //.register(new SourceFileRebuilder())
                .register(new StringDecomposer())
        ;//.register(new SourceFileRebuilder());
        return chain;
    }


}
