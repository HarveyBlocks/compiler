package org.harvey.compiler.analysis.core;

import lombok.AllArgsConstructor;
import org.harvey.compiler.exception.CompilerException;

import java.util.Objects;

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
@AllArgsConstructor
public class AccessControl {

    public static final AccessControl PRIVATE = buildDefault(Permission.PRIVATE);
    private static final byte SELF_CLASS_BIT = 0x01;
    private static final byte FILE_BIT = SELF_CLASS_BIT << 1;
    private static final byte CHILDREN_CLASS_BIT = SELF_CLASS_BIT << 2;
    private static final byte CHILDREN_CLASS_INTERNAL_BIT = SELF_CLASS_BIT << 3;
    private static final byte CHILDREN_PACKAGE_BIT = SELF_CLASS_BIT << 4;
    private static final byte PACKAGE_BIT = SELF_CLASS_BIT << 5;
    private static final byte PUBLIC_BIT = SELF_CLASS_BIT << 6;
    private byte permission = 0;

    private AccessControl() {

    }

    private static AccessControl buildDefault(Permission permission) {
        return new Builder().addPermission(permission).build();
    }

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

    private void setChildClassInternal() {
        permission |= CHILDREN_CLASS_INTERNAL_BIT;
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

    private void unsetChildClassInternal() {
        permission &= ~CHILDREN_CLASS_INTERNAL_BIT;
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

    public boolean canChildInternalClass() {
        return (permission & CHILDREN_CLASS_INTERNAL_BIT) != 0;
    }

    private AccessControl addPermission(Permission permission) {
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


    private AccessControl addInternalPermission(Permission permission) {
        switch (permission) {
            case PROTECTED:
                // 本类, 本类内部类, 子类, 子类内部类
                configInternalProtected();
                break;
            case PACKAGE:
                // 本文件, 本类内部类
                configInternalPackage();
                break;
            default:
                throw new CompilerException("Illegal permission argument", new IllegalArgumentException());
        }
        return this;
    }


    private void configPrivate() {
        this.setSelfClass();
    }

    private void configProtected() {
        this.setChildrenClass();
        configPrivate();
    }

    private void configInternalProtected() {
        configProtected();
        this.setChildClassInternal();
    }

    private void configFile() {
        this.setFile();
        configPrivate();
    }

    private void configPackage() {
        this.setPackage();
        configFile();
    }

    private void configInternalPackage() {
        this.setChildrenPackage();
        configPackage();
    }

    private void configPublic() {
        this.setPublic();
        configInternalProtected();
        configInternalPackage();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccessControl)) {
            return false;
        }
        AccessControl that = (AccessControl) o;
        return permission == that.permission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(permission);
    }

    /**
     * 和自身冲突<br>
     * public 和所有的冲突<br>
     * protected 和 protected , private , internal protected, internal private 两两冲突<br>
     * file 和 package 和 internal package 两两<br>
     */
    public static class Builder {
        private final AccessControl product;
        private Permission addedPublic = null;
        private Permission addedInherit = null;
        private Permission addedResource = null;

        public Builder() {
            product = new AccessControl();
        }

        public Builder addPermission(Permission permission) {
            checkConflict(permission);
            product.addPermission(permission);
            return this;
        }


        public Builder addInternalPermission(Permission permission) {
            checkConflict(permission);
            product.addInternalPermission(permission);
            return this;
        }

        private void checkConflict(Permission permission) {
            if (addedPublic != null) {
                throw new IllegalArgumentException("public");
            }
            switch (permission) {
                case PUBLIC:
                    addedPublic = permission;
                    break;
                case PROTECTED:
                case PRIVATE:
                    if (addedInherit != null) {
                        throw new IllegalArgumentException("inherit");
                    }
                    addedInherit = permission;
                    break;
                case FILE:
                case PACKAGE:
                    if (addedResource != null) {
                        throw new IllegalArgumentException("resource");
                    }
                    addedResource = permission;
                    break;
                default:
                    throw new CompilerException("Unexpected permission here");
            }
        }

        public AccessControl build() {
            return product;
        }
    }
}
