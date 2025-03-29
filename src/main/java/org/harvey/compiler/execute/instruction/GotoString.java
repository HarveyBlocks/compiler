package org.harvey.compiler.execute.instruction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

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
public class GotoString extends SourceString {
    private volatile LabelString label;

    private GotoString(String labelName, SourcePosition position) {
        super(SourceType.GOTO, labelName, position);
    }

    public static GotoString build(String labelName, SourcePosition gotoPosition, SourcePosition position) {
        return build(gotoPosition, new LabelString(labelName, position));
    }

    public static GotoString build(SourcePosition gotoPosition, LabelString label) {
        GotoString gotoString = new GotoString(label.getValue(), gotoPosition);
        gotoString.label = label;
        return gotoString;
    }
}