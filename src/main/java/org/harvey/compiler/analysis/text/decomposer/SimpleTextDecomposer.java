package org.harvey.compiler.analysis.text.decomposer;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.common.SystemConstant;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.entity.StringIterator;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;

import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * 文本分解器, 去除注释, 构建元素是空格分割的链表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:21
 */
public class SimpleTextDecomposer {

    public static final String FAKE_STRING_CIRCLE_SIGN = "" + PropertyConstant.ESCAPE_CHARACTER_IDENTIFIERS + PropertyConstant.STRING_ENCIRCLE_SIGN;

    // 完成分解的文本
    private final LinkedList<SourceString> phasedText = new LinkedList<>();
    private boolean lastComplete = true;
    private final SourcePosition nowPosition = new SourcePosition(0, 0);


    static {
        assert (PropertyConstant.MULTI_LINE_COMMENTS_PRE.length() == 2);
        assert (PropertyConstant.SINGLE_LINE_COMMENTS_START.length() == 2);
    }

    /**
     * @param apartText 一部分源码文本
     */
    public void appendDecomposed(boolean fullLine, String apartText) {
        if (apartText == null || apartText.isEmpty()) {
            return;
        }
        if (fullLine && !apartText.endsWith(SystemConstant.LINE_SEPARATOR)) {
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
        StringIterator sit = new StringIterator(initSb.append(apartText).toString());
        StringBuilder sb = new StringBuilder();
        while (sit.hasNext()) {
            int index = sit.nextIndex();
            char c = sit.next();
            oldPosition.columnIncreasing();
            if (PropertyConstant.SPRITE_SIGN.contains(c)) {
                // 一定要分割的符号, 就分割
                SourcePosition endPosition = oldPosition.clone(0, -1);
                add(SourceStringType.MIXED, sb.toString(), endPosition, false);
                sb = add(SourceStringType.SIGN, String.valueOf(c), endPosition, true);
                continue;
            }
            if (c == PropertyConstant.STRING_ENCIRCLE_SIGN) {
                // 是字符串的开头
                // 处理字符串
                SourcePosition beforeString = oldPosition.clone(0, -1);
                sb = add(SourceStringType.MIXED, sb.toString(), beforeString, false);
                if ((sit = skipString(index, sit, oldPosition)) == null) {
                    break;
                } else {
                    continue;
                }
            }

            if (Character.isWhitespace(c)) {
                // 空白符, 就分割
                StringIterator temp = tryMoveToCharEnd(c, sit, sb, oldPosition);

                if (temp == sit) {
                    sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -1), false);
                    continue;
                }
                sit = temp;
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition, false);
                if (sit == null) {
                    break;
                }
                continue;
            }
            StringBuilder startSign = new StringBuilder();
            startSign.append(c);
            if (!sit.hasNext()) {
                // 不含下一个, 直接跳出
                sb.append(c);
                break;
            }
            // 怀疑有些符号是两位的, 例如多行注释是两个字符组成的
            // 能处理三个字符组成的标识吗? 四位的呢? 不能qwq
            char cn = sit.next();
            oldPosition.columnIncreasing();
            startSign.append(cn);
            String cnString = startSign.toString();
            if (cnString.equals(PropertyConstant.SINGLE_LINE_COMMENTS_START)) {
                int preLen = PropertyConstant.SINGLE_LINE_COMMENTS_START.length();
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -preLen), false);
                if ((sit = skipSingleCommit(index, sit, oldPosition)) == null) {
                    break;
                }
            } else if (cnString.equals(PropertyConstant.MULTI_LINE_COMMENTS_PRE)) {
                sb = add(SourceStringType.MIXED, sb.toString(), oldPosition.clone(0, -PropertyConstant.MULTI_LINE_COMMENTS_PRE.length()), false);
                if ((sit = skipMultipleCommit(index, sit, oldPosition)) == null) {
                    break;
                }
            } else {
                sb.append(c);
                sit.previous();
                oldPosition.columnDecreasing();
            }
        }

        if (sit == null) {
            lastComplete = false;
            // 中道崩殂
            if (!sb.toString().isEmpty()) {
                throw new CompilerException("Logically, there should not be a situation " +
                        "where the StringBuilder is not cleared " +
                        "before parsing the comment string", new IllegalStateException());
            }
        } else {
            // 寿终正寝
            String value = sb.toString();
            add(SourceStringType.MIXED, value, nowPosition, false);
            lastComplete = value.isEmpty();
        }
    }

    /**
     * 字符类型中的空格是允许存在的(也只允许这个了)<br>
     * 有一个bug: '空格空格', 会将两个'分开, 看作是一个MIXED的最后和下一个MIXED的最前
     *
     * @return 如果是字符中的空格, 就将sit移动到字符的符号`'`之后, 否则保持不变<br>
     * 如果返回null, 表示半道崩殂了
     */
    private StringIterator tryMoveToCharEnd(char c, StringIterator sit, StringBuilder sb, SourcePosition oldPosition) {
        if (!PropertyConstant.READABLE_WHITESPACE.contains(c)) {
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
        if (pre != PropertyConstant.CHARACTER_ENCIRCLE_SIGN) {
            // 不是
            return sit;
        }
        if (!sit.hasNext()) {
            // 半道崩殂
            sb.append(c);
            return null;
        }
        char next = sit.nextWithoutMove(); // 此时返回char c (' ')的下一个
        if (next != PropertyConstant.CHARACTER_ENCIRCLE_SIGN) {
            // 不是
            return sit;
        }
        sit.next(); // 真正进行移动
        oldPosition.columnIncreasing();
        // 移到后面了
        sb.append(c).append(PropertyConstant.CHARACTER_ENCIRCLE_SIGN);
        return new StringIterator(sit.stringAfter());
    }

    private StringIterator skipMultipleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                PropertyConstant.MULTI_LINE_COMMENTS_PRE,
                PropertyConstant.MULTI_LINE_COMMENTS_POST,
                SourceStringType.MULTI_LINE_COMMENTS, false,
                (s, start) -> s.indexOf(PropertyConstant.MULTI_LINE_COMMENTS_POST, start));
    }

    private StringIterator skipString(int index, StringIterator sit, SourcePosition oldPosition) {
        return skipTextWithPairedSign(index, sit, oldPosition,
                String.valueOf(PropertyConstant.STRING_ENCIRCLE_SIGN),
                String.valueOf(PropertyConstant.STRING_ENCIRCLE_SIGN),
                SourceStringType.STRING, true, SimpleTextDecomposer::findTrueStringEnd);
    }

    private static int findTrueStringEnd(String apartText, int start) {
        while (true) {
            if (start > apartText.length()) {
                return -1;
            }
            int stringEndIndex = apartText.indexOf(PropertyConstant.STRING_ENCIRCLE_SIGN, start);
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

    private StringIterator skipTextWithPairedSign(
            int index, StringIterator sit, SourcePosition oldPosition,
            String pre, String post, SourceStringType type, boolean add2List,
            BiFunction<String, Integer, Integer> endIndexFinder) {
        int preLen = pre.length();
        int commitPreEnd = index + preLen;
        int commitEnd = endIndexFinder.apply(sit.toString(), commitPreEnd);// sit.toString().indexOf(post, commitPreEnd);
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

    private StringIterator skipSingleCommit(int index, StringIterator sit, SourcePosition oldPosition) {
        int commitPreEnd = index + PropertyConstant.SINGLE_LINE_COMMENTS_START.length();
        int commitEnd = sit.toString().indexOf(SystemConstant.LINE_SEPARATOR, commitPreEnd);
        if (commitEnd != -1) {
            sit = new StringIterator(sit.toString().substring(
                    commitEnd + SystemConstant.LINE_SEPARATOR.length()
            ));
            if (sit.hasNext()) {
                throw new CompilerException("Logically, there should not be a situation " +
                        "where a single line comment has been parsed but has not yet been wrapped", new IllegalStateException());
            }
            return sit;
        }
        // 只存一个开始
        add(SourceStringType.SINGLE_LINE_COMMENTS,
                PropertyConstant.SINGLE_LINE_COMMENTS_START,
                oldPosition, false
        );
        return null;
    }

    /**
     * 返回老的位置
     */
    private SourcePosition updatePosition(boolean fullLine, String apartText) {
        SourcePosition old = (SourcePosition) nowPosition.clone();
        if (fullLine) {
            nowPosition.rawIncreasing();
        } else {
            nowPosition.columnAdding(apartText.length());
        }
        return old;
    }


    private StringBuilder add(SourceStringType nowStatus, String value, SourcePosition sp, boolean start) {
        if (!value.isEmpty()) {
            phasedText.addLast(new SourceString(nowStatus, value, sp.clone(0, start ? 0 : -value.length())));
        }
        lastComplete = true;
        return new StringBuilder();
    }


    public SourceTextContext get() {
        if (!lastComplete) {
            SourceString last = phasedText.getLast();
            if (last.getType() == SourceStringType.SINGLE_LINE_COMMENTS) {
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
}
