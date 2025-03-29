package org.harvey.compiler.execute.control;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ListIterator;

/**
 * 工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 00:02
 */
@Deprecated
public class ControlStructureBuilder {

    public static SourceTextContext removeBodyCircle(SourceTextContext textContext) {
        if (textContext.isEmpty()) {
            return textContext;
        }
        /*if (textContext.size() == 1) {
            SourceString first = textContext.getFirst();
            if (first.getType() != SourceType.SIGN) {
                return textContext;
            }
            String value = first.getValue();
            if (String.valueOf(SourceFileConstant.BODY_START).equals(value)
                    || String.valueOf(SourceFileConstant.BODY_END).equals(value)) {
                throw new AnalysisExpressionException(first.getPosition(), "Illegal structure sign match");
            }
        }

         TODO SourceString first = textContext.removeFirst();
        SourceString last = textContext.removeLast();
        boolean firstIs = first.getType() == SourceType.SIGN && String.valueOf(SourceFileConstant.BODY_START).equals(first.getValue());
        boolean lastIs = last.getType() == SourceType.SIGN && String.valueOf(SourceFileConstant.BODY_END).equals(last.getValue());
        if (firstIs && lastIs) {
            return textContext;
        }
        if (!firstIs && !lastIs) {
            textContext.addFirst(first);
            textContext.addLast(last);
            return textContext;
        }*/
        throw new AnalysisExpressionException(
                SourcePosition.UNKNOWN/*first.getPosition(), last.getPosition()*/,
                "Illegal structure sign match"
        );
    }

    /**
     * 如果不是, 什么都不做
     */
    private static boolean tryIf(ListIterator<SourceString> source, SourceTextContext target) {
        SourceString first = firstExist(source);
        if (first == null) {
            return false;
        }
        String value = first.getValue();
        Keyword firstKeyword = Keyword.get(value);
        if (firstKeyword == Keyword.ELSE) {
            throw new AnalysisExpressionException(first.getPosition(), "Meet else before if !");
        } else if (firstKeyword != Keyword.IF) {
            source.previous();
            return false;
        }
        // TODO 正式开始
        return true;
    }


    /**
     * 如果不是, 什么都不做
     */
    private static boolean trySwitch(ListIterator<SourceString> source, SourceTextContext target) {
        SourceString first = firstExist(source);
        if (first == null) {
            return false;
        }
        String value = first.getValue();
        Keyword firstKeyword = Keyword.get(value);
        if (firstKeyword == Keyword.CASE) {
            throw new AnalysisExpressionException(first.getPosition(), "Meet case before switch !");
        } else if (firstKeyword == Keyword.DEFAULT) {
            throw new AnalysisExpressionException(first.getPosition(), "Meet default before switch !");
        } else if (firstKeyword == Keyword.BREAK) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet break before while or do-while or switch or for!"
            );
        } else if (firstKeyword != Keyword.SWITCH) {
            source.previous();
            return false;
        }
        // TODO 正式开始
        return true;
    }

    /**
     * 如果不是, 什么都不做
     */
    private static boolean tryWhile(ListIterator<SourceString> source, SourceTextContext target) {
        SourceString first = firstExist(source);
        if (first == null) {
            return false;
        }
        String value = first.getValue();
        Keyword firstKeyword = Keyword.get(value);
        if (firstKeyword == Keyword.CONTINUE) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet continue before while or do-while or for!"
            );
        } else if (firstKeyword == Keyword.BREAK) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet break before while or do-while or switch or for!"
            );
        } else if (firstKeyword != Keyword.WHILE) {
            source.previous();
            return false;
        }
        // TODO 正式开始
        return true;
    }

    /**
     * 如果不是, 什么都不做
     */
    private static boolean tryDoWhile(ListIterator<SourceString> source, SourceTextContext target) {
        SourceString first = firstExist(source);
        if (first == null) {
            return false;
        }
        String value = first.getValue();
        Keyword firstKeyword = Keyword.get(value);
        if (firstKeyword == Keyword.CONTINUE) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet continue before while or do-while or for!"
            );
        } else if (firstKeyword == Keyword.BREAK) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet break before while or do-while or switch or for!"
            );
        } else if (firstKeyword != Keyword.DO) {
            source.previous();
            return false;
        }
        // TODO 正式开始
        return true;
    }

    /**
     * 如果不是, 什么都不做
     */
    private static boolean tryFor(ListIterator<SourceString> source, SourceTextContext target) {
        SourceString first = firstExist(source);
        if (first == null) {
            return false;
        }
        String value = first.getValue();
        Keyword firstKeyword = Keyword.get(value);
        if (firstKeyword == Keyword.CONTINUE) {
            throw new AnalysisExpressionException(first.getPosition(), "Meet continue before while !");
        } else if (firstKeyword == Keyword.BREAK) {
            throw new AnalysisExpressionException(
                    first.getPosition(),
                    "Meet break before while or do-while or switch or for!"
            );
        } else if (firstKeyword != Keyword.FOR) {
            source.previous();
            return false;
        }
        // TODO 正式开始
        return true;
    }

    private static SourceString firstExist(ListIterator<SourceString> source) {
        if (!source.hasNext()) {
            return null;
        }
        SourceString first = source.next();
        if (first.getType() != SourceType.KEYWORD) {
            source.previous();
            return null;
        }
        return first;
    }

}
