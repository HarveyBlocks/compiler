package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.test.version1.env.CallableArgumentOuter;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 一个Argument, 一个表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 22:11
 */
public class ArgumentWarp extends DefaultComplexExpressionWrap {
    private final int index;
    private final CallableArgumentOuter callableArgumentOuter;

    public ArgumentWarp(SourcePosition position, int index, CallableArgumentOuter callableArgumentOuter) {
        super(position);
        this.index = index;
        this.callableArgumentOuter = callableArgumentOuter;
    }

    @Override
    protected void afterSet() {
        // 再解析完毕后进一步排除
        // 要么, 就是一个参数解析完排除一个, 要么就是全部解析完全部排除
        // 如果全部解析完毕, 再要获取下一个的话, 那么就无法依据param来判断argument的类型
        // 如果一个一个解析, 然后匹配, 那么就很难实现比较复杂和高级的函数匹配
        //
        // TODO
        // 成功匹配一个了callableArgumentOuter的paramIndex往下走一个
    }
}
