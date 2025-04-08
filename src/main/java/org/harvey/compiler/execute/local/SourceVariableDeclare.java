package org.harvey.compiler.execute.local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.declare.analysis.EmbellishSource;
import org.harvey.compiler.exception.VieCompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

/**
 * 比较适合作为局部变量的分解
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 17:55
 */
@Getter
@AllArgsConstructor
public class SourceVariableDeclare {
    // int a = 2, b ,c = 3, e, f, g;
    // 一开始identifier的, 后面道是
    // 先看前面的俩, 然后后面的作为表达式区分类型, 区分完类型之后呢, 区分出逗号是第一层的, 然后获取
    private final EmbellishSource embellish;
    private final SourceTextContext type;
    private final SourceString identifier;
    private final SourceTextContext assign;

    /**
     * @return null 表示不含有声明, 是表达式
     */
    public static SourceVariableDeclare create(SourceTextContext source) {
        Declarable declarable;
        try {
            declarable = DeclarableFactory.statementBasic(source);
        } catch (VieCompilerException ignored) {
            return null;
        }
        if (!declarable.isVariableDeclare()) {
            return null;
        }
        SourceTextContext permissions = declarable.getPermissions();
        if (permissions != null && !permissions.isEmpty()) {
            throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                    permissions.getLast().getPosition(), "permissions is illegal at local variable"
            );
        }
        SourceTextContext type = declarable.getType();
        if (type.isEmpty()) {
            return null;
        }
        SourceString identifier = declarable.getIdentifier();
        if (identifier == null) {
            return null;
        }
        EmbellishSource embellish = declarable.getEmbellish();
        validEmbellish(embellish);
        SourceTextContext assign = getAssign(declarable, identifier);
        return new SourceVariableDeclare(embellish, type, identifier, assign);
    }

    private static SourceTextContext getAssign(Declarable declarable, SourceString identifier) {
        SourceTextContext attachment = declarable.getAttachment();
        if (!attachment.isEmpty()) {
            attachment.addFirst(identifier);
        }
        return attachment;
    }

    private static void validEmbellish(EmbellishSource embellish) {
        if (embellish.getStaticMark() != null) {
            throw new AnalysisExpressionException(embellish.getStaticMark(), "static is illegal at local variable");
        }
        if (embellish.getSealedMark() != null) {
            throw new AnalysisExpressionException(embellish.getSealedMark(), "sealed is illegal at local variable");
        }
        if (embellish.getAbstractMark() != null) {
            throw new AnalysisExpressionException(embellish.getAbstractMark(), "abstract is illegal at local variable");
        }
    }

    public static LocalType localType(SourceTextContext type) {
        if (type.isEmpty()) {
            throw new CompilerException("type can not be empty");
        }
        return localType(type.listIterator(), type.getFirst().getPosition());
    }

    public static LocalType localType(ListIterator<SourceString> it, SourcePosition start) {
        // 解析作用域
        if (!it.hasNext()) {
            throw new CompilerException("expected a type");
        }
        SourceTextContext permissions = DeclarableFactory.getPermissions(it);
        if (!permissions.isEmpty()) {
            throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                    permissions.getLast().getPosition(), "permission is illegal here"
            );
        }
        // 解析修饰[static final sealed const]
        EmbellishSource embellish = DeclarableFactory.getEmbellish(it);
        validEmbellish(embellish);
        // 解析类型
        SourceTextContext sourceType = DeclarableFactory.getType(it, start);
        if (it.hasNext()) {
            throw new AnalysisExpressionException(it.next().getPosition(), "expected end the type");
        }
        return new LocalType(
                embellish.getConstMark(), embellish.getFinalMark(), null/*ExpressionFactory.type(sourceType)*/);
    }

    public static List<LocalType> phaseTypeList(
            ListIterator<SourceString> iterator,
            Predicate<SourceString> suspendConditions) {
        List<LocalType> result = new ArrayList<>();
        SourceTextContext part = new SourceTextContext();
        int inGeneric = 0;
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            part.add(next);
            if (suspendConditions != null && suspendConditions.test(next)) {
                break;
            }
            if (next.getType() != SourceType.OPERATOR) {
                continue;
            }
            String value = next.getValue();
            if (inGeneric == 0 && Operator.COMMA.nameEquals(value)) {
                part.removeLast();
                if (part.isEmpty()) {
                    throw new AnalysisExpressionException(next.getPosition(), "excepted a exception type");
                }
                result.add(SourceVariableDeclare.localType(part));
                part = new SourceTextContext();
            } else if (Operator.GENERIC_LIST_PRE.nameEquals(value)) {
                inGeneric++;
            } else if (Operator.GENERIC_LIST_POST.nameEquals(value)) {
                inGeneric--;
            }
        }
        if (part.isEmpty()) {
            throw new CompilerException("excepted a exception type, however, will not be the case");
        }
        result.add(SourceVariableDeclare.localType(part));
        return result;
    }

}
