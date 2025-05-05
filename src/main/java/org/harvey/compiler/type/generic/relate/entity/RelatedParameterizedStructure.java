package org.harvey.compiler.type.generic.relate.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.Tense;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.transform.CallableSignature;

import java.util.List;

/**
 * 把继承的形式保存下来
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 16:12
 */
@Getter
@AllArgsConstructor
public class RelatedParameterizedStructure implements ParameterizedRelation {
    private final RelationRawType rawType;
    private final RelatedGenericDefine[] defines;
    private final RelatedParameterizedType superType;
    private final RelatedParameterizedType[] superInterfaces;
    private final List<CallableSignature> constructors;
    @Setter
    private Tense selfConsistentInspection;

    @Override
    public RelatedParameterizedStructure getEndOrigin() {
        return this;
    }

    @Override
    public boolean isAlias() {
        return false;
    }
}
