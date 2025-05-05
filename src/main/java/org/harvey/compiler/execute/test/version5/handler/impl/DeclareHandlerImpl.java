package org.harvey.compiler.execute.test.version5.handler.impl;

import org.harvey.compiler.execute.test.version1.handler.TypeCastHandler;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version3.handler.DeclareHandler;
import org.harvey.compiler.execute.test.version3.msg.ControlContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 20:31
 */
public class DeclareHandlerImpl implements DeclareHandler {
    @Override
    public void handle(ControlContext context) {
        // 此时, 已经被包装成declare了
        // 确定是declare之后, 下面的解析可以有逗号, 确实
        // 解析表达式的时候, 特别注意环境, 环境是declare, 那就要在逗号的时候, 将东西放入局部变量表里

    }

    public MemberType skipDeclareType(ControlContext context) {
        // TODO
        return TypeCastHandler.tryMemberType(context.getOuterEnvironment(), context);
    }
}
