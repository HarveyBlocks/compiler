package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;

import java.util.List;

/**
 * 用于完成对Declare的枚举常量的引用转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-14 23:48
 */
@Getter
@AllArgsConstructor
public class EnumConstant {
    private final ReferenceElement identifier;
    /**
     * not nullable
     */
    private final List<? extends List<SourceString>> arguments;
}
