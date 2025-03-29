package org.harvey.compiler.execute.control;

import lombok.Getter;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.local.LocalType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:40
 */
@Getter
public class ForIndexStart extends BodyStart {
    public static final Byte CODE = 14;
    private final boolean declare;
    // assign和type全部放到init里去
    private final LocalType type;
    // 可以为empty
    private final ArrayList<Expression> init;
    // 不可null, 可empty
    private final Expression condition;
    // 不可null, 可empty
    private final Expression step;

    public ForIndexStart(
            LocalType localType, Expression assign, Expression condition,
            Expression step) {
        declare = true;
        this.type = localType;
        this.init = assign.splitWithComma();
        this.condition = condition;
        this.step = step;
    }

    public ForIndexStart(
            Expression init, Expression condition,
            Expression step) {
        declare = false;
        this.type = null;
        this.init = init.splitWithComma();
        this.condition = condition;
        this.step = step;
    }

    private ForIndexStart(
            int bodyEnd, boolean declare, LocalType unDepartedDeclareType, ArrayList<Expression> init,
            Expression condition,
            Expression step) {
        super(bodyEnd);
        this.declare = declare;
        this.type = unDepartedDeclareType;
        this.init = init;
        this.condition = condition;
        this.step = step;
    }

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
        boolean declare;
        try {
            declare = is.readNBytes(1)[0] != 0;
        } catch (IOException e) {
            throw new CompilerFileReadException();
        }
        LocalType type = declare ? LT_S.in(is) : null;
        int size = readInteger(is);
        ArrayList<Expression> init = new ArrayList<>(size);
        while (size-- > 0) {
            init.add(readExpression(is));
        }
        Expression condition = readExpression(is);
        Expression step = readExpression(is);
        return new ForIndexStart(bodyEnd, declare, type, init, condition, step);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        int length = super.out(os);
        try {
            os.write(new byte[]{(byte) (declare ? 1 : 0)});
            length += 1;
        } catch (IOException e) {
            throw new CompilerFileReadException();
        }
        if (declare) {
            length += LT_S.out(os, this.type);
        }
        length += writeInteger(os, this.init.size());
        for (Expression each : this.init) {
            length += writeExpression(os, each);
        }
        length += writeExpression(os, this.condition);
        length += writeExpression(os, this.step);
        return length;
    }

}
