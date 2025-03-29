package org.harvey.compiler.text.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 源码中的Structure
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:44
 */
@Getter
@AllArgsConstructor
public class SimpleStructure {
    private final int outerStructure;
    private final int depth;
    private final Declarable declarable;
    private final LinkedList<SourceTextContext> staticBlocks = new LinkedList<>();
    private final LinkedList<SourceTextContext> blocks = new LinkedList<>();

    private final List<Declarable> fieldTable;
    // 有随机访问需求
    private final List<DeclaredDepartedPart> methodTable;
    // 所有的类(包括内部类)存储在文件层
    // 所有方法(包括局部方法)存在类层
    // 所有函数(包括局部函数)存在文件层
    // 以此, 即使嵌套, 不过两层
    private final List<Integer> internalStructureReferenceList = new ArrayList<>();
    private final LinkedList<EnumConstantDeclarable> enumConstantDeclarableList;
    private final List<SourceTypeAlias> sourceTypeAliaseList;

    public SimpleStructure(
            int outerStructure, int depth, Declarable declarable,
            LinkedList<SimpleBlock> blocks,
            List<SourceTypeAlias> sourceTypeAliaseList,
            List<Declarable> fieldTable,
            List<DeclaredDepartedPart> methodTable) {
        this(outerStructure, depth, declarable, blocks, sourceTypeAliaseList, fieldTable, methodTable,
                null
        );
    }

    public SimpleStructure(
            int outerStructure, int depth, Declarable declarable,
            LinkedList<SimpleBlock> blocks,
            List<SourceTypeAlias> sourceTypeAliaseList,
            List<Declarable> fieldTable,
            List<DeclaredDepartedPart> methodTable,
            LinkedList<EnumConstantDeclarable> enumConstantDeclarableList) {
        this.outerStructure = outerStructure;
        this.depth = depth;
        this.declarable = declarable;
        this.sourceTypeAliaseList = sourceTypeAliaseList;
        this.fieldTable = fieldTable;
        this.methodTable = methodTable;
        for (SimpleBlock block : blocks) {
            (block.isStatic() ? this.staticBlocks : this.blocks).add(block.getBody());
        }
        this.enumConstantDeclarableList = enumConstantDeclarableList;
    }

    public final void registerInternalStructure(int index) {
        internalStructureReferenceList.add(index);
    }

    public boolean hasOuter() {
        return outerStructure >= 0;
    }

}
