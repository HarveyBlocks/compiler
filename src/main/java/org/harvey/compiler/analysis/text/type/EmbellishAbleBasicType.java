package org.harvey.compiler.analysis.text.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.exception.CompilerException;

/**
 * 有修饰前缀的类型<br>
 * 4. unsigned int8<br>
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 19:56
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class EmbellishAbleBasicType extends BasicType {
    private final Keyword embellish;

    public EmbellishAbleBasicType(Keyword embellish, Keyword type) {
        super(type);
        if (!Keywords.isBasicTypeEmbellish(embellish)) {
            throw new CompilerException("Keyword '" + embellish + "' should be a basic type embellish " +
                    "like signed or unsigned", new IllegalArgumentException());
        }
        this.embellish = embellish;
    }
}
