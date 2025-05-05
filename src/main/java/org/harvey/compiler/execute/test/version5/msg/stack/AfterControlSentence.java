package org.harvey.compiler.execute.test.version5.msg.stack;

import org.harvey.compiler.declare.analysis.Keyword;

/**
 * 操纵{@link SentenceAfterControlFrame}信息
 * 两个任务
 * 1. 判断一句是否结束
 * 2. 存储一句结束后需要进行的操作的必要信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 20:37
 */
public class AfterControlSentence {
    // if("if"){
    //  sentence.open if
    // }
    // ---------------------
    //  if(";"){ // 解析到表达式了
    //      解析完表达式了
    //    if next is "else":{
    //          if sentence.open with if
    //          else throw need if
    //    }else if next is "catch"{
    //          if sentence.open with try
    //          else if sentence.open with catch
    //          else throw need 'try'
    //    }else if "finally"{
    //          if sentence.open with try
    //          else if sentence.open with catch
    //          else throw need 'try'
    //    }else if sentence.open with try{
    //          throw try need catch or finally later
    //    }else if "while"{
    //          if sentence.open with do
    //          else sentence.end
    //    }else if sentence.open with do{
    //          throw need "while" later
    //    } else {
    //      sentence.end
    //    }
    //  }
    // try [catch]* [finally]?
    // (if (else)?)+
    // do-while
    // l1:
    //  exp
    // condition
    // if_true_goto l1
    //
    // return;
    // while
    // for
    //
    private final SentenceAfterControlFrame frame;

    AfterControlSentence(SentenceAfterControlFrame frame) {
        this.frame = frame;
    }

    public void openSentence() {
        frame.startSentence = true;
        frame.stillSentence = true;
    }

    public boolean opening() {
        return frame.startSentence;
    }

    public void closeSentence() {
        frame.reset();
    }

    public void startControlKeyword() {
        if (!frame.startSentence) {
            return;
        }
        frame.stillSentence = true;
    }


    public boolean stillSentence() {
        return frame.startSentence && frame.stillSentence;
    }

    public void expressionEnd() {
        if (!frame.startSentence) {
            return;
        }
        frame.stillSentence = false;
    }

    public void openWith(ControlKey controlKey) {
        frame.controlKeyStack.push(controlKey);
    }

    public ControlKey popPreOpenWith() {
        return frame.controlKeyStack.pop();
    }


    public boolean emptyOpenWith() {
        return frame.controlKeyStack.empty();
    }

    public boolean confirmTop(Keyword token) {
        return !emptyOpenWith() && frame.controlKeyStack.peek().confirm(token);
    }
}
