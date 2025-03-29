package org.harvey.compiler.io.cache.resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.context.ConstructorContext;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.serializer.StructureStatementFileSerializer;
import org.harvey.compiler.text.depart.RecursivelyDepartedBodyFactory;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.File;
import java.util.List;

/**
 * 复合结构文件文件资源
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:51
 */
@AllArgsConstructor
@Getter
public class StructureContextResource extends AbstractStructureStatementResource {
    private final StructureStatementFileSerializer serializer;

    @Override
    public Object getRaw() {
        return serializer.getResource();
    }

    @Override
    public boolean isStatic() {
        return serializer.getResource().getOuterStructure() == RecursivelyDepartedBodyFactory.FILE_OUTER ||
               serializer.getResource().getEmbellish().isMarkedStatic();
    }


    @Override
    public boolean isSealed() {
        return serializer.getResource().getEmbellish().isMarkedSealed();
    }

    @Override
    public File getFile() {
        return serializer.getFile();
    }

    @Override
    public AccessControl getAccessControl() {
        return serializer.getResource().getAccessControl();
    }

    @Override
    public GenericDefine[] getGenericMessage() {
        return serializer.getResource().getGenericMessage();
    }

    @Override
    public List<ConstructorContext> getConstructors() {
        return serializer.getResource().getConstructors();
    }

    @Override
    public IdentifierManager getManager() {
        return serializer.getResource().getManager();
    }

    @Override
    public ParameterizedType<ReferenceElement> getSuperComplexStructure() {
        return serializer.getResource().getSuperStructure();
    }

    @Override
    public List<ParameterizedType<ReferenceElement>> getInterfaceList() {
        return serializer.getResource().getInterfaceList();
    }

    @Override
    public StructureType getStructureType() {
        return serializer.getResource().getType();
    }

    @Override
    public List<TypeAlias> getTypeAliases() {
        return serializer.getResource().getTypeAliases();
    }

    @Override
    public ReferenceElement getDeclareIdentifierReference() {
        return serializer.getResource().getIdentifierReference();
    }

}
