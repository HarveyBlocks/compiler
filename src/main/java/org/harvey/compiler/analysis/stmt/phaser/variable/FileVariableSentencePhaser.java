package org.harvey.compiler.analysis.stmt.phaser.variable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.meta.mv.MetaFileVariable;
import org.harvey.compiler.analysis.stmt.phaser.SourceContextPhaser;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.type.SourceType;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 采用的C/C++的全局变量的策略进行解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 13:04
 * @deprecated 新: {@link FileVariablePhaser}
 */
@Deprecated
public class FileVariableSentencePhaser extends SourceContextPhaser {

    public FileVariableSentencePhaser(StatementContext context) {
        super(context);
    }

    @Override
    public boolean isTargetPart(DepartedPart bodyPart) {
        if (!(bodyPart instanceof DepartedSentencePart)) {
            return false;
        }
        // 引用语句一定是一个 ExpressionType.SENTENCE ;
        SourceTextContext sentence = ((DepartedSentencePart) bodyPart).getSentence();
        if (sentence == null) {
            return false;
        }
        int size = sentence.size();
        if (size < 2) {
            // 至少需要一个类型声明和一个变量
            return false;
        }
        // [internal] [AccessControl] [static, const, final] 类型(Keyword|Identifier) 标识符 [=Expression];
        CollectionUtil.Result<SourceString> type = findType(sentence);
        if (type == null) {
            throw new AnalysisExpressionException(sentence.get(0).getPosition(),
                    "Illegal variable declare way, type of the variable is needed.");
        }
        CollectionUtil.Result<SourceString> identifier = findFirstIdentifier(sentence, type);
        if (identifier == null) {
            throw new AnalysisExpressionException(type.getElement().getPosition(),
                    "Illegal variable declare way, identifier of the variable is needed.");
        }
        return true;
    }

    private static CollectionUtil.Result<SourceString> findFirstIdentifier(SourceTextContext part, CollectionUtil.Result<SourceString> type) {
        CollectionUtil.Result<SourceString> identifier = findIdentifier(part, 0);
        if (identifier == null) {
            return null;
        }
        if (type.getIndex() == identifier.getIndex()) {
            identifier = findIdentifier(part, identifier.getIndex() + 1);
        }
        if (identifier == null || type.getIndex() + 1 != identifier.getIndex()) {
            return null;
        }
        return identifier;
    }

    private static CollectionUtil.Result<SourceString> findType(SourceTextContext part) {
        Predicate<SourceString> predicate = s -> {
            SourceStringType type = s.getType();
            String value = s.getValue();
            if (type == SourceStringType.IDENTIFIER) {
                return true;
            }
            if (type != SourceStringType.KEYWORD) {
                return false;
            }
            if (Keyword.VAR == Keyword.get(value)) {
                return true;
            }
            return Keywords.isBasicType(value);
        };
        return CollectionUtil.find(part, predicate);
    }

    private static CollectionUtil.Result<SourceString> findIdentifier(
            SourceTextContext part, int fromIndex) {
        return CollectionUtil.find(
                part,
                s -> s.getType() == SourceStringType.IDENTIFIER,
                fromIndex
        );
    }

    private static CollectionUtil.Result<SourceString> findTypeEmbellish(
            SourceTextContext part) {
        return CollectionUtil.find(part, s -> Keywords.isBasicTypeEmbellish(s.getValue()));
    }

    private static final Set<AccessControl.Permission> FILE_VARIABLE_AVAILABLE_PERMISSION;
    private static final AccessControl.Permission DEFAULT_FILE_VARIABLE_PERMISSION = AccessControl.Permission.FILE;

    static {
        FILE_VARIABLE_AVAILABLE_PERMISSION = Set.of(
                AccessControl.Permission.PUBLIC,
                AccessControl.Permission.FILE,
                AccessControl.Permission.PACKAGE
        );
    }

    private SourcePosition position;

    @Override
    public void phase(DepartedPart part) {
        SourceTextContext sentence = ((DepartedSentencePart) part).getSentence();
        position = sentence.getFirst().getPosition();
        ListIterator<SourceString> it = sentence.listIterator();
        // 获取访问控制
        AccessControl accessControl;
        // [internal] [AccessControl] [static, const, final] 类型(Keyword|Identifier) 标识符 [=Expression];
        try {
            accessControl = getAccessControl(it);
        } catch (IndexOutOfBoundsException e) {
            throw new AnalysisExpressionException(position, "Leak of declaring part");
        }
        // [static, const, final] 类型(Keyword|Identifier) 标识符 [=Expression];
        VariableEmbellish embellish = getVariableEmbellish(it);
        boolean constVar = embellish.constVar;
        boolean finalVar = embellish.finalVar;
        // 类型(Keyword|Identifier) 标识符 [=Expression];
        Keyword typeEmbellish = getTypeEmbellish(it);
        Keyword basicType = getType(it);
        if (typeEmbellish != null && !Keywords.isBasicEmbellishAbleType(basicType)) {
            throw new AnalysisExpressionException(position,
                    typeEmbellish + " embellished the type of " + basicType + ", which is illegal");
        }
        String identifierType = basicType == null ? getIdentifierType(it) : null;
        if (typeEmbellish != null && identifierType != null) {
            throw new AnalysisExpressionException(position,
                    typeEmbellish + " embellished the type of " + identifierType + ", which is illegal");
        }
        // 标识符 [=Expression];(多个)
        // 标识符 [=...][,标识符[=...]][,标识符[=...]]
        registerVariable(it, typeEmbellish, basicType, identifierType, accessControl, constVar, finalVar);

    }

    private void registerVariable(
            ListIterator<SourceString> it,
            Keyword typeEmbellish, Keyword basicType,
            String identifierType, AccessControl accessControl,
            boolean constVar, boolean finalVar) {
        // 找标识符
        //      下一个要么没了
        //      下一个是赋值
        //      报错
        // 是赋值
        //      遍历找逗号
        // 找到逗号
        //      回到1
        // 没了就结束
        while (it.hasNext()) {
            SourceString identifierSource = it.next();
            position = identifierSource.getPosition();
            if (identifierSource.getType() != SourceStringType.IDENTIFIER) {
                throw new AnalysisExpressionException(position, " Identifier is needed");
            }
            MetaFileVariable.Builder builder = new MetaFileVariable.Builder();
            builder.type(SourceType.type(typeEmbellish, basicType, identifierType));
            builder.accessControl(accessControl);
            if (constVar) {
                builder.embellishConst();
            }
            if (finalVar) {
                builder.embellishFinal();
            }
            builder.identifier(identifierSource);
            SourceTextContext expression = findExpression(it, identifierSource);
            if (expression != null) {
                builder.assignExpression(expression);
            }
            context.addFileVariable(builder.build());
        }
    }

    private SourceTextContext findExpression(ListIterator<SourceString> it, SourceString identifierSource) {
        throw new CompilerException("@see org.harvey.compiler.analysis.stmt.phaser.variable.VariablePhaser.findExpression");
    }


    private AccessControl getAccessControl(ListIterator<SourceString> it) {
        AccessControl accessControl = new AccessControl();
        AccessControl.Permission permission;
        if ((permission = getPermission(it)) == null) {
            return accessControl.addPermission(DEFAULT_FILE_VARIABLE_PERMISSION);
        }
        if (permission != AccessControl.Permission.INTERNAL) {
            return accessControl.addPermission(permission);
        }

        // internal
        if ((permission = getPermission(it)) == null) {
            return accessControl.addPermission(DEFAULT_FILE_VARIABLE_PERMISSION);
        }
        if (permission != AccessControl.Permission.PACKAGE) {
            return null;
        }
        return accessControl.addInternalPermission(permission);
    }

    private AccessControl.Permission getPermission(ListIterator<SourceString> it) {
        if (!it.hasNext()) {
            throw new AnalysisExpressionException(position, "Leak of type");
        }
        SourceString next = it.next();
        position = next.getPosition();
        if (next.getType() != SourceStringType.KEYWORD) {
            it.previous();
            return null;
        }
        String value = next.getValue();

        AccessControl.Permission permission = AccessControl.Permission.get(value);
        if (permission == null) {
            throw new AnalysisExpressionException(position, "Unknown access control permission");
        }
        return permission;
    }

    @Getter
    @AllArgsConstructor
    private static class VariableEmbellish {
        private final boolean constVar;
        private final boolean finalVar;
    }

    private VariableEmbellish getVariableEmbellish(ListIterator<SourceString> it) {
        boolean constVar = false;
        boolean finalVar = false;
        while (it.hasNext()) {
            SourceString next = it.next();
            position = next.getPosition();
            if (next.getType() != SourceStringType.KEYWORD) {
                it.previous();
                break;
            }
            Keyword keyword = Keyword.get(next.getValue());
            if (keyword == null) {
                it.previous();
                break;
            }
            boolean endLoop = false;
            switch (keyword) {
                case CONST:
                    if (constVar) {
                        throw new AnalysisExpressionException(position, "Multiple of const");
                    }
                    constVar = true;
                    break;
                case FINAL:
                    if (finalVar) {
                        throw new AnalysisExpressionException(position, "Multiple of final");
                    }
                    finalVar = true;
                    break;
                case INTERNAL:
                    throw new AnalysisExpressionException(position, keyword + "is not allowed here");
                default:
                    if (Keywords.isBasicTypeEmbellish(keyword) || Keywords.isBasicType(keyword)) {
                        endLoop = true;
                    } else if (Keywords.isAccessControl(keyword)) {
                        throw new AnalysisExpressionException(position, keyword + ", access control keyword is not allowed in here");
                    } else {
                        throw new AnalysisExpressionException(position, keyword + " is not allowed in declare file variable");
                    }
            }
            if (endLoop) {
                it.previous();
                break;
            }
        }
        return new VariableEmbellish(constVar, finalVar);
    }

    private Keyword getTypeEmbellish(ListIterator<SourceString> it) {
        Keyword keyword = getNextKeyWord(it);
        if (keyword == null) {
            it.previous();
            return null;
        }
        if (!Keywords.isBasicTypeEmbellish(keyword)) {
            it.previous();
            return null;
        }
        return keyword;
    }

    private Keyword getType(ListIterator<SourceString> it) {
        Keyword keyword = getNextKeyWord(it);
        if (keyword == null) {
            it.previous();
            return null;
        }
        if (keyword == Keyword.VOID) {
            throw new AnalysisExpressionException(position, "void type is not allowed to be the type of variable");
        }
        if (!Keywords.isBasicType(keyword)) {
            it.previous();
            return null;
        }
        return keyword;
    }

    private Keyword getNextKeyWord(ListIterator<SourceString> it) {
        if (!it.hasNext()) {
            throw new AnalysisExpressionException(position, "Leak of type");
        }
        SourceString next = it.next();
        position = next.getPosition();
        if (next.getType() != SourceStringType.KEYWORD) {
            return null;
        }
        Keyword keyword = Keyword.get(next.getValue());
        if (keyword == null) {
            throw new AnalysisExpressionException(position, "Unknown keyword:" + keyword);
        }
        return keyword;
    }

    private String getIdentifierType(ListIterator<SourceString> it) {
        if (!it.hasNext()) {
            throw new AnalysisExpressionException(position, "Leak of type");
        }
        SourceString next = it.next();
        position = next.getPosition();
        SourceStringType type = next.getType();
        if (type != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(position, "Expect identifier, but not " + type);
        }
        String value = next.getValue();
        if (value == null || value.isEmpty()) {
            throw new CompilerException(position + "Expect identifier, but it is null!");
        }
        return value;
    }
}
