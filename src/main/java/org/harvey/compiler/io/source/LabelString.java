package org.harvey.compiler.io.source;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * 源码中的每一个部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:25
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class LabelString extends SourceString {
    /**
     * 只有能{@link GotoString}构造
     */
    LabelString(String labelName, SourcePosition position) {
        super(SourceStringType.LABEL, labelName, position);
    }
}