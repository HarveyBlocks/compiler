package org.harvey.compiler.execute.local;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 23:27
 */
public class DefaultLocalVariableManager implements LocalVariableManager {
    private final Stack<Map<String, Integer>> nameOffset = new Stack<>();

    private final Stack<Integer> variableStartPointer = new Stack<>();
    // TODO 糟糕的设计, 等待删除
    @Deprecated
    private final List<ParameterizedType> typePool = new ArrayList<>();
    @Getter
    private final List<LocalTableElementDeclare> declarePool = new ArrayList<>();

    @Override
    public LocalTableElementDeclare forDeclare(IdentifierString name, ParameterizedType parameterizedType) {
        return forDeclare(LocalVariableManager.LocalVariableType.REFERENCE, name, parameterizedType);
    }

    @Override
    public LocalTableElementDeclare forDeclare(LocalVariableType type, IdentifierString name) {
        return forDeclare(type, name, null);
    }

    @Override
    public LocalTableElementDeclare forDeclare(
            LocalVariableType type, IdentifierString name,
            ParameterizedType parameterizedType) {
        if (hasDeclared(name.getValue())) {
            throw new AnalysisExpressionException(name.getPosition(), "repeated declare");
        }
        Map<String, Integer> peek = nameOffset.peek();
        int typeReference = type.ordinal();
        if (type == LocalVariableManager.LocalVariableType.REFERENCE) {
            typeReference += typePool.size();
            typePool.add(parameterizedType);
        }
        LocalTableElementDeclare element = new LocalTableElementDeclare(name.getPosition(),
                variableStartPointer.peek(), typeReference
        );
        peek.put(name.getValue(), declarePool.size());
        declarePool.add(element);
        variableStartPointer.push(variableStartPointer.pop() + type.getOffset());
        return element;
    }

    @Override
    public boolean hasDeclared(String name) {
        for (Map<String, Integer> map : nameOffset) {
            if (map.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void intoBody() {
        nameOffset.push(new HashMap<>());
        variableStartPointer.push(variableStartPointer.peek());
    }

    @Override
    public void leaveBody() {
        nameOffset.pop();
        variableStartPointer.pop();
    }

    @Override
    public LocalTableElementDeclare forUse(String name, SourcePosition position) {
        for (Map<String, Integer> map : nameOffset) {
            Integer msg = map.get(name);
            if (msg != null) {
                return declarePool.get(msg);
            }
        }
        return null;
    }

    @Override
    public ParameterizedType getType(LocalTableElementDeclare declare) {
        int typeReference = declare.getTypeReference();
        if (typeReference >= LocalVariableManager.LocalVariableType.REFERENCE.ordinal()) {
            return typePool.get(typeReference - LocalVariableManager.LocalVariableType.REFERENCE.ordinal());
        }
        return null;
    }


}
