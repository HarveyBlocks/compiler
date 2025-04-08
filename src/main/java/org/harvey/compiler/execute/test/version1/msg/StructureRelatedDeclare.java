package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedStructure;

/**
 * TODO  
 *
 * @date 2025-04-05 15:52
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public interface StructureRelatedDeclare {
    RelatedParameterizedStructure get();

    StructureRelatedDeclare getAlias(int index);

    CallableRelatedDeclare getConstructor(int index);

    CallableRelatedDeclare getMethod(int index);

    VariableRelatedDeclare getField(int index);
}
