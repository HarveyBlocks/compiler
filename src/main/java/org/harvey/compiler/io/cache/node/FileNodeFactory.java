package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.PackageMessage;
import org.harvey.compiler.io.cache.resource.FileContextResource;
import org.harvey.compiler.io.cache.resource.PackageStatementResource;
import org.harvey.compiler.io.cache.resource.StructureContextResource;
import org.harvey.compiler.io.serializer.OnlyFileStatementSerializer;
import org.harvey.compiler.io.serializer.StatementFileSerializer;
import org.harvey.compiler.io.serializer.StructureStatementFileSerializer;

import java.io.File;

/**
 * 构建{@link FileNode}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 22:10
 */
@SuppressWarnings("DuplicatedCode")
public class FileNodeFactory {


    public static FileNode create(
            String simpleName, String joinedFullname, StatementFileSerializer serializer) {
        if (serializer instanceof OnlyFileStatementSerializer) {
            return new StatementRawFileNode(
                    simpleName, joinedFullname, new FileContextResource((OnlyFileStatementSerializer) serializer));
        } else if (serializer instanceof StructureStatementFileSerializer) {
            return new StatementStructureFileNode(simpleName, joinedFullname,
                    new StructureContextResource((StructureStatementFileSerializer) serializer)
            );
        }
        throw new CompilerException(serializer.getFile().getPath() + "Unknown type resource");
    }

    public static FileNode create(
            String simpleName, String[] fullname, StatementFileSerializer serializer) {
        if (serializer instanceof OnlyFileStatementSerializer) {
            return new StatementRawFileNode(
                    simpleName, fullname, new FileContextResource((OnlyFileStatementSerializer) serializer));
        } else if (serializer instanceof StructureStatementFileSerializer) {
            return new StatementStructureFileNode(simpleName, fullname,
                    new StructureContextResource((StructureStatementFileSerializer) serializer)
            );
        }
        throw new CompilerException(serializer.getFile().getPath() + "Unknown type resource");
    }

    public static FileNode createForPackage(
            String simpleName, String joinedFullname, File file, PackageMessage thisPackage) {
        return new PackageFileNode(simpleName, joinedFullname, file, new PackageStatementResource(thisPackage));
    }

    public static FileNode createForPackage(
            String simpleName, String[] fullname, File file, PackageMessage thisPackage) {
        return new PackageFileNode(simpleName, fullname, file, new PackageStatementResource(thisPackage));
    }
}
