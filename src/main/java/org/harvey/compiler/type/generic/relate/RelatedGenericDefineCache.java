package org.harvey.compiler.type.generic.relate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.harvey.compiler.common.collecction.BaseReference;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefineReference;

import java.util.*;

import static org.harvey.compiler.io.cache.node.FileNode.GET_MEMBER;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-24 22:31
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RelatedGenericDefineCache {
    /**
     * <declare from, <<identifier,index>, base index>>
     */
    private final Map<String, Pair<Map<String, Integer>, Integer>> dictionary;
    private final List<RelatedGenericDefine> genericDefinePool;

    public RelatedGenericDefineCache(RelatedGenericDefineCache relatedGenericDefineCache) {
        dictionary = relatedGenericDefineCache.dictionary;
        genericDefinePool = relatedGenericDefineCache.genericDefinePool;
    }

    public RelatedGenericDefineCache() {
        dictionary = new HashMap<>();
        genericDefinePool = new ArrayList<>();
    }

    /**
     * @return <pre>{@code
     * <basic declare index on generic defines, generic define index offset>
     * }</pre>
     */
    public BaseReference getIndexInDeclare(String declarePlaceKey, String name) {
        Pair<Map<String, Integer>, Integer> pair = dictionary.get(declarePlaceKey);
        if (pair == null) {
            return null;
        } else {
            Integer reference = pair.getKey().get(name);
            if (reference == null || pair.getValue() == null) {
                throw new CompilerException(
                        "declare place " + declarePlaceKey + " in  dictionary, but generic: " + name + " not in.");
            }
            return new BaseReference(pair.getValue(), reference - pair.getValue());
        }
    }

    /**
     * @return <pre>{@code
     * <basic declare index on generic defines, generic define index offset>
     * }</pre>
     */
    public BaseReference getIndexInDeclare(String[] declarePlace, String name) {
        String key = geDeclareFromKey(declarePlace);
        return getIndexInDeclare(key, name);
    }

    public RelatedGenericDefine get(String[] declarePlace, String name) {
        BaseReference index = getIndexInDeclare(declarePlace, name);
        if (index == null) {
            return null;
        } else {
            return genericDefinePool.get(index.getBase() + index.getOffset());
        }
    }

    public RelatedGenericDefine get(RelatedGenericDefineReference reference) {
        if (!reference.isOnCallable()) {
            return get(reference.getGenericDefineIndexBase(), reference.getGenericDefineIndexOffset());
        } else {
            throw new CompilerException("can not deal reference");
        }
    }

    public RelatedGenericDefine[] get(String declarePlaceKey) {
        Pair<Map<String, Integer>, Integer> pair = dictionary.get(declarePlaceKey);
        if (pair == null) {
            return null;
        }
        int size = pair.getKey().size();
        Integer base = pair.getValue();
        if (base == null || base + size >= genericDefinePool.size()) {
            throw new CompilerException(
                    "have some thing in identifier map : " + pair.getKey() + ", but not find in generic define pool");
        }
        RelatedGenericDefine[] result = new RelatedGenericDefine[size];
        for (int i = 0; i < size; i++) {
            result[i] = genericDefinePool.get(i + base);
        }
        return result;
    }

    public RelatedGenericDefine[] get(String[] declarePlace) {
        String key = geDeclareFromKey(declarePlace);
        return get(key);
    }

    public RelatedGenericDefine get(String[] declarePlace, int indexInDeclare) {
        String key = geDeclareFromKey(declarePlace);
        return get(key, indexInDeclare);
    }

    public RelatedGenericDefine get(String declarePlaceKey, int indexInDeclare) {
        Pair<Map<String, Integer>, Integer> pair = dictionary.get(declarePlaceKey);
        if (pair == null) {
            return null;
        } else {
            Integer reference = pair.getValue();
            if (reference == null || reference + indexInDeclare >= genericDefinePool.size()) {
                throw new CompilerException("declare place " +
                                            declarePlaceKey +
                                            " in  dictionary, but generic: " +
                                            indexInDeclare +
                                            " not in.");
            }
            return genericDefinePool.get(reference + indexInDeclare/*offset*/);
        }
    }

    public RelatedGenericDefine get(int base, int offset) {
        return genericDefinePool.get(base + offset);
    }

    public static String geDeclareFromKey(String[] declarePlace) {
        return StringUtil.join(declarePlace, GET_MEMBER);
    }

    public boolean contains(String[] declarePlace) {
        return dictionary.containsKey(geDeclareFromKey(declarePlace));
    }

    public int put(String declarePlaceKey, RelatedGenericDefine[] defines) {
        Pair<Map<String, Integer>, Integer> mapIntegerPair = dictionary.get(declarePlaceKey);
        if (mapIntegerPair != null) {
            return 0;
        }
        int reference = genericDefinePool.size();
        HashMap<String, Integer> nameMap = new HashMap<>();
        for (RelatedGenericDefine define : defines) {
            nameMap.put(define.getName().getValue(), genericDefinePool.size());
            genericDefinePool.add(define);
        }
        dictionary.put(declarePlaceKey, new Pair<>(Collections.unmodifiableMap(nameMap), reference));
        return reference;
    }

    public void put(String[] declarePlace, RelatedGenericDefine[] defines) {
        String key = geDeclareFromKey(declarePlace);
        put(key, defines);
    }


    /**
     * @return 开始, 结束
     */
    public Pair<BaseReference, BaseReference> getIndexInDeclare(String declarePlaceKey) {
        Pair<Map<String, Integer>, Integer> mapIntegerPair = this.dictionary.get(declarePlaceKey);
        if (mapIntegerPair == null) {
            return null;
        }
        return new Pair<>(
                new BaseReference(mapIntegerPair.getValue(), 0),
                new BaseReference(mapIntegerPair.getValue(), mapIntegerPair.getKey().size() - 1)
        );
    }

}
