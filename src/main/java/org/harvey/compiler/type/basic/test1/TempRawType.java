package org.harvey.compiler.type.basic.test1;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.PairList;
import org.harvey.compiler.common.tree.DefaultMultipleTree;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.*;

enum Level {
    TODO,/*
    RAW_TYPE,
    PARAMETER,*/
    FINISH;

    public static Level decide(Level l1, Level l2) {
        return Level.values()[Math.min(l1.ordinal(), l2.ordinal())];
    }

    public Level up(Level levelUsing) {
        if (this == Level.FINISH) {
            return this;
        }
        if (levelUsing.ordinal() < this.ordinal()) {
            return this;
        }
        return Level.values()[this.ordinal() + 1];
    }
}

interface RawTypeCanInParameter {
    String getName();

    String getNameWithLevel();
}

interface Assignable {
    void assign(Parameterized from);

    void assign(GenericDefine from);
}

interface SelfConsistentExecutable {
    void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks);

    Level getLevel();
}

@AllArgsConstructor
class ToBeCheck {
    final SelfConsistentExecutable earnFromStatus;
    final SelfConsistentExecutable earnTarget;
}

@SuppressWarnings("DuplicatedCode")
@AllArgsConstructor
class AssignableParameterized implements Assignable {
    final Parameterized to;


    @Override
    public void assign(Parameterized from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw new AnalysisException(SourcePosition.UNKNOWN, "can not 匹配 for from 是 null");
        }
        if (from.getRawType() != to.getRawType()) {
            System.out.printf("[%02d]assign: from '%s' to '%s'%n", Thread.currentThread().getStackTrace().length,
                    from.toStringValue(), to.toStringValue()
            );
        }


        // class A[T0,T1 extends List[? extends T0],T2 extends ArrayList[T0]]{}{}
        // class A[T0,T0_t extends T0,T1 extends List[T0_t],T2 extends ArrayList[T0]]{}{}

        // class A[T0,T1,T2]{}
        // class B[T0,T1] extends A[T0,List[T1],Map[T0,List[T1]]]{}
        // class C[T] extends B[T,List[T]]{}
        // from: C[String] c =  new();
        // to: A[String,String] a = b;//可以的
        // 1. 存在 from 到 to 的路径 C->B->A
        // 2. 不存在->不assign
        // 2. 存在->映射
        //      以此为例 B 到 A 有映射, B.T0 对应 C.T, B的Parent的所有
        //
        if (to.getRawType() instanceof GenericDefineReference) {
            // 循环(逻辑), 递归(实际), 一直引导到type是TempRawType为止
            throw new CompilerException("由factory制造的, 就不应该有这种问题");
        } else if (from.getRawType() instanceof GenericDefineReference) {
            throw new CompilerException("参数错误, 这个参数不应该在这里出现");
        } else if ((to.getRawType() instanceof TempRawType) && (from.getRawType() instanceof TempRawType)) {
            Stack<TempRawType> path = findPath((TempRawType) from.getRawType(), (TempRawType) to.getRawType());
            Level levelUsing = Level.decide(from.level, to.level);
            // to.raw 到 from.raw 的 一条 path 出来了
            Parameterized mappedParameterized = map(path, levelUsing);
            levelUsing = mappedParameterized.level;
            // 然后就是各个from.using.param->from.define.param严格匹配
            /*switch (levelUsing) {
                case TODO:
                    break;
                case RAW_TYPE:
                    break;
                case PARAMETER:
                    break;
                case FINISH:
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + levelUsing);
            }*/
            PairList<Integer, RawTypeCanInParameter> fromSequence = from.getType().toSequence();
            PairList<Integer, RawTypeCanInParameter> mappedSequence = mappedParameterized.getType().toSequence();
            if (mappedSequence.size() != fromSequence.size()) {
                throw new AnalysisException(
                        SourcePosition.UNKNOWN,
                        String.format("不匹配: from %s to %s. msg: from sequence'%s', mapped sequence '%s'",
                                from.toStringValue(), to.toStringValue(), fromSequence, mappedSequence
                        )
                );
            }
            for (int i = 0; i < mappedSequence.size(); i++) {
                if (Objects.equals(mappedSequence.getKey(i), fromSequence.getKey(i)) &&
                    typeSame(mappedSequence.getValue(i), fromSequence.getValue(i))) {
                    continue;
                }
                throw new AnalysisException(
                        SourcePosition.UNKNOWN, String.format(
                        "不匹配: from %s to %sat %d. msg: from sequence'%s', mapped sequence '%s'",
                        from.toStringValue(), to.toStringValue(), i, fromSequence, mappedSequence
                ));
            }
        } else {
            throw new CompilerException("Unknown type");
        }


    }

    private boolean typeSame(RawTypeCanInParameter to, RawTypeCanInParameter from) {
        Class<? extends RawTypeCanInParameter> sameClass = to.getClass();
        if (sameClass != from.getClass()) {
            return false;
        }
        if (to instanceof TempRawType) {
            return to == from;
        }
        if (to instanceof GenericDefineReference) {
            GenericDefine toGeneric = ((GenericDefineReference) to).getGeneric();
            GenericDefine fromGeneric = ((GenericDefineReference) from).getGeneric();
            try {
                AssignableFactory.create(toGeneric).assign(fromGeneric);
                return true;
            } catch (RuntimeException e) { // TODO 改进
                return false;
            }
        }
        throw new CompilerException("Unknown type");
    }

    private Parameterized map(Stack<TempRawType> path, Level levelUsing) {
        // path不可能为空
        if (path.empty()) {
            throw new CompilerException("不可能为空");
        }
        if (path.size() == 1) {
            return path.pop().defaultParameter();
        }
        TempRawType originCur = path.pop();
        // mappedParameterized. raw 一定是from
        MultipleTree<RawTypeCanInParameter> mappedParameterized = null;
        Level level = Level.decide(levelUsing, originCur.level);
        while (!path.empty()) {
            TempRawType top = path.pop();
            level = Level.decide(level, top.level);
            if (top.parent == null) {
                throw new CompilerException("if null, then can not find path");
            } else if (top.parent.getRawType() == originCur) {
                originCur = (TempRawType) top.parent.getRawType();
                if (mappedParameterized == null) {
                    mappedParameterized = top.parent.getType().cloneThis();
                    continue;
                }
                // mappedParameterized 遍历, 找到GenericDefine in originCur.GenericDefines的, 用 top 的 parent的Parameterized替换
                MultipleTree<RawTypeCanInParameter> source = top.parent.getType();
                TempRawType finalOriginCur = originCur;
                rebuildParameterized(mappedParameterized, finalOriginCur, source);
            } else {
                throw new CompilerException("找不到匹配路径的父类");
            }
            // origin 的 所有 涉及 top 的 generic define 的统统
        }
        // TODO 是比较合适的吗?
        Parameterized parameterized = new Parameterized(mappedParameterized);
        parameterized.level = level;
        return parameterized;
    }

    private static void rebuildParameterized(
            MultipleTree<RawTypeCanInParameter> mappedParameterized,
            TempRawType finalOriginCur,
            MultipleTree<RawTypeCanInParameter> source) {
        mappedParameterized.forEach((brothers, indexOfThisNode) -> {
            // 有一个坏处, brother加辈分, 就会无限循环下去
            MultipleTree<RawTypeCanInParameter> node = brothers.get(indexOfThisNode);
            int indexOfGenericDefine = ArrayUtil.indexOf(finalOriginCur.genericDefines, gd -> {
                // Name 的 值的比较是不好的,
                return Objects.equals(gd.name, node.getValue().getName());
            });
            if (indexOfGenericDefine == -1) {
                return;
            }
            MultipleTree<RawTypeCanInParameter> childToMap = source.getChild(indexOfGenericDefine);
            brothers.set(indexOfThisNode, childToMap);
        });
    }

    /**
     *
     * @return 不会有null 在 stack里
     */
    private Stack<TempRawType> findPath(TempRawType fromRawType, TempRawType toRawType) {
        Stack<TempRawType> path = new Stack<>();
        // 不是GenericDefine的情况? 有吗?
        TempRawType cur = fromRawType;
        path.push(cur);
        while (cur != toRawType) {
            if (cur.parent == null) {
                throw new AnalysisException(SourcePosition.UNKNOWN, "没有继承关系");
            }
            RawTypeCanInParameter parentRawType = cur.parent.getRawType();
            if (parentRawType instanceof GenericDefineReference) {
                throw new CompilerException("不允许 generic define 在 parent 作为 raw type");
            } else if (parentRawType instanceof TempRawType) {
                cur = (TempRawType) parentRawType;
                path.push(cur);
            } else {
                throw new CompilerException("Unknown type");
            }
        }
        return path;
    }

    @Override
    public void assign(GenericDefine from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw new CompilerException("can not assign");
        } else if (from.upper == null) {
            throw new CompilerException("can not assign");
        }
        assign(from.upper);
    }
}

@SuppressWarnings("DuplicatedCode")
@AllArgsConstructor
class AssignableGenericDefine implements Assignable {
    final GenericDefine to;

    @Override
    public void assign(Parameterized from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (to.upper == null) {
            return;
        }
        AssignableFactory.create(to.upper).assign(from);
    }

    @Override
    public void assign(GenericDefine from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
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

@SuppressWarnings("DuplicatedCode")
class AssignableFactory {
    public static Assignable create(Parameterized parameterized) {
        RawTypeCanInParameter value = parameterized.getRawType();
        if (value instanceof GenericDefineReference) {
            if (parameterized.childSize() != 0) {
                throw new CompilerException("Generic define 不能有 parameter");
            }
            return create((GenericDefineReference) value);
        } else if (value instanceof TempRawType) {
            return new AssignableParameterized(parameterized);
        } else {
            throw new CompilerException("Unknown type: " + value.getClass());
        }
    }

    public static AssignableGenericDefine create(GenericDefineReference value) {
        return create(value.getGeneric());
    }

    public static AssignableGenericDefine create(GenericDefine genericDefine) {
        return new AssignableGenericDefine(genericDefine);
    }

}

@SuppressWarnings("DuplicatedCode")
@Getter
class TempRawType implements RawTypeCanInParameter, SelfConsistentExecutable {
    Level level = Level.TODO;
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

    @Override
    public void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks) {
        if (this.level == Level.FINISH) {
            return;
        }
        Level levelUsing = Level.decide(outerLevel, this.level);
        for (GenericDefine genericDefine : genericDefines) {
            genericDefine.selfConsistent(levelUsing, toBeChecks);
        }
        if (this.parent != null) {
            this.parent.selfConsistent(levelUsing, toBeChecks);
        }
        this.level = this.level.up(levelUsing);

    }

    private Parameterized defaultParameter;

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

@Getter
@SuppressWarnings("DuplicatedCode")
class GenericDefine implements SelfConsistentExecutable {
    Level level = Level.TODO;
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

    @Override
    public void selfConsistent(Level outerLevel, LinkedList<ToBeCheck> toBeChecks) {
        if (this.level == Level.FINISH) {
            return;
        }
        if (upper == null) {
            this.level = Level.FINISH;
            return;
        }
        Level levelUsing = Level.decide(outerLevel, this.level);
        this.upper.selfConsistent(levelUsing, toBeChecks);
        this.level = this.level.up(levelUsing);

    }


}

@Getter
class GenericDefineReference implements RawTypeCanInParameter {
    int index;
    TempRawType declare;


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

    @Override
    public String getNameWithLevel() {
        return getName() + "[" + getGeneric().level + "]";
    }

    public GenericDefine getGeneric() {
        return declare.genericDefines[index];
    }
}

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
            GenericDefine genericDefine = ((GenericDefineReference) rawType).getGeneric();
            if (genericDefine.upper != null) {
                AssignableFactory.create(genericDefine.upper).assign(this);
            }
            toBeChecks.addLast(new ToBeCheck(this, genericDefine));
        } else if (rawType instanceof TempRawType) {
            TempRawType structureType = (TempRawType) rawType;
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
// 1. 基础 1 upper 1 parents 多 generic define
// 1.1 工程量变大, 多个文件拆分一下
// 2. 增加 多 uppers 和 多 parents
// 2.1 多uppers , generic define 的检查 和 assign 改变
// 2.1 多parents, raw type检查改变, 查找路径的方法改变, 要'选择'路径了;
// 3. 增加lower
// 3.1 generic define 的检查 和 assign 改变
// 4. 增加 alias
// 5. 增加 default
// 6. 增加 new / constructor
//

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
        exampleClass2.parent.addChild(exampleClass1);
        exampleClass2.parent.getChild(1).addChild(new GenericDefineReference(0, exampleClass2));
        exampleClass2.parent.addChild(exampleClass2);
        exampleClass2.parent.getChild(2).addChild(new GenericDefineReference(0, exampleClass2));

    }

    public static void main(String[] args) {
        buildRelation();
//        System.out.println(upper);
//        System.out.println(exampleClass1);
//        System.out.println(exampleClass2);

        buildRelation();
        deal(upper);
        // extracted(exampleClass1);
        // extracted(exampleClass2);

        System.out.println(upper);
        System.out.println(exampleClass1);
        System.out.println(exampleClass2);


        /*upper.selfConsistent(constant.TODO, toBeChecks);
        upper.selfConsistent(constant.RAW_TYPE, toBeChecks);
        upper.selfConsistent(constant.PARAMETER, toBeChecks);
        upper.selfConsistent(constant.FINISH, toBeChecks);*/
    }

    private static void deal(TempRawType type) {
        int i = 0;
        do {
            LinkedList<ToBeCheck> toBeChecks = new LinkedList<>();
            toBeChecks.addLast(new ToBeCheck(null, type));
            while (!toBeChecks.isEmpty()) {
                ToBeCheck first = toBeChecks.removeLast();
                Level outerLevel = first.earnFromStatus == null ? Level.TODO : first.earnFromStatus.getLevel();
                System.out.println(first.earnTarget + ", " + outerLevel);
                first.earnTarget.selfConsistent(outerLevel, toBeChecks);
            }
            System.out.println(i++);
        } while (type.getLevel() != Level.FINISH);
    }
}