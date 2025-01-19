package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 不确定的数据类型, 就是指var<br>
 * 1. var<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 19:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UnsureType extends SourceType {
    private volatile static UnsureType single = null;

    private UnsureType() {
    }

    public static UnsureType instance() {
        if (single != null) {
            return single;
        }
        synchronized (UnsureType.class) {
            if (single != null) {
                return single;
            }
            single = new UnsureType();
        }
        return single;
    }

    private Object readResolve() {
        return this;
    }
}
