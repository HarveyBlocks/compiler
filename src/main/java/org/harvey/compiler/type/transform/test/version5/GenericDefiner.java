package org.harvey.compiler.type.transform.test.version5;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-01 14:01
 */
interface GenericDefiner extends RawTypeCanInParameter, SelfConsistentExecutable {
    TempGenericDefine getGeneric(int index);

    int genericLength();

    TempGenericDefine[] genericDefines();
}
