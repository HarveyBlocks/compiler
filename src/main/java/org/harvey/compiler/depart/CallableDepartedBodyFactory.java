package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.Pair;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.DeclarePhaserUtil;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.ExpressionPhaser;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 16:13
 */
@Deprecated
public class CallableDepartedBodyFactory {

    @Deprecated
    private static void oldDepart(LinkedList<DepartedPart> callableBody, LinkedList<DepartedPart> executableBody,
                                  LinkedList<DeclaredDepartedPart> localCallables) {
        while (!callableBody.isEmpty()) {
            DepartedPart part = callableBody.removeFirst();
            if (part.isSentenceEnd()) {
                assert part.getBody() == null || part.getBody().isEmpty();
/*                SourceTextContext statement = part.getStatement();
                if (!statement.isEmpty()) {
                    SourceString first = statement.getFirst();
                    if (first.getType() == SourceStringType.KEYWORD && Keyword.FOR.equals(first.getValue())) {
                        // for(;;)___
                        // 必有三部分
                        if (callableBody.size()<=2){
                            throw new AnalysisException(first.getPosition(),"Illegal for");
                        }
                        // 加入for
                        executableBody.add(part);
                        executableBody.add(callableBody.removeFirst());
                        executableBody.add(callableBody.removeFirst());
                        continue;
                    }
                }*/
                executableBody.add(part);
                continue;
            }// TODO
            // 不对, 有些语句不是sentenceEnd的, 例如数组有关的语句
            // 它们也可能不能被创建成Declarable
            // a = Func(new A[]{});
            // -> a=Func(new A[]{}  和 );
            // 两部分, 这样就不好了(╯▔皿▔)╯
            // 走到这一步, 说明不是SentenceEnd了...
            // 甚至, 控制结构也无法被解析, 会被判错
            SourceTextContext statement = part.getStatement();
            if (statement.isEmpty()) {
                // body
                executableBody.add(part);
                continue;
            }
            // 不是单纯的body
            if (!DeclarePhaserUtil.isDeclarableInCallable(statement)) {

                executableBody.add(part);
                continue;
            }
            Declarable declarable = DeclarePhaserUtil.statementBasicPhase(statement);
            if (declarable.getType() == null || declarable.getType().isEmpty()) {
                executableBody.add(part);
            } else if (declarable.isComplexStructure()) {
                // 禁止类型声明
                throw new AnalysisExpressionException(declarable.getStart(),
                        "Illegal type declared here, only function is allowed here.");
            } else if (declarable.isCallable()) {
                localCallables.add(new DeclaredDepartedPart(declarable, part.getBody()));
            } else {
                executableBody.add(part);
            }
        }
    }

    private static LinkedList<ExecutableString> toLinkedList(ExecutableStringNode head) {
        LinkedList<ExecutableString> result = new LinkedList<>();
        ExecutableStringNode node = head;
        while (node != null) {
            result.add(node.value);
            node = node.next;
        }
        return result;
    }

    /**
     * 解析了控制结构, 将控制结构转换为命令式的语句;
     * 将声明语句, 表达式先整理成一块一块的, 先不解析.
     * <pre>{@code
     *  要不要拆解了控制结构?
     *  for(exp1;exp2;exp3){exp4};
     *  begin; exp1; while_begin(exp2);exp4; exp3; while_end;end;
     *  { exp1;while(exp2){exp4;exp3}}
     *
     *  注意控制结构
     *  bodyEnd 的, 不一定结束了
     *  只有Function和Body允许Sentence
     *  for后面(;;) , ;不代表结束
     *  TODO
     *  分成: 控制结构/一般语句/局部函数声明/局部函数体
     *  怎么说呢....从设计的角度想, 到底要不要支持函数内定义函数
     *  到底要不要在数据结构中支持函数
     *  数据结构是有嵌套的, 要不要在此处就分解了嵌套的结构? 还是说, 不做?
     *  如果不解析, 那么什么时候做呢? 数据结构的体内不能做定义吗? 这他妈的到底是什么鬼嘛
     *  为什么Java允许在函数里定义类, 却不允许在函数里定义方法啊? 为什么啊????
     *  局部函数有什么意义? 为什么一定要在函数里定义函数呢? 有这个必要吗? 有这个需求吗?
     *  有需求吗? 在函数里定义函数, 让其他函数访问不到? 有必要吗?
     *  而且, 局部函数的作用域.. 一定要在其声明之后才能调用吗? 真的吗? 有必要吗? 好吗? 合适吗?代码块中定义就好了吗? 可以了吗? 真的吗?
     *  而且, 转念一想, 多返回值和定义一个类, 孰优孰劣呢?
     *  不支持局部函数了, 使用了Lambda表达式代替
     *  有一个问题: while(){
     *       XXX = new lambda();
     *       1. 如何提高效率?
     *         只加载一次? 但是里面的参数可能发生变化的...
     *         Lambda表达式, 为止奈何?
     *         靠程序员自行判断哪里定义这个lambda
     *       2. 参数参数传递
     *           怎么办呢? 如果参数是函数的局部变量
     *           至少在这个lambda表达式里, 应该是值传递的吧? 那么就会产生里面加加, 外面不加的情况...
     *
     *         Runnable e = new Runnable(1,2,3,4){
     *             private final int a = 0;
     *             @Override
     *             public void run () {
     *
     *             }
     *         }
     *           如果不支持, 那么就要和Java完全一样了吗? 这样好吗?
     *       Runnable implements Callable(){
     *
     *       }
     *        e();
     *  }
     *
     * }</pre>
     *
     * @param callableBody 要去处大括号
     */
    public static SimpleCallable depart(Declarable statement, SourceTextContext callableBody) {
        ListIterator<SourceString> iterator = callableBody.listIterator();
        // 1. 声明函数内部类型的=> static class or class
        // 2. 控制结构, 可以分号结尾, 可以{结尾}
        // 3. 以分号结尾的
        // if next in 控制结构->进入控制结构分析
        // if next == "static" ->
        //      if 再下一个是"class"-> 进入类分析
        //      else 读到分号为止
        // if next == "class" -> 进入类分析
        // else 读到分号为止

        // 例如
        // int  a = 2;
        // if(a-->2){
        //      int b = 3;
        //      if(){
        //          int c = 4;
        //      }
        //      int d = 5;
        // }
        // int e = 6;
        // 0. 找到int a = 2;, 正常, Sentence存入链表list.add("int a = 2;");
        // 1. 解析到if的时候, 开始找{, 找到{, {前保存, 命名为*begin = "if(a-->2){";
        // 2. 开始找}, 中间的放到STC
        // 3. 找到}, *end = "}"
        // 4.  list.add(*begin).add(*end);
        // 5. 解析STC, 解析后为X类
        // 6. *mid=X;
        // 7. begin->next = mid, mid->next = end;
        ExecutableStringNode begin = new ExecutableStringNode();
        TaskQueue taskQueue = new TaskQueue();
        phase(taskQueue);
        LinkedList<ExecutableString> result = toLinkedList(begin.next);
        return null;
    }

    private static void phase(TaskQueue taskQueue) {
        Task task = taskQueue.removeFirst();
        SourceTextContext context = task.getKey();
        ExecutableStringNode begin = task.getValue();
        ListIterator<SourceString> iterator = context.listIterator();
        while (iterator.hasNext()) {
            SourceString next = iterator.next();
            if (SourceStringType.KEYWORD != next.getType()) {
                iterator.previous();
                phaseAsSentence(iterator, begin);
                continue;
            }
            Keyword nextKeyword = Keyword.get(next.getValue());
            if (nextKeyword == null) {
                throw new AnalysisException(next.getPosition(), "Unknown keyword");
            }
            if (Keywords.isControlStructure(nextKeyword)) {
                iterator.previous();
                phaseAsControlStructure(iterator, begin, taskQueue);
                continue;
            } else if (Keywords.isComplexStructure(nextKeyword)) {
                iterator.previous();
                phaseAsClass(iterator, begin, taskQueue);
                continue;
            } else if (nextKeyword != Keyword.STATIC) {
                iterator.previous();
                phaseAsSentence(iterator, begin);
                continue;
            }
            if (!iterator.hasNext()) {
                continue;
            }
            if (CollectionUtil.nextIs(iterator,
                    ss -> ss.getType() == SourceStringType.KEYWORD && Keywords.isComplexStructure(ss.getValue()))) {
                iterator.previous();
                phaseAsClass(iterator, begin, taskQueue);
            } else {
                iterator.previous();
                phaseAsSentence(iterator, begin);
            }
        }
    }

    private static void phaseAsClass(ListIterator<SourceString> iterator, ExecutableStringNode begin,
                                     TaskQueue taskQueue) {
        // 这个class... 就交给去DepartedBodyFactory去解析吧
        // class 里如果有函数, 那也交给去解析
        // 尽可能减少递归了, 能解决类里嵌套类, 函数里嵌套函数, 控制结构里嵌套控制结构的问题
        // 但是函数里有类, 类里又有函数, 函数里又有类, 如此, 不太好吧
        // 但是! Class 的信息存到哪里去?
        // 限制内部类吧, 不允许局部类有内部类, 不允许局部类的方法有内部类
        // 糟糕的设计! 如此复杂的类不应该作为函数的内部类! 把它设计成外部类吧!

        // 不对不对
        // lambda表达式是lambda表达式, 没必要搞成匿名内部类, 干脆不能在函数中定义类!
    }

    private static void phaseAsControlStructure(ListIterator<SourceString> iterator, ExecutableStringNode begin,
                                                TaskQueue taskQueue) {

    }

    private static void phaseAsSentence(ListIterator<SourceString> iterator, ExecutableStringNode begin) {
        ExpressionPhaser expressionPhaser = new ExpressionPhaser();
        //
        // exp获取结构
        // if(exp1){
        //      exp2
        // } else {
        //      exp3
        // }
        // exp4
        // ifn exp1 goto +1+len(exp2)+(else?1:0)
        // exp2
        // goto + len(exp3)
        // exp3
        // exp4
        // if -> ifn exp1 goto +1+len(exp2);exp2
        // else -> goto + len(exp3);exp3
        // else if -> goto + len(exp4);ifn exp4 goto +1 +len(exp5); exp5;
    }

    private static boolean isCallable(SourceTextContext statement) {
        // 判断是函数声明, 就应该去查callable, 要不就是TryCatch(Exception), 如果catch了, 就说明创建为函数声明失败了, 就应该是函数了...?
        // 怎么办呢...
        // 遍历查找吗? 这样好吗? 效率高吗?
        // 要不, callable这个关键字, 可能出现再其他地方吗? 这里我用callable来查,
        // callable就不能出现在其他地方了, 会不会导致可拓展性降低了呢?
        // 而且看逻辑, 这个方法的任务似乎已经变成了, 语句是否是可执行语句了...
        return true;
        // 🤔
        // 而且, 这里如果不是控制结构, 也不是函数, 是不是应该把可能的不完整的Sentence合并起来?
        // 这个以后解析函数可执行语句的时候再说...
        // 想到要删除文件层代码块和文件层变量就令人伤感qwq.qwq.qwq
    }

    private enum ExecutableStringType {
        SOURCE, CONTROL//源码 or 控制结构
    }

    private static class ExecutableStringNode {
        ExecutableStringNode pre;
        ExecutableString value;
        ExecutableStringNode next;
    }

    private static class Task extends Pair<SourceTextContext, ExecutableStringNode> {

        public Task(SourceTextContext context, ExecutableStringNode executableStringNode) {
            super(context, executableStringNode);
        }
    }

    private static class TaskQueue extends LinkedList<Task> {
        public TaskQueue() {
            super();
        }
    }

    @AllArgsConstructor
    @Getter
    public static class ExecutableString {
        private final ExecutableStringType type;
        private final SourceString origin;
    }
}