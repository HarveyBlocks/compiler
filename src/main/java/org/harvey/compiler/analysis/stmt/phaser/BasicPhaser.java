package org.harvey.compiler.analysis.stmt.phaser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:51
 */
public abstract class BasicPhaser extends SourceContextPhaser {

    public BasicPhaser(StatementContext context) {
        super(context);
    }

    @Getter
    protected static class Embellish {
        private final boolean markedConstant;
        private final boolean markedStatic;
        private final boolean markedFinal;
        private final boolean markedSealed;

        public Embellish(EmbellishSourceString embellish) {
            markedConstant = embellish.constantMark != null;
            markedStatic = embellish.staticMark != null;
            markedFinal = embellish.finalMark != null;
            markedSealed = embellish.sealedMark != null;
        }
    }

    @Getter
    protected static class EmbellishSourceString {
        private SourceString constantMark = null;
        private SourceString staticMark = null;
        private SourceString finalMark = null;
        private SourceString sealedMark = null;
    }

    @Getter
    @AllArgsConstructor
    protected static class BasicPhaseResult {
        private final SourcePosition start;
        private final SourceTextContext permissions;
        private final EmbellishSourceString embellish;
        private final SourceTextContext type;
        private final SourceString identifier;
        /**
         * 参数列表/函数参数列表/变量赋值
         */
        private final SourceTextContext attachment;
    }

    protected static BasicPhaseResult basicPhase(SourceTextContext sentence) {
        SourcePosition start = sentence.getFirst().getPosition();
        ListIterator<SourceString> it = sentence.listIterator();
        // 解析作用域
        SourceTextContext permissions = getPermissions(it);
        // 解析修饰[static final sealed const]
        EmbellishSourceString embellish = getEmbellish(it);
        // 解析类型
        SourceTextContext type = getType(it, start);
        // 解析标识符 一定是identifier
        SourceString identifier = getIdentifier(it, start);
        // 剩余
        SourceTextContext attachment = getAttachment(it);
        return new BasicPhaseResult(start, permissions, embellish, type, identifier, attachment);
    }


    private static SourceTextContext getPermissions(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        SourceTextContext permissions = new SourceTextContext();
        while (it.hasNext()) {
            SourceString next = it.next();
            if (next.getType() != SourceStringType.KEYWORD) {
                it.previous();
                break;
            }
            String value = next.getValue();
            if (!AccessControl.Permission.is(value)) {
                it.previous();
                break;
            }
            permissions.add(next);
        }
        return permissions;
    }

    private static EmbellishSourceString getEmbellish(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        EmbellishSourceString embellish = new EmbellishSourceString();
        while (it.hasNext()) {
            SourceString next = it.next();
            if (next.getType() != SourceStringType.KEYWORD) {
                it.previous();
                break;
            }
            String value = next.getValue();
            Keyword keyword = Keyword.get(value);
            if (keyword == null) {
                throw new CompilerException(next + " is not a keyword");
            }
            switch (keyword) {
                case CONST:
                    if (embellish.constantMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple const");
                    }
                    embellish.constantMark = next;
                    break;
                case STATIC:
                    if (embellish.staticMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple static");
                    }
                    embellish.staticMark = next;
                    break;
                case FINAL:
                    if (embellish.finalMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple final");
                    }
                    embellish.finalMark = next;
                    break;
                case SEALED:
                    if (embellish.sealedMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple sealed");
                    }
                    embellish.sealedMark = next;
                    break;
                default:
                    it.previous();
                    break;
            }
        }
        return embellish;
    }

    private static SourceTextContext getType(ListIterator<SourceString> it, SourcePosition start) {
        // 类型
        // 1. Identifier
        // 2. int8 int32, bool等
        // 3. var
        // 4. unsigned int8
        // 5. void func
        // 7. Identifier func
        // 8. (Identifier) func
        // 9. (Identifier1,Identifier2) func
        // 10. (Identifier1,Identifier2) abstract func
        // 11. struct/enum/class/interface
        // 12. abstract class
        // 13. ...
        SourceTextContext type = new SourceTextContext();
        if (!it.hasNext()) {
            throw new AnalysisExpressionException(start, "type is needed");
        }
        SourceString first = it.next();
        if (first.getType() == SourceStringType.IDENTIFIER) {
            // Identifier func, 特别的
            type.add(first);
            boolean nextIsFun = nextIsFun(it);
            if (nextIsFun) {
                type.add(it.next());
            }
            return type;
        }
        it.previous();
        int inTuple = 0;
        SourcePosition last = null;
        while (it.hasNext()) {
            SourceString next = it.next();
            last = next.getPosition();
            if (isBracketPre(next)) {
                inTuple++;
            } else if (isBracketPost(next)) {
                inTuple--;
            }
            if (inTuple < 0) {
                throw new AnalysisExpressionException(last, "Illegal bracket matching");
            }
            if (inTuple == 0 && first.getType() == SourceStringType.IDENTIFIER) {
                it.previous();
                break;
            }
            type.add(next);
        }
        if (inTuple != 0) {
            throw new AnalysisExpressionException(last, "Illegal bracket matching");
        }
        return type;
    }

    private static boolean isBracketPost(SourceString next) {
        return next.getType() == SourceStringType.OPERATOR && Operator.BRACKET_POST.nameEquals(next.getValue());
    }

    private static boolean isBracketPre(SourceString next) {
        return next.getType() == SourceStringType.OPERATOR && Operator.BRACKET_PRE.nameEquals(next.getValue());
    }

    private static boolean nextIsFun(ListIterator<SourceString> it) {
        return CollectionUtil.nextIs(it, ss -> {
            if (ss.getType() == SourceStringType.KEYWORD) {
                return false;
            }
            return Keyword.get(ss.getValue()) == Keyword.CALLABLE;
        });
    }

    private static SourceString getIdentifier(ListIterator<SourceString> it, SourcePosition start) {
        SourceString identifier;
        if (!it.hasNext() || (identifier = it.next()).getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(start, "identifier is expected");
        }
        return identifier;
    }

    private static SourceTextContext getAttachment(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        SourceTextContext attachment = new SourceTextContext();
        while (it.hasNext()) {
            attachment.add(it.next());
        }
        return attachment;
    }
}
