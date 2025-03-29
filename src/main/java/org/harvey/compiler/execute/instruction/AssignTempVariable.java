package org.harvey.compiler.execute.instruction;

import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 01:03
 */
public class AssignTempVariable extends SourceString {

    public AssignTempVariable(SourcePosition position) {
        super(SourceType.ASSIGN_TEMP, "__temp__st__store__", position);
    }
}
