package org.harvey.compiler.type.generic.register.entity;

import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.syntax.BasicTypeString;

import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
public interface ReferManager {
    /**
     *
     */
    LinkedList<ReferenceElement> refer(FullIdentifierString pre, FullIdentifierString sourceType);


    FullIdentifierString dereference(ReferenceElement referenceElement);

}
