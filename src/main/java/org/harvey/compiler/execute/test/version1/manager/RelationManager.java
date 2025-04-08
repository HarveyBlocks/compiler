package org.harvey.compiler.execute.test.version1.manager;


import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 15:42
 */
public interface RelationManager {
    MemberType relateParameterizedType(ParameterizedType<ReferenceElement> element);
}
