package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.declare.context.CallableContext;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 22:10
 */
@Getter
public class CatchStart extends BodyStart {
    public static final Byte CODE = 4;
    private static final CallableContext.Serializer EE_S = null;
    private final IdentifierString exceptionIdentifier;
    private final ArrayList<LocalType> exceptionTypes;

    public CatchStart(ArrayList<LocalType> exceptionTypes, IdentifierString exceptionIdentifier) {
        this.exceptionTypes = exceptionTypes;
        this.exceptionIdentifier = exceptionIdentifier;
        if (this.exceptionTypes == null) {
            throw new CompilerException("exception types can not be null");
        } else if (this.exceptionIdentifier == null) {
            throw new CompilerException("exception identifier can not be null ");
        }
    }

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
//        ExpressionElement ee = EE_S.in(is);
        ArrayList<LocalType> localTypes = StreamSerializerUtil.collectionIn(is, LT_S);
        CatchStart result = null;
       /* if (ee instanceof IdentifierString) {
            result = new CatchStart(localTypes, (IdentifierString) ee);
        } else {
            throw new CompilerException("only IdentifierString");
        }*/
        result.setBodyEnd(bodyEnd);
        return result;
    }

    public boolean isIgnore() {
        return exceptionIdentifier.isIgnore();
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return super.out(os) + EE_S.out(os, null/*exceptionIdentifier*/) +
               StreamSerializerUtil.collectionOut(os, exceptionTypes, LT_S);
    }
}
