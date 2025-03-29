package org.harvey.compiler.type.transform;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 21:11
 */
@Getter
@AllArgsConstructor
public class DefaultCallableSignature implements CallableSignature {
    private final RelatedGenericDefine[] genericDefine;
    private final RelatedLocalParameterizedType[] param;
    /**
     * default <= (param.length - 1 if defaultStartIndex  else param.length)
     */
    private final int defaultStartIndex;
    private final boolean lastMultiply;

    public DefaultCallableSignature(
            RelatedGenericDefine[] genericDefine,
            RelatedLocalParameterizedType[] param,
            boolean lastMultiply) {
        this.genericDefine = genericDefine;
        this.param = param;
        this.defaultStartIndex = param.length - (lastMultiply ? 1 : 0);
        this.lastMultiply = lastMultiply;
    }

    @Override
    public RelatedGenericDefine getGenericDefine(int index) {
        return genericDefine[index];
    }

    @Override
    public RelatedLocalParameterizedType getParam(int index) {
        return param[index];
    }

}
