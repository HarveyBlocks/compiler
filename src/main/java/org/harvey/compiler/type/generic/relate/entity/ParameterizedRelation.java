package org.harvey.compiler.type.generic.relate.entity;


import org.harvey.compiler.common.Tense;
import org.harvey.compiler.type.raw.RelationRawType;

/**
 * 放在缓存里的接口
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 16:23
 */
public interface ParameterizedRelation {
    Tense getSelfConsistentInspection();

    void setSelfConsistentInspection(Tense tense);

    RelationRawType getRawType();

    RelatedParameterizedStructure getEndOrigin();

    /**
     * @return true for alias, false for structure
     */
    boolean isAlias();

    default boolean isStructure() {
        return !isAlias();
    }

}
