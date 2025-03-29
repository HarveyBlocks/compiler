package org.harvey.compiler.io;

import lombok.Getter;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.ListPoint;
import org.harvey.compiler.common.constant.CompileCommandConstant;
import org.harvey.compiler.common.constant.CompileFileConstant;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.command.CommandException;
import org.harvey.compiler.exception.io.VieIOException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-15 21:55
 */
@Getter
public class PackageMessageFactory {
    public static final String COMPILE_FILE_SUFFIX = CompileFileConstant.FILE_SUFFIX;
    public static final String PATH_STRUCTURE_SEPARATOR = CompileFileConstant.STRUCTURE_SEPARATOR;
    private final String sourceAbsoluteFilePathPre;
    private final String targetAbsoluteFilePathPre;

    public PackageMessageFactory(Properties properties) {
        String sourcePath = properties.getProperty(CompileCommandConstant.SOURCE_PATH_KEY);
        File source = new File(sourcePath);
        if (!source.exists()) {
            throw new CommandException(sourcePath + " not found");
        }
        if (source.isFile()) {
            throw new CommandException(sourcePath + " expected a directory");
        }
        this.sourceAbsoluteFilePathPre = source.getAbsolutePath() + File.separator;


        String targetPath = properties.getProperty(CompileCommandConstant.COMPILED_FILE_TARGET_DICTIONARY_KEY);
        File target = new File(source.getParentFile().getAbsolutePath() + File.separator + targetPath);
        if (!target.exists()) {
            boolean ignore = target.mkdirs();
        }
        this.targetAbsoluteFilePathPre = target.getAbsolutePath() + File.separator;
    }

    private static boolean privateCanVisit(
            FullIdentifierString supplier, ListPoint<File> supplierFileInTarget, int firstDifferenceIndex) {
        if (firstDifferenceIndex == 0) {
            // 文件也不同, 肯定不能private访问
            return false;
        }
        int lastSameIndex = firstDifferenceIndex - 1;
        // private can visit  supplier in structure and supplier's structure on the path of  consumer
        //               supplier.end.previous.index>supplier.file.index
        //                  and supplier[0,length-1] in list<consumer.path> (not last)
        //                  or supplier.end.index>supplier.file.index (supplier self structure)
        //                  and supplier[0,length] in list<consumer.path> (not last)
        ListPoint<File> supplierStructureFileInTarget = PackageMessageFactory.findStructureFileInTarget(
                supplierFileInTarget, supplier.getFullname());
        if (supplierStructureFileInTarget.getIndex() == supplierFileInTarget.getIndex()) {
            // 文件里的元素, 肯定不能private
            return false;
        }
        return lastSameIndex >= supplierStructureFileInTarget.getIndex();
    }

    public static ListPoint<File> findStructureFileInTarget(ListPoint<File> fileListPoint, String[] fullname) {
        int fileIndex = fileListPoint.getIndex();
        if (fileIndex + 1 == fullname.length) {
            return fileListPoint;
        }
        File file = fileListPoint.getElement();
        StringBuilder fileNameBuilder = new StringBuilder(file.getParentFile().getAbsolutePath()).append(
                fullname[fileIndex]);
        int structureIndex = fileIndex;

        for (int i = fileIndex + 1; i < fullname.length; i++) {
            fileNameBuilder.append(CompileFileConstant.STRUCTURE_SEPARATOR).append(fullname[i]);
            file = new File(fileNameBuilder + CompileFileConstant.FILE_SUFFIX);
            if (file.exists()) {
                structureIndex = i;
                continue;
            }
            if (structureIndex == fileIndex) {
                return fileListPoint;
            }
            return new ListPoint<>(structureIndex, file);
        }
        return new ListPoint<>(structureIndex, file);
    }

    private static void validFile(File file, File directory) {
        if (file.exists() && !file.isFile()) {
            throw new CompileException("illegal file of: " + file.getPath());
        }
        if (directory.exists() && !directory.isDirectory()) {
            throw new CompileException("illegal file of: " + directory.getPath());
        }
        if (file.exists() && directory.exists()) {
            throw new CompileException(
                    "repeated file path: " + file.getAbsolutePath() + " and " + directory.getAbsolutePath());
        }
    }
    // -----------------------------------文件关系, 用于访问控制---------------------------------------

    public File getOuterStructureFile(FullIdentifierString string) {
        return getOuterStructureFileStack(string).peek();
    }

    /**
     * @return 如果string本身是structure, 那么不会包含string本身. 从target获取
     */
    public Stack<File> getOuterStructureFileStack(FullIdentifierString string) {
        PackageMessage beforeFilePackage = createFromTarget(string);
        if (beforeFilePackage == null) {
            // 不能是null吧... 否则不好吧...
            throw new CompilerException(string + " must compile first");
        }
        StringBuilder builder = new StringBuilder(
                beforeFilePackage.getCompiledFilepathPre() + string.get(beforeFilePackage.length()));
        Stack<File> outerStructureFileStack = new Stack<>();
        for (int i = beforeFilePackage.length() + 1, end = string.length() - 1; i < end; i++) {
            builder.append(PATH_STRUCTURE_SEPARATOR).append(string.get(i));
            File file = new File(builder + COMPILE_FILE_SUFFIX);
            if (!file.exists()) {
                break;
            } else if (!file.isFile()) {
                throw new VieIOException("excepted a structure file: " + string, new FileNotFoundException());
            }
            outerStructureFileStack.push(file);
        }
        return outerStructureFileStack;
    }

    /**
     * @return stack.top是最里面的, 也就是string所在的structure, 若string是structure, stack.top是string<br>
     * 如果string外面不是structure, 是文件. 返回一个空的stack, 若string是alias, 或其他成员, stack.top是成员所在类的文件
     */
    public Stack<String[]> getOuterStructureFilenameStack(FullIdentifierString string) {
        // isStatic的问题是
        // 获取static
        ListPoint<File> fileInTarget = findFileInTarget(string.getFullname());
        if (fileInTarget.getElement() == null) {
            throw new CompilerException("can not get (" + string + ")'s compiled file");
        }
        ListPoint<File> structureFileInTarget = PackageMessageFactory.findStructureFileInTarget(
                fileInTarget, string.getFullname());
        int fileInTargetIndex = fileInTarget.getIndex();
        int structureFileInTargetIndex = structureFileInTarget.getIndex();
        Stack<String[]> stack = new Stack<>();
        for (int i = structureFileInTargetIndex; i > fileInTargetIndex; i--) {
            stack.push(string.getRange(0, i));
        }
        return stack;
    }

    /**
     * @param upperPredicate 参数的element是consumer's outer structure's identifier, 也可能是 outer 的 outer <br>
     *                       但一定是是structure的identifier,<br>
     *                       如果outer不是structure则不会被调用.<br>
     *                       参数的index是consumer's outer file's identifier<br>
     *                       需要完成的工作是: <br>
     *                       判断supplier是否是param.element的父类, 如果是, 返回true, 否则, 返回false<br>
     */
    public boolean accessible(
            FullIdentifierString consumer,
            AccessControl accessControl,
            FullIdentifierString supplier,
            BiPredicate<String[], File> upperPredicate) {
        if (accessControl.canPublic()) {
            return true;
        }
        int firstDifferenceIndex = consumer.firstDifferenceIndex(supplier);
        ListPoint<File> supplierFileInTarget = findFileInTarget(supplier.getFullname());
        if (supplierFileInTarget.getElement() == null) {
            throw new CompilerException("can not get supplier(" + supplier + ")'s compiled file");
        }
        boolean privateCanVisit = privateCanVisit(supplier, supplierFileInTarget, firstDifferenceIndex);
        if (privateCanVisit) {
            return true;
        } else if (accessControl.onlySelfClass()) {
            return false;
        }
        if (!accessControl.canFile()) {
            return false;
        }
        // 我们需要获取哪些信息? file在identifier中的哪个位置, 重要!
        // file 后面就是file的成员, 哪个是structure结束的位置?
        //
        // file can visit    supplier's file is on the path of consumer
        //                      private can not visit
        //                      and
        boolean fileCanVisit = fileCanVisit(supplierFileInTarget, firstDifferenceIndex);
        if (fileCanVisit) {
            return true;
        }
        if (!accessControl.canPackage()) {
            return false;
        }
        // package can visit supplier's package is on the path of consumer
        //                      file can not visit
        //
        // consumer in supplier's package's child
        // 不检查 accessControl.canChildrenClass();
        boolean packageCanVisit = packageCanVisit(supplierFileInTarget, firstDifferenceIndex);
        if (packageCanVisit) {
            return true;
        }
        // 用BeforeFilePackageFactory#findStructureFileInTarget找到当前consumer的outerStructure(如果其outer为file, 返回false)
        // outerStructure的identifier与supplier的identifier送入RawTypeRelationshipFactory#isUpper对比
        // 返回true 表示可以访问, 返回false 表示不能访问
        if (!accessControl.canChildrenClass()) {
            return false;
        }
        Boolean result = forEachOuterStructure(consumer, (path, file) -> upperPredicate.test(path, file) ? true : null);
        return Boolean.TRUE.equals(result);
    }

    /**
     * @param action param not include structureIdentifier's structure self
     * @param <R>    null for still invoke, else for break
     */
    public <R> R forEachOuterStructure(FullIdentifierString structureIdentifier, BiFunction<String[], File, R> action) {
        ListPoint<File> consumerFileListPoint = findFileInTarget(structureIdentifier.getFullname());
        if (consumerFileListPoint.getElement() == null) {
            throw new CompilerException("can not get consumer(" + structureIdentifier + ")'s compiled file");
        }
        ListPoint<File> consumerStructureFileInTarget = PackageMessageFactory.findStructureFileInTarget(
                consumerFileListPoint, structureIdentifier.getFullname());
        if (consumerStructureFileInTarget.getIndex() <= consumerFileListPoint.getIndex()) {
            // outer is a file
            return null;
        }

        String[] consumerStructureIdentifier = structureIdentifier.getRange(
                0, consumerStructureFileInTarget.getIndex());
        LinkedList<String> identifierMaker =  CollectionUtil.toLinkedList(consumerStructureIdentifier);
        while (identifierMaker.size() > consumerFileListPoint.getIndex() + 1) {
            String[] path = identifierMaker.toArray(new String[0]);
            ListPoint<File> fileInTarget = findFileInTarget(path);
            if (fileInTarget == null || fileInTarget.getElement() == null || !fileInTarget.getElement().isFile()) {
                throw new CompilerException("outer is not a structure, but phase it as a structure file");
            }
            File file = fileInTarget.getElement();
            R apply = action.apply(path, file);
            if (apply != null) {
                return apply;
            }
            identifierMaker.removeLast();
        }
        return null;
    }

    private boolean fileCanVisit(
            ListPoint<File> supplierFileInTarget, int firstDifferenceIndex) {
        if (firstDifferenceIndex == 0) {
            // 文件也不同, 肯定不能private访问
            return false;
        }
        int lastSameIndex = firstDifferenceIndex - 1;
        return lastSameIndex >= supplierFileInTarget.getIndex();
    }

    private boolean packageCanVisit(ListPoint<File> supplierFileInTarget, int firstDifferenceIndex) {
        if (supplierFileInTarget.getIndex() == 0) {
            return true;
        }
        // 文件不同 firstDifference
        return firstDifferenceIndex >= supplierFileInTarget.getIndex();
    }

    /**
     * @param packageString org.harvey.demo
     */
    public PackageMessage create(String packageString) {
        return new PackageMessage(sourceAbsoluteFilePathPre, targetAbsoluteFilePathPre, packageString);
    }

    /**
     * @param packages org.harvey.demo
     */
    public PackageMessage create(String[] packages) {
        return new PackageMessage(sourceAbsoluteFilePathPre, targetAbsoluteFilePathPre, packages);
    }


    /**
     * @param identifier org.harvey.demo.file.Entity
     */
    public PackageMessage createFromImport(FullIdentifierString identifier) {
        ListPoint<File> fileListPoint = filePathForImport(identifier); // fileListPoint.element 可能是null
        String[] packages = identifier.getRange(0, fileListPoint.getIndex());
        return new PackageMessage(sourceAbsoluteFilePathPre, targetAbsoluteFilePathPre, packages);
    }

    /**
     * @return null for not compiled, 只是file, 不是structure
     */
    private PackageMessage createFromTarget(FullIdentifierString string) {
        ListPoint<File> fileInTarget = findFileInTarget(string.getFullname());
        if (null == fileInTarget.getElement()) {
            return null;
        } else {
            return create(string.getRange(0, fileInTarget.getIndex()));
        }
    }

    /**
     * 要考虑到没有这个文件的可能, 运行文件不存在, 因为要我们创建, 这里只返回文件名
     * 而且一定是在
     */
    public String createPathForFile(
            PackageMessage beforeFilePackage, String filename) {
        return beforeFilePackage.getSourceFilepathPre() + filename + CompileFileConstant.FILE_SUFFIX;
    }

    public String createPathForFile(
            PackageMessage beforeFilePackage, String filename, String[] fullname) {
        int start = beforeFilePackage.getPackages().length;
        StringJoiner joiner = new StringJoiner(CompileFileConstant.STRUCTURE_SEPARATOR);
        for (int i = start; i < fullname.length; i++) {
            joiner.add(fullname[i]);
        }
        return beforeFilePackage.getSourceFilepathPre() +
               filename +
               CompileFileConstant.STRUCTURE_SEPARATOR +
               joiner +
               CompileFileConstant.FILE_SUFFIX;
    }

    /**
     * @param fullIdentifier 必须是file级别的, 否则不会报错, 但会有逻辑错误
     * @return 可能是source, 可能是target, 先从target找, 再从source找, index is true file(source, but not structure compiled)<br>
     * fileListPoint.element 可能是null, for package
     */
    public ListPoint<File> filePathForImport(FullIdentifierString fullIdentifier) {
        // 检查和同名软件包冲突
        String[] fullname = fullIdentifier.getFullname();
        ListPoint<File> fileListPoint = getFileAsLaterAsPossibleInTarget(fullname);
        if (fileListPoint != null) {
            return fileListPoint;
        }
        // 还没有编译
        // 在源码
        fileListPoint = findFileInSource(fullname);
        File file = fileListPoint.getElement();
        if (file != null && file.isFile()) {
            return fileListPoint;
        }
        // 不在源码, TODO 可能在库, 但还不支持
        if (file == null) {
            SourcePosition illegalPoint = fullIdentifier.getPositionAt(findNotExistFileIndexInSource(fullname));
            throw new AnalysisException(illegalPoint, "not exist");
        }
        // package
        return new ListPoint<>(fileListPoint.getIndex(), null);
    }

    /**
     * @return 有结构回最接近fullname的结构, 有只有文件返回文件
     */
    public ListPoint<File> getFileAsLaterAsPossibleInTarget(String[] fullname) {
        ListPoint<File> fileListPoint = findFileInTarget(fullname);
        File file = fileListPoint.getElement();
        if (file == null || !file.isFile()) {
            return null;
        }
        // 编译了
        // 需要知道fileIndex在哪里...
        // 还有structure...
        File structureFileInTarget = findStructureFileInTarget(fileListPoint, fullname).getElement();
        return new ListPoint<>(fileListPoint.getIndex(), structureFileInTarget);
    }

    public File findAsPackage(SourcePosition position, String[] fullIdentifier) {
        StringBuilder pathBuilder = new StringBuilder(targetAbsoluteFilePathPre);
        File directory = null;
        for (String each : fullIdentifier) {
            pathBuilder.append(each);
            directory = new File(pathBuilder.toString());
            if (!directory.exists()) {
                throw new VieIOException(new FileNotFoundException(directory.getAbsolutePath()));
            }
            if (!directory.isDirectory()) {
                throw new AnalysisException(
                        position,
                        directory.getAbsolutePath() + " expect a package!"
                );
            }
        }
        return directory;
    }

    public File findAsPackage(FullIdentifierString fullIdentifier) {
        String[] fullname = fullIdentifier.getFullname();
        StringBuilder pathBuilder = new StringBuilder(targetAbsoluteFilePathPre);
        File directory = null;
        for (int i = 0, fullnameLength = fullname.length; i < fullnameLength; i++) {
            String each = fullname[i];
            pathBuilder.append(each);
            directory = new File(pathBuilder.toString());
            if (!directory.exists()) {
                throw new VieIOException(new FileNotFoundException(directory.getAbsolutePath()));
            }
            if (!directory.isDirectory()) {
                throw new AnalysisException(
                        fullIdentifier.getPositionAt(i),
                        directory.getAbsolutePath() + " expect a package!"
                );
            }
        }
        return directory;
    }

    /**
     * @return identifier[identifier.length - 1];
     */
    public static String getNameIfAlias(String[] fullnameFromFile, String[] identifier) {
        if (fullnameFromFile.length + 1 != identifier.length) {
            return null;
        }
        for (int i = 0; i < fullnameFromFile.length; i++) {
            if (fullnameFromFile[i].equals(identifier[i])) {
                return null;
            }
        }
        return identifier[identifier.length - 1];
    }
    /**
     * @return file 特指 源码的文件, 而不是structure在编译时会产生的类
     */
    public ListPoint<File> findFileInTarget(String[] fullname) {
        StringBuilder pathBuilder = new StringBuilder(targetAbsoluteFilePathPre);
        File directory = null;
        for (int i = 0; i < fullname.length; i++) {
            pathBuilder.append(fullname[i]);
            directory = new File(pathBuilder.toString());
            File file = new File(pathBuilder + CompileFileConstant.FILE_SUFFIX);
            pathBuilder.append(File.separator);
            validFile(file, directory);
            if (file.exists()) {
                return new ListPoint<>(i, file);
            } else if (!directory.exists()) {
                // 都不存在
                // 还没有编译 or structure
                // 但如果是structure, 一定有对应的文件在之前就被检测break了
                // 所以一定是没有编译(或信息丢失了)
                return new ListPoint<>(i, null);
            }
        }
        return new ListPoint<>(fullname.length, directory);
    }

    private int findNotExistFileIndexInSource(String[] fullname) {
        StringBuilder pathBuilder = new StringBuilder(sourceAbsoluteFilePathPre);
        File directory;
        for (int i = 0; i < fullname.length; i++) {
            pathBuilder.append(fullname[i]);
            directory = new File(pathBuilder.toString());
            File file = new File(pathBuilder + SourceFileConstant.FILE_SUFFIX);
            pathBuilder.append(File.separator);
            if (!file.exists() && !directory.exists()) {
                return i;
            }
        }
        throw new CompilerException("all exist is meaning less");
    }

    private ListPoint<File> findFileInSource(String[] fullname) {
        StringBuilder pathBuilder = new StringBuilder(sourceAbsoluteFilePathPre);
        File directory = null;
        for (int i = 0; i < fullname.length; i++) {
            String s = fullname[i];
            pathBuilder.append(s);
            directory = new File(pathBuilder.toString());
            File file = new File(pathBuilder + SourceFileConstant.FILE_SUFFIX);
            pathBuilder.append(File.separator);
            validFile(file, directory);
            if (file.exists()) {
                return new ListPoint<>(i, file);
            } else if (!directory.exists()) {
                // 都不存在
                // 缺失文件
                return new ListPoint<>(i, null);
            }
        }
        return new ListPoint<>(fullname.length, directory);
    }


}
