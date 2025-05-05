package org.harvey.compiler.type;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-16 10:24
 */
public class StructureMessageFactory {
    public static ParameterizedType<FullIdentifierString> map(
            ParameterizedType<ReferenceElement> referred, DIdentifierManager manager) {
        List<Pair<Integer, FullIdentifierString>> fullSequence = referred.toSequence()
                .stream()
                .map(e -> new Pair<>(e.getKey(), manager.getIdentifier(e.getValue())))
                .collect(Collectors.toList());
        return ParameterizedType.toTree(fullSequence);
    }


}
