package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.common.util.StringUtil;
import org.junit.Assert;
import org.junit.Test;

public class MixedItemDecomposerTest {

    @Test
    public void phase() {
        phase("1.0f", SourceStringType.FLOAT32, true);
        phase("1.f", SourceStringType.FLOAT32, true);
        phase(".0f", SourceStringType.FLOAT32, true);
        phase(".f", SourceStringType.FLOAT32, true);
        phase("1.0e10f", SourceStringType.FLOAT32, true);
        phase("1.fe10", SourceStringType.FLOAT32, true);
        phase(".0fe10", SourceStringType.FLOAT32, true);
        phase(".fe10", SourceStringType.FLOAT32, true);
        phase(".", SourceStringType.FLOAT64, false); // 不对
        phase("1.", SourceStringType.FLOAT64, false);
        phase(".1", SourceStringType.FLOAT64, false);
        phase(".1e10", SourceStringType.FLOAT64, false);
        phase("1.E10", SourceStringType.FLOAT64, false);
        phase(".E10", SourceStringType.FLOAT64, false);// 不对
        phase("1.E145140f", SourceStringType.FLOAT32, true);// 不对
        phase("123l", SourceStringType.INT64, true);
        phase("123l", SourceStringType.INT64, true);
        phase("0xffL", SourceStringType.INT64, true);
        phase("0Off", SourceStringType.INT32, true);
    }

    private static void phase(String src, SourceStringType type, boolean removeLast) {
        MixedItemDecomposer decomposer = new MixedItemDecomposer(
                new SourceString(SourceStringType.ITEM, src, new SourcePosition(0, 0)));
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
}