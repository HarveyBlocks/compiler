package org.harvey.compiler.execute.test.version0;

import org.harvey.compiler.execute.calculate.Operator;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO  
 *
 * @date 2025-04-03 15:10
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class PhasingOperatorFactory {
    private static final Map<Operator, PhasingOperator.Normal> NORMAL_CACHE = new HashMap<>();
    public static final PhasingOperator CALL = new PhasingOperator.Call();
    public static PhasingOperator normal(Operator operator) {
        PhasingOperator.Normal normal = NORMAL_CACHE.get(operator);
        if (normal == null) {
            normal = new PhasingOperator.Normal(operator);
            NORMAL_CACHE.put(operator, normal);
        }
        return normal;
    }
}
