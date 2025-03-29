package org.harvey.compiler.type.transform;

import lombok.AllArgsConstructor;
import org.harvey.compiler.type.generic.relate.ParameterizedRelationCache;
import org.harvey.compiler.type.generic.relate.RelatedGenericDefineCache;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefineReference;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;
import org.harvey.compiler.type.raw.RelationRawType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 21:49
 */
@AllArgsConstructor
public class AssignableFactory {
    private final ParameterizedRelationCache parameterizedRelationCache;

    private final RelatedGenericDefineCache relatedGenericDefineCache;

    public GenericDefineAssignable of(RelatedGenericDefine to) {
        return new GenericDefineAssignable(to, parameterizedRelationCache, this, relatedGenericDefineCache);
    }

    public RelatedGenericDefineCache wrap(RelatedGenericDefine[] defineOnCallable) {
        return new RelatedGenericDefineCacheOnCallable(relatedGenericDefineCache, defineOnCallable);
    }

    public RawTypeAssignable of(RelationRawType to) {
        return new RawTypeAssignable(to, parameterizedRelationCache, this);
    }

    public ParameterizedTypeAssignable of(RelatedParameterizedType to) {
        return new ParameterizedTypeAssignable(to, parameterizedRelationCache, this);
    }


    public CallableSignatureMatcher getCallableSignatureMatcher() {
        return new CallableSignatureMatcherImpl(this);
    }

    private static class RelatedGenericDefineCacheOnCallable extends RelatedGenericDefineCache {
        private final RelatedGenericDefine[] defineOnCallable;

        public RelatedGenericDefineCacheOnCallable(
                RelatedGenericDefineCache relatedGenericDefineCache, RelatedGenericDefine[] defineOnCallable) {
            super(relatedGenericDefineCache);
            this.defineOnCallable = defineOnCallable;
        }

        @Override
        public RelatedGenericDefine get(RelatedGenericDefineReference reference) {
            if (reference.isOnCallable()) {
                return defineOnCallable[reference.getGenericDefineIndexBase() +
                                            reference.getGenericDefineIndexOffset()];
            } else {
                return super.get(reference);
            }
        }
    }
}
