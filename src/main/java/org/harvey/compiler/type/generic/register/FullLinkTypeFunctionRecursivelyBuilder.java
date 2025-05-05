package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.type.generic.register.command.BoundsForPlaceholderStoreCommand;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterLowerBound;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterUpperCount;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeReferenceStoreCommand;
import org.harvey.compiler.type.generic.register.entity.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 把多态的递归改写成函数的递归
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:51
 */
@Deprecated
public class FullLinkTypeFunctionRecursivelyBuilder {
    private final ReferManager manager;

    public FullLinkTypeFunctionRecursivelyBuilder(ReferManager manager) {
        this.manager = manager;
    }

    public void sequential(FullLinkType type, LinkedList<AtSequentialTypeCommand> result) {
        ReferenceElement reference = type.getReference();
        TypeReferenceStoreCommand storeCommand = new TypeReferenceStoreCommand(reference);
        result.add(storeCommand);
        List<CanParameterType> params = type.getParams();
        // A
        for (CanParameterType param : params) {
            // B
            if (param instanceof BasicType) {
                // C
                BasicTypeString basic = ((BasicType) param).getBasic();
                result.add(new BasicTypeStoreCommand(basic));
            } else if (param instanceof BoundsType) {
                // D
                // bounds type
                result.add(new BoundsForPlaceholderStoreCommand(param.getPosition()));
                BoundsType boundsType = (BoundsType) param;
                FullLinkType lower = boundsType.getLower();
                // E
                if (lower != null) {
                    sequential(lower, result);
                    result.add(new RegisterLowerBound());
                }
                List<FullLinkType> uppers = boundsType.getUppers();
                // F
                for (FullLinkType upper : uppers) {
                    sequential(upper, result);
                }
                // G
                if (!uppers.isEmpty()) {
                    RegisterUpperCount registerUpperCount = new RegisterUpperCount(uppers.size());
                    result.add(registerUpperCount);
                }
            } else if (param instanceof FullLinkType) {
                sequential((FullLinkType) param, result);
            } else {
                throw new UnknownTypeException(CanParameterType.class, param);
            }
        }
        // A
        if (!params.isEmpty()) {
            result.add(new RegisterGenericParamCount(params.size()));
        }
        FullLinkType next = type.getInnerType();
        if (next != null) {
            sequential(next, result);
            result.add(new InnerType());
        }
    }


}


