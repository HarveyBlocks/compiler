package org.harvey.compiler.execute.test.version1.msg;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 17:33
 */
public interface FileRelatedDeclare {
    StructureRelatedDeclare getAlias(int index);

    StructureRelatedDeclare getStructure(int index);

    CallableRelatedDeclare getFunction(int index);
}
