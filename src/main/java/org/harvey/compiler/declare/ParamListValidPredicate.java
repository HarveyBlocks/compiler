package org.harvey.compiler.declare;

import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.List;

/**
 * TODO
 * 1. 是multiple, 则必须没有default
 * 2. 只有最后一个可以有multiple
 * 3. default 必须 靠后, 并在multiple之前
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 20:36
 */
public class ParamListValidPredicate {


    public interface Param {
        boolean isMultiple();

        boolean hasDefault();

        SourcePosition getDefaultPosition();

        SourcePosition getMultiple();
    }

    /**
     * @return 严格的, 也就是没有default, 没有multiple的参数个数有几个
     */
    public static int test(List<? extends Param> paramList) {
        boolean canDefault = true;
        int strict = 0;
        for (int i = paramList.size() - 1; i >= 0; i--) {
            Param param = paramList.get(i);
            if (i == paramList.size() - 1) {
                // 最后一个
                if (param.isMultiple() && param.hasDefault()) {
                    // 那完蛋了
                    throw new AnalysisDeclareException(
                            param.getDefaultPosition(), "can not being multiple and having default at the same time");
                }
            } else if (param.isMultiple()) {
                // 不是最后一个不能multiple
                throw new AnalysisDeclareException(param.getMultiple(), "multiple must at last");
            } else if (canDefault && !param.hasDefault()) {
                canDefault = false;
                strict = i + 1;
            } else if (!canDefault && param.hasDefault()) {
                throw new AnalysisDeclareException(
                        param.getDefaultPosition(), "default must at last, but before multiple");
            }
        }
        return strict;
    }
}
