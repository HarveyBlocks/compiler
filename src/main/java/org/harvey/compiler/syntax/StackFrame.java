package org.harvey.compiler.syntax;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IExpressionElement;

import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:08
 */
public class StackFrame {
    // 有一个坏处在于, one.addAll(other), 是加入所有元素, 这将导致遍历O(n), 不好, 本来 尾链接头, O(1)
    final LinkedList<IAbstractSyntaxTree> fromRootToCur = new LinkedList<>();
    final boolean acceptEmptyInner;
    final boolean acceptOnlyType;
    final Operator expectPost;
    IExpressionElement pre;


    StackFrame(boolean acceptEmptyInner, boolean acceptOnlyType, Operator expectPost) {
        this.acceptEmptyInner = acceptEmptyInner;
        this.acceptOnlyType = acceptOnlyType;
        this.pre = null;
        this.expectPost = expectPost;
    }

    boolean empty() {
        return pre == null;
    }

    boolean treeLinkEmpty() {
        return fromRootToCur.isEmpty();
    }

    public void initTree(IAbstractSyntaxTree root) {
        if (!fromRootToCur.isEmpty()) {
            throw new CompilerException("can not init for has built tree");
        }
        fromRootToCur.add(root);
    }


}
