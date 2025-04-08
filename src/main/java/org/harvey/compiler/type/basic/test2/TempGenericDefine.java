package org.harvey.compiler.type.basic.test2;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * TODO  
 *
 * @date 2025-03-30 23:06
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */

@Getter
@SuppressWarnings("DuplicatedCode")
class TempGenericDefine implements SelfConsistentExecutable {
    Level level = Level.TODO;
    String name;
    Parameterized parent;
    Parameterized[] interfaces;

    public TempGenericDefine(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String us;
        if (parent == null) {
            us = "";
        } else {
            us = " extends " + parent.toStringValue();
        }
        String is;
        if (interfaces == null || interfaces.length == 0) {
            is = "";
        } else {
            is = " & " +
                 Arrays.stream(interfaces).map(Parameterized::toStringValue).collect(Collectors.joining("&", "", ""));
        }
        return name + us + is;
    }

    @Override
    public void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks) {
        if (this.level == Level.FINISH) {
            return;
        }
        if (parent == null && (interfaces == null || interfaces.length == 0)) {
            this.level = Level.FINISH;
            return;
        }
        Level levelUsing = Level.decide(outerLevel, this.level);
        if (parent != null) {
            this.parent.selfConsistent(levelUsing, toBeChecks);
        }
        if (interfaces != null) {
            for (Parameterized each : interfaces) {
                each.selfConsistent(levelUsing, toBeChecks);
            }
        }
        this.level = this.level.up(levelUsing);

    }


}
