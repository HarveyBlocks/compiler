package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.io.source.SourcePosition;

/**
 * 为了 Type t = new();的语法糖
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-06 16:46
 */
public class UnsureConstructorSupplier extends PossibleCallableSupplier {
    /**
     * nullable
     */
    private final MemberSupplier previousLimit;

    public UnsureConstructorSupplier(
            SourcePosition position, MemberSupplier previousLimit) {
        super(position, null);
        // ??? = new()
        // ??? = A.B.C.new()
        // ??? = obj.new()
        // 前面也要有所限制啊
        this.previousLimit = previousLimit;
    }

    public boolean sure() {
        return super.possible != null;
    }

    public void makeSure(MemberType determinedType) {
        this.resetPossibleCallableRelatedDeclare(
                determinedType.findConstructor(getPosition()).getPossibleCallableRelatedDeclare());
    }
}
