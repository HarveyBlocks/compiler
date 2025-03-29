package org.harvey.compiler.declare.analysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 修饰, 例如{@link EmbellishWord}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 12:59
 */
@Getter
@AllArgsConstructor
public class Embellish {
    public static final Embellish NONE_EMBELLISH = new Embellish((byte) 0);

    private final byte code;

    public Embellish(Set<EmbellishWord> words) {
        if (words == null) {
            this.code = 0;
            return;
        }
        byte code = 0;
        for (EmbellishWord word : words) {
            code |= word.getCode();
        }
        this.code = code;
    }

    /**
     * @param onDefault        {@link #defaultWord(DetailedDeclarationType)}
     * @param onIllegal        {@link #illegalWord(DetailedDeclarationType)}
     * @param isMethod         {@link EmbellishSource#contradictoryOnMethod()}
     * @param constForOperator 如果使用Operator,这个Operator是否是read-only
     */
    public static Embellish create(
            DetailedDeclarationType onDefault, DetailedDeclarationType onIllegal,
            EmbellishSource source,
            boolean isMethod, boolean constForOperator) {
        if (isMethod) {
            source.contradictoryOnMethod();
        }
        Set<EmbellishWord> words = defaultWord(onDefault);
        for (EmbellishWord word : illegalWord(onIllegal)) {
            SourcePosition position = word.at(source);
            if (position != null) {
                throw new AnalysisExpressionException(position, onIllegal + "can not embellish " + word);
            }
        }
        return new Embellish(concat(words, source, constForOperator));
    }

    private static Set<EmbellishWord> concat(
            Set<EmbellishWord> words, EmbellishSource source,
            boolean constForOperator) {
        Set<EmbellishWord> wordsVar = new HashSet<>(words);
        if (source.getAbstractMark() != null) {
            wordsVar.add(EmbellishWord.ABSTRACT);
        }
        if (source.getSealedMark() != null) {
            wordsVar.add(EmbellishWord.SEALED);
        }
        if (source.getStaticMark() != null) {
            wordsVar.add(EmbellishWord.SEALED);
        }
        if (source.getFinalMark() != null) {
            wordsVar.add(EmbellishWord.FINAL);
        }
        if (source.getConstMark() != null || constForOperator) {
            wordsVar.add(EmbellishWord.CONST);
        }
        return wordsVar;
    }

    /**
     * return true && 没有加->自动加;
     * return true && 加了->不动;
     * return false && 加了->报错;
     * return false && 不加->不懂;
     */
    public static boolean readOnlyOperator(Operator operator, SourcePosition constMark) {
        if (Operators.cannotReloadable(operator)) {
            throw new CompilerException(operator + " can not reload");
        }
        boolean canEmbellishConst = !(Operators.isAssign(operator) || Operator.LEFT_INCREASING == operator ||
                                      Operator.RIGHT_INCREASING == operator || Operator.LEFT_DECREASING == operator ||
                                      Operator.RIGHT_DECREASING == operator);

        if (canEmbellishConst) {
            return true;
        } else if (constMark != null) {
            throw new AnalysisExpressionException(constMark, operator.getName() + "can not embellish const");
        } else {
            return false;
        }
    }

    /**
     * @param type {@link DetailedDeclarationType#FILE_STRUCTURE},
     *             {@link DetailedDeclarationType#FILE_INTERFACE},
     *             {@link DetailedDeclarationType#FIELD},
     *             {@link DetailedDeclarationType#METHOD},
     *             {@link DetailedDeclarationType#OPERATOR},
     *             {@link DetailedDeclarationType#INNER_STRUCTURE},
     *             {@link DetailedDeclarationType#FUNCTION}
     */
    public static Set<EmbellishWord> illegalWord(DetailedDeclarationType type) {
        if (type == null) {
            return Collections.emptySet();
        }
        switch (type) {
            case FILE_STRUCTURE:
                return Set.of(EmbellishWord.STATIC, EmbellishWord.FINAL, EmbellishWord.CONST);
            case FILE_INTERFACE:
                return Set.of(EmbellishWord.SEALED, EmbellishWord.FINAL, EmbellishWord.STATIC, EmbellishWord.CONST);
            case FIELD:
                return Set.of(EmbellishWord.SEALED, EmbellishWord.ABSTRACT);
            case METHOD:
                return Set.of(EmbellishWord.FINAL);
            case OPERATOR:
                return Set.of(EmbellishWord.STATIC, EmbellishWord.FINAL);
            case INNER_STRUCTURE: // inner class
                return Set.of(EmbellishWord.CONST, EmbellishWord.FINAL);
            case FUNCTION:
                return Set.of(EmbellishWord.STATIC, EmbellishWord.FINAL, EmbellishWord.CONST, EmbellishWord.SEALED,
                        EmbellishWord.ABSTRACT
                );
            default:
                throw new CompilerException("Unexpected value: " + type);

        }
    }

    /**
     * @param type {@link DetailedDeclarationType#FILE_INTERFACE},
     *             {@link DetailedDeclarationType#INTERFACE_METHOD},
     *             {@link DetailedDeclarationType#INTERFACE_FIELD},
     *             {@link DetailedDeclarationType#INTERFACE_INNER_STRUCTURE},
     *             {@link DetailedDeclarationType#STRUCTURE_INNER_INTERFACE},
     *             {@link DetailedDeclarationType#FILE_STRUCT},
     *             {@link DetailedDeclarationType#STRUCT_FIELD},
     *             {@link DetailedDeclarationType#STRUCT_METHOD},
     *             {@link DetailedDeclarationType#STRUCT_OPERATOR},
     */
    public static Set<EmbellishWord> defaultWord(DetailedDeclarationType type) {
        if (type == null) {
            return Collections.emptySet();
        }
        switch (type) {
            case FILE_INTERFACE:
            case INTERFACE_METHOD_OR_OPERATOR:
                return Set.of(EmbellishWord.ABSTRACT);
            case INTERFACE_FIELD:
                return Set.of(EmbellishWord.STATIC, EmbellishWord.FINAL);
            case INTERFACE_INNER_STRUCTURE:
            case STRUCTURE_INNER_INTERFACE:
                return Set.of(EmbellishWord.STATIC);
            case FILE_STRUCT: // inner class
                return Set.of(EmbellishWord.FINAL);
            case STRUCT_FIELD:
                return Set.of(EmbellishWord.FINAL, EmbellishWord.CONST);
            case STRUCT_METHOD_OR_OPERATOR:
                return Set.of(EmbellishWord.SEALED, EmbellishWord.CONST);
            default:
                throw new CompilerException("unexpected value");
        }
    }


    public boolean isMarkedConstant() {
        return (code & EmbellishWord.CONST.getCode()) != 0;
    }

    public boolean isMarkedStatic() {
        return (code & EmbellishWord.STATIC.getCode()) != 0;
    }

    public boolean isMarkedFinal() {
        return (code & EmbellishWord.FINAL.getCode()) != 0;
    }

    public boolean isMarkedSealed() {
        return (code & EmbellishWord.SEALED.getCode()) != 0;
    }

    public boolean isMarkedAbstract() {
        return (code & EmbellishWord.ABSTRACT.getCode()) != 0;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" ", "[", "]");
        if (isMarkedAbstract()) {
            joiner.add("abstract");
        }
        if (isMarkedStatic()) {
            joiner.add("static");
        }
        if (isMarkedConstant()) {
            joiner.add("const");
        }
        if (isMarkedFinal()) {
            joiner.add("final");
        }
        if (isMarkedSealed()) {
            joiner.add("sealed");
        }
        return joiner.toString();
    }

    @AllArgsConstructor
    @Getter
    public enum EmbellishWord {
        STATIC((byte) 1), FINAL((byte) 2), CONST((byte) 4), SEALED((byte) 8), ABSTRACT((byte) 16);
        private final byte code;


        public SourcePosition at(EmbellishSource source) {
            switch (this) {
                case STATIC:
                    return source.getStaticMark();
                case FINAL:
                    return source.getFinalMark();
                case CONST:
                    return source.getConstMark();
                case SEALED:
                    return source.getSealedMark();
                case ABSTRACT:
                    return source.getAbstractMark();
                default:
                    throw new CompilerException("Unexpected value: " + this);
            }
        }


    }
}