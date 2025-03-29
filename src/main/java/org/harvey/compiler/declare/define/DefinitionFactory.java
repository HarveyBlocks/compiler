package org.harvey.compiler.declare.define;

import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.analysis.Environment;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.identifier.DefaultIdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierPoolFactory;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.depart.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于完成对Declare的Identifier的引用转换的工厂类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:05
 */
public class DefinitionFactory {

    // 文件内部声明的Identifier


    public DefinitionFactory() {
    }

    public static Environment getEnvironment(StructureDefinition outerStructure) {
        if (outerStructure == null) {
            return Environment.FILE;
        }
        Embellish embellish = outerStructure.getEmbellish();
        StructureType type = outerStructure.getType();
        return getEnvironment(embellish, type);
    }

    public static Environment getEnvironment(Embellish embellish, StructureType type) {
        boolean markedAbstract = embellish.isMarkedAbstract();
        switch (type) {
            case ENUM:
                return Environment.ENUM;
            case CLASS:
                return markedAbstract ? Environment.ABSTRACT_CLASS : Environment.CLASS;
            case STRUCT:
                return markedAbstract ? Environment.ABSTRACT_STRUCT : Environment.CLASS;
            case INTERFACE:
                return Environment.INTERFACE;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private static StructureDefinition getOuterStructure(
            int outerStructureReference, List<StructureDefinition> structureDefinitions) {
        return outerStructureReference == RecursivelyDepartedBodyFactory.FILE_OUTER ? null :
                structureDefinitions.get(outerStructureReference);
    }

    public static CallableDefinition buildCallableDefinition(
            DeclaredDepartedPart function,
            IdentifierPoolFactory identifierPoolFactory,
            Environment environment,
            Stack<ReferenceElement> outerReferenceStack) {
        Declarable statement = function.getStatement();
        SourcePosition forEmpty = statement.getStart();
        ListIterator<SourceString> attachmentIterator = statement.getAttachment().listIterator();
        return new CallableDefinition.Builder(identifierPoolFactory, environment).identifierReference(
                        statement.getIdentifier(), forEmpty, statement.getType(), statement.getStart())
                // 需要reference来判断是不是oper
                .embellish(statement.getEmbellish()) // 需要oper来判断需不需要加Const
                .permission(statement.getPermissions()) // 需要embellish来判断abstract和private的矛盾
                .returnTypes(statement.getType(), forEmpty) // 需要identifier是否是 constructor 来获取generic message
                .genericDefine(attachmentIterator, outerReferenceStack) // 需要return type, 因为可能是 constructor
                .paramList(attachmentIterator)
                .throwsList(attachmentIterator)
                .noMore(attachmentIterator)
                .body(function.getBody())
                .build();
    }

    public static AliasDefinition buildAliasDefinition(
            SourceTypeAlias alias,
            IdentifierPoolFactory identifierPoolFactory,
            Environment environment,
            Stack<ReferenceElement> outerReferenceStack) {
        return new AliasDefinition.Builder(identifierPoolFactory, environment).permissions(
                        environment, alias.getPermissions())
                .staticAlias(alias.getStaticPosition())
                .identifierReference(alias.getIdentifier())
                .genericDefine(alias.getGenericMessage(), outerReferenceStack)
                .origin(alias.getOrigin())
                .build();
    }

    public static FieldDefinition buildFieldDefinition(
            Declarable field, Environment environment, IdentifierPoolFactory identifierPoolFactory) {
        SourceTextContext assigns = field.getAttachment();
        assigns.addFirst(field.getIdentifier());
        ListIterator<SourceString> assignIterator = assigns.listIterator();
        return new FieldDefinition.Builder(identifierPoolFactory, environment).permission(field.getPermissions())
                .embellish(field.getEmbellish())
                .type(field.getType())
                .assignMaps(assignIterator)
                .noMore(assignIterator)
                .build();
    }

    /**
     * 分析GenericMessage的阶段
     * 分析IdentifierMessage的阶段必须是同一个阶段
     * 而其中必须有一个中间类
     * 分析声明->分析表达式
     * 泛型定义中也会有泛型使用
     * 如果全部加入泛型定义, 后面的泛型可以用到前面的泛型, 前面的泛型可以用到后面的泛型吗
     * 非法向前引用
     *
     * @param filePath 去除后缀
     */
    public FileDefinition buildReferredDepartedBody(
            String filePath, RecursivelyDepartedBody body) {
        String filePathPre = filePath + IdentifierPoolFactory.MEMBER;
        IdentifierPoolFactory identifierPoolFactory = new IdentifierPoolFactory(filePathPre);
        // generic message可以有generic define
        List<AliasDefinition> aliases = body.getAliasList()
                .stream()
                .map(e -> buildAliasDefinition(e, identifierPoolFactory, Environment.FILE, new Stack<>()))
                .collect(Collectors.toList());
        List<CallableDefinition> functions = body.getCallableList()
                .stream()
                .map(function -> buildCallableDefinition(function, identifierPoolFactory, Environment.FILE,
                        new Stack<>()
                ))
                .collect(Collectors.toList());
        List<StructureDefinition> structureDefinitions = new ArrayList<>();
        Map<String, ImportString> importTable = body.getImportTable();
        for (SimpleStructure structure : body.getSimpleStructureList()) {
            StructureDefinition definition = buildStructureDefinition(
                    structure, structureDefinitions, identifierPoolFactory, importTable);
            structureDefinitions.add(definition);
        }
        return new FileDefinition(aliases, structureDefinitions, functions,
                new DefaultIdentifierManager(importTable,
                        identifierPoolFactory.getDeclaredIdentifierPool(), identifierPoolFactory.getPreLength()
                )
        );
    }

    public StructureDefinition buildStructureDefinition(
            SimpleStructure structure,
            List<StructureDefinition> structureDefinitions,
            IdentifierPoolFactory identifierPoolFactory,
            Map<String, ImportString> importTable) {
        StructureDefinition outerStructure = getOuterStructure(structure.getOuterStructure(), structureDefinitions);
        Declarable statement = structure.getDeclarable();
        Environment environment = getEnvironment(outerStructure);
        ListIterator<SourceString> attachmentIterator = statement.getAttachment().listIterator();
        Stack<ReferenceElement> referenceStack = outerStaticReferenceStack(outerStructure, structureDefinitions);
        return new StructureDefinition.Builder(environment, identifierPoolFactory).outerStructure(
                        structure.getOuterStructure())
                .depth(structure.getDepth())
                .permissions(statement.getPermissions())
                .embellish(statement.getEmbellish())
                .type(statement.getType())
                .identifierReference(statement.getIdentifier())
                .identifierPoolFactoryForInner()
                .referenceStack(referenceStack)
                .genericDefine(attachmentIterator)
                .superType(attachmentIterator)
                .implementsList(attachmentIterator)
                .noMore(attachmentIterator)
                .enumConstants(structure.getEnumConstantDeclarableList())
                .notStaticBlocks(structure.getBlocks())
                .staticBlocks(structure.getStaticBlocks())
                .aliases(structure.getSourceTypeAliaseList())
                .fields(structure.getFieldTable())
                .methods(structure.getMethodTable())
                .innerStructures(structure.getInternalStructureReferenceList())
                .identifierManager(importTable)
                .build();
    }

    private Stack<ReferenceElement> outerStaticReferenceStack(
            StructureDefinition outerStructure, List<StructureDefinition> structureDefinitions) {
        Stack<ReferenceElement> stack = new Stack<>();
        StructureDefinition cur = outerStructure;
        while (cur != null) {
            if (!cur.getEmbellish().isMarkedStatic()) {
                break;
            }
            stack.push(cur.getIdentifierReference());
            cur = getOuterStructure(outerStructure.getOuterStructure(), structureDefinitions);
        }
        return stack;
    }

}
