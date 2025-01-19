package org.harvey.compiler.io.serializer.collection;

import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.io.serializer.structure.ConstTableElementSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <li><p><span>第三阶段完成: 遍历常量池, 以此遍历它们的声明, 检查它们的声明正确与否</span></p>
 * <p><span>包括文件级别的函数变量和复合类型, 也包括类内嵌套的各种数据信息</span></p>
 *     <ul>
 *         <li><span>共计 (24Bit+)</span></li>
 *         <li><span>指向声明结构(12bit)</span></li>
 *         <li><span>字符串长度(12bit)</span></li>
 *         <li><span>名字字符串(字节数组)</span></li>
 *     </ul>
 * </li>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 02:08
 */
public class ConstantTableSerializer extends MapSerializer<String, Integer> {
    private static final int SIZE_BC = CompileFileConstants.CONSTANT_TABLE_SIZE_BC;

    public ConstantTableSerializer(InputStream is, OutputStream os) {
        super(is, os, new ConstTableElementSerializer(is, os), SIZE_BC, "constant table size");
    }
}
