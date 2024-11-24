package org.harvey.compiler.analysis.stmt.phaser.callable;

import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.StatementWithBodyPhaser;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.CollectionUtil;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 21:58
 */
public abstract class BodyCallablePhaser extends StatementWithBodyPhaser {
    public BodyCallablePhaser(StatementContext context) {
        super(context);
    }
    public static boolean callableInPart(DepartedSentencePart part) {
        return CollectionUtil.contains(part.getSentence(), ss ->
                ss.getType() == SourceStringType.KEYWORD && Keyword.CALLABLE.equals(ss.getValue())
        );
    }

}
