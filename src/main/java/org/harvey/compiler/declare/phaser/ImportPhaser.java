package org.harvey.compiler.declare.phaser;

import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.depart.DepartedPart;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Import解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 13:04
 */

public class ImportPhaser {

    public static final String IMPORT_END = SourceFileConstant.SENTENCE_END + "";
    public static final String IMPORT_KEYWORD = Keyword.IMPORT.getValue();

    private ImportPhaser() {
    }

    public static boolean isImport(DepartedPart bodyPart) {
        if (!bodyPart.getBody().isEmpty()) {
            return false;
        }
        SourceTextContext sentence = bodyPart.getStatement();
        if (sentence == null || sentence.size() <= 1) {
            return false;
        }
        SourceString first = sentence.getFirst();
        if (first.getType() != SourceStringType.KEYWORD) {
            return false;
        }
        return IMPORT_KEYWORD.equals(first.getValue());
    }

    public static ImportContext phase(DepartedPart part) {
        ListIterator<SourceString> it = part.getStatement().listIterator();
        SourceString first = it.next();
        it.previous();
        it.remove();
        List<SourceString> path = new ArrayList<>();
        for (int i = 0; it.hasNext(); i++) {
            SourceString sourceString = it.next();
            SourceStringType type = sourceString.getType();
            SourceStringType expectedType = (i & 1) == 0 ? SourceStringType.IDENTIFIER : SourceStringType.OPERATOR;
            if (type == SourceStringType.SIGN) {
                if (IMPORT_END.equals(sourceString.getValue())) {
                    break;
                } else {
                    throw new AnalysisExpressionException(sourceString.getPosition(),
                            "Excepted " + expectedType + "; but not " + sourceString.getValue());
                }
            }
            if (type != expectedType) {
                throw new AnalysisExpressionException(sourceString.getPosition(),
                        "Excepted " + expectedType + ", but not " + type);
            }
            if (type == SourceStringType.IDENTIFIER) {
                path.add(sourceString);
            }
        }
        if (path.isEmpty()) {
            throw new AnalysisExpressionException(first.getPosition(), "Import expected a path");
        }
        return new ImportContext(path.toArray(new SourceString[]{}));
    }
}
