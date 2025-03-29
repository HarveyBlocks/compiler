package org.harvey.compiler.type.generic.relate;

import org.harvey.compiler.common.collecction.BaseReference;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.context.ConstructorContext;
import org.harvey.compiler.declare.context.ParamContext;
import org.harvey.compiler.declare.context.TypeAlias;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.KeywordString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.cache.FileCache;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.cache.resource.StatementResource;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.relate.entity.*;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.KeywordBasicType;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.transform.CallableSignature;
import org.harvey.compiler.type.transform.DefaultCallableSignature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 进行关系转换, 不进行关系检查检查
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 16:33
 */
public class ParameterizedRelationLoader {
    private final FileCache fileCache;
    private final RelatedGenericDefineCache relatedGenericDefineCache;
    private final RelatedParameterizedTypeBuilderFactory relatedParameterizedTypeBuilderFactory;

    private final ParameterizedRelationCache parameterizedRelationCache;

    public ParameterizedRelationLoader(
            FileCache fileCache,
            RelatedGenericDefineCache relatedGenericDefineCache,
            RelatedParameterizedTypeBuilderFactory relatedParameterizedTypeBuilderFactory) {
        this.fileCache = fileCache;
        this.relatedGenericDefineCache = relatedGenericDefineCache;
        this.relatedParameterizedTypeBuilderFactory = relatedParameterizedTypeBuilderFactory;
        this.parameterizedRelationCache = new ParameterizedRelationCache();
    }

    public RelatedParameterizedAlias loadAlias(RelationRawType rawType) throws IOException {
        if (!rawType.isAlias()) {
            throw new CompilerException(
                    "expect a alias raw type, but not: " + rawType.getType(), new IllegalArgumentException());
        }
        RelatedParameterizedAlias inCache = parameterizedRelationCache.getAlias(rawType);
        if (inCache != null) {
            return inCache;
        }
        FileNode fileNode = fileCache.getInCache(rawType.getJoinedFullname());
        StatementResource resource = fileNode.getResource();
        TypeAlias typeAlias = resource.getTypeAlias(rawType.getDeclareIdentifier(), rawType.getJoinedFullname());
        return loadAlias(typeAlias, rawType);
    }

    /**
     * typeAlias and rawType are same thing different stage
     */
    public RelatedParameterizedAlias loadAlias(TypeAlias typeAlias, RelationRawType rawType) throws IOException {
        RelatedParameterizedAlias inCache = parameterizedRelationCache.getAlias(rawType);
        if (inCache != null) {
            return inCache;
        }
        RelatedParameterizedTypeBuilder relatedParameterizedTypeBuilder = relatedParameterizedTypeBuilderFactory.create(
                rawType);
        GenericDefine[] aliasGenericMessage = typeAlias.getAliasGenericMessage();
        RelatedGenericDefine[] relatedGenericDefines = relatedParameterizedTypeBuilder.relateGenericDefine(
                rawType.getJoinedFullname(), aliasGenericMessage);
        Pair<BaseReference, BaseReference> indexInDeclare = relatedGenericDefineCache.getIndexInDeclare(
                rawType.getJoinedFullname());
        ParameterizedType<ReferenceElement> origin = typeAlias.getOrigin();
        RelatedParameterizedType relatedParameterizedType = relatedParameterizedTypeBuilder.parameterizedTypeExcludeCallableGeneric(
                origin);
        RelatedParameterizedAlias result = new RelatedParameterizedAlias(rawType, indexInDeclare.getKey(),
                indexInDeclare.getValue(), relatedParameterizedType, null/*TODO end origin*/
        );
        parameterizedRelationCache.put(result);
        return result;
    }

    public RelatedParameterizedStructure loadStructure(RelationRawType rawType) throws IOException {
        if (!rawType.isStructure()) {
            throw new CompilerException(
                    "expect a structure raw type, but not: " + rawType.getType(), new IllegalArgumentException());

        }
        RelatedParameterizedStructure inCache = parameterizedRelationCache.getStructure(rawType);
        if (inCache != null) {
            return inCache;
        }
        FileNode fileNode = fileCache.getInCache(rawType.getJoinedFullname());
        if (!fileNode.isStructure()) {
            throw new CompilerException("raw type is structure, but not structure in file cache!");
        }
        StatementResource resource = fileNode.getResource();
        if (!resource.isStructure()) {
            throw new CompilerException(
                    "raw type is structure, and structure in file cache, but resource is not structure!");
        }
        return loadStructure(resource, rawType);
    }

    public RelatedParameterizedStructure loadStructure(
            StatementResource resource, RelationRawType rawType) throws IOException {
        if (!resource.isStructure()) {
            throw new CompilerException("expected a structure resource!", new IllegalArgumentException());
        }
        RelatedParameterizedStructure inCache = parameterizedRelationCache.getStructure(rawType);
        if (inCache != null) {
            return inCache;
        }
        RelatedParameterizedTypeBuilder relatedParameterizedTypeBuilder = relatedParameterizedTypeBuilderFactory.create(
                rawType);
        GenericDefine[] genericDefines = resource.getGenericMessage();
        RelatedGenericDefine[] relatedGenericDefines = relatedParameterizedTypeBuilder.relateGenericDefine(
                rawType.getJoinedFullname(), genericDefines);
        ParameterizedType<ReferenceElement> superComplexStructure = resource.getSuperComplexStructure();

        RelatedParameterizedType relatedSuperParameterizedType = relatedParameterizedTypeBuilder.parameterizedTypeExcludeCallableGeneric(
                superComplexStructure);
        List<ParameterizedType<ReferenceElement>> interfaceList = resource.getInterfaceList();
        RelatedParameterizedType[] relatedInterfaceList = new RelatedParameterizedType[interfaceList.size()];
        for (int i = 0; i < relatedInterfaceList.length; i++) {
            ParameterizedType<ReferenceElement> interfaceParameterizedType = interfaceList.get(i);
            relatedInterfaceList[i] = relatedParameterizedTypeBuilder.parameterizedTypeExcludeCallableGeneric(
                    interfaceParameterizedType);
        }
        List<ConstructorContext> constructorList = resource.getConstructors();
        List<CallableSignature> relatedConstructorList = new ArrayList<>(constructorList.size());

        for (ConstructorContext constructorContext : constructorList) {
            // 不需要 constructorContext.getBody();
            // 序列化之前需要, 现在不需要 constructorContext.getGenericMap();
            // 类内分析 不需要 constructorContext.getAccessControl();
            // 不需要constructorContext.getThrowsExceptions();
            List<GenericDefine> genericMessage = constructorContext.getGenericMessage();
            RelatedGenericDefine[] relatedDefineList = relatedParameterizedTypeBuilder.relateGenericDefine(
                    null,
                    genericMessage.toArray(GenericDefine[]::new)
            );
            // 关于constructor TODO 更多的检查, 不应该这么简单, 很奇怪
            // weird and strange
            List<ParamContext> paramList = constructorContext.getParamList();
            RelatedLocalParameterizedType[] relatedParamList = new RelatedLocalParameterizedType[paramList.size()];
            relatedConstructorList.add(new DefaultCallableSignature(relatedDefineList, relatedParamList,
                    constructorContext.startOfDefaultParam(), constructorContext.isLastMultiply()
            ));
            for (int i = 0; i < relatedParamList.length; i++) {
                LocalType localType = paramList.get(i).getLocalType();
                RelatedParameterizedType type = relatedParameterizedTypeBuilder.parameterizedType(
                        localType.getSourceType(), relatedDefineList);
                relatedParamList[i] = new RelatedLocalParameterizedType(localType.isFinal(), localType.isConst(), type);
            }
        }
        RelatedParameterizedStructure result = new RelatedParameterizedStructure(rawType, relatedGenericDefines,
                relatedSuperParameterizedType, relatedInterfaceList, relatedConstructorList,null/*TODO*/
        );
        parameterizedRelationCache.put(result);
        return result;
    }

    /**
     * 从缓存中获取
     */
    public RelatedGenericDefine getGenericDefineOnType(RelationRawType rawType) {
        if (!rawType.isGenericDefine()) {
            throw new CompilerException(
                    "expected a generic define reference raw type!", new IllegalArgumentException());
        }
        RelatedGenericDefineReference reference = (RelatedGenericDefineReference) rawType;
        if (reference.isOnCallable()) {
            throw new CompilerException(
                    "expected a generic define on 'type' refer raw type!", new IllegalArgumentException());
        } else {
            return relatedGenericDefineCache.get(reference);
        }
    }

    /**
     * @param referredTarget 从参数中获取
     */
    public RelatedGenericDefine referToDefineOnCallable(
            RelationRawType rawType, RelatedGenericDefine[] referredTarget) {
        if (!rawType.isGenericDefine()) {
            throw new CompilerException(
                    "expected a generic define reference raw type!", new IllegalArgumentException());
        }
        RelatedGenericDefineReference reference = (RelatedGenericDefineReference) rawType;
        if (reference.isOnCallable()) {
            return referredTarget[reference.getGenericDefineIndexOffset()];
        } else {
            throw new CompilerException(
                    "expected a generic define on callable reference raw type!", new IllegalArgumentException());
        }
    }

    /**
     * 不加入缓存
     */
    public static KeywordString loadBasic(RelationRawType rawType) {
        if (!rawType.isBasicType()) {
            return new KeywordString(rawType.getPosition(), null);
        }
        KeywordBasicType basicType = (KeywordBasicType) rawType;
        return new KeywordString(basicType.getPosition(), basicType.getBasicType());
    }
}
