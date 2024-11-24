package org.harvey.compiler.analysis.stmt.phaser.variable;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.ImportPhaser;
import org.harvey.compiler.analysis.stmt.phaser.SentenceBasicPhaser;
import org.harvey.compiler.analysis.stmt.phaser.callable.BodyCallablePhaser;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.type.SourceType;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 15:43
 */
public abstract class VariablePhaser extends SentenceBasicPhaser {
    public VariablePhaser(StatementContext context) {
        super(context);
    }

    @Override
    public boolean isTargetPart(DepartedPart bodyPart) {
        super.isTargetPart(bodyPart);
        DepartedSentencePart part = (DepartedSentencePart) bodyPart;
        if (ImportPhaser.isImport(bodyPart)) {
            return false;
        }
        return !BodyCallablePhaser.callableInPart(part);
    }

    protected static SourceType variableType(SourceTextContext type) {
        // 2. Identifier<br>
        // 3. int8 int32, bool等<br>
        // 4. unsigned int8<br>
        String tobeBuild = "file variable";
        SourceType sourceType = null;
        int size = type.size();
        assert size != 0;
        if (size == 1) {
            // 一定是Identifier或int8或bool
            SourceString first = type.pollFirst();
            if (first.getType() == SourceStringType.IDENTIFIER) {
                sourceType = SourceType.identifierType(first.getValue());
            } else if (first.getType() == SourceStringType.KEYWORD &&
                    CollectionUtil.contains(Keywords.BASIC_TYPE, kw -> kw.equals(first.getValue()))) {
                sourceType = SourceType.basicType(Keyword.get(first.getValue()));
            } else {
                throw new AnalysisExpressionException(first.getPosition(),
                        " is not allowed to be the type of " + tobeBuild);
            }
        } else if (size == 2) {
            // 一定是unsigned int8/signed int32
            SourceString pre = type.pollFirst();
            SourceString post = type.pollFirst();
            assert pre != null;
            assert post != null;
            String preValue = pre.getValue();
            if (pre.getType() != SourceStringType.KEYWORD || post.getType() != SourceStringType.KEYWORD) {
                throw new AnalysisExpressionException(pre.getPosition(),
                        SourcePosition.moveToEnd(post.getPosition(), post.getValue()),
                        " is not allowed to be the type of " + tobeBuild);
            }
            if ((Keyword.SIGNED.equals(preValue) || Keyword.UNSIGNED.equals(preValue)) && (
                    CollectionUtil.contains(Keywords.BASIC_EMBELLISH_ABLE_TYPE, kw -> kw.equals(post.getValue())))) {
                sourceType = SourceType.embellishAbleBasicType(Keyword.get(pre.getValue()), Keyword.get(post.getValue()));
            } else {
                throw new AnalysisExpressionException(pre.getPosition(),
                        SourcePosition.moveToEnd(post.getPosition(), post.getValue()),
                        " is not allowed to be the type of " + tobeBuild);
            }
        }
        // 如果还有剩余, 全部都错
        if (!type.isEmpty()) {
            type.throwAllAsCompileException(" is not allowed to be the type of " + tobeBuild,
                    AnalysisExpressionException.class);
        }
        assert sourceType != null;
        return sourceType;
    }

    protected static Map<SourceString, SourceTextContext> identifierExpressionMap(SourceString identifier, SourceTextContext attachment) {
        // TODO 检查
        Map<SourceString, SourceTextContext> map = new HashMap<>();
        attachment.add(identifier);
        ListIterator<SourceString> it = attachment.listIterator();
        while (it.hasNext()) {
            identifier = it.next();
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
        expression.add(expectAssign);
        while (it.hasNext()) {
            SourceString next = it.next();
            position = next.getPosition();
            if (expectAssign.getType() == SourceStringType.OPERATOR && Operator.COMMA.nameEquals(next.getValue())) {
                break;
            }
            expression.add(next);
        }
        return expression;
    }
}
