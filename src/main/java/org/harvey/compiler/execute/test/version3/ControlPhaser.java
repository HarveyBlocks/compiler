package org.harvey.compiler.execute.test.version3;

import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version3.command.GotoCommand;
import org.harvey.compiler.execute.test.version3.command.SequentialCommand;
import org.harvey.compiler.execute.test.version3.handler.ExecutableControlHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;
import org.harvey.compiler.execute.test.version3.msg.ControlHandlerRegister;
import org.harvey.compiler.execute.test.version3.msg.ProgramCounter;
import org.harvey.compiler.execute.test.version3.msg.SequentialControlElement;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:18
 */
@SuppressWarnings("DuplicatedCode")
public class ControlPhaser {
    // 构建先
    private final ControlHandlerRegister controlHandlerRegister;


    public ControlPhaser(ControlHandlerRegister controlHandlerRegister) {
        this.controlHandlerRegister = controlHandlerRegister;
    }

    private static void setFirstHandler(ControlContext controlContext) {
        if (controlContext.noNext()) {
            controlContext.nowHandler(controlContext.noneNextHandler());
        } else {
            SourceString next = controlContext.next();
            ExecutableControlHandler handler = getExecutableControlHandler(next, controlContext);
            controlContext.nowHandler(handler);
        }
    }

    private static ExecutableControlHandler getExecutableControlHandler(
            SourceString next, ControlContext controlContext) {
        switch (next.getValue()) {
            case "if":
                return controlContext.ifHandler();
            case "else":
                return controlContext.elseHandler();
            case "do":
                return controlContext.doHandler();
            case "while":
                return controlContext.whileHandler();
            case "{":
                return controlContext.bodyStartHandler();
            case "}":
                throw new AnalysisControlException(controlContext.now(), "except {");
            case "break":
                return controlContext.breakHandler();
            case "continue":
                return controlContext.continueHandler();
            case "return":
                return controlContext.returnHandler();
            default:
                controlContext.previousMove();
                if (declareOrExpression(controlContext)) {
                    return controlContext.declareHandler();
                } else {
                    return controlContext.sentenceExpressionHandler();
                }

                // throw new AnalysisControlException(next.getPosition(), "Unknown type");
        }
    }

    private static boolean declareOrExpression(ControlContext controlContext) {
        return false;
    }

    public List<SequentialControlElement> phase(OuterEnvironment outerEnvironment, SourceTextContext source) {
        ControlContext controlContext = new ControlContext(outerEnvironment, source, controlHandlerRegister);
        setFirstHandler(controlContext);
        controlContext.nowHandler().handle(controlContext);
        while (true) {
            if (controlContext.noNext()) {
                controlContext.nowHandler().notHaveNext(controlContext);
                break;
            }
            SourceString next = controlContext.next();
            dealNext(next, controlContext);
        }
        List<SequentialControlElement> sequential = controlContext.getSequential();
        jumpToLabel(sequential);
        return sequential;
    }

    private void jumpToLabel(List<SequentialControlElement> sequential) {
        ProgramCounter programCounter = new ProgramCounter(sequential);
        while (programCounter.hasNext()) {
            SequentialCommand command = programCounter.nextCommand();
            if (command instanceof GotoCommand) {
                GotoCommand gotoCommand = (GotoCommand) command;
                int step = programCounter.jumpToStep(gotoCommand.getLabel());
                if (step == 0) {
                    // 直接
                    // 移除当前命令
                    // 当前命令上的Label全部指向下一条
                }
                gotoCommand.transitionToJump(programCounter);
            }
        }
        // 反过来, 复制少一点
        // 从后往前遍历, 取得goto的label, label的line的地址是X
    }

    private void dealNext(SourceString next, ControlContext controlContext) {
        ExecutableControlHandler handler = controlContext.nowHandler();
        switch (next.getValue()) {
            case "if":
                handler.nextIsIf(controlContext);
                return;
            case "else":
                handler.nextIsElse(controlContext);
                return;
            case "do":
                handler.nextIsDo(controlContext);
                return;
            case "while":
                handler.nextIsWhile(controlContext);
                return;
            case "{":
                handler.nextIsBlockStart(controlContext);
                return;
            case "break":
                handler.nextIsBreakSentence(controlContext);
                return;
            case "continue":
                handler.nextIsContinueSentence(controlContext);
                return;
            case "}":
                handler.nextIsBodyEnd(controlContext);
                return;
            case "return":
                controlContext.returnHandler().handle(controlContext);
                return;
            default:
                controlContext.previousMove();
                if (declareOrExpression(controlContext)) {
                    handler.nextIsDeclareType(controlContext);
                } else {
                    handler.nextIsExpression(controlContext);
                }
                // throw new AnalysisControlException(next.getPosition(), "Unknown type");
        }
    }
}
