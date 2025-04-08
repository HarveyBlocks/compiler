package org.harvey.compiler.text;

import org.harvey.compiler.common.collecction.StringIterator;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * 分解源码
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-26 00:20
 */
public abstract class SourceFileManager {
    public static final String FAKE_STRING_CIRCLE_SIGN =
            "" + SourceFileConstant.ESCAPE_CHARACTER_IDENTIFIERS + SourceFileConstant.STRING_ENCIRCLE_SIGN;

    static {
        assert (SourceFileConstant.MULTI_LINE_COMMENTS_PRE.length() == 2);
        assert (SourceFileConstant.SINGLE_LINE_COMMENTS_START.length() == 2);
    }

    // 完成分解的文本
    protected final LinkedList<SourceString> phasedText = new LinkedList<>();
    protected final SourcePosition nowPosition = new SourcePosition(0, 0);
    protected boolean lastComplete = true;

    protected static int findTrueStringEnd(String apartText, int start) {
        while (true) {
            if (start > apartText.length()) {
                return -1;
            }
            int stringEndIndex = apartText.indexOf(SourceFileConstant.STRING_ENCIRCLE_SIGN, start);
            int fakeStringIndex = apartText.indexOf(FAKE_STRING_CIRCLE_SIGN, start);
            if (stringEndIndex == -1) {
                return -1;
            }
            // 找到了引号
            if (fakeStringIndex == -1) {
                // 从这里获取到最终的行列
                return stringEndIndex;
            }

            start = stringEndIndex + 1;
        }
    }

    /**
     * 字符类型中的空格是允许存在的(也只允许这个了)<br>
     * 有一个bug: '空格空格', 会将两个'分开, 看作是一个MIXED的最后和下一个MIXED的最前
     *
     * @return 如果是字符中的空格, 就将sit移动到字符的符号`'`之后, 否则保持不变<br>
     * 如果返回null, 表示半道崩殂了
     */
    protected StringIterator tryMoveToCharEnd(
            char c, StringIterator sit, StringBuilder sb,
            SourcePosition oldPosition) {
        if (!SourceFileConstant.READABLE_WHITESPACE.contains(c)) {
            // 不是
            return sit;
        }
        if (!sit.hasPrevious()) {
            // 不是
            throw new CompilerException("Non-existent? " +
                                        "So how did you get this c? " +
                                        "A situation that logically cannot exist", new IllegalStateException());
        }
        sit.previous(); // 移回空格
        if (!sit.hasPrevious()) {
            // 不是
            sit.next();
            return sit;
        }
        char pre = sit.previousWithoutMove();
        sit.next(); // 此时返回char c , 也就是' '
        if (pre != SourceFileConstant.CHARACTER_ENCIRCLE_SIGN) {
            // 不是
            return sit;
        }
        if (!sit.hasNext()) {
            // 半道崩殂
            sb.append(c);
            return null;
        }
        char next = sit.nextWithoutMove(); // 此时返回char c (' ')的下一个
        if (next != SourceFileConstant.CHARACTER_ENCIRCLE_SIGN) {
            // 不是
            return sit;
        }
        sit.next(); // 真正进行移动
        oldPosition.columnIncreasing();
        // 移到后面了
        sb.append(c).append(SourceFileConstant.CHARACTER_ENCIRCLE_SIGN);
        return new StringIterator(sit.stringAfter());
    }

    protected StringIterator skipMultipleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                SourceFileConstant.MULTI_LINE_COMMENTS_PRE,
                SourceFileConstant.MULTI_LINE_COMMENTS_POST,
                SourceType.MULTI_LINE_COMMENTS, false,
                (s, start) -> s.indexOf(SourceFileConstant.MULTI_LINE_COMMENTS_POST, start)
        );
    }

    protected StringIterator skipString(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                String.valueOf(SourceFileConstant.STRING_ENCIRCLE_SIGN),
                String.valueOf(SourceFileConstant.STRING_ENCIRCLE_SIGN),
                SourceType.STRING, true, SourceFileManager::findTrueStringEnd
        );
    }

    protected StringIterator skipTextWithPairedSign(
            int index, StringIterator sit, SourcePosition oldPosition,
            String pre, String post, SourceType type, boolean add2List,
            BiFunction<String, Integer, Integer> endIndexFinder) {
        int preLen = pre.length();
        int commitPreEnd = index + preLen;
        int commitEnd = endIndexFinder.apply(
                sit.toString(),
                commitPreEnd
        );// sit.toString().add(post, commitPreEnd);
        if (commitEnd == -1) {
            // 不能在这次做完
            add(type, sit.toString().substring(index), oldPosition.clone(0, -preLen), true);
            return null;
        }
        int postLen = post.length();
        String value = sit.toString().substring(index, commitEnd + postLen);
        if (add2List) {
            add(type, value, oldPosition.clone(0, -postLen), true);
        }
        SourcePosition move = SourcePosition.moveToEnd(oldPosition.clone(0, -postLen), value);
        oldPosition.setRaw(move.getRaw());
        oldPosition.setColumn(move.getColumn());
        return new StringIterator(sit.toString().substring(commitEnd + postLen));
    }

    protected StringIterator skipSingleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        int commitPreEnd = index + SourceFileConstant.SINGLE_LINE_COMMENTS_START.length();
        int commitEnd = sit.toString().indexOf(SourceFileConstant.LINE_SEPARATOR, commitPreEnd);
        if (commitEnd != -1) {
            sit = new StringIterator(sit.toString().substring(
                    commitEnd + SourceFileConstant.LINE_SEPARATOR.length()
            ));
            if (sit.hasNext()) {
                throw new CompilerException(
                        "Logically, there should not be a situation " +
                        "where a single line comment has been parsed but has not yet been wrapped",
                        new IllegalStateException()
                );
            }
            return sit;
        }
        // 只存一个开始
        add(
                SourceType.SINGLE_LINE_COMMENTS,
                SourceFileConstant.SINGLE_LINE_COMMENTS_START,
                oldPosition, false
        );
        return null;
    }

    /**
     * 返回老的位置
     */
    protected SourcePosition updatePosition(boolean fullLine, String apartText) {
        SourcePosition old = (SourcePosition) nowPosition.clone();
        if (fullLine) {
            nowPosition.rawIncreasing();
        } else {
            nowPosition.columnAdding(apartText.length());
        }
        return old;
    }


    protected StringBuilder add(SourceType nowStatus, String value, SourcePosition sp, boolean start) {
        if (!value.isEmpty()) {
            phasedText.addLast(new SourceString(nowStatus, value, sp.clone(0, start ? 0 : -value.length())));
        }
        lastComplete = true;
        return new StringBuilder();
    }

    protected boolean skipCommitIfExist(char c, DecomposedProcessAmount amount, int index) {
        StringBuilder startSign = new StringBuilder();
        startSign.append(c);
        if (!amount.sit.hasNext()) {
            // 不含下一个, 直接跳出
            amount.sb.append(c);
            return true;
        }
        // 怀疑有些符号是两位的, 例如多行注释是两个字符组成的
        // 能处理三个字符组成的标识吗? 四位的呢? 不能qwq
        char cn = amount.sit.next();
        amount.oldPosition.columnIncreasing();
        startSign.append(cn);
        String cnString = startSign.toString();
        if (cnString.equals(SourceFileConstant.SINGLE_LINE_COMMENTS_START)) {
            int preLen = SourceFileConstant.SINGLE_LINE_COMMENTS_START.length();
            amount.sb = add(
                    SourceType.MIXED, amount.sb.toString(),
                    amount.oldPosition.clone(0, -preLen), false
            );
            return (amount.sit = skipSingleCommit(index, amount.sit, amount.oldPosition)) == null;
        } else if (cnString.equals(SourceFileConstant.MULTI_LINE_COMMENTS_PRE)) {
            amount.sb = add(
                    SourceType.MIXED, amount.sb.toString(),
                    amount.oldPosition.clone(0, -SourceFileConstant.MULTI_LINE_COMMENTS_PRE.length()), false
            );
            return (amount.sit = skipMultipleCommit(index, amount.sit, amount.oldPosition)) == null;
        } else {
            amount.sb.append(c);
            amount.sit.previous();
            amount.oldPosition.columnDecreasing();
        }
        return false;
    }

    protected void checkCompleteSourceString(DecomposedProcessAmount amount) {
        if (amount.sit == null) {
            lastComplete = false;
            // 中道崩殂
            if (!amount.sb.toString().isEmpty()) {
                throw new CompilerException("Logically, there should not be a situation " +
                                            "where the StringBuilder is not cleared " +
                                            "before parsing the comment string", new IllegalStateException());
            }
        } else {
            // 寿终正寝
            String value = amount.sb.toString();
            add(SourceType.MIXED, value, nowPosition, false);
            lastComplete = value.isEmpty();
        }
    }

    public SourceTextContext get() {
        if (!lastComplete) {
            SourceString last = phasedText.getLast();
            if (last.getType() == SourceType.SINGLE_LINE_COMMENTS) {
                phasedText.removeLast();
            } else {
                throw new AnalysisException(last.getPosition(), "Need the post part of sign");
            }
        }
        return new SourceTextContext(phasedText);
    }

    public void clear() {
        this.nowPosition.setRaw(0);
        this.nowPosition.setColumn(0);
        phasedText.clear();
    }

    public abstract void appendDecomposed(boolean fullLine, String apartText);

    protected DecomposedProcessAmount beforeDecompose(boolean fullLine, String apartText) {
        if (apartText == null || apartText.isEmpty()) {
            return null;
        }
        if (fullLine && !apartText.endsWith(SourceFileConstant.LINE_SEPARATOR)) {
            throw new CompilerException("The decomposed string is incorrect, " +
                                        "if it is full of one line, " +
                                        "there should be a newline character", new IllegalStateException());
        }
        // oldPosition总是指向下一个字符的位置
        SourcePosition oldPosition = updatePosition(fullLine, apartText);
        final StringBuilder initSb = new StringBuilder();
        if (!phasedText.isEmpty() && !lastComplete) {
            lastComplete = true;
            // 补上上次没完成的
            SourceString last = phasedText.removeLast();
            initSb.append(last.getValue());
            oldPosition = (SourcePosition) last.getPosition().clone();
        }
        return new DecomposedProcessAmount(
                new StringIterator(initSb.append(apartText).toString()),
                new StringBuilder(), oldPosition
        );
    }

    public abstract boolean completePhaseFile();

    protected static class DecomposedProcessAmount {
        public final SourcePosition oldPosition;
        public StringIterator sit;
        public StringBuilder sb;

        public DecomposedProcessAmount(StringIterator sit, StringBuilder sb, SourcePosition oldPosition) {
            this.sit = sit;
            this.sb = sb;
            this.oldPosition = oldPosition;
        }
    }


}
