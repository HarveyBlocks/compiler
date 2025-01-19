package org.harvey.compiler.declare;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 12:59
 */
@Getter
@AllArgsConstructor
public class Embellish {
    public static final Embellish NONE_EMBELLISH = new Embellish((byte) 0);
    public static final int CONST_MARK = 1;
    public static final int STATIC_MARK = 2;
    public static final int FINAL_MARK = 4;
    public static final int SEALED_MARK = 8;
    public static final int ABSTRACT_MARK = 16;
    private final byte code;

    public Embellish(EmbellishSourceString embellish, Keyword... plus) {
        byte code = 0;
        if (embellish.constMark != null) {
            code |= CONST_MARK;
        }
        if (embellish.staticMark != null) {
            if (embellish.finalMark != null) {
                throw new AnalysisExpressionException(embellish.finalMark.getPosition(), "conflict with static");
            }
            if (embellish.sealedMark != null) {
                throw new AnalysisExpressionException(embellish.sealedMark.getPosition(), "conflict with static");
            }
            if (embellish.abstractMark != null) {
                throw new AnalysisExpressionException(embellish.abstractMark.getPosition(), "conflict with static");
            }
            code |= STATIC_MARK;
        }
        if (embellish.finalMark != null) {
            code |= FINAL_MARK;
        }
        if (embellish.sealedMark != null) {
            code |= SEALED_MARK;
        }
        if (embellish.abstractMark != null) {
            code |= ABSTRACT_MARK;
        }
        if (plus != null) {
            for (Keyword keyword : plus) {
                code |= mark(keyword);
            }
        }
        this.code = code;
    }


    private static byte mark(Keyword keyword) {
        switch (keyword) {
            case CONST:
                return CONST_MARK;
            case STATIC:
                return STATIC_MARK;
            case SEALED:
                return SEALED_MARK;
            case FINAL:
                return FINAL_MARK;
            case ABSTRACT:
                return ABSTRACT_MARK;
            default:
                throw new CompilerException("Unknown embellish");
        }
    }

    public boolean isMarkedConstant() {
        return (code & CONST_MARK) != 0;
    }

    public boolean isMarkedStatic() {
        return (code & STATIC_MARK) != 0;
    }

    public boolean isMarkedFinal() {
        return (code & FINAL_MARK) != 0;
    }

    public boolean isMarkedSealed() {
        return (code & SEALED_MARK) != 0;
    }

    public boolean isMarkedAbstract() {
        return (code & ABSTRACT_MARK) != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isMarkedAbstract()) {
            sb.append("abstract ");
        }
        if (isMarkedAbstract()) {
            sb.append("abstract ");
        }
        if (isMarkedStatic()) {
            sb.append("static ");
        }
        if (isMarkedConstant()) {
            sb.append("const ");
        }
        if (isMarkedFinal()) {
            sb.append("final ");
        }
        if (isMarkedSealed()) {
            sb.append("sealed ");
        }
        return sb.toString();
    }
}