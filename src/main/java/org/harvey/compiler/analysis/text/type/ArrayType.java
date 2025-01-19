package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.exception.CompilerException;

/**
 * 数组类型
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-24 19:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArrayType extends SourceType {
    private final SourceType elementType;
    private final int dimension;

    public ArrayType(SourceType elementType, int dimension) {
        if (dimension <= 0) {
            throw new CompilerException("Zero or minus dimension array is impossible!");
        }
        this.elementType = elementType;
        this.dimension = dimension;
    }
}
