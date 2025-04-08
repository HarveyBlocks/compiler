package org.harvey.compiler.io.cache;

import org.harvey.compiler.common.collecction.ListPoint;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.FileUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.declare.define.FileDefinition;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.PackageMessage;
import org.harvey.compiler.io.PackageMessageFactory;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.cache.node.FileNodeFactory;
import org.harvey.compiler.io.cache.resource.PackageStatementResource;
import org.harvey.compiler.io.serializer.StatementFileSerializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;

import static org.harvey.compiler.io.cache.node.FileNode.GET_MEMBER;

/**
 * 对文件节点的基于内存的存储, 防止多次IO造成的性能损失
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 16:00
 */
public class FileCache {

    private final PackageMessageFactory packageMessageFactory;
    private final Map<String, FileNode> fileMap = new HashMap<>();
    private final CoreCompiler coreCompiler;

    public FileCache(PackageMessageFactory packageMessageFactory, CoreCompiler coreCompiler) {
        this.packageMessageFactory = packageMessageFactory;
        this.coreCompiler = coreCompiler;
    }


    /**
     * @return null for not exist
     */
    public FileNode getInCache(FullIdentifierString fullIdentifier) {
        return getInCache(fullIdentifier.joinFullnameString(GET_MEMBER));
    }
    /**
     * @return null for not exist
     */
    public FileNode getInCache(String joinedFullIdentifier) {
        return fileMap.get(joinedFullIdentifier);
    }

    /**
     * @return exception for not exist
     */
    public FileNode getInCacheMustExist(FullIdentifierString fullIdentifier) {
        String key = fullIdentifier.joinFullnameString(GET_MEMBER);
        FileNode node = fileMap.get(key);
        if (node == null) {
            throw new CompilerException("compile " + key + " first");
        }
        return node;
    }

    /**
     * @param compute 传入参数可能是null
     */
    public FileNode compute(FullIdentifierString fullIdentifier, Function<FileNode, FileNode> compute) {
        String joinFullname = fullIdentifier.joinFullnameString(GET_MEMBER);
        FileNode old = fileMap.get(joinFullname);
        FileNode newNode = compute.apply(old);
        if (old != newNode) {
            fileMap.put(joinFullname, newNode);
        }
        return newNode;
    }

    public FileNode getReadTargetOrCache(String[] filepath) throws IOException {
        String joinFullname = StringUtil.join(filepath, GET_MEMBER);
        FileNode result = fileMap.get(joinFullname);
        if (result != null) {
            return result;
        }
        ListPoint<File> fileListPoint = packageMessageFactory.getFileAsLaterAsPossibleInTarget(filepath);
        if (fileListPoint == null) {
            return null;
        }
        StatementFileSerializer resource = readFileInTarget(fileListPoint.getElement());
        FileNode fileNode = FileNodeFactory.create(filepath[filepath.length - 1], joinFullname, resource);
        fileMap.put(joinFullname, fileNode);
        return fileNode;
    }


    /**
     * @param path after compiled file(file/structure), will be thrown
     * @return only return nearest file message
     * @throws IOException 要读写IO
     */
    public FileNode getOrCompileOrReadTargetOrCache(FullIdentifierString path) throws IOException {
        String joinFullname = path.joinFullnameString(GET_MEMBER);
        FileNode result = fileMap.get(joinFullname);
        if (result != null) {
            return result;
        }
        // 不在缓存
        ListPoint<File> fileListPoint = packageMessageFactory.filePathForImport(path);
        String simpleName = path.get(path.length() - 1);
        if (fileListPoint == null) {
            // 是package,
            File packageFile = packageMessageFactory.findAsPackage(path);
            result = FileNodeFactory.createForPackage(
                    simpleName, joinFullname, packageFile, packageMessageFactory.create(path.getFullname()));
            fileMap.put(joinFullname, result);
            loadBeforePackage(path, path.length());
            return null;
        }
        File file = fileListPoint.getElement();
        int indexOfFile = fileListPoint.getIndex();
        loadBeforePackage(path, indexOfFile);
        if (FileUtil.inSource(file)) {
            // 编译到第一阶段
            compileSource(path, fileListPoint);
            fileListPoint = packageMessageFactory.filePathForImport(path);
            if (fileListPoint == null) {
                throw new CompilerException("write compile file failed");
            }
        }
        if (!FileUtil.inTarget(file)) {
            throw new CompilerException("write compile file failed");
        }
        // 获取文件
        // 加入缓存
        StatementFileSerializer resource = readFileInTarget(file);
        FileNode fileNode = FileNodeFactory.create(simpleName, joinFullname, resource);
        fileMap.put(joinFullname, fileNode);
        return fileNode;
    }


    public FileNode getParent(FileNode fileNode) throws IOException {
        if (!fileNode.hasParent()) {
            return null;
        }
        String[] fullname = fileNode.getFullname();

        StringJoiner joiner = new StringJoiner(GET_MEMBER);
        for (int i = 0; i < fullname.length - 1; i++) {
            joiner.add(fullname[i]);
        }
        String joinFullname = joiner.toString();
        String simpleName = fullname[fullname.length - 2];
        FileNode parent = fileMap.get(joinFullname);
        if (parent != null) {
            return parent;
        }
        // 不在缓存中
        ListPoint<File> fileListPoint = packageMessageFactory.getFileAsLaterAsPossibleInTarget(
                ArrayUtil.sub(fullname, 0, fullname.length - 1, new String[fullname.length - 1]));
        File file = fileListPoint.getElement();
        if (file == null) {
            throw new CompilerException(
                    "Since when compiling and getting this file node, " +
                    "the previous package will definitely be put into the cache, " +
                    "but it is not put here, so it is incorrect");
        }
        if (!FileUtil.inTarget(file)) {
            throw new CompilerException("write compile file failed");
        }
        StatementFileSerializer resource = readFileInTarget(file);
        parent = FileNodeFactory.create(simpleName, joinFullname, resource);
        fileMap.put(joinFullname, parent);
        return parent;
    }

    public void loadOuterUntilStaticToCache(File file) {
        // TODO
    }

    private void loadBeforePackage(FullIdentifierString fullIdentifier, int end) {
        StringJoiner joiner = new StringJoiner(GET_MEMBER);
        for (int i = 0; i < end; i++) {
            String packageName = fullIdentifier.get(i);
            joiner.add(packageName);
            String key = joiner.toString();
            FileNode old = fileMap.get(key);
            if (old == null) {
                String[] packages = fullIdentifier.getRange(0, i);
                PackageMessage resource = packageMessageFactory.create(packages);
                fileMap.put(key, FileNodeFactory.createForPackage(packageName, key,
                        packageMessageFactory.findAsPackage(fullIdentifier), resource
                ));
            } else if (old.isPackage()) {
                if (old.getResource() != null) {
                    return;
                }
                String[] packages = fullIdentifier.getRange(0, i);
                PackageMessage resource = packageMessageFactory.create(packages);
                old.setResource(new PackageStatementResource(resource));
            } else {
                throw new CompilerException("old and new are conflicted");
            }
        }
    }


    private void compileSource(
            FullIdentifierString path, ListPoint<File> fileListPoint) throws IOException {
        File file = fileListPoint.getElement();
        int fileIndex = fileListPoint.getIndex();
        String[] packages = new String[fileIndex];
        String[] fullname = path.getFullname();
        ArrayUtil.sub(fullname, 0, fileIndex, packages);
        writeOneFile(packages, file);
    }

    /**
     * readFileInTarget and add to cache
     *
     * @return null if success;
     * {@link }
     */
    private StatementFileSerializer readFileInTarget(
            File compiledFile) throws IOException {
        if (FileUtil.isCompiledStructure(compiledFile)) {
            // 是structure
            return coreCompiler.readStructure(compiledFile);
        } else {
            // 是file
            return coreCompiler.readFile(compiledFile);
        }
    }


    private void writeOneFile(String[] packages, File file) throws IOException {
        PackageMessage beforeFilePackage = packageMessageFactory.create(packages);
        FileDefinition body = CoreCompiler.compileOneFileDeclare(beforeFilePackage, file);
        coreCompiler.writeOneFile(body, beforeFilePackage, file);
    }


}
