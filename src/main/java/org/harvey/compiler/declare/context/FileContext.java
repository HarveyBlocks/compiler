package org.harvey.compiler.declare.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.define.FileDefinition;
import org.harvey.compiler.declare.define.StructureDefinition;
import org.harvey.compiler.declare.identifier.DefaultIdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.harvey.compiler.core.CoreCompiler.map2Context;

/**
 * 要便于序列化, 已经要有常量池的概念了...
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:36
 */
@Getter
@AllArgsConstructor
public class FileContext {
    // 包含enum, interface, struct
    // ---------------------第一阶段加载------------------------
    private final List<TypeAlias> aliasList;
    private final List<ReferenceElement> complexStructureTable;
    private final IdentifierManager identifierManager;
    // ---------------------第二阶段加载------------------------
    private final List<CallableContext> functionTable;
    // ---------------------第三阶段加载------------------------
    private final List<? extends List<SourceString>> executablePool;
    @Getter(AccessLevel.NONE)
    private final SourceStringContextPoolFactory poolFactory;

    public FileContext(FileDefinition body) {
        this.executablePool = new ArrayList<>();
        this.poolFactory = new SourceStringContextPoolFactory(executablePool);
        this.identifierManager = body.getIdentifierManager();
        this.aliasList = map2Context(body.getAliases(), d -> new TypeAlias(d, identifierManager));
        this.complexStructureTable = body.getStructures()
                .stream()
                .map(StructureDefinition::getIdentifierReference)
                .collect(Collectors.toList());
        this.functionTable = map2Context(
                body.getFunctions(), d -> new CallableContext(d, identifierManager, poolFactory,false));
    }

    public static FileContext empty() {
        return new FileContext(Collections.emptyList(), Collections.emptyList(),
                new DefaultIdentifierManager(Collections.emptyList(), 0, 0, new ArrayList<>()), Collections.emptyList(),
                Collections.emptyList(), null
        );
    }

    public FullIdentifierString getIdentifier(ReferenceElement reference) {
        return identifierManager.getIdentifier(reference);
    }
}
