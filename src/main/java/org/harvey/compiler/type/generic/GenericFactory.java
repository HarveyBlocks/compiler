package org.harvey.compiler.type.generic;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.ExceptionUtil;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.*;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.LocalParameterizedType;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.*;

/**
 * TODO
 * 如果解析过程中出现了GenericDefine怎么半?又没有完成对GenericDefine的注册
 * 如果找不到, 长度还只有1, 那就去找Identifier
 * 补全identifier的工作
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-01 19:39
 */
public class GenericFactory {

    private static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    private static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    private static final String COMMA_NAME = Operator.COMMA.getName();

    private GenericFactory() {
    }

    public static ParameterizedType<ReferenceElement>[] genericList(
            SourcePosition position, ListIterator<SourceString> iterator, IdentifierManager manager) {
        ReferenceElement fake = new ReferenceElement(position, ReferenceType.IDENTIFIER, -1);
        return parameterizedType(fake, iterator, manager).getChildren();
    }

    /**
     * rawType<type,type,type>
     * 一定要是Generic开头, 不是的报错, Generic完了, iterator不一定要完, 也可以不完
     *
     * @param iterator <type,type,type>
     */
    public static ParameterizedType<ReferenceElement> parameterizedType(
            ReferenceElement rawType, ListIterator<SourceString> iterator, IdentifierManager manager) {
        if (!iterator.hasNext()) {
            ParameterizedType<ReferenceElement> result = new ParameterizedType<>(rawType);
            result.setReadOnly(true);
            return result;
        }
        SourcePosition position = genericPreCheck(iterator, rawType.getPosition());
        Stack<ParameterizedType<ReferenceElement>> stack = new Stack<>();
        stack.push(new ParameterizedType<>(rawType));
        while (iterator.hasNext()) {
            RawType element = rawType(iterator);
            position = element.getPosition();
            ReferenceElement reference = rawType2Reference(element, manager);
            ParameterizedType<ReferenceElement> parameter = toParameter(reference);
            ParameterizedType<ReferenceElement> top = stack.pop();
            top.addParameter(parameter);
            stack.push(top);
            if (skipIf(iterator, Operator.COMMA)) {
                continue;
            }
            if (skipIf(iterator, Operator.GENERIC_LIST_PRE)) {
                if (top.getRawType().getType() == ReferenceType.GENERIC_IDENTIFIER) {
                    throw new AnalysisExpressionException(
                            iterator.previous().getPosition(), "illegal for generic parameter in generic define");
                }
                stack.push(parameter);
                continue;
            }
            if (element instanceof FullIdentifierString && skipIf(iterator, Operator.GENERIC_LIST_POST)) {
                ParameterizedType<ReferenceElement> pop = stack.pop();
                if (stack.empty()) {
                    pop.setReadOnly(true);
                    return pop;
                } else {
                    continue;
                }
            }
            throw new AnalysisExpressionException(position, "not expected what after type");
        }
        throw new AnalysisExpressionException(position, "not a generic message: not completed");
    }


    public static ReferenceElement rawType2Reference(RawType element, IdentifierManager manager) {
        if (element instanceof KeywordString) {
            return ReferenceElement.of(((KeywordString) element));
        } else if (element instanceof FullIdentifierString) {
            return manager.getReferenceAndAddIfNotExist((FullIdentifierString) element);
        } else if (element instanceof IdentifierString) {
            IdentifierString identifierString = (IdentifierString) element;
            return manager.getReferenceAndAddIfNotExist(
                    new FullIdentifierString(identifierString.getPosition(), identifierString.getValue()));
        } else {
            throw new CompilerException("unknown using element type");
        }

    }

    public static boolean genericPreCheck(ListIterator<SourceString> iterator) {
        return nextIs(iterator);
    }

    /**
     * skip first `<`
     *
     * @return first's position
     */
    private static SourcePosition genericPreCheck(ListIterator<SourceString> iterator, SourcePosition spForEmpty) {
        if (!iterator.hasNext()) {
            throw new AnalysisExpressionException(spForEmpty, "genericMessage can not be empty, expected: <");
        }
        SourceString first = iterator.next();
        if (first.getType() != SourceType.OPERATOR || !GENERIC_LIST_PRE_NAME.equals(first.getValue())) {
            throw new AnalysisExpressionException(first.getPosition(), "not a generic message");
        }
        return first.getPosition();
    }


    private static ParameterizedType<ReferenceElement> toParameter(
            ReferenceElement reference) {
        return new ParameterizedType<>(reference);
    }


    private static boolean skipIf(ListIterator<SourceString> iterator, Operator target) {
        return CollectionUtil.skipIf(iterator,
                ss -> ss.getType() == SourceType.OPERATOR && target.nameEquals(ss.getValue())
        );
    }

    private static boolean nextIs(ListIterator<SourceString> iterator) {
        return CollectionUtil.nextIs(iterator,
                ss -> ss.getType() == SourceType.OPERATOR && Operator.GENERIC_LIST_PRE.nameEquals(ss.getValue())
        );
    }

    private static boolean skipIf(ListIterator<SourceString> iterator, Keyword target) {
        return CollectionUtil.skipIf(iterator,
                ss -> ss.getType() == SourceType.KEYWORD && target.equals(ss.getValue())
        );
    }

    private static boolean nextIsIdentifier(ListIterator<SourceString> iterator) {
        return CollectionUtil.nextIs(iterator, ss -> ss.getType() == SourceType.IDENTIFIER);
    }

    public static GenericDefine genericForDefine(
            ReferenceElement name, SourceTextContext define, IdentifierManager manager) {
        // <T extends BaseClass & BaseInterfaces & super LowerInterface & new<int,int> & new<long> = DefaultType >

        ListIterator<SourceString> iterator = define.listIterator();
        if (!iterator.hasNext()) {
            return new GenericDefine(name);
        }
        // 结构拆分, 按照&
        boolean multiple = skipIf(iterator, Operator.MULTIPLE_TYPE);
        SourceString next = CollectionUtil.getNext(iterator);
        if (next == null) {
            throw new CompilerException("impossible", new NullPointerException());
        }
        SourcePosition position = next.getPosition();
        List<List<SourceString>> splitWithAnd = new ArrayList<>();
        ParameterizedType<ReferenceElement> defaultType = splitDefineWithAnd(iterator, manager, position, splitWithAnd);
        // 分析被&拆分的各个结构
        ParameterizedType<ReferenceElement> lower = null;
        List<ParameterizedType<ReferenceElement>> upper = new ArrayList<>();
        List<LocalParameterizedType[]> constructorParameters = new ArrayList<>();
        boolean isUpper = false;
        for (List<SourceString> part : splitWithAnd) {
            ListIterator<SourceString> partIterator = part.listIterator();
            if (skipIf(partIterator, Keyword.EXTENDS) || isUpper && nextIsIdentifier(partIterator)) {
                upper.add(parameterizedType(partIterator, manager));
                isUpper = true;
                continue;
            }
            isUpper = false;
            if (skipIf(partIterator, Keyword.SUPER)) {
                if (lower == null) {
                    lower = parameterizedType(partIterator, manager);
                } else {
                    throw new AnalysisExpressionException(partIterator.previous().getPosition(),
                            "Only one lower bound is allowed"
                    );
                }
            } else if (skipIf(partIterator, Keyword.NEW)) {
                SourceString previous = CollectionUtil.getPrevious(partIterator);
                if (previous == null) {
                    throw new CompilerException("impossible", new NullPointerException());
                }
                position = previous.getPosition();
                constructorParameters.add(forLocalGenericListCircleUse(partIterator, manager, position));
            } else {
                throw new AnalysisExpressionException(partIterator.previous().getPosition(), "expected ,");
            }
        }
        return new GenericDefine(name, multiple, constructorParameters, defaultType, lower,
                upper.toArray(ParameterizedType[]::new)
        );
    }

    /**
     * {@code <const result[0],const final result[1], final result[2],...>}
     */
    public static LocalParameterizedType[] forLocalGenericListCircleUse(
            ListIterator<SourceString> iterator, IdentifierManager manager, SourcePosition spForEmpty) {
        genericPreCheck(iterator, spForEmpty);
        List<LocalParameterizedType> result = new ArrayList<>();
        while (iterator.hasNext()) {
            //  constMark, 不允许final
            SourcePosition constMark = null;
            if (CollectionUtil.nextIs(iterator, ss -> Keyword.CONST.equals(ss.getValue()))) {
                constMark = iterator.next().getPosition();
            }
            ParameterizedType<ReferenceElement> type = parameterizedType(iterator, manager);
            result.add(new LocalParameterizedType(constMark, null, type));
            if (!iterator.hasNext()) {
                throw new AnalysisExpressionException(iterator.previous().getPosition(), "not complete generic");
            }
            if (skipIf(iterator, Operator.GENERIC_LIST_POST)) {
                return result.toArray(LocalParameterizedType[]::new);
            } else if (!skipIf(iterator, Operator.COMMA)) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "not complete: expected comma");
            }
        }
        throw new AnalysisExpressionException(iterator.previous().getPosition(), "not complete generic");
    }


    /**
     * @return default nullable for no default
     */
    private static ParameterizedType<ReferenceElement> splitDefineWithAnd(
            ListIterator<SourceString> iterator,
            IdentifierManager manager,
            SourcePosition spForEmpty,
            List<List<SourceString>> splitWithAnd) {
        ParameterizedType<ReferenceElement> defaultType = null;
        List<SourceString> each = new ArrayList<>();
        while (iterator.hasNext()) {
            if (defaultType != null) {
                throw new AnalysisExpressionException(iterator.previous().getPosition(),
                        "default type should at the last"
                );
            }
            if (skipIf(iterator, Operator.BITWISE_AND)) {
                splitWithAnd.add(each);
                each = new ArrayList<>();
            } else if (skipIf(iterator, Operator.ASSIGN)) {
                if (each.isEmpty()) {
                    throw new AnalysisExpressionException(iterator.previous().getPosition(), "empty is illegal");
                }
                splitWithAnd.add(each);
                defaultType = parameterizedType(iterator, manager);
            } else {
                each.add(iterator.next());
            }
        }
        if (each.isEmpty()) {
            throw new AnalysisExpressionException(
                    iterator.hasPrevious() ? iterator.previous().getPosition() : spForEmpty,
                    "expected end with `,`, but not `&`"
            );
        }
        splitWithAnd.add(each);
        return defaultType;
    }


    public static ParameterizedType<ReferenceElement> parameterizedType(
            ListIterator<SourceString> iterator, IdentifierManager manager) {
        RawType rawType = rawType(iterator);
        ReferenceElement reference = rawType2Reference(rawType, manager);
        if (!iterator.hasNext()) {
            ParameterizedType<ReferenceElement> result = new ParameterizedType<>(reference);
            result.setReadOnly(true);
            return result;
        }
        ParameterizedType<ReferenceElement> result = parameterizedType(reference, iterator, manager);
        result.setReadOnly(true);
        return result;
    }


    /**
     * 一定要有type, 没有type报错
     */
    public static RawType rawType(ListIterator<SourceString> iterator) {
        ExceptionUtil.iteratorHasNext(iterator, "type can not be empty");
        SourceString identifier = iterator.next();
        Keyword mayKeyword = Keyword.get(identifier.getValue());
        if (identifier.getType() == SourceType.IDENTIFIER) {
            return ExpressionFactory.fullIdentifier(identifier.getPosition(), identifier.getValue(), iterator);
        } else if (identifier.getType() == SourceType.KEYWORD && Keywords.isBasicType(mayKeyword)) {
            if (iterator.hasNext()) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "not basic type");
            }
            String value = identifier.getValue();
            if (Keyword.VAR.equals(value)) {
                throw new AnalysisExpressionException(identifier.getPosition(), "var is illegal here");
            }
            if (Keyword.VOID.equals(value)) {
                throw new AnalysisExpressionException(identifier.getPosition(), "var is illegal here");
            }
            return new KeywordString(identifier.getPosition(), mayKeyword);
        } else {
            throw new AnalysisExpressionException(identifier.getPosition(), "not a type");
        }
    }

    /**
     * @param genericDefineIterator <T [Define][,...] > 如果不是泛型, 直接报错, 所以要{@link #genericPreCheck(ListIterator)}检查
     * @return pairs of <T,Defines>
     */
    public static List<Pair<IdentifierString, SourceTextContext>> defineSourceDepart(
            ListIterator<SourceString> genericDefineIterator) {
        if (!genericDefineIterator.hasNext()) {
            return Collections.emptyList();
        }
        SourcePosition position = genericPreCheck(genericDefineIterator, SourcePosition.UNKNOWN);
        List<Pair<IdentifierString, SourceTextContext>> result = new ArrayList<>();
        while (genericDefineIterator.hasNext()) {
            SourceString expectedName = genericDefineIterator.next();
            SourceType sourceType = expectedName.getType();
            position = expectedName.getPosition();
            IdentifierString genericName;
            if (sourceType == SourceType.IGNORE_IDENTIFIER) {
                genericName = new IdentifierString(position);
            } else if (sourceType == SourceType.IDENTIFIER) {
                genericName = new IdentifierString(position, expectedName.getValue());
            } else {
                throw new AnalysisExpressionException(position, "expected identifier");
            }
            SourceTextContext define = skipOneDefine(genericDefineIterator, position);
            if (!define.isEmpty()) {
                position = define.getLast().getPosition();
            }
            result.add(new Pair<>(genericName, define));
            if (skipIf(genericDefineIterator, Operator.GENERIC_LIST_POST)) {
                return result;
            } else if (!skipIf(genericDefineIterator, Operator.COMMA)) {
                throw new AnalysisExpressionException(position, "expected " + Operator.COMMA.getName());
            }
        }
        throw new AnalysisExpressionException(position, "not a generic message: not completed");
    }

    private static SourceTextContext skipOneDefine(
            ListIterator<SourceString> genericDefineIterator, SourcePosition firstPosition) {
        int inGeneric = 0;
        SourceTextContext result = new SourceTextContext();
        SourcePosition position = firstPosition;
        while (genericDefineIterator.hasNext()) {
            SourceString next = genericDefineIterator.next();
            position = next.getPosition();
            if (next.getType() != SourceType.OPERATOR) {
                result.add(next);
                continue;
            }
            String value = next.getValue();
            if (inGeneric == 0) {
                if (GENERIC_LIST_POST_NAME.equals(value) || COMMA_NAME.equals(value)) {
                    genericDefineIterator.previous();
                    return result;
                }
            } else if (inGeneric < 0) {
                throw new AnalysisExpressionException(position, "Illegal generic list match");
            }
            if (GENERIC_LIST_PRE_NAME.equals(value)) {
                inGeneric++;
            } else if (GENERIC_LIST_POST_NAME.equals(value)) {
                inGeneric--;
            }
            result.add(next);
        }
        throw new AnalysisExpressionException(position, "Illegal generic list match");
    }

    /**
     * 以逗号分割
     *
     * @param iterator iterator.next()==rawType
     * @see #parameterizedType(ListIterator, IdentifierManager)
     */
    public static Pair<RawType, SourceTextContext> skipSourceForUse(ListIterator<SourceString> iterator) {
        RawType element = rawType(iterator);
        SourceTextContext genericList = new SourceTextContext();
        skipGenericList(iterator, genericList);
        return new Pair<>(element, genericList);
    }

    public static void skipGenericList(ListIterator<SourceString> it) {
        skipGenericList(it, null);
    }

    /**
     * 快速跳过, 省去了的写入集合时间
     *
     * @param it it.next()==`<`, 否则没意义
     */
    public static void skipGenericList(ListIterator<SourceString> it, List<SourceString> genericList) {
        CollectionUtil.skipNest(it, ss -> GENERIC_LIST_PRE_NAME.equals(ss.getValue()),
                ss -> GENERIC_LIST_POST_NAME.equals(ss.getValue()), () -> genericList
        );

    }

    public static List<Pair<IdentifierString, SourceTextContext>> defineSourceDepart(
            ListIterator<SourceString> genericDefineStartedIterator, SourceTextContext restPart) {
        List<Pair<IdentifierString, SourceTextContext>> pairs = defineSourceDepart(genericDefineStartedIterator);
        CollectionUtil.restCopy(genericDefineStartedIterator, restPart);
        return pairs;
    }


}
