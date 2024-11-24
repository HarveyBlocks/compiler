package org.harvey.compiler.analysis.stmt.phaser;


import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:31
 */
public abstract class SourceContextPhaser {
    protected final StatementContext context;

    public SourceContextPhaser(StatementContext context) {
        this.context = context;
    }

    /**
     * @param bodyPart 被filter 命中的声明部分
     */
    public abstract boolean isTargetPart(DepartedPart bodyPart);
    /**
     * 将解析结果树注入{@link java.lang.module.Configuration}
     *
     * @param sentence 被filter 命中的声明部分
     */
    public abstract void phase(DepartedPart sentence);
}
