package org.harvey.compiler.type.basic.test5;

import java.util.List;

/**
 * default是我很纠结的一点,
 * 优点:
 *  1. 减少函数的层层嵌套, 减少函数调用导致的资源损耗
 *  2. 减少函数的定义
 * 缺点:
 *  1. 可读性下降(Python的就是一坨)
 *  2. 复杂的编译和运行机制
 * @date 2025-04-01 17:14
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
interface ParameterListMatcher {
    int match(List<ParameterListElementType> from, ParameterListSignature to);

    int legal(ParameterListSignature signature);
}

class DefaultLaterMatcher implements ParameterListMatcher {
    enum MatcherCode {
        SUCCESS,
        SIGNATURE_ILLEGAL,
        EXPECT_MORE_ON_STRICT,
        EXPECT_MORE_ON_DEFAULT,
        CAN_NOT_MATCH_ON_STRICT,
        CAN_NOT_MATCH_ON_DEFAULT,
        CAN_NOT_ASSIGN_ON_MULTIPLY
    }

    @Override
    public int match(List<ParameterListElementType> from, ParameterListSignature to) {
        // strict 就 严格匹配
        // default 就 延后 匹配
        // multiply 就 继续走
        return match0(from, to).ordinal();
    }

    private MatcherCode match0(List<ParameterListElementType> from, ParameterListSignature to) {
        int legal = this.legal(to);
        if (legal != SignatureCheckCode.SUCCESS.ordinal()) {
            return MatcherCode.SIGNATURE_ILLEGAL;
        }
        for (int i = 0, j = 0; i < to.size(); ) {
            ParameterListElement standard = to.get(i);
            if (standard.hasDefault()) {
                // 进入 default 模式
                if (j >= from.size()) {
                    // 之后的都采用默认值
                    return MatcherCode.SUCCESS;
                }
                ParameterListElementType supplier = from.get(j);
                while (true) {
                    if (standard.getType().assignSuccess(supplier)) {
                        break;
                    }
                    // TODO C++/C# 的处理策略是, 删除以下部分, 然后直接返回 CAN_NOT
                    i++;
                    if (i >= to.size()) {
                        return MatcherCode.CAN_NOT_MATCH_ON_DEFAULT;
                    }
                    standard = to.get(i);
                    if (standard.multiply()) {
                        i--; // undo
                        break;
                    }
                }
            } else if (standard.multiply()) {
                // 进入 multiply 模式
                for (; j < from.size(); j++) {
                    ParameterListElementType supplier = from.get(j);
                    if (!standard.getType().assignSuccess(supplier)) {
                        return MatcherCode.CAN_NOT_ASSIGN_ON_MULTIPLY;
                    }
                }
                return MatcherCode.SUCCESS;
            } else {
                // strict 模式
                if (j >= from.size()) {
                    return MatcherCode.EXPECT_MORE_ON_STRICT;
                }
                ParameterListElementType supplier = from.get(j);
                i++;
                j++;
                if (!standard.getType().assignSuccess(supplier)) {
                    return MatcherCode.CAN_NOT_MATCH_ON_STRICT;
                }
            }
        }
        return MatcherCode.SUCCESS;
    }

    enum SignatureCheckCode {
        SUCCESS, STRICT_AFTER_FIRST_DEFAULT, MULTIPLY_NOT_LAST, DEFAULT_MULTIPLY_SAME_TIME;
    }

    @Override
    public int legal(ParameterListSignature signature) {
        return legal0(signature).ordinal();
    }

    private static SignatureCheckCode legal0(ParameterListSignature signature) {
        boolean defaultAppeared = false;
        for (int i = 0; i < signature.size(); i++) {
            ParameterListElement element = signature.get(i);
            if (element.hasDefault() && element.multiply()) {
                return SignatureCheckCode.DEFAULT_MULTIPLY_SAME_TIME;
            }
            if (element.hasDefault()) {
                defaultAppeared = true;
            } else if (element.multiply()) {
                // 是 最后
                if (i + 1 == signature.size()) {
                    break;
                } else {
                    return SignatureCheckCode.MULTIPLY_NOT_LAST;
                }
            } else {
                if (defaultAppeared) {
                    return SignatureCheckCode.STRICT_AFTER_FIRST_DEFAULT;
                }
            }
        }
        return SignatureCheckCode.SUCCESS;
    }
}

interface ParameterListSignature {
    ParameterListElement get(int index);

    int size();
}

interface ParameterListElement {
    boolean multiply();

    boolean hasDefault();

    ParameterListElementType getType();
}

interface ParameterListElementType {
    boolean assignSuccess(ParameterListElementType other);
}