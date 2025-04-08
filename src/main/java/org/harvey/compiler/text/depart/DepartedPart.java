package org.harvey.compiler.text.depart;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

/**
 * 依据`{}`分解结构
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 18:33
 */
@EqualsAndHashCode
@Data
@Getter
public class DepartedPart {

    private final SourceTextContext statement;
    private final SourceTextContext body;
    private final boolean sentenceEnd;

    public DepartedPart(SourceTextContext part) {
        sentenceEnd = isSentenceEnd(part);
        statement = new SourceTextContext();
        body = new SourceTextContext();
        boolean isBody = false;
        for (SourceString sourceString : part) {
            String value = sourceString.getValue();
            if (!isBody && value.equals(String.valueOf(SourceFileConstant.BODY_START))) {
                isBody = true;
            }
            if (isBody) {
                body.add(sourceString);
            } else {
                statement.add(sourceString);
            }
        }

    }

    public DepartedPart(SourceTextContext statement, SourceTextContext body) {
        this.statement = statement;
        this.body = body;
        if (body != null && !body.isEmpty()) {
            sentenceEnd = false;
        } else {
            sentenceEnd = isSentenceEnd(statement);
        }
    }

    private boolean isSentenceEnd(SourceTextContext part) {
        if (part == null || part.isEmpty()) {
            throw new CompilerException("Part should not be null or empty.");
        }
        SourceString last = part.getLast();
        SourceType type = last.getType();
        String value = last.getValue();
        if (type != SourceType.SIGN) {
            throw new CompilerException(last.getPosition() + ":" + type + " is illegal to be the last of part");
        } else if (value == null || value.length() != 1) {
            throw new CompilerException(last.getPosition() + ":" + value + " is illegal to be the last of part");
        }
        char c = value.charAt(0);
        if (c != SourceFileConstant.SENTENCE_END && c != SourceFileConstant.BODY_END) {
            throw new CompilerException(last.getPosition() + ":" + value + " is illegal to be the last of part");
        }
        return SourceFileConstant.SENTENCE_END == c;
    }

    public SourcePosition getPosition() {
        if (!statement.isEmpty()) {
            return statement.getFirst().getPosition().clone(0, 0);
        } else if (!body.isEmpty()) {
            return body.getFirst().getPosition().clone(-1, -1);
        } else {
            throw new CompilerException("Empty departed part", new IllegalStateException());
        }
    }
}
