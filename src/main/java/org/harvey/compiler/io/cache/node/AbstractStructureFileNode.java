package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.io.cache.resource.StatementResource;

import java.io.File;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 21:28
 */
public abstract class AbstractStructureFileNode extends AbstractFileNode {

    protected AbstractStructureFileNode(String simpleName, String[] fullname, File file) {
        super(simpleName, fullname, file, null);
    }

    protected AbstractStructureFileNode(String simpleName, String joinedFullname, File file) {
        super(simpleName, joinedFullname, file, null);
    }

    protected AbstractStructureFileNode(String simpleName, String[] fullname, File file, StatementResource resource) {
        super(simpleName, fullname, file, resource);
    }

    public AbstractStructureFileNode(
            String simpleName,
            String joinedFullname,
            File file,
            StatementResource resource) {
        super(simpleName, joinedFullname, file, resource);
    }

    @Override
    public ImportType getImportType() {
        return ImportType.STRUCTURE;
    }
}
