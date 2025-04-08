package org.harvey.compiler.execute.test.version1.element;

import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;

/**
 * 接口规范{@link PossibleCallableSupplier}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 17:32
 */
public interface CallableSupplierElement {
    CallableRelatedDeclare[] getPossibleCallableRelatedDeclare();

    void resetPossibleCallableRelatedDeclare(CallableRelatedDeclare[] callableRelatedDeclares);
}
