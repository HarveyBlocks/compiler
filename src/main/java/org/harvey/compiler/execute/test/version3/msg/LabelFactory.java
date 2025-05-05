package org.harvey.compiler.execute.test.version3.msg;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-07 21:11
 */
@SuppressWarnings("DuplicatedCode")
public class LabelFactory {
    private int id;

    public Label create() {
        return new Label(id++);
    }
}
