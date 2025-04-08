package org.harvey.compiler.common.reflect;

import org.harvey.compiler.exception.self.CompilerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 简单封装Java反射, 用于构造对象
 * LaVie
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-26 15:11
 */
public class VieConstructor<T> {
    private final Constructor<T> constructor;

    public VieConstructor(Class<T> target, Class<?>... argTypes) {
        try {
            this.constructor = target.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            throw new CompilerException(e.getMessage());
        }
    }

    public T instance(Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new CompilerException(e.getMessage());
        }
    }
}
