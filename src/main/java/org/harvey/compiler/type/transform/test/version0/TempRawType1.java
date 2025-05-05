package org.harvey.compiler.type.transform.test.version0;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.tree.DefaultMultipleTree;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.type.transform.RawTypeAssignable;

import java.util.Arrays;
import java.util.List;

interface constant {

    int TODO = 0;
    // 表示已经完成了RawType的检查
    int RAW_TYPE = 1;
    int PARAMETER = 2;
    int FINISH = 3;
}

interface RawTypeCanInParameter {
    String getName();
}

interface Assignable {
    void assign(Parameterized from);

    void assign(GenericDefine from);
}

@AllArgsConstructor
class AssignableParameterized implements Assignable {
    final Parameterized to;

    @Override
    public void assign(Parameterized from) {
        System.out.println("assign");
    }

    @Override
    public void assign(GenericDefine from) {
        if (from == null) {
            throw new CompilerException("can not assign");
        } else if (from.upper == null) {
            throw new CompilerException("can not assign");
        }
        assign(from.upper);
    }
}

@AllArgsConstructor
class AssignableGenericDefine implements Assignable {
    final GenericDefine to;

    @Override
    public void assign(Parameterized from) {
        if (to.upper == null) {
            return;
        }
        AssignableFactory.create(to.upper).assign(from);
    }

    @Override
    public void assign(GenericDefine from) {
        if (to.upper == null) {
            return;
        }
        if (from == null) {
            throw new CompilerException("can not assign");
        } else if (from.upper == null) {
            throw new CompilerException("can not assign");
        }
        AssignableFactory.create(to.upper).assign(from.upper);
    }
}

class AssignableFactory {
    public static Assignable create(Parameterized parameterized) {
        RawTypeCanInParameter value = parameterized.getType().getValue();
        if (value instanceof GenericDefineReference) {
            if (parameterized.childSize() != 0) {
                throw new CompilerException("Generic define 不能有 parameter");
            }
            return new AssignableGenericDefine(((GenericDefineReference) value).getGeneric());
        } else if (value instanceof RawTypeAssignable) {
            return new AssignableParameterized(parameterized);
        } else {
            throw new CompilerException("Unknown type");
        }
    }
}

@Getter
class TempRawType implements RawTypeCanInParameter {
    int status = constant.TODO;
    String name;
    GenericDefine[] genericDefines;
    Parameterized parent;

    public TempRawType(String name) {
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
        return name + gds + ps;
    }

    public void selfConsistent(int outerStatus) {
        if (status == constant.FINISH) {
            return;
        }
        int status = Math.min(outerStatus, this.status);
        for (GenericDefine genericDefine : this.genericDefines) {
            genericDefine.selfConsistent(status);
        }
        if (this.parent != null) {
            this.parent.selfConsistent(status);
            AssignableFactory.create(this.parent).assign(this.defaultParameter());
        }
        levelUp(status);
    }

    private Parameterized defaultParameter() {
        Parameterized parameterized = new Parameterized(this);
        parameterized.status = this.status;
        for (int i = 0; i < this.genericDefines.length; i++) {
            parameterized.addChild(new GenericDefineReference(i, this));
        }
        return parameterized;
    }

    private void levelUp(int usingStatus) {
        if (this.status == constant.FINISH) {
            return;
        }
        if (usingStatus < this.status) {
            return;
        }
        this.status++;
        if (usingStatus > this.status) {
            selfConsistent(usingStatus);
        }
    }
}

class GenericDefine {
    int status = constant.TODO;
    String name;
    Parameterized upper;

    public GenericDefine(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String us;
        if (upper == null) {
            us = "";
        } else {
            us = " extends " + upper.toStringValue();
        }
        return name + us;
    }

    public void selfConsistent(int outerStatus) {
        if (this.upper == null) {
            status = constant.FINISH;
            return;
        }
        if (status == constant.FINISH) {
            return;
        }
        int status = Math.min(outerStatus, this.status);
        this.upper.selfConsistent(status);
        levelUp(status);
    }

    private void levelUp(int usingStatus) {
        if (this.status == constant.FINISH) {
            return;
        }
        if (usingStatus < this.status) {
            return;
        }
        this.status++;
        if (usingStatus > this.status) {
            selfConsistent(usingStatus);
        }
    }
}

@Getter
class GenericDefineReference implements RawTypeCanInParameter {
    int index;
    TempRawType declare;
    int status = constant.TODO;

    public GenericDefineReference(int index, TempRawType declare) {
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

    public GenericDefine getGeneric() {
        return declare.genericDefines[index];
    }
}

@Getter
class Parameterized {
    int status = constant.TODO;
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
        parameterized.status = status;
        return parameterized;
    }

    public List<String> toStringList() {
        return type.toStringList(",", "[", "]", RawTypeCanInParameter::getName);
    }

    public String toStringValue() {
        return String.join("", toStringList());
    }

    public void selfConsistent(int outerStatus) {
        if (status == constant.FINISH) {
            return;
        }

        // TODO
        int status = Math.min(outerStatus, this.status);
        RawTypeCanInParameter rawType = type.getValue();
        if (rawType instanceof GenericDefineReference) {
            if (!type.getChildren().isEmpty()) {
                throw new CompilerException("generic define can not have parameters");
            }
            // 正确
            ((GenericDefineReference) rawType).getGeneric().selfConsistent(status);
        } else if (rawType instanceof TempRawType) {
            TempRawType tempRawType = (TempRawType) rawType;
            for (int i = 0, end = type.childrenSize(); i < end; i++) {
                Parameterized parameterizedChild = getChild(i);
                // 判断自洽
                parameterizedChild.selfConsistent(status);
                GenericDefine genericDefine = tempRawType.genericDefines[i];
                if (genericDefine.upper != null) {
                    AssignableFactory.create(genericDefine.upper).assign(parameterizedChild);
                }
                genericDefine.selfConsistent(status);
            }
            tempRawType.selfConsistent(status);
        } else {
            throw new CompilerException("unknown type");
        }
        levelUp(status);

    }

    private void levelUp(int usingStatus) {
        if (this.status == constant.FINISH) {
            return;
        }
        if (usingStatus < this.status) {
            return;
        }
        this.status++;
        if (usingStatus > this.status) {
            selfConsistent(usingStatus);
        }
    }

    public int childSize() {
        return type.childrenSize();
    }

    private void assign(RawTypeCanInParameter fromRaw, RawTypeCanInParameter toRaw) {

    }

    private RawTypeCanInParameter getRawType() {
        return this.type.getValue();
    }
}

class Deal {
    static TempRawType upper = new TempRawType("Upper");
    static TempRawType exampleClass1 = new TempRawType("ExampleClass1");
    static TempRawType exampleClass2 = new TempRawType("ExampleClass2");
    static TempRawType object = new TempRawType("Object");

    static void buildRelation() {
        // Upper[T0, T1 extends ExampleClass1[Upper.T0], T2 extends ExampleClass2[Upper.T0]]
        // ExampleClass1[T] extends Upper[ExampleClass1.T,ExampleClass1[ExampleClass1.T],ExampleClass2[ExampleClass1.T]]
        // ExampleClass2[T] extends Upper[ExampleClass2.T,ExampleClass1[ExampleClass2.T],ExampleClass2[ExampleClass2.T]]
        upper.genericDefines = new GenericDefine[3];
        upper.genericDefines[0] = new GenericDefine("T0");
        upper.genericDefines[1] = new GenericDefine("T1");
        upper.genericDefines[2] = new GenericDefine("T2");
        upper.genericDefines[0].upper = null;
        upper.genericDefines[1].upper = new Parameterized(exampleClass1);
        upper.genericDefines[1].upper.addChild(new GenericDefineReference(0, upper));
        upper.genericDefines[2].upper = new Parameterized(exampleClass2);
        upper.genericDefines[2].upper.addChild(new GenericDefineReference(0, upper));
        //
        exampleClass1.genericDefines = new GenericDefine[1];
        exampleClass1.genericDefines[0] = new GenericDefine("T");
        exampleClass1.parent = new Parameterized(upper);
        exampleClass1.parent.addChild(new GenericDefineReference(0, exampleClass1));
        exampleClass1.parent.addChild(exampleClass1);
        exampleClass1.parent.getChild(1).addChild(new GenericDefineReference(0, exampleClass1));
        exampleClass1.parent.addChild(exampleClass2);
        exampleClass1.parent.getChild(2).addChild(new GenericDefineReference(0, exampleClass1));
        //
        exampleClass2.genericDefines = new GenericDefine[1];
        exampleClass2.genericDefines[0] = new GenericDefine("T");
        exampleClass2.parent = new Parameterized(upper);
        exampleClass2.parent.addChild(new GenericDefineReference(0, exampleClass2));
        exampleClass2.parent.addChild(exampleClass2);
        exampleClass2.parent.getChild(1).addChild(new GenericDefineReference(0, exampleClass2));
        exampleClass2.parent.addChild(exampleClass2);
        exampleClass2.parent.getChild(2).addChild(new GenericDefineReference(0, exampleClass2));

    }

    public static void main(String[] args) {
        buildRelation();
        System.out.println(upper);
        System.out.println(exampleClass1);
        System.out.println(exampleClass2);
        upper.selfConsistent(constant.TODO);
        upper.selfConsistent(constant.RAW_TYPE);
        upper.selfConsistent(constant.PARAMETER);
        upper.selfConsistent(constant.FINISH);
    }

}