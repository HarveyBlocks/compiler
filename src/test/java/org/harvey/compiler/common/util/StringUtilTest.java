package org.harvey.compiler.common.util;

import org.junit.Assert;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void split() {
        Assert.assertArrayEquals(new String[]{"A", "B", "C"}, StringUtil.simpleSplit("A--B--C", "--"));
        Assert.assertArrayEquals(new String[]{"A", "B-C"}, StringUtil.simpleSplit("A--B-C", "--"));
        Assert.assertArrayEquals(new String[]{"A", "B", "C", ""}, StringUtil.simpleSplit("A--B--C--", "--"));
        Assert.assertArrayEquals(new String[]{"", "A", "B", "C", ""}, StringUtil.simpleSplit("--A--B--C--", "--"));
        Assert.assertArrayEquals(new String[]{"", "A", "B", "C"}, StringUtil.simpleSplit("--A--B--C", "--"));
    }
}