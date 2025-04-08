package org.harvey.compiler.execute.test.version2.msg;
/**
 * TODO  
 * 
 * @date    2025-04-07 21:11
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class LabelFactory {
    private int id;

    public Label create() {
        return new Label(id++);
    }
}
