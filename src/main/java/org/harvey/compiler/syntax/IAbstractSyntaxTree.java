package org.harvey.compiler.syntax;


import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:58
 */
public interface IAbstractSyntaxTree extends ItemString {
    OperatorString getOperator();

    ItemString getLeft();

    void setLeft(ItemString left);

    ItemString getRight();

    void setRight(ItemString right);

    ItemString removeLeft();

    ItemString removeRight();
}
