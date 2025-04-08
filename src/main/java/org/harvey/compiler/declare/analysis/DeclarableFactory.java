package org.harvey.compiler.declare.analysis;

import org.harvey.compiler.common.collecction.*;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.*;

/**
 * 工具类
 * 函数/方法声明, 类声明, 变量声明, 都OK
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:51
 */
public final class DeclarableFactory {

    public static final String CALLABLE_DECLARE_NAME = Operator.CALLABLE_DECLARE.getName();
    private static final Operator CALL_PRE = Operator.CALL_PRE;

    private DeclarableFactory() {
    }

    public static Declarable statementBasic(SourceTextContext statement) {
        SourcePosition start = statement.getFirst().getPosition();
        ListIterator<SourceString> it = statement.listIterator();
        return statementBasic(it, start);
    }

    public static Declarable statementBasic(ListIterator<SourceString> it, SourcePosition start) {
        // 解析作用域
        SourceTextContext permissions = getPermissions(it);
        // 解析修饰[static final sealed const]
        EmbellishSource embellish = getEmbellish(it);
        // 解析类型
        SourceTextContext type = getGeneralizedType(it, start); // 可带.
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
            if (next.getType() != SourceType.KEYWORD) {
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

    public static EmbellishSource getEmbellish(ListIterator<SourceString> it) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        EmbellishSource.Builder builder = new EmbellishSource.Builder();
        while (it.hasNext()) {
            SourceString next = it.next();
            if (next.getType() != SourceType.KEYWORD) {
                it.previous();
                break;
            }
            String value = next.getValue();
            Keyword keyword = Keyword.get(value);
            if (keyword == null) {
                throw new CompilerException(next + " is not a keyword");
            }
            SourcePosition position = next.getPosition();
            boolean embellishEnded = true;
            switch (keyword) {
                case CONST:
                    builder.setConst(position);
                    break;
                case STATIC:
                    builder.setStatic(position);
                    break;
                case FINAL:
                    builder.setFinal(position);
                    break;
                case SEALED:
                    builder.setSealed(position);
                    break;
                case ABSTRACT:
                    builder.setAbstract(position);
                    break;
                default:
                    embellishEnded = false;
            }
            if (!embellishEnded) {
                it.previous();
                break;
            }
        }
        return builder.build();
    }



    /**
     * 不认为class这种是类型, 括号开头的也不算
     */
    public static SourceTextContext getType(ListIterator<SourceString> it, SourcePosition start) {
        SourceTextContext rawType = getGeneralizedType(it, start);
        if (rawType.size() == 1 && Keywords.isStructure(rawType.getFirst().getValue())) {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
        // 括号开头的不算
        if (!rawType.isEmpty() && nextIsOperator(rawType.listIterator(), Operator.PARENTHESES_PRE)) {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
        return rawType;
    }

    /**
     * 类型<br>
     * 1. Identifier(id.id.id.id)<br>
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
     * "," 之前的?<br>
     * 参数列表之前的?<br>
     * 广义的
     */
    private static SourceTextContext getGeneralizedType(ListIterator<SourceString> it, SourcePosition start) {
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
            // 一上来就是Identifier;
            // 只有Identifier后面是Identifier的, 后面的才是声明的主体
            // 本Identifier就是类了, 否则, 这个Identifier也是类型的一部分
            type.addAll(departFullIdentifier(first.getPosition(), first.getValue(), it));
            if (nextIsOperator(it, Operator.GENERIC_LIST_PRE)) {
                // 带泛型
                type.addAll(SourceTextContext.skipNest(it, Operator.GENERIC_LIST_PRE.getName(),
                        Operator.GENERIC_LIST_POST.getName(), true
                ));
            }

            if (nextIsOperator(it, Operator.MULTIPLE_TYPE)) {
                // 带`...`
                type.add(it.next());
            }
            return type;
        } else if (nextIsOperator(it, Operator.PARENTHESES_PRE)) {
            return SourceTextContext.skipNest(it, Operator.PARENTHESES_PRE.getName(),
                    Operator.PARENTHESES_POST.getName(), true
            );
        } else {
            throw new AnalysisExpressionException(start, "Unknown type");
        }
    }

    /**
     * @param it start with dot, when func ended, it will move to zhe point after full identifier
     * @return until not . after id, can not distinguish operation reload like "id.id.+()", return ".id"
     */
    public static List<SourceString> departFullIdentifier(
            SourcePosition position, String first,
            ListIterator<SourceString> it) {
        // it.next()=="."
        if (!it.hasNext()) {
            return List.of(new SourceString(SourceType.IDENTIFIER, first, position));
        }
        boolean expectedDot = true;
        SourceTextContext full = new SourceTextContext();
        full.add(new SourceString(SourceType.IDENTIFIER, first, position));
        while (it.hasNext()) {
            SourceString next = it.next();
            if (expectedDot && Operator.GET_MEMBER.nameEquals(next.getValue()) ||
                !expectedDot && next.getType() == SourceType.IDENTIFIER) {
                full.add(next);
                expectedDot = !expectedDot;
            } else {
                it.previous();
                return full;
            }
        }
        if (expectedDot) {
            if (full.isEmpty()) {
                throw new CompilerException("it's impossible for full identifier is empty");
            }
            throw new AnalysisExpressionException(full.getLast().getPosition(), "expected an identifier");
        }
        return full;
    }

    /**
     * @param undoIt it.previous是DOT
     * @return 结束后it指向
     */
    public static List<SourceString> operatorReloadDepart(RandomlyIterator<SourceString> undoIt) {
        // it.previous是dot
        undoIt.mark();
        SourceString first = undoIt.next();
        if (first.getType() != SourceType.OPERATOR) {
            undoIt.returnToAndRemoveMark();
            return null;
        }
        String value = first.getValue();
        if (Operator.ARRAY_AT_PRE.nameEquals(value)) {
            SourceString second = undoIt.next();
            if (Operator.ARRAY_AT_POST.nameEquals(second.getValue())) {
                return List.of(new SourceString(
                        SourceType.OPERATOR, Operator.ARRAY_DECLARE.getName(),
                        first.getPosition()
                ));
            } else {
                undoIt.returnToAndRemoveMark();
                return null;
            }
        } else if (Operator.PARENTHESES_PRE.nameEquals(value)) {
            SourceString second = undoIt.next();// after oper
            if (Operator.PARENTHESES_POST.nameEquals(second.getValue())) {
                return List.of(new SourceString(
                        SourceType.OPERATOR, Operator.CALLABLE_DECLARE.getName(),
                        first.getPosition()
                ));
            } else {
                undoIt.returnToAndRemoveMark();
                return CollectionUtil.skipTo(
                        undoIt, s -> s.getType() == SourceType.OPERATOR &&
                                     Operator.PARENTHESES_POST.nameEquals(s.getValue()), true);
            }
            // ( -> .()() call 运算符
            // ( -> .()<>() call 运算符
            // ( -> .(a.b.c)() cast运算符
            // ( -> .(a.b.c)<>() cast运算符
            // ( -> .(a.b.c<A,B,C>)() cast运算符
            // ( -> .(a.b.c<A,B,C>)<>() cast运算符
            // public a<> ();
            // public a <();
            // ( -> .(int)() cast运算符
            // ( -> .(int)<>() cast运算符
        }
        Operator[] operator = Operators.reloadableOperator(value);
        if (operator == null) {
            undoIt.returnToAndRemoveMark();
            return null;
        }
        // + -> +() 运算符重载
        return List.of(first);
    }

    public static ListPoint<List<SourceString>> departFullIdentifier(
            SourcePosition position, String firstName,
            int indexOfDot,
            List<SourceString> sourceList) {
        RandomlyIterator<SourceString> it = CollectionUtil.randomlyIterator(sourceList, indexOfDot);
        List<SourceString> element = departFullIdentifier(position, firstName, it);
        return new ListPoint<>(it.nextIndex(), element);
    }

    public static ListPoint<List<SourceString>> departOperatorReloadOnCall(
            int index,
            List<SourceString> sourceList) {
        RandomlyIterator<SourceString> undoIt = CollectionUtil.randomlyIterator(sourceList, index);
        List<SourceString> element = operatorReloadDepart(undoIt);
        return new ListPoint<>(undoIt.nextIndex(), element);
    }


    private static boolean nextIsOperator(ListIterator<SourceString> it, Operator operator) {
        return CollectionUtil.nextIs(it, ss -> isOperator(ss, operator));
    }

    private static boolean isOperator(SourceString ss, Operator operator) {
        return ss.getType() == SourceType.OPERATOR && operator.nameEquals(ss.getValue());
    }


    /**
     * 迭代器不移动
     */
    private static boolean nextIsIdentifier(ListIterator<SourceString> it) {
        return CollectionUtil.nextIs(it, ss -> ss.getType() == SourceType.IDENTIFIER);
    }

    private static boolean nextIsTypeAvailableKeyword(ListIterator<SourceString> it) {
        return CollectionUtil.nextIs(it, ss -> {
            String value = ss.getValue();
            return ss.getType() == SourceType.KEYWORD &&
                   (Keywords.isBasicType(value) || Keywords.isStructure(value) || Keyword.ALIAS.equals(value));
        });
    }

    /**
     * 对于枚举的单例成员, 可能没有类型, 但是一定要有标识符
     *
     * @param type 可能会因为没有identifier, 而失去type, 转为identity
     */
    private static SourceString getIdentifier(
            ListIterator<SourceString> it, SourceTextContext type,
            SourcePosition start) {
        if (!it.hasNext()) {
            // 特别的enum元素的声明, 无返回值
            /*不太对 if (type.size() != 1) {
                throw new AnalysisExpressionException(start, "identifier is expected");
            }*/
            if (type.isEmpty()) {
                throw new AnalysisExpressionException(start, "identifier is expected");
            }
            if (type.size() != 1) {
                throw new AnalysisExpressionException(type.getFirst().getPosition(), type.getLast().getPosition(),
                        "unknown declaration"
                );
            }

            // 暂且认为type为identifier
            SourceString identifier = type.removeLast();
            if (identifier.getType() != SourceType.IDENTIFIER) {
                throw new AnalysisExpressionException(identifier.getPosition(), type.getLast().getPosition(),
                        "unknown declaration"
                );
            }
            return identifier;
        }
        SourceString identifier = it.next();
        SourceType identifierType = identifier.getType();
        if (identifierType == SourceType.IDENTIFIER) {
            return identifier;
        } else if (identifierType == SourceType.KEYWORD) {
            // 类型转换的重载运算符
            if (Keywords.isBasicType(identifier.getValue())) {
                return identifier;
            } else {
                throw new AnalysisExpressionException(identifier.getPosition(), "identifier is expected");
            }
            // throw new AnalysisExpressionException(identifier.getPosition(), "identifier is expected");
        } else if (identifierType != SourceType.OPERATOR) {
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
            if (next.getType() == SourceType.OPERATOR && Operator.CALL_POST.nameEquals(next.getValue()) &&
                it.hasNext()) {
                // 是重载()运算符
                return new SourceString(SourceType.OPERATOR, CALLABLE_DECLARE_NAME, identifier.getPosition());
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
                        "Operator can't be identifier here"
                );
            }
            // 一直找, 直到找到一个是(的为止
            boolean nextIsOperator = CollectionUtil.nextIs(it, ss -> ss.getType() == SourceType.OPERATOR);
            if (!nextIsOperator) {
                throw new AnalysisExpressionException(it.next().getPosition(), "unknown cache");
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
        return new SourceString(SourceType.OPERATOR, operator, identifier.getPosition());

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
        if (last.getType() == SourceType.SIGN && Declarable.SENTENCE_END.equals(last.getValue())) {
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
            if (next.getType() == SourceType.OPERATOR) {
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
                    throw new AnalysisExpressionException(next.getPosition(), "illegal parentheses match");
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
        if (name.getType() != SourceType.IDENTIFIER) {
            throw new AnalysisExpressionException(name.getPosition(), "Illegal enum constant name");
        }
        if (enumConst.isEmpty()) {
            // 给个空进去, 结束
            return new EnumConstantDeclarable(new IdentifierString(name), Collections.emptyList());
        }
        if (enumConst.size() == 1) {
            throw new AnalysisExpressionException(enumConst.getFirst().getPosition(), "Illegal enum constant");
        }
        SourceString first = enumConst.removeFirst();
        SourceString last = enumConst.removeLast();
        if (!(first.getType() == SourceType.OPERATOR) || !(last.getType() == SourceType.OPERATOR) ||
            !(Operator.CALL_PRE.nameEquals(first.getValue())) ||
            !(Operator.CALL_POST.nameEquals(last.getValue()))) {
            throw new AnalysisExpressionException(first.getPosition(), last.getPosition(),
                    "Illegal enum constant define"
            );
        }
        ListIterator<SourceString> iterator = enumConst.listIterator();
        List<SourceTextContext> arguments = new ArrayList<>();
        while (iterator.hasNext()) {
            arguments.add(SourceTextContext.skipUntilComma(iterator));
            if (!iterator.hasNext()) {
                break;
            }
            if (!CollectionUtil.skipIf(iterator, s -> Operator.COMMA.nameEquals(s.getValue()))) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "expected (");
            }
            if (!iterator.hasNext()) {
                throw new AnalysisExpressionException(iterator.previous().getPosition(), "empty argument is illegal");
            }

        }
        return new EnumConstantDeclarable(new IdentifierString(name), arguments);
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
