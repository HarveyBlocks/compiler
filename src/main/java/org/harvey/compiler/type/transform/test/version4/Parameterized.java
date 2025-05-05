package org.harvey.compiler.type.transform.test.version4;

import lombok.Getter;
import org.harvey.compiler.common.tree.DefaultMultipleTree;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 23:08
 */


@SuppressWarnings("DuplicatedCode")
@Getter
class Parameterized implements SelfConsistentExecutable {
    Level level = Level.TODO;
    MultipleTree<RawTypeCanInParameter> type;

    public Parameterized(
            MultipleTree<RawTypeCanInParameter> type) {
        this.type = type;
    }

    public Parameterized(RawTypeCanInParameter type) {
        this.type = new DefaultMultipleTree<>(type);
    }

    void addChild(RawTypeCanInParameter add) {
        type.addChild(new DefaultMultipleTree<>(add));
    }

    Parameterized getChild(int i) {
        Parameterized parameterized = new Parameterized(type.getChild(i));
        parameterized.level = level;
        return parameterized;
    }

    public List<String> toStringList() {
        return type.toStringList(",", "[", "]", RawTypeCanInParameter::getNameWithLevel);
    }

    public String toStringValue() {
        return String.join("", toStringList());
    }

    @Override
    public void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks) {
        if (this.level == Level.FINISH) {
            return;
        }
        Level levelUsing = Level.decide(outerLevel, this.level);

        RawTypeCanInParameter rawType = getRawType();
        if (rawType instanceof GenericDefineReference) {
            if (childSize() != 0) {
                throw new CompilerException("generic can not have parameters");
            }
            /*? 一定是对的
            TempGenericDefine genericDefine = ((GenericDefineReference) rawType).getGeneric();
            if (genericDefine.parent != null) {
                AssignableFactory.create(genericDefine.parent).assign(this);
            }
            toBeChecks.addLast(new ToBeCheck(this, genericDefine));*/
        } else if (rawType instanceof TempStructure) {
            TempStructure structureType = (TempStructure) rawType;
            // 这是不好的
            //
            // 知道是RawType, 获取RawType的GenericDefine

            // 在构建上, 有:
            // class A[T extends List[Object]]{}
            // class A[T extends List[Object]]{}
            // A[List[Object]] 可以
            // A[ArrayList[Object]] 可以
            // A[List[String]] 不可以
            // A[ArrayList[String]] 不可以
            //
            for (int i = 0; i < structureType.genericDefines.length; i++) {
                // 由于Assignable是比较广泛的匹配匹配
                // 所以需要另一种匹配
                //
                Parameterized child = this.getChild(i);
                AssignableFactory.create(structureType.genericDefines[i]).assign(child);
            }

            toBeChecks.addLast(new ToBeCheck(this, structureType));
        } else {
            throw new CompilerException("Unknown type: " + rawType.getClass());
        }


        this.level = this.level.up(levelUsing);
    }

    public int childSize() {
        return type.childrenSize();
    }

    public RawTypeCanInParameter getRawType() {
        return this.type.getValue();
    }
}