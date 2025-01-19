package org.harvey.compiler.analysis.text.type.generic;

import lombok.Getter;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourceString;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 11:46
 */
@Getter
public class GenericType extends NormalType {
    private final GenericArgument[] args;

    public GenericType(SourceString name, NormalType parent, GenericArgument[] args) {
        super(name, parent);
        if (args == null) {
            throw new CompilerException("Generic Declare Argument", new NullPointerException());
        }
        this.args = args;
    }

    public GenericType(NormalType origin, GenericArgument[] args) {
        super(origin);
        if (args == null) {
            throw new CompilerException("Generic Declare Argument", new NullPointerException());
        }
        this.args = args;
    }

    public GenericType(SourceString name, GenericArgument[] args) {
        super(name);
        if (args == null) {
            throw new CompilerException("Generic Declare Argument", new NullPointerException());
        }
        this.args = args;
    }

    /**
     * 查找defaultGeneric中使用了name的位置
     * Template<T  = A<T>>
     * Template<T  = A<? extends T>>
     */
    private static SourceString nestingTarget(GenericType genericType, String target) {
        LinkedList<GenericArgument[]> tobeCheck = new LinkedList<>();
        tobeCheck.add(genericType.getArgs());
        while (!tobeCheck.isEmpty()) {
            GenericArgument[] tobeCheckArgs = tobeCheck.removeFirst();
            if (tobeCheckArgs == null) {
                continue;
            }
            for (GenericArgument arg : tobeCheckArgs) {
                SourceString name = arg.getName();
                if (name.getValue().equals(target)) {
                    return name;
                }
                GenericType lower = arg.getLower();
                if (lower != null) {
                    tobeCheck.addLast(lower.getArgs());
                }
                GenericType upper = arg.getUpper();
                if (upper != null) {
                    tobeCheck.addLast(upper.getArgs());
                }
                GenericType defaultGeneric = arg.getDefaultGeneric();
                if (defaultGeneric != null) {
                    tobeCheck.addLast(defaultGeneric.getArgs());
                }
            }
        }
        return null;
    }

    public Set<String> buildGenericArgNameSet() {
        Set<String> nameSet = new HashSet<>();
        for (GenericArgument arg : args) {
            SourceString name = arg.getName();
            String nameValue = name.getValue();
            if (nameSet.contains(nameValue)) {
                throw new AnalysisExpressionException(name.getPosition(), "Multiple generic argument declare here");
            }
            nameSet.add(nameValue);
        }
        return nameSet;
    }

    /**
     * Template<T  = A<T>>
     * 这个默认值是不行的, 定义默认的自己不能用自己作为参数吧?, 否则到时候就嵌套了, 应该检查<br>
     * 只需对最外层的GenericType使用
     */
    public void notNestingDefaultGenericDefinition() {
        for (GenericArgument arg : args) {
            GenericType defaultGeneric = arg.getDefaultGeneric();
            if (defaultGeneric == null) {
                continue;
            }
            // 检查defaultGeneric各层是否存在T1
            SourceString sourceString = nestingTarget(defaultGeneric, arg.getName().getValue());
            if (sourceString == null) {
                continue;
            }
            throw new AnalysisExpressionException(arg.getName().getPosition(), sourceString.getPosition(),
                    sourceString.getValue() + " constituted nesting default generic definition");
        }
    }
}
