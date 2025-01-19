package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.exception.CompilerException;

/**
 * 基本数据类型<br>
 * 3. int8 int32, bool等<br>
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 19:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BasicType extends SourceType {
    private final Keyword type;

    public BasicType(Keyword type) {
        if (!Keywords.isBasicType(type)) {
            throw new CompilerException("Keyword '" + type + "' should be a basic type",
                    new IllegalArgumentException());

        }
        this.type = type;
    }
}
