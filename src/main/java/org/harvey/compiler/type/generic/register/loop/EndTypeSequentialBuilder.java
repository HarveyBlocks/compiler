package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.RecursiveInvoker;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeReferenceStoreCommand;
import org.harvey.compiler.type.generic.register.entity.BasicType;
import org.harvey.compiler.type.generic.register.entity.EndType;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

/**
 * 把递归的方法转成循环的方法{@link org.harvey.compiler.common.recusive.demo.SpecialForestNode}
 * {@link org.harvey.compiler.common.recusive}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class EndTypeSequentialBuilder implements RecursiveInvoker {
    private final EndType endType;

    public EndTypeSequentialBuilder(EndType endType) {
        this.endType = endType;
    }


    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        EndTypeContextFrame typeContext = (EndTypeContextFrame) context;
        if (endType instanceof BasicType) {
            return invokeOnBasicType(typeContext, (BasicType) endType);
        }
        return invokeOnFullLinkType(typeContext, (FullLinkType) endType);
    }

    private static CallableStackFrame invokeOnFullLinkType(EndTypeContextFrame typeContext, FullLinkType type) {
        ReferenceElement reference = type.getReference();
        TypeReferenceStoreCommand storeCommand = new TypeReferenceStoreCommand(reference);
        typeContext.add(storeCommand);
        return EndTypeContextFrame.newParamListLoopSequentialFrame(type.getParams(), type.getInnerType());
    }

    private static CallableStackFrame invokeOnBasicType(EndTypeContextFrame typeContext, BasicType basicType) {
        BasicTypeString basic = basicType.getBasic();
        BasicTypeStoreCommand storeCommand = new BasicTypeStoreCommand(basic);
        typeContext.add(storeCommand);
        return null;
    }
}
