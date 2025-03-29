package org.harvey.compiler.execute.local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.IdentifierString;

import java.util.ArrayList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-31 15:27
 */
@AllArgsConstructor
@Getter
public class LocalVariableDeclareSequence {
    private final boolean markedConst;
    private final boolean markedFinal;
    private final Expression type;
    private final ArrayList<Pair<IdentifierString, Expression>> assignPair;


}
