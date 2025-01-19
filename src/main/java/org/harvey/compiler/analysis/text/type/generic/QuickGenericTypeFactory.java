package org.harvey.compiler.analysis.text.type.generic;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.io.source.SourceString;

import java.util.LinkedList;
import java.util.List;

/**
 * 构造{@link GenericType}, 不对构造进行检查, 以加快解析速度 <br>
 * 用于将已经存在于编译版半成品文件中的STC快速转换成{@link GenericType}的形式<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 11:52
 */
public class QuickGenericTypeFactory {
    public static final Operator COMMA = Operator.COMMA;
    private static final Operator GENERIC_LIST_POST = Operator.GENERIC_LIST_POST;
    private static final Operator GENERIC_LIST_PRE = Operator.GENERIC_LIST_PRE;
    private static final String EXTENDS = Keyword.EXTENDS.getValue();
    private static final String SUPER = Keyword.SUPER.getValue();
    private static final Operator ASSIGN = Operator.ASSIGN;

    private QuickGenericTypeFactory() {
    }

    public static GenericType create(SourceTextContext context, NormalType parent) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        SourceString first = context.removeFirst();
        context.removeFirst();
        context.removeLast();
        List<GenericArgument> result = new LinkedList<>();
        while (!context.isEmpty()) {
            SourceTextContext eachDeclare = new SourceTextContext();
            int depth = 0;
            while (!context.isEmpty()) {
                SourceString each = context.removeFirst();
                String value = each.getValue();
                if (GENERIC_LIST_PRE.nameEquals(value)) {
                    depth++;
                } else if (GENERIC_LIST_POST.nameEquals(value)) {
                    depth--;
                }
                if (depth == 0 && COMMA.nameEquals(value)) {
                    break;
                }
                eachDeclare.add(each);
            }
            result.add(QuickGenericTypeFactory.createOne(eachDeclare));
        }
        return new GenericType(first, parent, result.toArray(new GenericArgument[]{}));
    }


    public static GenericArgument createOne(SourceTextContext eachDeclare) {
        int superIndex = -1;
        int extendsIndex = -1;
        int assignIndex = -1;
        int i = 0;
        for (SourceString ss : eachDeclare) {
            String value = ss.getValue();
            if (SUPER.equals(value)) {
                superIndex = i;
            } else if (EXTENDS.equals(value)) {
                extendsIndex = i;
            } else if (ASSIGN.nameEquals(value)) {
                assignIndex = i;
            }
            i++;
        }
        return departToGenericType(eachDeclare, superIndex, assignIndex, extendsIndex);
    }


    /**
     * @see GenericTypeFactory#createOne(SourceTextContext) 以这个为准
     */
    @SuppressWarnings("DuplicatedCode")
    public static GenericArgument departToGenericType(SourceTextContext eachDeclare, int superIndex, int assignIndex,
                                                      int extendsIndex) {
        GenericType lower = superIndex != -1 ? validBound(eachDeclare.subContext(0, superIndex)) : null;
        GenericType defaultGeneric = assignIndex != -1 ? validBound(eachDeclare.subContext(assignIndex + 1)) : null;
        GenericType upper = null;
        if (extendsIndex != -1) {
            int end = assignIndex != -1 ? assignIndex : eachDeclare.size();
            upper = validBound(eachDeclare.subContext(extendsIndex + 1, end));
        }
        int start = superIndex != -1 ? superIndex + 1 : 0;
        int end = extendsIndex != -1 ? extendsIndex : (assignIndex != -1 ? assignIndex : eachDeclare.size());
        return new GenericArgument(eachDeclare.subContext(start, end).getFirst(), upper, lower, defaultGeneric);
    }


    private static GenericType validBound(SourceTextContext sourceStrings) {
        GenericType result = create(sourceStrings, null);
        return new GenericType(result.getName(), result.getArgs());
    }


}
