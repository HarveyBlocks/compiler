package org.harvey.compiler.type.transform.test.version3;

import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 23:07
 */
@Getter
class GenericDefineReference implements RawTypeCanInParameter {
    int index;
    TempStructure declare;


    public GenericDefineReference(int index, TempStructure declare) {
        this.index = index;
        this.declare = declare;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        return declare.name + "." + getGeneric().name;
    }

    @Override
    public String getNameWithLevel() {
        return getName() + "[" + getGeneric().level + "]";
    }

    public TempGenericDefine getGeneric() {
        return declare.genericDefines[index];
    }
}
