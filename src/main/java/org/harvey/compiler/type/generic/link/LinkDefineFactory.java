package org.harvey.compiler.type.generic.link;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-16 19:39
 */
public class LinkDefineFactory {
    private static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    private static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();

    public static LinkedGenericDefine[] genericDefineList(ListIterator<SourceString> iterator) {
        if (!iterator.hasNext()) {
            return new LinkedGenericDefine[0];
        }
        SourceTextContext genericDefineList = SourceTextContext.skipNest(iterator, GENERIC_LIST_PRE_NAME,
                GENERIC_LIST_POST_NAME, false
        );
        if (genericDefineList.isEmpty()) {
            return new LinkedGenericDefine[0];
        }
        List<LinkedGenericDefine> result = new ArrayList<>();
        while (iterator.hasNext()) {
            LinkedGenericDefine define = genericDefine(iterator);
            result.add(define);
            if (skipIf(iterator, Operator.GENERIC_LIST_POST)) {
                return result.toArray(new LinkedGenericDefine[0]);
            } else if (!skipIf(iterator, Operator.COMMA)) {
                throw new AnalysisTypeException(
                        iterator.previous().getPosition(), "expected " + Operator.COMMA.getName());
            }
        }
        throw new AnalysisTypeException(
                iterator.previous().getPosition(), "not a generic message: not completed");
    }

    public static LinkedGenericDefine genericDefine(ListIterator<SourceString> iterator) {
        // <T extends BaseClass & BaseInterfaces & super LowerInterface & new<int,int> & new<long> = DefaultType >
        if (!iterator.hasNext()) {
            throw new CompilerException("empty define cna not invoke this callable.");
        }
        // generic name
        SourceString exceptName = iterator.next();
        IdentifierString name;
        SourcePosition position = exceptName.getPosition();
        if (exceptName.getType() == SourceType.IGNORE_IDENTIFIER) {
            name = new IdentifierString(position);
        } else if (exceptName.getType() == SourceType.IDENTIFIER) {
            name = new IdentifierString(position, exceptName.getValue());
        } else {
            throw new AnalysisTypeException(position, "expected identifier");
        }

        if (!iterator.hasNext()) {
            return new LinkedGenericDefine(name);
        }
        // 结构拆分, 按照&
        boolean multiple = skipIf(iterator, Operator.MULTIPLE_TYPE);
        SourceString next = CollectionUtil.getNext(iterator);
        if (next == null) {
            throw new CompilerException("impossible", new NullPointerException());
        }
        position = next.getPosition();
        List<List<SourceString>> splitWithAnd = new ArrayList<>();
        LinkedGenericDefine.Using defaultType = splitDefineWithAndReturnDefault(iterator, position, splitWithAnd);
        // 分析被&拆分的各个结构
        LinkedGenericDefine.Using lower = null;
        List<LinkedGenericDefine.Using> upper = new ArrayList<>();
        List<LocalParameterizedLink[]> constructorParameters = new ArrayList<>();
        boolean isUpper = false;
        for (List<SourceString> part : splitWithAnd) {
            ListIterator<SourceString> partIterator = part.listIterator();
            if (skipIf(partIterator, Keyword.EXTENDS) || isUpper && nextIsIdentifier(partIterator)) {
                upper.add(LinkTypeFactory.parameterizedLink(partIterator));
                isUpper = true;
                continue;
            }
            isUpper = false;
            if (skipIf(partIterator, Keyword.SUPER)) {
                if (lower == null) {
                    lower = LinkTypeFactory.parameterizedLink(partIterator);
                } else {
                    throw new AnalysisTypeException(
                            partIterator.previous().getPosition(),
                            "Only one lower bound is allowed"
                    );
                }
            } else if (skipIf(partIterator, Keyword.NEW)) {
                SourceString previous = CollectionUtil.getPrevious(partIterator);
                if (previous == null) {
                    throw new CompilerException("impossible", new NullPointerException());
                }
                constructorParameters.add(forLocalGenericListCircleUse(partIterator));
            } else if (skipIf(partIterator, Operator.COMMA)) {
                break;
            } else {
                throw new AnalysisTypeException(partIterator.previous().getPosition(), "expected ,");
            }
        }
        return new LinkedGenericDefine(name, multiple, constructorParameters, defaultType, lower,
                upper.toArray(LinkedGenericDefine.Using[]::new)
        );
    }

    /**
     * @return default nullable for no default
     */
    private static LinkedGenericDefine.Using splitDefineWithAndReturnDefault(
            ListIterator<SourceString> iterator,
            SourcePosition spForEmpty,
            List<List<SourceString>> splitWithAnd) {
        LinkedGenericDefine.Using defaultType = null;
        List<SourceString> each = new ArrayList<>();
        while (iterator.hasNext()) {
            if (defaultType != null) {
                throw new AnalysisTypeException(
                        iterator.previous().getPosition(),
                        "default type should at the last"
                );
            }
            if (skipIf(iterator, Operator.BITWISE_AND)) {
                splitWithAnd.add(each);
                each = new ArrayList<>();
            } else if (skipIf(iterator, Operator.ASSIGN)) {
                if (each.isEmpty()) {
                    throw new AnalysisTypeException(iterator.previous().getPosition(), "empty is illegal");
                }
                splitWithAnd.add(each);
                defaultType = LinkTypeFactory.parameterizedLink(iterator);
            } else {
                each.add(iterator.next());
            }
        }
        if (each.isEmpty()) {
            throw new AnalysisTypeException(
                    iterator.hasPrevious() ? iterator.previous().getPosition() : spForEmpty,
                    "expected end with `,`, but not `&`"
            );
        }
        splitWithAnd.add(each);
        return defaultType;
    }

    /**
     * {@code <const result[0],const final result[1], final result[2],...>}
     */
    public static LocalParameterizedLink[] forLocalGenericListCircleUse(
            ListIterator<SourceString> iterator) {
        if (!iterator.hasNext()) {
            return new LocalParameterizedLink[0];
        }
        List<LocalParameterizedLink> result = new ArrayList<>();
        if (!skipIf(iterator, Operator.GENERIC_LIST_PRE)) {
            return new LocalParameterizedLink[0];
        }
        while (iterator.hasNext()) {
            //  constMark, 不允许final
            SourcePosition constMark = null;
            if (CollectionUtil.nextIs(iterator, ss -> Keyword.CONST.equals(ss.getValue()))) {
                constMark = iterator.next().getPosition();
            }
            LinkedGenericDefine.Using type = LinkTypeFactory.parameterizedLink(iterator);
            result.add(new LocalParameterizedLink(constMark, null, type));
            if (!iterator.hasNext()) {
                throw new AnalysisTypeException(iterator.previous().getPosition(), "not complete generic");
            }
            if (skipIf(iterator, Operator.GENERIC_LIST_POST)) {
                return result.toArray(LocalParameterizedLink[]::new);
            } else if (!skipIf(iterator, Operator.COMMA)) {
                throw new AnalysisTypeException(iterator.next().getPosition(), "not complete: expected comma");
            }
        }
        throw new AnalysisTypeException(iterator.previous().getPosition(), "not complete generic");
    }

    private static boolean skipIf(ListIterator<SourceString> iterator, Operator target) {
        return CollectionUtil.skipIf(
                iterator,
                ss -> ss.getType() == SourceType.OPERATOR && target.nameEquals(ss.getValue())
        );
    }

    private static boolean skipIf(ListIterator<SourceString> iterator, Keyword target) {
        return CollectionUtil.skipIf(
                iterator,
                ss -> ss.getType() == SourceType.KEYWORD && target.equals(ss.getValue())
        );
    }

    private static boolean nextIsIdentifier(ListIterator<SourceString> iterator) {
        return CollectionUtil.nextIs(iterator, ss -> ss.getType() == SourceType.IDENTIFIER);
    }

    public static void main(String[] args) {

    }
}
