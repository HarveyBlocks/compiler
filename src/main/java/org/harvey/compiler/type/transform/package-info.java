/**
 * TODO 远远未完成
 * <p>
 *
 * <h2 id='泛型的自洽'><span>泛型的自洽</span></h2>
 * <ol start=''>
 *     <li><p><span>T.Lower&lt;=T.Upper</span></p>
 *         <p><span>对于任意i, T.Lower&lt;=T.Interfaces[i]. 见</span><a
 *                 href='#带泛型的类型之间的转换'><span>类型转换</span></a></p></li>
 *     <li><p><span>DefaultType必须能转换成T. 见</span><a
 *             href='#泛型参数和普通类型可能带泛型之间的转换'><span>类型转换</span></a></p></li>
 * </ol>
 * <h2 id='类型转换'><span>类型转换</span></h2>
 * <h3 id='带泛型的类型之间的转换'><span>带泛型的类型之间的转换</span></h3>
 * <p><span>要使得 FROM&lt;T [define of T]&gt; 转换成 TO&lt;T [define of T]&gt;</span></p>
 * <p><span>即</span><code>FROM&lt;T [define of T]&gt; &lt;= TO&lt;T [define of T]&gt;</code></p>
 * <ol start=''>
 *     <li><span>FROM &lt;= TO</span></li>
 *     <li><span>对于任意i, FROM.T[i] &lt;= TO.T[i]. 见</span><a href='#泛型参数和泛型参数之间的类型转换'><span>泛型参数转换</span></a>
 *     </li>
 * </ol>
 * <h3 id='泛型参数和普通类型可能带泛型之间的转换'><span>泛型参数和普通类型(可能带泛型)之间的转换</span></h3>
 * <p><span>要使得定义为</span></p>
 * <p><code>Lower&lt;=T&lt;=Upper &amp; T &lt;= Interfaces &amp; new&lt;....&gt; in T</code></p>
 * <p><span>的泛型和类型Obj相互转化</span></p>
 * <ul>
 *     <li><p><span>类型转换为泛型参数</span></p>
 *         <p><span>即</span><code>FROM &lt;= TO [define of TO]</code></p>
 *         <ol start=''>
 *             <li><span>TO.Lower&lt;=FROM&lt;=TO.Upper</span></li>
 *             <li><span>且, 对于任意i, FROM&lt;=TO.Interfaces[i]</span></li>
 *             <li><span>且, 对于任意i, TO.new[i] in FROM</span></li>
 *         </ol>
 *     </li>
 *     <li><p><span>泛型参数转化为类型</span></p>
 *         <p><span>即</span><code>FROM [define of FROM] &lt;= TO</code></p>
 *         <ol start=''>
 *             <li><span>FROM &gt;= TO.Upper</span></li>
 *             <li><span>或, 存在i, 使得FROM &gt;= TO.Interface[i]</span></li>
 *         </ol>
 *     </li>
 * </ul>
 * <h3 id='泛型参数和泛型参数之间的类型转换'><span>泛型参数和泛型参数之间的类型转换</span></h3>
 * <p><span>泛型参数FROM怎么转化成泛型参数TO</span></p>
 * <p><span>即</span><code>FROM &lt;= TO</code></p>
 * <p><span>可得FROM, TO的定义:</span></p>
 * <p><code>Lower&lt;=FROM&lt;=Upper &amp; FROM&lt;= Interfaces &amp; new&lt;....&gt; in FROM</code></p>
 * <p><code>Lower&lt;=TO&lt;=Upper &amp; TO&lt;= Interfaces &amp; new&lt;....&gt; in TO</code></p>
 * <p><span>FROM转TO需要, FROM是TO的子集</span></p>
 * <ul>
 *     <li><span>FROM.Lower&gt;=TO.Lower</span></li>
 *     <li><span>FROM.Upper&lt;=TO.Upper</span></li>
 *     <li><span>对于任意j, 都存在i, 使FROM.Interfaces[i]&lt;=TO.Interfaces[j]</span></li>
 *     <li><span>对于任意j, 都存在i, 使FROM.new[i]能转换成TO.new[j]. 见</span><a
 *             href='#带泛型的类型之间的转换'><span>类型转换</span></a></li>
 * </ul>
 * <h3 id='对于类型转化中的说明'><span>对于类型转化中</span><code>&lt;=</code><span>的说明</span></h3>
 * <p><code>FROM &lt;= TO</code><span>, 表示 FROM 能转化为 TO.</span></p>
 * <ol start=''>
 *     <li><span>基本数据类型的转化</span></li>
 *     <li><span>有继承关系类型的转化表示子类的情况</span></li>
 *     <li><span>带泛型的复杂的转化</span></li>
 * </ol>
 */
package org.harvey.compiler.type.transform;
/*
 * 可行的...
 * Object[] a = new String[0];
 * Number[] b = new Integer[0];
 * 警告...运行时异常...
 * Object[] a = new String[2];
 * a[0] = new File("");
 * a[1] = new File("");
 * 是不被允许的
 * List<Object> l =  (new ArrayList<String>());
 * TODO 思考: 允许了会有什么缺点?
 * 运行阶段 a[0] 是不知道自己使用了多态的. 它以为自己是String[]
 * 如果允许编译阶段允许通过泛型的父类多态, 而泛型是作为复合类型的成员的, 如果允许编译阶段进行类型转换
 * 那么, 就会导致, 成员指向的是子类, 编译阶段以为是父类, 可以写入并非是子类的一个类,
 * 运行阶段会发现不是和定义出来的类是同一个继承链的类, 就会异常!
 * 这肯定是不好的, 所以, 禁止
 * 可以用修饰模式解决改变泛型导致的拷贝的问题, 而且通过对修饰类的有关写方法的禁止, 不会出现对成员的不正确的写
 *
 *
 * 那么, 在经过理论指导之后, 要怎么匹配ParameterizedType呢?
 * List<Number> = List<Number> 可
 * List<Number> = ArrayList<Number> 可
 * List<Number> = ArrayList<Integer> 不可
 * ArrayList<Number> = ArrayList<Integer> 不可
 * ArrayList<Number> = ArrayList<Number> 可
 * public static <T extends Integer> void aa(List<T> t) {
 *      a(t); // 可以匹配
 * }
 *  public static <T extends Number> void a(List<T> t) {}
 * 1. raw type 要匹配(有继承关系)
 * 2. 检查 generic list 是否合适
 *      1. 获取raw type 的 son到parent的继承路径
 *      2. 依据继承路径将泛型映射
 *      3. 泛型映射时, 对GenericDefine进行检查(是子集关系)
 *      4. 最终的映射结果进行完全一致的匹配
 *           TODO 是否存在一种情况, 映射后不完全一致, 但是依旧能够转换, 例如GenericDefine的位置不同, 但是定义是相同的
 *            这个或许可以用定义生效的优先级来解决
 *              例如, 先用param推理generic参数, 且这个param上的generic映射到define上, 是匹配的, 那么就能转了
 *        其实也不是完全一致的匹配,
 *              Structure 的 RawType 完全一致匹配
 *              不同的Alias, 映射到Structure上是完全一致的, 那就是完全一致匹配的
 *              GenericDefineReference的RawType, 用范围匹配, 不是完全匹配
 *
 *      全矣
 * */

// 有泛型的Constructor
//
// class A[T extends List[_/*这个属于是ignore, 也就是占位符, 表示所有*/]] {
//
//        A[List[Object]] a = new A();
//        A[List[String]] a1 = new A();
//        A[List[String]] a2 = new A[]();
//        A[List[String]] a3 = new A[List[String]]();
//        A[List[String]] a4 = new();
//  constructor上有generic message的:
//        A[List[String]] a5 = new (A[List[String]])[int]();
//        A[List[String]] a6 = new (A[])[int]();
//        A[List[String]] a7 = new (A)[int]();
//        A[List[String]] a7 = new[int]();
//    }