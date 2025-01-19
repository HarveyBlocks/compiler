package org.harvey.compiler.declare;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.type.SourceType;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.analysis.GenericUnsupportedException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 12:33
 */

public class FurtherVariablePhaser {

    protected static SourceType variableType(SourceTextContext type) {
        // 2. Identifier
        // 3. int8 int32, bool等
        // 4. unsigned int8
        // 5. 类型[]
        // 6. 类型<...>
        String tobeBuilt = "file variable";
        SourceType sourceType = null;
        int size = type.size();
        if (size == 0) {
            throw new AnalysisExpressionException(SourcePosition.UNKNOWN,
                    "declare " + tobeBuilt + " need a type");
        }
        SourceString last = type.getLast();
        /*不知何意 if (last.getType() == SourceStringType.KEYWORD) {
            throw new AnalysisExpressionException
            (last.getPosition(), last.getValue() + " is not allowed to be the type of " + tobeBuilt ();
        } else */
        if (last.getType() == SourceStringType.OPERATOR) {
            // 处理复合类型, 例如
            // 数组 类型[]
            /*取消了数组声明 if (Operator.ARRAY_DECLARE.nameEquals(last.getValue())) {
                // 数组类型
                // 不断遍历, 直到是ElementType
                int dimension = 0;
                SourceTextContext tempType = new SourceTextContext(type);
                while (!tempType.isEmpty()) {
                    last = tempType.removeLast();
                    if (!Operator.ARRAY_DECLARE.nameEquals(last.getValue())) {
                        tempType.add(last);
                        break;
                    }
                    dimension++;
                }
                // (AAA[])[][][]
                // 虽然有递归, 但是只递归一遍, 会赢的
                return SourceType.arrayType(variableType(tempType), dimension);
            } else*/
            if (Operator.GENERIC_LIST_POST.nameEquals(last.getValue())) {
                // 泛型 类型<>
                // 暂时不支持
                // 如何解析减少递归, 各个类呢?
                // TODO
                throw new GenericUnsupportedException(last.getPosition());
//                return SourceType.genericType(type);
            } else {
                throw new AnalysisExpressionException(
                        last.getPosition(), "Operator:" + last.getValue() + " is illegal here");
            }
        }
        if (size == 1) {
            // 一定是Identifier或int8等或bool
            SourceString first = type.pollFirst();
            if (first.getType() == SourceStringType.IDENTIFIER) {
                sourceType = SourceType.identifierType(first.getValue());
            } /*无用的unsigned int 判断分支 else if (first.getType() == SourceStringType.KEYWORD &&
                    CollectionUtil.contains(Keywords.BASIC_TYPE_EMBELLISH, kw -> kw.equals(first.getValue()))) {
                sourceType = SourceType.embellishAbleBasicType(Keyword.get(first.getValue()), Keyword.INT32);
            } */ else if (first.getType() == SourceStringType.KEYWORD &&
                    CollectionUtil.contains(Keywords.BASIC_TYPE, kw -> kw.equals(first.getValue()))) {
                sourceType = SourceType.basicType(Keyword.get(first.getValue()));
            } else {
                throw new AnalysisExpressionException(first.getPosition(),
                        first.getValue() + " is not allowed to be the type of " + tobeBuilt);
            }
        } /*无用的判断unsigned int8的代码
        else if (size == 2) {
            // 一定是unsigned int8/signed int32
            SourceString pre = type.pollFirst();
            SourceString post = type.pollFirst();
            assert pre != null;
            assert post != null;
            String preValue = pre.getValue();
            if (pre.getType() != SourceStringType.KEYWORD || post.getType() != SourceStringType.KEYWORD) {
                throw new AnalysisExpressionException(pre.getPosition(),
                        SourcePosition.moveToEnd(post.getPosition(), post.getValue()),
                        pre.getValue() + " " + post.getValue() + " is not allowed to be the type of " + tobeBuilt);
            }
            if ((Keyword.SIGNED.equals(preValue) || Keyword.UNSIGNED.equals(preValue)) && (
                    CollectionUtil.contains(Keywords.BASIC_EMBELLISH_ABLE_TYPE, kw -> kw.equals(post.getValue())))) {
                sourceType = SourceType.embellishAbleBasicType(Keyword.get(pre.getValue()), Keyword.get(post.getValue()));
            } else {
                throw new AnalysisExpressionException(pre.getPosition(),
                        SourcePosition.moveToEnd(post.getPosition(), post.getValue()),
                        " is not allowed to be the type of " + tobeBuilt);
            }
        }*/
        // 如果还有剩余, 全部都错
        if (!type.isEmpty()) {
            type.throwExceptionIncludingAll(
                    type.getFirst().getValue() + " is not allowed to be the type of " + tobeBuilt,
                    AnalysisExpressionException.class);
        }
        assert sourceType != null;
        return sourceType;
    }

    protected static Map<SourceString, SourceTextContext> identifierExpressionMap(SourceString identifier,
                                                                                  SourceTextContext attachment) {
        Map<SourceString, SourceTextContext> map = new HashMap<>();
        attachment.addFirst(identifier);
        ListIterator<SourceString> it = attachment.listIterator();
        while (it.hasNext()) {
            identifier = it.next();
            String identifierValue = identifier.getValue();
            if (CollectionUtil.contains(map.keySet(), ss -> identifierValue.equals(ss.getValue()))) {
                throw new AnalysisExpressionException(identifier.getPosition(),
                        identifierValue + " repeat definitions");
            }
            SourceTextContext expression = findExpression(it, identifier);
            map.put(identifier, expression == null ? SourceTextContext.EMPTY : expression);
        }
        return map;
    }

    private static SourceTextContext findExpression(ListIterator<SourceString> it, SourceString identifierSource) {
        // 找标识符
        //      下一个要么没了
        //      下一个是赋值
        //      报错
        // 是赋值
        //      遍历找逗号
        //      找到逗号
        //          回到1
        // 是逗号
        //      返回null
        // 没了就结束
        if (!it.hasNext()) {
            return null;
        }
        SourceString expectAssign = it.next();
        SourcePosition position = expectAssign.getPosition();
        if (expectAssign.getType() != SourceStringType.OPERATOR) {
            throw new AnalysisExpressionException(position, "Expect assign operator or comma.");
        }
        String value = expectAssign.getValue();
        if (Operator.COMMA.nameEquals(value)) {
            return null;
        }
        boolean isAssign = Operators.isAssign(value);
        if (!isAssign) {
            throw new AnalysisExpressionException(position, "Expect assign operator or comma.");
        }
        SourceTextContext expression = new SourceTextContext();
        expression.add(identifierSource);
        String identifierSourceValue = identifierSource.getValue();
        expression.add(expectAssign);
        while (it.hasNext()) {
            SourceString next = it.next();
            SourceStringType type = next.getType();
            String nextValue = next.getValue();
            if (type == SourceStringType.OPERATOR &&
                    Operator.COMMA.nameEquals(nextValue) || type == SourceStringType.SIGN) {
                // TODO 区分函数内逗号和泛型内逗号
                //  要区分泛型内逗号, 就需要先区分啥是泛型
                //  区分啥是泛型, 就要先注册类
                // 要区分函数内逗号, 就要先注册函数
                // 先延迟对变量的声明吧... 果然, 用关键字声明方法好了(ˉ▽ˉ；)...
                break;
            } else if (type == SourceStringType.KEYWORD &&
                    Keywords.isControlStructure(nextValue)) {
                throw new AnalysisExpressionException(next.getPosition(), nextValue + " is not allowed here.");
            } else if (type == SourceStringType.IDENTIFIER && nextValue.equals(identifierSourceValue)) {
                throw new AnalysisExpressionException(next.getPosition(), nextValue + " recursive definition.");
            }
            expression.add(next);
        }
        assert !it.hasNext();
        return expression;
    }

    protected SourceType registerType(SourceTextContext type) {
        return variableType(type);
    }
}
