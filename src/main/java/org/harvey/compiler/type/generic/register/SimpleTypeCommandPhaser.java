package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.exception.analysis.AnalysisTextException;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.type.generic.register.command.BoundsForPlaceholderStoreCommand;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.command.bounds.MultipleUpperBounds;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterLowerBound;
import org.harvey.compiler.type.generic.register.command.bounds.RegisterUpperBound;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.SourceTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeStoreCommand;
import org.harvey.compiler.type.generic.register.entity.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 14:45
 */
public class SimpleTypeCommandPhaser {

    public static EndType buildTree(List<GenericTypeRegisterCommand> expression, ReferManager referManager) {
        Stack<TypeTreeNode> typeStack = new Stack<>();
        for (GenericTypeRegisterCommand each : expression) {
            if (each instanceof TypeStoreCommand) {
                if (each instanceof BoundsForPlaceholderStoreCommand) {
                    typeStack.add(new BoundsType(((BoundsForPlaceholderStoreCommand) each).getPosition()));
                } else if (each instanceof SourceTypeStoreCommand) {
                    SourceTypeStoreCommand st = (SourceTypeStoreCommand) each;
                    LinkedList<ReferenceElement> refer = referManager.refer(st.getPre(), st.getSourceType());
                    if (refer.isEmpty()) {
                        throw new AnalysisTextException(st.getPosition(), "can not refer to this identifier");
                    }
                    FullLinkType cur = new FullLinkType(refer.removeFirst());
                    while (!refer.isEmpty()) {
                        ReferenceElement innerReference = refer.removeFirst();
                        FullLinkType innerType = new FullLinkType(innerReference);
                        innerType.setOuterType(cur);
                        cur.setInnerType(innerType);
                        cur = cur.getInnerType();
                    }
                    typeStack.add(cur);
                } else if (each instanceof BasicTypeStoreCommand) {
                    BasicTypeStoreCommand st = (BasicTypeStoreCommand) each;
                    typeStack.add(new BasicType(st.getBasicType()));
                } else {
                    throw unknownType(each);
                }
            } else if (each instanceof MultipleUpperBounds) {
                // ? 完全没必要啊
                enoughTypeInStack(each, typeStack, 2);
                TypeTreeNode top1 = typeStack.pop();
                TypeTreeNode top2 = typeStack.pop();
                typeStack.push(UnionType.union(top2, top1));
            } else if (each instanceof InnerType) {
                // 下一个store的就是
                enoughTypeInStack(each, typeStack, 2);
                TypeTreeNode inner = typeStack.pop();
                TypeTreeNode outer = typeStack.pop();
                typeStack.push(FullLinkType.link(outer, inner));
            } else if (each instanceof RegisterLowerBound) {
                // 下一个store的就是
                enoughTypeInStack(each, typeStack, 2);
                TypeTreeNode bound = typeStack.pop();
                TypeTreeNode target = typeStack.pop();
                typeStack.push(BoundsType.boundLower(target, bound));
            } else if (each instanceof RegisterUpperBound) {
                enoughTypeInStack(each, typeStack, 2);
                TypeTreeNode bound = typeStack.pop();
                TypeTreeNode target = typeStack.pop();
                typeStack.push(BoundsType.boundUpper(target, bound));
            } else if (each instanceof RegisterGenericParamCount) {
                // top 的 在 最后,
                int parameterCount = ((RegisterGenericParamCount) each).getParameterCount();
                enoughTypeInStack(each, typeStack, parameterCount + 1);
                LinkedList<CanParameterType> params = new LinkedList<>();
                while (parameterCount-- > 0) {
                    TypeTreeNode top = typeStack.pop();
                    if (!(top instanceof CanParameterType)) {
                        throw new AnalysisTypeException(
                                top.getPosition(),
                                "can not be generic parameter type: " + top.getClass().getSimpleName()
                        );
                    }
                    if (top instanceof FullLinkType) {
                        top = ((FullLinkType) top).toOutermost();
                    }
                    params.addFirst((CanParameterType) top);
                }
                TypeTreeNode target = typeStack.pop();
                typeStack.push(FullLinkType.addParams(target, params));
            } else {
                throw unknownType(each);
            }

        }
        if (typeStack.size() != 1) {
            throw illegalSize(typeStack.size());
        }
        TypeTreeNode last = typeStack.pop();
        return returnEndType(last);
    }

    private static void enoughTypeInStack(
            GenericTypeRegisterCommand each, Stack<TypeTreeNode> typeStack, int expectCount) {
        if (typeStack.empty()) {
            throw expectItem(each);
        }
        if (typeStack.size() < expectCount) {
            TypeTreeNode top = typeStack.pop();
            throw new AnalysisTypeException(top.getPosition(), "expect more type");
        }
    }

    private static CompilerException expectItem(GenericTypeRegisterCommand each) {
        return new CompilerException(
                "Illegal type generic expression: expected item before " + each.getClass().getSimpleName());
    }

    private static EndType returnEndType(TypeTreeNode last) {
        if (!(last instanceof EndType)) {
            throw new AnalysisTypeException(last.getPosition(), "can not be a type");
        }
        if (last instanceof FullLinkType) {
            // 指向pre
            return ((FullLinkType) last).toOutermost();
        } else {
            return (EndType) last;
        }
    }

    private static CompilerException illegalSize(int size) {
        return new CompilerException("Illegal type generic expression: link stack size expected one, but: " + size);
    }

    private static CompilerException unknownType(GenericTypeRegisterCommand each) {
        return new CompilerException(
                "Illegal type generic expression: unknown type of: " + each.getClass().getSimpleName());
    }

}
