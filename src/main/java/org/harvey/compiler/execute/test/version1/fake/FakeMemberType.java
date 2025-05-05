package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-11 00:02
 */
public class FakeMemberType implements MemberType {
    @Override
    public MemberSupplier find(SourcePosition position, String innerMemberName) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier find(SourcePosition position, Operator operator) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier find(SourcePosition position, Operator[] sameNameOperator) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier findPossibleCallable(SourcePosition position, String callableName) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier findCastOperator(SourcePosition position, MemberType castTarget) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier findConstructor(SourcePosition position) {
        throw new UnfinishedException();
    }

    @Override
    public PossibleCallableSupplier findInnerTypeConstructor(SourcePosition position, MemberType memberType) {
        throw new UnfinishedException();
    }

    @Override
    public boolean strictSame(MemberType other) {
        throw new UnfinishedException();
    }

}
