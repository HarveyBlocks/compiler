package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.SourceContextTestCreator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.link.LinkTypeFactory;
import org.harvey.compiler.type.generic.link.ParameterizedTypeLink;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.command.store.SourceTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.InnerType;
import org.harvey.compiler.type.generic.register.command.RegisterGenericParamCount;
import org.harvey.compiler.type.generic.register.command.store.BasicTypeStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeReferenceStoreCommand;
import org.harvey.compiler.type.generic.register.command.store.TypeStoreCommand;
import org.harvey.compiler.type.raw.KeywordBasicType;

import java.util.*;

/**
 * WithoutBounds
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-27 15:22
 */
@Deprecated
public class SequentialTypeCommandFactoryWithoutBounds {
    public static List<GenericTypeRegisterCommand> create(ListIterator<SourceString> sourceIterator) {
        // id[][].id
        // skip 一个id.id.id.id, until not 这个结构
        // [][]这个结构, 怎么办?
        // [ 前一定是 st
        // ] 前存在[ , 则register, 不存在则previous-break
        // stack <int> 是 [ 则 stack.push(0)
        //              是 comma 则 ++
        //              是 ] 则 pop + 1
        Stack<Integer> operatorStack = new Stack<>();
        LinkedList<GenericTypeRegisterCommand> command = new LinkedList<>();
        boolean expectItem = true;
        boolean preInner = false;
        SourcePosition position = null;
        Stack<FullIdentifierString> preStack = new Stack<>();
        while (sourceIterator.hasNext()) {
            SourceString next = sourceIterator.next();
            position = next.getPosition();
            if (isItem(next.getType())) {
                if (!expectItem) {
                    throw new AnalysisTypeException(position, "unexpected an identifier");
                }
                command.add(typeItem(preStack, next, sourceIterator, preInner));
                if (preInner) {
                    command.add(new InnerType());
                }
                expectItem = false;
            } else if (expectItem) {
                throw new AnalysisTypeException(position, "expected an identifier");
            } else if (next.getType() == SourceType.OPERATOR) {
                boolean still = dealOperator(next, command, operatorStack, preStack);
                if (!still) {
                    sourceIterator.previous();
                    break;
                }
                expectItem = !"]".equals(next.getValue());
                preInner = ".".equals(next.getValue());
            } else {
                sourceIterator.previous();
                break;
            }
        }
        // stack is clear
        if (!operatorStack.empty()) {
            throw new AnalysisTypeException(position, "expect " + Operator.GENERIC_LIST_PRE.getName());
        }
        return command;
    }


    private static TypeStoreCommand typeItem(
            Stack<FullIdentifierString> preStack,
            SourceString next,
            ListIterator<SourceString> sourceIterator, boolean preInner) {
        if (next.getType() == SourceType.KEYWORD) {
            if (preInner) {
                throw new AnalysisTypeException(next.getPosition(), "can not be inner type");
            }
            // basic type
            preStack.push(null);// null for basic
            Keyword keyword = Keyword.get(next.getValue());
            if (keyword == null || !Keywords.isNormalBasicType(keyword)) {
                throw new AnalysisTypeException(next.getPosition(), "expected an identifier");
            }
            return new BasicTypeStoreCommand(new BasicTypeString(next.getPosition(), KeywordBasicType.get(keyword)));
        } else if (next.getType() == SourceType.IDENTIFIER) {
            sourceIterator.previous();
            FullIdentifierString sourceType = skipFullIdentifier(sourceIterator);
            if (!preInner) {
                preStack.push(sourceType);
                return new SourceTypeStoreCommand(FullIdentifierString.emptyFullname(), sourceType);
            }
            if (preStack.empty()) {
                throw new AnalysisTypeException(next.getPosition(), "expect pre identifier");
            }
            FullIdentifierString pre = preStack.pop();
            if (pre == null) {
                throw new AnalysisTypeException(next.getPosition(), "no type inner basic type");
            }
            FullIdentifierString union = FullIdentifierString.union(pre, sourceType);
            preStack.push(union);
            return new SourceTypeStoreCommand(pre, sourceType);
        } else {
            throw new CompilerException("Unknown type");
        }
    }

    private static boolean isItem(SourceType type) {
        return type == SourceType.IDENTIFIER || type == SourceType.KEYWORD;
    }

    private static boolean dealOperator(
            SourceString next, LinkedList<GenericTypeRegisterCommand> command, Stack<Integer> operatorStack,
            Stack<FullIdentifierString> preStack) {
        switch (next.getValue()) {
            case "[":
                if (command.isEmpty()) {
                    throw new AnalysisTypeException(next.getPosition(), "expect identifier pre");
                }
                operatorStack.push(0);
                // except item
                break;
            case "]":
                if (operatorStack.empty()) {
                    // end
                    // sourceIterator.previous();
                    return false;
                }
                preStack.pop();
                command.add(new RegisterGenericParamCount(operatorStack.pop() + 1));
                // except comma or dot or not still
                break;
            case ",":
                if (operatorStack.empty()) {
                    // end
                    // sourceIterator.previous();
                    return false;
                }
                preStack.pop();
                int newTop = operatorStack.pop() + 1;
                operatorStack.push(newTop);
                // except item
                break;
            case ".":
                if (command.isEmpty()) {
                    throw new AnalysisTypeException(next.getPosition(), "expect pre identifier");
                }
                // except item
                break;
            default:
                // sourceIterator.previous();
                return false;
        }
        return true;
    }

    private static FullIdentifierString skipFullIdentifier(ListIterator<SourceString> sourceIterator) {
        return LinkTypeFactory.skipFullIdentifier(sourceIterator);
    }

    public static ParameterizedTypeLink<SourcePositionSupplier> source2Structure(List<GenericTypeRegisterCommand> command) {
        Stack<ParameterizedTypeLink<SourcePositionSupplier>> stack = new Stack<>();
        for (GenericTypeRegisterCommand each : command) {
            if (each instanceof InnerType) {
                inner(stack);
            } else if (each instanceof TypeStoreCommand) {
                ArrayList<Pair<SourcePositionSupplier, List<ParameterizedTypeLink<SourcePositionSupplier>>>> pairs = new ArrayList<>();
                pairs.add(new Pair<>(rawType(each), new ArrayList<>()));
                stack.push(new ParameterizedTypeLink<>(pairs));
            } else if (each instanceof RegisterGenericParamCount) {
                registerGeneric(((RegisterGenericParamCount) each).getParameterCount(), stack);
            } else {
                throw new CompilerException("unknown type: " + each);
            }
        }
        if (stack.size() != 1) {
            throw new CompilerException("illegal size: " + stack.size());
        }
        return stack.pop();
    }

    private static void inner(Stack<ParameterizedTypeLink<SourcePositionSupplier>> stack) {
        if (stack.empty()) {
            throw new CompilerException("no inner");
        }
        ParameterizedTypeLink<SourcePositionSupplier> inner = stack.pop();
        // 设置parameters
        if (stack.empty()) {
            throw new CompilerException("no inner target");
        }
        ParameterizedTypeLink<SourcePositionSupplier> target = stack.peek();
        target.getList().addAll(inner.getList());
    }

    private static void registerGeneric(
            int parameterCount, Stack<ParameterizedTypeLink<SourcePositionSupplier>> stack) {
        LinkedList<ParameterizedTypeLink<SourcePositionSupplier>> params = new LinkedList<>();
        while (parameterCount-- > 0) {
            if (stack.empty()) {
                throw new CompilerException("param not enough for parameter count in register command");
            }
            ParameterizedTypeLink<SourcePositionSupplier> pop = stack.pop();
            params.addFirst(pop);
        }
        if (stack.empty()) {
            throw new CompilerException("no register target");
        }
        ParameterizedTypeLink<SourcePositionSupplier> target = stack.peek();
        // 设置parameters
        target.getList().get(target.getList().size() - 1).getValue().addAll(params);
    }

    private static SourcePositionSupplier rawType(GenericTypeRegisterCommand command) {
        if (command instanceof SourceTypeStoreCommand) {
            return ((SourceTypeStoreCommand) command).getSourceType();
        } else if (command instanceof BasicTypeStoreCommand) {
            return ((BasicTypeStoreCommand) command).getBasicType();
        } else {
            throw new CompilerException("illegal type: " + command);
        }
    }

    public static List<GenericTypeRegisterCommand> referFullIdentifier(
            List<GenericTypeRegisterCommand> command,
            ReferManager manager) {
        List<GenericTypeRegisterCommand> result = new LinkedList<>();
        for (GenericTypeRegisterCommand each : command) {
            if (each instanceof SourceTypeStoreCommand) {
                SourceTypeStoreCommand storeCommand = (SourceTypeStoreCommand) each;
                FullIdentifierString pre = storeCommand.getPre();
                FullIdentifierString sourceType = storeCommand.getSourceType();
                LinkedList<ReferenceElement> referred = manager.refer(pre, sourceType);
                if (pre.empty()) {
                    ReferenceElement first = referred.removeFirst();
                    result.add(new TypeReferenceStoreCommand(first));
                }
                while (!referred.isEmpty()) {
                    ReferenceElement first = referred.removeFirst();
                    result.add(new TypeReferenceStoreCommand(first));
                    result.add(new InnerType());
                }
            } else if (!(each instanceof InnerType)) {
                result.add(each);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        SourceTextContext source = SourceContextTestCreator.newSource(
                " org . harvey . type . A  [ org . harvey . type . B [ C ] . A . D [ A . D ] , org . harvey . type . A [ C ]  ] . A . D ");
        source = CoreCompiler.registerChain().execute(source);
        source.forEach(System.out::println);
        List<GenericTypeRegisterCommand> genericTypeRegisterCommands = SequentialTypeCommandFactoryWithoutBounds.create(
                source.listIterator());
        genericTypeRegisterCommands.forEach(System.out::println);
        ParameterizedTypeLink<SourcePositionSupplier> type = SequentialTypeCommandFactoryWithoutBounds.source2Structure(
                genericTypeRegisterCommands);
        System.out.println("----");
        printType(type);
    }

    private static void printType(ParameterizedTypeLink<SourcePositionSupplier> type) {
        printType(0, type);
        System.out.println();
    }

    private static void printType(int depth, ParameterizedTypeLink<SourcePositionSupplier> type) {
        List<Pair<SourcePositionSupplier, List<ParameterizedTypeLink<SourcePositionSupplier>>>> list = type.getList();
        String tableMaker = "\t".repeat(depth);
        for (int i = 0; i < list.size(); i++) {
            Pair<SourcePositionSupplier, List<ParameterizedTypeLink<SourcePositionSupplier>>> each = list.get(i);
            if (i != 0) {
                System.out.print(".");
            } else {
                System.out.print(tableMaker);
            }
            System.out.print(each.getKey());
            List<ParameterizedTypeLink<SourcePositionSupplier>> value = each.getValue();
            if (!value.isEmpty()) {
                System.out.println("[");
            }
            for (int j = 0; j < value.size(); j++) {
                ParameterizedTypeLink<SourcePositionSupplier> link = value.get(j);
                printType(depth + 1, link);
                if (j != value.size() - 1) {
                    System.out.println(",");
                }
            }
            if (!value.isEmpty()) {
                System.out.print("\n" + tableMaker + "]");
            }
        }
    }

    public interface ReferManager {
        LinkedList<ReferenceElement> refer(FullIdentifierString pre, FullIdentifierString sourceType);
    }
}
