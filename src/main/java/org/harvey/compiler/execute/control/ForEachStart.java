package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.local.LocalType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:40
 */
@Getter
@AllArgsConstructor
public class ForEachStart extends BodyStart {
    public static final Byte CODE = 13;
    private final LocalType itemType;
    private final IdentifierString itemIdentifier;
    // range(1,2)
    // map.keys();
    private final Expression list;

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
        LocalType itemType = LT_S.in(is);
        IdentifierString itemIdentifier = IS_S.in(is);
        Expression list = readExpression(is);
        ForEachStart forEachStart = new ForEachStart(itemType, itemIdentifier, list);
        forEachStart.setBodyEnd(bodyEnd);
        return forEachStart;
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return super.out(os) + LT_S.out(os, this.itemType) + IS_S.out(os, this.itemIdentifier) +
               writeExpression(os, this.list);
    }
}
