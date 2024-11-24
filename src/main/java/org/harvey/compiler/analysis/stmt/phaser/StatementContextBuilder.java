package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedFileBody;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 13:29
 */
public class StatementContextBuilder {
    private final List<SourceContextPhaser> phaserChain = new ArrayList<>();
    private final StatementContext context = new StatementContext();

    public StatementContextBuilder() {
    }

    public StatementContextBuilder registerPhaser(Function<StatementContext, SourceContextPhaser> constructor) {
        phaserChain.add(constructor.apply(context));
        return this;
    }

    public StatementContext build(DepartedFileBody body) {
        for (DepartedPart part : body.get()) {
            boolean phased = false;
            for (SourceContextPhaser phaser : phaserChain) {
                if (!phaser.isTargetPart(part)) {
                    continue;
                }
                phaser.phase(part);
                phased = true;
            }
            if (!phased) {
                // TODO throw new AnalysisException(partPosition, "Not allowed to appear globally");
            }
        }
        return context;
    }

}