package org.harvey.compiler.declare.analysis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.self.CompilerException;

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
@Getter
@AllArgsConstructor
public class AccessControl {
    public static final AccessControl PRIVATE = new AccessControl(Permission.PRIVATE);
    private static final byte SELF_CLASS_BIT = 0x01;
    private static final byte FILE_BIT = SELF_CLASS_BIT << 1;
    private static final byte CHILDREN_CLASS_BIT = SELF_CLASS_BIT << 2;
    private static final byte PACKAGE_BIT = SELF_CLASS_BIT << 3;
    private static final byte PUBLIC_BIT = SELF_CLASS_BIT << 4;
    private final byte permission;

    public AccessControl(Permission permission) {
        if (permission == null) {
            throw new CompilerException(new NullPointerException());
        }
        byte code = SELF_CLASS_BIT;
        switch (permission) {
            case PROTECTED:
                code |= CHILDREN_CLASS_BIT;
                break;
            case PRIVATE:
                break;
            case PUBLIC:
                code |= PUBLIC_BIT;
            case PACKAGE_PROTECTED:
                code |= CHILDREN_CLASS_BIT;
            case PACKAGE:
                code |= PACKAGE_BIT;
                code |= FILE_BIT;
                break;
            case FILE_PROTECTED:
                code |= CHILDREN_CLASS_BIT;
            case FILE:
                code |= FILE_BIT;
                break;
            default:
                throw new CompilerException("unknown permission");
        }
        this.permission = code;
    }

    @Override
    public String toString() {
        if (canPublic()) {
            return Keyword.PUBLIC.getValue();
        } else if (canChildrenClass()) {
            if (canFile()) {
                return Keyword.PROTECTED.getValue() + " " + Keyword.PUBLIC.getValue();
            } else if (canPackage()) {
                return Keyword.PROTECTED.getValue() + " " + Keyword.PUBLIC.getValue();
            } else {
                return Keyword.PROTECTED.getValue();
            }
        } else if (canFile()) {
            return Keyword.FILE.getValue();
        } else if (canPackage()) {
            return Keyword.PACKAGE.getValue();
        } else {
            return Keyword.PRIVATE.getValue();
        }
    }

    public boolean canPublic() {
        return (permission & PUBLIC_BIT) != 0;
    }

    public boolean canPackage() {
        return (permission & PACKAGE_BIT) != 0;
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


    public boolean onlySelfClass() {
        return !canPublic() && !canPackage() && !canChildrenClass() && !canFile();
    }
}
