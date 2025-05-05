package org.harvey.compiler.syntax;

import lombok.Getter;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 19:57
 */
@Getter
public class NullItem implements ItemString {
    private final SourcePosition position;

    public NullItem(SourcePosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "NullItem";
    }
}
