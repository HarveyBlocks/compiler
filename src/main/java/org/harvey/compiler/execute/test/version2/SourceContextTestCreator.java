package org.harvey.compiler.execute.test.version2;

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
public class SourceContextTestCreator {
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
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 1)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 2)));
        source.add(new SourceString(SourceType.STRING, "c1", new SourcePosition(0, 3)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 4)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 5)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 6)));
        source.add(new SourceString(SourceType.STRING, "c2_1", new SourcePosition(0, 7)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 8)));
        source.add(new SourceString(SourceType.STRING, "exp2_1", new SourcePosition(0, 9)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 10)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 11)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 12)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 13)));
        source.add(new SourceString(SourceType.STRING, "c2_2", new SourcePosition(0, 14)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 15)));
        source.add(new SourceString(SourceType.STRING, "exp2_2", new SourcePosition(0, 16)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 17)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 18)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 19)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 20)));
        source.add(new SourceString(SourceType.STRING, "c2_3", new SourcePosition(0, 21)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 22)));
        source.add(new SourceString(SourceType.STRING, "exp2_3", new SourcePosition(0, 23)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 24)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 25)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 26)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 27)));
        source.add(new SourceString(SourceType.STRING, "c2_4", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 29)));
        source.add(new SourceString(SourceType.STRING, "exp2_4", new SourcePosition(0, 30)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 31)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 32)));
        source.add(new SourceString(SourceType.STRING, "exp2_5", new SourcePosition(0, 33)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 34)));
        source.add(new SourceString(SourceType.STRING, "exp1", new SourcePosition(0, 35)));
        return source;
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
        source.add(new SourceString(SourceType.STRING, "{", new SourcePosition(0, 0)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 1)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 2)));
        source.add(new SourceString(SourceType.STRING, "c0_1", new SourcePosition(0, 3)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 4)));
        source.add(new SourceString(SourceType.STRING, "{", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 5)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 6)));
        source.add(new SourceString(SourceType.STRING, "c1_1", new SourcePosition(0, 7)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 8)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 9)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 10)));
        source.add(new SourceString(SourceType.STRING, "c2_1", new SourcePosition(0, 11)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 12)));
        source.add(new SourceString(SourceType.STRING, "exp2_1", new SourcePosition(0, 13)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 14)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 15)));
        source.add(new SourceString(SourceType.STRING, "if", new SourcePosition(0, 16)));
        source.add(new SourceString(SourceType.STRING, "(", new SourcePosition(0, 17)));
        source.add(new SourceString(SourceType.STRING, "c_2_2", new SourcePosition(0, 18)));
        source.add(new SourceString(SourceType.STRING, ")", new SourcePosition(0, 19)));
        source.add(new SourceString(SourceType.STRING, "exp2_2", new SourcePosition(0, 20)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 21)));
        source.add(new SourceString(SourceType.STRING, "}", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "else", new SourcePosition(0, 22)));
        source.add(new SourceString(SourceType.STRING, "exp2_3", new SourcePosition(0, 23)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 24)));
        source.add(new SourceString(SourceType.STRING, "exp1_2", new SourcePosition(0, 25)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 26)));
        source.add(new SourceString(SourceType.STRING, "exp1_3", new SourcePosition(0, 27)));
        source.add(new SourceString(SourceType.STRING, ";", new SourcePosition(0, 28)));
        source.add(new SourceString(SourceType.STRING, "}", new SourcePosition(0, 0)));
        return source;
    }

}
