package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.cache.resource.StructureContextResource;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;

/**
 * 对复合结构文件的FileNode
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 21:27
 */
public class StatementStructureFileNode extends AbstractStructureFileNode {
    public StatementStructureFileNode(String simpleName, String[] fullname, File file) {
        super(simpleName, fullname, file, null);
    }

    public StatementStructureFileNode(String simpleName, String joinedFullname, File file) {
        super(simpleName, joinedFullname, file, null);
    }

    public StatementStructureFileNode(String simpleName, String[] fullname, StatementResource resource) {
        super(simpleName, fullname, resource.getFile(), resource);
    }

    public StatementStructureFileNode(String simpleName, String joinedFullname, StatementResource resource) {
        super(simpleName, joinedFullname, resource.getFile(), resource);
    }

    @Override
    public FileNode setResource(StatementResource resource) {
        if (!(resource instanceof StructureContextResource)) {
            throw new CompilerException("excepted a StructureContextResource, but: " + resource.getClass().getName());
        }
        return super.setResource(resource);
    }

    @Override
    public CompileStage getStage() {
        return CompileStage.STATEMENT;
    }
}
