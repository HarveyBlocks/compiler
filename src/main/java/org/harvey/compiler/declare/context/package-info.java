/**
 * File有Import, FileVariable, Function, ComplexStructure
 * <div id='write' class=''><h2 id='声明-1'><span>声明</span></h2>
 *         <h3 id='访问控制'><span>访问控制</span></h3>
 *         <ul>
 *             <li><p><span>作为一个文件层的变量/函数/复合结构, 其一定是的file|public|package|inner package</span></p></li>
 *             <li><p><span>作为一个成员, 其作用域一定是任意作用域</span></p></li>
 *             <li><p><span>作为一个代码块里的局部成员, 一定没有作用域</span></p></li>
 *             <li><p><span>作为一个抽象方法, 其方法一定要被重写</span></p>
 *                 <p>
 *                     <span>所以其作用域一定是public或[protected|internal protected]+[ 无|internal package|package|file ]</span>
 *                 </p></li>
 *         </ul>
 *         <h3 id='类型'><span>类型</span></h3>
 *         <ol>
 *             <li><span>基本数据类型 </span></li>
 *             <li><span>带有signed和unsigned修饰的基本数据类型</span></li>
 *             <li><span>自定义类型</span></li>
 *             <li><span>数组元素类型[]</span></li>
 *         </ol>
 *         <h3 id='参数列表'><span>参数列表</span></h3>
 *         <ol>
 *             <li><p><span>括号包围</span></p></li>
 *             <li><p><span>有类型也有标识符</span></p></li>
 *             <li><p><span>有逗号</span></p></li>
 *             <li><p><span>([类型 标识符 [....],[...]])</span></p></li>
 *             <li><p><span>按顺序有:</span></p>
 *                 <ol>
 *                     <li><p><span>普通参数</span></p></li>
 *                     <li><p><span>缺省参数, 已经有了默认值, 可以选择用形式参数名来指定</span></p>
 *                         <p><span>type identifier = default</span></p></li>
 *                     <li><p><span>不定参数 类型... 一个函数只能有一个, 不定参数如果没有就是null</span></p>
 *                         <p><span>type... identifier</span></p></li>
 *                     <li><p><span>关键字参数, 需要被指明形式参数名的参数, 已经有了默认值,不指定使用默认值</span></p>
 *                         <p><span>type identifier = default</span></p></li>
 *                 </ol>
 *             </li>
 *             <li><p><span>一个缺省参数在不定参数前, 表示普通缺省参数; 在不定参数后, 表示关键字参数</span></p></li>
 *             <li><p><span>缺省参数也可以用形式参数名指定</span></p></li>
 *         </ol>
 *         <h3 id='返回值列表'><span>返回值(列表?)</span></h3>
 *         <ul>
 *             <li><p><span>返回值就是给函数发放一块空间, 这篇空间可以存放一个地址, 这个地址指向</span></p>
 *                 <p><span>返回的时候就将这片地址中的空间给填上返回值的地址即可</span></p></li>
 *             <li><p><span>void表示不返回</span></p></li>
 *             <li><p><span>可以省略括号表示返回一个值</span></p></li>
 *             <li><p><span>有括号表示返回多个值(其实是打包返回)</span></p></li>
 *             <li><p><span>有括号, 但是一个值, 表示返回打包的一个值(不一样)</span></p></li>
 *             <li><p><span>有括号的返回值是元组, 成员是[index,index]的有序列</span></p></li>
 *             <li><p><span>可以依据索引获取, 底层原理是依据索引从数组中获取指针, 依据指针返回值</span></p></li>
 *         </ul>
 *         <h3 id='修饰'><span>修饰</span></h3>
 *         <ul>
 *             <li><p><span>final</span></p>
 *                 <ul>
 *                     <li><span>变量只读</span></li>
 *                     <li><span>类可以被继承, 但不能重写父类方法, 父类的成员不能在子类直接修改</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>const</span></p>
 *                 <ul>
 *                     <li><p><span>变量只能调用const修饰的函数</span></p>
 *                         <p><span>变量可以改变</span></p></li>
 *                     <li><p><span>函数内的同层以及外层变量</span></p>
 *                         <p><span>(对于方法, 外层变量即文件层和同类成员类)不可写</span></p>
 *                         <p><span>只能使用const修饰的成员方法, 局部变量可写</span></p>
 *                         <p><span>方法只能调用本类const成员(其实是this变成了const)</span></p></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>sealed </span></p>
 *                 <ul>
 *                     <li><span>类/结构不可继承</span></li>
 *                     <li><span>方法不可被重写</span></li>
 *                 </ul>
 *             </li>
 *             <li><p>&nbsp;</p></li>
 *         </ul>
 *         <h2 id='变量'><span>变量</span></h2>
 *         <h3 id='声明-2'><span>声明</span></h3>
 *         <ul>
 *             <li><p><span>LocalVariable/Argument</span></p>
 *                 <ol>
 *                     <li><span>代码块层作用域</span></li>
 *                     <li><span>[const|final]</span></li>
 *                     <li><span>类型</span></li>
 *                     <li><span>identifier</span></li>
 *                     <li><span>用逗号分割的多个identifier和不定的赋值语句</span></li>
 *                 </ol>
 *             </li>
 *             <li><p><span>FileVariable</span></p>
 *                 <ol>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>[const|final]</span></li>
 *                     <li><span>类型</span></li>
 *                     <li><span>用逗号分割的多个identifier和不定的赋值语句</span></li>
 *                 </ol>
 *             </li>
 *             <li><p><span>Field</span></p>
 *                 <ol>
 *                     <li><span>成员层作用域</span></li>
 *                     <li><span>[const|final|static]</span></li>
 *                     <li><span>类型</span></li>
 *                     <li><span>用逗号分割的多个identifier和不定的赋值语句</span></li>
 *                 </ol>
 *             </li>
 *         </ul>
 *         <h3 id='赋值'><span>赋值</span></h3>
 *         <p><span>不定的赋值语句</span></p>
 *         <h2 id='代码块'><span>代码块</span></h2>
 *         <ul>
 *             <li><span>static-structure 初始化静态字段</span></li>
 *             <li><span>无-structure 初始化非静态字段</span></li>
 *         </ul>
 *         <p><span>静态代码块的声明是</span><code>static{...}</code></p>
 *         <p><span>非静态代码块的声明是</span><code>{}</code></p>
 *         <p><span>只能在复合类型和方法中出现</span></p>
 *         <ol>
 *             <li><span>局部变量</span></li>
 *             <li><span>内部函数</span></li>
 *             <li><span>控制结构 关键字+非静态代码块/ 非静态代码块+关键字</span></li>
 *             <li><span>非静态代码块</span></li>
 *         </ol>
 *         <h2 id='复合类型'><span>复合类型</span></h2>
 *         <h3 id='声明-3'><span>声明</span></h3>
 *         <ul>
 *             <li><p><span>Class</span></p>
 *                 <ol>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>[abstract]</span></li>
 *                     <li><span>[final, sealed]</span></li>
 *                     <li><span>class</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>[extends Identifier(必须是class)]</span></li>
 *                     <li><span>[implements ...(必须是interface)]</span></li>
 *                 </ol>
 *             </li>
 *             <li><p><span>Interface</span></p>
 *                 <ul>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>interface</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>[implements ...(必须是interface)]</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Enum</span></p>
 *                 <ul>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>enum</span></li>
 *                     <li><span>class</span></li>
 *                     <li><span>Identifier</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Struct</span></p>
 *                 <ul>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>[sealed]</span></li>
 *                     <li><span>struct</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>[extends Identifier(必须是Struct)]</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>成员复合类型</span></p>
 *                 <ol>
 *                     <li><span>成员型作用域</span></li>
 *                     <li><span>其余同对应复合类型声明</span></li>
 *                 </ol>
 *             </li>
 *         </ul>
 *         <h3 id='structure-1'><span>Body</span></h3>
 *         <ul>
 *             <li><p><span>Class</span></p>
 *                 <ul>
 *                     <li><span>ComplexStructure List</span></li>
 *                     <li><span>Field List</span></li>
 *                     <li><span>Method List</span></li>
 *                     <li><span>Constructor List</span></li>
 *                     <li><span>AbstractMethod List</span></li>
 *                     <li><span>structure List</span></li>
 *                     <li><span>static-structure List</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Enum</span></p>
 *                 <ul>
 *                     <li><span>第一句一定是成员数组</span></li>
 *                     <li><span>同Class</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Struct</span></p>
 *                 <ul>
 *                     <li><span>所有成员可读不可写</span></li>
 *                     <li><span>所有的成员必须加const, 不加默认加上</span></li>
 *                     <li><span>所有的成员变量必须加final, 不加默认加上</span></li>
 *                     <li><span>必须在创建出成员的时候全部初始化成员</span></li>
 *                     <li><span>可以被拷贝, 可以在拷贝过程中修改成员生成新成员</span></li>
 *                     <li><span>同Class</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Interface</span></p>
 *                 <ul>
 *                     <li><p><span>structure</span></p>
 *                         <ul>
 *                             <li><p><span>FieldList, 抽象层作用域, </span></p>
 *                                 <p><span>必须是(final修饰, 不写默认加final)</span></p>
 *                                 <p><span>必须是static, 不写自动加static</span></p></li>
 *                             <li><p><span>AbstractMethod</span></p></li>
 *                             <li><p><span>static修饰Method</span></p></li>
 *                         </ul>
 *                     </li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Internal ComplexStructure</span></p>
 *                 <ul>
 *                     <li><p><span>structure</span></p>
 *                         <ul>
 *                             <li><span>同本ComplexStructure</span></li>
 *                         </ul>
 *                     </li>
 *                     <li><p><span> [作用域(全部)]</span></p>
 *                         <ul>
 *                             <li><span>[abstract]</span></li>
 *                             <li><span>[final, sealed]</span></li>
 *                             <li><span>class</span></li>
 *                             <li><span>Identifier</span></li>
 *                             <li><span>[extends Identifier(必须是class) [implements [...(必须是interface)]]]</span></li>
 *                         </ul>
 *                     </li>
 *                 </ul>
 *             </li>
 *         </ul>
 *         <h2 id='函数'><span>函数</span></h2>
 *         <p><span>使用func关键字来减少编译时区分变量和函数的时间</span></p>
 *         <h3 id='声明-4'><span>声明</span></h3>
 *         <ul>
 *             <li><p><span>Function</span></p>
 *                 <ul>
 *                     <li><span>文件层作用域</span></li>
 *                     <li><span>func?</span></li>
 *                     <li><span>[const] 能不能文件层变量的非const</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>参数列表</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Method</span></p>
 *                 <ul>
 *                     <li><span>成员层作用域</span></li>
 *                     <li><span>func?</span></li>
 *                     <li><span>[static|const|sealed] 能不能文件层&amp;成员层变量的非const</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>参数列表</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>LocalFunction</span></p>
 *                 <ul>
 *                     <li><span>代码块层访问控制</span></li>
 *                     <li><span>func?</span></li>
 *                     <li><span>[const] 能不能文件层&amp;成员层&amp;代码块层变量的非const </span></li>
 *                     <li><span>type</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>参数列表</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Constructor</span></p>
 *                 <ul>
 *                     <li><span>成员层访问控制</span></li>
 *                     <li><span>func?</span></li>
 *                     <li><span>本Class/Struct名</span></li>
 *                     <li><span>参数列表</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>AbstractMethod</span></p>
 *                 <ul>
 *                     <li><span>成员层访问控制</span></li>
 *                     <li><span>func?</span></li>
 *                     <li><span>[] 不能有修饰(static和const和sealed都不合适)</span></li>
 *                     <li><span>type</span></li>
 *                     <li><span>Identifier</span></li>
 *                     <li><span>参数列表</span></li>
 *                 </ul>
 *             </li>
 *         </ul>
 *         <h3 id='structure-2'><span>Body</span></h3>
 *         <ul>
 *             <li><p><span>Function</span></p>
 *                 <ul>
 *                     <li><span>同代码块</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Method</span></p>
 *                 <ul>
 *                     <li><span>同代码块</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>LocalFunction</span></p>
 *                 <ul>
 *                     <li><span>同代码块</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>Constructor</span></p>
 *                 <ul>
 *                     <li>
 *                         <span>第一行必须用</span><code>this()</code><span>或</span><code>super()</code><span>指明用什么构造</span>
 *                     </li>
 *                     <li><span>同代码块</span></li>
 *                 </ul>
 *             </li>
 *             <li><p><span>AbstractMethod</span></p>
 *                 <ul>
 *                     <li><span>无</span></li>
 *                 </ul>
 *             </li>
 *         </ul>
 *     </div>
 */
package org.harvey.compiler.declare.context;