package org.harvey.compiler.execute.control;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.execute.expression.Expression;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 00:29
 */
@Getter
public class SwitchStart extends BodyStart {
    /**
     * 由于DEFAULT之前一定有switch, 所以是0没问题
     */
    public static final int NO_DEFAULT = 0;
    public static final Byte CODE = 17;
    private final Expression condition;
    private final List<Integer> caseList = new ArrayList<>();
    @Setter
    private int defaultPlaceholder = NO_DEFAULT;

    public SwitchStart(Expression condition) {
        this.condition = condition;
    }

    public static Executable in(InputStream is) {
        int bodyEnd = readInteger(is);
        Expression condition = readExpression(is);
        SwitchStart switchStart = new SwitchStart(condition);
        switchStart.setBodyEnd(bodyEnd);
        switchStart.caseList.addAll(readCases(is));
        switchStart.setDefaultPlaceholder(readInteger(is));
        return switchStart;
    }

    private static ArrayList<Integer> readCases(InputStream is) {
        int size = readInteger(is);
        ArrayList<Integer> ls = new ArrayList<>(size);
        while (size-- > 0) {
            ls.add(readInteger(is));
        }
        return ls;
    }

    public void addCase(Integer casePlaceholder) {
        caseList.add(casePlaceholder);
    }

    @Override
    public byte getCode() {
        return CODE;
    }

    @Override
    public int out(OutputStream os) {
        return super.out(os) + writeExpression(os, condition) + writeCases(os, this.caseList) +
               writeInteger(os, this.defaultPlaceholder);
    }

    private int writeCases(OutputStream os, List<Integer> caseList) {
        int length = writeInteger(os, caseList.size());
        for (Integer i : caseList) {
            length += writeInteger(os, i);
        }
        return length;
    }
}
