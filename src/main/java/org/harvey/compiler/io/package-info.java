/**
 * <h1 id='文件io'><span>文件IO</span></h1><h2 id='编译阶段'><span>编译阶段</span></h2><p><span>解决循环import问题</span>
 * </p><p><span>要分析变量-&gt;</span></p><p><span>要能解析泛型-&gt;</span></p><p>
 * <span>解析泛型的依据是它的前面是类型-&gt;</span></p><p><span>要知道前面是不是类型需要解析import的文件-&gt;先解析import文件</span>
 * </p><h3 id='解决方案'><span>解决方案</span></h3>
 * <ol start=''>
 *     <li><span>生成Departed, 先将类型放在文件前面做标识, 类型的信息不做考虑, 因为只有类型会对第3步编译产生影响(内部类, 为之奈何qwq)</span>
 *     </li>
 *     <li><span>遍历import表, 对import的文件执行1相关操作</span></li>
 *     <li><span>解析文件结构, 解析所有的声明, 再次写入文件</span></li>
 *     <li><span>遍历import表, 对import的文件执行3相关操作</span></li>
 *     <li><span>逻辑分析, import表中的所有类型都已知结构, 就可以分析本文件中对import的部分结构使用方式是否正确</span>
 *         <span>本文件解析完毕</span></li>
 *     <li><span>遍历import表, 对import表import的文件执行5相关操作</span></li>
 * </ol><h3 id='阶段一-类型声明'><span>阶段一: 类型声明</span></h3>
 * <ul>
 *     <li><span>任务: 本类中的类声明注册完毕</span></li>
 *     <li><span>source.file-&gt;SourceString-&gt;DepartedPart</span></li>
 *     <li><span>SourceTextContext-&gt;DepartedBody-&gt;binary.file</span></li>
 * </ul><h3 id='阶段二-结构解析'><span>阶段二: 结构解析</span></h3>
 * <ul>
 *     <li><span>任务: 解析变量/复合结构/方法的结构后, 即可完成import表的用法</span></li>
 *     <li><span>DepartedBody-&gt;FileContext-&gt;binary.file</span></li>
 * </ul><h3 id='阶段三-完成解析'><span>阶段三: 完成解析</span></h3>
 * <ul>
 *     <li><span>任务: 完全完善import表信息</span></li>
 *     <li><span>检查之前的声明是否正确</span></li>
 *     <li><span>解析表达式和可执行块, 检查final, const的使用是否正确</span></li>
 * </ul><h2 id='文件结构设计'><span>文件结构设计</span></h2><h3 id='总体设计'><span>总体设计</span></h3>
 * <ul>
 *     <li><p><span>阶段二</span></p>
 *         <ul>
 *             <li><span>魔数</span></li>
 *             <li><span>完成阶段</span></li>
 *             <li><span>import表</span></li>
 *             <li><span>常量池</span></li>
 *             <li><span>已割裂源码信息</span></li>
 *         </ul>
 *     </li>
 *     <li><p><span>阶段二</span></p>
 *         <ul>
 *             <li><span>魔数</span></li>
 *             <li><span>完成阶段</span></li>
 *             <li><span>import表</span></li>
 *             <li><span>常量池</span></li>
 *             <li><span>结构声明</span></li>
 *             <li><span>复合类型信息(泛型或元组, 函数签名)</span></li>
 *             <li><span>代码块,实现和赋值(已割裂源码信息)</span></li>
 *         </ul>
 *     </li>
 *     <li><p><span>阶段三</span></p>
 *         <ul>
 *             <li><span>魔数</span></li>
 *             <li><span>完成阶段</span></li>
 *             <li><span>import表</span></li>
 *             <li><span>常量池</span></li>
 *             <li><span>结构声明</span></li>
 *             <li><span>复合类型信息(泛型或元组, 函数签名)</span></li>
 *             <li><span>代码块,实现和赋值(已编译)</span></li>
 *         </ul>
 *     </li>
 * </ul><p><span>都设计协议了, 难道要使用Nio了吗qwq</span></p><h3 id='魔数和完成阶段'><span>魔数和完成阶段</span></h3><p>
 *     <span>[</span><span>完成阶段</span><span>]</span><span>, 2bit? 8bit?</span></p><h3 id='常量池'><span>常量池</span>
 * </h3><p><span>[基本数据类型?]</span><span>[</span><span>常量池表开始标识?</span><span>]</span></p>
 * <ul>
 *     <li><p>
 *         <span>第一阶段完成: 注册复合结构名, 内部结构名 (函数名(不行吧, 返回值类型涉及泛型)和变量名(不太行, 因为变量涉及泛型))</span>
 *     </p></li>
 *     <li><p><span>第二阶段完成: 注册类名, 复合结构名, 变量名, 函数名, 将类名后面指向文件中自己的具体声明</span></p></li>
 *     <li><p><span>第三阶段完成: 遍历常量池, 以此遍历它们的声明, 检查它们的声明正确与否</span></p>
 *         <p><span>包括文件级别的函数变量和复合类型, 也包括类内嵌套的各种数据信息</span></p>
 *         <ul>
 *             <li><span>共计 (24Bit+)</span></li>
 *             <li><span>字符串长度(12bit)</span></li>
 *             <li><span>指向声明结构(12bit)</span></li>
 *             <li><span>名字字符串(字节数组)</span></li>
 *         </ul>
 *     </li>
 *     <li><p><span>[import表] 其实是常量池的一部分, 但是和常量池的元素的区别在于无法指向内部的文件</span></p>
 *         <ul>
 *             <li><span>共计 (24Bit+)</span></li>
 *             <li><span>字符串长度(12bit)</span></li>
 *             <li><span>指向声明结构(12bit), 全空</span></li>
 *             <li><span>名字字符串(字节数组)</span></li>
 *         </ul>
 *     </li>
 * </ul><h3 id='结构声明'><span>结构声明</span></h3><p><span>在第二阶段完成,</span></p><p><span>如果是复合类型, 就指向复合类型, 基本数据类型, 设计特定的编码, 设计一位表示数组类型</span>
 * </p><p><span>[</span><span>结构声明表开始标识</span><span>]</span></p>
 * <ul>
 *     <li><span>共计 共56Bit</span></li>
 *     <li><span>访问控制(8bit)</span></li>
 *     <li><span>类型id, 指向类型具体信息, (12Bit)</span></li>
 *     <li><span>变量名id, 指向常量池, (16bit)</span></li>
 *     <li><span>代码块实现/赋值ID, (16Bit)\</span></li>
 * </ul><h3 id='复合类型信息'><span>复合类型信息</span></h3><p><span>泛型或元组, 函数签名 ,基本数据类型, 设计特定的编码, 设计一位表示数组类型, (函数内函数...)也需要...</span>
 * </p><p><span>[</span><span>复合类型信息表开始标识</span><span>]</span></p>
 * <ul>
 *     <li><span>共计 16Bit+</span></li>
 *     <li><span>常量ID数量(12Bit)</span></li>
 *     <li><span>是泛型/元组/函数签名(4bit)</span></li>
 *     <li><span>常量池id列表或基本数据类型(16Bit)</span></li>
 * </ul><h3 id='代码块'><span>代码块</span></h3>
 * <blockquote><p><span>实现和赋值</span></p></blockquote>
 * <ul>
 *     <li><span>一个代码块的长度字节数</span><span>[</span><span>24Bit</span><span>]</span></li>
 *     <li><span>很长,指令( TODO )</span></li>
 *     <li><span>一个代码块中的源码表的长度</span></li>
 *     <li><span>每一个表达式在源码中的行信息要保留, 用于异常栈的输出</span></li>
 * </ul><h2 id='基本数据类型'><span>基本数据类型</span></h2>
 * <ol start=''>
 *     <li><span>import的类型</span></li>
 * </ol>
 * <figure>
 *     <table>
 *         <thead>
 *         <tr>
 *             <th><span>类型</span></th>
 *             <th><span>编码</span></th>
 *         </tr>
 *         </thead>
 *         <tbody>
 *         <tr>
 *             <td><span>import的类型</span></td>
 *             <td><span>0</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>bool</span></td>
 *             <td><span>1</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>char</span></td>
 *             <td><span>2</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>float32</span></td>
 *             <td><span>3</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>float64</span></td>
 *             <td><span>4</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>int8</span></td>
 *             <td><span>5</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>int16</span></td>
 *             <td><span>6</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>int32</span></td>
 *             <td><span>7</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>int64</span></td>
 *             <td><span>8</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>unsigned int8</span></td>
 *             <td><span>9</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>unsigned int16</span></td>
 *             <td><span>10</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>unsigned int32</span></td>
 *             <td><span>11</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>unsigned int64</span></td>
 *             <td><span>12</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>var</span></td>
 *             <td><span>13</span></td>
 *         </tr>
 *         <tr>
 *             <td><span>未确定类型</span></td>
 *             <td><span>14</span></td>
 *         </tr>
 *         </tbody>
 *     </table>
 * </figure><h2 id='var-类型'><span>var 类型</span></h2><p>
 *     <span>在解析代码编译的最终阶段, var类型将变为最终的合适的类型</span></p><p><span>如果是某个方法的返回值, 应当将方法的返回值导入(import), 放入import表中</span>
 * </p><h2 id='依赖影响'><span>依赖影响</span></h2><p><span>一个文件发生改变, 实现的改变不论, 但是方法/函数签名改变, 字段签名改变或类签名改变</span>
 * </p><p><span>都将导致这个类对引用它的对象造成改变, 要重新检查其他类型对它的引用是否正确</span></p><p><span>编译项目/多文件时, 总是在根目录创建一个依赖互相引用表(邻接矩阵表(吗?))</span>
 * </p><p><span>也在开头构造常量表, 然后获取常量表指向索引, 索引和索引构成二维矩阵, 0表示不引用, 1表示引用</span></p><p>
 *     <span>1Byte可以存8个, 真不错</span></p><h2 id='文件变化'><span>文件变化</span></h2><p>
 *     <span>发生实质性变化的时候进行编译(好难啊)</span></p><p>
 *     <span>我看Java, 改了空格或注释, 是会变的, 也会进行依赖分析</span></p><p><span>那咋办呢... 文件比对吗? 源码和字节码怎么比对? 阿西吧</span>
 * </p><p><span>本文件先进行一波编译, 然后编译了的和已经存在的比对吗? 比对不通过覆盖原字节码, 然后进行依赖分析吗? 🤔</span>
 * </p><h3 id='文件比对'><span>文件比对</span></h3><p>
 *     <span>注意到, 字节码文件的各种表, 它们是无序的, 比对的时候不能简单比较它们是否是相同的吧?</span></p><p><span>可以重写Hash和Equals吧, 然后用Set, 这样形成的Set表应该是相同的</span>
 * </p><p><span>代码块的话直接仔细比对(可能嵌套常量池比对)</span></p><p><span>常量池比对:</span></p>
 * <ul>
 *     <li><span>常量池-&gt;结构声明</span></li>
 *     <li><span>结构声明的方法-&gt;复合类型声明</span></li>
 *     <li><span>代泛型的类型-&gt;复合类型声明</span></li>
 *     <li><span>变量也可能指向复合类型声明, 可能是代泛型的类做类型</span></li>
 *     <li><span>函数不能直接做类型, 而是用函数类callable&lt;AAA,BBB&gt;这样</span></li>
 *     <li><span>比对常量池的时候, 只比对常量名</span></li>
 *     <li><span>然后指向结构, 比对结构</span></li>
 *     <li><span>结构指向复合类型或常量池</span></li>
 *     <li><span>结构如果指向常量池, 再次比对常量池的名字, 然后结束</span></li>
 *     <li><span>如果指向复合结构, 比对复合结构, 复合结构会指向常量池, 比对常量池然后结束</span></li>
 * </ul><p><span>一言以蔽之: </span></p>
 * <ul>
 *     <li><span>遍历常量池表, 递归嵌套比对, 以第二次比对常量池的数据为递归出口</span></li>
 * </ul><h2 id='难点'><span>难点</span></h2>
 * <ul>
 *     <li><p><span>文件内如何指向? 答: 每个部分建造一个表, 使用表内的索引</span></p></li>
 *     <li><p><span>源码分割设计? (48Bit+)</span></p>
 *         <ul>
 *             <li><span>raw (14bit)</span></li>
 *             <li><span>col (14bit)</span></li>
 *             <li><span>TYPE (8Bit)</span></li>
 *             <li><span>字符串长 (12Bit)</span></li>
 *             <li><span>字符串 (不定长)</span></li>
 *         </ul>
 *     </li>
 * </ul>
 */
package org.harvey.compiler.io;