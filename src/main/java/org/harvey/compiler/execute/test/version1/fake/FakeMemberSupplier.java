package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-11 00:00
 */
public class FakeMemberSupplier extends MemberSupplier {
    private final MemberType memberType;

    public FakeMemberSupplier(SourcePosition position, MemberType memberType) {
        super(position);
        this.memberType = memberType;
    }

    @Override
    public MemberType getType() {
        return memberType;
    }

    @Override
    public String show() {
        return getPosition() + "";
    }
}
