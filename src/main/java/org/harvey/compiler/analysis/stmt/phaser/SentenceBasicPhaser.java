package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;

/**
 * 不包括Import
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:52
 */
public abstract class SentenceBasicPhaser extends BasicPhaser {
    public SentenceBasicPhaser(StatementContext context) {
        super(context);
    }

    @Override
    public boolean isTargetPart(DepartedPart bodyPart) {
        return bodyPart instanceof DepartedSentencePart;
    }

    @Override
    public void phase(DepartedPart part) {
        DepartedSentencePart bodyPart = (DepartedSentencePart) part;
        phase(BasicPhaser.basicPhase(bodyPart.getSentence()));
    }

    protected abstract void phase(BasicPhaseResult sentence);
}
