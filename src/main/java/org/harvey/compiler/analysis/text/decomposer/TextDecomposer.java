package org.harvey.compiler.analysis.text.decomposer;


import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 19:12
 */
public interface TextDecomposer {
    /**
     * @param source 原有的文本
     * @return 将原有的文本再次分解, 成一个新Context, 删除原有节点,<br>
     *          context的元素代替原有节点的位置;<br>
     *          如果为null, 则不删除原有的
     */
    SourceTextContext decompose(SourceString source);
}
