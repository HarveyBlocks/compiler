package org.harvey.compiler.analysis.text;

import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.text.SimpleTextDecomposer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SimpleTextDecomposerTest {
    private SimpleTextDecomposer decomposer;

    @Before
    public void initDecomposer() {
        decomposer = new SimpleTextDecomposer();
    }

    @Test
    public void testString() {
        decomposer.clear();
        String textCircle = SourceFileConstant.STRING_ENCIRCLE_SIGN + "";
        String textMid = SimpleTextDecomposer.FAKE_STRING_CIRCLE_SIGN.repeat(100);

        decomposer.appendDecomposed(false, textCircle + textMid);
        for (int i = 0; i < 100; i++) {
            decomposer.appendDecomposed(false, textMid);
        }
        decomposer.appendDecomposed(false, textCircle);
        Assert.assertEquals(decomposer.get().size(), 1);

        decomposer.appendDecomposed(false, textCircle + textMid);
        for (int i = 0; i < 20; i++) {
            decomposer.appendDecomposed(false, textMid);
        }
        decomposer.appendDecomposed(false, textCircle);
        Assert.assertEquals(decomposer.get().size(), 2);
    }

    @Test
    public void testWhitespaceInChar() {
        decomposer.clear();
        // Old
        decomposer.appendDecomposed(false, "aaa' ' ");
        Assert.assertEquals(1, decomposer.get().size());
        Assert.assertEquals("aaa' '", decomposer.get().get(0).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        decomposer.clear();
        // Normal
        decomposer.appendDecomposed(false, "aaa' 'bbb ");
        Assert.assertEquals(2, decomposer.get().size());
        Assert.assertEquals("aaa' '", decomposer.get().get(0).getValue());
        Assert.assertEquals("bbb", decomposer.get().get(1).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 6), decomposer.get().get(1).getPosition());
        decomposer.clear();
        decomposer.appendDecomposed(false, "aaa' '");
        Assert.assertEquals(1, decomposer.get().size());
        Assert.assertEquals("aaa' '", decomposer.get().get(0).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        decomposer.clear();
        decomposer.appendDecomposed(false, "' 'bbb ");
        Assert.assertEquals(2, decomposer.get().size());
        Assert.assertEquals("' '", decomposer.get().get(0).getValue());
        Assert.assertEquals("bbb", decomposer.get().get(1).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 3), decomposer.get().get(1).getPosition());
        decomposer.clear();
        // 中道崩殂
        decomposer.appendDecomposed(false, "aaa' ");
        decomposer.appendDecomposed(false, "'bbb ");
        Assert.assertEquals(2, decomposer.get().size());
        Assert.assertEquals("aaa' '", decomposer.get().get(0).getValue());
        Assert.assertEquals("bbb", decomposer.get().get(1).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 6), decomposer.get().get(1).getPosition());
        decomposer.clear();

        decomposer.appendDecomposed(false, " aaa'");
        decomposer.appendDecomposed(false, " 'bbb ");
        Assert.assertEquals(2, decomposer.get().size());
        Assert.assertEquals("aaa' '", decomposer.get().get(0).getValue());
        Assert.assertEquals("bbb", decomposer.get().get(1).getValue());
        Assert.assertEquals(new SourcePosition(0, 1), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 7), decomposer.get().get(1).getPosition());
        decomposer.clear();
        // 其他
        decomposer.appendDecomposed(false, " aaa'");
        decomposer.appendDecomposed(false, "  'bbb ");
        Assert.assertEquals(2, decomposer.get().size());
        Assert.assertEquals("aaa'", decomposer.get().get(0).getValue());
        Assert.assertEquals("'bbb", decomposer.get().get(1).getValue());
        Assert.assertEquals(new SourcePosition(0, 1), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 7), decomposer.get().get(1).getPosition());
        decomposer.clear();

        // 混合
        decomposer.appendDecomposed(false, "000 aaa'");
        decomposer.appendDecomposed(false, "  'bbb ");
        Assert.assertEquals(3, decomposer.get().size());
        Assert.assertEquals("000", decomposer.get().get(0).getValue());
        Assert.assertEquals("aaa'", decomposer.get().get(1).getValue());
        Assert.assertEquals("'bbb", decomposer.get().get(2).getValue());
        Assert.assertEquals(new SourcePosition(0, 0), decomposer.get().get(0).getPosition());
        Assert.assertEquals(new SourcePosition(0, 4), decomposer.get().get(1).getPosition());
        Assert.assertEquals(new SourcePosition(0, 10), decomposer.get().get(2).getPosition());
        decomposer.clear();
    }

    @Test
    public void testEasy() {

    }
}