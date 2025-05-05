package org.harvey.compiler.type.generic.register.loop;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.RecursiveInvoker;
import org.harvey.compiler.type.generic.register.command.BoundsForPlaceholderStoreCommand;
import org.harvey.compiler.type.generic.register.entity.BoundsType;
import org.harvey.compiler.type.generic.register.entity.FullLinkType;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
public class BoundsTypeSequentialBuilder implements RecursiveInvoker {
    private final BoundsType boundsType;

    public BoundsTypeSequentialBuilder(BoundsType boundsType) {
        this.boundsType = boundsType;
    }

    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        ((EndTypeContextFrame) context).add(new BoundsForPlaceholderStoreCommand(boundsType.getPosition()));
        List<FullLinkType> uppers = boundsType.getUppers();
        return EndTypeContextFrame.newUpperListLoopSequentialFrame(uppers, boundsType.getLower());

    }
}
