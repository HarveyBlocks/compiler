package org.harvey.compiler.type.generic.relate;

import lombok.AllArgsConstructor;
import org.harvey.compiler.type.generic.relate.entity.ParameterizedRelation;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedAlias;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedStructure;
import org.harvey.compiler.type.raw.RelationRawType;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 16:20
 */
@AllArgsConstructor
public class ParameterizedRelationCache {
    private final Map<String, ParameterizedRelation> cache = new HashMap<>();

    public void put(ParameterizedRelation relation) {
        cache.put(relation.getRawType().getJoinedFullname(), relation);
    }

    public ParameterizedRelation get(RelationRawType rawType) {
        if (rawType.isAlias() || rawType.isStructure()) {
            return cache.get(rawType.getJoinedFullname());
        } else {
            return null;
        }
    }

    public RelatedParameterizedAlias getAlias(String rawTypeKey) {
        ParameterizedRelation inCache = cache.get(rawTypeKey);
        if (inCache != null && inCache.isAlias()) {
            return (RelatedParameterizedAlias) inCache;
        } else {
            return null;
        }
    }
    public RelatedParameterizedAlias getAlias(RelationRawType rawType) {
        return getAlias(rawType.getJoinedFullname());
    }
    public RelatedParameterizedStructure getStructure(RelationRawType rawType) {
        return getStructure(rawType.getJoinedFullname());
    }

    public RelatedParameterizedStructure getStructure(String rawTypeKey) {
        ParameterizedRelation inCache = cache.get(rawTypeKey);
        if (inCache != null && inCache.getRawType().isStructure()) {
            return (RelatedParameterizedStructure) inCache;
        } else {
            return null;
        }
    }
}
