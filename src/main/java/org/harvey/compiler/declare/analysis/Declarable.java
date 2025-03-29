package org.harvey.compiler.declare.analysis;

import lombok.Getter;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.depart.SimpleDepartedBodyFactory;
import org.harvey.compiler.type.generic.GenericFactory;

import java.util.ListIterator;

/**
 * 声明的存储, field, callable和structure的声明可以统一
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 13:02
 */

@Getter
//@AllArgsConstructor
public class Declarable {
    public static final String SENTENCE_END = SimpleDepartedBodyFactory.SENTENCE_END;
    private final SourcePosition start;
    private final SourceTextContext permissions;
    private final EmbellishSource embellish;
    private final SourceTextContext type;
    private final SourceString identifier;
    /**
     * 参数列表/函数泛型列表/变量赋值/类型泛型列表
     */
    private final SourceTextContext attachment;
    private final boolean callable;

    public Declarable(
            SourcePosition start, SourceTextContext permissions,
            EmbellishSource embellish, SourceTextContext type,
            SourceString identifier, SourceTextContext attachment) {
        this.start = start;
        this.permissions = permissions;
        this.embellish = embellish;
        this.type = type;
        this.identifier = identifier;
        this.attachment = attachment;
        if (attachment == null || attachment.isEmpty()) {
            this.callable = false;
            return;
        }
        SourceString last = this.attachment.getLast();
        if (last.getType() == SourceType.SIGN && SENTENCE_END.equals(last.getValue())) {
            this.attachment.removeLast();
        }
        this.callable = Declarable.isCallable(this.attachment);
    }

    /**
     * 声明的最后不是), 返回false, 否则继续
     * attachment,
     * 1. 开始是<, 找匹配的>, >之后是(, 就是callable
     * 2. 开始是(, 就是callable
     */
    public static boolean isCallable(SourceTextContext attachment) {
        if (attachment == null || attachment.isEmpty() || attachment.size() == 1) {
            return false;
        }
        if (!lastIsCallPost(attachment)) {
            return false;
        }
        if (firstIsCallPre(attachment)) {
            return true;
        }
        if (!firstIsGenericPre(attachment)) {
            return false;
        }
        // generic pre->generic post
        ListIterator<SourceString> iterator = attachment.listIterator();
        return isCallPreAfterGeneric(iterator);
    }

    private static boolean isCallPreAfterGeneric(ListIterator<SourceString> iterator) {
        GenericFactory.skipGenericList(iterator);
        if (!iterator.hasNext()) {
            return false;
        }
        SourceString afterPost = iterator.next();
        return afterPost.getType() == SourceType.OPERATOR &&
               Operator.CALL_PRE.nameEquals(afterPost.getValue());
    }

    private static boolean firstIsCallPre(SourceTextContext attachment) {
        SourceString first = attachment.getFirst();
        return first.getType() == SourceType.OPERATOR && Operator.CALL_PRE.nameEquals(first.getValue());
    }

    private static boolean firstIsGenericPre(SourceTextContext attachment) {
        SourceString first = attachment.getFirst();
        return first.getType() == SourceType.OPERATOR && Operator.GENERIC_LIST_PRE.nameEquals(first.getValue());
    }


    private static boolean lastIsCallPost(SourceTextContext attachment) {
        SourceString last = attachment.getLast();
        return last.getType() == SourceType.OPERATOR && Operator.CALL_POST.nameEquals(last.getValue());
    }

    private static Keyword lastKeyword(SourceTextContext type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        SourceString last = type.getLast();
        if (last.getType() != SourceType.KEYWORD) {
            return null;
        }
        return Keyword.get(last.getValue());
    }

    public boolean isVariableDeclare() {
        if (type == null || type.isEmpty()) {
            return false;
        }
        return !callable && !Keywords.isStructure(lastKeyword(type));
    }

    public boolean isComplexStructure() {
        return Keywords.isStructure(lastKeyword(type));
    }

    /**
     * 忽略Abstract
     */
    public boolean isClass() {
        return Keyword.CLASS == lastKeyword(type);
    }

    public boolean isStruct() {
        return Keyword.STRUCT == lastKeyword(type);
    }

    public boolean isInterface() {
        return Keyword.INTERFACE == lastKeyword(type);
    }

    public boolean isEnum() {
        return Keyword.ENUM == lastKeyword(type);
    }

    @Override
    public String toString() {
        return "Declarable{" +
               "type=" + type +
               ", identifier=" + identifier +
               ", attachment=" + attachment +
               '}';
    }
}
