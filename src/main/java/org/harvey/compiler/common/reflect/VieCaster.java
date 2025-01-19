package org.harvey.compiler.common.reflect;

import org.harvey.compiler.common.Pair;
import org.harvey.compiler.common.PairList;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

import java.util.function.Consumer;

/**
 * TODO  
 *
 * @date 2025-01-10 14:54
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class VieCaster {
    public static void cast(Object obj, PairList<Class<?>, Consumer<Object>> list) {
        if (obj == null) {
            return;
        }
        Class<?> objClass = obj.getClass();
        for (Pair<Class<?>, Consumer<Object>> entry : list) {
            if (entry.getKey().isAssignableFrom(objClass)) {
                entry.getValue().accept(obj);
                break;
            }
        }
    }

    public static void main(String[] args) {
        PairList<Class<?>, Consumer<Object>> list = new PairList<>();
        list.add(AnalysisExpressionException.class, obj -> {
            AnalysisExpressionException re = (AnalysisExpressionException) obj;
            System.out.println("aee");
        }).add(AnalysisException.class, obj -> {
            AnalysisException t = (AnalysisException) obj;
            System.out.println("ae");
        }).add(CompilerException.class, obj -> {
            CompilerException re = (CompilerException) obj;
            System.out.println("ce");
        }).add(Throwable.class, obj -> {
            Throwable t = (Throwable) obj;
            System.out.println("t");
        });
        cast(new CompilerException(), list);
    }
}
