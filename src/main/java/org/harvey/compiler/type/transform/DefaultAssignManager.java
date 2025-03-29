package org.harvey.compiler.type.transform;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.execute.expression.KeywordString;
import org.harvey.compiler.type.generic.relate.RelatedParameterizedTypeBuilderFactory;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.RawTypeRelationshipLoader;
import org.harvey.compiler.type.raw.RelationUsing;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 14:36
 */
@AllArgsConstructor
public class DefaultAssignManager implements AssignManager {
    public static final Map<Keyword, Integer> NUMBER_PRIORITY;

    static {
        NUMBER_PRIORITY = Map.of(Keyword.INT8, 0, Keyword.UINT8, 1, Keyword.INT16, 2, Keyword.UINT16, 3, Keyword.INT32,
                4, Keyword.UINT32, 5, Keyword.INT64, 6, Keyword.UINT64, 7, Keyword.FLOAT32, 8, Keyword.FLOAT64, 9
        );
    }

    private final RawTypeRelationshipLoader rawTypeRelationshipLoader;
    private final RelatedParameterizedTypeBuilderFactory relatedParameterizedTypeBuilderFactory;

    /**
     * <p>To value = new From();</p>
     * <p>From <= To</p>
     * 任意signed->unsigned
     * int8<int16<int32<int64<float32<float64
     */
    public static void assignable(KeywordString to, KeywordString from, File fromFile) {
        if (to.getKeyword() == null || !NUMBER_PRIORITY.containsKey(to.getKeyword())) {
            throw new CompileMultipleFileException(fromFile, from.getPosition(), "Unknown type cast");
        }
        if (from.getKeyword() == null || !NUMBER_PRIORITY.containsKey(from.getKeyword())) {
            throw new CompileMultipleFileException(fromFile, from.getPosition(), "Unknown type cast");
        }
        if (to.getKeyword() == from.getKeyword()) {
            return;
        }

        if (NUMBER_PRIORITY.get(from.getKeyword()) <= NUMBER_PRIORITY.get(to.getKeyword())) {
            return;
        }
        throw CanNotAssignUtil.canNotAssign(fromFile, from.getPosition(),
                "illegal cast from " + from.getKeyword().getValue() + " to " + to.getKeyword().getValue()
        );
    }

    @Override
    public void selfConsistent(RelatedGenericDefine define) {
        //
    }


    /**
     * P T S C, short for Parameterized Type Self Consistent
     */
    static class PTSCTask extends Pair<RelationUsing, ParameterizedType<RelationUsing>[]> {
        public PTSCTask(RelationUsing rawType, ParameterizedType<RelationUsing>[] children) {
            super(rawType, children);
        }
    }

    @Override
    public void selfConsistent(RelatedParameterizedType type) throws IOException {
        Stack<PTSCTask> taskStack = pushPTSCTask(type);
        while (!taskStack.empty()) {
            PTSCTask task = taskStack.pop();
            RelationUsing rawType = task.getKey();
            ParameterizedType<RelationUsing>[] genericArguments = task.getValue();

            // 1. 获取genericMessage的reference
            RelatedGenericDefine[] defineReferences = relatedParameterizedTypeBuilderFactory.create(
                            rawType.getRawType().getDeclareIdentifier(), rawType.getRawType().getFromFile())
                    .genericDefine(rawType.getRawType().getDeclareIdentifier());
            assignable(genericArguments, defineReferences);
        }
    }

    public void assignable(
            ParameterizedType<RelationUsing>[] typeArgumentList, RelatedGenericDefine[] defineParamList) {
        // 2. 获取索引信息
        int defaultStartIndex = ArrayUtil.indexOf(defineParamList, r -> r.getDefaultType().isNull());
        boolean lastIsMultiply = ArrayUtil.indexOf(
                defineParamList, RelatedGenericDefine::isMultiple, defaultStartIndex + 1) > 0;
        // 3. [0,defaultStartIndex) 严格匹配
        int endOfDefault = defineParamList.length - (lastIsMultiply ? 1 : 0);
        int endOfStrict = defaultStartIndex < 0 ? endOfDefault : defaultStartIndex;
        int indexOfArgument = 0;
        for (int i = 0; i < endOfStrict; i++, indexOfArgument++) {

        }
        // 4. [defaultStartIndex,defineReferences.length-(lastIsMultiply?1:0)]默认参数匹配
        for (int i = endOfStrict; i < endOfDefault; i++) {

        }
        // 5. lastIsMultiply?不定参数匹配:pass
        if (!lastIsMultiply) {
            return;
        }
        for (; indexOfArgument < typeArgumentList.length; indexOfArgument++) {

        }

    }

    private static Stack<PTSCTask> pushPTSCTask(
            RelatedParameterizedType type) {
        Stack<PTSCTask> taskStack = new Stack<>();
        LinkedList<ParameterizedType<RelationUsing>> rawTypeQueue = new LinkedList<>();
        rawTypeQueue.addLast(type.getValue());
        while (!rawTypeQueue.isEmpty()) {
            ParameterizedType<RelationUsing> first = rawTypeQueue.removeFirst();
            RelationUsing rawType = first.getRawType();
            ParameterizedType<RelationUsing>[] children = first.getChildren();
            taskStack.push(new PTSCTask(rawType, children));
            for (ParameterizedType<RelationUsing> child : children) {
                rawTypeQueue.addLast(child);
            }
        }
        return taskStack;
    }

    @Override
    public void assignable(RelatedGenericDefine to, RelatedGenericDefine from) {

    }

    @Override
    public void assignable(
            RelatedParameterizedType to, RelatedGenericDefine from) {

    }

    @Override
    public void assignable(
            RelatedGenericDefine to, RelatedParameterizedType from) {

    }

    @Override
    public void assignable(
            RelatedParameterizedType to, RelatedParameterizedType from) {

    }


}
