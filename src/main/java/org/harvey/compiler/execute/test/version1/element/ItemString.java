package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.expression.IExpressionElement;

/**
 * 表达式
 * exp -> item
 * exp -> exp bi_oper exp
 * exp -> un_oper exp
 * exp -> (exp)
 * 这个接口就是上面的item
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 15:55
 */
public interface ItemString extends IExpressionElement {

}
