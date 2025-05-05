package org.harvey.compiler.syntax;

import lombok.Getter;
import org.harvey.compiler.execute.test.version1.element.TypeString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.raw.KeywordBasicType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 01:10
 */
@Getter
public class BasicTypeString implements TypeString {
    private final KeywordBasicType keywordBasicType;
    private final SourcePosition position;

    public BasicTypeString(SourcePosition position, KeywordBasicType keywordBasicType) {
        this.keywordBasicType = keywordBasicType;
        this.position = position;
    }


}
