package org.harvey.compiler.analysis.stmt.phaser.depart;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.common.entity.SourceString;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DepartedStatementWithBodyPart extends DepartedPart {
    private final SourceTextContext statement;
    private final SourceTextContext body;

    public DepartedStatementWithBodyPart(SourceTextContext part) {
        boolean isBody = false;
        statement = new SourceTextContext();
        body = new SourceTextContext();
        for (SourceString sourceString : part) {
            String value = sourceString.getValue();
            if (!isBody && value.equals(String.valueOf(PropertyConstant.BODY_START))) {
                isBody = true;
            }
            if (isBody) {
                body.add(sourceString);
            } else {
                statement.add(sourceString);
            }
        }
    }
}
