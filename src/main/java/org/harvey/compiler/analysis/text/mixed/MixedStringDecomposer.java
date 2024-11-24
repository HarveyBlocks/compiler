package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.PropertyConstant;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.CharacterUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.CompilerException;

import java.util.*;

/**
 * Number必须是合在一起的才算一个Number, <code>1. 2</code> 不算一个Number, <code>12 f</code>不算一个Number<br>
 * Number和Word, Number和Number, Word和Word不可能相邻<br>
 * Number和点在一起解析, 解析成浮点数
 * Word和点分开解析, 看作调用运算符
 * Number+.+Word先整体看作一个Number, 看作调用运算符
 * Number+.+Number看作一个浮点数
 * Number+.+ 末尾自动加0
 * Number+字母检查是否是进制, 如果是进制, 就只能看作是整数
 * Number+(.+(Number))+E/e+Number => 科学计数法(十进制)
 * <p>
 * 对于., 如果被分成两组, .一定被视为运算符, 如果和数字在一起, 则视为一个数字
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-18 15:00
 */
public class MixedStringDecomposer {
    private static final Set<Character> OPERATOR_SIGN_SET;
    private static final char DOT = PropertyConstant.DOT;

    static {
        final Set<Character> operatorSet = new HashSet<>();
        Operators.NAME_SET.stream().map(String::toCharArray).forEach(cs -> {
            for (char c : cs) {
                operatorSet.add(c);
            }
        });
        operatorSet.remove(DOT); // .被移除
        OPERATOR_SIGN_SET = Collections.unmodifiableSet(operatorSet);
    }

    // 完成分解的文本
    private LinkedList<SourceString> phasedString = new LinkedList<>();
    private SourcePosition nowPosition;

    private StringBuilder sb = new StringBuilder();
    private final String source;
    private SourceStringType nowType;

    public MixedStringDecomposer(SourceString mixedSource) {
        if (mixedSource.getType() != SourceStringType.MIXED) {
            throw new CompilerException("Only MIXED source string type is allowed" +
                    " in mix string decomposer", new IllegalArgumentException());
        }
        nowType = SourceStringType.MIXED;
        this.nowPosition = (SourcePosition) mixedSource.getPosition().clone();
        this.source = mixedSource.getValue();
    }

    public SourceTextContext phase() {
        decomposeSimply();
        SourceTextContext.legalTypeAssert(phasedString, Set.of(
                SourceStringType.STRING, SourceStringType.SIGN,
                SourceStringType.MIXED, SourceStringType.ITEM, SourceStringType.OPERATOR
        ));
        analysisDot();
        concatItem();
        SourceTextContext.legalTypeAssert(phasedString, Set.of(
                SourceStringType.STRING, SourceStringType.SIGN,
                SourceStringType.ITEM, SourceStringType.OPERATOR
        ));
        return new SourceTextContext(phasedString);
    }


    private void decomposeSimply() {
        // 分出操作数Item和operator和dot
        // dot进一步分析分析出是小数点还是运算符
        nowType = SourceStringType.MIXED;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (isDot(c)) {
                add();
                sb.append(DOT);
                nowType = SourceStringType.MIXED;
                add();
                continue;
            }
            if (nowType == SourceStringType.MIXED) {
                nowType = isOperatorSign(c) ? SourceStringType.OPERATOR : SourceStringType.ITEM;
                sb.append(c);
                continue;
            }
            boolean operatorSign = isOperatorSign(c);
            if (nowType == SourceStringType.OPERATOR && !operatorSign) {
                add();
                this.nowType = SourceStringType.ITEM;
            } else if (nowType == SourceStringType.ITEM && operatorSign) {
                add();
                this.nowType = SourceStringType.OPERATOR;
            }
            sb.append(c);
        }
        add();
    }

    /**
     * 分析上下文,dot是小数点还是一个
     */
    private void analysisDot() {
        LinkedList<SourceString> newPhasedString = new LinkedList<>();
        for (ListIterator<SourceString> lit = phasedString.listIterator(); lit.hasNext(); ) {
            final SourceString item = lit.next();
            if (item.getType() != SourceStringType.MIXED) {
                newPhasedString.add(item);
                continue;
            }
            if (!String.valueOf(DOT).equals(item.getValue())) {
                throw new CompilerException("Mixed oly DOT", new IllegalArgumentException());
            }
            SourceString post = getPost(lit);
            SourceString pre = getPre(lit);
            analysisDot(post, pre, item, newPhasedString, lit);
        }
        this.phasedString = newPhasedString;
    }

    private static void analysisDot(
            SourceString post, SourceString pre, SourceString item,
            LinkedList<SourceString> newPhasedString, ListIterator<SourceString> lit) {
        if (post == null && pre == null) {
            item.setType(SourceStringType.OPERATOR);
            newPhasedString.add(item);
            return;
        }
        if (post == null) {
            if (fullNumber(pre.getValue())) {
                newPhasedString.removeLast();
                newPhasedString.add(new SourceString(SourceStringType.ITEM, pre.getValue() + DOT, pre.getPosition()));
            } else {
                item.setType(SourceStringType.OPERATOR);
                newPhasedString.add(item);
            }
            return;
        }
        if (pre != null && !fullNumber(pre.getValue())) {
            item.setType(SourceStringType.OPERATOR);
            newPhasedString.add(item);
            return;
        }
        analysisScientificSign(post, item, newPhasedString, lit);
    }

    private static void analysisScientificSign(
            SourceString post, SourceString item,
            LinkedList<SourceString> newPhasedString, ListIterator<SourceString> lit) {
        String postValue = post.getValue();
        int lowerScience = postValue.indexOf(PropertyConstant.SCIENTIFIC_NOTATION_LOWER_SIGN);
        int upperScience = postValue.indexOf(PropertyConstant.SCIENTIFIC_NOTATION_UPPER_SIGN);
        if (upperScience != -1 && lowerScience != -1) {
            item.setType(SourceStringType.OPERATOR);
        } else if (lowerScience == -1 && upperScience == -1) {
            if (fullNumber(postValue)) {
                newPhasedString.add(new SourceString(SourceStringType.ITEM, DOT + post.getValue(), item.getPosition()));
                lit.next();
            } else {
                item.setType(SourceStringType.OPERATOR);
                newPhasedString.add(item);
            }
        } else if (lowerScience != -1) {
            if (checkScienceNotation(postValue, PropertyConstant.SCIENTIFIC_NOTATION_LOWER_SIGN, lowerScience, item)) {
                newPhasedString.add(new SourceString(SourceStringType.ITEM, DOT + post.getValue(), item.getPosition()));
                lit.next();
            } else {
                item.setType(SourceStringType.OPERATOR);
                newPhasedString.add(item);
            }
        } else {
            if (checkScienceNotation(postValue, PropertyConstant.SCIENTIFIC_NOTATION_UPPER_SIGN, upperScience, item)) {
                newPhasedString.add(new SourceString(SourceStringType.ITEM, DOT + post.getValue(), item.getPosition()));
                lit.next();
            } else {
                item.setType(SourceStringType.OPERATOR);
                newPhasedString.add(item);
            }
        }
    }

    private static boolean checkScienceNotation(String postValue, char scienceSign, int scienceIndex, SourceString item) {
        if (postValue.indexOf(scienceSign, scienceIndex + 1) != -1) {
            item.setType(SourceStringType.OPERATOR);
        }
        // 唯一的e
        return fullNumber(postValue, 0, scienceIndex) && fullNumber(postValue, scienceIndex + 1, postValue.length());
    }

    private static SourceString getPost(ListIterator<SourceString> lit) {
        SourceString post;
        if (lit.hasNext()) {
            post = lit.next();
            lit.previous();
        } else {
            post = null;
        }

        return post;
    }

    private static SourceString getPre(ListIterator<SourceString> lit) {
        lit.previous();
        final SourceString pre;
        if (lit.hasPrevious()) {
            pre = lit.previous();
            lit.next();
        } else {
            pre = null;
        }
        lit.next();
        return pre;
    }

    private static boolean fullNumber(String s) {
        return fullNumber(s, 0, s.length() - (StringUtil.endWithNumberSuffix(s) ? 1 : 0));
    }


    private static boolean fullNumber(String s, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!CharacterUtil.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将几个Item合成成一个, (主要是DOT)<br>
     * 分析Operator是否是没注册的Operator<br>
     * 由于传入的数据是已经被空格隔开的了, 所以是可以合成的
     */
    private void concatItem() {
        LinkedList<SourceString> newPhasedString = new LinkedList<>();
        for (SourceString string : phasedString) {
            SourceStringType lastType = newPhasedString.isEmpty() ? null : newPhasedString.getLast().getType();
            String value = string.getValue();
            if (lastType == SourceStringType.ITEM && string.getType() == SourceStringType.ITEM) {
                SourceString last = newPhasedString.removeLast();
                newPhasedString.add(new SourceString(lastType, last.getValue() + value, last.getPosition()));
            } else {
                newPhasedString.add(string);
            }
        }
        this.phasedString = newPhasedString;
    }

    private static boolean isDot(char c) {
        return c == DOT;
    }


    private static boolean isOperatorSign(char c) {
        return OPERATOR_SIGN_SET.contains(c);
    }

    private void add() {
        String part = sb.toString();
        if (!part.isEmpty()) {
            phasedString.add(new SourceString(nowType, part, nowPosition));
        }
        nowPosition = SourcePosition.moveToEnd(nowPosition, part);
        sb = new StringBuilder();
    }


}
