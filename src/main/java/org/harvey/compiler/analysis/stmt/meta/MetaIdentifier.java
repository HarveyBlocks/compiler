package org.harvey.compiler.analysis.stmt.meta;

import lombok.Getter;
import lombok.ToString;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.exception.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 22:34
 */
@Getter
@ToString(callSuper = true)
public abstract class MetaIdentifier extends MetaMessage {
    protected String identifier;

    public MetaIdentifier() {
        this.identifier = null;
    }

    public MetaIdentifier(SourceString identifier) {
        super();
        setIdentifier(identifier);
    }

    protected void setIdentifier(SourceString identifier) {
        if (identifier.getType() != SourceStringType.KEYWORD) {
            throw new CompilerException("Illegal");
        }
        super.declare = identifier.getPosition();
        this.identifier = identifier.getValue();
    }
}
