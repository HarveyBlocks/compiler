package org.harvey.compiler.type.basic.test4;

/**
 * TODO  
 *
 * @date 2025-03-30 22:58
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
enum Level {
    TODO,
    RAW_TYPE,
    PARAMETER,
    FINISH;

    public static Level decide(Level l1, Level l2) {
        return Level.values()[Math.min(l1.ordinal(), l2.ordinal())];
    }

    public Level up(Level levelUsing) {
        if (this == Level.FINISH) {
            return this;
        }
        if (levelUsing.ordinal() < this.ordinal()) {
            return this;
        }
        return Level.values()[this.ordinal() + 1];
    }
}
