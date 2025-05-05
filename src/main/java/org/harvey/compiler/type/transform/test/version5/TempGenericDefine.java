package org.harvey.compiler.type.transform.test.version5;

import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 3.0 增加Lower
 * @date 2025-03-30 23:06
 */

@Getter
@SuppressWarnings("DuplicatedCode")
class TempGenericDefine implements SelfConsistentExecutable {
    Level level = Level.TODO;
    String name;
    Parameterized lower;
    Parameterized parent;
    Parameterized[] interfaces;
    boolean multiple;

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
        String ls;
        if (lower == null) {
            ls = "";
        } else {
            ls = " super " + lower.toStringValue();
        }
        String ms = multiple ? "..." : "";
        return name + ms + us + is + ls;
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
        if (lower != null) {
            this.lower.selfConsistent(levelUsing, toBeChecks);

            // lower 要 赋值哦, 咋办呢? 可以吗?
            if (parent != null) {
                AssignableFactory.create(parent).assign(lower);
            }
            if (interfaces != null) {
                for (Parameterized each : interfaces) {
                    AssignableFactory.create(each).assign(lower);
                }
            }
        }

        this.level = this.level.up(levelUsing);

    }


}
