package org.harvey.compiler.declare.context;

import org.harvey.compiler.io.source.SourceString;

import java.util.List;

/**
 * 用于将所有的Executable放在一个pool里, 其他地方换成引用, 方便编译时, 不会读入所有信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 13:41
 */
public class SourceStringContextPoolFactory {
    public static final int REFERENCE_FOR_NULL = -1;
    private final List<List<SourceString>> pool;

    public SourceStringContextPoolFactory(List<? extends List<SourceString>> pool) {
        this.pool = (List<List<SourceString>>) pool;
    }

    public int add(List<SourceString> context) {
        if (context == null || context.isEmpty()) {
            return REFERENCE_FOR_NULL;
        }
        int reference = pool.size();
        pool.add(context);
        return reference;
    }

    public List<SourceString> get(int index) {
        return pool.get(index);
    }
}
