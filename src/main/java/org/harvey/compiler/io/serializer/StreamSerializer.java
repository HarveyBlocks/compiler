package org.harvey.compiler.io.serializer;


import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.local.LocalType;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link LocalVariableDeclare.Serializer}
 * {@link ExpressionElement.Serializer}
 * {@link LocalType.Serializer}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-19 16:59
 */
public interface StreamSerializer<T> {


    /**
     * @param is src
     * @return target
     */
    T in(InputStream is);

    /**
     * @param os target
     * @return byte array, write to os, size
     */
    int out(OutputStream os, T src);


}
