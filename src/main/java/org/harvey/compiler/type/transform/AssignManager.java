package org.harvey.compiler.type.transform;

import org.harvey.compiler.exception.VieGenericAssignableException;
import org.harvey.compiler.execute.expression.KeywordString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;
import org.harvey.compiler.type.generic.relate.entity.RelatedLocalParameterizedType;
import org.harvey.compiler.type.generic.relate.entity.RelatedParameterizedType;

import java.io.IOException;

/**
 * TODO
 * 类型匹配
 * <h2 id='类型转换'><span>类型转换</span></h2>
 * <h3 id='带泛型的类型之间的转换'><span>带泛型的类型之间的转换</span></h3>
 * <p><span>要使得 FROM&lt;T [define of T]&gt; 转换成 TO&lt;T [define of T]&gt;</span></p>
 * <p><span>即</span><code>FROM&lt;T [define of T]&gt; &lt;= TO&lt;T [define of T]&gt;</code></p>
 * <ol start=''>
 *     <li><span>FROM &lt;= TO</span></li>
 *     <li><span>对于任意i, FROM.T[i] &lt;= TO.T[i]. 见</span><a href='#泛型参数和泛型阐述之间的类型转换'><span>泛型参数转换</span></a>
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
 * <h3 id='泛型参数和泛型阐述之间的类型转换'><span>泛型参数和泛型阐述之间的类型转换</span></h3>
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
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 14:10
 */
public interface AssignManager {

    /**
     * lower<=default<=任意Upper
     * <pre>{@code
     *  if(default < lower):
     *      throw ERROR
     *  for upper in uppers:
     *      if(lower > upper):
     *          throw ERROR
     *      if(default>upper):
     *          throw ERROR
     * }</pre>
     */
    void selfConsistent(RelatedGenericDefine define);

    /**
     * <pre>{@code
     * rawType = type.getRawType
     * genericDefines = find(rawType)
     * for i in range(genericDefines.length):
     *      child = type.getChild(i);
     *      define = genericDefines(i);
     *      if !assignable(from=child, to=define):
     *          throw ERROR
     * }</pre>
     */
    void selfConsistent(RelatedParameterizedType type) throws IOException;

    /**
     * <p>to value = new from();</p>
     * <p>from <= to</p>
     *
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
     */
    void assignable(RelatedGenericDefine to, RelatedGenericDefine from);


    /**
     * <p>to value = new from();</p>
     * <p>from <= to</p>
     *
     * <h3 id='泛型参数和普通类型可能带泛型之间的转换'><span>泛型参数和普通类型(可能带泛型)之间的转换</span></h3>
     * <p><span>要使得定义为</span></p>
     * <p><code>Lower&lt;=T&lt;=Upper &amp; T &lt;= Interfaces &amp; new&lt;....&gt; in T</code></p>
     * <p><span>的泛型和类型Obj相互转化</span></p>
     * <ul>
     *     <li><p><span>泛型参数转化为类型</span></p>
     *         <p><span>即</span><code>FROM [define of FROM] &lt;= TO</code></p>
     *         <ol start=''>
     *             <li><span>FROM &gt;= TO.Upper</span></li>
     *             <li><span>或, 存在i, 使得FROM &gt;= TO.Interface[i]</span></li>
     *         </ol>
     *     </li>
     * </ul>
     */
    void assignable(RelatedParameterizedType to, RelatedGenericDefine from);

    /**
     * <p>to value = new from();</p>
     * <p>from <= to</p>
     *
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
     * </ul>
     */
    void assignable(RelatedGenericDefine to, RelatedParameterizedType from);

    /**
     * <p>to value = new from();</p>
     * <p>from <= to</p>
     */
    default void assignable(
            RelatedLocalParameterizedType to,
            RelatedLocalParameterizedType from,
            SourcePosition fromPosition)
            throws VieGenericAssignableException {
        if (from.isFinalMark() && !to.isConstMark()) {
            throw new VieGenericAssignableException(fromPosition, "not allowed assign from const to not const");
        }
        assignable(to.getType(), from.getType());
    }

    /**
     * <p>to value = new from();</p>
     * <p>from <= to</p>
     *
     * <h3 id='带泛型的类型之间的转换'><span>带泛型的类型之间的转换</span></h3>
     * <p><span>要使得 FROM&lt;T [define of T]&gt; 转换成 TO&lt;T [define of T]&gt;</span></p>
     * <p><span>即</span><code>FROM&lt;T [define of T]&gt; &lt;= TO&lt;T [define of T]&gt;</code></p>
     * <ol start=''>
     *     <li><span>FROM &lt;= TO</span></li>
     *     <li><span>对于任意i, FROM.T[i] &lt;= TO.T[i]. 见</span><a href='#泛型参数和泛型阐述之间的类型转换'><span>泛型参数转换</span></a>
     *     </li>
     * </ol>
     */
    void assignable(RelatedParameterizedType to, RelatedParameterizedType from);
}

