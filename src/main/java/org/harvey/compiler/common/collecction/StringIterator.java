package org.harvey.compiler.common.collecction;

/**
 * 能够随机访问string的iterator
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:00
 */
public class StringIterator extends DefaultRandomlyIterator<Character> {
    private final String str;

    public StringIterator(String value) {
        super(RandomlyAccessAble.forString(value), 0);
        this.str = value;
    }

    /**
     * 用`next`还没有遍历过的部分
     */
    public String stringAfter() {
        return str.substring(index);
    }

    public String stringPrevious() {
        return str.substring(0, index);
    }

    @Override
    public String toString() {
        return str;
    }
}
