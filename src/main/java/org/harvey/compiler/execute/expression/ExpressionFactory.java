package org.harvey.compiler.execute.expression;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.common.util.EncirclePair;
import org.harvey.compiler.common.util.ListPoint;
import org.harvey.compiler.depart.SimpleDepartedBodyFactory;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

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

    public static Expression genericMessage(ListIterator<SourceString> iterator) {
        return genericMessage(iterator, new Expression());
    }

    private static Expression genericMessage(ListIterator<SourceString> iterator, Expression expression) {
        if (!iterator.hasNext()) {
            throw new CompilerException("genericMessage can not be empty");
        }
        SourceString first = iterator.next();
        if (!iterator.hasNext()) {
            throw new AnalysisExpressionException(first.getPosition(), "not a generic message");
        }
        if (first.getType() != SourceStringType.OPERATOR || !Operator.GENERIC_LIST_PRE.nameEquals(first.getValue())) {
            throw new AnalysisExpressionException(first.getPosition(), "not a generic message");
        }
        int inGeneric = 1;
        SourcePosition position = first.getPosition();
        while (iterator.hasNext()) {
            SourceString string = iterator.next();
            SourceStringType sourceType = string.getType();
            position = string.getPosition();
            String value = string.getValue();
            if (sourceType == SourceStringType.IDENTIFIER) {
                expression.add(new IdentifierString(position, string.getValue()));
                continue;
            } else if (sourceType == SourceStringType.KEYWORD && Keywords.isBasicType(value)) {
                expression.add(new KeywordString(position, Keyword.get(value)));
                continue;
            } else if (sourceType == SourceStringType.IGNORE_IDENTIFIER) {
                expression.add(new IgnoreIdentifierString(position));
                continue;
            } else if (sourceType != SourceStringType.OPERATOR) {
                throw new AnalysisExpressionException(position, "not allowed here");
            }
            ExpressionElement last = expression.get(expression.size() - 1);
            if (!(last instanceof IdentifierString) && !(last instanceof IgnoreIdentifierString)) {
                throw new AnalysisExpressionException(position, "expected a identifier");
            }
            Operator operator;
            if (Operator.GENERIC_LIST_PRE.nameEquals(value)) {
                operator = Operator.GENERIC_LIST_PRE;
                inGeneric++;
            } else if (Operator.GENERIC_LIST_POST.nameEquals(value)) {
                operator = Operator.GENERIC_LIST_POST;
                inGeneric--;
            } else if (Operator.COMMA.nameEquals(value)) {
                operator = Operator.COMMA;
            } else {
                throw new AnalysisExpressionException(position, "Illegal operator");
            }
            expression.add(new OperatorString(position, operator));
            if (inGeneric == 0) {
                return expression;
            }
            if (inGeneric < 0) {
                throw new AnalysisExpressionException(position, "Illegal generic match");
            }
        }
        throw new AnalysisExpressionException(position, "not a generic message");
    }

    public static Expression type(SourceTextContext type) {
        return type(type.listIterator());
    }

    public static Expression type(ListIterator<SourceString> iterator) {
        if (!iterator.hasNext()) {
            throw new CompilerException("type can not be empty");
        }
        SourceString identifier = iterator.next();
        Expression expression = new Expression();

        Keyword mayKeyword = Keyword.get(identifier.getValue());
        if (identifier.getType() == SourceStringType.IDENTIFIER) {
            expression.add(new IdentifierString(identifier));
        } else if (identifier.getType() == SourceStringType.KEYWORD && Keywords.isBasicType(mayKeyword)) {
            if (iterator.hasNext()) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "not basic type");
            }
            expression.add(new KeywordString(identifier.getPosition(), mayKeyword));
            return expression;
        } else {
            throw new AnalysisExpressionException(identifier.getPosition(), "not a type");
        }
        return iterator.hasNext() ? genericMessage(iterator, expression) : expression;
    }

    public static Expression depart(SourceTextContext expression) {
        ArrayList<SourceString> sourceList = new ArrayList<>(expression);
        Expression result = new Expression();
        for (int i = 0; i < sourceList.size(); i++) {
            try {
                ListPoint<ExpressionElement> listPoint = switchSourceType(sourceList, i, result, result.size());
                result.add(listPoint.getElement());
                i = listPoint.getIndex();
            } catch (CompileException ce) {
                throw new CompileException(sourceList.get(i).getPosition(), ce.getOriginMessage(), ce);
            }
        }
        return result;
    }

    private static ListPoint<ExpressionElement> switchSourceType(ArrayList<SourceString> sourceList, int index,
                                                                 Expression result, int putToIndex) {
        SourceString next = sourceList.get(index);
        String value = next.getValue();
        SourcePosition position = next.getPosition();
        ExpressionElement element;
        switch (next.getType()) {
            case IDENTIFIER:
                return new ListPoint<>(index + 1, new IdentifierString(position, next.getValue()));
            case IGNORE_IDENTIFIER:
                return new ListPoint<>(index + 1, new IgnoreIdentifierString(position));
            case OPERATOR:
                return dealOperator(position, Operators.get(value), sourceList, index, result);
            case KEYWORD:
                // TODO 表达式中可以出现哪些Keyword呢?
                return new ListPoint<>(index + 1, dealKeyword(Keyword.get(value), position));
            case STRING:
                element = new ConstantString(position, LiterallyConstantUtil.stringData(value), ConstantType.STRING);
                return new ListPoint<>(index + 1, element);
            case CHAR:
                element = new ConstantString(position, LiterallyConstantUtil.charData(value), ConstantType.CHAR);
                return new ListPoint<>(index + 1, element);
            case BOOL:
                element = new ConstantString(position, boolToBytes(value), ConstantType.BOOL);
                return new ListPoint<>(index + 1, element);
            case INT32:
                element = new ConstantString(position, numberToBytes(value), ConstantType.INT32);
                return new ListPoint<>(index + 1, element);
            case INT64:
                element = new ConstantString(position, numberToBytes(value), ConstantType.INT64);
                return new ListPoint<>(index + 1, element);
            case FLOAT32:
                element = new ConstantString(position, numberToBytes(value), ConstantType.FLOAT32);
                return new ListPoint<>(index + 1, element);
            case FLOAT64:
                element = new ConstantString(position, numberToBytes(value), ConstantType.FLOAT64);
                return new ListPoint<>(index + 1, element);
            case SIGN:
                ComplexExpression complexExpression;
                if (SimpleDepartedBodyFactory.SENTENCE_END.equals(value)) {
                    throw new AnalysisExpressionException(position, "Illegal here");
                } else if (SimpleDepartedBodyFactory.BODY_START.equals(value)) {
                    complexExpression = complexExpression(sourceList, index, result);
                } else if (SimpleDepartedBodyFactory.BODY_END.equals(value)) {
                    // body end 匹配没事, 前面检查过了
                    if (result.isEmpty()) {
                        throw new AnalysisExpressionException(position, "body end can not be first");
                    }
                    complexExpression = bodyEndMap(value, result, putToIndex);
                } else {
                    throw new AnalysisExpressionException(position, "unknown sign");
                }
                element = new ComplexExpressionElement(position, complexExpression);
                return new ListPoint<>(index + 1, element);
            case GOTO:
            case LABEL:
            case ASSIGN_TEMP:
                throw new CompilerException(
                        "A SourceStringType " + next.getType() + ", " + next + " that doesn't fit in: too early");
            case SCIENTIFIC_NOTATION_FLOAT32:
            case SCIENTIFIC_NOTATION_FLOAT64:
            case ITEM:
            case SINGLE_LINE_COMMENTS:
            case LINE_SEPARATOR:
            case MULTI_LINE_COMMENTS:
            case MIXED:
                throw new CompilerException(
                        "A SourceStringType " + next.getType() + ", " + next + " that doesn't fit in: too late");
        }
        throw new CompilerException("Unknown source string type: " + next.getType());
    }

    private static ComplexExpression bodyEndMap(String value, Expression result, int endIndex) {

        switch (value) {
            case ArrayInitExpression.END_MSG:
                ArrayInitExpression aiEnd = new ArrayInitExpression(false);
                ListPoint<ArrayInitExpression> aiStart = findComplexExpressionPre(result,
                        ComplexExpressionPredicate.ARRAY_INIT);
                aiEnd.setOtherSide(aiStart.getIndex());
                aiStart.getElement().setOtherSide(endIndex);
                return aiEnd;
            case StructCloneExpression.END_MSG:
                StructCloneExpression scEnd = new StructCloneExpression(false);
                ListPoint<StructCloneExpression> scStart = findComplexExpressionPre(result,
                        ComplexExpressionPredicate.STRUCT_CLONE);
                scEnd.setOtherSide(scStart.getIndex());
                scStart.getElement().setOtherSide(endIndex);
                return scEnd;
            default:
                throw new CompilerException("Unknown body end: " + value);
        }

    }


    private static <T extends ComplexExpression> ListPoint<T> findComplexExpressionPre(Expression result,
                                                                                       ComplexExpressionPredicate<T> predicate) {
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
    private static ComplexExpression complexExpression(ArrayList<SourceString> sourceList, int bodyStart,
                                                       Expression result) {
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
        if (!(callPost instanceof OperatorString) || ((OperatorString) callPost).getValue() != Operator.CALL_POST ||
                !(identifier instanceof IdentifierString) || !(callPre instanceof OperatorString) ||
                ((OperatorString) callPre).getValue() != Operator.CALL_PRE) {
            return false;
        }
        if (unknown instanceof KeywordString) {
            return Keyword.NEW == ((KeywordString) unknown).getKeyword();
        }
        if (!(unknown instanceof OperatorString)) {
            return false;
        }
        OperatorString os = (OperatorString) unknown;
        if (Operator.GENERIC_LIST_POST != os.getValue()) {
            return false;
        }
        int inGeneric = 0;
        for (resultIndex = result.size() - 3; resultIndex >= 0; resultIndex--) {
            ExpressionElement element = result.get(resultIndex);
            if (element instanceof OperatorString) {
                os = (OperatorString) element;
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


    private static ListPoint<ExpressionElement> dealOperator(SourcePosition position, Operator[] operators,
                                                             ArrayList<SourceString> src, int index,
                                                             Expression expressionResult) {
        if (operators.length == 0) {
            throw new CompilerException();
        } else if (operators.length != 1) {
            return new ListPoint<>(index + 1, new OperatorString(position,
                    decideCorrectOperator(operators, position, src, index, expressionResult)));
        }
        Operator operator = operators[0];
        if (operator == Operator.LAMBDA) {
            ListPoint<LambdaExpression> listPoint = lambdaExpressionCatcher(src, index, expressionResult);
            return new ListPoint<>(listPoint.getIndex(),
                    new ComplexExpressionElement(position, listPoint.getElement()));
        } else {
            return new ListPoint<>(index + 1, new OperatorString(position, operator));
        }
    }

    private static ExpressionElement dealKeyword(Keyword keyword, SourcePosition position) {
        if (Keywords.isOperator(keyword)) {
            // 应该不会进入, 因为之前已经处理好了
            return new OperatorString(position, Operator.fromKeyword(keyword));
        } else if (keyword == Keyword.THIS || keyword == Keyword.SUPER || keyword == Keyword.NEW) {
            return new KeywordString(position, keyword);
        } else {
            throw new AnalysisExpressionException(position, "Illegal here");
        }
    }

    /**
     * @param index 此时index是->
     */
    private static ListPoint<LambdaExpression> lambdaExpressionCatcher(ArrayList<SourceString> src, int index,
                                                                       Expression expression) {
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
        return new ListPoint<>(listPoint.getIndex(),
                new LambdaExpression(arguments.toArray(new SourceString[]{}), listPoint.getElement()));
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
    private static ListPoint<SourceTextContext> getLambdaExecutable(ArrayList<SourceString> src, int index) {
        SourceString next = src.get(index + 1);
        if (next.getType() != SourceStringType.SIGN) {
            // index 指向 -> 之后
            return new ListPoint<>(index + 1, null);
        }
        if (SimpleDepartedBodyFactory.SENTENCE_END.equals(next.getValue())) {
            throw new CompilerException("`;` is Illegal here: too late");
        } else if (SimpleDepartedBodyFactory.BODY_END.equals(next.getValue())) {
            throw new AnalysisExpressionException(next.getPosition(),
                    "expected " + SimpleDepartedBodyFactory.BODY_START);
        }
        return moveToEndOfBody(src, index + 1);
    }

    /**
     * @return index 指向 } 之后一个,element不含{和}
     */
    private static ListPoint<SourceTextContext> moveToEndOfBody(ArrayList<SourceString> src, int startOfBody) {
        // 从index+1, 到}
        int inBody = 1;
        SourceTextContext body = new SourceTextContext();
        for (int i = startOfBody + 1; i < src.size(); i++) {
            SourceString element = src.get(i);
            if (element.getType() != SourceStringType.SIGN ||
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

    private static List<SourceString> getLambdaArgument(ArrayList<SourceString> src, int index, Expression expression) {
        SourceString previous = src.get(index - 1);
        if (previous.getType() == SourceStringType.IDENTIFIER) {
            // 去除最后一个, 就是previous
            expression.remove(expression.size() - 1);
            // 单独一个参数列表可以省略括号
            return List.of(previous);
        } else if (previous.getType() != SourceStringType.OPERATOR ||
                !Operator.BRACKET_POST.nameEquals(previous.getValue())) {
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
            SourceStringType type = ss.getType();
            // 删去最后一个
            expression.remove(expression.size() - 1);
            if (type == SourceStringType.IDENTIFIER && identifier == null) {
                identifier = ss;
                continue;
            }
            if (type != SourceStringType.OPERATOR) {
                throw new AnalysisExpressionException(ss.getPosition(),
                        "excepted an " + (identifier == null ? "identifier as an argument" : ","));
            }
            if (Operator.BRACKET_PRE.nameEquals(value)) {
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
     * @param index    此时, 其指向operator的源码
     */
    private static Operator decideCorrectOperator(Operator[] operators, SourcePosition position,
                                                  ArrayList<SourceString> src, int index, Expression result) {
        if (operators.length != 2) {
            throw new CompilerException("Distinguishing between more than two operators is not supported");
        }
        SourceString sourceString = src.get(index);
        switch (sourceString.getValue()) {
            case "<": // 泛型< or 大于号
                return Operator.LESS;
            case ">": // 泛型> or 小于号
                return distinguishLarger(src, index, result);
            case "(": // 括号 or 函数调用
                if (result.isEmpty()) {
                    throw new AnalysisExpressionException(position, "body end can not be first");
                }
                return distinguishPreCall(result, sourceString);
            case ")": // 括号 or 函数调用
                return distinguishPostCall(result, sourceString);
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


    private static Operator distinguishLarger(ArrayList<SourceString> src, int index, Expression result) {
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
        Operator operator;
        if (index + 1 >= src.size()) {
            operator = Operator.GENERIC_LIST_POST;
        } else {
            operator = src.get(index + 1).getType() == SourceStringType.OPERATOR ? Operator.GENERIC_LIST_POST :
                    Operator.LARGER;
        }
        if (operator != Operator.LARGER) {
            return operator;
        }
        for (int i = result.size() - 1; i >= 0; i--) {
            ExpressionElement element = result.get(i);
            if (!(element instanceof OperatorString)) {
                continue;
            }
            OperatorString os = (OperatorString) element;
            Operator value = os.getValue();
            if (value == Operator.LESS) {
                result.set(i, new OperatorString(os.getPosition(), Operator.GENERIC_LIST_PRE));
                return operator;
            }
        }
        throw new AnalysisExpressionException(result.get(0).getPosition(), "expected a " + Operator.LESS.getName());
    }

    private static Operator distinguishPreCall(Expression result, SourceString sourceString) {
        // 是单元运算符?Call:)
        // 是Call? 看前面
        //      没有了->不是
        //      前一个是operator->不是
        //      前一个是identifier->是
        //      前一个是GenericPre->是
        //      前一个是常数->不是
        //      前一个是不知道->不是
        ExpressionElement pre = result.get(result.size() - 1);
        boolean isIdentifier = pre instanceof IdentifierString;
        boolean isGenericPre =
                pre instanceof OperatorString && ((OperatorString) pre).getValue() == Operator.GENERIC_LIST_POST;
        return isIdentifier || isGenericPre ? Operator.CALL_PRE : Operator.BRACKET_PRE;
    }

    private static Operator distinguishPostCall(Expression result, SourceString sourceString) {
        // 依据前面的内容是call来决定这个是什么
        // 用匹配找
        // ()
        int inTuple = 0;
        int inCall = 0;
        for (int i = result.size() - 1; i >= 0; i--) {
            ExpressionElement element = result.get(i);
            if (!(element instanceof OperatorString)) {
                continue;
            }
            OperatorString os = (OperatorString) element;
            Operator value = os.getValue();
            if (value == Operator.BRACKET_PRE) {
                inTuple++;
                if (inTuple == 1) {
                    if (inCall != 0) {
                        break;
                    }
                    return Operator.BRACKET_POST;
                }
            } else if (value == Operator.BRACKET_POST) {
                inTuple--;
            } else if (value == Operator.CALL_POST) {
                inCall--;
            } else if (value == Operator.CALL_PRE) {
                inCall++;
                if (inCall == 1) {
                    if (inTuple != 0) {
                        break;
                    }
                    return Operator.CALL_POST;
                }
            }
        }
        throw new AnalysisExpressionException(sourceString.getPosition(), "can not find pre");
    }


    private static Operator distinguishDecrease(ArrayList<SourceString> src, int index) {
        return distinguishLeftRight(src, index, Operator.LEFT_DECREASING, Operator.RIGHT_DECREASING);
    }

    private static Operator distinguishIncrease(ArrayList<SourceString> src, int index) {
        return distinguishLeftRight(src, index, Operator.LEFT_INCREASING, Operator.RIGHT_INCREASING);
    }

    private static Operator distinguishLeftRight(ArrayList<SourceString> src, int index, Operator onLeft,
                                                 Operator onRight) {
        EncirclePair<SourceString> pair = CollectionUtil.getEncirclePair(src, index);
        if (pair.bothNull()) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        }
        SourceString pre = pair.getPre();
        SourceString post = pair.getPost();
        if (pre == null) {
            if (post.getType() == SourceStringType.IDENTIFIER) {
                // ++x
                return onLeft;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a variable");
            }
        } else if (post == null) {
            if (pre.getType() == SourceStringType.IDENTIFIER) {
                // x++
                return onRight;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a variable");
            }
        } else {
            if (pre.getType() == SourceStringType.IDENTIFIER && post.getType() == SourceStringType.OPERATOR) {
                // x++ + 2
                return onRight;
            } else if (pre.getType() == SourceStringType.OPERATOR && post.getType() == SourceStringType.IDENTIFIER) {
                // 2 + ++x
                return onLeft;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a only variable");
            }
        }
    }

    private static Operator distinguishMinus(ArrayList<SourceString> src, int index) {
        return distinguishSign(src, index, Operator.NEGATIVE, Operator.SUBTRACT);
    }

    private static Operator distinguishPlus(ArrayList<SourceString> src, int index) {
        return distinguishSign(src, index, Operator.POSITIVE, Operator.ADD);
    }

    private static Operator distinguishSign(ArrayList<SourceString> src, int index, Operator sign, Operator operator) {
        EncirclePair<SourceString> pair = CollectionUtil.getEncirclePair(src, index);
        if (pair.bothNull()) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        }
        SourceString pre = pair.getPre();
        SourceString post = pair.getPost();
        if (pre == null) {
            if (post.getType() == SourceStringType.IDENTIFIER) {
                // +x
                return sign;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
            }
        } else if (post == null) {
            throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a number");
        } else {
            if (pre.getType() == SourceStringType.IDENTIFIER && post.getType() == SourceStringType.OPERATOR) {
                // x+ +2
                return operator;
            } else if (pre.getType() == SourceStringType.OPERATOR && post.getType() == SourceStringType.IDENTIFIER) {
                // 2 + +x
                return sign;
            } else {
                throw new AnalysisExpressionException(src.get(index).getPosition(), "expected a only number");
            }
        }
    }


}
