package org.harvey.compiler.type.basic.test5;

/**
 * TODO  
 *
 * @date 2025-04-01 14:01
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
interface GenericDefiner extends RawTypeCanInParameter ,SelfConsistentExecutable {
    TempGenericDefine getGeneric(int index);

    int genericLength();

    TempGenericDefine[] genericDefines();
}
