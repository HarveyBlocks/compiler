/**
 * 对声明的检查
 * 1. 检查访问控制
 * 2. 检查关键字修饰
 * 4. 类型此时检查? 参数列表此时检查? 返回值列表此时检查? 泛型类型列表检查?
 * 5. identifier转为index?
 * <p>
 * 参数列表
 * 参数列表(类型 变量, 类型 变量, 类型 变量 = 默认, 类型 变量 = 默认, 类型... 变量, 类型 变量 = 默认, 类型 变量 = 默认)
 * 参数列表(..., 类型 变量 = 默认, 类型 变量 = 默认)
 * print(object... args,char end='\n',char separator=' ',); // 无序
 * <p>
 * 泛型列表?
 * 使用树的嵌套模式下的泛型结构无法序列化, 非常的糟糕
 * srcType的结构耶不好序列化, 无论是什么, 只要是类型的都不好序列化
 * <p>
 * 返回值列表?
 * (int,int,string,int) a(){
 * return 1,1,"a",1,1;
 * }
 * // 可
 * 1,_,"a",1,1 = a();
 * // 不可
 * int a = a();
 * int a,a1,a2,a3,a4=a()
 * tuple x = a();
 * StructType
 * ClassType
 * InterfaceType
 * EnumType
 * Object
 * Enum
 * Struct
 * 对象头种的Klass, 指向元数据, 可用否? 不可.
 * C#的做法似乎是typeof(), 在编译期间就分析出类型信息, 然后再返回
 * typeof的参数一定是一个变量, 暂不考虑类名直接在表达式里使用
 * 这个变量在typeof使用之前一定是被声明的, 被声明, 就可以找到他的类对象
 * 可以用, 一个函数, 看似允许时, 其实是编译时, 参数是原始的类类型, 不定参数是泛型参数列表
 * 还是那句话, 怎么描述的嵌套的泛型列表?
 * {@code Type<Type,Type,Type<Type<Type<Type>>,Type<Type>>,Type<? extends A>,Type,k<>>}
 * TODO 描述一种可序列化的泛型列表
 * GenericType{
 *     Type Origin
 *     <Type,int>[] types_index_map; // index指向Type里注册的可能的类型, -1表示每有使用泛型列表, 1表示哪种泛型列表
 * }
 * Type{
 *     String identifier; // 原子类型
 *     Type[] genericTypeArgs; // 定义在源码里的参数列表类型
 *     Type[][] 注册在这里的几种可能的泛型类型
 * }
 */
package org.harvey.compiler.declare;
