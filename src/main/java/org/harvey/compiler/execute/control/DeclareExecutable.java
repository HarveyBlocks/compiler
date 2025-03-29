package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.local.LocalType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 声明语句
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 13:22
 */
@Getter
@AllArgsConstructor
public class DeclareExecutable extends SequentialExecutable {
    public static final Byte CODE = 6;
    private final LocalType type;
    private final IdentifierString identifier;

    public static Executable in(InputStream is) {
        LocalType type = LT_S.in(is);
        IdentifierString identifier = IS_S.in(is);
        return new DeclareExecutable(type, identifier);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return LT_S.out(os, type) + IS_S.out(os, identifier);
    }
}
