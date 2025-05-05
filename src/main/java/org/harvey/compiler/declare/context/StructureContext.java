package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.define.Definition;
import org.harvey.compiler.declare.define.StructureDefinition;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.RawType;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 复合体 * * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a> * @version 1.0 * @date 2024-11-21 15:40
 */
@EqualsAndHashCode
@Data
@AllArgsConstructor
public class StructureContext implements DeclaredContext {

    // ----------------------------第一阶段加载----------------------------
    private final int outerStructure;
    /**
     * 对于outer是-1的, depth=0
     */
    private final int depth;
    private final AccessControl accessControl;
    private final Embellish embellish;
    private final StructureType type;
    private final ReferenceElement identifierReference;
    /**
     * 不可以为null. 包含`<>`
     */
    private final GenericDefine[] genericMessage;
    /**
     * nonnull
     */
    private final ParameterizedType<ReferenceElement> superStructure;
    private final List<ConstructorContext> constructors;
    /**
     * 在编译器中的Extends的概念是特指单继承, 而不是指extends后的继承, 也就是说, <pre>{@code
     * interface extends interface[1],interface[2],interface[3]
     * }</pre>,应该算到implement里面去
     */
    private final List<ParameterizedType<ReferenceElement>> interfaceList;
    private final DIdentifierManager manager;
    private final List<TypeAlias> typeAliases;
    private final List<Integer> innerStructureReferences;
    // ----------------------------第二阶段加载----------------------------
    private final List<EnumConstantContext> enumConstants;
    private final List<ValueContext> fieldTable;
    private final List<CallableContext> methodTable;
    // ----------------------------第三阶段加载----------------------------
    private final List<Integer> blocks;
    private final List<Integer> staticBlocks;
    public final List<? extends List<SourceString>> executablePool;
    public final SourceStringContextPoolFactory poolFactory;

    /**
     * 指向外面(一层), null指外面一层是文件
     */


    public StructureContext(StructureDefinition definition) {
        this.executablePool = new ArrayList<>();
        this.poolFactory = new SourceStringContextPoolFactory(executablePool);
        this.manager = definition.getIdentifierManager();
        this.identifierReference = definition.getIdentifierReference();
        this.accessControl = definition.getPermissions();
        this.embellish = definition.getEmbellish();
        this.type = definition.getType();
        this.depth = definition.getDepth();
        this.outerStructure = definition.getOuterStructure();
        this.genericMessage = map2Context(
                definition.getGenericDefine(),
                p -> GenericFactory.genericForDefine(p.getKey(),
                        p.getValue(), manager
                )
        ).toArray(GenericDefine[]::new);
        // superComplexStructure.rawType
        this.superStructure = definition.getSuperType() == null ? ParameterizedType.empty() :
                getParameterizedTypeAsUpper(definition.getSuperType(), manager);
        this.interfaceList = map2Context(definition.getImplementsList(), d -> getParameterizedTypeAsUpper(d, manager));
        this.enumConstants = definition.getEnumConstants()
                .stream()
                .map(e -> new EnumConstantContext(e, poolFactory))
                .collect(Collectors.toList());
        this.typeAliases = map2Context(definition.getAlias(), d -> new TypeAlias(d, manager));
        this.fieldTable = map2Context(definition.getFields(), d -> new ValueContext(d, manager, poolFactory));
        this.methodTable = new ArrayList<>();
        this.constructors = new ArrayList<>();
        List<CallableContext> callables = map2Context(
                definition.getMethods(),
                d -> new CallableContext(d, manager, poolFactory, d.getType() == CallableType.CONSTRUCTOR)
        );
        for (CallableContext callable : callables) {
            if (callable.getType() == CallableType.CONSTRUCTOR) {
                this.constructors.add(new ConstructorContext(callable));
            } else {
                this.methodTable.add(callable);
            }
        }
        this.blocks = definition.getNotStaticBlocks().stream().map(poolFactory::add).collect(Collectors.toList());
        this.staticBlocks = definition.getStaticBlocks().stream().map(poolFactory::add).collect(Collectors.toList());
        this.innerStructureReferences = definition.getInnerStructures();
        validUpper();
    }

    private static <R, P> List<R> map2Context(List<P> sources, Function<P, R> mapper) {
        return sources.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * @param superSource nullable
     */
    private static ParameterizedType<ReferenceElement> getParameterizedTypeAsUpper(
            Pair<RawType, SourceTextContext> superSource, DIdentifierManager manager) {
        if (superSource == null) {
            return null;
        }
        ReferenceElement reference = GenericFactory.rawType2Reference(superSource.getKey(), manager);
        if (reference.getType() == ReferenceType.GENERIC_IDENTIFIER) {
            throw new AnalysisDeclareException(reference.getPosition(), "can not be a generic");
        }
        ListIterator<SourceString> it = superSource.getValue().listIterator();
        ParameterizedType<ReferenceElement> parameterizedType = GenericFactory.parameterizedType(
                reference, it, manager);
        if (it.hasNext()) {
            throw new AnalysisDeclareException(it.previous().getPosition(), "expected {");
        }
        return parameterizedType;
    }

    private void validUpper() {
        for (ParameterizedType<ReferenceElement> each : this.interfaceList) {
            validUpperType(each.getRawType());
        }
        if (superStructure != null) {
            validUpperType(superStructure.getRawType());
        }
    }

    private void validUpperType(ReferenceElement upperReference) {
        // 内部类不能是upper
        Definition.notNullValid(identifierReference, "identifier reference");
        if (upperReference.getType() != ReferenceType.IDENTIFIER) {
            throw new AnalysisDeclareException(
                    upperReference.getPosition(),
                    upperReference.getType() + " can not be upper"
            );
        }
        FullIdentifierString upper = manager.getIdentifier(upperReference);
        FullIdentifierString now = manager.getIdentifier(identifierReference);
        int firstDifferenceIndex = now.firstDifferenceIndex(upper);
        if (firstDifferenceIndex == now.length()) {
            throw new AnalysisDeclareException(upper.getPosition(), "structure's member(or self) can not be upper.");
        }
    }


}