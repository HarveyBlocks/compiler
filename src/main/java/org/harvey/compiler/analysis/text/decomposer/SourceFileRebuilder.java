package org.harvey.compiler.analysis.text.decomposer;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.exception.CompilerException;

import java.io.PrintStream;

/**
 * 重建源码
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 20:16
 */
public class SourceFileRebuilder implements TextDecomposer {
    private final PrintStream out;
    private SourcePosition sp = new SourcePosition(0, 0);

    public SourceFileRebuilder() {
        this(System.out);
    }

    public SourceFileRebuilder(PrintStream stream) {
        out = stream;
    }

    @Override
    public SourceTextContext decompose(SourceString source) {
        SourcePosition now = source.getPosition();
        int deltaRaw = now.getRaw() - sp.getRaw();
        if (deltaRaw < 0) {
            throw new CompilerException(now + ",Illegal Column");
        }
        if (deltaRaw > 0) {
            sp.setColumn(0);
        }

        for (int i = 0; i < deltaRaw; i++) {
            out.println();
        }
        int deltaColumn = now.getColumn() - sp.getColumn();
        if (deltaColumn < 0) {
            throw new CompilerException(now + ", Illegal Column");
        }
        for (int i = 0; i < deltaColumn; i++) {
            out.print(" ");
        }
        String value = source.getValue();
        out.print(value);
        sp = SourcePosition.moveToEnd(now, value);
        return null;
    }
}
