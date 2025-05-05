package org.harvey.compiler.execute.test.version5.msg;

import lombok.Getter;
import org.harvey.compiler.common.collecction.DefaultRandomlyIterator;
import org.harvey.compiler.common.collecction.RandomlyAccessAble;
import org.harvey.compiler.exception.analysis.AnalysisControlException;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version5.command.SequentialCommand;
import org.harvey.compiler.execute.test.version5.handler.*;
import org.harvey.compiler.execute.test.version5.msg.stack.BodyStack;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:00
 */
@SuppressWarnings("DuplicatedCode")
public class ControlContext extends DefaultRandomlyIterator<SourceString> {


    public final BodyStack bodyStack = new BodyStack();
    private final Stack<Label> breakStack = new Stack<>();
    private final Stack<Label> continueStack = new Stack<>();
    private final List<SequentialControlElement> sequentialControl = new ArrayList<>();
    @Getter
    private final OuterEnvironment outerEnvironment;
    private final LabelFactory labelFactory = new LabelFactory();
    private final ControlHandlerRegister handlerRegister;
    private SourceString now;
    private ExecutableControlHandler nowHandler;

    public ControlContext(
            OuterEnvironment outerEnvironment, SourceTextContext source, ControlHandlerRegister handlerRegister) {
        super(RandomlyAccessAble.forList(source), 0);
        this.outerEnvironment = outerEnvironment;
        this.handlerRegister = handlerRegister;
    }

    public void bodyStackNotEmpty() {
        if (bodyStack.empty()) {
            throw new AnalysisControlException(now(), "expect in a block");
        }
    }

    public SourcePosition now() {
        return now.getPosition();
    }

    public SourceString next() {
        return now = super.next();
    }

    public Label createLabel() {
        return labelFactory.create();
    }

    public void registerLabel(int index, Label label) {
        if (label == null) {
            return;
        }
        this.sequentialControl.get(index).registerLabel(label);
    }

    public void registerLabel(int index, Collection<Label> label) {
        this.sequentialControl.get(index).registerLabel(label);
    }

    public int addSequential(SequentialCommand command) {
        SequentialControlElement lastEmpty = lastEmptyInSequence();
        if (lastEmpty != null) {
            lastEmpty.instance(command, sourceLine());
            return this.sequentialControl.size() - 1;
        }
        int line = this.sequentialControl.size();
        this.sequentialControl.add(new SequentialControlElement(line, command, sourceLine()));
        return line;
    }

    private int sourceLine() {
        return now.getPosition().getRaw();
    }


    public int addEmptySequential() {
        SequentialControlElement lastEmpty = lastEmptyInSequence();
        if (lastEmpty != null) {
            return this.sequentialControl.size() - 1;
        }
        int line = this.sequentialControl.size();
        this.sequentialControl.add(SequentialControlElement.empty(line));
        return line;
    }

    private SequentialControlElement lastEmptyInSequence() {
        if (this.sequentialControl.isEmpty()) {
            return null;
        }
        int line = this.sequentialControl.size() - 1;
        SequentialControlElement last = this.sequentialControl.get(line);
        return last.nullCommand() ? last : null;
    }

    public void registerLabelOnNextSequential(Label label) {
        SequentialControlElement element = lastEmptySequentialCreateIfAbsent();
        element.registerLabel(label);
    }

    public void registerLabelOnNextSequential(Collection<Label> label) {
        SequentialControlElement element = lastEmptySequentialCreateIfAbsent();
        element.registerLabel(label);
    }

    private SequentialControlElement lastEmptySequentialCreateIfAbsent() {
        SequentialControlElement last = lastEmptyInSequence();
        if (last != null) {
            return last;
        }
        int line = this.sequentialControl.size();
        SequentialControlElement element = SequentialControlElement.empty(line);
        this.sequentialControl.add(element);
        return element;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    public void pushSwitchBreak(Label breakTargetLabel) {
        this.breakStack.push(breakTargetLabel);
    }

    public void pushBreakAndContinue(
            Label continueLabel,
            Label breakLabel) {
        this.continueStack.push(continueLabel);
        this.breakStack.push(breakLabel);
    }

    public Label peekContinue() {
        return this.continueStack.peek();
    }

    public Label peekBreak() {
        return this.breakStack.peek();
    }

    public void popContinueAndBreak() {
        this.continueStack.pop();
        this.breakStack.pop();
    }

    public void popSwitchBreak() {
        this.breakStack.pop();
    }

    public LocalVariableTableBuilder.LocalVariableMessage declare(LocalVariableType type, String name) {
        // 1. 注入local variable
        return bodyStack.localVariableTableBuilder.declare(type, name, now());
    }

    public LocalVariableTableBuilder.LocalVariableMessage usingVariable(String name) {
        return bodyStack.localVariableTableBuilder.using(name, now());
    }

    // -------------------------------------------------------
    public IfHandler ifHandler() {
        return handlerRegister.ifHandler;
    }


    public ElseHandler elseHandler() {
        return handlerRegister.elseHandler;
    }


    public DoHandler doHandler() {
        return handlerRegister.doHandler;
    }


    public WhileHandler whileHandler() {
        return handlerRegister.whileHandler;
    }


    public BodyStartHandler bodyStartHandler() {
        return handlerRegister.bodyStartHandler;
    }

    public BodyEndHandler bodyEndHandler() {
        return handlerRegister.bodyEndHandler;
    }


    public ControlExpressionHandler conditionExpressionHandler() {
        return handlerRegister.conditionExpressionHandler;
    }


    public ControlExpressionHandler sentenceExpressionHandler() {
        return handlerRegister.sentenceExpressionHandler;
    }


    public NoneNextHandler noneNextHandler() {
        return handlerRegister.noneNextHandler;
    }


    public DeclareHandler declareHandler() {
        return handlerRegister.declareHandler;
    }


    public BreakHandler breakHandler() {
        return handlerRegister.breakHandler;
    }


    public ContinueHandler continueHandler() {
        return handlerRegister.continueHandler;
    }


    public ReturnHandler returnHandler() {
        return handlerRegister.returnHandler;
    }

    public ConstantExpressionHandler constantExpressionHandler() {
        return handlerRegister.constantExpressionHandler;
    }

    public SwitchHandler switchHandler() {
        return handlerRegister.switchHandler;
    }

    public CaseHandler caseHandler() {
        return handlerRegister.caseHandler;
    }

    public DefaultHandler defaultHandler() {
        return handlerRegister.defaultHandler;
    }

    public boolean noNext() {
        return !hasNext();
    }

    public void previousMove() {
        previous();
    }

    public void nowHandler(ExecutableControlHandler handler) {
        this.nowHandler = handler;
    }

    public ExecutableControlHandler nowHandler() {
        return nowHandler;
    }

    public List<SequentialControlElement> getSequential() {
        return sequentialControl;
    }
    // -------------------------------------------------------


}


