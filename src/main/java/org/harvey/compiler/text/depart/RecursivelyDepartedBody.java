package org.harvey.compiler.text.depart;

import lombok.Getter;
import org.harvey.compiler.declare.context.ImportString;

import java.util.List;
import java.util.Map;

/**
 * 递归状态的结构, 转成常数级可序列化结构
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:45
 */
@Getter
public class RecursivelyDepartedBody {


    private final Map<String, ImportString> importTable;
    private final List<SourceTypeAlias> aliasList;
    // 有随机访问需求
    private final List<DeclaredDepartedPart> callableList;
    private final List<SimpleStructure> simpleStructureList;


    public RecursivelyDepartedBody(
            Map<String, ImportString> importTable, List<SourceTypeAlias> aliasList,
            List<DeclaredDepartedPart> callableList,
            List<SimpleStructure> simpleStructureList) {
        this.importTable = importTable;
        this.aliasList = aliasList;
        this.callableList = callableList;
        this.simpleStructureList = simpleStructureList;
    }

}

