package org.harvey.compiler.type.generic.relate;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.collecction.BaseReference;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.stage.CompileStage;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefineReference;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;
import org.harvey.compiler.type.generic.using.LocalParameterizedType;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.RawTypeRelationshipLoader;
import org.harvey.compiler.type.raw.RelationCache;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.raw.RelationUsing;
import org.harvey.compiler.type.transform.AssignManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 构建{@link RelatedGenericDefineReference}{@link RelatedParameterizedType}
 * 一个Builder对应一个文件
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-25 18:45
 */
public class RelatedParameterizedTypeBuilder {
    private final GenericDefineReader genericDefineReader;
    private final RawTypeRelationshipLoader rawTypeRelationshipLoader;
    private final RelatedGenericDefineCache relatedGenericDefineCache;
    private final File typeFromFile;
    private final CompileStage compileStage;
    private final IdentifierManager identifierManager;
    private final AssignManager assignManager;

    public RelatedParameterizedTypeBuilder(
            AssignManager assignManager,
            GenericDefineReader genericDefineReader,
            RawTypeRelationshipLoader rawTypeRelationshipLoader,
            RelatedGenericDefineCache relatedGenericDefineCache,
            File typeFromFile,
            CompileStage compileStage,
            IdentifierManager identifierManager) {
        this.genericDefineReader = genericDefineReader;
        this.rawTypeRelationshipLoader = rawTypeRelationshipLoader;
        this.relatedGenericDefineCache = relatedGenericDefineCache;
        this.typeFromFile = typeFromFile;
        this.compileStage = compileStage;
        this.identifierManager = identifierManager;
        if (compileStage == CompileStage.COMPILED) {
            // 不需要 assign manager, 因为这个Builder构建出来的, 已经检查过了, 不需要检查了
            this.assignManager = null;
        } else {
            if (assignManager == null) {
                throw new CompilerException("assign manager is needed at stage: " + compileStage);
            } else {
                this.assignManager = assignManager;
            }
        }

    }


    // ---------------------------------分析GenericDefine-----------------------------------------
    public RelatedGenericDefine[] genericDefine(
            FullIdentifierString declareFromIdentifier) throws IOException {
        // 1. 检查缓存是否存在, 存在就直接返回
        String[] fullname = declareFromIdentifier.getFullname();
        String key = RelatedGenericDefineCache.geDeclareFromKey(fullname);
        Pair<BaseReference,BaseReference> fromCache = relatedGenericDefineCache.getIndexInDeclare(key);
        if (fromCache != null) {
            // return referToCache(key, fullname, fromCache.getValue(), fromCache.getKey());
            return fromCache(key);
        }
        readDefineThenAddToCache(declareFromIdentifier, key);
        //  将泛型转换后的泛型定义转为关联类型, 返回
        fromCache = relatedGenericDefineCache.getIndexInDeclare(key);
        if (fromCache == null) {
            throw new CompilerException("put to related generic define cache failed");
        }
        // return referToCache(key, fullname, fromCache.getValue(), fromCache.getKey());
        return fromCache(key);
    }

    public RelatedGenericDefine[] fromCache(String key) {
        // 1. 检查缓存是否存在, 存在就直接返回
        return relatedGenericDefineCache.get(key);
    }

    /**
     * 从缓存中获取RelatedGenericDefine, 并转化为RelatedGenericDefineReference
     */
    @Deprecated
    private RelatedGenericDefineReference[] referToCache(
            String key, String[] fullname, Integer defineSize, Integer genericDefineIndexBase) {
        RelatedGenericDefine[] relatedGenericDefines = relatedGenericDefineCache.get(key);
        RelatedGenericDefineReference[] fromCacheReference = new RelatedGenericDefineReference[defineSize];
        for (int i = 0; i < defineSize; i++) {
            fromCacheReference[i] = new RelatedGenericDefineReference(fullname, key, genericDefineIndexBase, i,
                    relatedGenericDefines[i].getName(), typeFromFile, false
            );
        }
        return fromCacheReference;
    }

    @AllArgsConstructor
    private static class QueueElement {
        private ParameterizedType<ReferenceElement> toBeMap;
        private Consumer<RelatedParameterizedType> consumer;

        private void set(RelatedParameterizedType reference) {
            consumer.accept(reference);
        }
    }

    // ---------------------------------分析ParameterType-----------------------------------------\
    public RelatedParameterizedType parameterizedTypeExcludeCallableGeneric(ParameterizedType<ReferenceElement> type) throws IOException {
        return parameterizedType(type, null);
    }

    public RelatedParameterizedType parameterizedType(
            ParameterizedType<ReferenceElement> type,
            RelatedGenericDefine[] onCallable) throws IOException {
        LinkedList<QueueElement> queue = new LinkedList<>();
        queue.addLast(new QueueElement(type, a -> {
        }));
        RelatedParameterizedType result = null;
        while (!queue.isEmpty()) {
            QueueElement element = queue.removeFirst();
            result = parameterizedType(element.toBeMap, queue, onCallable);
            element.set(result);
        }
        return result;
    }

    /**
     * @deprecated {@link #relateGenericDefine(String, GenericDefine[])}
     */
    @Deprecated
    public RelatedGenericDefine[] relateGenericDefineOnCallable(GenericDefine[] onCallable) throws IOException {
        LinkedList<QueueElement> queue = new LinkedList<>();
        RelatedGenericDefine[] relatedGenericDefines = relateGenericDefine(onCallable, queue);
        while (!queue.isEmpty()) {
            QueueElement element = queue.removeFirst();
            RelatedParameterizedType result = parameterizedType(element.toBeMap, queue,
                    relatedGenericDefines
            );
            element.set(result);
        }
        return relatedGenericDefines;
    }

    /**
     * 不加入缓存, 不从缓存读, if declare key == null
     */
    public RelatedGenericDefine[] relateGenericDefine(
            String declareKey,
            GenericDefine[] genericDefine) throws IOException {
        if (declareKey != null) {
            RelatedGenericDefine[] inCache = relatedGenericDefineCache.get(declareKey);
            if (inCache != null) {
                return inCache;
            }
        }
        LinkedList<QueueElement> queue = new LinkedList<>();
        RelatedGenericDefine[] relatedGenericDefines = relateGenericDefine(genericDefine, queue);
        while (!queue.isEmpty()) {
            QueueElement element = queue.removeFirst();
            RelatedParameterizedType result = parameterizedType(element.toBeMap, queue,
                    relatedGenericDefines
            );
            element.set(result);
        }
        if (declareKey != null) {
            relatedGenericDefineCache.put(declareKey, relatedGenericDefines);
        }
        return relatedGenericDefines;
    }

    private void readDefineThenAddToCache(
            FullIdentifierString declareFromIdentifier,
            String key) throws IOException {
        LinkedList<QueueElement> queue = new LinkedList<>();
        readDefineThenAddToCache(declareFromIdentifier, key, queue);
        while (!queue.isEmpty()) {
            QueueElement element = queue.removeFirst();
            RelatedParameterizedType result = parameterizedTypeExcludeCallableGeneric(element.toBeMap, queue);
            element.set(result);
        }
    }

    private RelatedParameterizedType parameterizedTypeExcludeCallableGeneric(
            ParameterizedType<ReferenceElement> type,
            LinkedList<QueueElement> queue) throws IOException {
        return parameterizedType(type, queue, null);
    }

    private RelatedParameterizedType parameterizedType(
            ParameterizedType<ReferenceElement> type,
            LinkedList<QueueElement> queue,
            RelatedGenericDefine[] onCallable) throws IOException {
        List<Pair<Integer, ReferenceElement>> sourceSequence = type.toSequence();
        List<Pair<Integer, RelationUsing>> resultSequence = new ArrayList<>(sourceSequence.size());
        for (Pair<Integer, ReferenceElement> pair : sourceSequence) {
            ReferenceElement reference = pair.getValue();
            RelationRawType relatedRawType = parameterizedType(reference, queue, onCallable);
            RelationUsing typeUsing = new RelationUsing(reference.getPosition(), relatedRawType);
            resultSequence.add(new Pair<>(pair.getKey(), typeUsing));
        }
        RelatedParameterizedType result = new RelatedParameterizedType(
                typeFromFile, ParameterizedType.toTree(resultSequence));
        if (assignManager != null) {
            // 检查一下
            assignManager.selfConsistent(result);
        }
        return result;
    }


    private RelationRawType parameterizedType(
            ReferenceElement reference,
            LinkedList<QueueElement> queue,
            RelatedGenericDefine[] onCallable) throws IOException {
        switch (reference.getType()) {
            case KEYWORD:
                return RelationCache.dealBasicType(typeFromFile, reference.keyword(), reference.getPosition());
            case CAST_OPERATOR:
            case CONSTRUCTOR:
            case OPERATOR:
                throw new CompileMultipleFileException(typeFromFile, reference.getPosition(), "except a type");
            case GENERIC_IDENTIFIER:
                FullIdentifierString genericFullIdentifier = identifierManager.getIdentifier(reference);
                FullIdentifierString declareFromIdentifier = genericFullIdentifier.getRangeWithPosition(
                        0,
                        genericFullIdentifier.length() - 1
                );
                IdentifierString genericIdentifier = new IdentifierString(
                        genericFullIdentifier.getPosition(),
                        genericFullIdentifier.get(genericFullIdentifier.length() - 1)
                );
                return mapRelatedGenericDefineReference(declareFromIdentifier, genericIdentifier, queue);
            case CALLABLE_GENERIC_IDENTIFIER:
                if (onCallable == null) {
                    throw new CompileMultipleFileException(
                            typeFromFile, reference.getPosition(), "Generics defined on callables are not allowed");
                }
                int referenceOnCallable = reference.getReference();
                RelatedGenericDefine genericDefineOnCallable = onCallable[referenceOnCallable];
                return new RelatedGenericDefineReference(null, null, 0, referenceOnCallable,
                        genericDefineOnCallable.getName(), typeFromFile, true
                );
            case IDENTIFIER:
                return rawTypeRelationshipLoader.load(typeFromFile, identifierManager.getIdentifier(reference));
            case IGNORE:
                throw new CompileMultipleFileException(typeFromFile, reference.getPosition(),
                        "ignore is not allowed here"
                );
            default:
                throw new CompilerException("Unexpected value: " + reference.getType(), new IllegalStateException());
        }
    }

    private RelatedGenericDefineReference mapRelatedGenericDefineReference(
            FullIdentifierString declareFromIdentifier,
            IdentifierString genericIdentifier,
            LinkedList<QueueElement> queue) throws IOException {
        // 1. 检查缓存是否存在, 存在就直接返回
        String key = RelatedGenericDefineCache.geDeclareFromKey(declareFromIdentifier.getFullname());
        RelatedGenericDefineReference fromCache = referFromCache(declareFromIdentifier, key, genericIdentifier);
        if (fromCache != null) {
            return fromCache;
        }
        readDefineThenAddToCache(declareFromIdentifier, key, queue);
        //  将泛型转换后的泛型定义转为关联类型, 返回,
        RelatedGenericDefineReference result = referFromCache(declareFromIdentifier, key, genericIdentifier);
        if (result == null) {
            throw new CompilerException("put to related generic define cache failed");
        }
        return result;
    }

    private void readDefineThenAddToCache(
            FullIdentifierString declareFromIdentifier, String key, LinkedList<QueueElement> queue) throws IOException {
        // 2. 获取泛型定义
        GenericDefine[] genericDefines = genericDefineReader.read(typeFromFile, declareFromIdentifier);
        RelatedGenericDefine[] relatedGenericDefines = relateGenericDefine(genericDefines, queue);
        // 4. 转换后的泛型加入缓存
        relatedGenericDefineCache.put(key, relatedGenericDefines);
    }

    private RelatedGenericDefine[] relateGenericDefine(
            GenericDefine[] genericDefines, LinkedList<QueueElement> queue) throws IOException {
        // 3. 将泛型定义转换
        RelatedGenericDefine[] relatedGenericDefines = new RelatedGenericDefine[genericDefines.length];
        for (int i = 0; i < genericDefines.length; i++) {
            relatedGenericDefines[i] = mapRelatedGenericDefine(genericDefines[i], queue);
        }
        return relatedGenericDefines;
    }

    private RelatedGenericDefineReference referFromCache(
            FullIdentifierString declareFromIdentifier, String key, IdentifierString genericIdentifier) {
        BaseReference inCache = relatedGenericDefineCache.getIndexInDeclare(key, genericIdentifier.getValue());
        if (inCache == null) {
            return null;
        }
        return new RelatedGenericDefineReference(declareFromIdentifier.getFullname(), key, inCache.getBase(),
                inCache.getOffset(), genericIdentifier, typeFromFile, false
        );
    }

    private RelatedGenericDefine mapRelatedGenericDefine(
            GenericDefine define, LinkedList<QueueElement> queue) throws IOException {
        // 解决递归和向前引用
        // 对于向前引用, 在identifier pool-reference阶段已经解决
        IdentifierString name = identifierManager.getGenericIdentifier(define.getName());

        RelatedGenericDefine result = new RelatedGenericDefine(typeFromFile, name, define.isMultiple(), assignManager);
        // RelatedParameterizedType param = parameterizedType(define.getDefaultType());
        queue.addLast(new QueueElement(define.getDefaultType(), result::setDefaultType));
        // RelatedParameterizedType param = parameterizedType(define.getLower());
        queue.addLast(new QueueElement(define.getLower(), result::setLower));
        ParameterizedType<ReferenceElement>[] uppers = define.getUppers();
        int constructorSize = define.getConstructorParameters().size();
        List<RelatedLocalParameterizedType[]> constructors = new ArrayList<>(constructorSize);
        for (LocalParameterizedType[] localParameterizedTypes : define.getConstructorParameters()) {
            RelatedLocalParameterizedType[] relatedLocalParameterizedTypes = new RelatedLocalParameterizedType[localParameterizedTypes.length];
            for (int i = 0; i < localParameterizedTypes.length; i++) {
                LocalParameterizedType localParameterizedType = localParameterizedTypes[i];
                // RelatedParameterizedType param = parameterizedType(localParameterizedType.getType());
                queue.addLast(
                        new QueueElement(localParameterizedType.getType(), relatedLocalParameterizedTypes[i]::setType));
                relatedLocalParameterizedTypes[i] = new RelatedLocalParameterizedType(
                        localParameterizedType.isMarkFinal(), localParameterizedType.isMarkConst());
            }
            constructors.add(relatedLocalParameterizedTypes);
        }
        result.setConstructors(constructors);
        if (uppers.length == 0) {
            result.setAbsentParent();
            result.setAbsentInterfaces();
            return result;
        }
        // 0 for first is interface, 1 for not
        boolean firstIsInterface = isInterface(uppers[0].getRawType());
        int interfacesStart = firstIsInterface ? 0 : 1;
        // RelatedParameterizedType firstOrigin = parameterizedType(uppers[0]);
        if (firstIsInterface) {
            // 需要setParent
            result.setAbsentParent();
        } else {
            queue.addLast(new QueueElement(uppers[0], result::setParent));
        }
        RelatedParameterizedType[] relatedInterfaces = new RelatedParameterizedType[uppers.length - interfacesStart];
        result.setInterfaces(relatedInterfaces);
        for (int i = interfacesStart, j = 0; i < uppers.length; i++, j++) {
            // relatedInterfaces[i] = parameterizedType(uppers[i]);
            int indexOfInterface = j;
            queue.addLast(new QueueElement(uppers[i], related -> result.setInterface(indexOfInterface, related)));
        }
        return result;
    }

    private boolean isInterface(ReferenceElement rawType) throws IOException {

        if (rawType.getType() == ReferenceType.GENERIC_IDENTIFIER) {
            return false;
        }
        RelationRawType load = rawTypeRelationshipLoader.load(typeFromFile, identifierManager.getIdentifier(rawType));
        return load.getType() == StructureType.INTERFACE;
    }
}
