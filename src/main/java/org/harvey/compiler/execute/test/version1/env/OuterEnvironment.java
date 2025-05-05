package org.harvey.compiler.execute.test.version1.env;


import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * outer的接口规范
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 22:48
 */
public interface OuterEnvironment {
    int
            IN_CALLABLE_EXECUTABLE_CONTROL = 0,
            IN_BLOCK_EXECUTABLE_CONTROL = 1,
            IN_CALLABLE_ARGUMENT = 2,
            IN_ARRAY_INIT_ELEMENT = 3,
            IN_DECLARE = 4,
            IN_STRUCT_CLONE = 5,
            IN_CONDITION = 6;

    MemberManager getMemberManager();

    MemberSupplier createFromMemberManager(SourcePosition using, String name);

    boolean isType(int type);

    PossibleCallableSupplier createPossibleFromMemberManager(SourcePosition using, String callableName);

    DIdentifierManager getIdentifierManager();

    MemberType relateParameterizedType(ParameterizedType<ReferenceElement> element);

    OuterEnvironment getOuter();

    int getType();

    RelationManager getRelationManager();

    /**
     * 类型已经确定
     */
    boolean typeDetermined();

    /**
     * 获取类
     */
    MemberType determinedType();

}
