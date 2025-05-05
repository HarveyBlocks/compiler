package org.harvey.compiler.execute.test.version5.msg.stack;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.execute.test.version5.handler.ConstantExpressionHandler;
import org.harvey.compiler.execute.test.version5.msg.Label;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-10 15:53
 */
@Getter
public class SwitchBody {
    private final Label switchTable;
    private final Label switchBreak;
    private final List<Pair<ConstantExpressionHandler.ConstantResult, Label>> caseConditionLabelPair;
    @Setter
    private Label defaultLabel;

    public SwitchBody(Label switchTable, Label switchBreak) {
        this.switchTable = switchTable;
        this.switchBreak = switchBreak;
        this.caseConditionLabelPair = new ArrayList<>();
        this.defaultLabel = null;
    }

    /**
     * ConstantExpressionHandler.ConstantResult calculate = context.constantExpressionHandler()
     * .calculate(condition);
     */
    public void registerCase(ConstantExpressionHandler.ConstantResult constantResult, Label caseLabel) {
        caseConditionLabelPair.add(new Pair<>(constantResult, caseLabel));
    }
}
