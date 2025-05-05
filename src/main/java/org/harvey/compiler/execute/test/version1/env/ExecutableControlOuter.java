package org.harvey.compiler.execute.test.version1.env;


import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;

/**
 * TODO
 * 控制结构作为outer
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:50
 */
public abstract class ExecutableControlOuter implements OuterEnvironment {
    final CallableRelatedDeclare callableRelatedDeclare;

    ExecutableControlOuter(CallableRelatedDeclare callableRelatedDeclare) {
        this.callableRelatedDeclare = callableRelatedDeclare;
        // 需要更多, 例如局部变量表,其他的应该也有才对
    }

    @Override
    public boolean isType(int type) {
        return type == IN_CALLABLE_ARGUMENT;
    }


}
