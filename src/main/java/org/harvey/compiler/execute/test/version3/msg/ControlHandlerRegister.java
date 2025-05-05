package org.harvey.compiler.execute.test.version3.msg;

import lombok.AllArgsConstructor;
import org.harvey.compiler.execute.test.version3.handler.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:35
 */
@SuppressWarnings("DuplicatedCode")
@AllArgsConstructor
public class ControlHandlerRegister {
    IfHandler ifHandler;
    ElseHandler elseHandler;
    DoHandler doHandler;
    WhileHandler whileHandler;
    BodyStartHandler bodyStartHandler;
    NormalExpressionHandler conditionExpressionHandler;
    NormalExpressionHandler sentenceExpressionHandler;
    NoneNextHandler noneNextHandler;
    DeclareHandler declareHandler;
    BreakHandler breakHandler;
    ContinueHandler continueHandler;
    ReturnHandler returnHandler;
    BodyEndHandler bodyEndHandler;

    public ControlHandlerRegister() {

    }

    public ControlHandlerRegister ifHandler(
            IfHandler ifHandler) {
        this.ifHandler = ifHandler;
        return this;
    }

    public ControlHandlerRegister elseHandler(
            ElseHandler elseHandler) {
        this.elseHandler = elseHandler;
        return this;
    }

    public ControlHandlerRegister doHandler(
            DoHandler doHandler) {
        this.doHandler = doHandler;
        return this;
    }

    public ControlHandlerRegister whileHandler(
            WhileHandler whileHandler) {
        this.whileHandler = whileHandler;
        return this;
    }

    public ControlHandlerRegister bodyStartHandler(
            BodyStartHandler bodyStartHandler) {
        this.bodyStartHandler = bodyStartHandler;
        return this;
    }

    public ControlHandlerRegister bodyEndHandler(BodyEndHandler bodyEndHandler) {
        this.bodyEndHandler = bodyEndHandler;
        return this;
    }

    public ControlHandlerRegister conditionExpressionHandler(
            NormalExpressionHandler conditionExpressionHandler) {
        this.conditionExpressionHandler = conditionExpressionHandler;
        return this;
    }

    public ControlHandlerRegister sentenceExpressionHandler(
            NormalExpressionHandler sentenceExpressionHandler) {
        this.sentenceExpressionHandler = sentenceExpressionHandler;
        return this;
    }

    public ControlHandlerRegister noneNextHandler(
            NoneNextHandler noneNextHandler) {
        this.noneNextHandler = noneNextHandler;
        return this;
    }

    public ControlHandlerRegister declareHandler(
            DeclareHandler declareHandler) {
        this.declareHandler = declareHandler;
        return this;
    }

    public ControlHandlerRegister breakHandler(
            BreakHandler breakHandler) {
        this.breakHandler = breakHandler;
        return this;
    }

    public ControlHandlerRegister continueHandler(ContinueHandler continueHandler) {
        this.continueHandler = continueHandler;
        return this;
    }

    public ControlHandlerRegister returnHandler(ReturnHandler returnHandler) {
        this.returnHandler = returnHandler;
        return this;
    }
}
