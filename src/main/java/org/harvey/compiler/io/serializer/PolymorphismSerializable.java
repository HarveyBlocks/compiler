package org.harvey.compiler.io.serializer;

import java.io.OutputStream;

/**
 * 用于将多态的一类对象序列化, 方法是加前缀code作为id分辨类型
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-22 01:18
 */
public abstract class PolymorphismSerializable {
    public abstract int out(OutputStream os);
}
