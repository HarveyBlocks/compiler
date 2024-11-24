package org.harvey.compiler.analysis.stmt.phaser.depart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class DepartedSentencePart extends DepartedPart {
    private final SourceTextContext sentence;
}
