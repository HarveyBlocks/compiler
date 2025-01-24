package org.harvey.compiler.declare.phaser.phaser;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.*;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.common.util.Singleton;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.declare.EmbellishSourceString;
import org.harvey.compiler.declare.context.ComplexStructureContext;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionFactory;
import org.harvey.compiler.execute.expression.SourceVariableDeclare;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 22:27
 */
public class StructurePhaser implements DeclarePhaser<ComplexStructureContext> {
    public static final Singleton<StructurePhaser> SINGLETON = new Singleton<>();

    private StructurePhaser() {
    }


    public static StructurePhaser instance() {
        return SINGLETON.instance(StructurePhaser::new);
    }

    private static StructureType getType(SourceTextContext typeContext, boolean isAbstract) {
        if (typeContext.isEmpty()) {
            throw new CompilerException("excepted type");

        }
        SourceString typeSource = typeContext.get(0);
        Keyword typeKeyword = Keyword.get(typeSource.getValue());
        if (typeContext.size() != 1 || !Keywords.isComplexStructure(typeKeyword)) {
            throw new AnalysisExpressionException(typeContext.getFirst().getPosition(),
                    typeContext.getLast().getPosition(), "excepted class");
        }
        StructureType structureType = StructureType.get(typeKeyword);
        if (structureType != null && isAbstract) {
            try {
                structureType = StructureType.embellishAbstract(structureType);
            } catch (CompilerException ce) {
                throw new AnalysisExpressionException(typeSource.getPosition(),
                        "conflict with abstract, " + ce.getMessage());
            }
        }
        return structureType;
    }

    @Override
    public ComplexStructureContext phase(Declarable declarable, int identifierIndex, Environment environment) {
        if (!declarable.isComplexStructure()) {
            throw new CompilerException("expected a complex structure");
        }
        if (identifierIndex < 0) {
            throw new CompilerException("identifier is needed");
        }
        // extends表
        // implements表
        ListIterator<SourceString> attachmentIterator = declarable.getAttachment().listIterator();
        StructureType type = getType(declarable.getType(), declarable.getEmbellish().getAbstractMark() != null);
        ComplexStructureContext.Builder builder = new ComplexStructureContext.Builder().accessControl(
                        phasePermission(declarable.getPermissions(), environment, type))
                .embellish(phaseEmbellish(declarable.getEmbellish(), environment, type))
                .type(type)
                .identifierReference(identifierIndex)
                .genericMessage(phaseGenericMessage(attachmentIterator, type))
                .superComplexStructure(phaseSuper(attachmentIterator, type))
                .addInterface(phaseImplements(attachmentIterator, type));
        if (attachmentIterator.hasNext()) {
            throw new AnalysisExpressionException(attachmentIterator.next().getPosition(), "excepted {");
        }
        return builder.build(declarable.getStart());
    }

    private Expression phaseGenericMessage(ListIterator<SourceString> iterator, StructureType type) {
        if (!CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.GENERIC_LIST_PRE))) {
            // 没有`<`开头
            // 认为没有泛型
            return Expression.EMPTY;
        }
        if (type == StructureType.ENUM || type == StructureType.INTERFACE) {
            throw new AnalysisException(iterator.next().getPosition(),
                    "generic message is conflict with " + type.name());
        }
        return ExpressionFactory.genericMessage(iterator);
    }

    private Expression phaseSuper(ListIterator<SourceString> iterator, StructureType type) {
        if (!iterator.hasNext()) {
            return Expression.EMPTY;
        }
        SourceString extendsKeyword = iterator.next();
        if (extendsKeyword.getType() != SourceStringType.KEYWORD ||
                !Keyword.EXTENDS.equals(extendsKeyword.getValue())) {
            return Expression.EMPTY;
        }
        if (type == StructureType.ENUM) {
            throw new AnalysisExpressionException(extendsKeyword.getPosition(), "enum can not extends");
        }
        if (!iterator.hasNext()) {
            throw new AnalysisExpressionException(extendsKeyword.getPosition(), "expected a super type");
        }
        SourceVariableDeclare.LocalType localType = SourceVariableDeclare.localType(iterator,
                extendsKeyword.getPosition());
        if (localType.isFinal() || localType.isConst()) {
            throw new AnalysisExpressionException(localType.getFinalPosition(), "not allowed here");
        }
        return localType.getSourceType();
    }

    private List<Expression> phaseImplements(ListIterator<SourceString> iterator, StructureType type) {
        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }
        SourceString implementsKeyword = iterator.next();
        if (implementsKeyword.getType() != SourceStringType.KEYWORD ||
                !Keyword.IMPLEMENTS.equals(implementsKeyword.getValue())) {
            throw new AnalysisExpressionException(implementsKeyword.getPosition(), "excepted implements");
        }
        if (type == StructureType.ENUM) {
            throw new AnalysisExpressionException(implementsKeyword.getPosition(), "enum can not implements");
        } else if (type == StructureType.INTERFACE) {
            throw new AnalysisExpressionException(implementsKeyword.getPosition(),
                    "interface can not implements, It is suggested to change to: extends!");
        }
        List<SourceVariableDeclare.LocalType> localTypes = SourceVariableDeclare.phaseTypeList(iterator, null);
        List<Expression> result = new ArrayList<>();
        for (SourceVariableDeclare.LocalType localType : localTypes) {
            if (localType.isFinal() || localType.isConst()) {
                throw new AnalysisExpressionException(localType.getFinalPosition(), "not allowed here");
            }
            result.add(localType.getSourceType());
        }
        return result;
    }

    private AccessControl phasePermission(SourceTextContext permissions, Environment environment, StructureType type) {
        switch (environment) {
            case FILE:
                return AccessControls.buildFileAccessControl(permissions, "complex structure", Permission.FILE);
            case ENUM:
            case CLASS:
            case STRUCT:
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
                return AccessControls.buildMemberAccessControl(permissions, Permission.PRIVATE);
            case INTERFACE:
                return AccessControls.buildMemberAccessControl(permissions, Permission.PUBLIC);
            default:
                throw new CompilerException("Unknown environment");
        }
        /*switch (type){
            // 没有
        }*/
    }

    private Embellish phaseEmbellish(EmbellishSourceString embellish, Environment environment, StructureType type) {
        // static const final abstract sealed
        DeclarePhaser.forbidden(embellish.getConstMark());
        DeclarePhaser.forbidden(embellish.getFinalMark());
        // interface里默认是static的
        if (environment == Environment.FILE) {
            DeclarePhaser.forbidden(embellish.getStaticMark());
            switch (type) {
                case ABSTRACT_CLASS:
                case ABSTRACT_STRUCT:
                case INTERFACE:
                    DeclarePhaser.forbidden(embellish.getSealedMark());
            }
        } else if (type == StructureType.ABSTRACT_CLASS ||
                type == StructureType.ABSTRACT_STRUCT ||
                type == StructureType.INTERFACE) {
            DeclarePhaser.forbidden(embellish.getSealedMark());

        }
        return new Embellish(embellish);
    }
}
