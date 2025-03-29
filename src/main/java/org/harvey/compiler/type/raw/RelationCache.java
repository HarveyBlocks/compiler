package org.harvey.compiler.type.raw;

import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.harvey.compiler.io.cache.node.FileNode.GET_MEMBER;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 19:42
 */
public class RelationCache {
    /**
     * 建立raw type之间的关系, 要求是检查A和B之间是否有继承关系
     */
    private final Map<String, RelationRawType> relationCache = new HashMap<>();

    {
        // 注册int等基本数据类型
        registerBasicType(KeywordBasicType.BOOL);
        registerBasicType(KeywordBasicType.CHAR);
        registerBasicType(KeywordBasicType.FLOAT32);
        registerBasicType(KeywordBasicType.FLOAT64);
        registerBasicType(KeywordBasicType.INT8);
        registerBasicType(KeywordBasicType.INT16);
        registerBasicType(KeywordBasicType.INT32);
        registerBasicType(KeywordBasicType.INT64);
        registerBasicType(KeywordBasicType.UINT8);
        registerBasicType(KeywordBasicType.UINT16);
        registerBasicType(KeywordBasicType.UINT32);
        registerBasicType(KeywordBasicType.UINT64);
    }

    public static RelationRawType dealBasicType(File typeFromFile, Keyword keyword, SourcePosition position) {
        if (keyword == null) {
            throw new CompilerException("keyword can not be null");
        }
        RelationRawType relationRawType = KeywordBasicType.get(keyword);
        if (relationRawType == null) {
            throw new CompileMultipleFileException(
                    typeFromFile, position, "except a type, and " + keyword + " is not allowed there");
        }
        return relationRawType;
    }

    private void registerBasicType(RelationRawType type) {
        relationCache.put(type.getJoinedFullname(), type);
    }

    public RelationRawType put(String key, RelationRawType result) {
        return relationCache.put(key, result);
    }

    public boolean containsKey(String key) {
        return get(key) != null;
    }
    public boolean containsKey(FullIdentifierString key) {
        return get(key) != null;
    }


    public RelationRawType get(String identifier) {
        return relationCache.get(identifier);
    }
    public RelationRawType get(FullIdentifierString fullIdentifier) {
        return relationCache.get(fullIdentifier.joinFullnameString(GET_MEMBER));
    }

    public boolean contains(FullIdentifierString fullIdentifier) {
        return get(fullIdentifier) != null;
    }

}
