package org.harvey.compiler.declare.define;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * 主要用于完成对Declare的Identifier的引用转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:07
 */
public interface Definition {

    static boolean skipIf(ListIterator<SourceString> iterator, Operator operator) {
        return CollectionUtil.skipIf(
                iterator, s -> s.getType() == SourceType.OPERATOR && operator.nameEquals(s.getValue()));
    }

    static void notNullValid(Object obj, String name) {
        if (obj == null) {
            throw new CompilerException("not complete: " + name);
        }
    }

    static void notEqualsValid(int id, int illegal, String name) {
        if (id == illegal) {
            throw new CompilerException("not complete: " + name);
        }
    }


    static void notRepeat(List<Pair<IdentifierString, SourceTextContext>> genericDefine) {
        Set<String> name = new HashSet<>();
        for (Pair<IdentifierString, SourceTextContext> pair : genericDefine) {
            String key = pair.getKey().getValue();
            if (name.contains(key)) {
                throw new AnalysisDeclareException(pair.getKey().getPosition(), "repeated generic name");
            }
            name.add(key);
        }
    }


    AccessControl getPermissions();

    Embellish getEmbellish();

    ReferenceElement getIdentifierReference();
}
