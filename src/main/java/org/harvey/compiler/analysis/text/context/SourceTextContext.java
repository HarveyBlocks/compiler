package org.harvey.compiler.analysis.text.context;

import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.exception.CompileException;
import org.harvey.compiler.exception.CompilerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    public SourceTextContext(Collection<SourceString> c) {
        super(c);
    }

    /**
     * 仅仅作为标识使用
     */
    public static final SourceTextContext EMPTY = new SourceTextContext();

    public SourceTextContext() {
        super();
    }

    public static void legalTypeAssert(Iterable<SourceString> it, Set<SourceStringType> legalType) {
        if (legalType == null || legalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            if (!legalType.contains(ss.getType())) {
                throw new CompilerException("Illegal text string type", new IllegalStateException());
            }
        }
    }

    public static void illegalTypeAssert(Iterable<SourceString> it, Set<SourceStringType> illegalType) {
        if (illegalType == null || illegalType.isEmpty()) {
            return;
        }
        for (SourceString ss : it) {
            if (illegalType.contains(ss.getType())) {
                throw new CompilerException("Illegal text string type", new IllegalStateException());
            }
        }
    }

    public void throwAllAsCompileException(String message, Class<? extends CompileException> ec) {
        if (this.isEmpty()) {
            return;
        }
        SourceString first = this.pollFirst();
        SourceString last = first;
        while (!this.isEmpty()) {
            last = this.pollFirst();
        }
        CompileException compileException;
        try {
            Constructor<? extends CompileException> constructor = getConstructor(ec);
            compileException = constructor.newInstance(first.getPosition(),
                    SourcePosition.moveToEnd(last.getPosition(), last.getValue()), message);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new CompilerException(e.getMessage());
        }
        throw compileException;
    }

    private Constructor<? extends CompileException> getConstructor(Class<? extends CompileException> ec) throws NoSuchMethodException {
        return ec.getDeclaredConstructor(SourcePosition.class, SourcePosition.class, String.class);
    }
}
