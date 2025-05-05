package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.link.LinkTypeFactory;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.store.SourceTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.BoundsForPlaceholderStoreCommand;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeStoreCommand;
import org.harvey.compiler.type.generic.register.entity.GenericOperator;
import org.harvey.compiler.type.raw.KeywordBasicType;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * TODO
 * 先用简单的一趟式转成简单的逆波兰表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
public class SimpleTypeCommandFactory {
    public static final String AND_NAME = Operator.BITWISE_AND.getName();
    public static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    public static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    public static final String COMMA_NAME = Operator.COMMA.getName();
    public static final String INNER_NAME = Operator.GET_MEMBER.getName();

    /**
     * 转成逆波兰表达式
     * 需要在合适的时候知道表达式已经结束了
     * 每当没有[的时候, 就是已经结束了
     */
    public static List<GenericTypeRegisterCommand> create(ListIterator<SourceString> sourceIterator) {
        // id[][].id
        // skip 一个id.id.id.id, until not 这个结构
        // [][]这个结构, 怎么办?
        // [ 前一定是 st
        // ] 前存在[ , 则register, 不存在则previous-break
        // stack <int> 是 [ 则 stack.push(0)
        //              是 comma 则 ++
        //              是 ] 则 pop + 1
        Stack<GenericOperator> operatorStack = new Stack<>();
        LinkedList<GenericTypeRegisterCommand> expression = new LinkedList<>();
        boolean expectItem = true;
        boolean preInner = false;
        SourcePosition position = null;
        Stack<FullIdentifierString> preStack = new Stack<>();
        while (sourceIterator.hasNext()) {
            SourceString next = sourceIterator.next();
            position = next.getPosition();
            // 可能已经结束了, 除非
            // next is DOT 表示没结束, 否则表示结束
            if (next.getType() == SourceType.OPERATOR) {
                boolean still = dealOperator(next, expression, operatorStack, preStack);
                if (!still) {
                    sourceIterator.previous();
                    break;
                }
                if (GENERIC_LIST_POST_NAME.equals(next.getValue())) {
                    if (!isExpressionStill(operatorStack, CollectionUtil.getNext(sourceIterator))) {
                        break;
                    }
                    expectItem = false;
                } else {
                    expectItem = true;
                }
                preInner = INNER_NAME.equals(next.getValue());
            } else if (Keyword.EXTENDS.equals(next.getValue())) {
                preStack.pop();
                addOperator(expression, operatorStack, GenericOperator.EXTENDS, next.getPosition());
                expectItem = true;
                preInner = false;
            } else if (Keyword.SUPER.equals(next.getValue())) {
                preStack.pop(); // operatorStack
                addOperator(expression, operatorStack, GenericOperator.SUPER, next.getPosition());
                expectItem = true;
                preInner = false;
            } else if (isItem(next.getType())) {
                if (!expectItem) {
                    // throw new AnalysisTypeException(position, "unexpected an identifier");
                    sourceIterator.previous();
                    break;
                }
                TypeStoreCommand typeStoreCommand = typeItem(preStack, next, sourceIterator, preInner);
                if (typeStoreCommand == null) {
                    // 没有
                    sourceIterator.previous();
                    break;
                }
                expression.add(typeStoreCommand);
                if (preInner) {
                    expression.add(new InnerType());
                }
                expectItem = false;
                preInner = false;
            } else if (expectItem) {
                throw new AnalysisTypeException(position, "expected an identifier");
            } else {
                sourceIterator.previous();
                break;
            }
        }
        // stack is clear
        if (!operatorStack.empty()) {
            throw new AnalysisTypeException(position, "expect " + Operator.GENERIC_LIST_PRE.getName());
        }
        return expression;
    }

    private static boolean isExpressionStill(
            Stack<GenericOperator> operatorStack, SourceString next) {
        // id[]->end
        // id.->not end
        if (!operatorStack.empty()) {
            return true;
        }
        if (next.getType() != SourceType.OPERATOR) {
            return false;
        }
        return INNER_NAME.equals(next.getValue());
    }

    private static void addOperator(
            LinkedList<GenericTypeRegisterCommand> expression,
            Stack<GenericOperator> operatorStack,
            GenericOperator genericOperator,
            SourcePosition sourcePosition) {
        if (genericOperator == GenericOperator.POST) {
            // until pre
            // a + b - c
            //
            int comma = 0;
            while (!operatorStack.empty()) {
                GenericOperator top = operatorStack.pop();
                if (top == GenericOperator.PRE) {
                    // top 比 当前 更优先
                    expression.add(new RegisterGenericParamCount(comma + 1));
                    return;
                } else if (top == GenericOperator.COMMA) {
                    comma++;
                } else {
                    expression.add(top.getCommandSupplier().get());
                }
            }
            throw new AnalysisTypeException(sourcePosition, "expect generic list pre");
        } else {
            // 优先级更小的统统出来
            while (!operatorStack.empty()) {
                GenericOperator top = operatorStack.pop();
                if (top == GenericOperator.PRE || top.getPriority() > genericOperator.getPriority()) {
                    // 当前 比 top 更优先
                    operatorStack.push(top);
                    break;
                }
                // top 比 当前 更优先
                expression.add(top.getCommandSupplier().get());
            }
            operatorStack.push(genericOperator);
        }
    }

    private static boolean dealOperator(
            SourceString next,
            LinkedList<GenericTypeRegisterCommand> expression,
            Stack<GenericOperator> operatorStack,
            Stack<FullIdentifierString> preStack) {
        String value = next.getValue();
        if (GENERIC_LIST_PRE_NAME.equals(value)) {
            if (expression.isEmpty()) {
                // throw new AnalysisTypeException(next.getPosition(), "expect identifier pre");
                // not still
                return false;
            }
            addOperator(expression, operatorStack, GenericOperator.PRE, next.getPosition());
            // except item
        } else if (GENERIC_LIST_POST_NAME.equals(value)) {
            if (operatorStack.empty()) {
                // 没有pre,当然
                // end
                // sourceIterator.previous();
                return false;
            }
            preStack.pop();
            addOperator(expression, operatorStack, GenericOperator.POST, next.getPosition());
            // except comma or dot or not still
        } else if (COMMA_NAME.equals(value)) {
            if (operatorStack.empty()) {
                // end
                // sourceIterator.previous();
                return false;
            }
            preStack.pop();
            addOperator(expression, operatorStack, GenericOperator.COMMA, next.getPosition());
            // except item
        } else if (INNER_NAME.equals(value)) {
            if (expression.isEmpty()) {
                throw new AnalysisTypeException(next.getPosition(), "expect pre identifier");
            }
            // except item
            // 由于inner是优先级最高的, 所以直接加inner type
            // 这样也保证了最后operator stack 一定是empty的
        } else if (AND_NAME.equals(value)) {
            if (expression.isEmpty()) {
                throw new AnalysisTypeException(next.getPosition(), "expect pre identifier");
            }
            // except item
            preStack.pop();
            addOperator(expression, operatorStack, GenericOperator.AND, next.getPosition());
        } else {// sourceIterator.previous();
            return false;
        }
        return true;
    }

    private static boolean isItem(SourceType type) {
        return type == SourceType.IDENTIFIER || type == SourceType.KEYWORD || type == SourceType.IGNORE_IDENTIFIER;
    }

    /**
     * @return null for not still
     */
    private static TypeStoreCommand typeItem(
            Stack<FullIdentifierString> preStack,
            SourceString next,
            ListIterator<SourceString> sourceIterator,
            boolean preInner) {
        if (next.getType() == SourceType.IGNORE_IDENTIFIER) {
            if (preInner) {
                throw new AnalysisTypeException(next.getPosition(), "can not be inner type");
            }
            // basic type
            preStack.push(null);// null for ignore
            return new BoundsForPlaceholderStoreCommand(next.getPosition());
        } else if (next.getType() == SourceType.KEYWORD) {
            if (preInner) {
                throw new AnalysisTypeException(next.getPosition(), "can not be inner type");
            }
            // basic type
            preStack.push(null);// null for basic
            Keyword keyword = Keyword.get(next.getValue());
            if (keyword == null || !Keywords.isNormalBasicType(keyword)) {
                // 其实可以结束了的
                // throw new AnalysisTypeException(next.getPosition(), "expected a type");
                return null;
            }
            return new BasicTypeStoreCommand(new BasicTypeString(next.getPosition(), KeywordBasicType.get(keyword)));
        } else if (next.getType() == SourceType.IDENTIFIER) {
            sourceIterator.previous();
            FullIdentifierString sourceType = skipFullIdentifier(sourceIterator);
            if (!preInner) {
                preStack.push(sourceType);
                return new SourceTypeStoreCommand(FullIdentifierString.emptyFullname(), sourceType);
            }
            if (preStack.empty()) {
                throw new AnalysisTypeException(next.getPosition(), "expect pre identifier");
            }
            FullIdentifierString pre = preStack.pop();
            if (pre == null) {
                throw new AnalysisTypeException(next.getPosition(), "no type inner");
            }
            FullIdentifierString union = FullIdentifierString.union(pre, sourceType);
            preStack.push(union);
            return new SourceTypeStoreCommand(pre, sourceType);
        } else {
            throw new CompilerException("Unknown type");
        }
    }

    private static FullIdentifierString skipFullIdentifier(ListIterator<SourceString> sourceIterator) {
        return LinkTypeFactory.skipFullIdentifier(sourceIterator);
    }


}