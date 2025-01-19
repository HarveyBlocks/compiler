package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.DeclarePhaserUtil;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:25
 */
public class RecursivelyDepartedBodyFactory {

    public static final String BODY_END = SimpleDepartedBodyFactory.BODY_END;
    public static final String BODY_START = SimpleDepartedBodyFactory.BODY_START;

    private RecursivelyDepartedBodyFactory() {
    }

    public static RecursivelyDepartedBody depart(DepartedBody fileBody) {
        if (fileBody.getBlocks() != null && !fileBody.getBlocks().isEmpty()) {
            throw new AnalysisException(fileBody.getBlocks().getFirst().getBody().getFirst().getPosition(),
                    "blocks is not allowed here");
        }
        List<SourceTypeAlias> aliasList = fileAliasMap(fileBody.getDeclarableSentenceList());
        List<DeclaredDepartedPart> functionList = new ArrayList<>();
        LinkedList<RecursivePartPair> tobeDepartedStructureList = new LinkedList<>();
        for (DeclaredDepartedPart part : fileBody.getDeclarableRecursiveList()) {
            Declarable statement = part.getStatement();
            if (statement.isComplexStructure()) {
                tobeDepartedStructureList.add(new RecursivePartPair(-1, part));
            } else if (statement.isCallable()) {
                // 如何获取所有的内部函数呢? 快速地获取
                // functionList.add(CallableDepartedBodyFactory.depart(part.getStatement(), removeBodyCircle(part.getBody()))/*TODO new RecursivePartPair(-1, part)*/);
                // 删除内部函数声明, 内部函数需要像lambda表达式一样实现
                functionList.add(part);
            } else {
                throw new AnalysisException(statement.getStart(), "What kind of structure you are?");
            }
        }
        //  TODO 不确定需求的功能 List<SimpleCallable> functionList = departRecursiveCallable(tobeDepartedFunctionList);
        List<SimpleComplexStructure> simpleComplexStructureLinkedList = departRecursiveStructure(
                tobeDepartedStructureList);
        return new RecursivelyDepartedBody(fileBody.getImportTable(), aliasList, functionList,
                simpleComplexStructureLinkedList);
    }

    private static List<SourceTypeAlias> fileAliasMap(LinkedList<Declarable> declarableSentenceList) {
        if (declarableSentenceList == null) {
            return null;
        }
        List<SourceTypeAlias> aliasList = new ArrayList<>();
        for (Declarable declarable : declarableSentenceList) {
            List<SourceTypeAlias> aliasExpression = tryPhaseTypeAlias(declarable);
            if (aliasExpression == null) {
                throw new AnalysisException(declarable.getStart(), "file variable is not allowed");
            }
            aliasList.addAll(aliasExpression);
        }
        return aliasList;
    }

    private static List<SourceTypeAlias> tryPhaseTypeAlias(Declarable declarable) {
        if (!isAlias(declarable.getType())) {
            return null;
        }
        SourceTextContext permissions = declarable.getPermissions();

        declarable.getEmbellish().existIsIllegal("type alias");
        SourceTextContext expression = declarable.getAttachment();
        expression.addFirst(declarable.getIdentifier());
        if (expression.isEmpty()) {
            throw new AnalysisException(declarable.getStart(), "expected alias expression");
        }
        return fileAliasMap(permissions, expression);
    }

    private static List<SourceTypeAlias> fileAliasMap(SourceTextContext permissions, SourceTextContext expression) {
        List<SourceTypeAlias> aliasList = new ArrayList<>();
        LinkedList<SourceTextContext> types = new LinkedList<>();
        SourceTextContext type = new SourceTextContext();
        int inGeneric = 0;
        for (SourceString ss : expression) {
            // Type<> = Type<> = Type<>, Type = Type;
            type.add(ss);
            if (ss.getType() != SourceStringType.OPERATOR) {
                continue;
            }
            if (inGeneric == 0 && Operator.ASSIGN.nameEquals(ss.getValue())/*是=*/) {
                type.removeLast();
                if (type.isEmpty()) {
                    throw new AnalysisExpressionException(ss.getPosition(), "need a type");
                }
                types.add(type);
                type = new SourceTextContext();
                continue;
            } else if (Operator.COMMA.nameEquals(ss.getValue())/*是,*/) {
                if (inGeneric != 0) {
                    continue;
                }
                type.removeLast();
                if (type.isEmpty()) {
                    throw new AnalysisExpressionException(ss.getPosition(), "need a type");
                }
                types.add(type);
                type = new SourceTextContext();
                aliasList.addAll(fileAliasMap(permissions, types, ss.getPosition()));
                types = new LinkedList<>();
                continue;
            } else if (Operator.GENERIC_LIST_PRE.nameEquals(ss.getValue())/*是pre*/) {
                inGeneric++;
            } else if (Operator.GENERIC_LIST_POST.nameEquals(ss.getValue())/*是post*/) {
                inGeneric--;
            } else {
                throw new AnalysisException(ss.getPosition(), "unexpected operator");
            }

            if (inGeneric < 0) {
                throw new AnalysisException(ss.getPosition(), "illegal generic `<>` match");
            }
        }
        if (inGeneric != 0) {
            throw new AnalysisException(expression.getLast().getPosition(), "excepted `>`");
        }
        types.add(type);
        aliasList.addAll(fileAliasMap(permissions, types, expression.getLast().getPosition()));
        if (aliasList.isEmpty()) {
            throw new AnalysisException(expression.getFirst().getPosition(), "expected alias expression");
        }
        return aliasList;
    }

    private static List<SourceTypeAlias> fileAliasMap(SourceTextContext permissions,
                                                      LinkedList<SourceTextContext> types,
                                                      SourcePosition sp) {
        if (types.size() == 1) {
            SourceTextContext first = types.getFirst();
            if (first.isEmpty()) {
                throw new AnalysisExpressionException(sp, "Not enough to alias type");
            } else {
                throw new AnalysisExpressionException(
                        first.getFirst().getPosition(),
                        first.getLast().getPosition(),
                        "Not enough to alias type");
            }
        }
        // 进行验收
        SourceTextContext origin = types.removeLast();
        if (origin.isEmpty()) {
            throw new AnalysisExpressionException(sp, "origin type is needed");
        }
        SourcePosition originStart = origin.getFirst().getPosition();
        return types.stream().map(stc -> {
            if (stc.isEmpty()) {
                throw new AnalysisExpressionException(originStart, "alias type is needed");
            }
            return new SourceTypeAlias(permissions, stc, origin);
        }).collect(Collectors.toList());
    }

    private static boolean isAlias(SourceTextContext type) {
        if (type.size() != 1) {
            return false;
        }
        SourceString t = type.getFirst();
        return t.getType() == SourceStringType.KEYWORD && Keyword.ALIAS.equals(t.getValue());
    }

    private static List<SimpleComplexStructure> departRecursiveStructure(
            LinkedList<RecursivePartPair> tobeDepartedList) {
        ArrayList<SimpleComplexStructure> simpleComplexStructureLinkedList = new ArrayList<>();
        int index = 0;
        while (!tobeDepartedList.isEmpty()) {
            RecursivePartPair structurePair = tobeDepartedList.removeFirst();
            SimpleComplexStructure newStructure = departSimpleComplexStructure(structurePair, tobeDepartedList, index);
            simpleComplexStructureLinkedList.add(newStructure);
            if (newStructure.hasOuter()) {
                SimpleComplexStructure outer = simpleComplexStructureLinkedList.get(newStructure.getOuterStructure());
                outer.registerInternalStructure(index);
            }
            index++;
        }
        return simpleComplexStructureLinkedList;
    }

    /*private static List<SimpleCallable> departRecursiveCallable(LinkedList<RecursivePartPair> tobeDepartedFunctionList) {
        List<SimpleCallable> simpleCallableLinkedList = new LinkedList<>();
        int index = 0;
        while (!tobeDepartedFunctionList.isEmpty()) {
            RecursivePartPair structurePair = tobeDepartedFunctionList.removeFirst();
            SimpleCallable newCallable = departSimpleCallable(structurePair, tobeDepartedFunctionList, index);
            simpleCallableLinkedList.add(newCallable);
            if (newCallable.hasOuterCallable()) {
                SimpleCallable outerCallable = simpleCallableLinkedList.get(newCallable.getOuterCallable());
                outerCallable.registerLocalFunction(index);
            }
            index++;
        }
        return simpleCallableLinkedList;
    }

    private static SimpleCallable departSimpleCallable(
            RecursivePartPair callablePair, LinkedList<RecursivePartPair> tobeDepartedFunctionList, int outerIndex) {
        SourceTextContext body = removeBodyCircle(callablePair.tobeDeparted.getBody());
        CallableDepartedBody funcBody = CallableDepartedBodyFactory.depart(body);
        // SourceString outerIdentifier = callablePair.tobeDeparted.getStatement().getIdentifier();
        if (funcBody.getLocalCallables() != null) {
            tobeDepartedFunctionList.addAll(funcBody
                    .getLocalCallables()
                    .stream()
                    .map(p -> new RecursivePartPair(outerIndex, p))
                    .collect(Collectors.toList())
            );
        }
        return new SimpleCallable(callablePair.outer, callablePair.tobeDeparted.getStatement(),
                funcBody.getExecutableBody());
    }*/

    private static SimpleComplexStructure departSimpleComplexStructure(RecursivePartPair structurePair,
                                                                       LinkedList<RecursivePartPair> tobeDepartedList,
                                                                       int structureOuterIndex) {
        SourceTextContext body = removeBodyCircle(structurePair.tobeDeparted.getBody());
        LinkedList<DepartedPart> simpleDepart = SimpleDepartedBodyFactory.depart(body);
        LinkedList<EnumConstantDeclarable> enumConstList = null;
        if (isEnum(structurePair.tobeDeparted.getStatement().getType())) {
            if (simpleDepart.getFirst().isSentenceEnd()) {
                enumConstList = DeclarePhaserUtil.enumConstantListPhase(simpleDepart.removeFirst().getStatement());
            }
        }
        DepartedBody structureBody = DepartedBodyFactory.depart(simpleDepart);
        List<DeclaredDepartedPart> methodList = new ArrayList<>();
        /*LinkedList<SourceString> internalStructureIdentifiers = new LinkedList<>();
        SourceString identifier = structurePair.tobeDeparted.getStatement().getIdentifier();*/
        for (DeclaredDepartedPart part : structureBody.getDeclarableRecursiveList()) {
            Declarable statement = part.getStatement();
            if (statement.isComplexStructure()) {
                tobeDepartedList.add(new RecursivePartPair(structureOuterIndex, part));
            } else if (statement.isCallable()) {
                // 删除了内部函数这个功能!
                methodList.add(part/*CallableDepartedBodyFactory.depart(
                        part.getStatement(), part.getBody()*//*new RecursivePartPair(-1, part)*//*
                )*/);
            } else {
                throw new AnalysisException(statement.getStart(), "What kind of structure you are?");
            }
        }
        if (!structureBody.getImportTable().isEmpty()) {
            SourcePosition sp = SourcePosition.UNKNOWN;
            for (Map.Entry<String, ImportContext> entry : structureBody.getImportTable().entrySet()) {
                sp = entry.getValue().getPosition();
                break;
            }
            throw new AnalysisException(sp, "Import is illegal here");
        }
        List<Declarable> fieldList = new ArrayList<>();
        List<SourceTypeAlias> sourceTypeAliaseList = new ArrayList<>();
        for (Declarable declarable : structureBody.getDeclarableSentenceList()) {
            if (declarable.isCallable()) {
                methodList.add(new DeclaredDepartedPart(declarable, null));
            } else {
                List<SourceTypeAlias> sourceTypeAliases = tryPhaseTypeAlias(declarable);
                if (sourceTypeAliases == null) {
                    fieldList.add(declarable);
                } else {
                    sourceTypeAliaseList.addAll(sourceTypeAliases);
                }
            }
        }

        // List<SimpleCallable> methodList = departRecursiveCallable(tobeDepartedMethodList);
        return new SimpleComplexStructure(
                structurePair.outer, structurePair.tobeDeparted.getStatement(),
                structureBody.getBlocks(),
                sourceTypeAliaseList, fieldList, methodList, enumConstList
        );
    }

    private static List<EnumConstantDeclarable> phaseEnumConstantList(SourceTextContext enumConstVarList) {
        return DeclarePhaserUtil.enumConstantListPhase(enumConstVarList);
    }

    private static boolean isEnum(SourceTextContext type) {
        if (type == null || type.size() != 1) {
            return false;
        }
        return Keyword.get(type.getFirst().getValue()) == Keyword.ENUM;
    }

    private static SourceTextContext removeBodyCircle(SourceTextContext body) {
        if (body == null || body.isEmpty()) {
            throw new AnalysisException(SourcePosition.UNKNOWN, "body can not be null");
        }
        if (body.size() == 1) {
            throw new AnalysisException(body.remove().getPosition(), "It is imposible for body only has one size");
        }
        // 括号
        SourceString first = body.removeFirst();
        SourceString last = body.removeLast();
        if (first.getType() != SourceStringType.SIGN || !BODY_START.equals(first.getValue())) {
            throw new AnalysisException(first.getPosition(),
                    "Body should start with `" + BODY_START + "`, but not: " + first.getValue());
        }
        if (last.getType() != SourceStringType.SIGN || !BODY_END.equals(last.getValue())) {
            throw new AnalysisException(last.getPosition(),
                    "Body should end with `" + BODY_END + "`, but not: " + last.getValue());
        }
        return body;
    }

    @AllArgsConstructor
    private static class RecursivePartPair {
        private final int outer;
        private final DeclaredDepartedPart tobeDeparted;
    }
}
