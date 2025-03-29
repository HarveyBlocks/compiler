package org.harvey.compiler.declare.context;

import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.execute.expression.ReferenceElement;


/**
 * 需要被序列化的的对象的统一接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 22:34
 */
public interface DeclaredContext {
    // -1表示无, 否则表示索引, 不是索引的, 就是未知的标识
    ReferenceElement getIdentifierReference();

    AccessControl getAccessControl();

    Embellish getEmbellish();

}
