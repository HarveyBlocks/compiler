package org.harvey.compiler.type.transform.test.version4;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 22:59
 */
interface Assignable {
    void assign(Parameterized from);

    void assign(TempGenericDefine from);
}
