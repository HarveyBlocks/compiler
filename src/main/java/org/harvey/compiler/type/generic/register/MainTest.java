package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.core.CoreCompiler;
import org.harvey.compiler.exception.self.UnknownTypeException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.execute.test.SourceContextTestCreator;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.syntax.BasicTypeString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.register.command.sequential.AtSequentialTypeCommand;
import org.harvey.compiler.type.generic.register.entity.*;
import org.harvey.compiler.type.generic.register.loop.DefaultTypeContextFrame;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;

import java.util.*;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 17:40
 */
class MainTest {
    private static int depthMin = Integer.MAX_VALUE;
    private static int depthMax = Integer.MIN_VALUE;

    public static void registerDepth() {
        int depth = Thread.currentThread().getStackTrace().length;
        if (depth < depthMin) {
            depthMin = depth;
        }
        if (depth > depthMax) {
            depthMax = depth;
        }
    }

    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        System.out.println("seed = " + seed);
        Random random = new Random(seed);
        for (int i = 0; i < 1; i++) {
            /* SpecialForestNode tree = buildTree();*/
            int size = 1000/*random.nextInt(1000) + 1*/;
            /* 比例*/
            double becomingNextOrChildProportion = random.nextDouble();
            /*" org . harvey . type . A  [ " +
               "org . harvey . type . B [ C , _ extends C & D super C  ] " +
               ". A . D [ A . D ] , org . harvey . type . A [ C ]  ]" +
               " . A . D "
               "A[B].C[D].E[F].G[H].I[J].K[L]"
               "A[B[C[D[E[F[G[H[I[J[K[L[M]]]]]]]]]]]]"
               */
            SourceTextContext source = SourceContextTestCreator.newSource("A[B] extends B");
            source = CoreCompiler.registerChain().execute(source);
            ListIterator<SourceString> sourceIterator = source.listIterator();
            List<GenericTypeRegisterCommand> genericTypeRegisterCommands = SimpleTypeCommandFactory.create(
                    sourceIterator);
            while (sourceIterator.hasNext()) {
                System.out.print(sourceIterator.next().getValue() + " ");
            }
            System.out.println();
            genericTypeRegisterCommands.forEach(System.out::println);
            ReferManager referManager = testReferManager();
            EndType link = SimpleTypeCommandPhaser.buildTree(genericTypeRegisterCommands, referManager);
            // FullLinkType type =new RandomFullnameTypeGenerator(size, becomingNextOrChildProportion, random).build();
            String treeShownString = showString(link);
            System.out.println(treeShownString);

            List<AtSequentialTypeCommand> result = getResult(link);
            result.forEach(System.out::println);
            EndType fromCommand = SequentialTypeCommandPhaser.phaseCommand(result);
            String fromCommandShowString = MainTest.showString(fromCommand);
            System.out.println(fromCommandShowString);
            /*System.out.println(fromCommandShowString);*/
            if (!fromCommandShowString.equals(treeShownString)) {
                // throw new CompilerException("NOT EQUALS, loop = " + i);
            }
        }
        System.out.println("depth min = " + depthMin);
        System.out.println("depth max = " + depthMax);
    }


    private static String showString(EndType endType) {
        if (endType instanceof BasicType) {
            BasicTypeString basic = ((BasicType) endType).getBasic();
            return basic.getKeywordBasicType().name();
        }
        FullLinkType type = (FullLinkType) endType;
        String id;
        id = "id" + type.getReference().getReference();
        StringJoiner paramsJoiner = new StringJoiner(",", "[", "]");
        for (CanParameterType param : type.getParams()) {
            if (param instanceof BasicType) {
                paramsJoiner.add(showString((BasicType) param));
            } else if (param instanceof FullLinkType) {
                paramsJoiner.add(showString((FullLinkType) param));
            } else if (param instanceof BoundsType) {
                // bounds type
                StringBuilder boundsBuilder = new StringBuilder("_");
                StringJoiner upperJoiner = new StringJoiner("&");
                BoundsType boundsType = (BoundsType) param;
                if (!boundsType.getUppers().isEmpty()) {
                    boundsBuilder.append(" upper ");
                }
                for (FullLinkType upper : boundsType.getUppers()) {
                    upperJoiner.add(showString(upper));
                }
                boundsBuilder.append(upperJoiner);
                if (boundsType.getLower() != null) {
                    boundsBuilder.append(" lower ").append(showString(boundsType.getLower()));
                }
                paramsJoiner.add(boundsBuilder);
            } else {
                throw new UnknownTypeException(CanParameterType.class, param);
            }
        }
        String params;
        if (type.getParams().isEmpty()) {
            params = "";
        } else {
            params = paramsJoiner.toString();
        }
        String next = type.innermost() ? "" : "." + showString(type.getInnerType());
        return id + params + next;
    }

    private static List<AtSequentialTypeCommand> getResult(EndType type) {
        return RegisterTypeBuildUtil.buildSequentialCommand(new DefaultTypeContextFrame(type));
    }

    private static ReferManager testReferManager() {
        return new ReferManager() {
            int count = 0;
            @Override
            public LinkedList<ReferenceElement> refer(FullIdentifierString pre, FullIdentifierString sourceType) {
                LinkedList<ReferenceElement> referenceElements = new LinkedList<>();
                referenceElements.add(
                        new ReferenceElement(sourceType.getPosition(), ReferenceType.IDENTIFIER, count++));
                referenceElements.add(
                        new ReferenceElement(sourceType.getPosition(), ReferenceType.IDENTIFIER, count++));
                return referenceElements;
            }
            @Override
            public FullIdentifierString dereference(ReferenceElement referenceElement) {
                if (referenceElement.getType() == ReferenceType.KEYWORD) {
                    return new FullIdentifierString(
                            referenceElement.getPosition(),
                            "keyword" + referenceElement.getReference()
                    );
                } else {
                    return new FullIdentifierString(
                            referenceElement.getPosition(),
                            "identifier" + referenceElement.getReference()
                    );
                }

            }
        };
    }


}
