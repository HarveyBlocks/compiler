package org.harvey.compiler.declare;

import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-02 16:25
 */
@Getter
public class EnumConstantDeclarable {
    private final IdentifierString name;
    /**
     * 去除了括号, for use
     */
    private final List<SourceTextContext> argumentList;


    public EnumConstantDeclarable(IdentifierString name, List<SourceTextContext> argumentList) {
        this.name = name;
        this.argumentList = argumentList;
    }
}
