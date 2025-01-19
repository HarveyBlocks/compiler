package org.harvey.compiler.analysis.text.context;

import org.harvey.compiler.common.reflect.VieConstructor;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:13
 */
public class SourceTextContext extends LinkedList<SourceString> {
    /**
     * 仅仅作为标识使用
     */
    public static final SourceTextContext EMPTY = new SourceTextContext();

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

    public static void legalTypeAssert(Iterable<SourceString> it, Set<SourceStringType> legalType) {
        if (legalType == null || legalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            SourceStringType type = ss.getType();
            if (!legalType.contains(type)) {
                throw new CompilerException("Illegal text string type: " + type, new IllegalStateException());
            }
        }
    }

    public static void illegalTypeAssert(Iterable<SourceString> it, Set<SourceStringType> illegalType) {
        if (illegalType == null || illegalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            SourceStringType type = ss.getType();
            if (illegalType.contains(type)) {
                throw new CompilerException("Illegal text string type " + type, new IllegalStateException());
            }
        }
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
    public void throwExceptionIncludingAll(String message, Class<? extends CompileException> ec) {
        if (this.isEmpty()) {
            return;
        }
        SourceString first = this.pollFirst();
        SourceString last = first;
        while (!this.isEmpty()) {
            last = this.pollFirst();
        }
        throw new VieConstructor<>(ec, SourcePosition.class, SourcePosition.class, String.class)
                .instance(first.getPosition(), SourcePosition.moveToEnd(last.getPosition(), last.getValue()), message);
    }


}
