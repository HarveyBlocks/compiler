package org.harvey.compiler.type.transform.test.version5;

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
    GenericDefiner definer;


    public GenericDefineReference(int index, GenericDefiner definer) {
        this.index = index;
        this.definer = definer;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Level getLevel() {
        return getGeneric().level;
    }

    @Override
    public String getName() {
        return definer.getName() + "." + getGeneric().name;
    }

    @Override
    public String getNameWithLevel() {
        return getName() + "[" + getGeneric().level + "]";
    }

    public TempGenericDefine getGeneric() {
        return definer.getGeneric(index);
    }

}
