package org.harvey.compiler.execute.test.version2.stack;

import org.harvey.compiler.execute.test.version2.msg.Label;

import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 19:35
 */
public class IfSentence {
    private final OneSentenceFrame frame;

    IfSentence(OneSentenceFrame frame) {
        this.frame = frame;
    }

    public void initSentence() {
        frame.startSentenceIf = true;
        frame.stillSentence = true;
    }

    public boolean starting() {
        return frame.startSentenceIf;
    }

    public void closeSentence() {
        frame.reset();
    }

    public void startIfSentence() {
        if (!frame.startSentenceIf) {
            return;
        }
        frame.stillSentence = true;
    }

    public void endIfSentence() {
        if (!frame.startSentenceIf) {
            return;
        }
        frame.stillSentence = false;
    }

    public void elseSentence() {
        if (!frame.startSentenceIf) {
            return;
        }
        frame.stillSentence = true;
    }

    public boolean stillSentence() {
        return frame.startSentenceIf && frame.stillSentence;
    }

    public Label popLastIfFalse() {
        return frame.lastIfFalse.pop();
    }

    public Collection<Label> allIfFalse() {
        ArrayList<Label> labels = new ArrayList<>(frame.lastIfFalse);
        frame.lastIfFalse.clear();
        return labels;
    }

    public void pushLastIfFalse(Label label) {
        this.frame.lastIfFalse.push(label);
    }

    public Label popSkipOtherElse() {
        Label skipOtherElse = frame.skipOtherElse;
        frame.skipOtherElse = null;
        return skipOtherElse;
    }

    public void setSkipOtherElse(Label skipOtherElse) {
        frame.skipOtherElse = skipOtherElse;
    }
}
