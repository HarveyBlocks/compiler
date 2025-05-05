package org.harvey.compiler.execute.test.version4.handler.impl;

import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version4.handler.CaseHandler;
import org.harvey.compiler.execute.test.version4.handler.ConstantExpressionHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.msg.Label;
import org.harvey.compiler.io.source.SourceString;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 15:15
 */
public class CaseHandlerImpl implements CaseHandler {
    public static final String CASE_CONDITION = SourceFileConstant.CASE_CONDITION;

    @Override
    public void handle(ControlContext context) {
        // 过一个, 然后到: 停
        if (!context.bodyStack.isSwitchBody()) {
            throw new AnalysisControlException(context.now(), "except in switch body");
        }
        List<SourceString> caseCondition = caseCondition(context);
        if (caseCondition.isEmpty()) {
            throw new AnalysisControlException(context.now(), "except constant expression for case condition");
        }
        ConstantExpressionHandler.ConstantResult calculate = context.constantExpressionHandler()
                .calculate(caseCondition);
        Label caseLabel = context.createLabel();
        context.registerLabelOnNextSequential(caseLabel);
        context.bodyStack.switchBody().registerCase(calculate, caseLabel);
    }

    private List<SourceString> caseCondition(ControlContext context) {
        List<SourceString> result = new ArrayList<>();
        int inConditionExpression = 0;
        while (context.hasNext()) {
            SourceString next = context.next();
            result.add(next);
            if (CASE_CONDITION.equals(next.getValue())) {
                if (inConditionExpression == 0) {
                    result.remove(result.size() - 1);
                    return result;
                } else if (inConditionExpression > 0) {
                    inConditionExpression--;
                } else {
                    throw new AnalysisControlException(context.now(), "too many " + CASE_CONDITION);
                }
            } else if (Operator.CONDITION_CHECK.nameEquals(next.getValue())) {
                if (inConditionExpression < 0) {
                    throw new AnalysisControlException(context.now(), "too many " + CASE_CONDITION);

                }
                inConditionExpression++;
            }
        }
        throw new AnalysisControlException(context.now(), "can not find " + CASE_CONDITION + " for case");
    }
}
