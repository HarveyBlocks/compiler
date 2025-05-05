package org.harvey.compiler.type.transform.test.version5;

import lombok.Getter;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-01 14:00
 */
@SuppressWarnings("DuplicatedCode")
class TempAlias implements GenericDefiner {

    TempGenericDefine[] genericDefines;
    Parameterized origin;
    // 映射啊
    Parameterized endMappedParameterized;
    @Getter
    Level level = Level.TODO;
    @Getter
    String name;
    private Parameterized defaultParameter;

    public TempAlias(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        String gds = genericDefines == null ? " " : Arrays.toString(genericDefines);
        String ps;
        if (origin == null) {
            ps = "";
        } else {
            ps = " extends " + origin.toStringValue();
        }
        return name + gds + ps;
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
        if (this.origin != null) {
            this.origin.selfConsistent(levelUsing, toBeChecks);
        }
        this.level = this.level.up(levelUsing);
    }

    @Override
    public int genericLength() {
        return genericDefines.length;
    }

    @Override
    public TempGenericDefine[] genericDefines() {
        return genericDefines;
    }

    public Parameterized defaultParameter() {
        if (defaultParameter != null) {
            synchronized (this) {
                defaultParameter.level = this.level;
            }
            return defaultParameter;
        }
        synchronized (this) {
            if (defaultParameter != null) {
                defaultParameter.level = this.level;
                return defaultParameter;
            }
            return defaultParameter = buildDefaultParameter();
        }
    }

    private Parameterized buildDefaultParameter() {
        Parameterized parameterized = new Parameterized(this);
        parameterized.level = this.level;
        for (int i = 0; i < this.genericDefines.length; i++) {
            parameterized.addChild(new GenericDefineReference(i, this));
        }
        parameterized.level = this.level;
        return parameterized;
    }

    public Parameterized endMappedParameter() {
        if (endMappedParameterized != null) {
            synchronized (this) {
                endMappedParameterized.level = this.level;
            }
            return endMappedParameterized;
        }
        synchronized (this) {
            if (endMappedParameterized != null) {
                endMappedParameterized.level = this.level;
                return endMappedParameterized;
            }
            buildEndMappedParameterAndSet();
        }
        if (endMappedParameterized != null) {
            endMappedParameterized.level = this.level;
            return endMappedParameterized;
        } else {
            throw new CompilerException("build failed");
        }
    }

    private void buildEndMappedParameterAndSet() {
        // build 些啥?
        Parameterized cur = this.origin;
        Stack<TempAlias> path = new Stack<>();
        path.push(this);
        Parameterized endMappedParameterized; // 是一个 structure开头的parameter
        while (true) {
            if (cur.isGeneric()) {
                throw new AnalysisTypeException(SourcePosition.UNKNOWN, "generic define 不能做 origin");
            } else if (cur.isAlias()) {
                TempAlias aliasRawType = (TempAlias) cur.getRawType();
                cur = aliasRawType.origin;
                if (aliasRawType.endMappedParameterized != null) {
                    MultipleTree<RawTypeCanInParameter> clonedEndTree = aliasRawType.endMappedParameterized.type.cloneThis();
                    endMappedParameterized = new Parameterized(clonedEndTree);
                    endMappedParameterized.level = aliasRawType.endMappedParameterized.level;
                    break;
                } else {
                    path.push(aliasRawType);
                }
            } else if (cur.isStructure()) {
                Parameterized lastOrigin = path.peek().origin;
                MultipleTree<RawTypeCanInParameter> clonedEndTree = lastOrigin.type.cloneThis();
                endMappedParameterized = new Parameterized(clonedEndTree);
                endMappedParameterized.level = lastOrigin.level;
                break;
            } else {
                throw new CompilerException("Unknown type");
            }
        }
        if (!endMappedParameterized.isStructure()) {
            throw new CompilerException("map 失败, 此alias的end mapped 不是structure");
        }
        Level using = Level.decide(this.level, origin.getLevel());
        LinkedList<Parameterized> parameterizedList = ParameterizedMapper.mapForAlias(
                CollectionUtil.cloneStack(path), endMappedParameterized, using);
        while (!path.empty()) {
            if (parameterizedList.isEmpty()) {
                throw new CompilerException("need more end mapped parameter for origin alias");
            }
            Parameterized mapped = parameterizedList.removeLast();
            TempAlias top = path.pop();
            top.setEndMappedParameterized(mapped);
        }
        if (!parameterizedList.isEmpty()) {
            throw new CompilerException("too much end mapped parameters for origin alias");
        }
    }

    @Override
    public String getNameWithLevel() {
        return getName() + "[" + level + "]";
    }

    @Override
    public TempGenericDefine getGeneric(int index) {
        return genericDefines[index];
    }

    public void setEndMappedParameterized(Parameterized parameterized) {
        if (this.endMappedParameterized != null) {
            return;
        }
        synchronized (this) {
            if (this.endMappedParameterized != null) {
                return;
            }
            this.endMappedParameterized = parameterized;
            this.endMappedParameterized.type.setReadOnly(true);
            this.endMappedParameterized.level = this.level;
        }
    }

    /**
     * 转为structure的Parameterized
     */
    public Parameterized mapAndInject(Parameterized parameterizedWithAlias) {
        if (!parameterizedWithAlias.isAlias()) {
            throw new CompilerException("only alias is legal to mapAndInject");
        }
        MultipleTree<RawTypeCanInParameter> endMappedTree = this.endMappedParameter().type.cloneThis();
        MultipleTree<RawTypeCanInParameter> endMappedAndWiredTree = ParameterizedMapper.injectParameterized(
                endMappedTree, this, parameterizedWithAlias.getType());
        endMappedAndWiredTree.setReadOnly(true);
        Parameterized parameterized = new Parameterized(endMappedTree);
        parameterized.level = parameterizedWithAlias.level;
        return parameterized;
    }
}