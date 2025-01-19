package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.LinkedList;
import java.util.List;

/**
 * 分离Word, Char, Integer, Float
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-18 14:59
 */
public class MixedOperatorDecomposer {

    private static final char DECIMAL_POINT = SourceFileConstant.DECIMAL_POINT;
    private final String source;
    private SourcePosition position;

    public MixedOperatorDecomposer(SourceString item) {
        if (item.getType() != SourceStringType.OPERATOR) {
            throw new CompilerException("Only operator is allowed", new IllegalArgumentException());
        }
        this.source = item.getValue();
        if (source == null || source.isEmpty()) {
            throw new CompilerException("text can't be null or empty", new IllegalArgumentException());
        }
        this.position = item.getPosition();
    }

    /**
     * 不检查正确与否, 只确定类型
     */
    public SourceTextContext phase() {
        return new SourceTextContext(phaseValue());
    }

    private List<SourceString> phaseValue() {
        List<String> strings = Operators.trySplit(source, position);
        List<SourceString> operators = new LinkedList<>();
        for (String string : strings) {
            if (Operator.CALLABLE_DECLARE.nameEquals(string)) {
                operators.add(mapToSourceString(Operator.CALL_PRE.getName()));
                operators.add(mapToSourceString(Operator.CALL_POST.getName()));
            } else {
                operators.add(mapToSourceString(string));
            }
        }

        return operators;
    }

    private SourceString mapToSourceString(String name) {
        return new SourceString(
                SourceStringType.OPERATOR, name, biasPosition(name)
        );
    }

    private SourcePosition biasPosition(String s) {
        SourcePosition old = position.clone(0, 0);
        position = SourcePosition.moveToEnd(position, s);
        return old;
    }
}
