package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-30 18:51
 */
@Getter
@AllArgsConstructor
public class SourceTypeAlias {
    private final SourceTextContext permissions;
    private final SourceTextContext alias;
    private final SourceTextContext origin;
}
