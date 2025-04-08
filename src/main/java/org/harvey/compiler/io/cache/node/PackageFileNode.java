package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.io.cache.resource.PackageStatementResource;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;

/**
 * 对包的FileNode
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 21:07
 */
public final class PackageFileNode extends AbstractFileNode {


    public PackageFileNode(String simpleName, String[] fullname, File file) {
        super(simpleName, fullname, file, null);
    }

    public PackageFileNode(String simpleName, String joinedFullname, File file) {
        super(simpleName, joinedFullname, file, null);
    }

    public PackageFileNode(String simpleName, String[] fullname, File file, StatementResource resource) {
        super(simpleName, fullname, file, resource);
    }

    public PackageFileNode(String simpleName, String joinedFullname, File file, StatementResource resource) {
        super(simpleName, joinedFullname, file, resource);
    }

    @Override
    public ImportType getImportType() {
        return ImportType.PACKAGE;
    }

    @Override
    public FileNode setResource(StatementResource resource) {
        if (!(resource instanceof PackageStatementResource)) {
            throw new CompilerException("excepted a PackageStatementResource, but: " + resource.getClass().getName());
        }
        return super.setResource(resource);
    }

    @Override
    public CompileStage getStage() {
        return CompileStage.PACKAGE;
    }

}
