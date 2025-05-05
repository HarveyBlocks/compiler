package org.harvey.compiler.io.cache.node;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.cache.resource.FileContextResource;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.io.stage.CompileStage;

import java.io.File;

/**
 * 对普通文件的FileNode
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 21:24
 */
public class StatementRawFileNode extends AbstractRawFileNode {

    public StatementRawFileNode(String simpleName, String[] fullname, File file) {
        super(simpleName, fullname, file, null);
    }

    public StatementRawFileNode(String simpleName, String joinedFullname, File file) {
        super(simpleName, joinedFullname, file, null);
    }

    public StatementRawFileNode(String simpleName, String[] fullname, StatementResource resource) {
        super(simpleName, fullname, resource.getFile(), resource);
    }

    public StatementRawFileNode(String simpleName, String joinedFullname, StatementResource resource) {
        super(simpleName, joinedFullname, resource.getFile(), resource);
    }

    @Override
    public FileNode setResource(StatementResource resource) {
        if (!(resource instanceof FileContextResource)) {
            throw new CompilerException("excepted a file context resource, but: " + resource.getClass().getName());
        }
        return super.setResource(resource);
    }

    @Override
    public CompileStage getStage() {
        return CompileStage.STATEMENT;
    }
}
