package org.harvey.compiler.common.reflect;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.collecction.PairList;

import java.util.function.Consumer;

/**
 * 进行类型转换的工具, 一个转换无法成功用下一个转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 14:54
 */
public class VieCaster {
    public static void cast(Object obj, PairList<Class<?>, Consumer<Object>> list) {
        if (obj == null) {
            return;
        }
        Class<?> objClass = obj.getClass();
        for (Pair<Class<?>, Consumer<Object>> entry : list) {
            if (entry.getKey().isAssignableFrom(objClass)) {
                entry.getValue().accept(obj);
                break;
            }
        }
    }

}
