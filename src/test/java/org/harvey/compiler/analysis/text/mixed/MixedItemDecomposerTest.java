package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.mixed.MixedItemDecomposer;
import org.junit.Assert;
import org.junit.Test;

public class MixedItemDecomposerTest {

    private static void phase(String src, SourceType type, boolean removeLast) {
        MixedItemDecomposer decomposer = new MixedItemDecomposer(
                new SourceString(SourceType.ITEM, src, new SourcePosition(0, 0)));
        SourceString sourceString;
        try {
            sourceString = decomposer.phase().get(0);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return;
        }
        if (removeLast) {
            src = StringUtil.substring(src, -1);
        }

        Assert.assertEquals(type, sourceString.getType());
        Assert.assertEquals(src, sourceString.getValue());
    }

    @Test
    public void phase() {
        phase("1.0f", SourceType.FLOAT32, true);
        phase("1.f", SourceType.FLOAT32, true);
        phase(".0f", SourceType.FLOAT32, true);
        phase(".f", SourceType.FLOAT32, true);
        phase("1.0e10f", SourceType.FLOAT32, true);
        phase("1.fe10", SourceType.FLOAT32, true);
        phase(".0fe10", SourceType.FLOAT32, true);
        phase(".fe10", SourceType.FLOAT32, true);
        phase(".", SourceType.FLOAT64, false); // 不对
        phase("1.", SourceType.FLOAT64, false);
        phase(".1", SourceType.FLOAT64, false);
        phase(".1e10", SourceType.FLOAT64, false);
        phase("1.E10", SourceType.FLOAT64, false);
        phase(".E10", SourceType.FLOAT64, false);// 不对
        phase("1.E145140f", SourceType.FLOAT32, true);// 不对
        phase("123l", SourceType.INT64, true);
        phase("123l", SourceType.INT64, true);
        phase("0xffL", SourceType.INT64, true);
        phase("0Off", SourceType.INT32, true);
    }
}