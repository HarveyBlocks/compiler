package org.harvey.compiler.analysis.text.type.generic;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.LinkedList;
import java.util.List;

/**
 * 构造{@link GenericType}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 11:52
 */
public class GenericTypeFactory {
    public static final String GENERIC_WILDCARD = SourceFileConstant.GENERIC_WILDCARD;
    public static final Operator COMMA = Operator.COMMA;
    private static final Operator GENERIC_LIST_POST = Operator.GENERIC_LIST_POST;
    private static final Operator GENERIC_LIST_PRE = Operator.GENERIC_LIST_PRE;
    private static final String EXTENDS = Keyword.EXTENDS.getValue();
    private static final String SUPER = Keyword.SUPER.getValue();
    private static final Operator ASSIGN = Operator.ASSIGN;

    private GenericTypeFactory() {
    }

    /**
     * 将结构进行分解, 但是不进行使用声明正确与否的解析
     */
    public static GenericType create(SourceTextContext context, NormalType parent) {
        // 由于是下届, 上下届的概念来源于继承, 只有类才能继承
        // 所以基本数据类型不能作为上下界
        if (context == null || context.isEmpty()) {
            return null;
        }
        SourceString first = context.removeFirst();
        if (first.getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(first.getPosition(),
                    first.getType() + " is illegal here, expected identifier");
        }
        if (context.isEmpty()) {
            throw new CompilerException(
                    "Class declarations that are not generics are not suitable for this construction");
        }
        SourceString genericPre = context.removeFirst();
        SourceString genericPost = context.removeLast();
        if (!GENERIC_LIST_PRE.nameEquals(genericPre.getValue())) {
            throw new AnalysisExpressionException(genericPre.getPosition(),
                    "Expect " + Operator.GENERIC_LIST_PRE.getName());
        }
        if (!GENERIC_LIST_POST.nameEquals(genericPost.getValue())) {
            throw new AnalysisExpressionException(genericPost.getPosition(),
                    "Expect " + Operator.GENERIC_LIST_POST.getName());
        }
        if (context.isEmpty()) {
            // 区别A< > (不合法) 和 A (合法)
            throw new AnalysisExpressionException(genericPre.getPosition(), genericPost.getPosition(),
                    "Empty generic argument list is not allowed");
        }
        List<GenericArgument> result = new LinkedList<>();
        while (!context.isEmpty()) {
            int depth = 0;
            SourceTextContext eachDeclare = new SourceTextContext();
            SourcePosition sp = SourcePosition.UNKNOWN;
            while (!context.isEmpty()) {
                SourceString each = context.removeFirst();
                sp = each.getPosition();
                String value = each.getValue();
                if (GENERIC_LIST_POST.nameEquals(value)) {
                    depth++;
                } else if (GENERIC_LIST_POST.nameEquals(value)) {
                    depth--;
                }
                if (depth < 0) {
                    throw new AnalysisExpressionException(sp, "generic list circle matching is incorrect");
                }
                if (depth == 0 && COMMA.nameEquals(value)) {
                    break;
                }
                eachDeclare.add(each);
            }
            if (eachDeclare.isEmpty()) {
                throw new AnalysisExpressionException(sp, "Empty generic declare is not allowed");
            }
            result.add(GenericTypeFactory.createOne(eachDeclare));
        }
        // 检查result中, assign总是在最后出现, 而不是中途出现又消失,这样不利于赋默认值
        assertDefaultAssignAtLast(result);
        return new GenericType(first, parent, result.toArray(new GenericArgument[]{}));
    }

    /**
     * 检查result中, assign总是在最后出现, 而不是中途出现又消失,这样不利于赋默认值
     *
     * @param result 泛型参数列表
     */
    private static void assertDefaultAssignAtLast(List<GenericArgument> result) {
        boolean defaultAppeared = false;
        for (GenericArgument arg : result) {
            if (arg.getDefaultGeneric() != null) {
                defaultAppeared = true;
            } else if (defaultAppeared) {
                // 如果defaultAppeared已经未true
                // 还是遇到了arg.getDefaultGeneric() == null的情况
                // 就是预见了违法的情况了
                throw new AnalysisExpressionException(arg.getName().getPosition(),
                        "The definition of the default value should be placed at the end of the list of generic parameters");
            }
        }
    }


    public static GenericArgument createOne(SourceTextContext eachDeclare) {
        SourcePosition defaultPosition = eachDeclare.getFirst().getPosition();
        // [] super [] extends [] = []
        int superIndex = -1;
        int extendsIndex = -1;
        int assignIndex = -1;
        int i = 0;
        for (SourceString ss : eachDeclare) {
            String value = ss.getValue();
            if (SUPER.equals(value)) {
                if (superIndex != -1) {
                    throw new AnalysisExpressionException(ss.getPosition(), "multiple super is illegal here");
                }
                superIndex = i;

            } else if (EXTENDS.equals(value)) {
                if (extendsIndex != -1) {
                    throw new AnalysisExpressionException(ss.getPosition(), "multiple extends is illegal here");
                }
                extendsIndex = i;

            } else if (ASSIGN.nameEquals(value)) {
                if (assignIndex != -1) {
                    throw new AnalysisExpressionException(ss.getPosition(), "multiple assign is illegal here");
                }
                assignIndex = i;

            }
            i++;
        }
        if (!isValidIndex(superIndex, extendsIndex, assignIndex)) {
            // 顺序有问题
            throw new AnalysisExpressionException(defaultPosition, "Illegal sign order");
        }
        //  A<T extends List<? extends SourceString>>
        // 有super, 就有下限
        GenericType lower = superIndex != -1 ? validBound(eachDeclare.subContext(0, superIndex)) : null;
        // 有=就有默认值
        GenericType defaultGeneric = assignIndex != -1 ? validBound(eachDeclare.subContext(assignIndex + 1)) : null;
        GenericType upper = null;
        if (extendsIndex != -1) {
            // 有Upper
            // Lower super T extends upper = Default;
            int end = assignIndex != -1 ? assignIndex : eachDeclare.size();
            upper = validBound(eachDeclare.subContext(extendsIndex + 1, end));
        }
        int start = superIndex != -1 ? superIndex + 1 : 0;
        int end = extendsIndex != -1 ? extendsIndex : (assignIndex != -1 ? assignIndex : eachDeclare.size());
        SourceString name = validName(eachDeclare.subContext(start, end), defaultPosition);
        return new GenericArgument(name, upper, lower, defaultGeneric);
    }

    private static boolean isValidIndex(int superIndex, int extendsIndex, int assignIndex) {
        if (superIndex == -1 && extendsIndex == -1 || superIndex == -1 && assignIndex == -1 ||
                superIndex != -1 && extendsIndex == -1 && assignIndex == -1) {
            return true;
        }
        if (superIndex == -1) {
            return extendsIndex < assignIndex;
        }
        if (extendsIndex == -1) {
            return superIndex < assignIndex;
        }
        if (assignIndex == -1) {
            return superIndex < extendsIndex;
        }
        return superIndex < extendsIndex && extendsIndex < assignIndex;
    }

    private static SourceString validName(SourceTextContext name, SourcePosition defaultPosition) {
        if (name == null || name.isEmpty()) {
            throw new AnalysisExpressionException(defaultPosition, " Identifier is needed");
        }
        if (name.size() >= 2) {
            throw new AnalysisExpressionException(name.getFirst().getPosition(), name.getLast().getPosition(),
                    " only one identifier is allowed");
        }
        return name.getFirst();
    }

    private static GenericType validBound(SourceTextContext sourceStrings) {
        // 上下届的概念来源于继承, 只有类才能继承
        // 所以基本数据类型不能作为上下界
        // X<T extends A<M<M>,M<N>>>
        GenericType result = create(sourceStrings, null);

        // 上下界的定义不能再复杂了, 只能有名, 不能有上下界和默认值
        if (result.getArgs() == null) {
            return new GenericType(result.getName(), null);
        }
        GenericArgument[] args = result.getArgs();
        for (GenericArgument arg : args) {
            //  A<T extends List<? extends SourceString>>
            SourceString name = arg.getName();
            // 通配符可以有上下限, 但不能有DefaultGeneric
            if (arg.getDefaultGeneric() != null) {
                throw new AnalysisExpressionException(name.getPosition(),
                        "default generic is illegal if the identifier is not the generic wildcard `" +
                                GENERIC_WILDCARD + "` in bounds");
            }
            if (GENERIC_WILDCARD.equals(name.getValue())) {
                // 通配符可以有上下限, 但不能有DefaultGeneric
                continue;
            }
            // 不可在泛型范围的声明中声明新的泛型变量
            // 如果不是新的泛型变量, 为什么要定义上下界?
            if (arg.getLower() != null) {
                throw new AnalysisExpressionException(name.getPosition(),
                        "lower bound is illegal if the identifier is not the generic wildcard `" + GENERIC_WILDCARD +
                                "` in bounds");
            }
            if (arg.getUpper() != null) {
                throw new AnalysisExpressionException(name.getPosition(),
                        "upper bound is illegal if the identifier is not the generic wildcard `" + GENERIC_WILDCARD +
                                "` in bounds");
            }
        }
        return new GenericType(result.getName(), args);
    }


}
