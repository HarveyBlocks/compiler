package org.harvey.compiler.analysis.text;

import org.junit.Test;

import java.util.LinkedList;
import java.util.ListIterator;

public class TextDecomposerChainTest {
    @Test
    public void testIterator() {
        LinkedList<String> ll = new LinkedList<>();
        ll.add("A");
        ll.add("B");
        ll.add("C");
        ll.add("D");
        for (ListIterator<String> it = ll.listIterator(); it.hasNext(); ) {
            String next = it.next();
            System.out.println(next);
            it.remove();
            it.add("X");
            it.add("Y");
            it.add("Z");
        }
        System.out.println(ll);
    }

}