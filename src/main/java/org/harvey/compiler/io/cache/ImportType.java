package org.harvey.compiler.io.cache;

/**
 * 在整个编译的结构中是什么声明
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 12:15
 */
public enum ImportType {
    PACKAGE, FILE, STRUCTURE, FIELD, CALLABLE;
    // 其中, VARIABLE和CALLABLE是终结节点
    // 其余的可以是终结的, 也可以不是
    // 导入: import aaa.bbb
    // 使用: bbb.get();
    // 解析: 依据路径从相关文件中获取bbb的信息
    //       调用bbb

    public boolean mustBeLast() {
        return this == FIELD || this == CALLABLE;
    }

    public boolean isFile() {
        return this == FILE || this == PACKAGE;
    }
}

