package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.meta.mi.MetaImport;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedPart;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 13:04
 */
public class ImportPhaser extends SourceContextPhaser {

    public static final String IMPORT_KEYWORD = Keyword.IMPORT.getValue();
    public static final String GET_MEMBER_OPER_NAME = Operator.GET_MEMBER.getName();

    public ImportPhaser(StatementContext context) {
        super(context);
    }

    @Override
    public boolean isTargetPart(DepartedPart bodyPart) {
        return isImport(bodyPart);
    }

    public static boolean isImport(DepartedPart bodyPart) {
        if (!(bodyPart instanceof DepartedSentencePart)) {
            return false;
        }
        DepartedSentencePart sentencePart = (DepartedSentencePart) bodyPart;
        SourceTextContext sentence = sentencePart.getSentence();
        if (sentence == null || sentence.size() <= 1) {
            return false;
        }
        SourceString first = sentence.getFirst();
        if (first.getType() != SourceStringType.KEYWORD) {
            return false;
        }
        return IMPORT_KEYWORD.equals(first.getValue());
    }

    @Override
    public void phase(DepartedPart part) {
        SourceTextContext sentence = ((DepartedSentencePart) part).getSentence();
        List<String> importPackage = new ArrayList<>();
        for (int i = 1; i < sentence.size(); i++) {
            SourceString ss = sentence.get(i);
            SourceStringType exceptType = (i & 1) == 1 ? SourceStringType.IDENTIFIER : SourceStringType.OPERATOR;
            if (ss.getType() != exceptType) {
                throw new AnalysisExpressionException(ss.getPosition(),
                        "Illegal source type of:`" + ss.getType() + "`. Expected for " + exceptType);
            }
            if (exceptType == SourceStringType.IDENTIFIER) {
                importPackage.add(ss.getValue());
                continue;
            }
            if (!GET_MEMBER_OPER_NAME.equals(ss.getValue())) {
                throw new AnalysisExpressionException(ss.getPosition(),
                        "Illegal operator of:`" + ss.getType() + "`. Expected for " + GET_MEMBER_OPER_NAME);
            }
        }
        context.addImport(new MetaImport(
                importPackage.toArray(new String[]{}),
                sentence.getFirst().getPosition()
        ));
    }
}
