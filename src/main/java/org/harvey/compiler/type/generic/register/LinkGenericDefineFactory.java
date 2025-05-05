package org.harvey.compiler.type.generic.register;

import org.harvey.compiler.declare.ParamListValidPredicate;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.type.generic.register.command.GenericTypeRegisterCommand;
import org.harvey.compiler.type.generic.register.entity.CommandGenericDefineStruct;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-05 20:01
 */
public class LinkGenericDefineFactory {
    public static final String AND_NAME = SimpleTypeCommandFactory.AND_NAME;
    public static final String GENERIC_LIST_PRE_NAME = SimpleTypeCommandFactory.GENERIC_LIST_PRE_NAME;
    public static final String GENERIC_LIST_POST_NAME = SimpleTypeCommandFactory.GENERIC_LIST_POST_NAME;
    public static final String COMMA_NAME = SimpleTypeCommandFactory.COMMA_NAME;
    public static final String EXTENDS_NAME = Keyword.EXTENDS.getValue();
    public static final String SUPER_NAME = Keyword.SUPER.getValue();
    public static final String MULTIPLE_NAME = Operator.MULTIPLE_TYPE.getName();
    private static final String DEFAULT_ASSIGN_NAME = Operator.ASSIGN.getName();

    /**
     * 校验GenericDefine是否合法
     * 是multiple, 则必须没有default,
     * 只有最后一个可以有multiple
     * default 必须 靠后, 在multiple之前
     *
     * @return {@link ParamListValidPredicate#test(List)}
     */
    public static int validGenericDefine(List<CommandGenericDefineStruct> genericDefine) {
        return ParamListValidPredicate.test(genericDefine);
    }

    public static List<CommandGenericDefineStruct> create(ListIterator<SourceString> sourceIterator) {
        // one end with , start with identifier
        // start with [
        // end with ]
        // [] empty is illegal
        if (!sourceIterator.hasNext()) {
            return null;
        }
        SourceString expectGenericDefineStart = sourceIterator.next();// [
        if (!isGenericDefineStart(expectGenericDefineStart)) {
            sourceIterator.previous();
            return null;
        }
        List<CommandGenericDefineStruct> result = new ArrayList<>();
        while (sourceIterator.hasNext()) {
            CommandGenericDefineStruct struct = createEach(sourceIterator);
            result.add(struct);
            if (!sourceIterator.hasNext()) {
                throw new AnalysisDeclareException(
                        sourceIterator.previous().getPosition(), "expected " + GENERIC_LIST_POST_NAME);
            }
            SourceString expectedComma = sourceIterator.next();
            if (isGenericDefineEnd(expectedComma)) {
                break;
            } else if (!isComma(expectedComma)) {
                throw new AnalysisDeclareException(expectedComma.getPosition(), "expected " + COMMA_NAME);
            }
        }
        return result;
    }


    private static boolean isGenericDefineStart(SourceString source) {
        return source.getType() == SourceType.OPERATOR && GENERIC_LIST_PRE_NAME.equals(source.getValue());
    }

    private static boolean isGenericDefineEnd(SourceString source) {
        return source.getType() == SourceType.OPERATOR && GENERIC_LIST_POST_NAME.equals(source.getValue());
    }

    private static boolean isComma(SourceString source) {
        return source.getType() == SourceType.OPERATOR && COMMA_NAME.equals(source.getValue());
    }

    private static CommandGenericDefineStruct createEach(ListIterator<SourceString> sourceIterator) {
        // id ...? ((extends type(&type)*)*|(super type)) (= default)?

        final IdentifierString identifier = skipIfIdentifier(sourceIterator);
        final SourcePosition multiple = skipIfMultiple(sourceIterator);
        final List<List<GenericTypeRegisterCommand>> uppers = new ArrayList<>();
        List<GenericTypeRegisterCommand> lower = null;

        while (sourceIterator.hasNext()) {
            SourceString next = sourceIterator.next();
            if (isComma(next) || isGenericDefineEnd(next)) {
                sourceIterator.previous();
                return new CommandGenericDefineStruct(identifier, multiple, uppers, lower, null);
            }
            // ((extends type(&type)*)*|(super type)) (= default)?
            // if "extends" -> expect type -> expect type if "&" else continue
            // if "super" -> expect type
            // if "=" -> expect type -> 必须直接comma
            String value = next.getValue();
            if (next.getType() == SourceType.KEYWORD) {
                if (EXTENDS_NAME.equals(value)) {
                    skipThenAddUppers(uppers, sourceIterator);
                    continue;
                } else if (SUPER_NAME.equals(value)) {
                    if (lower != null) {
                        throw new AnalysisDeclareException(next.getPosition(), "Multiple lower is not allowed");
                    }
                    lower = skipOneType(sourceIterator);
                    continue;
                }
            } else if (next.getType() == SourceType.OPERATOR) {
                if (DEFAULT_ASSIGN_NAME.equals(value)) {
                    List<GenericTypeRegisterCommand> defaultType = skipOneType(sourceIterator);
                    // 必须是comma,但是外面会检查
                    return new CommandGenericDefineStruct(identifier, multiple, uppers, lower, defaultType);
                }
            }
            throw new AnalysisDeclareException(next.getPosition(), "unexpected value, expected comma");
        }
        throw new AnalysisDeclareException(
                sourceIterator.previous().getPosition(), "unfinished generic define declare, expect " + COMMA_NAME);
    }


    private static IdentifierString skipIfIdentifier(ListIterator<SourceString> sourceIterator) {
        if (sourceIterator.hasNext()) {
            SourceString expectIdentifier = sourceIterator.next();
            if (expectIdentifier.getType() == SourceType.IDENTIFIER) {
                return new IdentifierString(expectIdentifier);
            }
        }
        throw new AnalysisDeclareException(sourceIterator.previous().getPosition(), "expect identifier");
    }

    private static SourcePosition skipIfMultiple(ListIterator<SourceString> sourceIterator) {
        if (!sourceIterator.hasNext()) {
            return null;
        }
        SourceString expectMultiple = sourceIterator.next();
        if (expectMultiple.getType() != SourceType.OPERATOR || !MULTIPLE_NAME.equals(expectMultiple.getValue())) {
            sourceIterator.previous();
            return null;
        }
        return expectMultiple.getPosition();
    }


    private static void skipThenAddUppers(
            List<List<GenericTypeRegisterCommand>> uppers, ListIterator<SourceString> sourceIterator) {
        // type (& type)*
        List<GenericTypeRegisterCommand> oneType = skipOneType(sourceIterator);
        uppers.add(oneType);
        while (sourceIterator.hasNext()) {
            SourceString expectAnd = sourceIterator.next();
            if (expectAnd.getType() == SourceType.OPERATOR && AND_NAME.equals(expectAnd.getValue())) {
                oneType = skipOneType(sourceIterator);
                uppers.add(oneType);
            } else {
                sourceIterator.previous();
                return;
            }
        }
        throw new AnalysisDeclareException(
                sourceIterator.previous().getPosition(), "unfinished generic define declare, expect " + COMMA_NAME);
    }

    private static List<GenericTypeRegisterCommand> skipOneType(ListIterator<SourceString> sourceIterator) {
        List<GenericTypeRegisterCommand> commands = SimpleTypeCommandFactory.create(sourceIterator);
        if (commands.isEmpty()) {
            throw new AnalysisDeclareException(sourceIterator.previous().getPosition(), "expect at least one type");
        }
        return commands;
    }
}
