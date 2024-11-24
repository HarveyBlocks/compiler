package org.harvey.compiler.common.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.SystemConstant;

/**
 * 源码中的行列
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 12:22
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class SourcePosition implements Cloneable {

    /**
     * 行, 第几行
     */
    private int raw;
    /**
     * 列, 行中的第几个字
     */
    private int column;

    public void rawIncreasing() {
        raw++;
        column = 0;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return clone(0, 0);
    }

    public void columnIncreasing() {
        column++;
    }

    public void columnDecreasing() {
        column--;
    }


    public void columnAdding(int delta) {
        column += delta;
    }

    public SourcePosition clone(int rawDelta, int columnDelta) {
        return new SourcePosition(raw + rawDelta, column + columnDelta);
    }

    /**
     *
     * @return 移动后的新值
     */
    public static SourcePosition moveToEnd(SourcePosition start, String value) {
        int lineCounter = 0;
        int fromIndex = 0;
        int index = 0;
        while (true) {
            index = value.indexOf(SystemConstant.LINE_SEPARATOR, fromIndex);
            if (index < 0) {
                break;
            }
            fromIndex = index + SystemConstant.LINE_SEPARATOR.length();
            lineCounter++;
        }
        return new SourcePosition(
                start.getRaw() + lineCounter,
                value.length() + (lineCounter == 0 ? start.getColumn() : -fromIndex)
        );
    }

    @Override
    public String toString() {
        return "[" + raw + ":" + column + "]";
    }
}
