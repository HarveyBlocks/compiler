package org.harvey.compiler.syntax;


import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;

/**
 * 这个类优先被认定为Item, 如果Item在构建语法树的时候会出错, 则这个类将称为OperatorString
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-02 15:04
 */
public interface MayBeOperatorWhileErrorItem extends ItemString, OperatorString {

    DefaultAbstractSyntaxTree becomeTreeAsOperator();

    ItemString becomeItem();

    boolean asOperator();
}
