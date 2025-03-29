package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.define.EnumConstant;
import org.harvey.compiler.execute.expression.ReferenceElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 需要被序列化的的Enum常量对象(成员)
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 13:58
 */
@AllArgsConstructor
@Getter
public class EnumConstantContext {
    private final ReferenceElement identifier;
    /**
     * not nullable
     */
    private final List<Integer> arguments;

    public EnumConstantContext(EnumConstant enumConstant, SourceStringContextPoolFactory poolFactory) {
        identifier = enumConstant.getIdentifier();
        arguments = enumConstant.getArguments().stream().map(poolFactory::add).collect(Collectors.toList());
    }
}
