package org.harvey.compiler.io.cache.resource;

import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.context.ConstructorContext;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:43
 */
public abstract class AbstractFileStatementResource implements StatementResource {
    @Override
    public final ParameterizedType<ReferenceElement> getSuperComplexStructure() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public List<ConstructorContext> getConstructors() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public final boolean isSealed() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public boolean isStatic() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public final List<ParameterizedType<ReferenceElement>> getInterfaceList() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public final StructureType getStructureType() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public ReferenceElement getDeclareIdentifierReference() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public AccessControl getAccessControl() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public GenericDefine[] getGenericMessage() {
        throw new CompilerException("resource is a file", new UnsupportedOperationException());
    }

    @Override
    public final ImportType getImportType() {
        return ImportType.FILE;
    }

    @Override
    public final boolean isFile() {
        return true;
    }

    @Override
    public final boolean isStructure() {
        return false;
    }

    @Override
    public final boolean isPackage() {
        return false;
    }
}
