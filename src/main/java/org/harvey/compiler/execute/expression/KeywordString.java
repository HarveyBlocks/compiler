package org.harvey.compiler.execute.expression;

import lombok.Getter;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.exception.command.CommandException;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.RawType;

/**
 * 表达式中的关键字
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:49
 */
@Getter
public class KeywordString extends ExpressionElement implements RawType, ItemString {    // 大端
    private final Keyword keyword;

    public KeywordString(SourcePosition sp, Keyword keyword) {
        super(sp);
        this.keyword = keyword;
        if (Keywords.isOperator(keyword)) {
            throw new CommandException("can not be as a key word, please use as operator");
        }
    }


}
