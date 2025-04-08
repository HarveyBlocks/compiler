package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version1.element.CallableSupplierElement;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 一个函数的调用处用一个PossibleCallableSupplier, 到时候逐渐缩小范围, 大家都可以感知到
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 17:47
 */
public abstract class PossibleCallableSupplier extends MemberSupplier implements CallableSupplierElement {
    protected CallableRelatedDeclare[] possible;

    public PossibleCallableSupplier(SourcePosition position, CallableRelatedDeclare[] possible) {
        super(position);
        this.possible = possible;
    }

    @Override
    public CallableRelatedDeclare[] getPossibleCallableRelatedDeclare() {
        if (possible.length == 0) {
            throw new AnalysisExpressionException(getPosition(), "can not matched callable capture");
        }
        return possible;
    }

    @Override
    public void resetPossibleCallableRelatedDeclare(CallableRelatedDeclare[] callableRelatedDeclares) {
        possible = callableRelatedDeclares;
    }

    @Override
    public MemberType getType() {
        throw new AnalysisExpressionException(getPosition(), "do not use this method for multiple possible callable.");
    }


    public CallableRelatedDeclare one() {
        if (possible.length == 0) {
            throw new AnalysisExpressionException(getPosition(), "can not matched callable capture");
        } else if (possible.length > 1) {
            throw new AnalysisExpressionException(getPosition(), "too much matched callable capture");
        } else {
            return possible[0];
        }
    }



    public void eliminateImpossible(Predicate<CallableRelatedDeclare> predicate) {
        List<CallableRelatedDeclare> newPossible = new ArrayList<>();
        for (CallableRelatedDeclare callableRelatedDeclare : possible) {
            if (predicate.test(callableRelatedDeclare)) {
                // 符合, 就还在
                newPossible.add(callableRelatedDeclare);
            }
        }
        if (newPossible.isEmpty()) {
            throw new AnalysisExpressionException(getPosition(), "no callable match argument list size");
        }
        resetPossibleCallableRelatedDeclare(newPossible.toArray(new CallableRelatedDeclare[0]));
    }
    public void eliminateImpossibleByLength(int argumentLength) {
        eliminateImpossible(d -> d.testParameterSize(argumentLength));
    }

    public void eliminateImpossibleByGenericList(MemberType[] genericList) {
        eliminateImpossible(d -> d.testGenericList(genericList));
    }
    public void eliminateImpossibleResultType(MemberType determinedType) {
        eliminateImpossible(d->d.testOnlyResult(determinedType));
    }
}
