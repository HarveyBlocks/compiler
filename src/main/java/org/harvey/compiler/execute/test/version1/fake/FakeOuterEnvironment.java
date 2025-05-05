package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
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
 * @date 2025-04-10 23:48
 */
public class FakeOuterEnvironment implements OuterEnvironment {
    private final MemberManager manager;
    private final DIdentifierManager identifierManager;

    public FakeOuterEnvironment(FakeMemberManager fakeMemberManager, DIdentifierManager identifierManager) {
        this.manager = fakeMemberManager;
        this.identifierManager = identifierManager;
    }

    @Override
    public MemberManager getMemberManager() {
        return manager;
    }

    @Override
    public MemberSupplier createFromMemberManager(SourcePosition using, String name) {
        return manager.create(using, name);
    }

    @Override
    public boolean isType(int type) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier createPossibleFromMemberManager(SourcePosition using, String callableName) {
        return manager.createPossibleCallable(using, callableName);
    }

    @Override
    public DIdentifierManager getIdentifierManager() {
        return identifierManager;
    }

    @Override
    public MemberType relateParameterizedType(ParameterizedType<ReferenceElement> element) {
        throw new UnfinishedException();
    }

    @Override
    public OuterEnvironment getOuter() {
        // TODO throw new UnfinishedException();
        return null;
    }

    @Override
    public int getType() {
        throw new UnfinishedException();
    }

    @Override
    public RelationManager getRelationManager() {
        throw new UnfinishedException();
    }

    @Override
    public boolean typeDetermined() {
        throw new UnfinishedException();
    }

    @Override
    public MemberType determinedType() {
        throw new UnfinishedException();
    }
}
