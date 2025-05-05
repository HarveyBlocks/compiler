package org.harvey.compiler.execute.test.version3.handler.impl;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version3.command.DefaultSequentialCommand;
import org.harvey.compiler.execute.test.version3.command.SimpleGotoCommand;
import org.harvey.compiler.execute.test.version3.handler.NormalExpressionHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;
import org.harvey.compiler.execute.test.version3.msg.Label;
import org.harvey.compiler.execute.test.version3.stack.AfterControlSentence;
import org.harvey.compiler.execute.test.version3.stack.key.ControlKey;
import org.harvey.compiler.io.source.SourceString;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 22:22
 */
@SuppressWarnings("DuplicatedCode")
public class SentenceEndExpressionHandler implements NormalExpressionHandler {
    public static final Predicate<SourceString> SENTENCE_END_PREDICATE = ss -> ";".equals(ss.getValue());

    public static void markOneSentence(AfterControlSentence sentence) {
        if (sentence.emptyOpenWith()) {
            return;
        }
        LinkedList<ControlKey> temp = new LinkedList<>();
        while (!sentence.emptyOpenWith()) {
            ControlKey top = sentence.popPreOpenWith();
            temp.addLast(top);
            if (top.isSentenceEndAfterControl()) {
                break;
            }
            top.setSentenceEndAfterControl(true);

        }
        while (!temp.isEmpty()) {
            ControlKey bottom = temp.removeLast();
            sentence.openWith(bottom);
        }
    }

    public static void skipExpressionAfter(ControlContext context, AfterControlSentence sentence) {
        ControlKey controlKey = sentence.popPreOpenWith();
        Label returnBack = controlKey.getPassiveReturnBack();
        if (returnBack != null) {
            context.addSequential(new SimpleGotoCommand(returnBack));
        }
        controlKey.setPassiveReturnBack(null);
        Label skipExpressionAfter = controlKey.getSkipExpressionAfter();
        registerLaggingLabel(skipExpressionAfter, context);
        controlKey.setSkipExpressionAfter(null);
        sentence.openWith(controlKey);
    }

    public static void dealLoggingLabels(ControlContext context, AfterControlSentence sentence) {
        while (!sentence.emptyOpenWith()) {
            ControlKey controlKey = sentence.popPreOpenWith();
            if (controlKey.confirm(Keyword.DO)) {
                throw new AnalysisControlException(context.now(), "except while for do");
            }
            dealLoggingLabels(context, controlKey);
        }

    }

    private static void dealLoggingLabels(ControlContext context, ControlKey controlKey) {
        Label returnBack = controlKey.getPassiveReturnBack();
        if (returnBack != null) {
            context.addSequential(new SimpleGotoCommand(returnBack));
        }
        registerLaggingLabel(controlKey.getSkipExpressionAfter(), context);
        registerLaggingLabel(controlKey.getIfOnFalse(), context);
    }

    private static void registerLaggingLabel(Label laggingLabel, ControlContext context) {
        if (laggingLabel != null) {
            context.registerLabelOnNextSequential(laggingLabel);
        }
    }

    public static void beforeElse(ControlContext context) {
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.opening() || sentence.emptyOpenWith()) {
            throw new AnalysisControlException(context.now(), "except pre if");
        }
        // 同级, 要转换
        while (sentence.confirmTop(Keyword.ELSE)) {
            // else 把expression救活了
            ControlKey controlKey = sentence.popPreOpenWith();
            if (sentence.emptyOpenWith()) {
                throw new AnalysisControlException(context.now(), "except pre if");
            }
            dealLoggingLabels(context, controlKey);
        }
        if (!sentence.confirmTop(Keyword.IF)) {
            throw new AnalysisControlException(context.now(), "except pre if");
        }
        sentence.openSentence();

    }

    public static void beforeWhileForDo(ControlContext context) {
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (!sentence.opening() || sentence.emptyOpenWith()) {
            return;
        }
        LinkedList<ControlKey> temp = new LinkedList<>();
        // 同级, 要转换
        while (!sentence.emptyOpenWith()) {

            ControlKey controlKey = sentence.popPreOpenWith();
            if (!controlKey.confirm(Keyword.DO)) {
                // 找do
                temp.addLast(controlKey);
                continue;
            }
            // 找到do
            // do 里面的都是要被处理掉的语句
            while (!temp.isEmpty()) {
                dealLoggingLabels(context, temp.removeFirst());
            }
            // exp 结束了, while 来检查
            // while 把 do 救活了
            sentence.openWith(controlKey);
            sentence.openSentence();
            return;
        }
        // 没找到
        while (!temp.isEmpty()) {
            // 恢复
            sentence.openWith(temp.removeLast());
        }

    }

    @Override
    public void handle(ControlContext context) {
        AfterControlSentence sentence = context.bodyStack.sentenceAfterControl();
        if (sentence.stillSentence()) {
            handleSentenceExpression(context);
            sentence.expressionEnd();
            markOneSentence(sentence);
            skipExpressionAfter(context, sentence);
        } else {
            if (sentence.opening()) {
                dealLoggingLabels(context, sentence);
                sentence.closeSentence();
            }
            handleSentenceExpression(context);
        }
    }

    private void handleSentenceExpression(ControlContext context) {
        normalHandle(context, context, SENTENCE_END_PREDICATE);
    }

    @Override
    public void nextIsBodyEnd(ControlContext context) {
        // 允许执行}
        handleNext(context, context.bodyEndHandler());
    }

    @Override
    public void normalHandle(
            ControlContext context, ListIterator<SourceString> source, Predicate<SourceString> stopCondition) {
        while (source.hasNext()) {
            SourceString next = source.next();
            if (stopCondition.test(next)) {
                break;
            }
            context.addSequential(new DefaultSequentialCommand(next.getValue()));
        }
    }

    @Override
    public void nextIsElse(ControlContext context) {
        beforeElse(context);
        NormalExpressionHandler.super.nextIsElse(context);
    }

    @Override
    public void nextIsWhile(ControlContext context) {
        beforeWhileForDo(context);
        NormalExpressionHandler.super.nextIsWhile(context);
    }
}
