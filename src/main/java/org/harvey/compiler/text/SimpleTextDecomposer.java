package org.harvey.compiler.text;

import org.harvey.compiler.common.collecction.StringIterator;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceType;

/**
 * 文本分解器, 去除注释, 构建元素是空格分割的链表
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-15 21:21
 */
public class SimpleTextDecomposer extends SourceFileManager {
    /**
     * @param apartText 一部分源码文本
     */
    @Override
    public void appendDecomposed(boolean fullLine, String apartText) {
        DecomposedProcessAmount amount = beforeDecompose(fullLine, apartText);
        if (amount == null) {
            return;
        }
        while (amount.sit.hasNext()) {
            int index = amount.sit.nextIndex();
            char c = amount.sit.next();
            amount.oldPosition.columnIncreasing();
            if (SourceFileConstant.SPRITE_SIGN.contains(c)) {
                // 一定要分割的符号, 就分割
                SourcePosition endPosition = amount.oldPosition.clone(0, -1);
                add(SourceType.MIXED, amount.sb.toString(), endPosition, false);
                amount.sb = add(SourceType.SIGN, String.valueOf(c), endPosition, true);
                continue;
            }
            if (c == SourceFileConstant.STRING_ENCIRCLE_SIGN) {
                // 是字符串的开头
                // 处理字符串
                SourcePosition beforeString = amount.oldPosition.clone(0, -1);
                amount.sb = add(SourceType.MIXED, amount.sb.toString(), beforeString, false);
                if ((amount.sit = skipString(index, amount.sit, amount.oldPosition)) == null) {
                    break;
                } else {
                    continue;
                }
            }

            if (Character.isWhitespace(c)) {
                // 空白符, 就分割
                StringIterator temp = tryMoveToCharEnd(c, amount.sit, amount.sb, amount.oldPosition);
                if (temp == amount.sit) {
                    amount.sb = add(
                            SourceType.MIXED, amount.sb.toString(), amount.oldPosition.clone(0, -1),
                            false
                    );
                    continue;
                }
                amount.sit = temp;
                amount.sb = add(SourceType.MIXED, amount.sb.toString(), amount.oldPosition, false);
                if (amount.sit == null) {
                    break;
                }
                continue;
            }
            if (skipCommitIfExist(c, amount, index)) {
                break;
            }
        }

        checkCompleteSourceString(amount);
    }


    @Override
    public boolean completePhaseFile() {
        return false;
    }
}
