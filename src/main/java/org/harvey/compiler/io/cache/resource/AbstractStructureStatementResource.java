package org.harvey.compiler.io.cache.resource;

import org.harvey.compiler.io.cache.ImportType;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:50
 */
public abstract class AbstractStructureStatementResource implements StatementResource {
    @Override
    public ImportType getImportType() {
        return ImportType.STRUCTURE;
    }

    @Override
    public final boolean isFile() {
        return false;
    }

    @Override
    public final boolean isStructure() {
        return true;
    }

    @Override
    public final boolean isPackage() {
        return false;
    }
}
