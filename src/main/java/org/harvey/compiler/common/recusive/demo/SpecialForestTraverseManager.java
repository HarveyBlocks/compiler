package org.harvey.compiler.common.recusive.demo;

import org.harvey.compiler.common.recusive.CallableStackFrame;
import org.harvey.compiler.common.recusive.ForCallableStackFrame;
import org.harvey.compiler.common.recusive.RecursiveInvoker;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class SpecialForestTraverseManager implements RecursiveInvoker {
    private final SpecialForestNode root;

    public SpecialForestTraverseManager(SpecialForestNode root) {
        this.root = root;
    }

    public static SpecialForestNode phaseCommand(List<String> result) {
        Stack<SpecialForestNode> stack = new Stack<>();
        for (String s : result) {
            if (s.startsWith("store ")) {
                int id = Integer.parseInt(s.substring("store ".length()));
                stack.push(new SpecialForestNode(id));
            } else if (s.startsWith("children_size ")) {
                int childrenSize = Integer.parseInt(s.substring("children_size ".length()));
                LinkedList<SpecialForestNode> children = new LinkedList<>();
                while (childrenSize-- > 0) {
                    SpecialForestNode top = stack.pop();
                    children.addFirst(top);
                }
                SpecialForestNode target = stack.pop();
                if (!target.getLast().children.isEmpty()) {
                    throw new CompilerException("不正确的command, 重复对children的设置");
                }
                target.getLast().children.addAll(children);
                stack.push(target);
            } else if (s.startsWith("inner")) {
                SpecialForestNode inner = stack.pop();
                SpecialForestNode outer = stack.pop();
                outer.getLast().next = inner;
                stack.push(outer);
            } else {
                throw new CompilerException("不正确的command, 不知道的command类型");
            }
        }
        if (stack.size() == 1) {
            return stack.pop();
        }
        throw new CompilerException("不正确的command, 最终不止一个类");
    }

    @Override
    public CallableStackFrame invokeRecursive(CallableStackFrame context) {
        MainTest.registerDepth();
        ContextFrame contextFrame = (ContextFrame) context;
        contextFrame.add("store " + this.root.id);
        return ContextFrame.newLoopChildren(this.root.children, this.root.next);
    }


    public interface ContextFrame extends CallableStackFrame {
        static CallableStackFrame newNextInvoker(SpecialForestNode rootNext) {
            return new CallableStackFrame() {
                @Override
                public CallableStackFrame invokeRecursive(CallableStackFrame context) {
                    MainTest.registerDepth();
                    CallableStackFrame next = getInvoker().invokeRecursive(context);
                    ((ContextFrame) context).add("inner");
                    return next;
                }

                private RecursiveInvoker getInvoker() {
                    return new SpecialForestTraverseManager(rootNext);
                }
            };
        }

        static CallableStackFrame newLoopChildren(List<SpecialForestNode> children, SpecialForestNode rootNext) {
            return new ForCallableStackFrame() {
                private int index;
                private boolean initialed = false;

                @Override
                public void initial() {
                    index = 0;
                    initialed = true;
                }

                @Override
                public boolean initialed() {
                    return initialed;
                }

                @Override
                public void nextStep() {
                    index++;
                }

                @Override
                public boolean condition() {
                    return index < children.size();
                }

                private RecursiveInvoker getInvoker() {
                    return new SpecialForestTraverseManager(children.get(index));
                }

                @Override
                public CallableStackFrame invokeRecursive(CallableStackFrame context) {
                    MainTest.registerDepth();
                    return getInvoker().invokeRecursive(context);

                }

                @Override
                public CallableStackFrame invokeRecursiveAfterLoop(CallableStackFrame context) {
                    MainTest.registerDepth();
                    if (!children.isEmpty()) {
                        ((ContextFrame) context).add("children_size " + children.size());
                    }
                    if (rootNext == null) {
                        return null;
                    } else {
                        return newNextInvoker(rootNext);
                    }
                }
            };
        }

        List<String> getResult();

        void add(String element);
    }
}

