package org.harvey.compiler.analysis.calculate;

import org.harvey.compiler.common.util.StringUtil;
import org.junit.Test;

public class ExpressionPhaserTest {
    @Test
    public void testExpression() {
        /*new ExpressionPhaser0().phaseExpression("1 + 2 * ( 3 + 4 * 5 ) * 6 + 7 ".simpleSplit(" "));
        new ExpressionPhaser0().phaseExpression("1 + 2 * 3 + 4 * 5 + 6 + 7 + 8 * 9".simpleSplit(" "));
        new ExpressionPhaser0().phaseExpression("x = 2 * 3 + 4".simpleSplit(" "));*/
        // new ExpressionPhaser0().phaseExpression(StringUtil.simpleSplit("a = b = c + 4", " "));
        // new ExpressionPhaser0().phaseExpression(StringUtil.simpleSplit("a ( 1 * 1 , 2 , 3 , 4 ) ", " "));
        //  new ExpressionPhaser0().phaseExpression("a = ( 2 , b = c + 4 )".split(" "));
        int b = 3;
    }
}