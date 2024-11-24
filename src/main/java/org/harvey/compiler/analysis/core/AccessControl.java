package org.harvey.compiler.analysis.core;

import lombok.Getter;
import org.harvey.compiler.exception.CompilerException;

/**
 * <h3 id='访问控制'><span>访问控制</span></h3>
 * <ul>
 *     <li><p><span>作为一个文件层的变量/函数/复合结构, 其一定是的file|public|package|inner package</span></p></li>
 *     <li><p><span>作为一个成员, 其作用域一定是任意作用域</span></p></li>
 *     <li><p><span>作为一个代码块里的局部成员, 一定没有作用域</span></p></li>
 *     <li><p><span>作为一个抽象方法, 其方法一定要被重写</span></p>
 *         <p>
 *             <span>所以其作用域一定是public或[protected|internal protected]+[ 无|internal package|package|file ]</span>
 *         </p></li>
 * </ul>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 21:30
 */
public class AccessControl {
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

        // 全局 1BIt
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

        public static boolean is(String keyword) {
            return get(keyword) != null;
        }

        public static boolean is(Keyword keyword) {
            return get(keyword) != null;
        }
    }

    private static final byte INNER_CLASS_BIT = 0x01;
    private static final byte SELF_CLASS_BIT = INNER_CLASS_BIT << 1;
    private static final byte FILE_BIT = INNER_CLASS_BIT << 2;
    private static final byte CHILDREN_CLASS_BIT = INNER_CLASS_BIT << 3;
    private static final byte CHILDREN_PACKAGE_BIT = INNER_CLASS_BIT << 4;
    private static final byte PACKAGE_BIT = INNER_CLASS_BIT << 5;
    private static final byte PUBLIC_BIT = INNER_CLASS_BIT << 6;
    private byte permission = 0;

    private void setPublic() {
        permission |= PUBLIC_BIT;
    }

    private void setPackage() {
        permission |= PACKAGE_BIT;
    }

    private void setChildrenPackage() {
        permission |= CHILDREN_PACKAGE_BIT;
    }

    private void setChildrenClass() {
        permission |= CHILDREN_CLASS_BIT;
    }

    private void setFile() {
        permission |= FILE_BIT;
    }

    private void setSelfClass() {
        permission |= SELF_CLASS_BIT;
    }

    private void setInternalClass() {
        permission |= INNER_CLASS_BIT;
    }

    private void unsetPublic() {
        permission &= ~PUBLIC_BIT;
    }

    private void unsetPackage() {
        permission &= ~PACKAGE_BIT;
    }

    private void unsetChildrenPackage() {
        permission &= ~CHILDREN_PACKAGE_BIT;
    }

    private void unsetChildrenClass() {
        permission &= ~CHILDREN_CLASS_BIT;
    }

    private void unsetFile() {
        permission &= ~FILE_BIT;
    }

    private void unsetSelfClass() {
        permission &= ~SELF_CLASS_BIT;
    }

    private void unsetInternalClass() {
        permission &= ~INNER_CLASS_BIT;
    }

    public boolean canPublic() {
        return (permission & PUBLIC_BIT) != 0;
    }

    public boolean canPackage() {
        return (permission & PACKAGE_BIT) != 0;
    }

    public boolean canChildrenPackage() {
        return (permission & CHILDREN_PACKAGE_BIT) != 0;
    }

    public boolean canChildrenClass() {
        return (permission & CHILDREN_CLASS_BIT) != 0;
    }

    public boolean canFile() {
        return (permission & FILE_BIT) != 0;
    }

    public boolean canSelfClass() {
        return (permission & SELF_CLASS_BIT) != 0;
    }

    public boolean canInternalClass() {
        return (permission & INNER_CLASS_BIT) != 0;
    }

    public AccessControl addPermission(Permission permission) {
        switch (permission) {
            case PUBLIC:
                configPublic();
                break;
            case PROTECTED:
                configProtected();
                break;
            case PRIVATE:
                configPrivate();
                break;
            case FILE:
                configFile();
                break;
            case PACKAGE:
                configPackage();
                break;
            default:
                throw new CompilerException("Illegal permission argument", new IllegalArgumentException());
        }
        return this;
    }


    public AccessControl addInternalPermission(Permission permission) {
        switch (permission) {
            case PROTECTED:
                configInternalProtected();
                break;
            case PRIVATE:
                configInternalPrivate();
                break;
            case PACKAGE:
                configInternalPackage();
                break;
            default:
                throw new CompilerException("Illegal permission argument", new IllegalArgumentException());
        }
        return this;
    }

    private void configPublic() {
        this.setPublic();
        configInternalProtected();
        configInternalPackage();
    }

    private void configInternalPackage() {
        this.setChildrenPackage();
        configPackage();
    }

    private void configPackage() {
        this.setPackage();
        configFile();
    }

    private void configFile() {
        this.setFile();
        this.setInternalClass();
        this.setSelfClass();
    }

    private void configInternalPrivate() {
        configPrivate();
        this.setInternalClass();
    }

    private void configInternalProtected() {
        configProtected();
        this.setInternalClass();
    }


    private void configProtected() {
        this.setChildrenClass();
        this.setSelfClass();
    }

    private void configPrivate() {
        this.setSelfClass();
    }

    public byte getByte() {
        return permission;
    }

    @Override
    public String toString() {
        String binary = Integer.toString(permission, 2);
        return "AccessControl(" +
                "0".repeat(7 - binary.length()) +
                binary +
                ')';
    }
}
