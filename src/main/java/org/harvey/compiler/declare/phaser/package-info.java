/**
 * 策略模式
 * 由一个类决定是使用哪个类中的策略
 * 1. 判断应该的和默认的访问控制
 * 2. 其实使用了Enum区分了各个方法/变量/结构的话, 就不需要关键字进行标识了
 * 3. 判断合适的修饰, 是否重名. 是否在import中重名? 其实不用, 重名了, 本文件的优先, import的失效
 * 对于是Value的, 由于解析地不完全, 所以是否重名
 * 还要有关Value的Import...是不能被解析的...看起来就好像"找不到"一样
 * 4. 对于类, 区分出父类和接口列表, 区分出泛型参数列表(直接存STC序列)
 * 6. 对于方法, 区分出返回值类型和参数列表, 按照, 参数列表按照","区分, 区分出"类型"-"标识符"-"默认值"对
 * <p>
 * 成员:
 * enum/class 默认private
 * interface/struct 默认public
 * <p>
 * abstract的method, 不能有sealed, 不能是private, file可以吗? 可以的, protected可以吗? 可以的.. 只要不是private单个, 都可以的
 *
 */
package org.harvey.compiler.declare.phaser;