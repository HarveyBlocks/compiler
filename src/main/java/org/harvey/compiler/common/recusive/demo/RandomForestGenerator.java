package org.harvey.compiler.common.recusive.demo;


import org.harvey.compiler.exception.self.CompilerException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 12:52
 */
public class RandomForestGenerator {

    private final List<SpecialForestNode> pool;
    /**
     * 还没用成为children或还没用成为next
     */
    private final LinkedList<SpecialForestNode> noCharacter;
    /**
     * 还没用设置next
     */
    private final List<SpecialForestNode> nextSetAble;
    private final List<SpecialForestNode> childSetAble;
    private final double becomingNextOrChildProportion;
    private final Random random;

    public RandomForestGenerator(int size, double becomingNextOrChildProportion, Random random) {
        this.random = random;
        this.pool = new ArrayList<>(size);
        this.noCharacter = new LinkedList<>();
        this.nextSetAble = new ArrayList<>();
        this.childSetAble = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            SpecialForestNode node = new SpecialForestNode(i);
            this.pool.add(node);
        }
        nextSetAble.add(this.pool.get(0));
        childSetAble.add(this.pool.get(0));
        for (int i = 1; i < size; i++) {
            this.noCharacter.add(this.pool.get(i));
        }
        if (becomingNextOrChildProportion < 0 || becomingNextOrChildProportion > 1) {
            throw new CompilerException("不正确的becomingNextOrChildProportion, 超出合理范围[0,1]");
        }
        this.becomingNextOrChildProportion = becomingNextOrChildProportion;
    }

    public SpecialForestNode build() {
        while (!noCharacter.isEmpty()) {
            SpecialForestNode node = noCharacter.removeFirst();
            while (true) {
                boolean beNext = beNext();
                boolean beChild = beChild();
                if (beNext == beChild) {
                    continue;
                }
                if (beNext) {
                    // 选举出需要被next的
                    getNextSetAbleNode().next = node;
                } else {
                    // 选举出需要被set child的
                    getChildSetTargetNode().children.add(node);
                }
                break;
            }
            // 已经再node0上了, 可以是被加入的对象了
            this.nextSetAble.add(node);
            this.childSetAble.add(node);
        }
        return pool.get(0);
    }

    public boolean beNext() {
        return random.nextDouble() <= becomingNextOrChildProportion;
    }

    public boolean beChild() {
        return random.nextDouble() <= (1 - becomingNextOrChildProportion);
    }

    public SpecialForestNode getNextSetAbleNode() {
        return nextSetAble.remove(random.nextInt(nextSetAble.size()));
    }

    public SpecialForestNode getChildSetTargetNode() {
        return childSetAble.get(random.nextInt(childSetAble.size()));
    }
}
