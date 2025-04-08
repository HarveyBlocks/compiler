package org.harvey.compiler.text.depart;

import lombok.AllArgsConstructor;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.analysis.*;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 递归地解析声明, 用于内部类等, 类成员等结构
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:25
 */
public class RecursivelyDepartedBodyFactory {

    public static final String BODY_END = SimpleDepartedBodyFactory.BODY_END;
    public static final String BODY_START = SimpleDepartedBodyFactory.BODY_START;
    public static final int FILE_OUTER = -1;
    public static final int UNSURE_OUTER = -2;
    public static final Embellish.EmbellishWord[] ALIAS_ILLEGAL_EMBELLISH_WORDS = {Embellish.EmbellishWord.FINAL,
            Embellish.EmbellishWord.CONST, Embellish.EmbellishWord.SEALED, Embellish.EmbellishWord.ABSTRACT};

    private RecursivelyDepartedBodyFactory() {
    }

    public static RecursivelyDepartedBody depart(DepartedBody fileBody) {
        if (fileBody.getBlocks() != null && !fileBody.getBlocks().isEmpty()) {
            throw new AnalysisException(
                    fileBody.getBlocks().getFirst().getBody().getFirst().getPosition(),
                    "blocks is not allowed here"
            );
        }
        List<SourceTypeAlias> aliasList = fileAliasMap(fileBody.getDeclarableSentenceList());
        List<DeclaredDepartedPart> functionList = new ArrayList<>();
        LinkedList<RecursivePartPair> tobeDepartedStructureList = new LinkedList<>();
        // addToBeDepartedFromBody
        addToBeDepartedFromBody(fileBody, FILE_OUTER, tobeDepartedStructureList, functionList);
        //  TODO 不确定需求的功能 List<SimpleCallable> functionList = departRecursiveCallable(tobeDepartedFunctionList);
        List<SimpleStructure> simpleStructureLinkedList = departRecursiveStructure(
                tobeDepartedStructureList);
        return new RecursivelyDepartedBody(fileBody.getImportTable(), aliasList, functionList,
                simpleStructureLinkedList
        );
    }

    private static void addToBeDepartedFromBody(
            DepartedBody body, int outer,
            LinkedList<RecursivePartPair> tobeDepartedStructureList,
            List<DeclaredDepartedPart> functionList) {
        for (DeclaredDepartedPart part : body.getDeclarableRecursiveList()) {
            Declarable statement = part.getStatement();
            if (statement.isComplexStructure()) {
                tobeDepartedStructureList.add(new RecursivePartPair(outer, part));
            } else if (statement.isCallable()) {
                // 删除内部函数声明, 内部函数需要像lambda表达式一样实现
                functionList.add(part);
            } else {
                throw new AnalysisException(statement.getStart(), "What kind of structure you are?");
            }
        }
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

        EmbellishSource embellish = declarable.getEmbellish();
        embellish.illegalOn("alias type", ALIAS_ILLEGAL_EMBELLISH_WORDS);
        SourcePosition staticPosition = embellish.getStaticMark();

        SourceTextContext expression = declarable.getAttachment();
        expression.addFirst(declarable.getIdentifier());
        if (expression.isEmpty()) {
            throw new AnalysisException(declarable.getStart(), "expected alias using");
        }
        return fileAliasMap(permissions, staticPosition, expression);
    }

    private static List<SourceTypeAlias> fileAliasMap(
            SourceTextContext permissions, SourcePosition staticPosition,
            SourceTextContext expression) {
        if (expression.isEmpty()) {
            if (!permissions.isEmpty()) {
                throw new AnalysisExpressionException(
                        permissions.removeLast().getPosition(),
                        "empty alias map is illegal"
                );
            } else {
                throw new CompilerException("permission and empty alias map can not empty at the same time");
            }
        }

        List<SourceTypeAlias> aliasList = new ArrayList<>();
        SourceTextContext alias = null;
        SourceTextContext type = new SourceTextContext();
        int inGeneric = 0;
        for (SourceString ss : expression) {
            // Type<> = Type<> = Type<>, Type = Type;
            // 不允许连续的等号!
            type.add(ss);
            if (ss.getType() != SourceType.OPERATOR) {
                continue;
            }
            if (inGeneric == 0 && Operator.ASSIGN.nameEquals(ss.getValue())/*是=*/) {
                type.removeLast();
                if (type.isEmpty()) {
                    throw new AnalysisExpressionException(ss.getPosition(), "need a type");
                }
                if (alias == null) {
                    alias = type;
                } else {
                    throw new AnalysisException(ss.getPosition(), "expected , or ;");
                }
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
                if (alias == null) {
                    throw new AnalysisException(ss.getPosition(), "expected  =");
                }
                aliasList.add(fileAliasMap(permissions, staticPosition, alias, type, ss.getPosition()));
                alias = null;
                type = new SourceTextContext();
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
        if (alias == null) {
            throw new AnalysisException(expression.removeLast().getPosition(), "expected  =");
        }
        aliasList.add(fileAliasMap(permissions, staticPosition, alias, type, expression.getLast().getPosition()));
        return aliasList;
    }

    private static SourceTypeAlias fileAliasMap(
            SourceTextContext permissions, SourcePosition staticPosition,
            SourceTextContext alias, SourceTextContext origin, SourcePosition sp) {
        // 进行验收

        if (origin.isEmpty()) {
            throw new AnalysisExpressionException(sp, "origin type is needed");
        }
        SourcePosition originStart = origin.getFirst().getPosition();
        if (alias.isEmpty()) {
            throw new AnalysisExpressionException(originStart, "alias type is needed");
        }
        SourceString identifier = alias.removeFirst();
        if (identifier.getType() != SourceType.IDENTIFIER) {
            throw new AnalysisExpressionException(identifier.getPosition(), "must be identifier");
        }
        return new SourceTypeAlias(permissions, staticPosition, new IdentifierString(identifier), alias, origin);
    }

    private static boolean isAlias(SourceTextContext type) {
        if (type.size() != 1) {
            return false;
        }
        SourceString t = type.getFirst();
        return t.getType() == SourceType.KEYWORD && Keyword.ALIAS.equals(t.getValue());
    }

    private static List<SimpleStructure> departRecursiveStructure(
            LinkedList<RecursivePartPair> tobeDepartedList) {
        ArrayList<SimpleStructure> simpleStructureList = new ArrayList<>();
        int index = 0;
        while (!tobeDepartedList.isEmpty()) {
            RecursivePartPair structurePair = tobeDepartedList.removeFirst();

            int depth =
                    structurePair.outer < 0 ? 0 : simpleStructureList.get(structurePair.outer).getDepth() + 1;
            SimpleStructure newStructure = departSimpleComplexStructure(
                    structurePair, depth, tobeDepartedList, index);
            simpleStructureList.add(newStructure);
            if (newStructure.hasOuter()) {
                SimpleStructure outer = simpleStructureList.get(newStructure.getOuterStructure());
                outer.registerInternalStructure(index);
            }
            index++;
        }
        return simpleStructureList;
    }

    /*删除的功能, 函数内的函数定义
    private static List<SimpleCallable> departRecursiveCallable(LinkedList<RecursivePartPair> tobeDepartedFunctionList) {
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
        CallableDepartedBody funcBody = CallableDepartedBodyFactory.simplyMapToExpression(body);
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

    private static SimpleStructure departSimpleComplexStructure(
            RecursivePartPair structurePair,
            int depth, LinkedList<RecursivePartPair> tobeDepartedList,
            int structureOuterIndex) {
        SourceTextContext body = removeBodyCircle(structurePair.tobeDeparted.getBody());
        LinkedList<DepartedPart> simpleDepart = SimpleDepartedBodyFactory.depart(body);
        LinkedList<EnumConstantDeclarable> enumConstList = null;
        if (isEnum(structurePair.tobeDeparted.getStatement().getType())) {
            if (simpleDepart.getFirst().isSentenceEnd()) {
                enumConstList = DeclarableFactory.enumConstantListPhase(simpleDepart.removeFirst().getStatement());
            }
        }
        DepartedBody structureBody = DepartedBodyFactory.depart(simpleDepart);
        List<DeclaredDepartedPart> methodList = new ArrayList<>();
        addToBeDepartedFromBody(structureBody, structureOuterIndex, tobeDepartedList, methodList);
        if (!structureBody.getImportTable().isEmpty()) {
            SourcePosition sp = SourcePosition.UNKNOWN;
            for (Map.Entry<String, ImportString> entry : structureBody.getImportTable().entrySet()) {
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
        return new SimpleStructure(structurePair.outer, depth, structurePair.tobeDeparted.getStatement(),
                structureBody.getBlocks(), sourceTypeAliaseList, fieldList, methodList, enumConstList
        );
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
        if (first.getType() != SourceType.SIGN || !BODY_START.equals(first.getValue())) {
            throw new AnalysisException(
                    first.getPosition(),
                    "Body should start with `" + BODY_START + "`, but not: " + first.getValue()
            );
        }
        if (last.getType() != SourceType.SIGN || !BODY_END.equals(last.getValue())) {
            throw new AnalysisException(
                    last.getPosition(),
                    "Body should end with `" + BODY_END + "`, but not: " + last.getValue()
            );
        }
        return body;
    }

    @AllArgsConstructor
    private static class RecursivePartPair {
        private final int outer;
        private final DeclaredDepartedPart tobeDeparted;
    }
}
