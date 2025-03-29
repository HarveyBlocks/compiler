package org.harvey.compiler.declare.analysis;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 保留SourcePosition, 方便检查错误
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 13:00
 */
@Getter
public class EmbellishSource {
    private SourcePosition constMark = null;
    private SourcePosition staticMark = null;
    private SourcePosition finalMark = null;
    private SourcePosition sealedMark = null;
    private SourcePosition abstractMark = null;

    public boolean isNull() {
        return constMark == null &&
               staticMark == null &&
               finalMark == null &&
               sealedMark == null &&
               abstractMark == null;
    }

    /**
     * 存在即违法
     */
    public void illegalOn(String name, Embellish.EmbellishWord... illegal) {
        if (illegal == null) {
            return;
        }
        for (Embellish.EmbellishWord embellishWord : illegal) {
            SourcePosition position = embellishWord.at(this);
            if (position != null) {
                throw new AnalysisExpressionException(
                        position,
                        name + " use embellish of '" + embellishWord + "' is not allowed."
                );
            }
        }
    }

    /**
     * static不能和sealed, final, constMark共存
     */
    public void contradictoryOnMethod() {
        if (staticMark != null) {
            if (sealedMark != null || abstractMark != null || finalMark != null || constMark != null) {
                throw new AnalysisExpressionException(staticMark, "contradictory with final or abstract or sealed");
            }
        }
    }

    public static class Builder {
        private final EmbellishSource product = new EmbellishSource();

        public Builder setConst(SourcePosition constMark) {
            if (product.constMark != null) {
                throw new AnalysisExpressionException(constMark, "repeated embellish");
            }
            product.constMark = constMark;
            return this;
        }

        public Builder setStatic(SourcePosition staticMark) {
            if (product.staticMark != null) {
                throw new AnalysisExpressionException(staticMark, "repeated embellish");
            }
            product.staticMark = staticMark;
            return this;
        }

        public Builder setFinal(SourcePosition finalMark) {
            if (product.finalMark != null) {
                throw new AnalysisExpressionException(finalMark, "repeated embellish");
            }
            product.finalMark = finalMark;
            return this;
        }

        public Builder setSealed(SourcePosition sealedMark) {
            if (product.sealedMark != null) {
                throw new AnalysisExpressionException(sealedMark, "repeated embellish");
            }
            product.sealedMark = sealedMark;
            return this;
        }

        public Builder setAbstract(SourcePosition abstractMark) {
            if (product.abstractMark != null) {
                throw new AnalysisExpressionException(abstractMark, "repeated embellish");
            }
            product.abstractMark = abstractMark;
            return this;
        }

        public EmbellishSource build() {
            contradictory();
            return product;
        }

        private void contradictory() {
            boolean hasSealed = product.sealedMark != null;
            boolean hasAbstract = product.abstractMark != null;
            boolean hasFinal = product.finalMark != null;

            if (!hasAbstract) {
                if (hasSealed && hasFinal) {
                    throw new AnalysisExpressionException(product.sealedMark, "contradictory with final");
                } else {
                    return;
                }
            }
            if (hasFinal) {
                throw new AnalysisExpressionException(
                        product.finalMark,
                        "contradictory with abstract"
                );
            } else if (hasSealed) {
                throw new AnalysisExpressionException(
                        product.sealedMark,
                        "contradictory with abstract"
                );
            }
        }
    }
}