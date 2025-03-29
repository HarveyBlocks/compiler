package org.harvey.compiler.text.context;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.reflect.VieConstructor;
import org.harvey.compiler.exception.CompileFileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.harvey.compiler.text.depart.SimpleDepartedBodyFactory.*;

/**
 * 源码文本进行初始分解后的样子, 把每一个结构都分开了, 然后再进行下面的解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:13
 */
public class SourceTextContext extends LinkedList<SourceString> {
    private static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    private static final String AT_INDEX_POST_NAME = Operator.ARRAY_AT_POST.getName();
    private static final String AT_INDEX_PRE_NAME = Operator.ARRAY_AT_PRE.getName();
    private static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    private static final String PARENTHESES_POST_NAME = Operator.PARENTHESES_POST.getName();
    private static final String PARENTHESES_PRE_NAME = Operator.PARENTHESES_PRE.getName();
    private static final String COMMA_NAME = Operator.COMMA.getName();
    private static final String ASSIGN_NAME = Operator.ASSIGN.getName();

    public SourceTextContext(Collection<SourceString> c) {
        super(c);
    }

    public SourceTextContext() {
        super();
    }


    public static SourceTextContext of(SourceString... value) {
        if (value == null) {
            return null;
        }
        SourceTextContext context = new SourceTextContext();
        for (SourceString sourceString : value) {
            context.addLast(sourceString);
        }
        return context;
    }

    public static SourceTextContext empty() {
        return new SourceTextContext();
    }

    public static void legalTypeAssert(Iterable<SourceString> it, Set<SourceType> legalType) {
        if (legalType == null || legalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            SourceType type = ss.getType();
            if (!legalType.contains(type)) {
                throw new CompilerException("Illegal text string type: " + type, new IllegalStateException());
            }
        }
    }

    public static void illegalTypeAssert(Iterable<SourceString> it, Set<SourceType> illegalType) {
        if (illegalType == null || illegalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            SourceType type = ss.getType();
            if (illegalType.contains(type)) {
                throw new CompilerException("Illegal text string type " + type, new IllegalStateException());
            }
        }
    }

    /**
     * @see CollectionUtil#skipNest(ListIterator, Predicate, Predicate, Supplier)
     */
    public static SourceTextContext skipNest(
            ListIterator<SourceString> it, String pre, String post,
            boolean mustHaveNest) {
        SourceTextContext result;
        try {
            result = CollectionUtil.skipNest(it, ss -> pre.equals(ss.getValue()), ss -> post.equals(ss.getValue()),
                    SourceTextContext::new
            );

        } catch (CompilerException ce) {
            throw new AnalysisExpressionException(it.previous().getPosition(), ce.getMessage());
        }

        if (mustHaveNest && result.isEmpty()) {
            throw new AnalysisException(it.previous().getPosition(), "expected " + pre);
        }
        return result;
    }

    /*
     * 不要以逗号分割, 以等号分割
     * 在第一个等号, 定位, 找下一个等号, 等号前面一定是identifier
     * 对于param, 再前面是type,type前是','
     * 对于declare, 再前面是',', 否则就不正确
     * ','前就是表达式
     * 如果找不到下一个等号, 那么前面的就都是表达式
     * 当然要skip一些东西
     */

    /**
     * @param iterator <br>
     *                 1. map -> id<br>
     *                 2. map -> id = exp<br>
     *                 3. map -> map,map<br>
     */
    public static SourceTextContext skipUntilComma(ListIterator<SourceString> iterator) {
        // ()  直接skipNest
        // []  直接skipNest
        // {}  直接skipNest
        // 如果默认值是给出一个数组咋办
        // <>可能有逗号, 当然, < 可能是 大于小于, 很完蛋
        // 如果要不得不使用>后面是运算符的,>就是GenericPost, 向前的一个>作为start吗?
        // 说到底, 一个default的常量表达式里到底能不能有一个ParameterizedType, 也是一个问题
        // 要怎么找到一个逗号是真的逗号呢?
        // 答案是不用找! 除非你用括号括起来, 否则认为你就是Operator
        SourceTextContext beforeComma = new SourceTextContext();
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            SourceType type = next.getType();
            String value = next.getValue();
            if (type == SourceType.OPERATOR) {
                if (COMMA_NAME.equals(value)) {
                    iterator.previous();
                    return beforeComma;
                } else if (PARENTHESES_PRE_NAME.equals(value)) {
                    iterator.previous();
                    beforeComma.addAll(skipNest(iterator, PARENTHESES_PRE_NAME, PARENTHESES_POST_NAME, true));
                } else if (AT_INDEX_PRE_NAME.equals(value)) {
                    iterator.previous();
                    beforeComma.addAll(skipNest(iterator, AT_INDEX_PRE_NAME, AT_INDEX_POST_NAME, true));
                } else if (PARENTHESES_POST_NAME.equals(value)) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal matching of `{}`");
                } else if (AT_INDEX_POST_NAME.equals(value)) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal matching of `{}`");
                } else {
                    // method的引用,如何分辨
                    // bool l = a[A,X]() , y = a[B]();
                    beforeComma.add(next);
                }
            } else if (type == SourceType.SIGN) {
                if (SENTENCE_END.equals(value)) {
                    throw new AnalysisExpressionException(next.getPosition(), "do not expect ;");
                } else if (BODY_START.equals(value)) {
                    iterator.previous();
                    beforeComma.addAll(skipNest(iterator, BODY_START, BODY_END, true));
                } else if (BODY_END.equals(value)) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal matching of `{}`");
                } else {
                    throw new CompilerException("unknown sign of: " + next);
                }
            } else {
                beforeComma.add(next);
            }
        }
        return beforeComma;
    }


    private static boolean beforeCommaHasNext(ListIterator<SourceString> iterator) {
        return iterator.hasNext() && !CollectionUtil.nextIs(iterator, s -> Operator.COMMA.nameEquals(s.getValue()));
    }


    public SourceTextContext subContext(int from, int to) {
        int i = 0;
        SourceTextContext result = new SourceTextContext();
        for (SourceString sourceString : this) {
            if (i >= to) {
                break;
            }
            if (i >= from) {
                result.add(sourceString);
            }
            i++;
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("`");
        super.forEach(s -> sb.append(s.getValue()).append(" "));
        sb.append("`");
        return sb.toString();
    }

    public SourceTextContext subContext(int from) {
        return subContext(from, this.size());
    }

    /**
     * 抛出一个跨越全文的异常
     */
    public void throwExceptionIncludingAll(String message, Class<? extends CompileFileException> ec) {
        if (this.isEmpty()) {
            return;
        }
        SourceString first = this.pollFirst();
        SourceString last = first;
        while (!this.isEmpty()) {
            last = this.pollFirst();
        }
        throw new VieConstructor<>(ec, SourcePosition.class, SourcePosition.class, String.class).instance(
                first.getPosition(), SourcePosition.moveToEnd(last.getPosition(), last.getValue()), message);
    }


}
