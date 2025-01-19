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
public class GotoString extends SourceString {
    private volatile LabelString label;

    private GotoString(String labelName, SourcePosition position) {
        super(SourceStringType.GOTO, labelName, position);
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