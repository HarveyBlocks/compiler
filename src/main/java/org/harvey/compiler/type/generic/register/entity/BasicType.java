package org.harvey.compiler.type.generic.register.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.syntax.BasicTypeString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
@AllArgsConstructor
@Getter
public class BasicType implements EndType {
    private final BasicTypeString basic;

    @Override
    public SourcePosition getPosition() {
        return basic.getPosition();
    }
}
