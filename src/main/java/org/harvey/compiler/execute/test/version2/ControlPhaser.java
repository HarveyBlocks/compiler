package org.harvey.compiler.execute.test.version2;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.test.version2.command.GotoCommand;
import org.harvey.compiler.execute.test.version2.command.SequentialCommand;
import org.harvey.compiler.execute.test.version2.handler.ExecutableControlHandler;
import org.harvey.compiler.execute.test.version2.handler.impl.*;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.msg.ControlContextBuilder;
import org.harvey.compiler.execute.test.version2.msg.ProgramCounter;
import org.harvey.compiler.execute.test.version2.msg.SequentialControlElement;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version2.handler.impl.*;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:18
 */
public class ControlPhaser {
    // 构建先
    private final ControlContextBuilder controlContextBuilder;

    public static void main(String[] args) {
        ControlContextBuilder builder = new ControlContextBuilder();
        builder.conditionExpressionHandler(new ConditionExpressionHandler());
        builder.ifHandler(new DefaultIfHandler());
        builder.elseHandler(new DefaultElseHandler());
        builder.sentenceExpressionHandler(new SentenceEndExpressionHandler());
        builder.noneNextHandler(new DefaultNoneNextHandler());
        builder.bodyStartHandler(new DefaultBodyStartHandler());
        builder.bodyEndHandler(new DefaultBodyEndHandler());
        ControlPhaser controlPhaser = new ControlPhaser(builder);
        List<SequentialControlElement> phase = controlPhaser.phase(null, newSource());
        for (SequentialControlElement element : phase) {
            System.out.println(element);
        }
    }

    /**
     * "if"
     * "("
     * "exp1"
     * ")"
     * "if"
     * "("
     * "exp2"
     * ")"
     * "exp3"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp4"
     * ")"
     * "exp5"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp6"
     * ")"
     * "exp7"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp8"
     * ")"
     * "exp9"
     * ";"
     * "else"
     * "exp10"
     * ";"
     * "exp11"
     * ;
     */
    private static SourceTextContext newSource() {
        SourceTextContext source = new SourceTextContext();
        /*
         * if(c1)
         *   if(c2_1)
         *       exp2_1;
         *   else if (c2_2)
         *       exp2_2;
         *   else if(c2_3)
         *       exp2_3;
         *   else if(c2_4)
         *       exp2_4;
         *   else
         *       exp2_5;
         *  exp1;
         * */
        // c1_1
        // if_false_goto L0
        // c2_2
        // if_false_goto L1
        // exp2_1
        // goto_L2
        // L1:
        // exp2_2
        // L2:
        // L0:
        // exp1_2
        source.add(new SourceString(SourceType.STRING, "{", new SourcePosition(0, 0)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 1)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 2)));
        source.add(new SourceString(SourceType.STRING, "c0_1", new SourcePosition(0, 3)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 4)));
        source.add(new SourceString(SourceType.STRING, "{", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 5)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 6)));
        source.add(new SourceString(SourceType.STRING, "c1_1", new SourcePosition(0, 7)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 8)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 9)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 10)));
        source.add(new SourceString(SourceType.STRING, "c2_1", new SourcePosition(0, 11)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 12)));
        source.add(new SourceString(SourceType.STRING, "exp2_1", new SourcePosition(0, 13)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 14)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 15)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 16)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 17)));
        source.add(new SourceString(SourceType.STRING, "c_2_2", new SourcePosition(0, 18)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 19)));
        source.add(new SourceString(SourceType.STRING, "exp2_2", new SourcePosition(0, 20)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 21)));
        source.add(new SourceString(SourceType.STRING, "}", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 22)));
        source.add(new SourceString(SourceType.STRING, "exp2_3", new SourcePosition(0, 23)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 24)));
        source.add(new SourceString(SourceType.STRING, "exp1_2", new SourcePosition(0, 25)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 26)));
        source.add(new SourceString(SourceType.STRING, "exp1_3", new SourcePosition(0, 27)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "}", new SourcePosition(0, 0)));
        /*source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 1)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 2)));
        source.add(new SourceString(SourceType.STRING, "c1", new SourcePosition(0, 3)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 4)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 5)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 6)));
        source.add(new SourceString(SourceType.STRING, "c2_1", new SourcePosition(0, 7)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 8)));
        source.add(new SourceString(SourceType.STRING, "exp2_1", new SourcePosition(0, 9)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 10)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 11)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 12)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 13)));
        source.add(new SourceString(SourceType.STRING, "c2_2", new SourcePosition(0, 14)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 15)));
        source.add(new SourceString(SourceType.STRING, "exp2_2", new SourcePosition(0, 16)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 17)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 18)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 19)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 20)));
        source.add(new SourceString(SourceType.STRING, "c2_3", new SourcePosition(0, 21)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 22)));
        source.add(new SourceString(SourceType.STRING, "exp2_3", new SourcePosition(0, 23)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 24)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 25)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 26)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 27)));
        source.add(new SourceString(SourceType.STRING, "c2_4", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 29)));
        source.add(new SourceString(SourceType.STRING, "exp2_4", new SourcePosition(0, 30)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 31)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 32)));
        source.add(new SourceString(SourceType.STRING, "exp2_5", new SourcePosition(0, 33)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 34)));
        source.add(new SourceString(SourceType.STRING, "exp1", new SourcePosition(0, 35)));*/
        return source;
    }

    public ControlPhaser(ControlContextBuilder controlContextBuilder) {
        this.controlContextBuilder = controlContextBuilder;
    }

    public List<SequentialControlElement> phase(OuterEnvironment outerEnvironment, SourceTextContext source) {
        ControlContext controlContext = controlContextBuilder.outerEnvironment(outerEnvironment).source(source).build();
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


    private static void setFirstHandler(ControlContext controlContext) {
        if (controlContext.noNext()) {
            controlContext.nowHandler(controlContext.noneNextHandler());
        } else {
            SourceString next = controlContext.next();
            ExecutableControlHandler handler = getExecutableControlHandler(next, controlContext);
            controlContext.nowHandler(handler);
        }
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
                return;
            // throw new AnalysisExpressionException(next.getPosition(), "Unknown type");
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
                throw new AnalysisExpressionException(controlContext.now(), "except {");

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

                // throw new AnalysisExpressionException(next.getPosition(), "Unknown type");
        }
    }

    private static boolean declareOrExpression(ControlContext controlContext) {
        return false;
    }
}
