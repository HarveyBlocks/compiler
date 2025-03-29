/**
 * <h2 id='泛型参数定义语法设计'><span>泛型参数定义语法设计</span></h2>
 * <h3 id='示例'><span>示例</span></h3>
 * <p><code>&lt;T extends BaseClass &amp; BaseInterfaces &amp; super LowerClass &amp; new&lt;int&gt; &amp; new&lt;long&gt;&gt;</code>
 * </p>
 * <p><code>&lt;T extends BaseClass &amp; BaseInterfaces &amp; super LowerInterface &amp; new&lt;int,int&gt; &amp;
 * new&lt;long&gt; = DefaultType &gt;</code></p>
 * <ol start=''>
 *     <li><p><code>extends</code><span>限制泛型的上界</span></p>
 *         <ol start=''>
 *             <li><code>BaseClass</code><span>表示继承, 可以有0个或1个</span></li>
 *             <li><code>BaseInterface</code><span>表示实现的接口, 可以有0个或多个</span></li>
 *             <li><code>BaseInterface</code><span>必须在</span><code>BaseClass</code><span>之后</span></li>
 *             <li><span>每个类或接口前都可以加</span><code>extends</code><span>, 最后回作为集合合并, 可以出现重复的, 只不过会被无视或者extends后面跟着多个</span>
 *             </li>
 *         </ol>
 *     </li>
 *     <li><p><code>super</code><span>限制泛型的下界</span></p>
 *         <ol start=''>
 *             <li><code>LowerClass</code><span>的父类是T, 或</span><code>LowerClass</code><span>实现了T接口, 可以有0个或1个</span>
 *             </li>
 *             <li><code>LowerInterface</code><span>继承了T接口, 可以有0个或1个</span></li>
 *             <li><code>LowerClass</code><span>或者</span><code>LowerInterface</code><span>二选一</span></li>
 *         </ol>
 *     </li>
 *     <li><p><code>new&lt;...&gt;</code><span> 表示泛型需要含有的构造器, 中括号中表示构造器的参数列表</span></p>
 *     </li>
 *     <li><p><code>DefaultType</code><span> 表示默认的泛型类型, 必须在泛型列表的最后, 可以有0个或多个</span></p>
 *     </li>
 * </ol>
 */
package org.harvey.compiler.type.generic.using;