package org.harvey.compiler.execute.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.ArrayList;

/**
 * type identifier [assign];
 *
 * @date 2025-01-10 20:48
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public class LocalVariableDeclare {
    private final boolean markedConst;
    private final boolean markedFinal;
    /**
     * 多个变量共享一个type...这好吗? 这不好... 不好吗?
     */
    private final Expression type;
    private final IdentifierString identifier;
    // int a = 2, b ,c = 3, e, f, g;
    // 一开始identifier的, 后面道是
    // 先看前面的俩, 然后后面的作为表达式区分类型, 区分完类型之后呢, 区分出逗号是第一层的, 然后获取
    private final Expression assign;

    public LocalVariableDeclare(SourceVariableDeclare declare) {
        this.markedConst = declare.getEmbellish().getConstMark() != null;
        this.markedFinal = declare.getEmbellish().getFinalMark() != null;
        this.type = ExpressionFactory.type(declare.getType());
        this.assign = ExpressionFactory.depart(declare.getAssign());
        ExpressionElement element = assign.get(0);
        if (!(element instanceof IdentifierString)) {
            throw new AnalysisExpressionException(element.getPosition(), "expected a identifier");
        }
        this.identifier = (IdentifierString) element;
    }


    public ArrayList<LocalVariableDeclare> depart() {
        return assign.splitWithComma(this::checkDeclare);
    }

    private LocalVariableDeclare checkDeclare(Expression part, SourcePosition position) {
        if (part.isEmpty()) {
            throw new AnalysisExpressionException(position, "expected an identifier");
        }
        ExpressionElement identifier = part.get(0);
        if (!(identifier instanceof IdentifierString)) {
            throw new AnalysisExpressionException(identifier.getPosition(), "expected an identifier");
        }
        return new LocalVariableDeclare(markedConst, markedFinal, type, (IdentifierString) identifier, part);
    }


}
