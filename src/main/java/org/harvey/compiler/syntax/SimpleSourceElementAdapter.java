package org.harvey.compiler.syntax;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.ConstantString;
import org.harvey.compiler.execute.expression.IExpressionElement;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.raw.KeywordBasicType;

import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:27
 */
public class SimpleSourceElementAdapter {

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
                return new ReloadOperatorString(position, Operator.CALLABLE_DECLARE);
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

    /**
     * TODO 暂时只有 on expression, 还没有别的
     *
     * @param onExpression true for on expression and false for on control
     * @return 不联系上下文的初步转换
     */
    public IExpressionElement toElement(
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
                // TODO
                return new IdentifierString(position);
            case IDENTIFIER:
                // item的Identifier
                // TODO
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
            CastOperator castOperator = new CastOperator(new BasicTypeString(position, KeywordBasicType.get(keyword)));
            if (preGetMember) {
                return new ReloadOperatorString(castOperator);
            }
            return castOperator;
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
