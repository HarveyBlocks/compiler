package org.harvey.compiler.analysis.stmt.phaser;


import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedStatementWithBodyPart;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:31
 */
public abstract class StatementWithBodyPhaser extends BasicPhaser {

    public StatementWithBodyPhaser(StatementContext context) {
        super(context);
    }

    @Override
    public boolean isTargetPart(DepartedPart bodyPart) {
        return bodyPart instanceof DepartedStatementWithBodyPart;
    }

    @Override
    public void phase(DepartedPart part) {
        DepartedStatementWithBodyPart bodyPart = (DepartedStatementWithBodyPart) part;
        phase(BasicPhaser.basicPhase(bodyPart.getStatement()), bodyPart.getBody());
    }

    protected abstract void phase(BasicPhaseResult stmt, SourceTextContext body);
}
