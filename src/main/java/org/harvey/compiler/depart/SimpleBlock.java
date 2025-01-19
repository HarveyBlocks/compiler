package org.harvey.compiler.depart;

import lombok.Getter;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * Block里就不应该定义函数嘛, 函数里能定义函数, 代码块里就不应该定义函数
 * 如果代码块里能, 那if里, while里都能定义函数了, 这合理吗?
 * 而且还得解析函数的代码块里定义的函数了, 又得层层递归解析了, 这样好吗? 这样不好
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-05 17:20
 */

@Getter
public class SimpleBlock {
    private final boolean isStatic;
    private final SourceTextContext body;
    // do{}
    // do ;
    // while();
    // while(){}
    // while() ;
    // if(){}
    // if() ;
    // if(){}
    // else{}
    // else if(){}
    // else if() ;
    // for(;;;){}
    // ->
    // for(;
    // ;
    // ;
    // ){}
    // for(;;;);
    //

    public SimpleBlock(boolean isStatic, SourceTextContext body) {
        this.isStatic = isStatic;
        this.body = body;
    }
}
