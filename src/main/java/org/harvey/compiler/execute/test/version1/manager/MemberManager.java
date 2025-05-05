package org.harvey.compiler.execute.test.version1.manager;

import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * 一个函数持有一份, 注意时空调整作用域
 */
public interface MemberManager {
    /**
     * @param name
     * @return null for not find
     */
    MemberSupplier create(SourcePosition using, String name);

    PossibleCallableSupplier createPossibleCallable(SourcePosition using, String callableName);
}
