package org.harvey.compiler.type.basic.test5;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @date 2025-04-01 15:30
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
class AssignableAlias implements Assignable {


    private final Assignable assignableEndMappedParameter;

    public AssignableAlias(Parameterized alias) {
        // alias 转
        if (!alias.isAlias()) {
            throw new CompilerException("only alias is legal here");
        }
        TempAlias aliasRawType = (TempAlias) alias.getRawType();
        this.assignableEndMappedParameter = AssignableFactory.create(aliasRawType.mapAndInject(alias));
    }

    @Override
    public void assign(Parameterized from) {
        assignableEndMappedParameter.assign(from);
    }

    @Override
    public void assign(TempGenericDefine from) {
        assignableEndMappedParameter.assign(from);
    }
}
