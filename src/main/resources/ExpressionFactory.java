package org.harvey.compiler.execute.expression;

import org.harvey.compiler.command.CompileProperties;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.ListPoint;
import org.harvey.compiler.common.collecction.RandomlyAccessAble;
import org.harvey.compiler.common.util.EncirclePair;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.local.LocalTableElementDeclare;
import org.harvey.compiler.execute.local.LocalVariableManager;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.depart.SimpleDepartedBodyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static org.harvey.compiler.execute.expression.ConstantString.ConstantType;

/**
 * 解析表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-03 19:29
 */
public class ExpressionFactory {
    /**
     * @param localVariableManager nullable
     */
    public static Expression simplyMapToExpression(
            List<SourceString> expression,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        List<SourceString> sourceList =
                expression instanceof RandomlyAccessAble ? expression : new ArrayList<>(expression);
        Expression result = new Expression();
        /*for (int i = 0; i < sourceList.size(); ) {
            try {
                ListPoint<ExpressionElement> listPoint = switchSourceType(sourceList, i, result, identifierManager,
                        localVariableManager
                );
                result.add(listPoint.getElement());
                i = listPoint.getIndex();
            } catch (CompileFileException ce) {
                throw new CompileFileException(sourceList.get(i).getPosition(), ce.getOriginMessage(), ce);
            }
        }*/
        return result;
    }

    private static ListPoint<ExpressionElement> switchSourceType(
            List<SourceString> sourceList,
            int index,
            Expression result,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        SourceString next = sourceList.get(index);
        String value = next.getValue();
        SourcePosition position = next.getPosition();
        ExpressionElement element;
        switch (next.getType()) {
            case IDENTIFIER:
                // 1. 局部变量
                if (localVariableManager != null) {
                    // 尝试使用局部变量

                    LocalTableElementDeclare localVariableUsing = localVariableManager.forUse(value, position);
                    if (localVariableUsing != null) {
                        // 是局部变量了
                        return new ListPoint<>(index + 1, localVariableUsing);
                    }
                }
                // 2. 参数
                ListPoint<FullIdentifierString> fullIdentifier = fullIdentifier(position, value, index + 1, sourceList);
                ReferenceElement reference = identifierManager.getReferenceAndAddIfNotExist(
                        fullIdentifier.getElement());
                return new ListPoint<>(fullIdentifier.getIndex(), reference);
            // 现阶段还不能分辨这个identifier是 local variable 还是 field
            case IGNORE_IDENTIFIER:
                return new ListPoint<>(index + 1, new ReferenceElement(position, ReferenceType.IGNORE, 0));
            case OPERATOR:
                return dealOperator(position, Operators.get(value), sourceList, index, result);
            case KEYWORD:
                return new ListPoint<>(index + 1, dealKeyword(Keyword.get(value), position, identifierManager));
            case STRING:
            case CHAR:
            case BOOL:
            case INT32:
            case INT64:
            case FLOAT32:
            case FLOAT64:
                return dealConstant(position, value, next.getType(), index);
            case SIGN:
                ComplexExpression complexExpression;
                if (SimpleDepartedBodyFactory.SENTENCE_END.equals(value)) {
                    throw new AnalysisExpressionException(position, "Illegal here");
                } else if (SimpleDepartedBodyFactory.BODY_START.equals(value)) {
                    complexExpression = complexExpression(sourceList, index, result);
                } else if (SimpleDepartedBodyFactory.BODY_END.equals(value)) {
                    throw new AnalysisExpressionException(position, "body end that not completed with start");
                } else if (isEndMsg(value)) {
                    if (result.isEmpty()) {
                        // body end 匹配没事, 前面检查过了
                        throw new AnalysisExpressionException(position, "body end can not be first");
                    }
                    complexExpression = bodyEndMap(value, result, index + 1);
                } else {
                    throw new AnalysisExpressionException(position, "unknown sign");
                }
                element = new ComplexExpressionElement(position, complexExpression);
                return new ListPoint<>(index + 1, element);
            case SCIENTIFIC_NOTATION_FLOAT32:
            case SCIENTIFIC_NOTATION_FLOAT64:
            case ITEM:
            case SINGLE_LINE_COMMENTS:
            case LINE_SEPARATOR:
            case MULTI_LINE_COMMENTS:
            case MIXED:
                throw new CompilerException(
                        "A SourceType " + next.getType() + ", " + next + " that doesn't fit collectionIn: too late");
        }
        throw new CompilerException("Unknown source string type: " + next.getType());
    }

    private static ListPoint<ExpressionElement> dealConstant(
            SourcePosition position, String value, SourceType type, int index) {
        switch (type) {
            case STRING: {
                ExpressionElement element = new ConstantString(
                        position, LiterallyConstantUtil.stringData(value), ConstantType.STRING);
                return new ListPoint<>(index + 1, element);
            }
            case CHAR: {
                ExpressionElement element = new ConstantString(
                        position, LiterallyConstantUtil.charData(value), ConstantType.CHAR);
                return new ListPoint<>(index + 1, element);
            }
            case BOOL: {
                ExpressionElement element = new ConstantString(position, boolToBytes(value), ConstantType.BOOL);
                return new ListPoint<>(index + 1, element);
            }
            case INT32: {
                ExpressionElement element = new ConstantString(position, numberToBytes(value), ConstantType.INT32);
                return new ListPoint<>(index + 1, element);
            }
            case INT64: {
                ExpressionElement element = new ConstantString(position, numberToBytes(value), ConstantType.INT64);
                return new ListPoint<>(index + 1, element);
            }
            case FLOAT32: {
                ExpressionElement element = new ConstantString(position, numberToBytes(value), ConstantType.FLOAT32);
                return new ListPoint<>(index + 1, element);
            }
            case FLOAT64: {
                ExpressionElement element = new ConstantString(position, numberToBytes(value), ConstantType.FLOAT64);
                return new ListPoint<>(index + 1, element);
            }
            default:
                throw new CompilerException("not constant type:" + type);
        }
    }

    private static boolean isEndMsg(String value) {
        return value != null && value.endsWith("__end__");
    }

    private static ComplexExpression bodyEndMap(String value, Expression result, int endIndex) {
        switch (value) {
            case ArrayInitExpression.END_MSG:
                ArrayInitExpression aiEnd = new ArrayInitExpression(false);
                ListPoint<ArrayInitExpression> aiStart = findComplexExpressionPre(
                        result,
                        ComplexExpressionPredicate.ARRAY_INIT
                );
                aiEnd.setOtherSide(aiStart.getIndex());
                aiStart.getElement().setOtherSide(endIndex);
                return aiEnd;
            case StructCloneExpression.END_MSG:
                StructCloneExpression scEnd = new StructCloneExpression(false);
                ListPoint<StructCloneExpression> scStart = findComplexExpressionPre(
                        result,
                        ComplexExpressionPredicate.STRUCT_CLONE
                );
                scEnd.setOtherSide(scStart.getIndex());
                scStart.getElement().setOtherSide(endIndex);
                return scEnd;
            default:
                throw new CompilerException("Unknown body end: " + value);
        }

    }


    private static <T extends ComplexExpression> ListPoint<T> findComplexExpressionPre(
            Expression result, ComplexExpressionPredicate<T> predicate) {
        if (result.isEmpty()) {
            throw new CompilerException("result can not be empty", new IndexOutOfBoundsException());
        }
        int inComplex = -1;
        for (int i = result.size() - 1; i >= 0; i--) {
            ExpressionElement element = result.get(i);
            if (!(element instanceof ComplexExpressionElement)) {
                continue;
            }
            ComplexExpression complexElement = ((ComplexExpressionElement) element).getExpression();
            T t = predicate.tryToCast(complexElement);
            if (t == null) {
                continue;
            }
            // element instance of T
            if (!predicate.isPre(t)) {
                inComplex--;
                continue;
            }
            inComplex++;
            if (inComplex == 0) {
                return new ListPoint<>(i, t);
            }
        }
        throw new AnalysisExpressionException(result.get(0).getPosition(), "Not find body encircle");
    }

    /**
     * @param bodyStart 是`{`的部分
     */
    private static ComplexExpression complexExpression(
            List<SourceString> sourceList, int bodyStart, Expression result) {
        // 可能有多种ComplexExpression情况...
        // array init/ struct clone...
        // 甚至是switch表达式...(暂无)
        // 选择以哪种形式解析表达式
        // 区别
        // 内部也是表达式分割啦
        // 其实可以直接找到对应的{}然后标注ArrayStart,ArrayEnd
        // 选出
        String endMsg;
        ComplexExpression startExpression;
        if (guessStructClone(result)) {
            endMsg = StructCloneExpression.END_MSG;
            startExpression = new StructCloneExpression(true);
        } else {
            endMsg = ArrayInitExpression.END_MSG;
            startExpression = new ArrayInitExpression(true);
        }
        int bodyEnd = moveToEndOfBody(sourceList, bodyStart).getIndex() - 1;
        SourceString originEnd = sourceList.get(bodyEnd);
        sourceList.set(bodyEnd, new SourceString(originEnd.getType(), endMsg, originEnd.getPosition()));
        return startExpression;
    }

    private static boolean guessStructClone(Expression result) {
        // 怀疑是StructClone
        // 从后往前一定是),identifier,(,1. new 2. type new
        // 3. type: >...<不做过多的检查, 直奔匹配的, 然后identifier

        int resultIndex = result.size() - 1;
        if (resultIndex - 3 < 0) {
            return false;
        }

        ExpressionElement callPost = result.get(resultIndex);
        ExpressionElement identifier = result.get(resultIndex - 1);
        ExpressionElement callPre = result.get(resultIndex - 2);
        ExpressionElement unknown = result.get(resultIndex - 3);
        if (!(callPost instanceof NormalOperatorString) ||
            ((NormalOperatorString) callPost).getValue() != Operator.CALL_POST ||
            !(identifier instanceof IdentifierString) ||
            !(callPre instanceof NormalOperatorString) ||
            ((NormalOperatorString) callPre).getValue() != Operator.CALL_PRE) {
            return false;
        }
        if (unknown instanceof KeywordString) {
            return Keyword.NEW == ((KeywordString) unknown).getKeyword();
        }
        if (!(unknown instanceof NormalOperatorString)) {
            return false;
        }
        NormalOperatorString os = (NormalOperatorString) unknown;
        if (Operator.GENERIC_LIST_POST != os.getValue()) {
            return false;
        }
        int inGeneric = 0;
        for (resultIndex = result.size() - 3; resultIndex >= 0; resultIndex--) {
            ExpressionElement element = result.get(resultIndex);
            if (element instanceof NormalOperatorString) {
                os = (NormalOperatorString) element;
                if (Operator.GENERIC_LIST_POST == os.getValue()) {
                    inGeneric++;
                } else if (Operator.GENERIC_LIST_PRE == os.getValue()) {
                    inGeneric--;
                }
            }
            if (inGeneric != 0) {
                continue;
            }
            if (resultIndex - 1 < 0) {
                return false;
            }
            element = result.get(resultIndex - 1);
            if (!(element instanceof KeywordString)) {
                return false;
            }
            return Keyword.NEW == ((KeywordString) element).getKeyword();
        }
        return false;
    }

    private static byte[] boolToBytes(String value) {
        byte[] bytes = new byte[1];
        boolean isTrue = Keyword.TRUE.equals(value);
        boolean isFalse = Keyword.FALSE.equals(value);
        if (isTrue == isFalse) {
            throw new CompilerException(value + " is not a bool");
        }
        bytes[0] = (byte) (isTrue ? 0 : 1);
        return bytes;
    }

    private static byte[] numberToBytes(String value) {
        return value.getBytes(CompileProperties.NUMBER_CHARSET);
    }


    private static ListPoint<ExpressionElement> dealOperator(
            SourcePosition position,
            Operator[] operators,
            List<SourceString> src,
            int index,
            Expression expressionResult) {
        if (operators.length == 0) {
            throw new CompilerException();
        } else if (operators.length != 1) {
            return new ListPoint<>(index + 1, new NormalOperatorString(
                    position,
                    decideCorrectOperator(operators, position, src, index, expressionResult)
            ));
        }
        Operator operator = operators[0];
        if (operator == Operator.LAMBDA) {
            ListPoint<LambdaExpression> listPoint = lambdaExpressionCatcher(src, index, expressionResult);
            return new ListPoint<>(
                    listPoint.getIndex(),
                    new ComplexExpressionElement(position, listPoint.getElement())
            );
        } else if (operator == Operator.GET_MEMBER && index + 1 < src.size()) {
            // 可能是重载operator的情况
            expressionResult.add(new NormalOperatorString(position, Operator.GET_MEMBER));
            SourceString next = src.get(index + 1);
            if (next.getType() == SourceType.OPERATOR) {
                // 是重载运算符
                return callReloadOperator(index + 1, src, expressionResult);
            } else if (next.getType() == SourceType.IDENTIFIER) {
                // 是获取成员
                return getMember(index + 1, next);
            } else if (next.getType() == SourceType.KEYWORD) {
                // 是获取Keyword信息
                return getKeywordMember(index, next);
            } else {
                throw new AnalysisExpressionException(next.getPosition(), "expect identifier");
            }
        }
        return new ListPoint<>(index + 1, new NormalOperatorString(position, operator));
    }


    /**
     * get member 过程获取到了一个关键字的情况, 对这个关键字进行处理
     * TODO
     * <pre>{@code
     * class Outer {
     *     int value;
     *     class Inner {
     *         int value;
     *         public void fun() {
     *             System.out.println(this.value);
     *             System.out.println(Inner.this.value);
     *             System.out.println(Outer.this.value);
     *         }
     *     }
     * }
     * }</pre>
     */
    private static ListPoint<ExpressionElement> getKeywordMember(int indexOfKeywordMember, SourceString keywordSource) {
        Keyword keyword = Keyword.get(keywordSource.getValue());
        switch (keyword) {
            case THIS:
                // 这个this是什么情况呢?
                break;
            // case TYPE:
            // Type<int> a = new Type<>(); 能根据int获取type信息的
            // or meta or class
            // 获取类型字节码信息么... 好吧
            // break;
            default:
                throw new AnalysisExpressionException(keywordSource.getPosition(), "Unexpected value: " + keyword);
        }
        return new ListPoint<>(indexOfKeywordMember + 1, new KeywordString(keywordSource.getPosition(), keyword));
    }

    private static ListPoint<ExpressionElement> getMember(int indexOfMember, SourceString member) {
        return new ListPoint<>(indexOfMember + 1, new IdentifierString(member));
    }

    private static ListPoint<ExpressionElement> callReloadOperator(
            int indexOfOperator, List<SourceString> src, Expression expressionResult) {
        StringBuilder operSb = new StringBuilder();
        SourceString first = src.get(indexOfOperator);
        SourcePosition start = first.getPosition();
        if (Operator.CALL_PRE.nameEquals(first.getValue())) {
            indexOfOperator++;
            if (indexOfOperator >= src.size() || !Operator.CALL_POST.nameEquals(src.get(indexOfOperator).getValue())) {
                throw new AnalysisExpressionException(start, "expected)");
            }
            expressionResult.add(new NormalOperatorString(start, Operator.CALLABLE_DECLARE));
            indexOfOperator++;
            if (indexOfOperator >= src.size() || !Operator.CALL_PRE.nameEquals(src.get(indexOfOperator).getValue())) {
                throw new AnalysisExpressionException(start, "expected(");
            }
            return new ListPoint<>(indexOfOperator + 1, new NormalOperatorString(start, Operator.CALL_PRE));
        }

        SourcePosition end = start;
        for (; indexOfOperator < src.size(); indexOfOperator++) {
            SourceString operStr = src.get(indexOfOperator);
            if (operStr.getType() != SourceType.OPERATOR) {
                throw new AnalysisExpressionException(operStr.getPosition(), "expected a operator");
            }
            end = operStr.getPosition();
            if (!Operator.CALL_PRE.nameEquals(operStr.getValue())) {
                operSb.append(operStr.getValue());
                continue;
            }
            Operator[] operators = Operators.get(operSb.toString());
            if (operators.length == 0) {
                throw new AnalysisExpressionException(start, end, "Unknown operator");
            }
            expressionResult.add(new NormalOperatorString(start, operators[0]));
            return new ListPoint<>(indexOfOperator + 1, new NormalOperatorString(end, Operator.CALL_PRE));
        }
        throw new AnalysisExpressionException(end, "expected (");
    }

    private static ExpressionElement dealKeyword(
            Keyword keyword, SourcePosition position, IdentifierManager identifierManager) {
        // TODO 表达式中可以出现哪些Keyword呢?
        if (Keywords.isOperator(keyword)) {
            // 应该不会进入, 因为之前已经处理好了
            return new NormalOperatorString(position, Operator.fromKeyword(keyword));
        } else if (keyword == Keyword.THIS ||
                   keyword == Keyword.SUPER ||
                   keyword == Keyword.NEW ||
                   keyword == Keyword.FILE) {
            return new KeywordString(position, keyword);
        } else if (Keywords.isBasicType(keyword) && Keyword.VOID != keyword) {
            return new KeywordString(position, keyword);
        } else {
            throw new AnalysisExpressionException(position, "Illegal here");
        }
    }

    /**
     * @param index 此时index是->
     */
    private static ListPoint<LambdaExpression> lambdaExpressionCatcher(
            List<SourceString> src, int index, Expression expression) {
        // 被多算入expression的, 应该退回
        if (index < 0) {
            throw new CompilerException("Illegal timing of the call");
        }
        SourceString lambdaSign = src.get(index);
        if (index == 0) {
            throw new AnalysisExpressionException(lambdaSign.getPosition(), "expected argument list");
        }
        // 1. 参数列表
        List<SourceString> arguments = getLambdaArgument(src, index, expression);
        // 2. 可执行程序
        //  -> out of bound 报错
        if (index >= src.size() - 1) {
            // 是最后一个, 就是没用了
            throw new AnalysisExpressionException(lambdaSign.getPosition(), "expected executable body");
        }
        ListPoint<SourceTextContext> listPoint = getLambdaExecutable(src, index);
        return new ListPoint<>(
                listPoint.getIndex(),
                new LambdaExpression(arguments.toArray(new SourceString[]{}), listPoint.getElement())
        );
    }

    /**
     * 2. 可执行程序
     * -> out of bound 报错
     * -> { 一直到 }
     * -> 其他-> TODO LambdaExpression 可再生子类, 起一可直接为表达式, 其二为可执行函数
     * 要不直接为null, 后面的也一并作为表达式解析,
     * 然后, 下一轮进一步解析的时候, 如果是这种情况, 需要根据上下文分辨
     * 例如到end了..之类
     *
     * @param index 指向`->`
     */
    private static ListPoint<SourceTextContext> getLambdaExecutable(List<SourceString> src, int index) {
        SourceString next = src.get(index + 1);
        if (next.getType() != SourceType.SIGN) {
            // index 指向 -> 之后
            return new ListPoint<>(index + 1, null);
        }
        if (SimpleDepartedBodyFactory.SENTENCE_END.equals(next.getValue())) {
            throw new CompilerException("`;` is Illegal here: too late");
        } else if (SimpleDepartedBodyFactory.BODY_END.equals(next.getValue())) {
            throw new AnalysisExpressionException(
                    next.getPosition(),
                    "expected " + SimpleDepartedBodyFactory.BODY_START
            );
        }
        return moveToEndOfBody(src, index + 1);
    }

    /**
     * @return index 指向 } 之后一个,element不含{和}
     */
    private static ListPoint<SourceTextContext> moveToEndOfBody(List<SourceString> src, int startOfBody) {
        // 从index+1, 到}
        int inBody = 1;
        SourceTextContext body = new SourceTextContext();
        for (int i = startOfBody + 1; i < src.size(); i++) {
            SourceString element = src.get(i);
            if (element.getType() != SourceType.SIGN ||
                SimpleDepartedBodyFactory.SENTENCE_END.equals(element.getValue())) {
                body.add(element);
            }
            if (SimpleDepartedBodyFactory.BODY_START.equals(element.getValue())) {
                inBody++;
            } else if (SimpleDepartedBodyFactory.BODY_END.equals(element.getValue())) {
                inBody--;
            }
            if (inBody == 0) {
                // index 指向 } 之后
                return new ListPoint<>(i + 1, body);
            } else if (inBody < 0) {
                throw new AnalysisExpressionException(element.getPosition(), "Fifth wheel `}`");
            }
        }
        throw new AnalysisExpressionException(src.get(src.size() - 1).getPosition(), "expected }");
    }

    private static List<SourceString> getLambdaArgument(List<SourceString> src, int index, Expression expression) {
        SourceString previous = src.get(index - 1);
        if (previous.getType() == SourceType.IDENTIFIER) {
            // 去除最后一个, 就是previous
            expression.remove(expression.size() - 1);
            // 单独一个参数列表可以省略括号
            return List.of(previous);
        } else if (previous.getType() != SourceType.OPERATOR ||
                   !Operator.PARENTHESES_POST.nameEquals(previous.getValue())) {
            throw new AnalysisExpressionException(previous.getPosition(), "expected )");
        }
        List<SourceString> arguments = new ArrayList<>();
        //  (a,b)->{};(a,b)->func(a,b)
        // 一个或多个或0个参数
        // 往前找, 到为止(
        // 期间只能有identifier和, 且一个一个间隔
        expression.remove(expression.size() - 1);
        int p = index - 1; // )
        SourceString identifier = null;
        while (true) {
            SourceString ss = src.get(p);
            p--;
            String value = ss.getValue();
            SourceType type = ss.getType();
            // 删去最后一个
            expression.remove(expression.size() - 1);
            if (type == SourceType.IDENTIFIER && identifier == null) {
                identifier = ss;
                continue;
            }
            if (type != SourceType.OPERATOR) {
                throw new AnalysisExpressionException(
                        ss.getPosition(),
                        "excepted an " + (identifier == null ? "identifier as an argument" : ",")
                );
            }
            if (Operator.PARENTHESES_PRE.nameEquals(value)) {
                // identifier == null && arguments.isEmpty()-> ()
                // identifier != null && !arguments.isEmpty() -> 匹配正确
                // else -> 匹配错误
                // identifier == null && !arguments.isEmpty()->excepted identifier
                // identifier != null && arguments.isEmpty()->匹配正确
                if (identifier == null && !arguments.isEmpty()) {
                    throw new AnalysisExpressionException(ss.getPosition(), "excepted an identifier as an argument");
                }
                if (identifier != null) {
                    arguments.add(identifier);
                }
                break;
            }
            // 还不是pre
            if (!Operator.COMMA.nameEquals(value)) {
                throw new AnalysisExpressionException(ss.getPosition(), "excepted a `,` here");
            }
            if (identifier == null) {
                throw new AnalysisExpressionException(ss.getPosition(), "excepted an identifier as an argument");
            }
            arguments.add(identifier);
            identifier = null;
        }

        return arguments;
    }

    /**
     * @param index 此时, 其指向operator的源码
     */
    private static Operator decideCorrectOperator(
            Operator[] operators, SourcePosition position, List<SourceString> src, int index, Expression result) {
        if (operators.length != 2) {
            throw new CompilerException("Distinguishing between more than two operators is not supported");
        }
        SourceString sourceString = src.get(index);
        switch (sourceString.getValue()) {
            case "[": // 泛型[ or list]
                // 前面不是identifier, 报错
                ExpressionElement last = result.get(result.size() - 1);
                if (last instanceof IdentifierString || last instanceof ReferenceElement) {

                }
                return Operator.ARRAY_AT_PRE;
            case "]": // 泛型']' or list']'
                return Operator.ARRAY_AT_POST;
            case "(": // 括号 or 函数调用
                if (result.isEmpty()) {
                    throw new AnalysisExpressionException(position, "body end can not be first");
                }
                return distinguishPreCall(result) ? Operator.CALL_PRE : Operator.PARENTHESES_PRE;
            case ")": // 括号 or 函数调用
                return distinguishPostCall(result, sourceString.getPosition()) ? Operator.CALL_POST :
                        Operator.PARENTHESES_POST;
            case "+": // 加 or 正
                return distinguishPlus(src, index);
            case "-": // 减 or 负
                return distinguishMinus(src, index);
            case "++": // 前 or 后
                return distinguishIncrease(src, index);
            case "--": // 前 or 后
                return distinguishDecrease(src, index);
            default:
                throw new CompilerException("Unknown operator");
        }
    }


    private static boolean distinguishLarger(ArrayList<SourceString> src, int index, Expression result) {
        // 对于 >
        // 是Generic? 看后面
        //      没用了->是
        //      下一个是operator->是
        //      下一个是identifier->不是
        //      下一个是常数->不是
        //      下一个是->不是
        // 是Generic, 修改前面的<, 第一个<(还依然是Lesser的状态)就是, 其他不要管
        if (result.isEmpty()) {
            throw new CompilerException("result can not be empty", new IndexOutOfBoundsException());
        }
        if (index + 1 >= src.size()) {
            return false;
        }
        SourceString next = src.get(index + 1);
        if (next.getType() != SourceType.OPERATOR) {
            return true;
        }
        String nextValue = next.getValue();
        return !Operator.GENERIC_LIST_PRE.nameEquals(nextValue) &&
               !Operator.CALL_PRE.nameEquals(nextValue) &&
               !Operator.ARRAY_AT_PRE.nameEquals(nextValue) &&
               !Operator.ARRAY_DECLARE.nameEquals(nextValue);
    }

    private static void resetGenericListPre(Expression result) {
        for (int i = result.size() - 1; i >= 0; i--) {
            ExpressionElement element = result.get(i);
            if (!(element instanceof NormalOperatorString)) {
                continue;
            }
            NormalOperatorString os = (NormalOperatorString) element;
            Operator value = os.getValue();
            if (value == Operator.LESS) {
                result.set(i, new NormalOperatorString(os.getPosition(), Operator.GENERIC_LIST_PRE));
                return;
            }
        }
        throw new AnalysisExpressionException(result.get(0).getPosition(), "expected a " + Operator.LESS.getName());
    }

    private static boolean distinguishPreCall(Expression result) {
        // 是单元运算符?Call:)
        // 是Call? 看前面
        //      没有了->不是
        //      前一个是operator->不是
        //      前一个是identifier->是
        //      前一个是GenericPre->是
        //      前一个是常数->不是
        //      前一个是不知道->不是
        ExpressionElement pre = result.get(result.size() - 1);
        if (pre instanceof IdentifierString  || pre instanceof ReferenceElement) {
            return true;
        }
        if (pre instanceof KeywordString && Keywords.callable(((KeywordString) pre).getKeyword())) {
            return true;
        }
        if (pre instanceof ComplexExpressionElement) {
            return true;
        }

        // 如果是对重载运算符的函数调用呢? 如何解决? 答案是在前面发现如果是重载运算符的函数调用, 就已经决定了Operator
        // 一个类里重载了+运算符
        // int a = 12 + +(55); ???? what? 不行, 大大的不行
        // int a = 12 + this.+(55); 行吧
        if (!(pre instanceof NormalOperatorString)) {
            return false;
        }
        Operator preOperator = ((NormalOperatorString) pre).getValue();
        return preOperator == Operator.GENERIC_LIST_POST ||
               preOperator == Operator.CALL_POST ||
               preOperator == Operator.ARRAY_AT_POST ||
               preOperator == Operator.CALLABLE_DECLARE ||
               preOperator == Operator.ARRAY_DECLARE;
    }

    private static boolean distinguishPostCall(Expression result, SourcePosition position) {
        // 依据前面的内容是call来决定这个是什么
        // 用匹配找
        // ()
        int inTuple = 0;
        int inCall = 0;
        for (int i = result.size() - 1; i >= 0; i--) {
            ExpressionElement element = result.get(i);
            if (!(element instanceof NormalOperatorString)) {
                continue;
            }
            NormalOperatorString os = (NormalOperatorString) element;
            Operator value = os.getValue();
            if (value == Operator.PARENTHESES_PRE) {
                inTuple++;
                if (inTuple == 1) {
                    if (inCall != 0) {
                        break;
                    }
                    return false;
                }
            } else if (value == Operator.PARENTHESES_POST) {
                inTuple--;
            } else if (value == Operator.CALL_POST) {
                inCall--;
            } else if (value == Operator.CALL_PRE) {
                inCall++;
                if (inCall == 1) {
                    if (inTuple != 0) {
                        break;
                    }
                    return true;
                }
            }
        }
        throw new AnalysisExpressionException(position, "can not find pre");
    }


    private static Operator distinguishDecrease(List<SourceString> src, int index) {
        return distinguishLeftRight(src, index, Operator.LEFT_DECREASING, Operator.RIGHT_DECREASING);
    }

    private static Operator distinguishIncrease(List<SourceString> src, int index) {
        return distinguishLeftRight(src, index, Operator.LEFT_INCREASING, Operator.RIGHT_INCREASING);
    }

    private static Operator distinguishLeftRight(
            List<SourceString> src, int index, Operator onLeft, Operator onRight) {
        EncirclePair<SourceString> pair = CollectionUtil.getEncirclePair(src, index);
        if (pair.bothNull()) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        }
        SourceString pre = pair.getPre();
        SourceString post = pair.getPost();
        if (pre == null) {
            if (post.getType() == SourceType.IDENTIFIER) {
                // ++x
                return onLeft;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a variable");
            }
        } else if (post == null) {
            if (pre.getType() == SourceType.IDENTIFIER) {
                // x++
                return onRight;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a variable");
            }
        } else {
            if (pre.getType() == SourceType.IDENTIFIER && post.getType() == SourceType.OPERATOR) {
                // x++ + 2
                return onRight;
            } else if (pre.getType() == SourceType.OPERATOR && post.getType() == SourceType.IDENTIFIER) {
                // 2 + ++x
                return onLeft;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a only variable");
            }
        }
    }

    private static Operator distinguishMinus(List<SourceString> src, int index) {
        return distinguishSign(src, index, Operator.NEGATIVE, Operator.SUBTRACT);
    }

    private static Operator distinguishPlus(List<SourceString> src, int index) {
        return distinguishSign(src, index, Operator.POSITIVE, Operator.ADD);
    }

    private static Operator distinguishSign(List<SourceString> src, int index, Operator sign, Operator operator) {
        EncirclePair<SourceString> pair = CollectionUtil.getEncirclePair(src, index);
        if (pair.bothNull()) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        }
        SourceString pre = pair.getPre();
        SourceString post = pair.getPost();
        if (pre == null) {
            return sign;
        } else if (post == null) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        } else {
            if (pre.getType() == SourceType.OPERATOR &&
                !Operator.PARENTHESES_POST.nameEquals(pre.getValue()) &&
                !Operator.ARRAY_AT_POST.nameEquals(pre.getValue()) &&
                !Operator.ARRAY_DECLARE.nameEquals(pre.getValue()) &&
                !Operator.CALLABLE_DECLARE.nameEquals(pre.getValue())) {
                return sign;
            } else if (pre.getType() == SourceType.SIGN &&
                       (SimpleDepartedBodyFactory.BODY_START.equals(pre.getValue()) ||
                        SimpleDepartedBodyFactory.SENTENCE_END.equals(pre.getValue()))) {
                return sign;
            } else {
                return operator;
            }
        }
    }

    public static ListPoint<FullIdentifierString> fullIdentifier(
            SourcePosition position, String firstName, int indexOfDot, List<SourceString> src) {
        ListPoint<List<SourceString>> listListPoint = DeclarableFactory.departFullIdentifier(
                position, firstName, indexOfDot, src);
        return new ListPoint<>(listListPoint.getIndex(), fullIdentifier(listListPoint.getElement()));
    }

    /**
     * @param iterator previous is identifier
     */
    public static FullIdentifierString fullIdentifier(
            SourcePosition position, String firstName, ListIterator<SourceString> iterator) {
        List<SourceString> fullIdentifierString = DeclarableFactory.departFullIdentifier(position, firstName, iterator);
        return fullIdentifier(fullIdentifierString);
    }

    private static FullIdentifierString fullIdentifier(List<SourceString> fullIdentifierString) {
        String[] fullname = fullIdentifierString.stream().map(SourceString::getValue).toArray(String[]::new);
        SourcePosition[] positions = fullIdentifierString.stream()
                .map(SourceString::getPosition)
                .toArray(SourcePosition[]::new);
        return new FullIdentifierString(positions, fullname);
    }
}
