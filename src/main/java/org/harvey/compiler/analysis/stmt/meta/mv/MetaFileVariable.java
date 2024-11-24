package org.harvey.compiler.analysis.stmt.meta.mv;

import lombok.Getter;
import lombok.ToString;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * <li><p><span>关于全局变量</span></p>
 *      <ol start=''>
 *          <li><span>C/C++: 允许声明, 允许声明＋赋值, 不允许直接赋值</span></li>
 *          <li><span>Python: 允许任意的语句</span></li>
 *          <li><span>C#/Java: 不允许全局变量</span></li>
 *          <li><span>GO: ?</span></li>
 *      </ol>
 *  </li>
 * <li><p><span>FileVariable</span></p>
 *     <ol start=''>
 *         <li><span>文件层作用域{@link org.harvey.compiler.analysis.core.AccessControl}</span></li>
 *         <li><span>[const|final]</span></li>
 *         <li><span>类型</span></li>
 *         <li><span>用逗号分割的多个identifier和不定的赋值语句</span></li>
 *     </ol>
 * </li>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:41
 */
@Getter
@ToString(callSuper = true)
public class MetaFileVariable extends MetaValue {

    public static final AccessControl.Permission DEFAULT_ACCESS_CONTROL_PERMISSION = AccessControl.Permission.FILE;
    private SourceTextContext assignExpression;

    protected MetaFileVariable() {
        super();
        assignExpression = null;
    }

    public static class Builder extends MetaValue.Builder<MetaFileVariable, Builder> {

        public Builder() {
            super(new MetaFileVariable());
        }

        public Builder assignExpression(SourceTextContext assignExpression) {
            product.assignExpression = assignExpression;
            return this;
        }
    }

}
