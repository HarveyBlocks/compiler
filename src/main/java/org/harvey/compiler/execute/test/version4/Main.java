package org.harvey.compiler.execute.test.version4;

import org.harvey.compiler.execute.test.version4.handler.impl.*;
import org.harvey.compiler.execute.test.version4.msg.ControlHandlerRegister;
import org.harvey.compiler.execute.test.version4.msg.SequentialControlElement;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 20:13
 */
@SuppressWarnings("DuplicatedCode")
public class Main {

    public static final ControlHandlerRegister REGISTER = new ControlHandlerRegister().conditionExpressionHandler(
                    new ConditionExpressionHandler())
            .ifHandler(new IfHandlerImpl())
            .elseHandler(new ElseHandlerImpl())
            .whileHandler(new WhileHandlerImpl())
            .doHandler(new DoHandlerImpl())
            .sentenceExpressionHandler(new SentenceEndExpressionHandler())
            .noneNextHandler(new NoneNextHandlerImpl())
            .bodyStartHandler(new BodyStartHandlerImpl())
            .bodyEndHandler(new BodyEndHandlerImpl());

    public static void main(String[] args) {
        ControlPhaser controlPhaser = new ControlPhaser(REGISTER);
        /*{
            if ( c1_1 )
                if ( c2_1 )
                    exp1 ;
                else if ( c2_1 )
                    exp2 ;
                else
                    exp3 ;
            else if ( c1_2 )
                exp4 ;
            else
                exp5 ;
            exp6 ;
         }
         * */
        // in switch
        // 第一行一定是case或者default, 然后表达式, 然后一个:
        // 然后有不限次数个的control, 普通
        // 然后需要case了, 发现外面是switch的block的, 可以加case, 不然, 智能加block
        // 7 的人

        String s = " {                              \n" +
                   "     if ( c1_1 )                \n" +
                   "         if ( c2_1 )            \n" +
                   "             exp1 ;             \n" +
                   "         else if ( c2_1 )       \n" +
                   "             exp2 ;             \n" +
                   "         else                   \n" +
                   "             exp3 ;             \n" +
                   "     else if ( c1_2 )           \n" +
                   "         while  ( c2_2 )        \n" +
                   "            if ( c3_2 )         \n" +
                   "                exp3 ;          \n" +
                   "     else                       \n" +
                   "         do                     \n" +
                   "            while ( c3_3 ) ;    \n" +
                   "         while ( c2_3 ) ;       \n" +
                   "     exp6 ;                     \n" +
                   " }                             ";
        // s = "{ if ( c ) for_true ; else for_false ; }";
        SourceTextContext source = SourceContextTestCreator.newSource(s);
        List<SequentialControlElement> phase = controlPhaser.phase(null, source);
        for (SequentialControlElement element : phase) {
            System.out.println(element);
        }
    }

}
