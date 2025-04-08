package org.harvey.compiler.execute.test.version1.env;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.execute.test.version1.msg.UnsureConstructorSupplier;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

/**
 * 默认的outer环境实现, 给出各种需要的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-04 22:12
 */
@AllArgsConstructor
@Getter
public class DefaultOuterEnvironment implements OuterEnvironment {
    private final OuterEnvironment outer;
    private final int type;
    private final MemberManager memberManager;
    private final IdentifierManager identifierManager;
    private final RelationManager relationManager;
    private final MemberType determinedType;

    @Override
    public MemberManager getMemberManager() {
        return memberManager;
    }

    @Override
    public MemberSupplier createFromMemberManager(SourcePosition using, String name) {
        return memberManager.create(using, name);
    }

    @Override
    public boolean isType(int type) {
        return this.type == type;
    }

    @Override
    public PossibleCallableSupplier createPossibleFromMemberManager(SourcePosition using, String callableName) {
        return memberManager.createPossibleCallable(using, callableName);
    }

    @Override
    public MemberType relateParameterizedType(ParameterizedType<ReferenceElement> element) {
        return relationManager.relateParameterizedType(element);
    }



    @Override
    public boolean typeDetermined() {
        return determinedType != null;
    }

    @Override
    public MemberType determinedType() {
        return determinedType;
    }




}

