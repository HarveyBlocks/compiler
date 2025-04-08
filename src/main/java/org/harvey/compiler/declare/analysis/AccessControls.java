package org.harvey.compiler.declare.analysis;

import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.Arrays;

/**
 * 工具类<br>
 *
 * <h3 id='访问控制'><span>访问控制</span></h3>
 * <ul>
 *     <li><p><span>作为一个文件层的变量/函数/复合结构, 其一定是的file|public|package|inner package</span></p></li>
 *     <li><p><span>作为一个成员, 其作用域一定是任意作用域</span></p></li>
 *     <li><p><span>作为一个代码块里的局部成员, 一定没有作用域</span></p></li>
 *     <li><p><span>作为一个抽象方法, 其方法一定要被重写</span></p>
 *         <p>
 *             <span>所以其作用域一定是public或[internal]protected+[[internal]package|file ]</span>
 *         </p></li>
 * </ul>
 * 1. public
 * 1. protected
 * 2. internal protected
 * 2. protected package
 * 2. protected file
 * 3. internal protected package
 * 3. internal protected file
 * 3. protected internal package
 * 4. internal protected internal package;
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 21:15
 */
public class AccessControls {
    private static final Permission[] NONE_ILLEGAL = new Permission[0];
    private static final Permission[] FILE_ILLEGAL = {Permission.PRIVATE, Permission.PROTECTED,
            Permission.PACKAGE_PROTECTED, Permission.FILE_PROTECTED};
    private static final Permission[] STRUCTURE_ILLEGAL = NONE_ILLEGAL;
    private static final Permission[] INTERFACE_ILLEGAL = {Permission.PRIVATE};


    public static Permission buildPermission(SourceTextContext permissions, Permission defaultPermission) {
        // 1. file
        // 2. package
        // 3. public
        // 4. private
        // 5. protected
        // 6. protected package
        // 7 package protected
        // 8. file protected
        // 9.  protected file
        if (permissions.isEmpty()) {
            return defaultPermission;
        } else if (permissions.size() == 1) {
            Permission permission = Permission.get(permissions.getFirst().getValue());
            if (permission != null) {
                return permission;
            }
        } else if (permissions.size() == 2) {
            Permission first = Permission.get(permissions.getFirst().getValue());
            Permission last = Permission.get(permissions.getLast().getValue());
            if (first == Permission.PACKAGE && last == Permission.PROTECTED ||
                last == Permission.PACKAGE && first == Permission.PROTECTED) {
                return Permission.PACKAGE_PROTECTED;
            }
            if (first == Permission.FILE && last == Permission.PROTECTED ||
                last == Permission.FILE && first == Permission.PROTECTED) {
                return Permission.FILE_PROTECTED;
            }
        }
        throw new AnalysisExpressionException(permissions.getFirst().getPosition(), permissions.getLast().getPosition(),
                "illegal permission"
        );
    }

    public static AccessControl buildAccessControl(Permission permission, Permission... illegalPermission) {
        if (illegalPermission == null) {
            return new AccessControl(permission);
        }
        for (Permission illegal : illegalPermission) {
            if (illegal == permission) {
                return null;
            }
        }
        return new AccessControl(permission);
    }

    public static Permission getDefaultPermission(Environment environment) {
        switch (environment) {
            case FILE:
                return Permission.FILE;
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
            case ENUM:
            case CLASS:
                return Permission.PRIVATE;
            case STRUCT:
            case INTERFACE:
                return Permission.PUBLIC;
            default:
                throw new CompilerException("Unexpected value: " + environment);
        }
    }

    public static Permission[] illegalPermission(Environment environment) {
        switch (environment) {
            case FILE:
                return FILE_ILLEGAL;
            case ENUM:
            case CLASS:
            case STRUCT:
                return STRUCTURE_ILLEGAL;
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
            case INTERFACE:
                return INTERFACE_ILLEGAL;
            default:
                throw new CompilerException("Unexpected value: " + environment);
        }
    }

    public static AccessControl buildAccessControl(Environment environment, SourceTextContext permissions) {
        Permission defaultPermission = getDefaultPermission(environment);
        Permission permission = AccessControls.buildPermission(permissions, defaultPermission);
        Permission[] illegalPermission = AccessControls.illegalPermission(environment);
        AccessControl accessControl = buildAccessControl(permission, illegalPermission);
        if (accessControl != null) {
            return accessControl;
        }
        // 存在不正常的情况
        if (permissions.isEmpty()) {
            throw new CompilerException(
                    "default access version2 of " + environment + " is in the illegal version2 set: " +
                    Arrays.toString(illegalPermission));
        } else if (permissions.size() == 1) {
            throw new AnalysisExpressionException(
                    permissions.getFirst().getPosition(),
                    "illegal in: " + environment
            );
        } else {
            throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                    permissions.getLast().getPosition(), "illegal in: " + environment
            );
        }
    }

}
