package org.harvey.compiler.execute.test.version1.env;

import org.harvey.compiler.execute.test.version1.msg.VariableRelatedDeclare;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:50
 */
public abstract class DeclareOuter implements OuterEnvironment {

    private final VariableRelatedDeclare variableRelatedDeclare;

    DeclareOuter(VariableRelatedDeclare variableRelatedDeclare) {
        this.variableRelatedDeclare = variableRelatedDeclare;
    }

    @Override
    public boolean isType(int type) {
        return type == IN_DECLARE;
    }
}
