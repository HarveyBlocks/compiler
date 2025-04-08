package org.harvey.compiler.type.basic.test5;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;

/**
 * TODO  
 *
 * @date 2025-04-01 14:29
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@SuppressWarnings("DuplicatedCode")
class ParameterizedMapper {
    /**
     *
     * @param parameterized 把lower 拉到和upper同级
     * @param pathToUpper empty 表示from 就是 to, -1 表示parent, index 表示interfaces中的索引, last是to, first是from
     * @param levelUsing
     * @return parameterized转成了Upper, mappedToUpper
     */
    public static Parameterized mapForStructure(
            Parameterized parameterized, LinkedList<Pair<TempStructure, Integer>> pathToUpper, Level levelUsing) {
        // 初始化
        // 第一个的structure没有用
        if (pathToUpper.isEmpty()) {
            throw new CompilerException("不可能为null");
        }
        Level level = Level.decide(levelUsing, parameterized.level);
        TempStructure lowerDefine = pathToUpper.removeFirst().getKey();
        MultipleTree<RawTypeCanInParameter> source = parameterized.type;
        // parameter的param传给lower define, 换掉upper using中的lower.generic_define
        while (!pathToUpper.isEmpty()) {
            Pair<TempStructure, Integer> top = pathToUpper.removeFirst();
            TempStructure upperDefine = top.getKey();
            Integer upperReference = top.getValue();
            Parameterized upperUsing = getUpper(lowerDefine, upperReference);
            level = Level.decide(level, upperUsing.level);
            MultipleTree<RawTypeCanInParameter> upperUsingClone = upperUsing.type.cloneThis();
            injectParameterized(upperUsingClone, lowerDefine, source);
            lowerDefine = upperDefine;
            source = upperUsingClone;
        }
        source.setReadOnly(true);
        Parameterized result = new Parameterized(source);
        result.level = level;
        return result;
    }

    private static Parameterized getUpper(TempStructure structure, Integer reference) {
        return StructureUpperPathFideIterator.upper(structure, reference);
    }

    /**
     * @param mappedParameterized 会被修改结构, 所以希望是clone, 其中有genericDefiner可提供的generic,
     * @param genericUsing 给出generic define 上的值
     * @param genericDefiner 给出 generic define的定义
     * @return param mappedParameterized, read only, 可放心使用
     */
    public static MultipleTree<RawTypeCanInParameter> injectParameterized(
            MultipleTree<RawTypeCanInParameter> mappedParameterized,
            GenericDefiner genericDefiner,
            MultipleTree<RawTypeCanInParameter> genericUsing) {
        if (genericUsing.getValue() != genericDefiner) {
            throw new CompilerException("generic definer should be match with source, " +
                                        "for generic define must provide generic define in mapped parameterized in source");
        }
        mappedParameterized.forEach((brothers, indexOfThisNode) -> {
            // 有一个坏处, brother加辈分, 就会无限循环下去
            MultipleTree<RawTypeCanInParameter> node = brothers.get(indexOfThisNode);
            String nodeName = node.getValue().getName();
            // node in generic define
            TempGenericDefine[] genericDefines = genericDefiner.genericDefines();
            int indexOfGenericDefine = ArrayUtil.indexOf(genericDefines, gd -> {
                // Name 的 值的比较是不好的,
                return Objects.equals(gd.name, nodeName);
            });
            if (indexOfGenericDefine == -1) {
                return;
            }
            MultipleTree<RawTypeCanInParameter> childToMap = genericUsing.getChild(indexOfGenericDefine);
            brothers.set(indexOfThisNode, childToMap.cloneThis());
        });
        return mappedParameterized;
    }

    /**
     * @param path                   都是没有被映射的TempAlias, 底部的是alias, top的是origin, 需要为所有人设置aliasMapped
     * @param endMappedParameterized 一个structure的parameter,
     * @param using                  level
     * @return front是alias方向, tail是origin方向
     */
    public static LinkedList<Parameterized> mapForAlias(
            Stack<TempAlias> path, Parameterized endMappedParameterized, Level using) {
        LinkedList<Parameterized> result = new LinkedList<>();
        while (!path.empty()) {
            TempAlias top = path.pop();
            Parameterized originSource = top.origin;
            endMappedParameterized.type.forEach((brothers, indexOfThisNode) -> {
                // 有一个坏处, brother加辈分, 就会无限循环下去
                MultipleTree<RawTypeCanInParameter> node = brothers.get(indexOfThisNode);
                String nodeName = node.getValue().getName();
                // node in generic define
                int indexOfGenericDefine = ArrayUtil.indexOf(top.genericDefines, gd -> {
                    // Name 的 值的比较是不好的,
                    return Objects.equals(gd.name, nodeName);
                });
                if (indexOfGenericDefine == -1) {
                    return;
                }
                Parameterized childToMap = originSource.getChild(indexOfGenericDefine);
                brothers.set(indexOfThisNode, childToMap.type.cloneThis());
            });
            MultipleTree<RawTypeCanInParameter> mappedTree = endMappedParameterized.type.cloneThis();
            mappedTree.setReadOnly(true);
            Parameterized mapped = new Parameterized(mappedTree);
            mapped.level = endMappedParameterized.level;
            result.addFirst(mapped);

        }
        return result;
    }
}
