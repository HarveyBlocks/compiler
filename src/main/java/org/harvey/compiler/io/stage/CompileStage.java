package org.harvey.compiler.io.stage;

/**
 * 编译阶段
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-26 09:55
 */
public enum CompileStage {
    NONE,
    // 解决泛型类型问题, 只有类型后面可以加<>吗? 函数呢?函数也得可以使用泛型才合理啊qwq咋办
    // 干脆把能注册的常量通通注册了吧...
    // 但是.. 只有方法和类型的声明会在前面加class callable之类的...
    // func a<Integer, aaa>();
    // class a
    // func a; // 没用的
    // func a = new func[]{};// 没用的
    // Func<A,(B),(? extends A,? super B, ? extends C)> a = aaa;
    // Method<类型, A, (B)>
    // A func aaa<C>(B){
    //
    // }
    // a.Invoke();
    //
    // 函数类型要用Func<?????>类
    // func a(){}// 函数
    // func a<>(){} //// 变量
    TYPE_STATEMENT,
    STATEMENT, FINISHED;

    public static CompileStage at(int ordinal) {
        return values()[ordinal];
    }
}
