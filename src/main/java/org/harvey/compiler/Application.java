package org.harvey.compiler;

import org.harvey.compiler.analysis.text.SimpleTextDecomposer;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.decomposer.CommitClearChecker;
import org.harvey.compiler.analysis.text.decomposer.SourceFileRebuilder;
import org.harvey.compiler.analysis.text.decomposer.TextDecomposerChain;
import org.harvey.compiler.analysis.text.mixed.MixedTextDecomposer;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.phaser.FileStatementContextBuilder;
import org.harvey.compiler.depart.*;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.command.CommandException;
import org.harvey.compiler.io.SourceFileReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
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
        int[] a = new int[112];
        a[1] = 2;
        a[5] = 3;
        a[2] = 1;
        a[3] = 6;
        a[11]++;
        a[41]--;
        // application(args);
    }

    public static void application(String[] args) {
        if (args.length == 0) {
            throw new CommandException("Need text file path");
        }
        globalExceptionHandler(new File(args[0]), (args.length == 1 ? null : loadProperty(args[1], args)));
        // HBC text target=targetPath base=BasePath
        //
    }

    private static Properties loadProperty(String filename, String[] args) {
        Properties properties = new Properties();
        File file = new File(filename);
        if (!"_".equals(filename)) {
            try {
                properties.load(new FileReader(file.getAbsolutePath()));
            } catch (IOException ignore) {
                System.err.println("Not find property file: " + file.getAbsolutePath());
            }
        }
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            int depart = arg.indexOf('=');
            if (depart == -1) {
                throw new CommandException("Illegal command argument:" + arg);
            }
            properties.setProperty(arg.substring(0, depart), arg.substring(depart + 1));
        }
        return properties;
    }

    private static void globalExceptionHandler(File file, Properties properties) {
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
            System.err.println("[ERROR] " + ce.getClass().getSimpleName() + ":" + ce.getMessage());
            ce.printStackTrace(System.err);
        } catch (Exception e) {
            throw new RuntimeException("Compiler internal exceptions, it is recommended to report" +
                    "[mailto:harvey.blocks@outlook.com]", e);
        }
    }

    private static void compileDirectory(String absolutePath) {
        // TODO
    }

    public static FileContext compileFile(String filename) throws Exception {

        SourceTextContext textContext = new SourceFileReader<>(FileReader.class)
                .read(filename, new SimpleTextDecomposer());
        TextDecomposerChain decomposerChain = registerChain();
        SourceTextContext context = decomposerChain.execute(textContext);
        textContext = null;
        decomposerChain = null;
        if (context.isEmpty()) {
            return null;
        }
        LinkedList<DepartedPart> body = SimpleDepartedBodyFactory.depart(context);
        context = null;
        DepartedBody departedBody = DepartedBodyFactory.depart(body);
        body = null;
        // blocks 也可以有内部方法
        RecursivelyDepartedBody depart = RecursivelyDepartedBodyFactory.depart(departedBody);
        departedBody = null;
        FileStatementContextBuilder builder = new FileStatementContextBuilder();
        FileContext fileContext = builder.build(depart);
        depart = null;
        System.gc();
        // TODO PackagePhaser pp = new PackagePhaser()
        //      Package nowP = pp.phase(context);
        //      SourceFile sf = new SourceFile(nowP,filename);
        //      ImportTablePhaser itp =  new ImportTablePhaser();
        //      ImportTable it = itp.phase(context);
        //      SourceContext srcContext = new sc(bool checked = false,sf,it,context);
        //      FileContext statContext = StatementContextAdapter.adapt(sc);
        //      StatementFileWriter.write(statContext);
        //      it.map(new Function{
        //          public Queue taskQueue = new Queue();
        //          public Set<String> checkedFile;
        //          public R apply(){
        //              f->taskQueue.pull(new SourceFileReader<>(FileReader.class).read(f.getFilename(),(Checked File)sf)
        //          }
        //      });
        System.out.println("end");
        return fileContext;
    }


    public static TextDecomposerChain registerChain() {
        TextDecomposerChain chain = new TextDecomposerChain();
        chain.register(new CommitClearChecker())
                .register(new SourceFileRebuilder())
                .register(new MixedTextDecomposer())
        // .register(new SourceFileRebuilder())
        // .register(new StringDecomposer())
        // .register(new SourceFileRebuilder())
        ;
        return chain;
    }


}
