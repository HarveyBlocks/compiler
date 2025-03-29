package org.harvey.compiler.text.decomposer;

import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 将几个分解器轮流运行
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 23:15
 */

public class TextDecomposerChain {
    private final List<TextDecomposer> chain = new ArrayList<>();

    public TextDecomposerChain register(TextDecomposer decomposer) {
        chain.add(decomposer);
        return this;
    }

    public SourceTextContext execute(SourceTextContext context) {
        for (TextDecomposer decomposer : chain) {
            for (ListIterator<SourceString> it = context.listIterator(); it.hasNext(); ) {
                SourceString next = it.next();
                SourceTextContext newContext = decomposer.decompose(next);
                if (newContext == null) {
                    continue;
                }
                it.remove();
                for (SourceString sourceString : newContext) {
                    it.add(sourceString);
                }
            }
        }
        return context;
    }

}
