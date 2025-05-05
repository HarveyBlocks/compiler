package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:52
 */
public interface MemberType {

    MemberSupplier find(SourcePosition position, String innerMemberName);

    PossibleCallableSupplier find(SourcePosition position, Operator operator);

    PossibleCallableSupplier find(SourcePosition position, Operator[] sameNameOperator);

    PossibleCallableSupplier findPossibleCallable(SourcePosition position, String callableName);

    PossibleCallableSupplier findCastOperator(SourcePosition position, MemberType castTarget);

    PossibleCallableSupplier findConstructor(SourcePosition position);

    /**
     * 针对非static
     */
    PossibleCallableSupplier findInnerTypeConstructor(SourcePosition position, MemberType memberType);

    /**
     * param的类型是否匹配
     */
    boolean strictSame(MemberType other);


}
