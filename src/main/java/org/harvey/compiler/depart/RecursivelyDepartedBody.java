package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.context.ImportContext;

import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:45
 */
@Getter
@AllArgsConstructor
public class RecursivelyDepartedBody {
    private final Map<String, ImportContext> importTable;
    private final List<SourceTypeAlias> aliasList;
    // 有随机访问需求
    private final List<DeclaredDepartedPart> callableList;
    private final List<SimpleComplexStructure> simpleComplexStructureList;


}
