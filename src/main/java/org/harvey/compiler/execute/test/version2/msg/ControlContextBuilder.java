package org.harvey.compiler.execute.test.version2.msg;

import org.harvey.compiler.execute.test.version2.handler.*;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:47
 */
public class ControlContextBuilder {
    OuterEnvironment outerEnvironment;
    SourceTextContext source;
    IfHandler ifHandler;
    ElseHandler elseHandler;
    DoHandler doHandler;
    WhileHandler whileHandler;
    BodyStartHandler bodyStartHandler;
    private BodyEndHandler bodyEndHandler;
    ControlExpressionHandler conditionExpressionHandler;
    ControlExpressionHandler sentenceExpressionHandler;
    NoneNextHandler noneNextHandler;
    DeclareHandler declareHandler;
    BreakHandler breakHandler;
    ContinueHandler continueHandler;
    ReturnHandler returnHandler;
    private ControlHandlerRegister register;


    public ControlContextBuilder outerEnvironment(
            OuterEnvironment outerEnvironment) {
        this.outerEnvironment = outerEnvironment;
        return this;
    }

    public ControlContextBuilder source(
            SourceTextContext source) {
        this.source = source;
        return this;
    }

    public ControlContextBuilder ifHandler(
            IfHandler ifHandler) {
        this.ifHandler = ifHandler;
        return this;
    }

    public ControlContextBuilder elseHandler(
            ElseHandler elseHandler) {
        this.elseHandler = elseHandler;
        return this;
    }

    public ControlContextBuilder doHandler(
            DoHandler doHandler) {
        this.doHandler = doHandler;
        return this;
    }

    public ControlContextBuilder whileHandler(
            WhileHandler whileHandler) {
        this.whileHandler = whileHandler;
        return this;
    }

    public ControlContextBuilder bodyStartHandler(
            BodyStartHandler bodyStartHandler) {
        this.bodyStartHandler = bodyStartHandler;
        return this;
    }

    public ControlContextBuilder bodyEndHandler(BodyEndHandler bodyEndHandler) {
        this.bodyEndHandler = bodyEndHandler;
        return this;
    }

    public ControlContextBuilder conditionExpressionHandler(
            ControlExpressionHandler conditionExpressionHandler) {
        this.conditionExpressionHandler = conditionExpressionHandler;
        return this;
    }

    public ControlContextBuilder sentenceExpressionHandler(
            ControlExpressionHandler sentenceExpressionHandler) {
        this.sentenceExpressionHandler = sentenceExpressionHandler;
        return this;
    }

    public ControlContextBuilder noneNextHandler(
            NoneNextHandler noneNextHandler) {
        this.noneNextHandler = noneNextHandler;
        return this;
    }

    public ControlContextBuilder declareHandler(
            DeclareHandler declareHandler) {
        this.declareHandler = declareHandler;
        return this;
    }

    public ControlContextBuilder breakHandler(
            BreakHandler breakHandler) {
        this.breakHandler = breakHandler;
        return this;
    }

    public ControlContextBuilder continueHandler(ContinueHandler continueHandler) {
        this.continueHandler = continueHandler;
        return this;
    }

    public ControlContextBuilder returnHandler(ReturnHandler returnHandler) {
        this.returnHandler = returnHandler;
        return this;
    }

    public ControlContext build() {
        this.register = new ControlHandlerRegister(ifHandler, elseHandler, doHandler, whileHandler,
                bodyStartHandler, conditionExpressionHandler, sentenceExpressionHandler, noneNextHandler,
                declareHandler, breakHandler, continueHandler, returnHandler, bodyEndHandler
        );
        return new ControlContext(outerEnvironment, source, register);
    }


}
