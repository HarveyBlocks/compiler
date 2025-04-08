package org.harvey.compiler.execute.test.version2.msg;

import lombok.AllArgsConstructor;
import org.harvey.compiler.execute.test.version2.handler.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 21:35
 */
@AllArgsConstructor
 class ControlHandlerRegister {
   final IfHandler ifHandler;
   final ElseHandler elseHandler;
   final DoHandler doHandler;
   final WhileHandler whileHandler;
   final BodyStartHandler bodyStartHandler;
   final ControlExpressionHandler conditionExpressionHandler;
   final ControlExpressionHandler sentenceExpressionHandler;
   final NoneNextHandler noneNextHandler;
   final DeclareHandler declareHandler;
   final BreakHandler breakHandler;
   final ContinueHandler continueHandler;
   final ReturnHandler returnHandler;
   final BodyEndHandler bodyEndHandler;
}
