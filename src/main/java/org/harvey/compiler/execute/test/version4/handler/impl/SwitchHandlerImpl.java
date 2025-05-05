package org.harvey.compiler.execute.test.version4.handler.impl;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version4.command.DefaultSequentialCommand;
import org.harvey.compiler.execute.test.version4.command.IfConditionGoto;
import org.harvey.compiler.execute.test.version4.command.SimpleGotoCommand;
import org.harvey.compiler.execute.test.version4.handler.CaseHandler;
import org.harvey.compiler.execute.test.version4.handler.ConstantExpressionHandler;
import org.harvey.compiler.execute.test.version4.handler.ExecutableControlHandler;
import org.harvey.compiler.execute.test.version4.handler.SwitchHandler;
import org.harvey.compiler.execute.test.version4.msg.ControlContext;
import org.harvey.compiler.execute.test.version4.msg.Label;
import org.harvey.compiler.execute.test.version4.stack.SwitchBody;
import org.harvey.compiler.io.source.SourceString;

import java.util.List;

import static org.harvey.compiler.text.depart.SimpleDepartedBodyFactory.BODY_START;

/**
 * TODO
 * condition
 * goto switch_table
 * <p>
 * <p>
 * <p>
 * switch_table:
 * stack_peek_is "A"
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 15:16
 */
public class SwitchHandlerImpl implements SwitchHandler {


    @Override
    public void switchTable(ControlContext context, SwitchBody switchBody) {
        // 为了防止顺序执行的语句进入switch table, 在这里goto break
        Label switchBreak = switchBody.getSwitchBreak();
        context.addSequential(new SimpleGotoCommand(switchBreak));
        // 开始 switch table
        context.registerLabelOnNextSequential(switchBody.getSwitchTable());
        // switch case
        switchCases(context, switchBody.getCaseConditionLabelPair());
        // switch default
        switchDefault(context, switchBody.getDefaultLabel());
        // 结束
        context.registerLabelOnNextSequential(switchBreak);
    }

    private void switchCases(
            ControlContext context,
            List<Pair<ConstantExpressionHandler.ConstantResult, Label>> caseConditionLabelPair) {
        for (Pair<ConstantExpressionHandler.ConstantResult, Label> pair : caseConditionLabelPair) {
            ConstantExpressionHandler.ConstantResult condition = pair.getKey();
            context.addSequential(new DefaultSequentialCommand("push_constant " + condition));
            context.addSequential(new DefaultSequentialCommand("peek_from_switch_condition_move_to_emp_stack"));
            context.addSequential(new DefaultSequentialCommand("pop_two_eq"));
            Label caseLabel = pair.getValue();
            context.addSequential(IfConditionGoto.onTrue(caseLabel));
        }
    }

    private void switchDefault(ControlContext context, Label defaultLabel) {
        if (defaultLabel != null) {
            context.addSequential(new SimpleGotoCommand(defaultLabel));
        }
    }

    @Override
    public void handle(ControlContext context) {
        // deal as body start
        SourceString expectBodyStart = context.next();
        context.conditionExpressionHandler().handle(context);
        // 结果移到switch的stack, 放到switch table 处理
        context.addSequential(new DefaultSequentialCommand("stack_pop_move_to_switch_register_stack"));
        if (!BODY_START.equals(expectBodyStart.getValue())) {
            throw new AnalysisControlException(context.now(), "except body start");
        }
        //
        Label switchTable = context.createLabel();
        Label switchBreak = context.createLabel();
        context.bodyStack.switchBodyIn(switchTable, switchBreak);
        context.pushSwitchBreak(switchBreak);
        context.addSequential(new SimpleGotoCommand(switchTable));
    }

    @Override
    public void handleNext(ControlContext context, ExecutableControlHandler handler) {
        if (!(handler instanceof CaseHandler)) {
            // 第一个要是case
            throw new AnalysisControlException(context.now(), "except case");
        }
        SwitchHandler.super.handleNext(context, handler);
    }
}
