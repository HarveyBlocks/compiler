package org.harvey.compiler.type.transform.test.version2;


import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@Getter
class TempStructure implements RawTypeCanInParameter, SelfConsistentExecutable {
    Level level = Level.TODO;
    String name;
    TempGenericDefine[] genericDefines;
    Parameterized parent;
    Parameterized[] interfaces;
    private Parameterized defaultParameter;

    public TempStructure(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String gds = genericDefines == null ? " " : Arrays.toString(genericDefines);
        String ps;
        if (parent == null) {
            ps = "";
        } else {
            ps = " extends " + parent.toStringValue();
        }
        String is;
        if (interfaces == null || interfaces.length == 0) {
            is = "";
        } else {
            is = " implements " +
                 Arrays.stream(interfaces).map(Parameterized::toStringValue).collect(Collectors.joining(",", "", ""));
        }
        return name + gds + ps + is;
    }

    @Override
    public void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks) {
        if (this.level == Level.FINISH) {
            return;
        }
        Level levelUsing = Level.decide(outerLevel, this.level);
        for (TempGenericDefine genericDefine : genericDefines) {
            genericDefine.selfConsistent(levelUsing, toBeChecks);
        }
        if (this.parent != null) {
            this.parent.selfConsistent(levelUsing, toBeChecks);
        }
        if (this.interfaces != null) {
            for (Parameterized each : this.interfaces) {
                each.selfConsistent(levelUsing, toBeChecks);
            }
        }
        this.level = this.level.up(levelUsing);

    }

    @Deprecated
    public Parameterized getDefaultParameter() {
        return defaultParameter;
    }

    public Parameterized defaultParameter() {
        if (defaultParameter != null) {
            defaultParameter.level = this.level;
            return defaultParameter;
        }
        synchronized (this) {
            if (defaultParameter != null) {
                defaultParameter.level = this.level;
                return defaultParameter;
            }
            Parameterized parameterized = new Parameterized(this);
            parameterized.level = this.level;
            for (int i = 0; i < this.genericDefines.length; i++) {
                parameterized.addChild(new GenericDefineReference(i, this));
            }
            parameterized.level = this.level;
            return defaultParameter = parameterized;
        }
    }


    @Override
    public String getNameWithLevel() {
        return getName() + "[" + level + "]";
    }
}
