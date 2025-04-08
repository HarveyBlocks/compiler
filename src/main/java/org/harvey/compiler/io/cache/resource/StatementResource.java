package org.harvey.compiler.io.cache.resource;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.context.ConstructorContext;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.cache.ImportType;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link org.harvey.compiler.io.stage.CompileStage#STATEMENT} 阶段的文件资源
 * statement的resource只关注这几点
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-18 20:37
 */
public interface StatementResource {
    Object getRaw();

    ImportType getImportType();

    /**
     * 这个static是广义的static, 而不是单独指static关键字, 修饰符. 对于文件重的元素元素来说, 都是static的, 对于类成员, 就取决于static修饰了
     */
    boolean isStatic();

    IdentifierManager getManager();

    ParameterizedType<ReferenceElement> getSuperComplexStructure();

    List<ParameterizedType<ReferenceElement>> getInterfaceList();

    StructureType getStructureType();


    List<TypeAlias> getTypeAliases();

    default boolean isFile() {
        return false;
    }

    default boolean isStructure() {
        return false;
    }


    default boolean isPackage() {
        return false;
    }

    ReferenceElement getDeclareIdentifierReference();

    boolean isSealed();

    File getFile();

    AccessControl getAccessControl();

    default TypeAlias getTypeAlias(FullIdentifierString identifier) {
        return getTypeAlias(identifier, identifier.joinFullnameString(Operator.GET_MEMBER.getName()));
    }

    default TypeAlias getTypeAlias(FullIdentifierString identifier, String joinedFullnameString) {
        File resourceFile = this.getFile();
        IdentifierManager manager = this.getManager();
        List<TypeAlias> aliasList = this.getTypeAliases();
        ReferenceElement referenceOfDeclare = manager.getFromDeclare(identifier.getFullname());
        if (referenceOfDeclare == null || referenceOfDeclare.getType() != ReferenceType.IDENTIFIER) {
            throw new CompileMultipleFileException(resourceFile, identifier.getPositionAt(identifier.length() - 1),
                    "expected a alias or a type"
            );
        }
        int reference = referenceOfDeclare.getReference();
        List<TypeAlias> collect = aliasList.stream()
                .filter(e -> e.getAliasNameReference().getReference() == reference)
                .collect(Collectors.toList());
        return CollectionUtil.one(collect, new CompilerException(
                        "One identifier of " + joinedFullnameString + " in identifier pool, but has multiple declare"),
                new CompilerException("Not declared type " + joinedFullnameString + " but in identifier pool")
        );
    }

    GenericDefine[] getGenericMessage();

    List<ConstructorContext> getConstructors();
}
