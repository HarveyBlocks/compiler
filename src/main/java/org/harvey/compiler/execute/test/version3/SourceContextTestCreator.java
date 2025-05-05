package org.harvey.compiler.execute.test.version3;

import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-09 19:57
 */
@SuppressWarnings("DuplicatedCode")
public class SourceContextTestCreator {
    static int i;

    public static SourceTextContext newSource(String s) {
        String[] raws = StringUtil.simpleSplit(s, "\n");
        SourceTextContext source = new SourceTextContext();
        for (int raw = 0; raw < raws.length; raw++) {
            if (raws[raw].isEmpty()) {
                continue;
            }
            String[] cals = raws[raw].split(" ");
            for (int cal = 0; cal < cals.length; cal++) {
                if (cals[cal].isEmpty()) {
                    continue;
                }
                source.add(instance(cals[cal], raw, cal));
            }
        }
        return source;
    }

    /**
     * "if"
     * "("
     * "exp1"
     * ")"
     * "if"
     * "("
     * "exp2"
     * ")"
     * "exp3"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp4"
     * ")"
     * "exp5"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp6"
     * ")"
     * "exp7"
     * ";"
     * "else"
     * "if"
     * "("
     * "exp8"
     * ")"
     * "exp9"
     * ";"
     * "else"
     * "exp10"
     * ";"
     * "exp11"
     * ;
     */
    public static SourceTextContext newSource1() {
        SourceTextContext source = new SourceTextContext();
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c1"));
        source.add(instance(")"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c2_1"));
        source.add(instance(")"));
        source.add(instance("exp2_1"));
        source.add(instance(";"));
        source.add(instance("else"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c2_2"));
        source.add(instance(")"));
        source.add(instance("exp2_2"));
        source.add(instance(";"));
        source.add(instance("else"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c2_3"));
        source.add(instance(")"));
        source.add(instance("exp2_3"));
        source.add(instance(";"));
        source.add(instance("else"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c2_4"));
        source.add(instance(")"));
        source.add(instance("exp2_4"));
        source.add(instance(";"));
        source.add(instance("else"));
        source.add(instance("exp2_5"));
        source.add(instance(";"));
        source.add(instance("exp1"));
        return source;
    }

    private static SourceString instance(String value) {
        return new SourceString(SourceType.STRING, value, new SourcePosition(0, i++));
    }

    private static SourceString instance(String value, int raw, int cal) {
        return new SourceString(SourceType.STRING, value, new SourcePosition(raw, cal));
    }

    public static SourceTextContext newSource2() {
        SourceTextContext source = new SourceTextContext();
        /*
         * if(c1)
         *   if(c2_1)
         *       exp2_1;
         *   else if (c2_2)
         *       exp2_2;
         *   else if(c2_3)
         *       exp2_3;
         *   else if(c2_4)
         *       exp2_4;
         *   else
         *       exp2_5;
         *  exp1;
         * */
        // c1_1
        // if_false_goto L0
        // c2_2
        // if_false_goto L1
        // exp2_1
        // goto_L2
        // L1:
        // exp2_2
        // L2:
        // L0:
        // exp1_2
        source.add(instance("{"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c0_1"));
        source.add(instance(")"));
        source.add(instance("{"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c1_1"));
        source.add(instance(")"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c2_1"));
        source.add(instance(")"));
        source.add(instance("exp2_1"));
        source.add(instance(";"));
        source.add(instance("else"));
        source.add(instance("if"));
        source.add(instance("("));
        source.add(instance("c_2_2"));
        source.add(instance(")"));
        source.add(instance("exp2_2"));
        source.add(instance(";"));
        source.add(instance("}"));
        source.add(instance("else"));
        source.add(instance("exp2_3"));
        source.add(instance(";"));
        source.add(instance("exp1_2"));
        source.add(instance(";"));
        source.add(instance("exp1_3"));
        source.add(instance(";"));
        source.add(instance("}"));
        return source;
    }

}
