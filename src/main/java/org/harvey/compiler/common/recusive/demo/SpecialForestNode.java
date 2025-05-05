package org.harvey.compiler.common.recusive.demo;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-30 12:53
 */ // 测试, 例子
@Getter
public class SpecialForestNode {
    final int id;
    final List<SpecialForestNode> children = new LinkedList<>();
    @Setter
    SpecialForestNode next;

    SpecialForestNode(int id) {
        this.id = id;
    }

    public String showString() {
        return id +
               (children.isEmpty() ? "" : children.stream()
                       .map(SpecialForestNode::showString)
                       .collect(Collectors.joining(",", "[", "]"))) +
               (next == null ? "" : "." + next.showString());
    }


    public SpecialForestNode getLast() {
        SpecialForestNode cur = this;
        while (cur.next != null) {
            cur = cur.next;
        }
        return cur;
    }
}
