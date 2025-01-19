package org.harvey.compiler.analysis.calculate;

import org.harvey.compiler.execute.expression.ExpressionPhaser;
import org.junit.Test;

public class ExpressionPhaserTest {
    @Test
    public void testExpression() {
        /*new ExpressionPhaser().phaseExpression("1 + 2 * ( 3 + 4 * 5 ) * 6 + 7 ".split(" "));
        new ExpressionPhaser().phaseExpression("1 + 2 * 3 + 4 * 5 + 6 + 7 + 8 * 9".split(" "));
        new ExpressionPhaser().phaseExpression("x = 2 * 3 + 4".split(" "));
        new ExpressionPhaser().phaseExpression("a = b = c + 4".split(" "));*/
        new ExpressionPhaser().phaseExpression("a = ( 2 , b = c + 4 )".split(" "));
        int b = 3;
    }
}