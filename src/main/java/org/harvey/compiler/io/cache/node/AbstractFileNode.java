package org.harvey.compiler.io.cache.node;

import lombok.Getter;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.io.cache.resource.StatementResource;

import java.io.File;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 20:41
 */
@Getter
public abstract class AbstractFileNode implements FileNode {
    private final String simpleName;
    private final String joinedFullname;
    private final String[] fullname;
    private final File file;
    private StatementResource object;


    protected AbstractFileNode(String simpleName, String[] fullname, File file) {
        this(simpleName, fullname, StringUtil.join(fullname, FileNode.GET_MEMBER), file, null);
    }

    protected AbstractFileNode(String simpleName, String joinedFullname, File file) {
        this(simpleName, StringUtil.simpleSplit(joinedFullname, GET_MEMBER), joinedFullname, file, null);
    }

    protected AbstractFileNode(String simpleName, String[] fullname, File file, StatementResource resource) {
        this(simpleName, fullname, StringUtil.join(fullname, FileNode.GET_MEMBER), file, resource);
    }

    protected AbstractFileNode(String simpleName, String joinedFullname, File file, StatementResource resource) {
        this(simpleName, StringUtil.simpleSplit(joinedFullname, GET_MEMBER), joinedFullname, file, resource);
    }

    private AbstractFileNode(
            String simpleName, String[] fullname, String joinedFullname, File file, StatementResource object) {

        this.simpleName = simpleName;
        this.fullname = fullname;
        this.joinedFullname = joinedFullname;
        this.file = file;
        setResource(object);
    }

    @Override
    public StatementResource getResource() {
        return object;
    }

    @Override
    public FileNode setResource(StatementResource resource) {
        this.object = resource;
        return this;
    }


    @Override
    public final boolean isFile() {
        return object.isFile();
    }

    @Override
    public final boolean isPackage() {
        return object.isPackage();
    }

    @Override
    public final boolean isStructure() {
        return object.isStructure();
    }
}
