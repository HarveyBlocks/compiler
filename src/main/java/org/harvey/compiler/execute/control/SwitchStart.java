package org.harvey.compiler.execute.control;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.execute.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO  
 *
 * @date 2025-01-09 00:29
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class SwitchStart extends BodyStart {
    private final Expression condition;
    private final List<Case> caseList = new ArrayList<>();
    @Setter
    private Default defaultPlaceholder;

    public SwitchStart(Expression condition) {
        this.condition = condition;
    }

    public void addCase(Case casePlaceholder) {
        caseList.add(casePlaceholder);
    }

}
