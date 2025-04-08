package org.harvey.compiler.type.basic.test5;

import java.util.LinkedList;

/**
 * TODO  
 *
 * @date 2025-03-30 22:59
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
 interface SelfConsistentExecutable {
    void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks);

    Level getLevel();
}
