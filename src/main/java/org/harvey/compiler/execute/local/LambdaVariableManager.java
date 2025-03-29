package org.harvey.compiler.execute.local;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 * lambda 要考虑到获取局部变量, 还要考虑到this.能获取outer对象的字段
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-11 22:08
 */
public class LambdaVariableManager implements LocalVariableManager {
    private final LocalVariableManager outer;
    private final LocalVariableManager inner = new DefaultLocalVariableManager();
    /**
     * 指向outer的pool的reference
     */
    private final List<LocalTableElementDeclare> innerEarnFromOuter = new ArrayList<>();
    private final List<LocalTableElementDeclare> regionForEarnFromOuter = new ArrayList<>();
    /**
     * 映射的方法论
     */
    private ArrayList<Pair<Integer, Integer>> declareStartMap;
    private int outerInnerMapRegion = -1;

    public LambdaVariableManager(LocalVariableManager outerLocalVariableManager) {
        this.outer = outerLocalVariableManager;
    }

    public ArrayList<Pair<Integer, Integer>> getDeclareStartMap() {
        if (declareStartMap == null) {
            throw new CompilerException("not complete map outer inner region");
        }
        return declareStartMap;
    }

    @Override
    public LocalTableElementDeclare forDeclare(IdentifierString name, ParameterizedType parameterizedType) {
        if (outer.hasDeclared(name.getValue())) {
            throw new AnalysisExpressionException(name.getPosition(), "outer has declared");
        }
        return inner.forDeclare(name, parameterizedType);
    }

    @Override
    public LocalTableElementDeclare forDeclare(LocalVariableType type, IdentifierString name) {
        if (outer.hasDeclared(name.getValue())) {
            throw new AnalysisExpressionException(name.getPosition(), "outer has declared");
        }
        return inner.forDeclare(type, name);
    }

    @Override
    public LocalTableElementDeclare forDeclare(
            LocalVariableType type, IdentifierString name,
            ParameterizedType parameterizedType) {
        if (outer.hasDeclared(name.getValue())) {
            throw new AnalysisExpressionException(name.getPosition(), "outer has declared");
        }
        return inner.forDeclare(type, name, parameterizedType);
    }

    @Override
    public boolean hasDeclared(String name) {
        if (inner.hasDeclared(name)) {
            return true;
        }
        return outer.hasDeclared(name);
    }

    @Override
    public void intoBody() {
        inner.intoBody();
    }

    @Override
    public void leaveBody() {
        inner.leaveBody();
    }

    @Override
    public LocalTableElementDeclare forUse(String name, SourcePosition position) {
        LocalTableElementDeclare element = inner.forUse(name, position);
        if (element != null) {
            return element;
        }
        element = outer.forUse(name, position);
        if (element == null) {
            return null;
        }
        innerEarnFromOuter.add(element);
        return element;
    }

    @Override
    public List<LocalTableElementDeclare> getDeclarePool() {
        if (regionForEarnFromOuter == null) {
            throw new CompilerException("not complete map outer inner region");
        }
        // outer的加inner的
        List<LocalTableElementDeclare> union = new ArrayList<>(
                regionForEarnFromOuter.size() + inner.getDeclarePool().size());
        union.addAll(regionForEarnFromOuter);
        union.addAll(inner.getDeclarePool());
        return Collections.unmodifiableList(union);
    }

    @Override
    public ParameterizedType getType(LocalTableElementDeclare declare) {
        return null;
    }

    public void mapOuterInnerRegion() {
        // 外到内的值映射
        // 内部的局部变量表的重构, 向后移动
        int size = 0;
        // 映射方式
        this.declareStartMap = new ArrayList<>();
        for (int i = 0; i < innerEarnFromOuter.size(); i++) {
            LocalTableElementDeclare outerDeclare = innerEarnFromOuter.get(i);
            declareStartMap.add(new Pair<>(outerDeclare.getStart(), size));
            size += outerDeclare.getLocalVariableType().getOffset();
            ParameterizedType type = outer.getType(outerDeclare);
            SourcePosition outerDeclarePosition = outerDeclare.getPosition();
            LocalTableElementDeclare mappedDeclare;
            if (type == null) {
                LocalVariableType localVariableType = outerDeclare.getLocalVariableType();
                mappedDeclare = new LocalTableElementDeclare(
                        outerDeclarePosition, size, localVariableType.ordinal());

            } else {
                mappedDeclare = new LocalTableElementDeclare(
                        // TODO 糟糕的设计, 不应该有typeReference, 但是每个地方都要存一个ParameterizedType是不是不合适?
                        // 所以应该指向的是声明的类型
                        outerDeclarePosition, size, 0/* inner.getTypePoolSize()*/);
            }
            innerEarnFromOuter.set(i, mappedDeclare);
        }
        for (LocalTableElementDeclare declare : innerEarnFromOuter) {
            // TODO
        }
        outerInnerMapRegion = size;
        for (LocalTableElementDeclare innerDeclare : inner.getDeclarePool()) {
            innerDeclare.resetStart(this);
        }

    }

    public int resetStart(int start) {
        if (outerInnerMapRegion < 0) {
            throw new CompilerException("not complete map outer inner region");
        }
        return start + outerInnerMapRegion;
    }
}
