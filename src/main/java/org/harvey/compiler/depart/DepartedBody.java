package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.context.ImportContext;

import java.util.LinkedList;
import java.util.Map;

/**
 * 文件信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 23:38
 */
@AllArgsConstructor
@Getter
public class DepartedBody {
    private final Map<String, ImportContext> importTable;
    private final LinkedList<SimpleBlock> blocks;
    // 可能是抽象函数/alias/字段
    private final LinkedList<Declarable> declarableSentenceList;
    private final LinkedList<DeclaredDepartedPart> declarableRecursiveList;
}


