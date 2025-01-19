package org.harvey.compiler.io.source;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-25 01:03
 */
public class AssignTempVariable extends SourceString {

    public AssignTempVariable(SourcePosition position) {
        super(SourceStringType.ASSIGN_TEMP, "__temp__st__store__", position);
    }
}
