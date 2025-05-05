package org.harvey.compiler.syntax;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;

import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 00:42
 */
public class AbstractSyntaxTreeBuilder {
    public static void buildTree(StackFrame frame, IExpressionElement next) {
        if (frame.empty() && !isOperator(next)) {
            return;
        }
        //  构建树
        if (isOperator(next)) {
            // 自己不能做子树了, 自己就是root
            if (!frame.treeLinkEmpty()) {
                buildTreeByPriorityCompare(frame.fromRootToCur, (OperatorString) next);
                return;
            }
            DefaultAbstractSyntaxTree root = new DefaultAbstractSyntaxTree((OperatorString) next);
            if (frame.empty()) {
                // 真是第一个, 那初始化
                frame.initTree(root);
            } else if (isItem(frame.pre)) {
                root.setLeft((ItemString) frame.pre);
                frame.initTree(root);
            } else {
                //  if (isOperator(frame.pre)) or else
                // 应该不会, 因为is operator就会添加list里区
                throw new CompilerException("Unable to build syntax tree: First is operator, but not in tree");
            }
        } else if (isItem(next)) {
            // 直接加到cur的右子树
            if (frame.treeLinkEmpty()) {
                // 这种, 就是初始化一个item之后, 再来一个item, 前面居然没有检查出来?
                throw new CompilerException("Unable to build syntax tree: Contiguous items");
            }
            IAbstractSyntaxTree last = frame.fromRootToCur.getLast();
            if (last.getRight() != null) {
                throw new CompilerException(
                        "Unable to build syntax tree: The right subtree of the item that is already occupied");
            }
            last.setRight((ItemString) next);
        } else {
            throw new UnknownTypeException(IExpressionElement.class, next);
        }
    }

    private static boolean isItem(IExpressionElement element) {
        return element instanceof ItemString;
    }

    private static boolean isOperator(IExpressionElement element) {
        return element instanceof OperatorString;
    }

    private static void buildTreeByPriorityCompare(LinkedList<IAbstractSyntaxTree> fromRootToCur, OperatorString next) {
        // TODO
        //  比较优先级后加
        //  优先级比last低. 那就往上走, remove last
        //  优先级一样, 如果是右结合, 做这个last的右子树
        //  优先级一样, 如果是左结合, remove last
        //  优先级比last高, remove last, last做这个的左子树, 自己做last的parent的右子树
        IAbstractSyntaxTree newNode = new DefaultAbstractSyntaxTree(next);
        IAbstractSyntaxTree cur = null;
        while (!fromRootToCur.isEmpty()) {
            cur = fromRootToCur.removeLast();
            OperatorString curOperator = cur.getOperator();
            if (OperatorString.priorityLower(next, curOperator)) {
                // 优先级比last低. 那就往上走, remove last
                continue;
            } else if (OperatorString.priorityLower(curOperator, next)) {
                // 优先级比last高
                linkToTree(fromRootToCur, cur, newNode);
                return;
            } else {
                // 优先级一样
                // 优先级一样, 结合律却不一样
                Associativity associativity = next.getAssociativity();
                if (associativity != curOperator.getAssociativity()) {
                    // 经过分类讨论得出, 不一样的皆不合适
                    if (next.getAssociativity() == Associativity.LEFT) {
                        // next is left and pre is right
                        // + a ++
                        //  +
                        //      ++
                        //   a
                        linkToTree(fromRootToCur, cur, newNode);
                        return;
                    } else {
                        throw new AnalysisTypeException(
                                next.getPosition(),
                                "Unreasonable, meaningless for same priority but  different associativity"
                        );
                    }
                }
                if (associativity == Associativity.LEFT) {
                    //  如果是左结合
                    continue;
                } else if (associativity == Associativity.RIGHT) {
                    // 如果是右结合, remove last
                    linkToTree(fromRootToCur, cur, newNode);
                    return;
                } else {
                    throw new CompilerException("Unknown associativity: " + associativity);
                }
            }
        }
        // 全部结束了, 还没有找到合适的优先级
        // next做root
        newNode.setLeft(cur);
        fromRootToCur.add(newNode);
    }

    private static void linkToTree(
            LinkedList<IAbstractSyntaxTree> fromRootToCur, IAbstractSyntaxTree cur, IAbstractSyntaxTree newNode) {
        //  做这个cur的右子树, cur的右孩子做自己的左子树
        ItemString oldRight = cur.removeRight();
        cur.setRight(newNode);
        newNode.setLeft(oldRight);
        fromRootToCur.addLast(cur);
        fromRootToCur.addLast(newNode);
    }


}
