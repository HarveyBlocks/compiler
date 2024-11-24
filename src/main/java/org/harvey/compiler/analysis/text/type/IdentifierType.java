package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义的类名<br>
 * 2. Identifier<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 19:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IdentifierType extends SourceType {
    private final String type;

    public IdentifierType(String type) {
        this.type = type;
    }
}
