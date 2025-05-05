package org.harvey.compiler.type.raw;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;
import org.harvey.compiler.type.generic.relate.RelatedGenericDefineCache;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefineReference;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-25 23:01
 */
@Getter
public class RelationUsing implements SourcePositionSupplier {
    private final SourcePosition position;
    private final RelationRawType rawType;
    /**
     * 在检查的时候, 发现对面是泛型, 且对构造器有需求, 就进行一一对应<br>
     * nullable
     */
    @Setter
    private int[] constructorReferenceIfNeeded;

    /**
     * @param position at using
     */
    public RelationUsing(SourcePosition position, RelationRawType rawType) {
        this.position = position;
        this.rawType = rawType;
    }

    public String toString(RelatedGenericDefineCache relatedGenericDefineCache) {
        return toString(relatedGenericDefineCache, null);
    }

    public String toString(RelatedGenericDefineCache relatedGenericDefineCache, RelatedGenericDefine[] onCallable) {
        if (!this.rawType.isGenericDefine()) {
            return this.toString();
        }
        RelatedGenericDefineReference reference = (RelatedGenericDefineReference) this.rawType;
        if (!reference.isOnCallable()) {
            return relatedGenericDefineCache.get(reference).getName().getValue();
        }
        if (onCallable == null) {
            throw new CompilerException("no callable generic define can use here.");
        } else {
            int index = reference.getGenericDefineIndexBase() + reference.getGenericDefineIndexOffset();
            return onCallable[index].getName().getValue();
        }
    }

    @Override
    public String toString() {
        return position + rawType.getSimpleName();
    }
}
