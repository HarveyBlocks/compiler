package org.harvey.compiler.io.serializer;


import java.io.IOException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 02:27
 */
public interface Serializer<T> {
    void serialize(T origin) throws IOException;

    // 好痛苦, 从IS中解析出一个T origin, 是和对象无关的, 是静态的
    // 静态的无法被抽象
    // 将os作为字段的对象进行抽象
    // 但是将is和os放在一个类, 这好吗? 这不好
    T deserialize() throws IOException;


}
