package org.harvey.compiler.execute.test.version1.env;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:50
 */
public class CallableArgumentOuter implements OuterEnvironment {
    private final DefaultOuterEnvironment environment;
    private final PossibleCallableSupplier possibleCallableRelatedDeclare;
    private final MemberType[] genericList;
    @Setter
    @Getter
    private int finishedIndexOfParam;

    public CallableArgumentOuter(
            PossibleCallableSupplier possibleCallableRelatedDeclare,
            MemberType[] genericList, OuterEnvironment outerEnvironment) {
        this.possibleCallableRelatedDeclare = possibleCallableRelatedDeclare;
        this.genericList = genericList;
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
        return type == OuterEnvironment.IN_CALLABLE_ARGUMENT;
    }

    @Override
    public PossibleCallableSupplier createPossibleFromMemberManager(SourcePosition using, String callableName) {
        return environment.createPossibleFromMemberManager(using, callableName);
    }

    @Override
    public IdentifierManager getIdentifierManager() {
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
        return IN_CALLABLE_ARGUMENT;
    }

    @Override
    public RelationManager getRelationManager() {
        return environment.getRelationManager();
    }

    @Override
    public boolean typeDetermined() {
        return determinedType() != null;
    }

    @Override
    public MemberType determinedType() {
        // 不可能没有
        CallableRelatedDeclare[] possibleCallableRelatedDeclares = possibleCallableRelatedDeclare.getPossibleCallableRelatedDeclare();
        MemberType first = possibleCallableRelatedDeclares[0].getParameterType(this.finishedIndexOfParam);
        if (possibleCallableRelatedDeclares.length == 1) {
            return first;
        }
        for (CallableRelatedDeclare callableRelatedDeclare : possibleCallableRelatedDeclares) {
            if (!first.strictSame(callableRelatedDeclare.getParameterType(this.finishedIndexOfParam))) {
                return null;
            }
        }
        return first;
    }
}
