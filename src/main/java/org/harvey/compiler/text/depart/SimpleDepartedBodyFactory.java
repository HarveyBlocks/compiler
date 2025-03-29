package org.harvey.compiler.text.depart;

import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 文件级地(不解析成员), 简单区分成语句和含有`{}`body的结构
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 16:41
 */
public class SimpleDepartedBodyFactory {

    public static final String BODY_START = "" + SourceFileConstant.BODY_START;
    public static final String BODY_END = "" + SourceFileConstant.BODY_END;
    public static final String SENTENCE_END = "" + SourceFileConstant.SENTENCE_END;
    public static final String PARENTHESES_PRE = Operator.PARENTHESES_PRE.getName();
    public static final String PARENTHESES_POST = Operator.PARENTHESES_POST.getName();

    private SimpleDepartedBodyFactory() {
    }

    public static LinkedList<DepartedPart> depart(SourceTextContext context) {
        if (context == null) {
            return null;
        }
        if (context.isEmpty()) {
            return new LinkedList<>();
        }
        LinkedList<DepartedPart> dps = new LinkedList<>();
        ListIterator<SourceString> iterator = context.listIterator();
        while (iterator.hasNext()) {
            SourceTextContext statement = new SourceTextContext();
            boolean sentenceEnd = skipStatement(iterator, statement);
            SourceTextContext body;
            if (sentenceEnd) {
                body = SourceTextContext.empty();
            } else if (!iterator.hasNext()) {
                throw new AnalysisException(context.getLast().getPosition(), "expected ;");
            } else {
                // body
                body = SourceTextContext.skipNest(iterator, BODY_START, BODY_END, true);
            }
            dps.add(new DepartedPart(statement, body));
        }
        return dps;
    }


    /**
     * @return true if sentence end
     */
    private static boolean skipStatement(ListIterator<SourceString> iterator, SourceTextContext statement) {

        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            statement.add(next);
            String value = next.getValue();
            if (SENTENCE_END.equals(value)) {
                return true;
            } else if (BODY_START.equals(value)) {
                iterator.previous();
                statement.removeLast();
                return false;
            } else if (PARENTHESES_PRE.equals(value)) {
                iterator.previous();
                statement.removeLast();
                statement.addAll(SourceTextContext.skipNest(iterator, PARENTHESES_PRE, PARENTHESES_POST, true));
            }
        }
        throw new AnalysisExpressionException(iterator.previous().getPosition(), "expected ';'");
    }
}
