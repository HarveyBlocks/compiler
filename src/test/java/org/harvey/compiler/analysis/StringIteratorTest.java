package org.harvey.compiler.analysis;


import org.harvey.compiler.common.collecction.StringIterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class StringIteratorTest {
    private final String text = "Hello the fucking world";
    private final int textLength = text.length();
    private final char[] back = new char[textLength];
    private final StringIterator it = new StringIterator(text);
    private final char[] fore = text.toCharArray();

    @Before
    public void initBack() {
        for (int i = 0; i < textLength; i++) {
            back[i] = fore[textLength - i - 1];
        }
    }

    @Test
    public void testEach() {
        it.toBegin();
        Character n = it.next();
        it.previous();
        Character npn = it.next();
        Assert.assertEquals(n, npn);
        it.toEnd();
        Character p = it.previous();
        it.next();
        Character pnp = it.previous();
        Assert.assertEquals(p, pnp);
    }

    @Test
    public void testTraverse() {
        it.toBegin();
        char[] test = new char[textLength];
        for (int i = 0; it.hasNext(); i++) {
            test[i] = it.next();
        }
        Assert.assertArrayEquals(test, fore);
        test = new char[textLength];

        for (int i = 0; it.hasPrevious(); i++) {
            test[i] = it.previous();
        }
        Assert.assertArrayEquals(test, back);
        test = new char[textLength];

        for (int i = 0; it.hasNext(); i++) {
            test[i] = it.next();
        }
        Assert.assertArrayEquals(test, fore);
        test = new char[textLength];

        it.toEnd();
        for (int i = 0; it.hasPrevious(); i++) {
            test[i] = it.previous();
        }
        Assert.assertArrayEquals(test, back);
        test = new char[textLength];

        it.toEnd();
        for (int i = 0; it.hasPrevious(); i++) {
            test[i] = it.previous();
        }
        Assert.assertArrayEquals(test, back);
        test = new char[textLength];

        it.toBegin();
        for (int i = 0; it.hasNext(); i++) {
            test[i] = it.next();
        }
        Assert.assertArrayEquals(test, fore);
    }


}