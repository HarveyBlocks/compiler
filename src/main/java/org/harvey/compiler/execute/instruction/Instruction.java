package org.harvey.compiler.execute.instruction;

import lombok.Getter;

/**
 * TODO
 * if-else->
 * 运算condition
 * if condition goto L1
 * 运算condition
 * if condition goto L2
 * 运算condition
 * if condition goto L3
 * 运算condition
 * if condition goto L4
 * 运算condition
 * if condition goto L5
 * goto LE
 * L1 : 编译的 if-块
 * goto LE
 * L2 : 编译的 if-块
 * goto LE
 * L3 : 编译的 if-块
 * goto LE
 * L4 : 编译的 if-块
 * goto LE
 * L5 : 编译的 if-块
 * LE :
 * <p>
 * <p>
 * switch-case
 * 编译switch语句
 * genericDefine-case-table CASE1_VALUE CASE1_POSITION
 * genericDefine-case-table CASE2_VALUE CASE2_POSITION
 * genericDefine-case-table CASE3_VALUE CASE3_POSITION
 * genericDefine-case-table CASE4_VALUE CASE4_POSITION
 * CASE1块
 * CASE2块
 * CASE3块
 * CASE4块
 * default
 * LE :
 * <p>
 * <p>
 * while(){}
 * L1:
 * 编译的 condition
 * ifn condition goto LE
 * while-快
 * goto L1
 * LE:
 * <p>
 * <p>
 * do{}while();->
 * L1:
 * do-while块
 * L2(用于continue): 编译的 condition
 * if condition goto L1
 * LE:
 * <p>
 * <p>
 * for-each->for i
 * for i....
 * for_i_start, i_分配空间, using i,
 * for(init;condition ;.){}
 * init
 * L1:
 * ifn condition goto LE
 * for-i 块
 * step
 * LE:
 * <p>
 * <p>
 * try-catch-finally
 * 在try_label_stack中加入第一个catch命令位置
 * 语句块
 * throw (可能是函数中throws)->从try_label_stack取出顶部, goto
 * 语句块
 * <p>
 * catch 匹配类型序列1 下一个catch的地址
 * ifn 匹配类型序列1 goto catch L+1
 * 注入exception的值到局部变量表
 * catch 块1
 * catch 匹配类型序列2 下一个catch的地址
 * goto finally
 * L : 匹配类型序列2
 * ifn 匹配成功 goto catch L+1
 * 注入exception的值到局部变量表
 * catch 块2
 * goto finally
 * L_FINALLY:
 * 执行finally块
 * goto outer finally 块
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-12 16:06
 */
@Getter
public class Instruction {
    int operatorCode;
    // operatorCode->String的描述
    // String的描述->operatorCode
    // 表达式
    long[] args; // 数字/引用
    // 还要维护一个库, 用来存储常量池, 例如类型, 需要常量池, 例如函数, 需要常量池
    // 需要维护局部变量表了, 解析表达式一定需要局部变量表,
    // 还需要字段表, 不是局部变量的, 认为是字段,
    // 还需要...先完成声明吧...

}
