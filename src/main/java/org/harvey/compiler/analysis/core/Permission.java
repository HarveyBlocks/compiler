package org.harvey.compiler.analysis.core;

import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-24 22:18
 */
@Getter
public enum Permission {
    // 访问控制权限
    PUBLIC(Keyword.PUBLIC),
    PROTECTED(Keyword.PROTECTED),
    PRIVATE(Keyword.PRIVATE),
    INTERNAL(Keyword.INTERNAL),
    FILE(Keyword.FILE),
    PACKAGE(Keyword.PACKAGE);
    private final Keyword keyword;

    Permission(Keyword keyword) {
        this.keyword = keyword;
    }

    // 全局 1Bit
    // 当前包 1Bit
    // 子包  1Bit
    // 子类 1Bit
    // 文件 1Bit
    // 当前类 1Bit
    // 内部类 1Bit
    public static Permission get(Keyword keyword) {
        for (Permission value : Permission.values()) {
            if (value.keyword == keyword) {
                return value;
            }
        }
        return null;
    }

    public static Permission get(String keyword) {
        return get(Keyword.get(keyword));
    }

    public static Permission getInternalAble(Keyword keyword) {
        Permission value = get(keyword);
        if (value == PROTECTED || value == PACKAGE) {
            return value;
        }
        return null;
    }

    public static Permission getInternalAble(String keyword) {
        return getInternalAble(Keyword.get(keyword));
    }

    public static boolean is(String keyword) {
        return get(keyword) != null;
    }

    public static boolean internalAble(String keyword) {
        return getInternalAble(keyword) != null;
    }

    public static boolean is(Keyword keyword) {
        return get(keyword) != null;
    }
}


