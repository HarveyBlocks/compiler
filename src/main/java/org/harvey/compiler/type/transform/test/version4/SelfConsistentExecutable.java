package org.harvey.compiler.type.transform.test.version4;

import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 22:59
 */
interface SelfConsistentExecutable {
    void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks);

    Level getLevel();
}
