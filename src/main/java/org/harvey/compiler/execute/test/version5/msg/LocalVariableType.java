package org.harvey.compiler.execute.test.version5.msg;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 21:02
 */
@Getter
@AllArgsConstructor
public enum LocalVariableType {
    BOOL(1),
    CHAR(2),
    INT8(1),
    UINT8(1),
    INT16(2),
    UINT16(2),
    INT32(4),
    UINT32(4),
    INT64(8),
    UINT64(8),
    FLOAT32(4),
    FLOAT64(8),
    REFERENCE(8);
    private final int typeSize;// 字节

    public String typeName() {
        return name().toLowerCase();
    }
}
