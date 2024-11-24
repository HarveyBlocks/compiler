package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.exception.CompilerException;

/**
 * 描述声明分析过程中的Function类型
 * 5. void func<br>
 * 7. Identifier func<br>
 * 8. (Identifier) func<br>
 * 9. (Identifier1,Identifier2) func<br>
 * 10. (Identifier1,Identifier2) abstract func<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 21:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallableReturnType extends SourceType {
    private final boolean markedAbstract;
    private final SourceType returnType;

    public CallableReturnType(boolean markedAbstract, SourceType returnType) {
        if (returnType instanceof CallableReturnType) {
            throw new CompilerException("return type shouldn't be a function");
        }
        this.markedAbstract = markedAbstract;
        this.returnType = returnType;
    }
}
