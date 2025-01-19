package org.harvey.compiler.analysis.core;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

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
    public static final AccessControl UNSURE_ACCESS_CONTROL = UnsureAccessControl.instance();

    public static AccessControl buildFileAccessControl(SourceTextContext permissions, String tobeBuilt,
                                                       Permission defaultPermission) {
        AccessControl.Builder control = new AccessControl.Builder();
        if (permissions.isEmpty()) {
            // 使用默认值
            return control.addPermission(defaultPermission).build();
        }
        SourcePosition sp = SourcePosition.UNKNOWN;
        try {
            while (!permissions.isEmpty()) {
                SourceString first = permissions.pollFirst();
                sp = first.getPosition();
                addEachFilePermission(permissions, first, control, tobeBuilt);
            }
        } catch (IllegalArgumentException iae) {
            throw new AnalysisExpressionException(sp, "Can be conflict at the " + iae.getMessage() + " layer");
        }
        return control.build();
    }

    private static void addEachFilePermission(
            SourceTextContext permissions, SourceString first,
            AccessControl.Builder control, String tobeBuilt) {
        Permission value = Permission.get(first.getValue());
        switch (value) {
            case INTERNAL:
                // 一定是package
                if (!permissions.isEmpty()) {
                    SourceString afterInternal = permissions.pollFirst();
                    if (!Keyword.PACKAGE.equals(afterInternal.getValue())) {
                        throw new AnalysisExpressionException(afterInternal.getPosition(),
                                " is illegal here.");
                    }
                }
                control.addInternalPermission(Permission.PACKAGE);
                break;
            case PUBLIC:
            case PACKAGE:
            case FILE:
                control.addPermission(value);
                break;
            default:
                throw new AnalysisExpressionException(first.getPosition(),
                        " is not allowed to be the access control of " + tobeBuilt);
        }
    }


    public static AccessControl buildMemberAccessControl(
            SourceTextContext permissions, Permission defaultPermission) {
        // 有哪些情况?
        AccessControl.Builder control = new AccessControl.Builder();
        // public
        // protected
        // private
        // file
        // private
        // package
        // internal private
        // internal package
        // internal protected
        if (permissions.isEmpty()) {
            // 使用默认值
            return control.addPermission(defaultPermission).build();
        }
        SourcePosition sp = SourcePosition.UNKNOWN;
        try {
            while (!permissions.isEmpty()) {
                SourceString first = permissions.pollFirst();
                sp = first.getPosition();
                addEachMemberPermission(permissions, first, control);
            }
        } catch (IllegalArgumentException iae) {
            throw new AnalysisExpressionException(sp, "Can be conflict at the " + iae.getMessage() + " layer");
        }
        return control.build();
    }

    private static void addEachMemberPermission(
            SourceTextContext permissions, SourceString first,
            AccessControl.Builder control) {
        if (!Keyword.INTERNAL.equals(first.getValue())) {
            // 没有internal, 直接返回了
            control.addPermission(Permission.get(first.getValue()));
            return;
        }
        // 如果省略, 那就是package
        Permission internalAblePermission = Permission.PACKAGE;
        if (!permissions.isEmpty()) {
            // 不省略, 那需要是InternalAble
            SourceString afterInternal = permissions.pollFirst();
            internalAblePermission = Permission.getInternalAble(afterInternal.getValue());
            if (internalAblePermission == null) {
                throw new AnalysisExpressionException(afterInternal.getPosition(), " is illegal after internal");
            }
        }
        control.addInternalPermission(internalAblePermission);
    }

    public static void abstractEmbellish(AccessControl control, SourcePosition position) {
        if (AccessControl.PRIVATE.equals(control)) {
            throw new AnalysisExpressionException(position, " Private is conflict with abstract");
        }
    }

    public static Permission getDefaultPermission(Environment environment) {
        switch (environment) {
            case FILE:
                return Permission.FILE;
            case ENUM:
            case CLASS:
            case STRUCT:
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
                return Permission.PRIVATE;
            case INTERFACE:
                return Permission.PUBLIC;
            default:
                throw new CompilerException("Unknown environment");
        }
    }
}
