package org.harvey.compiler.execute.test.version2.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-08 22:40
 */
@Getter
@Setter
@AllArgsConstructor
public class LineWarp {
    private int value;


    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
