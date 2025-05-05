package org.harvey.compiler.execute.test.version0;

import org.harvey.compiler.execute.calculate.Operator;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-03 15:10
 */
public class PhasingOperatorFactory {
    public static final PhasingOperator CALL = new PhasingOperator.Call();
    private static final Map<Operator, PhasingOperator.Normal> NORMAL_CACHE = new HashMap<>();

    public static PhasingOperator normal(Operator operator) {
        PhasingOperator.Normal normal = NORMAL_CACHE.get(operator);
        if (normal == null) {
            normal = new PhasingOperator.Normal(operator);
            NORMAL_CACHE.put(operator, normal);
        }
        return normal;
    }
}
