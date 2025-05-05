package org.harvey.compiler.type.generic.link;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.test.SourceContextTestCreator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.link.LinkedGenericDefine.TypeSequential;

import java.util.*;

/**
 * 没有bounds
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-14 22:04
 */
public class LinkTypeFactory {

    public static final String GET_MEMBER_NAME = Operator.GET_MEMBER.getName();
    public static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    public static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    public static final String COMMA_NAME = Operator.COMMA.getName();


    /**
     * pre (pre.raw[0],pre.raw[1],...){
     * (pre.raw[0].generic[0], pre.raw[0].generic[1], ...),
     * (pre.raw[1].generic[0], pre.raw[1].generic[1], ...),
     * ...
     * }
     *
     * @see ParameterizedTypeLink#toSequential()
     */
    public static LinkedGenericDefine.Using parameterizedLink(ListIterator<SourceString> source) {
        LinkedList<ListIterator<SourceString>> queue = new LinkedList<>();
        queue.addLast(source);
        List<TypeSequential> result = new ArrayList<>();
        boolean notExpectGetMemberThenEnd = true;//first is true;
        while (!queue.isEmpty()) {
            ListIterator<SourceString> first = queue.removeFirst();
            createParameterizedLink(first, result, queue, notExpectGetMemberThenEnd);
            notExpectGetMemberThenEnd = false; // inner not legal
        }
        return new LinkedGenericDefine.Using(result);
    }

    private static void createParameterizedLink(
            ListIterator<SourceString> sourceIterator,
            List<TypeSequential> result,
            LinkedList<ListIterator<SourceString>> sourceQueue,
            boolean notExpectGetMemberThenEnd) {
        if (!sourceIterator.hasNext()) {
            return;
        }
        // 1. skip full identifier
        FullIdentifierString rawType = skipFullIdentifier(sourceIterator);
        if (!sourceIterator.hasNext()) {
            // 没有 generic
            // pre.rawType, 没了
            result.add(onlyPath(rawType));
            return;
        }
        // 3. skip [ ], skip DOT, all
        List<Pair<FullIdentifierString, SourceTextContext>> typeSourceList = skipParameterizedLink(
                sourceIterator, rawType, notExpectGetMemberThenEnd);
        // 6. parameter split by comma

        List<Pair<FullIdentifierString, Integer>> sizePairs = new ArrayList<>();
        for (Pair<FullIdentifierString, SourceTextContext> sourcePair : typeSourceList) {
            FullIdentifierString eachRawType = sourcePair.getKey();
            SourceTextContext eachGeneric = sourcePair.getValue();
            if (eachGeneric.isEmpty()) {
                sizePairs.add(new Pair<>(eachRawType, 0));
                continue;
            }
            eachGeneric.removeFirst(); // [
            eachGeneric.removeLast(); // ]
            ListIterator<SourceString> genericIterator = eachGeneric.listIterator();
            int size = 0;
            while (genericIterator.hasNext()) {
                // 6.1. generic parameter split by comma
                SourceTextContext each = SourceTextContext.skipUntilComma(genericIterator);
                if (genericIterator.hasNext()) {
                    // 如果还有, 下一个一定是,
                    if (!CollectionUtil.skipIf(genericIterator, s -> COMMA_NAME.equals(s.getValue()))) {
                        throw new AnalysisTypeException(
                                genericIterator.next().getPosition(), "expected , for generic");
                    }
                    // , 后一定要有下一个
                    if (!genericIterator.hasNext()) {
                        throw new AnalysisTypeException(
                                genericIterator.previous().getPosition(), "not except empty");
                    }
                }
                if (each.isEmpty()) {
                    // 不能是空list
                    throw new AnalysisTypeException(
                            genericIterator.previous().getPosition(), "except more for generic");
                }
                size++;
                // 7. each to be created
                // 修复递归
                sourceQueue.add(each.listIterator());
            }
            sizePairs.add(new Pair<>(eachRawType, size));
        }
        result.add(new TypeSequential(sizePairs));
    }

    public static FullIdentifierString skipFullIdentifier(ListIterator<SourceString> sourceIterator) {
        LinkedList<SourcePosition> positions = new LinkedList<>();
        LinkedList<String> identifiers = new LinkedList<>();
        boolean exceptIdentifier = true;
        while (sourceIterator.hasNext()) {
            SourceString next = sourceIterator.next();
            if (exceptIdentifier && next.getType() == SourceType.IDENTIFIER) {
                identifiers.add(next.getValue());
                positions.add(next.getPosition());
            } else if (exceptIdentifier || !GET_MEMBER_NAME.equals(next.getValue())) {
                sourceIterator.previous();
                break;
            }
            exceptIdentifier = !exceptIdentifier;
        }
        return toFull(positions, identifiers);
    }

    private static List<Pair<FullIdentifierString, SourceTextContext>> skipParameterizedLink(
            ListIterator<SourceString> sourceIterator,
            FullIdentifierString rawType,
            boolean notExpectGetMemberThenEnd) {
        // 3.1. skip first
        List<Pair<FullIdentifierString, SourceTextContext>> typeSourceList = new ArrayList<>();
        SourceTextContext generic = SourceTextContext.skipNest(sourceIterator, GENERIC_LIST_PRE_NAME,
                GENERIC_LIST_POST_NAME, !notExpectGetMemberThenEnd
        );
        if (notExpectGetMemberThenEnd) {
            if (generic.isEmpty()) {
                return List.of(new Pair<>(rawType, generic));
            }
        }
        typeSourceList.add(new Pair<>(rawType, generic));
        // 3.2 then DOT ID generic, DOT ID generic, ...
        while (sourceIterator.hasNext()) {
            SourceString exceptDot = sourceIterator.next();
            if (!GET_MEMBER_NAME.equals(exceptDot.getValue())) {
                if (!notExpectGetMemberThenEnd) {
                    throw new AnalysisTypeException(exceptDot.getPosition(), "except get member operator");
                } else {
                    sourceIterator.previous();
                    return typeSourceList;
                }
            }
            if (!sourceIterator.hasNext()) {
                throw new AnalysisTypeException(exceptDot.getPosition(), "except more identifier");
            }
            SourceString exceptRawType = sourceIterator.next();
            if (exceptRawType.getType() != SourceType.IDENTIFIER) {
                throw new AnalysisTypeException(exceptRawType.getPosition(), "except an identifier");
            }
            FullIdentifierString nextRawType = growFullPath(rawType, exceptRawType);
            SourceTextContext nextGeneric = SourceTextContext.skipNest(
                    sourceIterator, GENERIC_LIST_PRE_NAME, GENERIC_LIST_POST_NAME, false);
            typeSourceList.add(new Pair<>(nextRawType, nextGeneric));
        }
        return typeSourceList;
    }

    private static FullIdentifierString growFullPath(
            FullIdentifierString rawType, SourceString exceptRawType) {
        String[] newPath = Arrays.copyOf(rawType.getFullname(), rawType.getFullname().length + 1, String[].class);
        newPath[newPath.length - 1] = exceptRawType.getValue();
        SourcePosition[] newPositionList = Arrays.copyOf(
                rawType.getPositionList(), rawType.getPositionList().length + 1, SourcePosition[].class);
        newPositionList[newPositionList.length - 1] = exceptRawType.getPosition();
        return new FullIdentifierString(newPositionList, newPath);
    }


    private static TypeSequential onlyPath(FullIdentifierString rawType) {
        ArrayList<Pair<FullIdentifierString, Integer>> typeList = new ArrayList<>();
        typeList.add(new Pair<>(rawType, 0));
        return new TypeSequential(typeList);
    }

    private static FullIdentifierString toFull(List<SourcePosition> positions, List<String> identifiers) {
        if (positions.size() != identifiers.size()) {
            throw new CompilerException("Different size on create full identifier");
        }
        SourcePosition[] positionsArray = positions.toArray(new SourcePosition[0]);
        String[] identifiersArray = identifiers.toArray(new String[0]);
        return new FullIdentifierString(positionsArray, identifiersArray);
    }

    public static void main(String[] args) {
        SourceTextContext source = SourceContextTestCreator.newSource(
                " a . b . c . file_name  . \n" +
                " StaticClass . NonStaticInner [ T1 , T2 [ T2_1 , T2_2 [ T222 ] , T2_3 ] , T3 ] \n" +
                " . InnerInner [ T4 , T5 [ T6 , T7 ]  ] . Last   int int int"
        );
        source = CoreCompiler.registerChain().execute(source);
        ListIterator<SourceString> source1 = source.listIterator();
        List<? extends ParameterizedTypeLink.Sequential<FullIdentifierString>> sequential = LinkTypeFactory.parameterizedLink(
                source1).getSequential();
        System.out.println(source1.next());
        ParameterizedTypeLink<FullIdentifierString> tree = ParameterizedTypeLink.toTree(sequential);
        List<ParameterizedTypeLink.Sequential<FullIdentifierString>> newSequential = tree.toSequential();
        for (int i = 0; i < sequential.size(); i++) {
            System.out.println(sequential.get(i).getList());
            System.out.println(newSequential.get(i).getList());
            System.out.println("===============================");
        }
        System.out.println("Hello world");
    }
}
