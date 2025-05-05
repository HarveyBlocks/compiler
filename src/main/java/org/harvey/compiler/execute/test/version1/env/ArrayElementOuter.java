package org.harvey.compiler.execute.test.version1.env;

import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.element.ArrayInitEachWarp;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * 所有的ArrayElement的init{@link ArrayInitEachWarp}的环境
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:50
 */
public class ArrayElementOuter implements OuterEnvironment {
    private final DefaultOuterEnvironment environment;

    public ArrayElementOuter(OuterEnvironment outerEnvironment) {
        // 避免递归
        environment = new OuterEnvironmentBuilder().outer(outerEnvironment).setDeterminedType(null).build();
    }


    @Override
    public MemberManager getMemberManager() {
        return environment.getMemberManager();
    }

    @Override
    public MemberSupplier createFromMemberManager(SourcePosition using, String name) {
        return environment.createFromMemberManager(using, name);
    }

    @Override
    public boolean isType(int type) {
        return type == OuterEnvironment.IN_ARRAY_INIT_ELEMENT;
    }

    @Override
    public PossibleCallableSupplier createPossibleFromMemberManager(SourcePosition using, String callableName) {
        return environment.createPossibleFromMemberManager(using, callableName);
    }

    @Override
    public DIdentifierManager getIdentifierManager() {
        return environment.getIdentifierManager();
    }

    @Override
    public MemberType relateParameterizedType(ParameterizedType<ReferenceElement> element) {
        return environment.relateParameterizedType(element);
    }

    @Override
    public OuterEnvironment getOuter() {
        return environment;
    }

    @Override
    public int getType() {
        return IN_ARRAY_INIT_ELEMENT;
    }

    @Override
    public RelationManager getRelationManager() {
        return environment.getRelationManager();
    }

    @Override
    public boolean typeDetermined() {
        return environment.typeDetermined();
    }

    @Override
    public MemberType determinedType() {
        return environment.determinedType();
    }

    public void availableContext(ExpressionContext context) {
        if (!context.hasNext()) {
            throw new AnalysisExpressionException(context.next().getPosition(), "not allowed after array init");
        }
    }
}
