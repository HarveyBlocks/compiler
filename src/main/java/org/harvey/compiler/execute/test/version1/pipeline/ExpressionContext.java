package org.harvey.compiler.execute.test.version1.pipeline;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.DefaultRandomlyIterator;
import org.harvey.compiler.common.collecction.RandomlyAccessAble;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 解析表达式的上下文
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:53
 */
@SuppressWarnings("unused")
public class ExpressionContext extends DefaultRandomlyIterator<SourceString> {
    private final LinkedList<TodoTask> todoQueue = new LinkedList<>();
    private final Expression expression = new Expression();
    private final OuterEnvironment outerEnvironment;

    public ExpressionContext(List<SourceString> source, OuterEnvironment outerEnvironment) {
        super(RandomlyAccessAble.forList(source), 0);
        this.outerEnvironment = outerEnvironment;
    }

    public void addTodoTask(TodoTask todo) {
        todoQueue.addLast(todo);
    }

    public boolean hasTodo() {
        return !todoQueue.isEmpty();
    }

    public TodoTask popTodoTask() {
        return todoQueue.removeFirst();
    }


    @Override
    public SourceString previous() {
        super.previous();
        return null;
    }

    public void previousMove() {
        super.previous();
    }

    public ExpressionElement getPrevious() {
        return getPrevious(1);
    }


    public boolean expressionHasPrevious(int i) {
        return expression.size() >= i && i > 0;
    }


    public boolean expressionHasPrevious() {
        return expressionHasPrevious(1);
    }

    public ExpressionElement getPrevious(int i) {
        return expression.get(expression.size() - i);
    }

    public void add(ExpressionElement next) {
        this.expression.add(next);
    }

    public boolean nextIs(Predicate<SourceString> predicate) {
        return CollectionUtil.nextIs(this, predicate);
    }

    public boolean finished() {
        return hasNext();
    }

    public Expression result() {
        if (finished()) {
            throw new CompilerException("not finished expression");
        }
        return expression;
    }


    public void addAllTodoTask(ExpressionContext other) {
        if (other == this) {
            return;
        }
        this.todoQueue.addAll(other.todoQueue);
    }


    public MemberSupplier createFromMemberManager(SourcePosition using, String name) {
        return outerEnvironment.createFromMemberManager(using, name);
    }

    public OuterEnvironment getEnvironment() {
        return outerEnvironment;
    }

    public PossibleCallableSupplier createFromMemberManagerAsCallable(SourcePosition using, String callableName) {
        return outerEnvironment.createPossibleFromMemberManager(using, callableName);
    }

    public boolean typeDetermined() {
        // 已经解析完毕, 且对象有确定类型
        return !this.hasNext()&& outerEnvironment.typeDetermined();
    }
}
