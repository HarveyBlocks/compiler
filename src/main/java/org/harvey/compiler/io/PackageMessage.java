package org.harvey.compiler.io;

import lombok.Getter;
import org.harvey.compiler.common.constant.CompileCommandConstant;
import org.harvey.compiler.common.constant.CompileFileConstant;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.io.VieIOException;
import org.harvey.compiler.exception.self.CompilerException;

import java.io.File;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 10:32
 */
@Getter
public class PackageMessage {
    public static final String PACKAGE_SEPARATOR = CompileCommandConstant.PACKAGE_SEPARATOR;
    private final String sourceFilepathPre;
    private final String compiledFilepathPre;
    private final String packageString;
    private final String[] packages;
    /**
     * separator and end with /
     */
    private final String packagePath;

    public PackageMessage(
            String sourceAbsoluteFilePathPre,
            String targetAbsoluteFilePathPre,
            String packageString) {
        this.packageString = packageString;
        this.packages = StringUtil.simpleSplit(packageString, CompileCommandConstant.PACKAGE_SEPARATOR);
        this.packagePath = StringUtil.join(packages, File.separator) + File.separator;
        this.sourceFilepathPre = sourceAbsoluteFilePathPre + packagePath;
        this.compiledFilepathPre = targetAbsoluteFilePathPre + packagePath;
    }

    public PackageMessage(
            String sourceAbsoluteFilePathPre,
            String targetAbsoluteFilePathPre,
            String[] packages) {
        this.packages = packages;
        this.packageString = StringUtil.join(packages, PACKAGE_SEPARATOR);
        this.packagePath = StringUtil.join(packages, File.separator);
        this.sourceFilepathPre = sourceAbsoluteFilePathPre + packagePath;
        this.compiledFilepathPre = targetAbsoluteFilePathPre + packagePath;
    }

    public PathBuilder getSource() {
        return new PathBuilder(sourceFilepathPre, true);
    }

    public PathBuilder getCompile() {
        return new PathBuilder(compiledFilepathPre, false);
    }

    public PackageMessage brith(String child) {
        return new PackageMessage(
                sourceFilepathPre, compiledFilepathPre, packageString + PACKAGE_SEPARATOR + child);
    }

    public String[] getFullname(String filename) {
        String[] ns = new String[1 + packages.length];
        System.arraycopy(packages, 0, ns, 0, packages.length);
        ns[ns.length - 1] = filename;
        return ns;
    }

    /**
     * @return {@link #packages}
     */
    public int length() {
        return packages.length;
    }

    public File build() {
        File directory = new File(compiledFilepathPre);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                throw new VieIOException("can not create directory: " + compiledFilepathPre);
            }
        }
        return directory;
    }

    public static class PathBuilder {
        private final StringBuilder builder;
        private final boolean source;


        private PathBuilder(String pre, boolean source) {
            builder = new StringBuilder(pre);
            this.source = source;
        }

        public PathBuilder appendDirectory(String directory) {
            builder.append(directory).append(File.separator);
            return this;
        }

        public PathBuilder appendFile(String file) {
            builder.append(file);
            return this;
        }

        public PathBuilder appendStructure(String structure) {
            if (source) {
                throw new CompilerException("con not append structure when genericDefine a source path");
            }
            builder.append(CompileFileConstant.STRUCTURE_SEPARATOR).append(structure);
            return this;
        }

        public PathBuilder suffix() {
            builder.append(source ? SourceFileConstant.FILE_SUFFIX : CompileFileConstant.FILE_SUFFIX);
            return this;
        }

        public File build() {
            return new File(builder.toString());
        }
    }


}
