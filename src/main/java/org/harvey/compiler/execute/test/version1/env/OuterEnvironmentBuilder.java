package org.harvey.compiler.execute.test.version1.env;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.manager.RelationManager;
import org.harvey.compiler.execute.test.version1.msg.MemberType;

/**
 * 用outer重新构建一个{@link DefaultOuterEnvironment}, 因为default能直接获取各种工具, 比较方便
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 22:46
 */
public class OuterEnvironmentBuilder {
    private OuterEnvironment outer;
    @Accessors(chain = true)
    @Setter
    private MemberManager memberManager;
    @Accessors(chain = true)
    @Setter
    private DIdentifierManager identifierManager;
    @Accessors(chain = true)
    private RelationManager relationManager;
    private int type;
    @Accessors(chain = true)
    @Setter
    private MemberType determinedType;

    public OuterEnvironmentBuilder() {

    }

    public OuterEnvironmentBuilder outer(OuterEnvironment outerEnvironment) {
        this.outer = outerEnvironment.getOuter();
        this.type = outerEnvironment.getType();
        this.memberManager = outerEnvironment.getMemberManager();
        this.identifierManager = outerEnvironment.getIdentifierManager();
        this.relationManager = outerEnvironment.getRelationManager();
        this.determinedType = outerEnvironment.determinedType();
        return this;
    }

    public DefaultOuterEnvironment build() {
        return new DefaultOuterEnvironment(
                outer, type, memberManager, identifierManager, relationManager, determinedType);
    }
}
