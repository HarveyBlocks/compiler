package org.harvey.compiler.syntax;

import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.BadCompilerDesignException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.ConstantString;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.execute.expression.IdentifierString;
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
@Deprecated
public class AbstractSyntaxTreeFactory {

    private static ItemString getTreeFromStackAfterCheck(StackFrame frame) {
        if (frame.empty()) {
            if (frame.acceptEmptyInner) {
                return null;
            } else {
                throw new CompilerException("it is impossible for expression is empty");
            }
        }
        if (!frame.treeLinkEmpty()) {
            return frame.fromRootToCur.getFirst();
        }
        if (!isItem(frame.pre)) {
            throw new AnalysisTypeException(frame.pre.getPosition(), "Does not constitute an expression");
        }
        return (ItemString) frame.pre;
    }

    private static void buildTree(StackFrame frame, IExpressionElement next) {
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

    private static boolean isOperator(IExpressionElement pre) {
        if (pre instanceof MayBeOperatorWhileErrorItem) {
            return ((MayBeOperatorWhileErrorItem) pre).asOperator();
        }
        return pre instanceof OperatorString;
    }

    private static boolean isItem(IExpressionElement pre) {
        if (pre instanceof MayBeOperatorWhileErrorItem) {
            return !((MayBeOperatorWhileErrorItem) pre).asOperator();
        }
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
            // 这个是Item, pre的Operator必须符合什么条件?
            return updateNextOperator((OperatorString) next, new OperatorPredicateForPreOperator(pre));
        } else {
            throw new UnknownTypeException(IExpressionElement.class, next);
        }
    }

    private static boolean expectPreItemElseError(
            StackFrame frame) {
        // item item吗? 那肯定不对了
        if (frame.pre instanceof MayBeOperatorWhileErrorItem) {
            // next 被看作Operator了, 那肯定要被感知到啊
            MayBeOperatorWhileErrorItem operatorWhileErrorItem = (MayBeOperatorWhileErrorItem) frame.pre;
            // 不对! 要重新构造这个结构吧 你这个cast就这么cast了吗?
            // TODO, 只有在CAST的优先级最高的情况下才不会出错
            frame.fromRootToCur.addLast(operatorWhileErrorItem.becomeTreeAsOperator());
            return true;
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

    private static boolean isConsiderCast(IExpressionElement pre, Operator expectPost) {
        if (!Operator.PARENTHESES_POST.nameEquals(expectPost.getName())) {
            return false;
        }
        return !CompileOperatorString.is(pre, CompileOperatorString.CompileOperator.INVOKE);
    }

    private static IExpressionElement linkInnerToOuter(
            StackFrame frame, SourcePosition position, boolean considerCast) {
        if (frame.empty()) {
            // 什么, inner什么也没有? 那直接过呗
            // 没啥好link的
            // 但如果外界有link的需求但是内部没有link, 那就要异常了
            if (!frame.acceptEmptyInner) {
                throw new AnalysisTypeException(position, "Does not constitute an expression, for empty inner");
            }
            return new NullItem(position);
        }
        // outer什么也没有, 那就初始化outer
        // (...) 这个是合法的, 其余, 例如 [...] 不合法, 不过应该也在pre就就检查, 所以统统加入
        if (frame.treeLinkEmpty()) {
            // inner 和 outer 的树都是空的
            // inner那就是单一个item,
            if (!(frame.pre instanceof ItemString)) {
                throw new AnalysisTypeException(frame.pre.getPosition(), "expect item");
            }
            return considerCast ? new ParenthesesMayBeCast(position, (ItemString) frame.pre) : frame.pre;
        } else {
            // outer 没有树, 但是inner 有
            IAbstractSyntaxTree root = frame.fromRootToCur.getFirst();

            // outer 直接构建树吗?
            // 如果要抛给下一步比较

            return considerCast ? new ParenthesesMayBeCast(position, root) : root;
        }
    }

    @Deprecated
    private static IExpressionElement linkInnerToOuter(
            StackFrame frame, StackFrame outerFrame, SourcePosition position, boolean considerCast) {
        if (frame.empty()) {
            // 什么, inner什么也没有? 那直接过呗
            // 没啥好link的
            // 但如果外界有link的需求但是内部没有link, 那就要异常了
            if (!frame.acceptEmptyInner) {
                throw new AnalysisTypeException(position, "Does not constitute an expression, for empty inner");
            }
            return new NullItem(position);
        }

        if (outerFrame.empty()) {
            // outer什么也没有, 那就初始化outer
            // (...) 这个是合法的, 其余, 例如 [...] 不合法, 不过应该也在pre就就检查, 所以统统加入
            if (frame.treeLinkEmpty()) {
                // inner 和 outer 的树都是空的
                // inner那就是单一个item,
                if (!(frame.pre instanceof ItemString)) {
                    throw new AnalysisTypeException(frame.pre.getPosition(), "expect item");
                }
                return considerCast ? new ParenthesesMayBeCast(position, (ItemString) frame.pre) : frame.pre;
            } else {
                // outer 没有树, 但是inner 有
                IAbstractSyntaxTree root = frame.fromRootToCur.getFirst();

                // outer 直接构建树吗?
                // 如果要抛给下一步比较

                return considerCast ? new ParenthesesMayBeCast(position, root) : root;
            }

        }
        if (outerFrame.treeLinkEmpty()) {
            // outer是单独一个item
            // 那就是item (item), 直接异常,
            // 因为这种i情况下 一般编译器要有意加入一些内部运算符, 例如INVOKE和AT
            // 应当在pre时就处理
            throw new CompilerException("\n" +
                                        "Can not build for outer frame is empty, should deal this while push new stack frame.\n" +
                                        "May be item after item, for no COMPILE_INNER_OPERATOR set before pre\n");
        }
        // outer 存在树
        ItemString root;
        if (frame.treeLinkEmpty()) {
            // inner 不存在树, 只存在一个item
            // outer 是 存在树的
            if (frame.pre instanceof ItemString) {
                root = (ItemString) frame.pre;
            } else {
                // operator? 由于frame.fromRootToCur是empty的, 所以, 这是一个单独的operator
                throw new AnalysisTypeException(
                        frame.pre.getPosition(),
                        "unique operator is illegal to build expression"
                );
            }
        } else {
            root = frame.fromRootToCur.getFirst();
        }
        IAbstractSyntaxTree cur = outerFrame.fromRootToCur.getLast();
        if (cur.getRight() == null) {
            cur.setRight(root);
        } else {
            throw new CompilerException("\n" +
                                        "Can not set item to right sub tree on cur for has item on it.\n" +
                                        "May be item after item, for no COMPILE_INNER_OPERATOR set before pre\n");
            // 这种情况, 一般就是item item的
            // 本来需要在pre 加入invoke 和 at 这种的, 让 item item 转成 item invoke item, 结果没加
        }
        return considerCast ? new ParenthesesMayBeCast(position, root) : root;
    }

    private static IExpressionElement whileOperator(
            boolean preGetMember, String value, SourcePosition position, ListIterator<SourceString> sourceIterator) {
        //  有些运算符, 看似运算符, 其实是重载运算符的Item!
        if (preGetMember) {
            // 是重载运算符
            Operator[] reloadableOperator = Operators.reloadableOperator(value);
            if (reloadableOperator == null) {
                throw new AnalysisTypeException(position, "Not a reloadable operator: " + value);
            }
            if (Operator.CALLABLE_DECLARE.getName().startsWith(value)) {
                SourceString next = sourceIterator.next();
                if (!Operator.CALLABLE_DECLARE.getName().equals(next.getValue())) {
                    sourceIterator.previous();
                    throw new AnalysisTypeException(next.getPosition(), "expect )");
                }
                return new ReloadOperatorString(position, Operator.ARRAY_DECLARE);
            } else if (Operator.ARRAY_DECLARE.getName().startsWith(value)) {
                SourceString next = sourceIterator.next();
                if (!Operator.ARRAY_DECLARE.getName().equals(next.getValue())) {
                    sourceIterator.previous();
                    throw new AnalysisTypeException(next.getPosition(), "expect ]");
                }
                return new ReloadOperatorString(position, Operator.ARRAY_DECLARE);
            } else if (reloadableOperator.length == 1) {
                return new ReloadOperatorString(position, reloadableOperator[0]);
            } else {
                return new ReloadOperatorString(position, reloadableOperator);
            }

        } else {
            Operator[] operators = Operators.get(value);
            if (operators == null || operators.length == 0) {
                throw new AnalysisTypeException(position, "Unknown operator: " + value);
            }

            if (operators.length == 1) {
                return new NormalOperatorString(position, operators[0]);
            }
            return new UncertainOperatorString(position, operators);
        }
    }

    public static void main(String[] args) {
        SourceTextContext source = SourceContextTestCreator.newSource(" a  ( c , d )");
        source = CoreCompiler.registerChain().execute(source);
        ItemString expression = new AbstractSyntaxTreeFactory().createExpression(source.listIterator());
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
                // 如果这是一个 pre , 那么应当有一个next, 这个next和outer.frame.pre是要检查关系的
                // 可是, 都要构建了 如果前面是item, 对于[] 如果不是item就要异常
                // 不对不对
                // 对于[]前面的, 不是item或者'左单'就要异常, 是item就正常进行
                // 对于()前面的, item或者'左单'的就是invoke, 不是'()'就是cast/ph
                // 还需要考虑什么左单吗? ()的优先级最高啦!
                // 答案是需要考虑的, 依照CPP的策略(cpp的重载运算符功能)
                updateFrame(nextSource.getValue(), nextSource.getPosition(), stack);
            } else {
                StackFrame frame = stack.peek();
                IExpressionElement next = toElement(nextSource, isGetMemberOperator(frame.pre), true, source);
                decideOperator(frame, next);
            }
        }
        if (stack.size() != 1) {
            throw new AnalysisTypeException(
                    source.previous().getPosition(), "Does not constitute an expression yet.");
        }
        return getTreeFromStackAfterCheck(stack.peek());
    }

    private void decideOperator(StackFrame frame, IExpressionElement next) {
        while (true) {
            boolean canMoveToNext = decideOperator0(frame, next);
            if (canMoveToNext) {
                break;
            }
        }
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
     * @return true if reset pre successfully, can move to next
     */
    private boolean decideOperator0(StackFrame frame, IExpressionElement next) {
        if (frame.empty()) {
            next = updateNextForPreNull(next);
            // 构建树-初始化
            if (!frame.treeLinkEmpty()) {
                throw new CompilerException(
                        "Unknown condition for tree build frame: frame.root != null || frame.cur == null");
            }
            if (isOperator(next)) {
                buildTree(frame, next);
            }
            frame.pre = next;
            return true;
        }
        if (isOperator(frame.pre)) {
            next = updateNextForPreOperator((OperatorString) frame.pre, next);
        } else if (isItem(frame.pre)) {
            // 其中有更改frame.pre, 如果发生了更改, 怎么办呢
            // pre
            if (next instanceof ItemString) {
                boolean canBeOperator = expectPreItemElseError(frame);
                // pre 是 operator了是否应该重新构建呢?
                if (!canBeOperator) {
                    throw new AnalysisTypeException(next.getPosition(), "expect operator");
                }
                // 应该重新构建
                return false;
            } else if (next instanceof OperatorString) {
                next = updateNextOperator((OperatorString) next, OperatorPredicate.FOR_PRE_ITEM);
            } else {
                throw new UnknownTypeException(IExpressionElement.class, next);
            }
        } else {
            throw new UnknownTypeException(IExpressionElement.class, frame.pre);
        }
        if (next instanceof UncertainOperatorString) {
            // 应当避免在前面审查之后还是不能确定Operator
            throw new BadCompilerDesignException(
                    "uncertain operators: " + Arrays.toString(((UncertainOperatorString) frame.pre).getOperators()));
        }
        buildTree(frame, next);
        frame.pre = next;
        return true;
    }

    /**
     * @param operator
     * @param position
     * @param stack
     * @return null for into, else for out to
     */
    private void updateFrame(String operator, SourcePosition position, Stack<StackFrame> stack) {
        StackFrame frame = stack.peek();
        // [ -> at / generic
        // ( -> call / cast / PARENTHESES

        // 所有的at和
        // 保证所有的 pre - post 结构都是同一优先级, 且是左结合的
        if (Operator.PARENTHESES_PRE.nameEquals(operator)) {
            // 这是pre了
            // 如果前面是empty/operator, pre就一定是(
            // 如果前面是item, pre就一定是( call 或 [
            //      其中依靠结合率 call 能和  cast / PARENTHESES 区分
            //      cast 和 PARENTHESES 的区分会在后续, CAST的转变需要时转变
            //          CALL 是 单元运算符, 左结合, CAST 是单元运算符, 右结合, PARENTHESES 是 ITEM
            if (canBeCircleInvoke(frame.pre)) {
                CompileOperatorString compileOperator = new CompileOperatorString(
                        position, CompileOperatorString.CompileOperator.INVOKE);
                StackFrame inner = innerFrameForCircleInvoke(frame, compileOperator, Operator.CALL_POST);
                stack.push(inner);
            } else {
                StackFrame inner = innerFrameForParentheses();
                stack.push(inner);
            }


        } else if (Operator.ARRAY_AT_PRE.nameEquals(operator)) {
            if (!canBeCircleInvoke(frame.pre)) {
                throw new AnalysisTypeException(position, "expect pre item");
            }
            CompileOperatorString compileOperator = new CompileOperatorString(
                    position, CompileOperatorString.CompileOperator.ARRAY_AT);
            StackFrame inner = innerFrameForCircleInvoke(frame, compileOperator, Operator.ARRAY_AT_POST);
            stack.push(inner);

        } else if (operator.equals(frame.expectPost.getName())) {
            stack.pop();
            if (stack.empty()) {
                // 不应该啊, 应该就是没有准备 invoke 和 at 之类的
                throw new CompilerException("expect outer frame to put frame as sub tree");
            }
            StackFrame outerFrame = stack.peek();
            boolean considerCast = isConsiderCast(outerFrame.pre, frame.expectPost);
            IExpressionElement nextItem = linkInnerToOuter(frame, position, considerCast);

            decideOperator(outerFrame, nextItem);
        } else {
            throw new CompilerException("Unknown operator: " + operator);
        }
        // return的frame, 应当认作pre是item吧?
        // 如果item被放到了别的tree里了?
        // 如果是pre, 那么就建立新的frame, frame.pre就是null
        // 如果是post, 那么从栈中取出前一个的最后, 然后组合树, 然后把外层的返回
        // pre , 返回null, 表示不用检查, 否则, 检查
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

    private StackFrame innerFrameForParentheses() {
        return new StackFrame(false, false, Operator.PARENTHESES_POST);
    }

    /**
     * 处理 array[index] 和 callable(argument)这种的pre
     *
     * @return inner
     */
    private StackFrame innerFrameForCircleInvoke(
            StackFrame outer, CompileOperatorString compileOperator, Operator expectPost) {
        // cur 指向 empty, 咋办...
        decideOperator(outer, compileOperator);
        // 构造inner
        return new StackFrame(true, false, expectPost);
    }

    private boolean isGetMemberOperator(IExpressionElement pre) {
        return pre instanceof NormalOperatorString && ((NormalOperatorString) pre).isOperator(Operator.GET_MEMBER);
    }

    /**
     * TODO 暂时只有 on expression, 还没有别的
     *
     * @param onExpression true for on expression and false for on control
     * @return 不联系上下文的初步转换
     */
    private IExpressionElement toElement(
            SourceString source,
            boolean preGetMember,
            boolean onExpression,
            ListIterator<SourceString> sourceIterator) {
        SourcePosition position = source.getPosition();
        String value = source.getValue();
        switch (source.getType()) {
            case SIGN:
                // ;->结束标志
                // { -> 何尝不是一种operator
                // } -> 何尝不是一种operator
                // 如果一个表达式里含有{}, 就要考虑lambda里的control, 如果一个方法能同时构建表达式和control, 那再好不过了!
                return whileSign(value, position, onExpression);
            case SINGLE_LINE_COMMENTS:
            case LINE_SEPARATOR:
            case MULTI_LINE_COMMENTS:
            case MIXED:
            case ITEM:
                // 异常, 不该出现在当前阶段
                throw new CompilerException(
                        "It shouldn't be in the case of building an abstract syntax tree,for source type:" +
                        source.getType());
            case STRING:
            case CHAR:
            case BOOL:
            case INT32:
            case INT64:
            case FLOAT32:
            case FLOAT64:
                // item的常量
                return ConstantString.constantString(source);
            case OPERATOR:
                // 运算符
                //
                return whileOperator(preGetMember, value, position, sourceIterator);
            case KEYWORD:
                // 可能是运算符, 可能是Item(this/super/null),可能是基本数据类型, 可能是控制结构, 可能是不允许
                Keyword keyword = Keyword.get(value);
                if (keyword == null) {
                    throw new CompilerException("Unknown keyword: " + value);
                }
                return whileKeyword(keyword, position, preGetMember, onExpression);
            case IGNORE_IDENTIFIER:
                // item的变量
                // 需要一个identifier 变成reference 的工具类呢!
                return new IdentifierString(position);
            case IDENTIFIER:
                // item的Identifier
                return new IdentifierString(position, value);
            default:
                throw new CompilerException("Unknown type:" + source.getType());
        }
    }

    private IExpressionElement whileSign(String value, SourcePosition position, boolean onExpression) {
        // TODO
        // 是停止解析?  外界如何得知要停止解析了?
        // 是抛出异常? 一个方式是让外界给处理结构, 以下是几个可接受的策略, 外界可以自定义选择哪个策略:
        //      - previous and break
        //      - previous and return
        //      - previous and throw
        //      - break
        //      - return
        //      - throw
        //      - continue
        //      - as a operator
        // 是返回一个Item 还是 一个 Operator, 需要一个 Control 和 expression 都可以的构建抽象语法树的逻辑\
        // 对于{}, expression中可能也有这个符号,
        //      可能是 ArrayInit,
        //      可能是Lambda, Lambda的话, 里面还要能分析Control
        //      可能是StructClone(还有吗?)
        switch (value) {
            case ";":
            case "{":
            case "}":
                break;
            default:
                throw new CompilerException("Unexpected value as sign: " + value);
        }
        throw new UnfinishedException("sign in expression");
    }

    private IExpressionElement whileKeyword(
            Keyword keyword, SourcePosition position, boolean preGetMember, boolean onExpression) {
        // 是 what ?
        // TODO
        if (Keyword.NEW == keyword) {
            System.out.println("new");
        } else if (Keywords.isOperator(keyword)) {
            System.out.println("operator");
        } else if (Keywords.isBasicType(keyword)) {
            System.out.println("basic type");
        } else if (Keywords.isControlStructure(keyword)) {
            System.out.println("control structure");
        } else if (isItemKeyword(keyword)) {
            System.out.println("item");
        } else if (Keywords.isAccessControl(keyword)) {
            System.out.println("access control");
        } else {
            System.out.println("else");
        }
        // keyword
        throw new UnfinishedException("keyword in expression");
    }

    private boolean isItemKeyword(Keyword keyword) {
        // TODO 什么叫item? 未定
        // 可以确定的有: this/super/
        // 未确定的有:
        //  - class
        //      .class来获取字节码对象,
        //      Type<MyClass> type = Type.of<MyClass>();
        //          使用这种方法, 就要想办法设计一种, 让翻译器获取到泛型的方法
        //          当然, 翻译器的前面n个参数可以获取泛型的结构信息的id啦
        //  - file
        //      用file关键字表示本文件
        //      文件名表示本文件
        return keyword == Keyword.THIS || keyword == Keyword.SUPER;
    }


}
