package org.harvey.compiler.depart;

import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.Declarable;

/**
 * TODO
 * 仿照Java, 也要有匿名内部类了, 那么, 为之奈何? 也是代码声明格式的一种, 不必多虑;
 * 有局部类的声明, 为之奈何? 作用域仅仅在函数之内, 不暴露到外部, 不必多虑
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 15:56
 */
@Getter
@Deprecated
public class SimpleCallable {
    private final Declarable declarable;
    private final SourceTextContext body;

    public SimpleCallable(Declarable declarable, SourceTextContext body) {
        this.declarable = declarable;
        this.body = body;
    }


}
