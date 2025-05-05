package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-11 00:16
 */
public class Int32Type implements MemberType {
    public static final Int32Type INT32 = new Int32Type();

    private Int32Type() {
    }

    @Override
    public MemberSupplier find(SourcePosition position, String innerMemberName) {
        throw new AnalysisExpressionException(position, "can not find member: " + innerMemberName);
    }

    @Override
    public PossibleCallableSupplier find(SourcePosition position, Operator operator) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public PossibleCallableSupplier find(SourcePosition position, Operator[] sameNameOperator) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public PossibleCallableSupplier findPossibleCallable(SourcePosition position, String callableName) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public PossibleCallableSupplier findCastOperator(SourcePosition position, MemberType castTarget) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public PossibleCallableSupplier findConstructor(SourcePosition position) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public PossibleCallableSupplier findInnerTypeConstructor(SourcePosition position, MemberType memberType) {
        return new FakePossibleCallableSupplier(position, new CallableRelatedDeclare[0]);
    }

    @Override
    public boolean strictSame(MemberType other) {
        return other == this;
    }


}
