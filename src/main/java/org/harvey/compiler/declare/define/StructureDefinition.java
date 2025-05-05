package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.analysis.*;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.declare.identifier.DIdentifierPoolFactory;
import org.harvey.compiler.declare.identifier.DeprecatedIdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.depart.DeclaredDepartedPart;
import org.harvey.compiler.text.depart.SourceTypeAlias;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.RawType;

import java.util.*;
import java.util.stream.Collectors;

import static org.harvey.compiler.text.depart.RecursivelyDepartedBodyFactory.UNSURE_OUTER;

/**
 * 完成对Structure内的所有成员的identifier到reference的转化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:06
 */
@AllArgsConstructor
@Getter
public class StructureDefinition implements Definition {

    // 声明
    private final DIdentifierManager identifierManager;
    private final AccessControl permissions;
    private final Embellish embellish;
    private final StructureType type;
    private final ReferenceElement identifierReference;
    private final List<Pair<IdentifierString, SourceTextContext>> genericDefine;
    /**
     * super  nullable
     */
    private final Pair<RawType, SourceTextContext> superType;
    private final List<Pair<RawType, SourceTextContext>> implementsList;
    // 结构
    // reference-arguments list
    private final LinkedList<EnumConstant> enumConstants;
    private final List<SourceTextContext> staticBlocks;
    private final List<SourceTextContext> notStaticBlocks;
    private final List<AliasDefinition> alias;
    private final List<FieldDefinition> fields;
    private final List<CallableDefinition> methods;
    private final int outerStructure;
    private final int depth;
    private final List<Integer> innerStructures;


    public static class Builder {
        private final Environment thisEnvironment;
        private DIdentifierPoolFactory identifierPoolFactoryForInner;
        // 声明
        private DIdentifierManager identifierManager;
        private AccessControl permissions;
        private Embellish embellish;
        private StructureType type;
        private ReferenceElement identifierReference;
        private List<Pair<IdentifierString, SourceTextContext>> genericDefine;
        private Pair<RawType, SourceTextContext> superType;
        private List<Pair<RawType, SourceTextContext>> implementsList;
        // 结构
        private LinkedList<EnumConstant> enumConstants;
        private List<SourceTextContext> staticBlocks;
        private List<SourceTextContext> notStaticBlocks;
        private List<AliasDefinition> aliases;
        private List<FieldDefinition> fields;
        private List<CallableDefinition> methods;
        private List<Integer> innerStructures;
        // 关系
        private int outerStructure = UNSURE_OUTER;
        private int depth = -1;
        private Environment environmentToInner;

        public Builder(Environment thisEnvironment) {
            this.thisEnvironment = thisEnvironment;
        }

        public Builder identifierManager(Map<String, ImportString> importTable) {
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            this.identifierManager = new DeprecatedIdentifierManager(
                    importTable,
                    identifierPoolFactoryForInner.getDeclaredIdentifierPool(),
                    identifierPoolFactoryForInner.getPreLength()
            );
            return this;
        }


        public Builder identifierReference(DIdentifierPoolFactory identifierPoolFactory, SourceString identifier) {
            this.identifierReference = identifierPoolFactory.addIdentifier(DetailedDeclarationType.STRUCTURE,
                    identifier.getValue(), identifier.getPosition()
            );
            return this;
        }


        public Builder identifierPoolFactoryForInner(DIdentifierPoolFactory identifierPoolFactory) {
            Definition.notNullValid(identifierReference, "identifier reference");
            identifierPoolFactoryForInner = identifierPoolFactory.cloneForInner(identifierReference);
            return this;
        }

        public Builder permissions(SourceTextContext permissions) {
            this.permissions = AccessControls.buildAccessControl(thisEnvironment, permissions);
            return this;
        }

        public Builder embellish(EmbellishSource source) {
            DetailedDeclarationType onDefault = DetailedDeclarationType.onDefaultForMember(0, thisEnvironment);
            DetailedDeclarationType onIllegal;
            if (thisEnvironment == Environment.FILE) {
                onIllegal = DetailedDeclarationType.onIllegalInFile(0);
            } else {
                onIllegal = DetailedDeclarationType.onIllegalInStructure(0);
            }
            embellish = Embellish.create(onDefault, onIllegal, source, false, false);
            return this;
        }

        public Builder type(SourceTextContext type) {
            if (type.size() != 1) {
                throw new AnalysisDeclareException(
                        type.getFirst().getPosition(), type.getLast().getPosition(), "expected class");
            }
            SourceString mayType = type.get(0);
            if (mayType.getType() != SourceType.KEYWORD && !Keywords.isStructure(mayType.getValue())) {
                throw new AnalysisDeclareException(mayType.getPosition(), "expected class");
            }
            Keyword keyword = Keyword.get(mayType.getValue());
            if (!Keywords.isStructure(keyword)) {
                throw new AnalysisDeclareException(mayType.getPosition(), "expected class");
            }
            this.type = StructureType.get(keyword);
            return this;
        }


        public Builder genericDefine(ListIterator<SourceString> attachmentIterator) {
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            Definition.notNullValid(embellish, "embellish");
            if (!GenericFactory.genericPreCheck(attachmentIterator)) {
                this.genericDefine = Collections.emptyList();
                return this;
            }
            this.genericDefine = GenericFactory.defineSourceDepart(attachmentIterator);
            Definition.notRepeat(this.genericDefine);
            // 同级不存在
            return this;
        }


        public Builder superType(ListIterator<SourceString> attachmentIterator) {
            if (!attachmentIterator.hasNext()) {
                return this;
            }
            SourceString meyExtends = attachmentIterator.next();
            if (meyExtends.getType() != SourceType.KEYWORD || !Keyword.EXTENDS.equals(meyExtends.getValue())) {
                this.superType = null;
                return this;
            }
            Definition.notNullValid(type, "structure type");
            if (type == StructureType.ENUM) {
                throw new AnalysisDeclareException(meyExtends.getPosition(), "enum can not extends");
            }
            if (type == StructureType.INTERFACE) {
                this.implementsList = new ArrayList<>();
                setInterfaces(attachmentIterator);
            } else {
                this.superType = GenericFactory.skipSourceForUse(attachmentIterator);
            }

            return this;
        }


        public Builder implementsList(ListIterator<SourceString> attachmentIterator) {
            if (!attachmentIterator.hasNext()) {
                this.implementsList = Collections.emptyList();
                return this;
            }
            SourceString mayImplements = attachmentIterator.next();
            if (mayImplements.getType() != SourceType.KEYWORD || !Keyword.IMPLEMENTS.equals(mayImplements.getValue())) {
                this.implementsList = Collections.emptyList();
                return this;
            }
            Definition.notNullValid(type, "structure type");
            if (type == StructureType.INTERFACE) {
                throw new AnalysisDeclareException(
                        mayImplements.getPosition(), "please use " + Keyword.EXTENDS.getValue());
            }
            this.implementsList = new ArrayList<>();
            setInterfaces(attachmentIterator);
            return this;
        }

        private void setInterfaces(ListIterator<SourceString> attachmentIterator) {

            while (attachmentIterator.hasNext()) {
                Pair<RawType, SourceTextContext> eachThrows = GenericFactory.skipSourceForUse(attachmentIterator);
                if (Definition.skipIf(attachmentIterator, Operator.COMMA)) {
                    implementsList.add(eachThrows);
                } else {
                    break;
                }
            }
        }

        public Builder noMore(ListIterator<SourceString> attachmentIterator) {
            if (attachmentIterator.hasNext()) {
                throw new AnalysisDeclareException(attachmentIterator.next().getPosition(), "expected {");
            }
            return this;
        }

        public Builder outerStructure(int outerReference) {
            this.outerStructure = outerReference;
            return this;
        }

        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder enumConstants(LinkedList<EnumConstantDeclarable> enumConstantDeclarableList) {
            this.enumConstants = new LinkedList<>();
            if (enumConstantDeclarableList == null) {
                return this;
            }
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            for (EnumConstantDeclarable declarable : enumConstantDeclarableList) {
                IdentifierString name = declarable.getName();
                ReferenceElement reference = identifierPoolFactoryForInner.addIdentifier(DetailedDeclarationType.FIELD,
                        name.getValue(), name.getPosition()
                );
                enumConstants.add(new EnumConstant(reference, declarable.getArgumentList()));
            }
            return this;
        }

        public Builder staticBlocks(LinkedList<SourceTextContext> staticBlocks) {
            this.staticBlocks = staticBlocks;
            return this;
        }


        public Builder notStaticBlocks(LinkedList<SourceTextContext> blocks) {
            this.notStaticBlocks = blocks;
            return this;
        }

        public Builder aliases(List<SourceTypeAlias> sourceTypeAliaseList) {
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            if (this.environmentToInner == null) {
                Definition.notNullValid(embellish, "embellish");
                Definition.notNullValid(type, "type");
                environmentToInner = DefinitionFactory.getEnvironment(embellish, type);
            }
            this.aliases = sourceTypeAliaseList.stream()
                    .map(a -> DefinitionFactory.buildAliasDefinition(a, identifierPoolFactoryForInner,
                            environmentToInner
                    ))
                    .collect(Collectors.toList());
            return this;
        }

        public Builder fields(List<Declarable> fieldTable) {
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            if (this.environmentToInner == null) {

                Definition.notNullValid(embellish, "embellish");
                Definition.notNullValid(type, "type");
                environmentToInner = DefinitionFactory.getEnvironment(embellish, type);
            }
            fields = fieldTable.stream()
                    .map(f -> DefinitionFactory.buildFieldDefinition(f, environmentToInner,
                            identifierPoolFactoryForInner
                    ))
                    .collect(Collectors.toList());
            return this;
        }


        public Builder methods(List<DeclaredDepartedPart> methodTable) {
            Definition.notNullValid(identifierPoolFactoryForInner, "identifier pool factory for inner");
            if (this.environmentToInner == null) {
                Definition.notNullValid(embellish, "embellish");
                Definition.notNullValid(type, "type");
                environmentToInner = DefinitionFactory.getEnvironment(embellish, type);
            }
            this.methods = methodTable.stream()
                    .map(c -> DefinitionFactory.buildCallableDefinition(c, identifierPoolFactoryForInner,
                            environmentToInner
                    ))
                    .collect(Collectors.toList());
            return this;
        }

        public Builder innerStructures(List<Integer> references) {
            this.innerStructures = references;
            return this;
        }


        public StructureDefinition build() {
            valid();
            return new StructureDefinition(identifierManager, permissions, embellish, type, identifierReference,
                    genericDefine, superType, implementsList, enumConstants, staticBlocks, notStaticBlocks, aliases,
                    fields, methods, outerStructure, depth, innerStructures
            );
        }

        private void valid() {
            Definition.notNullValid(identifierReference, "identifier reference");
            Definition.notNullValid(identifierManager, "identifier manager");
            Definition.notNullValid(permissions, "permissions");
            Definition.notNullValid(embellish, "embellish");
            Definition.notNullValid(type, "type");
            Definition.notNullValid(genericDefine, "generic define");
            // super 可以为null
            // Definition.notNullValid(superType, "super type");
            Definition.notNullValid(implementsList, "implements lists");
            Definition.notNullValid(enumConstants, "enum constants lists");
            Definition.notNullValid(staticBlocks, "static blocks");
            Definition.notNullValid(notStaticBlocks, "not static blocks");
            Definition.notNullValid(aliases, "aliases");
            Definition.notNullValid(fields, "fields");
            Definition.notNullValid(methods, "methods");
            Definition.notNullValid(aliases, "aliases");
            Definition.notNullValid(innerStructures, "inner structures");
            Definition.notEqualsValid(outerStructure, UNSURE_OUTER, "outer structure");
            Definition.notEqualsValid(depth, -1, "outer structure");
        }
    }
}
