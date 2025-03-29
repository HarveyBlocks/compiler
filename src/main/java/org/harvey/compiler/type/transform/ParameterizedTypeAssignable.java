package org.harvey.compiler.type.transform;

import lombok.AllArgsConstructor;
import org.harvey.compiler.type.generic.relate.ParameterizedRelationCache;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;
import org.harvey.compiler.type.raw.RelationUsing;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-28 10:58
 */
@AllArgsConstructor
public class ParameterizedTypeAssignable implements Assignable {
    private final RelatedParameterizedType to;
    private final ParameterizedRelationCache parameterizedRelationCache;
    private final AssignableFactory assignableFactory;

    @Override
    public void assign(RelatedGenericDefine from) {

    }

    @Override
    public void assign(RelatedParameterizedType from) {

    }

    @Override
    public void assign(RelationUsing from) {

    }

    @Override
    public void assign(RelatedLocalParameterizedType from) {

    }

    @Override
    public void selfConsistent() {

    }
}
