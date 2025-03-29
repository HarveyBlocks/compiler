package org.harvey.compiler.io.cache.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.io.serializer.OnlyFileStatementSerializer;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:47
 */
@AllArgsConstructor
@Getter
public class FileContextResource extends AbstractFileStatementResource {
    private final OnlyFileStatementSerializer serializer;

    @Override
    public Object getRaw() {
        return serializer.getResource();
    }

    @Override
    public IdentifierManager getManager() {
        return serializer.getResource().getIdentifierManager();
    }

    @Override
    public List<TypeAlias> getTypeAliases() {
        return serializer.getResource().getAliasList();
    }

    @Override
    public File getFile() {
        return serializer.getFile();
    }
}
