package org.harvey.compiler.declare.phaser.phaser;

import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.*;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.type.callable.ReturnType;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.common.util.Singleton;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.declare.EmbellishSourceString;
import org.harvey.compiler.declare.context.CallableContext;
import org.harvey.compiler.declare.context.CallableType;
import org.harvey.compiler.declare.phaser.FileStatementContextBuilder;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.*;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 22:27
 */
public class CallablePhaser implements DeclarePhaser<CallableContext> {
    private static final Singleton<CallablePhaser> SINGLETON = new Singleton<>();
    private static final int CAST_OPERATOR = FileStatementContextBuilder.CAST_OPERATOR;
    private static final int CONSTRUCTOR = FileStatementContextBuilder.CONSTRUCTOR;

    private CallablePhaser() {
    }

    public static CallablePhaser instance() {
        return SINGLETON.instance(CallablePhaser::new);
    }

    private static boolean isOperatorIdentifier(int identifierIndex) {
        return FileStatementContextBuilder.idToOper(identifierIndex) != null;
    }

    /**
     * throws 除去的东西要不要const标注?不要? 不能?
     */
    public static List<SourceVariableDeclare.LocalType> phaseThrows(ListIterator<SourceString> iterator) {
        if (!iterator.hasNext()) {
            return Collections.emptyList();
        }
        SourceString exceptedSource = iterator.next();
        if (exceptedSource.getType() != SourceStringType.KEYWORD ||
                !Keyword.THROWS.equals(exceptedSource.getValue())) {
            throw new AnalysisExpressionException(exceptedSource.getPosition(), "excepted throws");
        }
        if (!iterator.hasNext()) {
            throw new AnalysisExpressionException(exceptedSource.getPosition(), "expected a throwable type");
        }
        List<SourceVariableDeclare.LocalType> localTypes = SourceVariableDeclare.phaseTypeList(iterator, null);
        if (localTypes.isEmpty()) {
            throw new AnalysisExpressionException(iterator.previous().getPosition(), "expected a throwable type");
        }
        return localTypes;
    }

    @Override
    public CallableContext phase(Declarable declarable, int identifierIndex, Environment environment) {
        Embellish embellish = phaseEmbellish(declarable.getEmbellish(), environment);
        SourceTextContext permissions = declarable.getPermissions();
        AccessControl accessControl = phasePermission(permissions, environment);
        if (!permissions.isEmpty() && embellish.isMarkedAbstract()) {
            AccessControls.abstractEmbellish(accessControl, permissions.getFirst().getPosition());
        }
        CallableType callableType = phaseCallableType(identifierIndex, environment, embellish, declarable.getStart());
        ListIterator<SourceString> attachmentIterator = declarable.getAttachment().listIterator();

        CallableContext build = new CallableContext.Builder()
                .embellish(embellish)
                .accessControl(accessControl)
                .type(callableType)
                .returnType(callableType == CallableType.CONSTRUCTOR ? ReturnType.NONE :
                        phaseReturnType(declarable.getType()))
                .genericMessage(phaseGenericMessage(attachmentIterator))
                .paramList(phaseParamList(attachmentIterator))
                .throwsList(phaseThrows(attachmentIterator))
                .identifierReference(identifierIndex).build(declarable.getStart());
        if (attachmentIterator.hasNext()) {
            throw new AnalysisExpressionException(attachmentIterator.next().getPosition(), "excepted {");
        }
        return build;
    }

    private CallableType phaseCallableType(
            int identifierIndex, Environment environment,
            Embellish embellish, SourcePosition position) {
        if (CONSTRUCTOR == identifierIndex) {
            return CallableType.CONSTRUCTOR;
        } else if (CAST_OPERATOR == identifierIndex) {
            // 类型转换
            return CallableType.CAST_OPERATOR;
        } else if (isOperatorIdentifier(identifierIndex)) {
            if (embellish.isMarkedStatic()) {
                throw new AnalysisExpressionException(position,
                        "operator callable can not be static! Advising move it to file");
            }
            // 类型转换
            return CallableType.OPERATOR;
        }
        return switchType(environment);
    }

    private CallableType switchType(Environment environment) {
        switch (environment) {
            case FILE:
                return CallableType.FUNCTION;
            case ENUM:
            case CLASS:
            case INTERFACE:
            case STRUCT:
                return CallableType.METHOD;
            default:
                throw new CompilerException("Unknown environment");
        }
    }

    /**
     * @return 如果是单返回值, 就返回null, 如果单返回值有错误, 就抛出异常
     */
    private ReturnType phaseReturnType(SourceTextContext type) {
        if (type == null || type.isEmpty()) {
            throw new CompilerException("callable return type can not be null");
        }

        // 什么样的类型是合法的类型?
        // 单独一个返回值/多个返回值, 多返回值只有一层
        List<SourceTextContext> returnTypes = new ArrayList<>();
        SourceString first = type.getFirst();
        SourceString last = type.getLast();
        boolean firstIsBracketPre =
                first.getType() == SourceStringType.OPERATOR && Operator.BRACKET_PRE.nameEquals(first.getValue());
        boolean lastIsBracketPre = last.getType() == SourceStringType.OPERATOR &&
                Operator.BRACKET_POST.nameEquals(last.getValue());
        if (firstIsBracketPre != lastIsBracketPre) {
            if (!firstIsBracketPre) {
                throw new AnalysisExpressionException(first.getPosition(), ") is not match");
            } else {
                throw new AnalysisExpressionException(last.getPosition(), ") is not match");
            }
        }
        // public static const int func(){
        // }
        // public static (const int) func(){
        // }
        if (!firstIsBracketPre) {// 单个返回值
            assertSingleReturnType(type, null);
            SourceVariableDeclare.LocalType localType = SourceVariableDeclare.localType(type);
            if (localType.isFinal()) {
                throw new AnalysisExpressionException(localType.getFinalMark().getPosition(),
                        "is illegal here");
            }
            return new ReturnType(localType);
        }
        // 多返回值
        ListIterator<SourceString> iterator = type.listIterator();
        iterator.next(); // 忽略第一个(
        SourceTextContext singleType = new SourceTextContext();
        int inGeneric = 0;
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            singleType.add(next);
            if (next.getType() != SourceStringType.OPERATOR) {
                continue;
            }
            if (inGeneric == 0 && Operator.BRACKET_POST.nameEquals(next.getValue())) {
                singleType.removeLast(); // 去)
                assertSingleReturnType(singleType, next.getPosition());
                forbiddenVoid(singleType);
                returnTypes.add(singleType);
                return new ReturnType(
                        returnTypes.stream().map(s -> {
                            SourceVariableDeclare.LocalType localType = SourceVariableDeclare.localType(s);
                            if (localType.isFinal()) {
                                throw new AnalysisExpressionException(localType.getFinalMark().getPosition(),
                                        "is illegal here");
                            }
                            return localType;
                        }).collect(Collectors.toList()));
            } else if (Operator.COMMA.nameEquals(next.getValue())) {
                if (inGeneric != 0) {
                    continue;
                }
                singleType.removeLast();//去,
                assertSingleReturnType(singleType, next.getPosition());
                forbiddenVoid(singleType);
                returnTypes.add(singleType);
                singleType = new SourceTextContext();
                continue;
            } else if (Operator.GENERIC_LIST_PRE.nameEquals(next.getValue())) {
                inGeneric++;
            } else if (Operator.GENERIC_LIST_POST.nameEquals(next.getValue())) {
                inGeneric--;
            } else {
                throw new AnalysisExpressionException(next.getPosition(), "Illegal operator here");
            }
            if (inGeneric < 0) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "Illegal generic match");
            }
        }
        throw new AnalysisExpressionException(last.getPosition(), ") is not match");
    }

    private void forbiddenVoid(SourceTextContext singleType) {
        if (singleType.size() == 1) {
            SourceString first = singleType.getFirst();
            if (Keyword.get(first.getValue()) == Keyword.VOID) {
                throw new AnalysisExpressionException(first.getPosition(), "void is illegal here");
            }
        }

    }

    private void assertSingleReturnType(SourceTextContext type, SourcePosition position) {
        // 可能带泛型
        if (type.isEmpty()) {
            throw new AnalysisExpressionException(position, "empty return type");
        }
        if (type.size() == 1) {
            SourceString first = type.getFirst();
            if (first.getType() == SourceStringType.IDENTIFIER) {
                return;
            }
            if (first.getType() == SourceStringType.KEYWORD && Keywords.isBasicType(first.getValue())) {
                return;
            }
            throw new AnalysisExpressionException(first.getPosition(), "Illegal return type");
        }
        // 泛型
        ListIterator<SourceString> iterator = type.listIterator();
        SourceString first = iterator.next();
        if (first.getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(first.getPosition(), "Illegal return type");
        }
        phaseGenericMessage(iterator);
    }

    private Expression phaseGenericMessage(ListIterator<SourceString> iterator) {
        if (!CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.GENERIC_LIST_PRE))) {
            // 没有`<`开头
            // 认为没有泛型
            return Expression.EMPTY;
        }
        return ExpressionFactory.genericMessage(iterator);
    }

    private List<LocalVariableDeclare> phaseParamList(ListIterator<SourceString> iterator) {
        if (!iterator.hasNext()) {
            throw new CompilerException("iterator can not be empty here");
        }
        if (!CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.CALL_PRE))) {
            // 没有`(`开头
            throw new AnalysisExpressionException(iterator.next().getPosition(),
                    "expected `" + Operator.CALL_PRE.getName() + "`");
        }
        SourceTextContext eachParam = new SourceTextContext();
        List<LocalVariableDeclare> paramList = new ArrayList<>();
        SourcePosition sp = iterator.next().getPosition();// 跳过
        // 思考参数列表
        // 普通(int arg1, int arg2, int arg3)
        // 默认值表达式, 只能出现在普通之后
        // (int arg1, int arg2 = 1, int arg3 = 2)
        // 忽略参数(不写参数名, 函数的调用者需要给出值, 函数的实现者不需要这个参数)
        // 此情况不能给出默认值
        // (int arg1, int _, int arg3 = 2)
        // 不定参数
        // (int arg1, int arg2 = 1, int arg3 = 2, string... messages)
        // <=> (1,2,3,"1","3") <=> (1,3,2,"1","3") <=> (1,3,"1","3")
        // 编译检查之后, 全部编译成->(1,2,3,mul_param[0]="1",mul_param[1]="3")
        // 最终检查之后,
        int tuple = 1;
        while (iterator.hasNext()) {
            if (CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.CALL_PRE))) {
                tuple++;
            } else if (CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.CALL_POST))) {
                tuple--;
                if (tuple == 0) {
                    iterator.next();//忽略最后的)
                    if (!eachParam.isEmpty()) {
                        paramList.add(departParam(eachParam));
                    }
                    sameArgumentCheck(paramList);
                    return paramList;
                }
            } else if (CollectionUtil.nextIs(iterator, ss -> DeclarePhaser.isOperator(ss, Operator.COMMA))) {
                // eachParam 中找
                if (!eachParam.isEmpty()) {
                    paramList.add(departParam(eachParam));
                }
                eachParam = new SourceTextContext();
                continue;
            }
            eachParam.add(iterator.next());
        }
        // 没有`)`??
        throw new AnalysisExpressionException(sp,
                "expected `" + Operator.CALL_POST.getName() + "`");
    }

    private void sameArgumentCheck(List<LocalVariableDeclare> paramList) {
        // 如果是`_`表示忽略, 过
        // 如果是有重复的, 完
        Set<String> argumentPool = new HashSet<>();
        for (LocalVariableDeclare param : paramList) {
            if (param instanceof CallableContext.IgnoreArgument) {
                continue;
            }
            IdentifierString argument = param.getIdentifier();
            String argumentValue = argument.getValue();
            if (SourceFileConstant.IGNORE_IDENTIFIER.equals(argumentValue)) {
                throw new CompilerException(argument.getPosition() + "Unfinished ignore identifier check");
            }
            if (argumentPool.contains(argumentValue)) {
                throw new AnalysisExpressionException(argument.getPosition(), "duplicate parameter names");
            }
            argumentPool.add(argumentValue);
        }
    }

    private LocalVariableDeclare departParam(SourceTextContext eachParam) {
        SourceTextContext type = new SourceTextContext();
        SourceTextContext assign = null;
        int i = 0;
        for (; i < eachParam.size(); i++) {
            SourceString ss = eachParam.get(i);
            if (DeclarePhaser.isOperator(ss, Operator.ASSIGN)) {
                // 其后是assign
                assign = new SourceTextContext();
                assign.add(type.getLast());
                assign.add(ss);
                break;
            }
            type.add(ss);
        }
        if (assign != null) {
            for (; i < eachParam.size(); i++) {
                assign.add(eachParam.get(i));
            }
        }
        // 其前是identifier
        SourceString identifier = type.removeLast();
        if (type.isEmpty()) {
            throw new AnalysisExpressionException(identifier.getPosition(), "argument name is needed here");
        }
        if (identifier.getType() == SourceStringType.IGNORE_IDENTIFIER) {
            return CallableContext.IGNORE_ARGUMENT;
        }
        SourceVariableDeclare.LocalType localType = SourceVariableDeclare.localType(type);
        return new LocalVariableDeclare(localType.isConst(), localType.isFinal(), localType.getSourceType(),
                new IdentifierString(identifier), assign == null ? Expression.EMPTY :
                ExpressionFactory.depart(assign));
    }

    private AccessControl phasePermission(SourceTextContext permissions, Environment environment) {
        switch (environment) {
            case FILE:
                return AccessControls.buildFileAccessControl(permissions, "function", Permission.FILE);
            case ENUM:
            case CLASS:
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
            case STRUCT:
                return AccessControls.buildMemberAccessControl(permissions, Permission.PRIVATE);
            case INTERFACE:
                AccessControl accessControl = AccessControls.buildMemberAccessControl(permissions, Permission.PUBLIC);
                if (!accessControl.canPublic()) {
                    throw new AnalysisExpressionException(permissions.getFirst().getPosition(), permissions.getLast()
                            .getPosition(), "must be public, if you do not want use public, declare as abstract class");
                }
                return accessControl;
            default:
                throw new CompilerException("Unknown environment");
        }
    }

    private Embellish phaseEmbellish(EmbellishSourceString embellish, Environment environment) {
        if (embellish.getFinalMark() != null) {
            throw new AnalysisExpressionException(embellish.getAbstractMark().getPosition(), "illegal in callable");
        }
        Embellish code = new Embellish(embellish);
        if (embellish.getStaticMark() != null) {
            if (embellish.getConstMark() != null) {
                throw new AnalysisExpressionException(embellish.getConstMark().getPosition(), "conflict with static");
            } else if (embellish.getAbstractMark() != null) {
                throw new AnalysisExpressionException(embellish.getAbstractMark().getPosition(),
                        "conflict with static");
            } else if (embellish.getSealedMark() != null) {
                throw new AnalysisExpressionException(embellish.getSealedMark().getPosition(),
                        "conflict with static");
            }
        }
        switch (environment) {
            case FILE:
                DeclarePhaser.forbidden(embellish.getStaticMark());
                DeclarePhaser.forbidden(embellish.getSealedMark());
                DeclarePhaser.forbidden(embellish.getAbstractMark());
                break;
            case ENUM:
                DeclarePhaser.forbidden(embellish.getSealedMark());
                DeclarePhaser.forbidden(embellish.getAbstractMark());
                break;
            case CLASS:
            case STRUCT:
                DeclarePhaser.forbidden(embellish.getAbstractMark());
                break;
            case ABSTRACT_CLASS:
            case ABSTRACT_STRUCT:
                // TODO 要不要考虑STRUCT的继承
            case INTERFACE:
                break;
            default:
                throw new CompilerException("Unknown environment");
        }
        return code;
    }


}

