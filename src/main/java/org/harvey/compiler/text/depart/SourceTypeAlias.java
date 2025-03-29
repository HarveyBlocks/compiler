package org.harvey.compiler.text.depart;

import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * 源码中的Alias
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-30 18:51
 */
@Getter
public class SourceTypeAlias {
    private final SourceTextContext permissions;
    /**
     * file级别应该是null
     */
    private final SourcePosition staticPosition;
    private final IdentifierString identifier;
    private final SourceTextContext genericMessage;
    private final SourceTextContext origin;


    public SourceTypeAlias(
            SourceTextContext permissions, SourcePosition staticPosition, IdentifierString identifier,
            SourceTextContext genericMessage,
            SourceTextContext origin) {
        this.permissions = permissions;
        this.staticPosition = staticPosition;
        this.identifier = identifier;
        this.genericMessage = genericMessage;
        this.origin = origin;
    }
}
