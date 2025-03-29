package org.harvey.compiler.type.transform;


import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 21:08
 */
public interface CallableSignature {
    boolean isLastMultiply();

    RelatedGenericDefine getGenericDefine(int index);

    RelatedLocalParameterizedType getParam(int index);

    RelatedGenericDefine[] getGenericDefine();

    RelatedLocalParameterizedType[] getParam();
}
