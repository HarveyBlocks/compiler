package org.harvey.compiler.analysis.text.decomposer;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-17 20:16
 */
public class StringDecomposer implements TextDecomposer {
    @Override
    public SourceTextContext decompose(SourceString source) {
        if (source.getType() != SourceStringType.STRING) {
            // 不用做操作
            return null;
        }
        // TODO
        /*StringBuilder sb = new StringBuilder();
        // value 解析转义字符
        // 去除前后的引号
        for (StringIterator sit = new StringIterator(text.getValue()); sit.hasNext(); ) {
            char c = sit.next();
            if (PropertyConstant.ESCAPE_CHARACTER_MAP.containsKey(c)) {
                c = PropertyConstant.ESCAPE_CHARACTER_MAP.get(c);
            } else if (c == 'u') {

            }else if (c=='x'){
                // 向后读三个数, 是
            }
            sb.append(c);
        }
        text.setValue(sb.toString());*/
        return null;
    }
}
