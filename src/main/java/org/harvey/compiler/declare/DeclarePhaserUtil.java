package org.harvey.compiler.declare;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.core.Permission;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 工具类
 * 函数/方法声明, 类声明, 变量声明, 都OK
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:51
 */
public final class DeclarePhaserUtil {

    public static final String CALLABLE_DECLARE_NAME = Operator.CALLABLE_DECLARE.getName();
    private static final Operator CALL_PRE = Operator.CALL_PRE;

    private DeclarePhaserUtil() {
    }


    @Deprecated
    public static boolean isDeclarableInCallable(SourceTextContext statement) {
        if (statement.size() <= 1) {
            return false;
        }
        SourceString first = statement.getFirst();
        if (first.getType() != SourceStringType.KEYWORD && first.getType() != SourceStringType.IDENTIFIER) {
            // 声明一定是Keyword和Identifier开头的
            return false;
        }
        // 控制结构
        return first.getType() != SourceStringType.KEYWORD || !Keywords.isControlStructure(first.getValue());
        // 可以是String[]的声明
        // 可以是exec()的函数
        // 具有相同的结构, 无法分辨是不是declarable
        // 啊!!!!!!!!!!!!!!!
        // aaa
        // TODO 此方法写得十分寒酸
    }

    public static Declarable statementBasicPhase(SourceTextContext statement) {
        SourcePosition start = statement.getFirst().getPosition();
        ListIterator<SourceString> it = statement.listIterator();
        // 解析作用域
        SourceTextContext permissions = getPermissions(it);
        // 解析修饰[static final sealed const]
        EmbellishSourceString embellish = getEmbellish(it);
        // 解析类型
        SourceTextContext type = getType0(it, start);
        // 解析标识符 一定是identifier
        SourceString identifier = getIdentifier(it, type, start);
        // 剩余
        SourceTextContext attachment = getAttachment(it);
        return new Declarable(start, permissions, embellish, type, identifier, attachment);
    }

    public static SourceTextContext getPermissions(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        SourceTextContext permissions = new SourceTextContext();
        while (it.hasNext()) {
            SourceString next = it.next();
            if (next.getType() != SourceStringType.KEYWORD) {
                it.previous();
                break;
            }
            String value = next.getValue();
            if (!Permission.is(value)) {
                it.previous();
                break;
            }
            permissions.add(next);
        }
        return permissions;
    }

    public static EmbellishSourceString getEmbellish(ListIterator<SourceString> it) {
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
            boolean isEmbellish = true;
            switch (keyword) {
                case CONST:
                    if (embellish.constMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple const");
                    }
                    embellish.constMark = next;
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
                case ABSTRACT:
                    if (embellish.abstractMark != null) {
                        throw new AnalysisExpressionException(next.getPosition(), "multiple abstract");
                    }
                    embellish.abstractMark = next;
                    break;
                default:
                    isEmbellish = false;
            }
            if (!isEmbellish) {
                it.previous();
                break;
            }
        }
        return embellish;
    }

   /* private static SourceTextContext getType(ListIterator<SourceString> it, SourcePosition start) {
        // TODO 为啥要写这个方法?
        return getType0(it, start);
    }*/

    /**
     * 不认为class这种是类型, 括号开头的也不算
     */
    public static SourceTextContext getType(ListIterator<SourceString> it, SourcePosition start) {
        SourceTextContext type0 = getType0(it, start);
        if (type0.size() == 1 && Keywords.isComplexStructure(type0.getFirst().getValue())) {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
        // 括号开头的不算
        if (!type0.isEmpty() && nextIsOperator(type0.listIterator(), Operator.BRACKET_PRE)) {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
        return type0;
    }

    /**
     * 类型<br>
     * 1. Identifier<br>
     * 2. int8 int32, bool等<br>
     * 3. var<br>
     * 4. unsigned int8<br>
     * 5. void func<br>
     * 7. Identifier func<br>
     * 8. (Identifier) func<br>
     * 9. (Identifier1,Identifier2) func<br>
     * 10. (Identifier1,Identifier2) abstract func<br>
     * 11. struct/enum/class/interface<br>
     * 12. abstract class <b><I>非也!!!!!!!!!!!!!!</I></b><br>
     * 13. 类型[]<br>
     * 14. 类型{@code <类型,类型,...>}<br>
     * 15. ...<br>
     * 什么是Type?<br>
     * 在Identifier之前的是Type<br>
     * 什么是Identifier<br>
     * func之后的?<br>
     * class之后的?<br>
     * =之前的?<br>
     * ,之前的?<br>
     * 参数列表之前的?<br>
     */
    private static SourceTextContext getType0(ListIterator<SourceString> it, SourcePosition start) {
        // 迭代器结束之前的?
        SourceTextContext type = new SourceTextContext();
        if (!it.hasNext()) {
            throw new AnalysisExpressionException(start, "type is needed");
        }
        if (nextIsTypeAvailableKeyword(it)) {
            type.add(it.next());
            return type;
        } else if (nextIsIdentifier(it)) {
            SourceString first = it.next();
            type.add(first);
            // 一上来就是Identifier;
            // 只有Identifier后面是Identifier的, 后面的才是声明的主题
            // 本Identifier就是类了, 否则, 这个Identifier也是类型的一部分
            if (!nextIsOperator(it, Operator.GENERIC_LIST_PRE)) {
                return type;
            }
            // 带泛型
            type.addAll(departNest(it, Operator.GENERIC_LIST_PRE, Operator.GENERIC_LIST_POST));
            return type;
        } else if (nextIsOperator(it, Operator.BRACKET_PRE)) {
            return departNest(it, Operator.BRACKET_PRE, Operator.BRACKET_POST);
        } else {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
    }


    private static SourceTextContext departNest(ListIterator<SourceString> it, Operator pre, Operator post) {
        if (!nextIsOperator(it, pre)) {
            return SourceTextContext.EMPTY;
        }

        SourceTextContext nestedStructure = new SourceTextContext();
        nestedStructure.add(it.next()); // 跳过第一个
        int inNest = 1;
        while (it.hasNext()) {
            SourceString next = it.next();
            if (inNest == 0) {
                it.previous();
                break;
            }
            if (isOperator(next, pre)) {
                inNest++;
            } else if (isOperator(next, post)) {
                inNest--;
            }
            if (inNest < 0) {
                throw new AnalysisExpressionException(next.getPosition(), "Illegal matching");
            }
            nestedStructure.add(next);
        }
        return nestedStructure;
    }

    private static boolean nextIsOperator(ListIterator<SourceString> it, Operator operator) {
        return CollectionUtil.nextIs(it, ss -> isOperator(ss, operator));
    }

    private static boolean isOperator(SourceString ss, Operator operator) {
        return ss.getType() == SourceStringType.OPERATOR && operator.nameEquals(ss.getValue());
    }


    /**
     * 迭代器不移动
     */
    private static boolean nextIsIdentifier(ListIterator<SourceString> it) {
        return CollectionUtil.nextIs(it, ss -> ss.getType() == SourceStringType.IDENTIFIER);
    }

    private static boolean nextIsTypeAvailableKeyword(ListIterator<SourceString> it) {
        return CollectionUtil.nextIs(it, ss -> {
            String value = ss.getValue();
            return ss.getType() == SourceStringType.KEYWORD
                    &&
                    (Keywords.isBasicType(value) || Keywords.isComplexStructure(value) || Keyword.ALIAS.equals(value));
        });
    }

    /**
     * 对于枚举的单例成员, 可能没有类型, 但是一定要有标识符
     *
     * @param type 可能会因为没有identifier, 而失去type, 转为identity
     */
    private static SourceString getIdentifier(
            ListIterator<SourceString> it, SourceTextContext type, SourcePosition start) {
        if (!it.hasNext()) {
            // 特别的enum元素的声明, 无返回值
            /*不太对 if (type.size() != 1) {
                throw new AnalysisExpressionException(start, "identifier is expected");
            }*/
            if (type.isEmpty()) {
                throw new AnalysisExpressionException(start, "identifier is expected");
            }
            if (type.size() != 1) {
                throw new AnalysisExpressionException(
                        type.getFirst().getPosition(), type.getLast().getPosition(), "unknown declaration");
            }

            // 暂且认为type为identifier
            SourceString identifier = type.removeLast();
            if (identifier.getType() != SourceStringType.IDENTIFIER) {
                throw new AnalysisExpressionException(
                        identifier.getPosition(), type.getLast().getPosition(), "unknown declaration");
            }
            return identifier;
        }
        SourceString identifier = it.next();
        SourceStringType identifierType = identifier.getType();
        if (identifierType == SourceStringType.IDENTIFIER) {
            return identifier;
        } else if (identifierType == SourceStringType.KEYWORD) {
            // 类型转换的重载运算符
            if (Keywords.isBasicType(identifier.getValue())) {
                return identifier;
            } else {
                throw new AnalysisExpressionException(identifier.getPosition(), "identifier is expected");
            }
            // throw new AnalysisExpressionException(identifier.getPosition(), "identifier is expected");
        } else if (identifierType != SourceStringType.OPERATOR) {
            throw new AnalysisExpressionException(identifier.getPosition(), "identifier is expected");
        }
        if (Operator.CALL_PRE.nameEquals(identifier.getValue())) {
            // 猜测是重载运算符() or 构造器 or 类型转换的重载运算符
            // 找和这个(匹配的), 如果到最后了, 就是构造器 or 类型转换的重载运算符
            // 如果没有到最后, 就是重载运算符
            if (!it.hasNext()) {
                throw new AnalysisExpressionException(identifier.getPosition(), "Unknown operator");
            }
            SourceString next = it.next();
            if (next.getType() == SourceStringType.OPERATOR &&
                    Operator.CALL_POST.nameEquals(next.getValue()) &&
                    it.hasNext()) {
                // 是重载()运算符
                return new SourceString(SourceStringType.OPERATOR, CALLABLE_DECLARE_NAME, identifier.getPosition());
            }
            // 一定不是重载()运算符
            it.previous(); // )
            // 是构造器 or 类型转换的重载运算符
            it.previous(); // (
            return null;
        }
        // 猜测是重载运算符
        StringBuilder sb = new StringBuilder(identifier.getValue());
        SourcePosition position = identifier.getPosition();
        SourcePosition startOperator = position;
        while (true) {
            if (!it.hasNext()) {
                // 没找到Call pre
                throw new AnalysisExpressionException(identifier.getPosition(), position,
                        "Operator can't be identifier here");
            }
            // 一直找, 直到找到一个是(的为止
            boolean nextIsOperator = CollectionUtil.nextIs(it, ss -> ss.getType() == SourceStringType.OPERATOR);
            if (!nextIsOperator) {
                throw new AnalysisExpressionException(it.next().getPosition(), "unknown statement");
            }
            boolean nextIsCallPre = CollectionUtil.nextIs(it, ss -> CALL_PRE.nameEquals(ss.getValue()));
            if (nextIsCallPre) {
                break;
            }
            SourceString next = it.next();
            position = next.getPosition();
            sb.append(next.getValue());
        }
        String operator = sb.toString();
        if (!Operators.is(operator)) {
            throw new AnalysisExpressionException(startOperator, position, "Unknown operator");
        }
        return new SourceString(SourceStringType.OPERATOR, operator, identifier.getPosition());

    }

    private static SourceTextContext getAttachment(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        SourceTextContext attachment = new SourceTextContext();
        while (it.hasNext()) {
            attachment.add(it.next());
        }
        return attachment;
    }


    public static LinkedList<EnumConstantDeclarable> enumConstantListPhase(SourceTextContext enumConstList) {
        if (enumConstList == null) {
            return null;
        }
        if (enumConstList.isEmpty()) {
            return new LinkedList<>();
        }
        SourceString last = enumConstList.getLast();
        if (last.getType() == SourceStringType.SIGN && Declarable.SENTENCE_END.equals(last.getValue())) {
            enumConstList.removeLast();
        }
        if (enumConstList.isEmpty()) {
            return new LinkedList<>();
        }
        ListIterator<SourceString> iterator = enumConstList.listIterator();
        int inTuple = 0;
        SourceTextContext enumConst = new SourceTextContext();
        LinkedList<EnumConstantDeclarable> enumConstDeclarableList = new LinkedList<>();
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            if (next.getType() == SourceStringType.OPERATOR) {
                if (inTuple == 0 && Operator.COMMA.nameEquals(next.getValue())) {
                    if (enumConst.isEmpty()) {
                        throw new AnalysisExpressionException(next.getPosition(), "enum constant cannot be empty");
                    }
                    enumConstDeclarableList.add(enumConstantPhase(enumConst));
                    enumConst = new SourceTextContext();
                    continue;
                } else if (Operator.CALL_PRE.nameEquals(next.getValue())) {
                    inTuple++;
                } else if (Operator.CALL_POST.nameEquals(next.getValue())) {
                    inTuple--;
                }
                if (inTuple < 0) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal bracket match");
                }
            }
            enumConst.add(next);
        }
        if (enumConst.isEmpty()) {
            throw new AnalysisExpressionException(last.getPosition(), "enum constant cannot be empty");
        }
        enumConstDeclarableList.add(enumConstantPhase(enumConst));
        return enumConstDeclarableList;
    }

    public static EnumConstantDeclarable enumConstantPhase(SourceTextContext enumConst) {
        if (enumConst.isEmpty()) {
            throw new CompilerException();
        }
        SourceString name = enumConst.removeFirst();
        if (name.getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(name.getPosition(), "Illegal enum constant name");
        }
        if (enumConst.isEmpty()) {
            // 给个空进去, 结束
            return new EnumConstantDeclarable(name, null);
        }
        if (enumConst.size() == 1) {
            throw new AnalysisExpressionException(enumConst.getFirst().getPosition(), "Illegal enum constant");
        }
        SourceString first = enumConst.removeFirst();
        SourceString last = enumConst.removeLast();
        if (!(first.getType() == SourceStringType.OPERATOR) ||
                !(last.getType() == SourceStringType.OPERATOR) ||
                !(Operator.CALL_PRE.nameEquals(first.getValue())) ||
                !(Operator.CALL_POST.nameEquals(last.getValue()))) {
            throw new AnalysisExpressionException(first.getPosition(), last.getPosition(),
                    "Illegal enum constant define");
        }
        return new EnumConstantDeclarable(name, enumConst);
    }
    // public static String CC = Type<Integer>.Type;
    // expression中, new之后的才是类型, 可出现泛型, 且new之后一定是类型/(), 如果是类型, 一定到()结束
    // 其余根本不可能
    // instanceof 可以出现泛型, instance之后一定是泛型, 不会有大于/小于号
    // ... 那么, 对于函数呢? 函数怎么说? 对于各种的泛型.......
    // 带泛型的函数调用时会产生泛型
    // 用函数名代表函数指针时会产生泛型
    //     Number n = func<Number>(2);
    //     function<Number(int)> a = func<Number>;
    //     对于泛型, 如果>和(相连, 一定就是泛型了
    //      如果>是单独的再表达式的最后, 也一定就是泛型了
    // 综上
    // > 后面有item...?的, >就是比较
    // > 后面是Operator/没有的, >就是泛型
    //
}
