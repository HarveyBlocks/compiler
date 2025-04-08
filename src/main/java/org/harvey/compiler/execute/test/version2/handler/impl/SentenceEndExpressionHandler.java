package org.harvey.compiler.execute.test.version2.handler.impl;

import org.harvey.compiler.execute.test.version2.command.DefaultSequentialCommand;
import org.harvey.compiler.execute.test.version2.command.SimpleGotoCommand;
import org.harvey.compiler.execute.test.version2.handler.ControlExpressionHandler;
import org.harvey.compiler.execute.test.version2.handler.ExecutableControlHandler;
import org.harvey.compiler.execute.test.version2.msg.ControlContext;
import org.harvey.compiler.execute.test.version2.msg.Label;
import org.harvey.compiler.execute.test.version2.stack.IfSentence;
import org.harvey.compiler.io.source.SourceString;

import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 22:22
 */
public class SentenceEndExpressionHandler implements ControlExpressionHandler {
    @Override
    public void handle(ControlContext context) {
        IfSentence ifSentence = context.bodyStack.sentenceForIf();
        if (ifSentence.stillSentence()) {
            handle0(context);
            postExpression(context, ifSentence);
        } else {
            if (ifSentence.starting()) {
                sentenceFinished(context, ifSentence);
            }
            handle0(context);
        }
    }

    public static void postExpression(ControlContext context, IfSentence ifSentence) {
        Label old = ifSentence.popSkipOtherElse();
        if (old != null) {
            context.registerLabelOnNextSequential(old);
        }
        Label label = context.createLabel();
        context.addSequential(new SimpleGotoCommand(label));
        ifSentence.setSkipOtherElse(label);
        ifSentence.endIfSentence();
    }

    public static void sentenceFinished(ControlContext context, IfSentence ifSentence) {
        Collection<Label> labels = ifSentence.allIfFalse();
        context.registerLabelOnNextSequential(labels);
        Label skipOtherElse = ifSentence.popSkipOtherElse();
        if (skipOtherElse != null) {
            context.registerLabelOnNextSequential(skipOtherElse);
        }
        ifSentence.closeSentence();
    }


    private static void handle0(ControlContext context) {
        while (context.hasNext()) {
            SourceString next = context.next();
            if (";".equals(next.getValue())) {
                break;
            }
            context.addSequential(new DefaultSequentialCommand(next.getValue()));
        }
    }

    @Override
    public void nextIsBodyEnd(ControlContext context) {
        IfSentence ifSentence = context.bodyStack.sentenceForIf();
        if (ifSentence.starting()) {
            sentenceFinished(context, ifSentence);
        }
        ExecutableControlHandler.handleNext(context, context.bodyEndHandler());
    }
}
