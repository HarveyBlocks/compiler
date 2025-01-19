package org.harvey.compiler.analysis.core;

import org.harvey.compiler.common.util.Singleton;
import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 21:51
 */
public class UnsureAccessControl extends AccessControl {
    private static final Singleton<UnsureAccessControl> CONTROL_SINGLETON = new Singleton<>();

    private UnsureAccessControl(int code) {
        super(code);
    }

    public static UnsureAccessControl instance() {
        return CONTROL_SINGLETON.instance(() -> new UnsureAccessControl(0));
    }

    @Override
    public boolean canPublic() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canPackage() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canChildrenPackage() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canChildrenClass() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canFile() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canSelfClass() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public boolean canChildInternalClass() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }

    @Override
    public byte getByte() {
        throw new CompilerException("Unsure access control, can not invoke this method",
                new UnsupportedOperationException());
    }
}
