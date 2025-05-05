package org.harvey.compiler.io.cache.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.context.ConstructorContext;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.PackageMessage;
import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.File;
import java.util.List;

/**
 * 包文件资源
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:43
 */
@Getter
@AllArgsConstructor
public final class PackageStatementResource implements StatementResource {
    private final PackageMessage raw;

    @Override
    public ImportType getImportType() {
        return ImportType.PACKAGE;
    }

    @Override
    public boolean isStatic() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public DIdentifierManager getManager() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public ParameterizedType<ReferenceElement> getSuperComplexStructure() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public List<ParameterizedType<ReferenceElement>> getInterfaceList() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public StructureType getStructureType() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public List<TypeAlias> getTypeAliases() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }


    @Override
    public boolean isPackage() {
        return true;
    }

    @Override
    public ReferenceElement getDeclareIdentifierReference() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public boolean isSealed() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public File getFile() {
        return raw.build();
    }

    @Override
    public AccessControl getAccessControl() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public GenericDefine[] getGenericMessage() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }

    @Override
    public List<ConstructorContext> getConstructors() {
        throw new CompilerException("resource is a package", new UnsupportedOperationException());
    }
}
