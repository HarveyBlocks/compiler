package org.harvey.compiler.common.tree;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultMultipleTreeTest {

    @Test
    public void toStringList() {
        DefaultMultipleTree<String> root = new DefaultMultipleTree<>("A");
        root.addChild(new DefaultMultipleTree<>("B1"));
        DefaultMultipleTree<String> b2 = new DefaultMultipleTree<>("B2");
        root.addChild(b2);
        root.addChild(new DefaultMultipleTree<>("B3"));
        DefaultMultipleTree<String> b4 = new DefaultMultipleTree<>("B4");
        root.addChild(b4);
        DefaultMultipleTree<String> b5 = new DefaultMultipleTree<>("B5");
        root.addChild(b5);
        b2.addChild(new DefaultMultipleTree<>("B2C1"));
        DefaultMultipleTree<String> b2C2 = new DefaultMultipleTree<>("B2C2");
        b2.addChild(b2C2);
        b2.addChild(new DefaultMultipleTree<>("B2C3"));
        DefaultMultipleTree<String> b2C4 = new DefaultMultipleTree<>("B2C4");
        b2.addChild(b2C4);
        b4.addChild(new DefaultMultipleTree<>("B4C1"));
        b4.addChild(new DefaultMultipleTree<>("B4C2"));
        b4.addChild(new DefaultMultipleTree<>("B4C3"));
        DefaultMultipleTree<String> b5C1 = new DefaultMultipleTree<>("B5C1");
        b5.addChild(b5C1);
        b5.addChild(new DefaultMultipleTree<>("B5C2"));
        b2C2.addChild(new DefaultMultipleTree<>("B2C2D1"));
        b2C2.addChild(new DefaultMultipleTree<>("B2C2D2"));
        b2C2.addChild(new DefaultMultipleTree<>("B2C2D3"));
        b2C4.addChild(new DefaultMultipleTree<>("B2C4D1"));
        b2C4.addChild(new DefaultMultipleTree<>("B2C4D2"));
        b2C4.addChild(new DefaultMultipleTree<>("B2C4D3"));
        DefaultMultipleTree<String> b2C4D4 = new DefaultMultipleTree<>("B2C4D4");
        b2C4.addChild(b2C4D4);
        b2C4.addChild(new DefaultMultipleTree<>("B2C4D5"));
        b5C1.addChild(new DefaultMultipleTree<>("B5C1D1"));
        b2C4D4.addChild(new DefaultMultipleTree<>("B2C4D4E1"));
        b2C4D4.addChild(new DefaultMultipleTree<>("B2C4D4E2"));
        b2C4D4.addChild(new DefaultMultipleTree<>("B2C4D4E3"));
        b2C4D4.addChild(new DefaultMultipleTree<>("B2C4D4E4"));
        b2C4D4.addChild(new DefaultMultipleTree<>("B2C4D4E5"));
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : root.toStringList(",", "<", ">")) {
            stringBuilder.append(s);
        }
        System.out.println(stringBuilder);
    }
}