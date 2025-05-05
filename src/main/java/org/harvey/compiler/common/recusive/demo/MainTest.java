package org.harvey.compiler.common.recusive.demo;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.FrameInitializer;
import org.harvey.compiler.common.recusive.RecursiveInvokerRegister;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 12:53
 */
public class MainTest {
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
            SpecialForestNode tree = new RandomForestGenerator(size, becomingNextOrChildProportion, random).build();
            String treeShownString = tree.showString();
            System.out.println(treeShownString);
            List<String> result = getResult(tree);
            /* result.forEach(System.out::println);*/
            SpecialForestNode fromCommand = SpecialForestTraverseManager.phaseCommand(result);
            String fromCommandShowString = fromCommand.showString();
            /*System.out.println(fromCommandShowString);*/
            if (!fromCommandShowString.equals(treeShownString)) {
                throw new CompilerException("NOT EQUALS");
            }
        }
        System.out.println("depth min = " + depthMin);
        System.out.println("depth max = " + depthMax);
    }

    private static List<String> getResult(SpecialForestNode root) {
        CallableStackFrame context = new RecursiveInvokerRegister(newFrameInitializer(root)).execute();
        return ((SpecialForestTraverseManager.ContextFrame) context).getResult();
    }

    private static FrameInitializer newFrameInitializer(SpecialForestNode root) {
        return () -> newContextFrame(new LinkedList<>(), new SpecialForestTraverseManager(root));
    }

    private static SpecialForestTraverseManager.ContextFrame newContextFrame(
            List<String> result, SpecialForestTraverseManager manager) {
        return new SpecialForestTraverseManager.ContextFrame() {
            @Override
            public List<String> getResult() {
                return result;
            }

            @Override
            public void add(String element) {
                result.add(element);
            }

            @Override
            public CallableStackFrame invokeRecursive(CallableStackFrame context) {
                MainTest.registerDepth();
                return manager.invokeRecursive(context);
            }
        };
    }
}