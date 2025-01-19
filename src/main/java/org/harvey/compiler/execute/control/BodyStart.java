package org.harvey.compiler.execute.control;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO  
 *
 * @date 2025-01-08 23:35
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
@Setter
public class BodyStart extends Executable {
    public static final int NOT_CONFIRMED_REFERENCE = -1;
    private int bodyEnd = NOT_CONFIRMED_REFERENCE;
}
