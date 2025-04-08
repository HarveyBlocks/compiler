package org.harvey.compiler.type.basic.test3;

import lombok.Getter;

/**
 * TODO  
 *
 * @date 2025-03-30 23:07
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
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
