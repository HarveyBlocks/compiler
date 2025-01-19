package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.CompileProperties;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.common.StatementConstant;
import org.harvey.compiler.common.util.ByteUtil;
import org.harvey.compiler.common.util.CharacterUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.ConstantException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.List;

/**
 * 分离Word, Char, Integer, Float
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-18 14:59
 */
public class MixedItemDecomposer {

    private static final char DECIMAL_POINT = SourceFileConstant.DECIMAL_POINT;
    private final String source;
    private final SourcePosition position;


    public MixedItemDecomposer(SourceString item) {
        if (item.getType() != SourceStringType.ITEM) {
            throw new CompilerException("Only item is allowed", new IllegalArgumentException());
        }
        this.source = item.getValue();
        if (source == null || source.isEmpty()) {
            throw new CompilerException("text can't be null or empty", new IllegalArgumentException());
        }
        this.position = item.getPosition();
    }

    private static char getUpperLastChar(String src) {
        return Character.toUpperCase(src.charAt(src.length() - 1));
    }

    private static boolean hasDot(String src) {
        return StringUtil.contains(src, DECIMAL_POINT);
    }

    /**
     * 不检查正确与否, 只确定类型
     */
    public SourceTextContext phase() {
        return new SourceTextContext(List.of(phaseValue()));
    }

    private SourceString phaseValue() {
        if (source.length() == 1 && source.charAt(0) == SourceFileConstant.ITEM_SEPARATE_SIGN) {
            origin(SourceStringType.IGNORE_IDENTIFIER);
        }
        char begin = source.charAt(0);
        // 小数点
        if (CharacterUtil.isDigit(begin) || begin == DECIMAL_POINT) {
            try {
                return phaseAsNumber();
            } catch (NumberFormatException e) {
                throw new ConstantException(position, e.getMessage());
            }
        } else {
            return phaseAsWord();
        }

    }

    /**
     * 是数字忽略source中所有的_
     */
    private String ignoreItemSeparateSign(String source) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c != SourceFileConstant.ITEM_SEPARATE_SIGN) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private SourceString phaseAsNumber() {
        String result = ignoreItemSeparateSign(source);
        int radix = 10;
        if (StringUtil.startsWithIgnoreCase(result, "0X")) {
            radix = 16;
            result = result.substring(2);
        } else if (StringUtil.startsWithIgnoreCase(result, "0O")) {
            radix = 8;
            result = result.substring(2);
        } else if (StringUtil.startsWithIgnoreCase(result, "0B")) {
            radix = 2;
            result = result.substring(2);
        } else if (isFloat(result)) {
            if (isFloat32(result)) {
                result = StringUtil.substring(result, -1);
                float value = Float.parseFloat(result);
                return new SourceString(SourceStringType.FLOAT32, toRawData(Float.floatToRawIntBits(value)), position);
            } else {
                double value = Double.parseDouble(result);
                long raw = Double.doubleToRawLongBits(value);
                return new SourceString(SourceStringType.FLOAT64, toRawData(raw), position);
            }
        }
        //  结尾表明数据类型
        if (isInt64(result)) {
            String number = StringUtil.substring(result, -1);
            // 要以unsigned的形式存储
            byte[] uint8 = ByteUtil.phaseUnsignedInt8(number, radix);
            return new SourceString(SourceStringType.INT64, data2String(uint8), position);
        } else {
            // 要以unsigned的形式存储
            byte[] uint4 = ByteUtil.phaseUnsignedInt4(result, radix);
            return new SourceString(SourceStringType.INT32, data2String(uint4), position);
        }

    }

    private String toRawData(Integer value) {
        return data2String(ByteUtil.toRawBytes(value));
    }

    private String toRawData(Long value) {
        return data2String(ByteUtil.toRawBytes(value));
    }

    private String data2String(byte[] value) {
        return new String(value, CompileProperties.NUMBER_CHARSET);
    }

    private boolean isFloat(String src) {
        return isFloat32(src) || isScientificNotation(src) || hasDot(src);
    }

    private boolean isFloat32(String src) {
        return getUpperLastChar(src) == StatementConstant.FLOAT32_SUFFIX;
    }

    private boolean isInt64(String src) {
        return getUpperLastChar(src) == StatementConstant.INT64_SUFFIX;
    }

    private boolean isScientificNotation(String src) {
        return StringUtil.containsIgnoreCase(src, SourceFileConstant.SCIENTIFIC_NOTATION_LOWER_SIGN);
    }

    private SourceString phaseAsWord() {
        Keyword keyword = Keyword.get(source);
        SourceStringType type = keyword != null ? SourceStringType.KEYWORD : SourceStringType.IDENTIFIER;
        if (type == SourceStringType.KEYWORD) {
            // Keyword进一步判断
            if (Keywords.isBoolConstant(keyword)) {
                // 进一步判断bool值
                type = SourceStringType.BOOL;
            } else if (Keywords.isOperator(keyword)) {
                type = SourceStringType.OPERATOR;
            }
        }
        return origin(type);
    }

    private SourceString origin(SourceStringType type) {
        return new SourceString(type, source, position);
    }
}
