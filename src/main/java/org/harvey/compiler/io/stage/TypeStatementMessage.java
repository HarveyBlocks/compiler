package org.harvey.compiler.io.stage;

import lombok.Getter;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.depart.DeclaredDepartedPart;
import org.harvey.compiler.depart.RecursivelyDepartedBody;
import org.harvey.compiler.depart.SimpleComplexStructure;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 01:31
 */
@Getter
public class TypeStatementMessage {
    private final Map<String, ImportContext> importTable;
    private final Set<String> identifierSet = new HashSet<>();
    private final List<DeclaredDepartedPart> functionTable;
    private final List<SimpleComplexStructure> structureTable;


    public TypeStatementMessage(RecursivelyDepartedBody body) {
        this.importTable = body.getImportTable();

        this.functionTable = body.getCallableList();
        this.structureTable = body.getSimpleComplexStructureList();
        for (DeclaredDepartedPart callable : this.functionTable) {
            identifierSet.add(callable.getStatement().getIdentifier().getValue());
        }
        for (SimpleComplexStructure structure : this.structureTable) {
            identifierSet.add(structure.getDeclarable().getIdentifier().getValue());
        }
    }
}
