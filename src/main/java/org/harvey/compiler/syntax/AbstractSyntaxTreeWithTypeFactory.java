package org.harvey.compiler.syntax;

import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.BadCompilerDesignException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.test.SourceContextTestCreator;
import org.harvey.compiler.execute.test.version1.element.CompileOperatorString;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.*;

/**
 * 只构建结构 不检查Identifier和类型
 * 1. 构建结构
 * 2. Identifier具体是啥? 标注, a().b(), 所以类型检查也要在同一时间上qwq
 * 3. 括号 表示类型转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-01 16:08
 */
public class AbstractSyntaxTreeWithTypeFactory {
    private final SimpleSourceElementAdapter adapter;

    public AbstractSyntaxTreeWithTypeFactory() {
        adapter = new SimpleSourceElementAdapter();
    }

    private static IExpressionElement getTreeFromInnerStack(StackFrame inner, SourcePosition position) {
        if (inner.empty()) {
            if (inner.acceptEmptyInner) {
                return null;
            } else {
                throw new AnalysisTypeException(
                        position, "Does not constitute an expression, for empty inner expression");
            }
        }
        if (!inner.treeLinkEmpty()) {
            IAbstractSyntaxTree last = inner.fromRootToCur.getLast();
            OperatorString lastOperator = last.getOperator();
            if (lastOperator.getOperandCount() == OperandCount.BINARY) {
                if (last.getRight() == null) {
                    throw new AnalysisTypeException(position, "expected an item");
                }
            } else if (lastOperator.getOperandCount() == OperandCount.UNARY &&
                       lastOperator.getAssociativity() == Associativity.RIGHT) {
                if (inner.acceptOnlyType && lastOperator instanceof CastOperator) {
                    return lastOperator;
                }
                if (last.getRight() == null) {
                    throw new AnalysisTypeException(position, "expected an item");
                }
            }
            return inner.fromRootToCur.getFirst();
        }
        if (!isItem(inner.pre)) {
            throw new AnalysisTypeException(inner.pre.getPosition(), "Does not constitute an expression");
        }
        return inner.pre;
    }

    private static boolean isOperator(IExpressionElement pre) {
        return pre instanceof OperatorString;
    }

    private static boolean isItem(IExpressionElement pre) {
        return pre instanceof ItemString;
    }

    private static IExpressionElement updateNextForPreOperator(OperatorString pre, IExpressionElement next) {
        if (next instanceof ItemString) {
            // 这个是Item, pre的Operator必须符合什么条件?
            // 前一个是单左, 则next不能是item, 否则可
            if (pre.getAssociativity() == Associativity.LEFT && pre.getOperandCount() == OperandCount.UNARY) {
                throw new AnalysisTypeException(next.getPosition(), "not expect an item");
            } else {
                return next;
            }
        } else if (next instanceof OperatorString) {
            if (isComma((OperatorString) next) && isCast(pre)) {
                // 允许 type comma
                return next;
            } else {
                return updateNextOperator((OperatorString) next, new OperatorPredicateForPreOperator(pre));
            }
        } else {
            throw new UnknownTypeException(IExpressionElement.class, next);
        }
    }

    private static boolean isCast(OperatorString operator) {
        return operator instanceof CastOperator;
    }

    private static boolean isComma(OperatorString operator) {
        if (operator instanceof NormalOperatorString) {
            return ((NormalOperatorString) operator).getValue() == Operator.COMMA;
        } else {
            return false;
        }
    }

    private static IExpressionElement updateNextForPreNull(
            IExpressionElement next) {
        if (next instanceof ItemString) {
            return next;
        } else if (next instanceof OperatorString) {
            return updateNextOperator((OperatorString) next, OperatorPredicate.FOR_PRE_NULL);
        } else {
            throw new UnknownTypeException(IExpressionElement.class, next);
        }
    }

    private static OperatorString updateNextOperator(
            OperatorString next, OperatorPredicate predicate) {
        SourcePosition position = next.getPosition();
        if (next instanceof UncertainOperatorString) {
            // 单,右不行, 其余行
            Operator[] uncertainOperators = ((UncertainOperatorString) next).getOperators();
            // 挑选出operandCount是2,associativity是1的
            List<Operator> chosen = new ArrayList<>();
            for (Operator uncertainOperator : uncertainOperators) {
                OperandCount operandCount = uncertainOperator.getOperandCount();
                Associativity associativity = uncertainOperator.getAssociativity();
                // operandCount != OperandCount.UNARY || associativity != Associativity.RIGHT
                if (predicate.test(associativity, operandCount)) {
                    chosen.add(uncertainOperator);
                }
            }
            if (chosen.isEmpty()) {
                throw new AnalysisTypeException(position, "expect a correct operator");
            }
            if (chosen.size() == 1) {
                // 需要由post决定
                return new NormalOperatorString(position, chosen.get(0));
            } else {
                throw new BadCompilerDesignException("multiple operator to make sure" + chosen.get(0).getName());
            }
        } else {
            // 单,右不行, 其余行
            if (!predicate.test(next.getAssociativity(), next.getOperandCount())) {
                throw new AnalysisTypeException(next.getPosition(), "not expected this operator");
            }
        }
        return next;
    }

    public static void main(String[] args) {
        SourceTextContext source = SourceContextTestCreator.newSource(
                " + ++  a ++ ++ [ A , B ]  ( c , ( int8 ) ++ d )");
        source = CoreCompiler.registerChain().execute(source);
        ItemString expression = new AbstractSyntaxTreeWithTypeFactory().createExpression(source.listIterator());
        System.out.println(expression);
    }

    public ItemString createExpression(ListIterator<SourceString> source) {
        if (!source.hasNext()) {
            return null;
        }
        Stack<StackFrame> stack = new Stack<>();
        stack.push(new StackFrame(true, false, null));
        while (source.hasNext()) {
            SourceString nextSource = source.next();
            if (shouldUpdateFrame(nextSource)) {
                updateFrame(nextSource.getValue(), nextSource.getPosition(), stack);
            } else {
                StackFrame frame = stack.peek();
                IExpressionElement next = adapter.toElement(nextSource, isGetMemberOperator(frame.pre), true, source);
                decideOperator(frame, next);
            }
        }
        if (stack.size() != 1) {
            throw new AnalysisTypeException(
                    source.previous().getPosition(), "Does not constitute an expression yet.");
        }
        return (ItemString) getTreeFromInnerStack(stack.peek(), source.previous().getPosition());
    }

    private boolean isGetMemberOperator(IExpressionElement pre) {
        return pre instanceof NormalOperatorString && ((NormalOperatorString) pre).isOperator(Operator.GET_MEMBER);
    }

    private void decideOperator(StackFrame frame, IExpressionElement next) {
        next = decideOperator0(frame, next);
        if (next instanceof UncertainOperatorString) {
            // 应当避免在前面审查之后还是不能确定Operator
            throw new BadCompilerDesignException(
                    "uncertain operators: " + Arrays.toString(((UncertainOperatorString) frame.pre).getOperators()));
        }
        AbstractSyntaxTreeBuilder.buildTree(frame, next);
        frame.pre = next;
    }

    private boolean shouldUpdateFrame(SourceString next) {
        if (next.getType() != SourceType.OPERATOR) {
            return false;
        }
        return Operators.isPre(next.getValue()) || Operators.isPost(next.getValue());
    }

    /**
     * 根据上下文, 分析 item和operator之间的关系, 依据{@link OperandCount} 和{@link Associativity}判断Operator或item是否合适
     *
     * @return next
     */
    private IExpressionElement decideOperator0(StackFrame frame, IExpressionElement next) {
        if (frame.empty()) {
            next = updateNextForPreNull(next);
            // 构建树-初始化
            if (!frame.treeLinkEmpty()) {
                throw new CompilerException(
                        "Unknown condition for tree build frame: frame.root != null || frame.cur == null");
            }
            return next;
        }
        if (isOperator(frame.pre)) {
            return updateNextForPreOperator((OperatorString) frame.pre, next);
        } else if (isItem(frame.pre)) {
            // 其中有更改frame.pre, 如果发生了更改, 怎么办呢
            // pre
            if (next instanceof ItemString) {
                throw new AnalysisTypeException(next.getPosition(), "expect operator");
            } else if (next instanceof OperatorString) {
                return updateNextOperator((OperatorString) next, OperatorPredicate.FOR_PRE_ITEM);
            } else {
                throw new UnknownTypeException(IExpressionElement.class, next);
            }
        } else {
            throw new UnknownTypeException(IExpressionElement.class, frame.pre);
        }
    }

    private void updateFrame(String operator, SourcePosition position, Stack<StackFrame> stack) {
        StackFrame frame = stack.peek();
        // [ -> at / generic
        // ( -> call / cast / PARENTHESES
        if (Operator.PARENTHESES_PRE.nameEquals(operator)) {
            pushParentheses(position, stack, frame);
        } else if (Operator.ARRAY_AT_PRE.nameEquals(operator)) {
            pushArrayAt(position, stack, frame);
        } else if (operator.equals(frame.expectPost.getName())) {
            popInner(position, stack, frame);
        } else {
            throw new CompilerException("Unknown operator: " + operator);
        }

    }

    private void pushParentheses(SourcePosition position, Stack<StackFrame> stack, StackFrame frame) {
        // 这是pre了
        // 如果前面是empty/operator, pre就一定是(
        // 如果前面是item, pre就一定是( call 或 [
        //      其中依靠结合率 call 能和  cast / PARENTHESES 区分
        //      cast 和 PARENTHESES 的区分会在后续, CAST的转变需要时转变
        //          CALL 是 单元运算符, 左结合, CAST 是单元运算符, 右结合, PARENTHESES 是 ITEM
        if (canBeCircleInvoke(frame.pre)) {
            CompileOperatorString compileOperator = new CompileOperatorString(
                    position, CompileOperatorString.CompileOperator.INVOKE);
            StackFrame inner = innerFrameForCircleInvoke(frame, compileOperator, Operator.CALL_POST, false);
            stack.push(inner);
        } else {
            StackFrame inner = new StackFrame(false, true, Operator.PARENTHESES_POST);
            stack.push(inner);
        }
    }

    private void pushArrayAt(SourcePosition position, Stack<StackFrame> stack, StackFrame frame) {
        if (!canBeCircleInvoke(frame.pre)) {
            throw new AnalysisTypeException(position, "expect pre item");
        }
        CompileOperatorString compileOperator = new CompileOperatorString(
                position, CompileOperatorString.CompileOperator.ARRAY_AT);
        StackFrame inner = innerFrameForCircleInvoke(frame, compileOperator, Operator.ARRAY_AT_POST, true);
        stack.push(inner);
    }

    private void popInner(SourcePosition position, Stack<StackFrame> stack, StackFrame frame) {
        stack.pop();
        if (stack.empty()) {
            // 不应该啊, 应该就是没有准备 invoke 和 at 之类的
            throw new CompilerException("expect outer frame to put frame as sub tree");
        }
        StackFrame outerFrame = stack.peek();
        IExpressionElement nextItem = getTreeFromInnerStack(frame, position);
        decideOperator(outerFrame, nextItem);
    }

    private boolean canBeCircleInvoke(IExpressionElement pre) {
        // at 和 generic 的区分 需要等到Identifier
        // 所以无论如何都认定是expect pre item(优先级最高, 所以pre直接是item)
        if (pre == null) {
            // 不允许
            return false;
        }
        if (isOperator(pre)) {
            // 只允许左单
            OperatorString operatorString = (OperatorString) pre;
            return operatorString.getAssociativity() == Associativity.LEFT &&
                   operatorString.getOperandCount() == OperandCount.UNARY;
        }
        if (isItem(pre)) {
            // 这里不是item 就会出错, 所以有一个机会转成operator, 这个operator还能再次检查结构, 由于这个operator吗?
            // 我想, 如果前面是operator, 那么就不改, 下一次, 解析完毕后, 再次检查pre和next之间的关系
            return true;
        }
        throw new UnknownTypeException(IExpressionElement.class, pre);
    }

    /**
     * 处理 array[index] 和 callable(argument)这种的pre
     *
     * @return inner
     */
    private StackFrame innerFrameForCircleInvoke(
            StackFrame outer, CompileOperatorString compileOperator, Operator expectPost, boolean acceptOnlyType) {
        // cur 指向 empty, 咋办...
        decideOperator(outer, compileOperator);
        // 构造inner
        return new StackFrame(true, acceptOnlyType, expectPost);
    }


}
