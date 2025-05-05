package org.harvey.compiler.execute.test.version1.element;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 函数再调用完毕后, 可以将其看作结果, 例如 supplier.get().run();
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 17:11
 */
@Getter
public class CallableInvokeResultSupplier extends MemberSupplier {

    private final ComplexExpressionWrap[] argumentWraps;
    private final PossibleCallableSupplier possibleCallable;
    private final MemberType[] genericList;

    public CallableInvokeResultSupplier(
            SourcePosition sp,
            MemberType[] genericList,
            ComplexExpressionWrap[] argumentWraps,
            PossibleCallableSupplier possibleCallable) {
        super(sp);
        this.genericList = genericList;
        this.argumentWraps = argumentWraps;
        this.possibleCallable = possibleCallable;
    }


    public boolean finished() {
        for (ComplexExpressionWrap argument : argumentWraps) {
            if (!argument.finished()) {
                return false;
            }
        }
        return true;
    }


    @Override
    public MemberType getType() {
        if (!finished()) {
            throw new AnalysisExpressionException(
                    getPosition(),
                    "can not match possible callable before phase all arguments"
            );
        }
        CallableRelatedDeclare one = possibleCallable.one();
        if (one.returnTypeSize() != 1) {
            throw new AnalysisExpressionException(getPosition(), "");
        }
        return one.getReturnType(0);
    }

    @Override
    public String show() {
        return "" + this.argumentWraps.length;
    }
}
