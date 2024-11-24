package org.harvey.compiler.analysis.core;

import org.harvey.compiler.analysis.stmt.meta.mv.MetaFileVariable;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;

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
    public static AccessControl buildFileAccessControl(SourceTextContext permissions, String tobeBuild) {
        AccessControl control = new AccessControl();
        if (permissions.isEmpty()) {
            // 使用默认值
            control.addPermission(MetaFileVariable.DEFAULT_ACCESS_CONTROL_PERMISSION);
            return control;
        }
        int size = permissions.size();
        if (size == 1) {
            // 一定是public或package
            SourceString first = permissions.pollFirst();
            if (Keyword.PUBLIC.equals(first.getValue())) {
                control.addPermission(AccessControl.Permission.PUBLIC);
            } else if (Keyword.PACKAGE.equals(first.getValue())) {
                control.addPermission(AccessControl.Permission.PACKAGE);
            } else if (Keyword.FILE.equals(first.getValue())) {
                control.addPermission(AccessControl.Permission.FILE);
            } else {
                throw new AnalysisExpressionException(first.getPosition(),
                        " is not allowed to be the access control of " + tobeBuild);
            }
        } else if (size == 2) {
            // 一定是inner package
            SourceString pre = permissions.pollFirst();
            SourceString post = permissions.pollFirst();
            assert pre != null;
            assert post != null;
            if (Keyword.INTERNAL.equals(pre.getValue()) && Keyword.PACKAGE.equals(post.getValue())) {
                control.addInternalPermission(AccessControl.Permission.PACKAGE);
            } else {
                throw new AnalysisExpressionException(pre.getPosition(),
                        SourcePosition.moveToEnd(post.getPosition(), post.getValue()),
                        " is not allowed to be the access control of " + tobeBuild);
            }
        }
        if (!permissions.isEmpty()) {
            // 如果还有剩余, 全部都错
            permissions.throwAllAsCompileException(" is not allowed to be the access control of " + tobeBuild, AnalysisExpressionException.class);
        }
        return control;
    }
}
