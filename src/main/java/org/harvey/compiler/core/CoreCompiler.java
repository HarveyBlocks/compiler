package org.harvey.compiler.core;

import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.ExceptionUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.context.StructureContext;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.define.DefinitionFactory;
import org.harvey.compiler.declare.define.FileDefinition;
import org.harvey.compiler.declare.identifier.IdentifierPoolFactory;
import org.harvey.compiler.exception.io.VieIOException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.PackageMessage;
import org.harvey.compiler.io.SourceFileReader;
import org.harvey.compiler.io.StatementIoUtil;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.serializer.StatementFileSerializer;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.SimpleTextDecomposer;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.decomposer.CheckTypeDecomposer;
import org.harvey.compiler.text.decomposer.CommitClearChecker;
import org.harvey.compiler.text.decomposer.SourceFileRebuilder;
import org.harvey.compiler.text.decomposer.TextDecomposerChain;
import org.harvey.compiler.text.depart.*;
import org.harvey.compiler.text.mixed.MixedTextDecomposer;
import org.harvey.compiler.type.raw.RawTypeRelationshipLoader;
import org.harvey.compiler.type.raw.RelationCache;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO 对IO的统一调用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 20:11
 */
@Getter
public class CoreCompiler {
    private final PackageMessageFactory packageMessageFactory;
    private final Properties properties;
    private final RawTypeRelationshipLoader rawTypeRelationshipLoader;
    private final FileCache fileCache;
    private final RelationCache relationCache;

    public CoreCompiler(Properties properties) {
        this.properties = properties;
        this.packageMessageFactory = new PackageMessageFactory(properties);
        this.fileCache = new FileCache(packageMessageFactory, this);
        this.relationCache = new RelationCache();
        this.rawTypeRelationshipLoader = new RawTypeRelationshipLoader(
                relationCache, fileCache, packageMessageFactory);
    }

    public static FileDefinition compileOneFileDeclare(
            PackageMessage beforeFilePackage, File file) throws IOException {

        SourceTextContext textContext = new SourceFileReader<>(FileReader.class).read(file, new SimpleTextDecomposer());
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

        String packagePath = StringUtil.join(
                beforeFilePackage.getFullname(file.getName()), IdentifierPoolFactory.MEMBER);
        FileDefinition fileDefinition = new DefinitionFactory().buildReferredDepartedBody(
                packagePath, depart);

        depart = null;
        // 1. 表达式的使用
        // 2. identifier的映射
        // 当前表达式的解析, 当务之急, 是完成对声明的解析, 例如返回值类型, 参数类型, 字段类型, 继承父类类型, 实现接口类型, 默认值的表达式解析可以暂缓
        // 也就是说, 需要能对类型进行解析,
        // 对类型进行解析, 就需要其他文件的信息, 获取其他文件的第一次阶段分析的结果, 然后进行分析吗...
        // 而类型的定义不允许循环依赖吗?
        // class A<T extends B>{ }
        // class C extends A<D>{ }
        // class B<T extends A>{ }
        // class D extends B<A<D>>{ }
        // 想不出可以循环依赖的例子, 但是也不能排除循环依赖的可能, 就先不允许循环依赖, 然后再报错, 记录吧
        // 首先把类型全部转换成引用,
        // 依据引用解析文件,如果在源码阶段,解析到完成类型泛型解析阶段, 如果在类型泛型解析阶段...,如果在后面的阶段,直接获取类信息
        // 还是要三阶段吗?
        //      0. 源码阶段
        //      1. 类型泛型解析阶段(不允许循环依赖)
        //      2. 其他声明类型解析阶段(没有循环依赖的概念)
        //      3. Executable解析阶段(允许循环依赖)
        //
        System.gc();
        System.out.println("end");
        return fileDefinition;
    }

    public static TextDecomposerChain registerChain() {
        TextDecomposerChain chain = new TextDecomposerChain();
        chain.register(new CommitClearChecker()).register(new SourceFileRebuilder()).register(new MixedTextDecomposer())
                // .register(new SourceFileRebuilder())
                // .register(new StringDecomposer())
                // .register(new SourceFileRebuilder())
                .register(new CheckTypeDecomposer(Set.of(
                        SourceType.SIGN, // sign
                        SourceType.STRING, // 源码中是字符串
                        SourceType.OPERATOR, // 完成分解的独立运算符
                        SourceType.CHAR, // 源码中是常量
                        SourceType.BOOL,// 源码中是常量, 是关键字
                        SourceType.INT32, // 整形
                        SourceType.INT64, // 整形
                        SourceType.FLOAT32, // 浮点数
                        SourceType.FLOAT64, // 浮点数
                        SourceType.IGNORE_IDENTIFIER,  // 用于忽略的字符传, 例如`_`
                        SourceType.SCIENTIFIC_NOTATION_FLOAT32, // 科学计数法
                        SourceType.SCIENTIFIC_NOTATION_FLOAT64, // 科学计数法
                        SourceType.IDENTIFIER, // 源码中是标识符
                        SourceType.KEYWORD
                )));
        return chain;
    }

    public static <R, P> List<R> map2Context(List<P> sources, Function<P, R> mapper) {
        return sources.stream().map(mapper).collect(Collectors.toList());
    }

    public void compileDirectoryDeclare(PackageMessage beforeFilePackage, File packageFile) {
        // TODO
        LinkedList<Pair<File, PackageMessage>> packages = new LinkedList<>();
        packages.addLast(new Pair<>(packageFile, beforeFilePackage));
        while (!packages.isEmpty()) {
            File source = packages.removeFirst().getKey();
            PackageMessage packageMessage = packages.removeFirst().getValue();
            if (source.isFile()) {
                compileAndWriteOneFile(packageMessage, source);
                continue;
            } else if (!source.isDirectory()) {
                throw new VieIOException("Unknown file: " + source.getAbsolutePath());
            }

            File[] files = source.listFiles();
            if (files == null || files.length == 0) {
                continue;
            }
            PackageMessage child = packageMessage.brith(source.getName());
            packages.addAll(Arrays.stream(files).map(f -> new Pair<>(f, child)).collect(Collectors.toList()));
        }

    }

    public StatementFileSerializer readStructure(File compiledFile) throws IOException {
        return StatementIoUtil.readStructure(compiledFile);
    }

    public StatementFileSerializer readFile(File compiledFile) throws IOException {
        return StatementIoUtil.readFile(compiledFile);
    }

    public void writeOneFile(
            FileDefinition body, PackageMessage beforeFilePackage, File source) throws IOException {
        String filename = source.getName();
        String compiledFilePath = packageMessageFactory.createPathForFile(beforeFilePackage, filename);
        if (body == null) {
            // filenameWithPackage for file context
            StatementIoUtil.write(compiledFilePath, FileContext.empty());
            return;
        }
        StatementIoUtil.write(compiledFilePath, new FileContext(body));
        List<StructureContext> structures = map2Context(body.getStructures(), StructureContext::new);
        for (StructureContext structure : structures) {
            FullIdentifierString structureName = structure.getManager()
                    .getIdentifier(structure.getIdentifierReference());
            // structureName for structures
            String structureCompiledFilePath = packageMessageFactory.createPathForFile(
                    beforeFilePackage, filename, structureName.getFullname());
            StatementIoUtil.write(structureCompiledFilePath, structure);
        }
    }

    public void compileAndWriteOneFile(PackageMessage beforeFilePackage, File source) {
        ExceptionUtil.ExceptionWrap.invoke(beforeFilePackage.getPackages(), source, () -> {
            FileDefinition body = CoreCompiler.compileOneFileDeclare(beforeFilePackage, source);
            this.writeOneFile(body, beforeFilePackage, source);
        });
    }
}
