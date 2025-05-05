package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.identifier.DIdentifierManager;

import java.util.List;

/**
 * 对文件内所有成员的, 从identifier到引用的转化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:04
 */
@Getter
@AllArgsConstructor
public class FileDefinition {
    private final List<AliasDefinition> aliases;
    private final List<StructureDefinition> structures;
    private final List<CallableDefinition> functions;
    private final DIdentifierManager identifierManager;
}
