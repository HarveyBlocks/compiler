package org.harvey.compiler.common.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 源码中的每一个部分
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:25
 */
@Getter
@ToString
@EqualsAndHashCode
public class SourceString {

    @Setter
    private SourceStringType type;
    @Setter
    private String value;
    private final SourcePosition position;

    public SourceString(SourceStringType type, String value, SourcePosition position) {
        this.type = type;
        this.value = value;
        this.position = (SourcePosition) position.clone();
    }

    public int getStartRow() {
        // TODO
        return -1;
    }

    public int getStartCol() {
        // TODO
        return -1;
    }
}
