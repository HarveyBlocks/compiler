package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.analysis.Keywords;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.*;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.execute.local.LocalVariableDeclare;
import org.harvey.compiler.execute.local.LocalVariableManager;
import org.harvey.compiler.execute.local.SourceVariableDeclare;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.depart.SimpleDepartedBodyFactory;

import java.util.*;

/**
 * 分解器
 * 思考异常机制的实现过程
 * throw 关键字抛出异常
 * 先观察这个throw 字段所在的区域有没有try-catch
 * 如果没用, 往外走...?
 * <pre>{@code
 * start_try ->注册一个label用来给trow goto的
 * 在某一时刻遇到了throw
 * set_e
 * goto try_label;
 * try_label: -> 进入catch(RuntimeException|Exception|Exception e)
 * if_catch_type_instance_goto RuntimeException +2
 * if_catch_type_instance_goto Exception        +1
 * ifn_catch_type_instance_goto Exception       +3
 * build_init_param MyException->进入成果捕获的catch块
 * set_param e_pop
 * init MyException
 * goto +?
 * -> 此时进入第二个catch
 * ifn_catch_type_instance_goto +3
 * ...
 * goto+?
 * 都没用catch住
 * finally块
 * ...
 * 都没用catch住
 * if_e_has_throw 返回上一级, 丢弃当前函数栈, 返回上一个栈,
 * get_label 然后找外层函数的try的label块
 * }</pre>
 * try的label应该存储在一个栈里, 顶层的label下的都catch不住, 就用栈中下一个元素的
 * 如果都catch不住, 抛弃当前函数栈, 返回上一个函数栈
 * 注册try_label的时候, 注册绝对命令地址, 然后就可以直接goto了
 * 现在思考, 如何实现finally块在return之前执行
 * 创建return, 如果在try{}块里, 就设计成要先去final, 再return的逻辑
 * finally可以有一个finally end的命令, 会向外寻找finally ,一般不执行, 发现有return在, 就回去执行return
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 17:47
 */
@AllArgsConstructor
public class ExecutableBodyFactory {

    public static final int NO_BODY_REFERENCE = -1;
    private final ArrayList<UnfinishedExecutable> unfinishedPool = new ArrayList<>();
    /**
     * 一个文件一个pool
     */
    @Getter
    private final ArrayList<ExecutableBody> pool;

    /**
     * @param executableBody 有start和end比较好
     */
    private static ExecutableBody toSequential(
            SourceTextContext executableBody, IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        if (executableBody.isEmpty()) {
            throw new CompilerException("empty executable body is illegal");
        }
        ExecutableBody result = new ExecutableBody(executableBody.getFirst().getPosition());
        ListIterator<SourceString> bodyIt = executableBody.listIterator();
        Stack<BodyStartReference> breakableStack = new Stack<>();
        Stack<BodyStartReference> bodyStartStack = new Stack<>();
        while (bodyIt.hasNext()) {
            SourceString next = bodyIt.next();
            if (next.getType() == SourceType.SIGN) {
                if (SimpleDepartedBodyFactory.BODY_END.equals(next.getValue())) {
                    // body end
                    SourcePosition endPosition = next.getPosition();
                    checkSingleLineStartAtBodyEnd(bodyStartStack, endPosition, result, localVariableManager);
                    continue;
                }
                if (SimpleDepartedBodyFactory.BODY_START.equals(next.getValue())) {
                    // body start
                    BodyStart bodyStart = new BodyStart();
                    localVariableManager.intoBody();
                    result.add(bodyStart);
                    bodyStartStack.push(new BodyStartReference(result.size() - 1));
                    continue;
                }
                if (SimpleDepartedBodyFactory.SENTENCE_END.equals(next.getValue())) {
                    // sentence end
                    continue;
                }
            }

            if (!Keywords.isControlStructure(next.getValue())) {
                // bodyIt.next()
                // break, continue;
                // return
                bodyIt.previous();

                boolean returnInLine = false;
                if (Keyword.get(next.getValue()) == Keyword.RETURN) {
                    bodyIt.next();
                    returnInLine = true;
                }
                phaseAsSentence(bodyIt, result, returnInLine, identifierManager, localVariableManager);
                checkSingleLineStartAtSentenceEnd(bodyStartStack, result, localVariableManager);
                continue;
            }

            Keyword keyword = Keyword.get(next.getValue());
            SourcePosition position = next.getPosition();
            if (keyword == Keyword.CONTINUE || keyword == Keyword.BREAK) {
                // 检查下一个一定是;
                if (!bodyIt.hasNext()) {
                    throw new AnalysisExpressionException(position, "expected ;");
                }
                SourceString afterKey = bodyIt.next();
                if (afterKey.getType() != SourceType.SIGN &&
                    !SimpleDepartedBodyFactory.SENTENCE_END.equals(afterKey.getValue())) {
                    throw new AnalysisExpressionException(afterKey.getPosition(), "expected ;");
                }
                result.add(checkInCirculateBody(breakableStack, keyword, position, result));
                continue;
            }
            int preBodyStart = getPreBodyStart(result);
            BodyStart start = getPreBodyStart(result, preBodyStart);
            Executable executable = phaseControlAndScope(
                    identifierManager, localVariableManager, keyword, bodyIt, start, preBodyStart, position);
            result.add(executable);
            int executableIndex = result.size() - 1;
            if (executable instanceof Case || executable instanceof Default) {
                registerSwitch(outerSwitch(breakableStack, result), executable, executableIndex, position);
                continue;
            }
            pushToStack(executable, executableIndex, bodyIt, bodyStartStack, breakableStack);
        }
        return result;
    }

    private static Executable phaseControlAndScope(
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager, Keyword keyword,
            ListIterator<SourceString> bodyIt, BodyStart start, int preBodyStart,
            SourcePosition position) {
        Executable control = phaseControl(
                keyword, bodyIt, start, preBodyStart, position, identifierManager, localVariableManager);
        return dealScope(control, localVariableManager);
    }

    private static BodyStart getPreBodyStart(ExecutableBody result, int index) {
        if (index < 0) {
            return null;
        }
        Executable executable = result.get(index);
        if (executable instanceof BodyStart) {
            return (BodyStart) executable;
        } else {
            return null;
        }
    }

    // 先对控制结构进行分解
    // 1. 语句, ;结尾
    // 2. 控制结构, if开头
    //    while开头
    //      do开头
    //    for开头
    //    switch开头
    // 都有关键字开头
    // ... 嵌套, 为之奈何?
    // if(){
    //      if(){
    //
    //      }
    // }

    private static SwitchStart outerSwitch(Stack<BodyStartReference> breakableStart, ExecutableBody result) {
        if (breakableStart.empty()) {
            return null;
        }
        BodyStart peek = getBodyStart(result, breakableStart.peek());
        return peek instanceof SwitchStart ? (SwitchStart) peek : null;
    }

    private static void registerSwitch(
            SwitchStart switchStart, Executable executable, int executableIndex,
            SourcePosition position) {
        if (switchStart == null) {
            throw new AnalysisExpressionException(position, "should collectionIn switch");
        }
        if (executable instanceof Case) {
            switchStart.addCase(executableIndex);
        } else if (switchStart.getDefaultPlaceholder() == SwitchStart.NO_DEFAULT) {
            switchStart.setDefaultPlaceholder(executableIndex);
        } else {
            throw new AnalysisExpressionException(position, "repeated default");
        }
    }

    private static void pushToStack(
            Executable executable, int executableIndex, ListIterator<SourceString> bodyIt,
            Stack<BodyStartReference> bodyStartStack,
            Stack<BodyStartReference> breakableStart) {
        if (!(executable instanceof BodyStart)) {
            return;
        }
        BodyStart bodyStart = (BodyStart) executable;
        bodyStartStack.push(wrapSingleLineIfNeeded(bodyStart, executableIndex, bodyIt));
        if (isBreakable(bodyStart)) {
            breakableStart.push(new BodyStartReference(executableIndex));
        }
    }

    private static boolean isBreakable(BodyStart bodyStart) {
        return bodyStart instanceof DoStart || bodyStart instanceof ForIndexStart ||
               bodyStart instanceof ForEachStart || bodyStart instanceof WhileStart;
    }

    /**
     * @param result 只读最后
     */
    private static int getPreBodyStart(ExecutableBody result) {
        if (result.isEmpty()) {
            return -1;
        }
        Executable last = result.get(result.size() - 1);
        if (!(last instanceof BodyEnd)) {
            return -1;
        }
        return ((BodyEnd) last).getStart();

    }

    /**
     * @return {@link Continue} or {@link Break}
     */
    private static Executable checkInCirculateBody(
            Stack<BodyStartReference> breakableStart, Keyword keyword,
            SourcePosition position, ExecutableBody result) {
        if (breakableStart.empty()) {
            throw new AnalysisExpressionException(position, keyword.getValue() + " expected collectionIn a circulate");
        }
        BodyStartReference peek = breakableStart.peek();
        // break 也有可能是...switch..., 可知是switch先还是while先? 未可知也
        if (keyword == Keyword.BREAK) {
            return new Break(peek.reference);
        } else if (keyword != Keyword.CONTINUE) {
            throw new CompilerException("only break and continue can be argument", new IllegalArgumentException());
        }
        Stack<BodyStartReference> temp = new Stack<>();
        Executable executable = null;
        while (!breakableStart.empty()) {
            BodyStartReference popIndex = breakableStart.pop();
            BodyStart pop = getBodyStart(result, popIndex);
            if (pop instanceof SwitchStart) {
                temp.push(popIndex);
                continue;
            }
            // 是循环了
            executable = new Continue(popIndex.reference);
            breakableStart.push(popIndex);
            while (!temp.empty()) {
                // 回去
                breakableStart.push(temp.pop());
            }
            break;
        }
        if (executable == null) {
            throw new AnalysisExpressionException(position, keyword.getValue() + " expected collectionIn a circulate");
        }
        return executable;
    }

    /**
     * @param bodyIt 跳过{ 如果有
     */
    private static BodyStartReference wrapSingleLineIfNeeded(
            BodyStart bodyStart, int bodyStartIndex,
            ListIterator<SourceString> bodyIt) {
        if (!canWrapSingleLine(bodyStart)) {
            return new BodyStartReference(bodyStartIndex);
        }
        if (CollectionUtil.nextIs(
                bodyIt, ss -> ss.getType() == SourceType.SIGN &&
                              SimpleDepartedBodyFactory.BODY_START.equals(ss.getValue()))) {
            bodyIt.next();
            return new BodyStartReference(bodyStartIndex);
        }
        return new SingleLineStart(bodyStartIndex);
    }

    private static void checkSingleLineStartAtBodyEnd(
            Stack<BodyStartReference> bodyStartStack,
            SourcePosition endPosition, ExecutableBody result,
            LocalVariableManager localVariableManager) {
        if (bodyStartStack.empty()) {
            throw new AnalysisExpressionException(endPosition, "Illegal body end, excepted body start");
        }
        BodyStartReference top = bodyStartStack.pop();
        if (top instanceof SingleLineStart) {
            throw new AnalysisExpressionException(endPosition, "Illegal body end, excepted body start");
        }
        BodyEnd end = new BodyEnd(top.reference);
        localVariableManager.leaveBody();
        result.add(end);
        getBodyStart(result, top).setBodyEnd(result.size() - 1);
        while (!bodyStartStack.empty()) {
            // 还有? 就搞
            BodyStartReference pop = bodyStartStack.pop();
            if (!(pop instanceof SingleLineStart)) {
                // 没有了? 不搞了
                bodyStartStack.push(pop);
                break;
            }
            BodyStart value = getBodyStart(result, pop);
            end = new BodyEnd(pop.reference);
            localVariableManager.leaveBody();
            result.add(end);
            value.setBodyEnd(result.size() - 1);
        }
    }

    private static BodyStart getBodyStart(ExecutableBody result, BodyStartReference top) {
        Executable executable = result.get(top.reference);
        if (!(executable instanceof BodyStart)) {
            throw new CompilerException("index not define to body start");
        }
        return (BodyStart) executable;
    }

    /**
     * @param result 只加最后
     */
    private static void checkSingleLineStartAtSentenceEnd(
            Stack<BodyStartReference> bodyStartStack,
            ExecutableBody result,
            LocalVariableManager localVariableManager) {
        while (!bodyStartStack.empty()) {
            // 还有? 就搞
            BodyStartReference pop = bodyStartStack.pop();
            if (!(pop instanceof SingleLineStart)) {
                // 没有了? 不搞了
                bodyStartStack.push(pop);
                break;
            }
            BodyStart value = getBodyStart(result, pop);
            BodyEnd end = new BodyEnd(pop.reference);
            localVariableManager.leaveBody();
            result.add(end);
            value.setBodyEnd(result.size() - 1);
        }
    }

    /**
     * @param result       只加
     * @param returnInLine 关键字是return
     */
    private static void phaseAsSentence(
            ListIterator<SourceString> bodyIt, ExecutableBody result, boolean returnInLine,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        SourceTextContext sentence = skipToSentenceEnd(bodyIt);
        if (sentence == null || sentence.isEmpty()) {
            return;
        }
        SourceVariableDeclare declare = SourceVariableDeclare.create(sentence);
        if (declare == null) {
            // 表达式
            if (!sentence.isEmpty()) {
                Expression nextExpression = ExpressionFactory.simplyMapToExpression(sentence,
                        localVariableManager
                );
                result.add(returnInLine ? new Return(nextExpression) : new ExpressionExecutable(nextExpression));
                return;
            }
            if (returnInLine) {
                result.add(new Return());
            }
            return;
        }
        if (returnInLine) {
            throw new AnalysisExpressionException(
                    declare.getIdentifier().getPosition(),
                    "can not declare after return"
            );
        }
        // 多个声明
        LocalVariableDeclare variableTobeDeparted = new LocalVariableDeclare(declare, identifierManager,
                localVariableManager
        );
        ArrayList<Pair<IdentifierString, Expression>> depart = LocalVariableDeclare.departAssign(
                variableTobeDeparted.getAssign(), identifierManager, localVariableManager);
        LocalType type = variableTobeDeparted.getType();
        for (Pair<IdentifierString, Expression> each : depart) {
            result.add(new DeclareExecutable(type, each.getKey()));
            Expression assign = each.getValue();
            if (assign != null && !assign.isEmpty()) {
                result.add(new ExpressionExecutable(assign));
            }
        }
    }

    private static SourceString getConst(SourceString first) {
        if (first.getType() != SourceType.KEYWORD) {
            return null;
        }
        if (Keyword.CONST.equals(first.getValue())) {
            return first;
        }
        return null;
    }


    /**
     * @param bodyIt 不含;
     */
    public static SourceTextContext skipToSentenceEnd(ListIterator<SourceString> bodyIt) {
        if (!bodyIt.hasNext()) {
            return SourceTextContext.empty();
        }
        SourceTextContext sentence = new SourceTextContext();
        while (bodyIt.hasNext()) {
            SourceString next = bodyIt.next();
            if (next.getType() == SourceType.SIGN &&
                SimpleDepartedBodyFactory.SENTENCE_END.equals(next.getValue())) {
                return sentence;
            } else {
                sentence.add(next);
            }
        }
        throw new AnalysisExpressionException(bodyIt.previous().getPosition(), "expected ;");
    }

    private static boolean canWrapSingleLine(BodyStart bodyStart) {
        if (bodyStart == null) {
            return false;
        }
        return bodyStart instanceof IfStart || bodyStart instanceof ElseIfStart || bodyStart instanceof ElseStart ||
               bodyStart instanceof DoStart || bodyStart instanceof WhileStart || bodyStart instanceof ForIndexStart ||
               bodyStart instanceof ForEachStart || bodyStart instanceof TryStart || bodyStart instanceof CatchStart ||
               bodyStart instanceof FinallyStart;
    }

    /**
     * if-elseif-else
     * switch-case-default
     * do-while
     * while
     * for-i
     * for-each
     * try-catch-finally 原理类似if-elseif-else
     * 1. 对控制结构的关键字进行处理
     * 2. 没用被处理为控制结构的, 看作可执行语句, 一直分解直到;
     * 3. 对于语句, 判别是否含有声明, 含有声明的, 吧声明和赋值分开
     * case 于 switch 内, 可于往前找一个BodyStart, 必须是switch(如果是BodyEnd, 就找对应的BodyStart)
     * switch{   {{{}{}}{}}case{{}{{}{{}}{}}}    case
     * 目 标  <-         可以存在             <- 寻找起点
     * elif/else前必是bodyEnd, 找此bodyEnd的bodyStart
     * while是找自己的body
     * <p>
     * <p>
     * 如果是while-end, 会需要一个do-start, 怎么办呢?
     * case和default需要加入到switch的字段里, 要怎么办呢?
     *
     * @param keyword       控制结构的关键字
     * @param bodyIt        当前指向关键字后面, 后来移动到condition的)后面, 如果有的话, 如果还要{, 还会往后到{
     * @param preStart      nullable, 可能当前需要, 例如else之前一定需要if, while之前需要do(如果有), catch之前需要try等等
     *                      且脱去SingleLine的封装
     * @param preStartIndex preStart的index
     * @return 如果是single line, 会封装到single line
     */
    private static Executable phaseControl(
            Keyword keyword, ListIterator<SourceString> bodyIt, BodyStart preStart,
            int preStartIndex, SourcePosition controlPosition,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        switch (keyword) {
            case IF:
                return new IfStart(ExpressionFactory.simplyMapToExpression(skipCondition(bodyIt),
                        localVariableManager
                ));
            case ELSE:
                return dealElse(bodyIt, preStart, controlPosition, localVariableManager);
            case SWITCH:
                return new SwitchStart(ExpressionFactory.simplyMapToExpression(skipCondition(bodyIt),
                        localVariableManager
                ));
            case CASE:
                // 到:之后, 这个:是没有被?匹配到的:
                // a?b:c?d?e:f:g
                return new Case(ExpressionFactory.simplyMapToExpression(skipCaseCondition(bodyIt),
                        localVariableManager
                ));
            case DEFAULT:
                SourceTextContext condition = skipCaseCondition(bodyIt);
                if (condition.isEmpty()) {
                    return new Default();
                }
                throw new AnalysisExpressionException(condition.getFirst().getPosition(),
                        condition.pollLast().getPosition(), "default do not need condition"
                );
            case DO:
                return new DoStart();
            case WHILE:
                return dealWhile(bodyIt, preStart, preStartIndex, localVariableManager);
            case FOR:
                localVariableManager.intoBody();
                return buildForStartAndDealScope(skipForCondition(bodyIt), identifierManager, localVariableManager);
            case TRY:
                return new TryStart();
            case CATCH:
                if (!(preStart instanceof TryStart)) {
                    throw new AnalysisExpressionException(controlPosition, "expected after try");
                }
                return buildCatchStart(skipCondition(bodyIt));
            case FINALLY:
                if (!(preStart instanceof TryStart) && !(preStart instanceof CatchStart)) {
                    throw new AnalysisExpressionException(controlPosition, "expected after try or catch");
                }
                return new FinallyStart();
            default:
                throw new CompilerException(keyword + "is not a version2 structure");
        }
    }

    private static Executable dealScope(Executable executable, LocalVariableManager localVariableManager) {
        if (executable instanceof ForIndexStart || executable instanceof ForEachStart) {
            return executable;
        }
        if (executable instanceof BodyStart) {
            localVariableManager.intoBody();
        } else if (executable instanceof BodyEnd) {
            localVariableManager.leaveBody();
        }
        return executable;
    }

    /**
     * @return {@link ElseStart} or {@link ElseIfStart}
     */
    private static Executable dealElse(
            ListIterator<SourceString> bodyIt, BodyStart preStart,
            SourcePosition controlPosition,
            LocalVariableManager localVariableManager) {
        if (!(preStart instanceof IfStart) && !(preStart instanceof ElseIfStart)) {
            throw new AnalysisExpressionException(controlPosition, "expected after if");
        }
        // 考虑elseif和else
        boolean nextIsIf = CollectionUtil.nextIs(
                bodyIt,
                ss -> ss.getType() == SourceType.KEYWORD && Keyword.IF.equals(ss.getValue())
        );
        if (nextIsIf) {
            bodyIt.next();
            Expression condition = ExpressionFactory.simplyMapToExpression(skipCondition(bodyIt),
                    localVariableManager
            );
            return new ElseIfStart(condition);
        } else {
            return new ElseStart();
        }
    }

    /**
     * @return {@link WhileStart} or {@link WhileEnd}
     */
    private static Executable dealWhile(
            ListIterator<SourceString> bodyIt, BodyStart preStart, int preStartIndex,
            LocalVariableManager localVariableManager) {
        Expression condition = ExpressionFactory.simplyMapToExpression(skipCondition(bodyIt),
                localVariableManager
        );
        if (!(preStart instanceof DoStart)) {
            return new WhileStart(condition);
        }
        // assert next is ;
        SourceString next = bodyIt.next();
        if (!(next.getType() == SourceType.SIGN &&
              SimpleDepartedBodyFactory.SENTENCE_END.equals(next.getValue()))) {
            throw new AnalysisExpressionException(next.getPosition(), "expected ;");
        }
        bodyIt.previous();
        return new WhileEnd(preStartIndex, condition);
    }

    // 辅助函数
    private static SourceTextContext skipCaseCondition(ListIterator<SourceString> bodyIt) {
        // case condition, 到: 为止
        int inConditionExpression = 1;
        SourceTextContext part = new SourceTextContext();
        while (!bodyIt.hasNext()) {
            SourceString next = bodyIt.next();
            String value = next.getValue();
            part.add(next);
            SourceType type = next.getType();
            if (type == SourceType.SIGN) {
                throw new AnalysisExpressionException(next.getPosition(), "illegal here");
            }
            if (type != SourceType.OPERATOR) {
                continue;
            }
            if (Operator.CONDITION_CHECK.nameEquals(value)) {
                inConditionExpression++;
            } else if (Operator.CONDITION_DECIDE.nameEquals(value)) {
                inConditionExpression--;
                if (inConditionExpression == -1) {
                    part.removeLast();
                    if (part.isEmpty()) {
                        throw new AnalysisExpressionException(next.getPosition(), "expected a using");
                    }
                    return part;
                }
            }
            if (inConditionExpression < 0) {
                throw new AnalysisExpressionException(next.getPosition(), "illegal match");
            }
        }
        throw new AnalysisExpressionException(bodyIt.previous().getPosition(), "expected )");
    }

    /**
     * @return {@link ForIndexStart} or {@link ForEachStart};
     */
    private static BodyStart buildForStartAndDealScope(
            List<SourceTextContext> conditions,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        // TODO declare 系统的完善
        if (conditions.isEmpty()) {
            throw new CompilerException("expected a using");
        }
        if (conditions.size() == 2) {
            SourceTextContext each = conditions.get(0);
            if (each.size() < 2) {
                throw new AnalysisExpressionException(each.remove().getPosition(), "expected type identifier");
            }
            SourceString eachIdentifier = each.removeLast();
            LocalType eachType = SourceVariableDeclare.localType(each);
            SourceTextContext context = conditions.get(1);
            return new ForEachStart(eachType, new IdentifierString(eachIdentifier),
                    ExpressionFactory.simplyMapToExpression(context, localVariableManager)
            );
        } else if (conditions.size() == 3) {
            SourceTextContext init = conditions.get(0);
            Expression condition = ExpressionFactory.simplyMapToExpression(conditions.get(1),
                    localVariableManager
            );
            Expression step = ExpressionFactory.simplyMapToExpression(conditions.get(2),
                    localVariableManager
            );
            SourceVariableDeclare declare = SourceVariableDeclare.create(init);
            if (declare == null) {
                // 是表达式
                return new ForIndexStart(
                        ExpressionFactory.simplyMapToExpression(init, localVariableManager),
                        condition, step
                );
            } else {
                // for(declare;;){}
                LocalVariableDeclare localVariableDeclare = new LocalVariableDeclare(declare, identifierManager,
                        localVariableManager
                );
                return new ForIndexStart(localVariableDeclare.getType(),
                        ExpressionFactory.simplyMapToExpression(localVariableDeclare.getAssign(),
                                localVariableManager
                        ), condition, step
                );
            }
        } else {
            throw new AnalysisExpressionException(conditions.get(0).getLast().getPosition(), "Delimiter conflicts");
        }
    }

    private static CatchStart buildCatchStart(SourceTextContext catchCondition) {
        // catchCondition->|分割的types, 然后一个identifier
        SourceString last = catchCondition.removeLast();
        SourceType lastType = last.getType();
        ArrayList<LocalType> types = new ArrayList<>();
        // 异常类型
        // catch const的, 能把非const的catch住
        SourceTextContext part = new SourceTextContext();
        for (SourceString each : catchCondition) {
            // isCatchSeparator
            if (isCatchSeparator(each)) {
                if (part.isEmpty()) {
                    throw new AnalysisExpressionException(each.getPosition(), "Exception type can not be empty");
                }
                DeclarableFactory.enumConstantPhase(part);
                types.add(SourceVariableDeclare.localType(part));
            } else {
                part.add(each);
            }
        }
        if (part.isEmpty()) {
            throw new AnalysisExpressionException(last.getPosition(), "expected a identifier");
        }
        types.add(SourceVariableDeclare.localType(part));
        if (lastType == SourceType.IDENTIFIER) {
            return new CatchStart(types, new IdentifierString(last.getPosition()));
        } else if (lastType == SourceType.IGNORE_IDENTIFIER) {
            return new CatchStart(types, new IdentifierString(last.getPosition(), last.getValue()));
        } else {
            throw new AnalysisExpressionException(last.getPosition(), "expected a identifier");
        }
    }

    private static boolean isCatchSeparator(SourceString each) {
        return each.getType() == SourceType.OPERATOR && Operator.BITWISE_OR.nameEquals(each.getValue());
    }

    /**
     * @param bodyIt next指向(
     * @return 无前后()
     */
    private static SourceTextContext skipCondition(ListIterator<SourceString> bodyIt) {
        skipPreParentheses(bodyIt);
        int inTuple = 1;
        int inBody = 0;
        SourceTextContext part = new SourceTextContext();
        while (!bodyIt.hasNext()) {
            SourceString next = bodyIt.next();
            String value = next.getValue();
            part.add(next);
            SourceType type = next.getType();
            if (inBody == 0 && type == SourceType.OPERATOR) {
                if (Operator.PARENTHESES_PRE.nameEquals(value)) {
                    inTuple++;
                } else if (Operator.PARENTHESES_POST.nameEquals(value)) {
                    inTuple--;
                    if (inTuple == 0) {
                        part.removeLast();
                        if (part.isEmpty()) {
                            throw new AnalysisExpressionException(next.getPosition(), "expected a using");
                        }
                        return part;
                    }
                }
                if (inTuple < 0) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal match");
                }
            } else if (type == SourceType.SIGN) {
                switch (value) {
                    case SimpleDepartedBodyFactory.BODY_START:
                        inBody++;
                        break;
                    case SimpleDepartedBodyFactory.BODY_END:
                        inBody--;
                        break;
                    case SimpleDepartedBodyFactory.SENTENCE_END:
                        if (inBody == 0) {
                            throw new AnalysisExpressionException(next.getPosition(), "illegal here");
                        }
                }
                if (inBody < 0) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal match");
                }
            }

        }
        throw new AnalysisExpressionException(bodyIt.previous().getPosition(), "expected )");
    }

    private static List<SourceTextContext> skipForCondition(ListIterator<SourceString> bodyIt) {
        // 如果是; 就停下, 如果是in 就停下, 如果即由, 就报错
        // 如果遇到在for的第一个declare的lambda的呢?
        // 由于要找:和; , 如果遇到了bodyStart, 就忽略其中一切的;和,
        skipPreParentheses(bodyIt);
        int inTuple = 1; //
        int inBody = 0;
        List<SourceTextContext> result = new ArrayList<>();
        SourceTextContext part = new SourceTextContext();
        SourcePosition in = SourcePosition.UNKNOWN;
        boolean foreach = false;
        boolean forIndex = false;
        while (!bodyIt.hasNext()) {
            SourceString next = bodyIt.next();
            String value = next.getValue();
            part.add(next);
            SourceType type = next.getType();
            if (inBody == 0 && type == SourceType.OPERATOR) {
                if (Operator.PARENTHESES_PRE.nameEquals(value)) {
                    inTuple++;
                } else if (Operator.PARENTHESES_POST.nameEquals(value)) {
                    inTuple--;
                    if (inTuple == 0) {
                        part.removeLast();
                        // 可empty
                        if (foreach && part.isEmpty()) {
                            throw new AnalysisExpressionException(next.getPosition(), "expected a iterable");
                        }
                        result.add(part);
                        assertLegalForDelimiter(foreach, forIndex, result, in, next.getPosition());
                        return result;
                    }
                } else if (Operator.IN.nameEquals(value)) {
                    if (forIndex) {
                        throw new AnalysisExpressionException(next.getPosition(), "Delimiter conflicts");
                    }
                    if (foreach) {
                        throw new AnalysisExpressionException(next.getPosition(), "Delimiter repeated");
                    }
                    foreach = true;
                    in = next.getPosition();
                    part.removeLast();
                    if (part.isEmpty()) {
                        // 不可empty
                        throw new AnalysisExpressionException(next.getPosition(), "expected a using");
                    }
                    result.add(part);
                    part = new SourceTextContext();
                }
                if (inTuple < 0) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal match");
                }
            } else if (type == SourceType.SIGN) {
                switch (value) {
                    case SimpleDepartedBodyFactory.BODY_START:
                        inBody++;
                        break;
                    case SimpleDepartedBodyFactory.BODY_END:
                        inBody--;
                        break;
                    case SimpleDepartedBodyFactory.SENTENCE_END:
                        if (foreach) {
                            throw new AnalysisExpressionException(next.getPosition(), "Delimiter conflicts");
                        }
                        forIndex = true;
                        if (inBody == 0) {
                            part.removeLast();
                            // 可empty
                            result.add(part);
                            part = new SourceTextContext();
                        }
                        break;
                }
                if (inBody < 0) {
                    throw new AnalysisExpressionException(next.getPosition(), "illegal match");
                }
            }

        }
        throw new AnalysisExpressionException(bodyIt.previous().getPosition(), "expected )");
    }

    private static void assertLegalForDelimiter(
            boolean foreach, boolean forIndex, List<SourceTextContext> result,
            SourcePosition in, SourcePosition next) {
        if (foreach == forIndex) {
            throw new AnalysisExpressionException(next, "Delimiter conflicts");
        }
        int size = result.size();
        if (foreach && size != 2) {
            throw new AnalysisExpressionException(next, "expected one 'collectionIn'");
        } else if (forIndex && size != 3) {
            throw new AnalysisExpressionException(next, "expected tow ';'");
        }
        if (size == 2) {
            // for each
            if (result.get(0).size() < 2) {
                throw new AnalysisExpressionException(in, "expected each type and identifier");
            }
            if (result.get(1).isEmpty()) {
                throw new AnalysisExpressionException(next, "expected a iterator");
            }
        }
    }

    private static void skipPreParentheses(ListIterator<SourceString> bodyIt) {
        if (!bodyIt.hasNext()) {
            if (bodyIt.hasPrevious()) {
                throw new AnalysisExpressionException(bodyIt.previous().getPosition(), "expected (");
            } else {
                throw new CompilerException("expected body not empty");
            }
        }
        SourceString next = bodyIt.next();
        if (next.getType() != SourceType.OPERATOR || !Operator.GENERIC_LIST_PRE.nameEquals(next.getValue())) {
            throw new AnalysisExpressionException(next.getPosition(), "expected (");
        }
    }

    /**
     * TODO declare和use的分析, 进入body 和离开body, lambda表达式的局部变量表变化(映射关系), 克隆一份局部变量表, 然后获取所需
     *
     * @param executableBody 有start和end比较好
     * @return pool中的位置, -1表示没用函数体
     */
    public int depart(
            SourceTextContext executableBody, IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        if (executableBody.isEmpty()) {
            return NO_BODY_REFERENCE;
        }
        if (executableBody.size() == 1) {
            SourceString body = executableBody.get(0);
            if (body.getType() == SourceType.SIGN &&
                SimpleDepartedBodyFactory.SENTENCE_END.equals(body.getValue())) {
                return NO_BODY_REFERENCE;
            }
        }
        ExecutableBody sequential = toSequential(executableBody, identifierManager, localVariableManager);
        return addToPool(sequential, identifierManager, localVariableManager);
    }

    public int addToPool(
            ExecutableBody sequential, IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        this.findLambdaAdd2Unfinished(sequential);
        int addedIndex = pool.size();
        pool.add(sequential);
        finishUnfinished(identifierManager, localVariableManager);
        return addedIndex;
    }

    private void findLambdaAdd2Unfinished(ExecutableBody result) {
        for (Executable executable : result) {
            if (!(executable instanceof ExpressionExecutable)) {
                continue;
            }
            Expression expression = ((ExpressionExecutable) executable).getExpression();
            for (ExpressionElement element : expression) {
                if (!(element instanceof ComplexExpressionElement)) {
                    continue;
                }
                ComplexExpressionElement cee = (ComplexExpressionElement) element;
                ComplexExpression ce = cee.getExpression();
                if (!(ce instanceof LambdaExpression)) {
                    continue;
                }
                LambdaExpression lambda = (LambdaExpression) ce;
                SourceTextContext lambdaBody = lambda.getBody();
                int unfinishedIndex = unfinishedPool.size();
                int lambdaIndex = this.pool.size();
                this.unfinishedPool.add(new UnfinishedExecutable(lambdaBody, lambdaIndex));
                this.pool.add(new ExecutableBodyPlaceholder(unfinishedIndex));
                cee.setExpression(new LambdaReferenceExpression(Arrays.stream(lambda.getArguments())
                        .map(IdentifierString::new)
                        .toArray(IdentifierString[]::new), lambdaIndex));
            }
        }

    }

    /**
     * TODO 对于lambda表达式的局部变量
     */
    private void finishUnfinished(IdentifierManager identifierManager, LocalVariableManager localVariableManager) {
        while (!unfinishedPool.isEmpty()) {
            int unfinishedIndex = unfinishedPool.size() - 1;
            UnfinishedExecutable unfinished = unfinishedPool.remove(unfinishedIndex);
            ExecutableBody executableBody = pool.get(unfinished.placeholder);
            if (!(executableBody instanceof ExecutableBodyPlaceholder)) {
                throw new CompilerException("Unfinished executable didn't define to place holder");
            }
            ExecutableBodyPlaceholder placeholder = (ExecutableBodyPlaceholder) executableBody;
            if (placeholder.unfinished != unfinishedIndex) {
                throw new CompilerException("Unfinished executable define to incorrect place holder");
            }
            ExecutableBody sequential = toSequential(unfinished.body, identifierManager, localVariableManager);
            findLambdaAdd2Unfinished(sequential);
            pool.add(sequential);
            pool.set(unfinished.placeholder, toSequential(unfinished.body, identifierManager, localVariableManager));
        }
    }


    private static final class ExecutableBodyPlaceholder extends ExecutableBody {
        // 占位用
        private final int unfinished;

        public ExecutableBodyPlaceholder(int unfinished) {
            super(SourcePosition.UNKNOWN);
            this.unfinished = unfinished;
        }


    }

    private static final class UnfinishedExecutable extends ExecutableBody {
        // 占位用
        SourceTextContext body;
        int placeholder;

        public UnfinishedExecutable(SourceTextContext body, int placeholder) {
            super(SourcePosition.UNKNOWN);
            this.body = body;
            this.placeholder = placeholder;
        }
    }

    /**
     * only collectionIn bodyStartStack
     */
    @AllArgsConstructor
    private static class BodyStartReference {
        final int reference;
    }

    /**
     * only collectionIn bodyStartStack
     */
    @Getter
    private static class SingleLineStart extends BodyStartReference {
        public SingleLineStart(BodyStartReference reference) {
            super(reference.reference);
        }

        public SingleLineStart(int reference) {
            super(reference);
        }
    }


}
