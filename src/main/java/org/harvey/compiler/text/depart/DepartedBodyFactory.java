package org.harvey.compiler.text.depart;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.*;

/**
 * 解析文件中所有声明
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-01 00:24
 */
public class DepartedBodyFactory {

    public static final String SENTENCE_END = SimpleDepartedBodyFactory.SENTENCE_END;

    private DepartedBodyFactory() {
    }

    public static DepartedBody depart(LinkedList<DepartedPart> departList) {
        if (departList == null) {
            return null;
        }
        Map<String, ImportString> importContext = new HashMap<>();
        LinkedList<SimpleBlock> blocks = new LinkedList<>();
        LinkedList<DeclaredDepartedPart> declarableRecursiveList = new LinkedList<>();
        LinkedList<Declarable> declarableSentenceList = new LinkedList<>();
        boolean afterImport = false;
        Iterator<DepartedPart> iterator = departList.iterator();
        while (iterator.hasNext()) {
            DepartedPart part = iterator.next();
            boolean importPart = isImport(part);
            if (!afterImport && importPart) {
                DepartedBodyFactory.addImport(importContext, phaseImport(part));
                continue;
            }
            afterImport = true;
            if (importPart) {
                throw new AnalysisExpressionException(part.getPosition(), "Import only allowed at the top");
            }
            Boolean staticBlock = isStaticBlock(part);
            if (staticBlock != null) {
                // throw new AnalysisExpressionException(part.getPosition(),"code blocks is not allowed here.");
                blocks.add(new SimpleBlock(staticBlock, part.getBody()));
                continue;
            }
            if (isEmptySentence(part)) {
                continue;
            }
            Declarable declarable = DeclarableFactory.statementBasic(part.getStatement());
            if (!declarable.isVariableDeclare()) {
                declarableRecursiveList.add(new DeclaredDepartedPart(declarable, part.getBody()));
                continue;
            }
            if (part.isSentenceEnd()) {
                // throw new AnalysisExpressionException(part.getPosition(),"file variable is not allowed.");
                assert part.getBody() == null || part.getBody().isEmpty();
                declarableSentenceList.add(declarable);
                continue;
            }
            // 命名声明的是variable, 但是没用sentenceEnd
            // 索取更多, 直到获取到一个完整的类型位置
            DepartedBodyFactory.completeVariableAttachment(iterator, declarable.getAttachment(), part.getBody());
            // throw new AnalysisExpressionException(part.getPosition(),"file variable is not allowed.");
            declarableSentenceList.add(declarable);
        }
        return new DepartedBody(importContext, blocks, declarableSentenceList, declarableRecursiveList);
    }

    private static boolean isImport(DepartedPart part) {
        if (!part.isSentenceEnd()) {
            return false;
        }
        if (part.getBody() == null || !part.getBody().isEmpty()) {
            return false;
        }
        SourceTextContext statement = part.getStatement();
        if (statement == null) {
            return false;
        }
        SourceString first = statement.getFirst();
        return first.getType() == SourceType.KEYWORD && Keyword.IMPORT.equals(first.getValue());
    }

    private static ImportString phaseImport(DepartedPart part) {
        SourceTextContext statement = part.getStatement();
        SourceString mark = statement.removeFirst();
        SourceString last = statement.getLast();
        if (last.getType() == SourceType.SIGN && SENTENCE_END.equals(last.getValue())) {
            statement.removeLast();
        }
        ListIterator<SourceString> iterator = statement.listIterator();
        List<SourceString> identifiers = new ArrayList<>();
        while (iterator.hasNext()) {
            SourceString expectIdentifier = iterator.next();
            if (!CollectionUtil.skipIf(iterator, s -> Operator.GET_MEMBER.nameEquals(s.getValue()))) {
                if (iterator.hasNext()) {
                    throw new AnalysisExpressionException(iterator.next().getPosition(), "expected a dot: `.`");
                }
            }
            if (expectIdentifier.getType() != SourceType.IDENTIFIER) {
                throw new AnalysisExpressionException(expectIdentifier.getPosition(), "expected an identifier");

            }
            identifiers.add(expectIdentifier);
        }
        IdentifierString[] array = identifiers.stream().map(IdentifierString::new).toArray(IdentifierString[]::new);
        if (array.length == 0) {
            throw new AnalysisExpressionException(mark.getPosition(), "expect something to import");
        }
        return new ImportString(array, mark.getPosition());
    }

    private static boolean isEmptySentence(DepartedPart part) {
        SourceTextContext statement = part.getStatement();
        return part.isSentenceEnd() && statement.size() == 1 && SENTENCE_END.equals(statement.getLast().getValue());
    }

    /**
     * @return 是static就返回true, 不是就返回false, 如果不是block就返回null
     */
    private static Boolean isStaticBlock(DepartedPart part) {
        if (part.isSentenceEnd()) {
            return null;
        }
        SourceTextContext statement = part.getStatement();
        int size = statement.size();
        // 没有声明的, 就是body
        if (size == 0) {
            return false;
        }
        // 有声明
        if (size != 1) {
            return null;
        }
        // 只允许static
        SourceString last = statement.getLast();
        boolean isStatic = last.getType() == SourceType.KEYWORD && Keyword.get(last.getValue()) == Keyword.STATIC;
        return isStatic ? true : null;
    }

    private static void addImport(Map<String, ImportString> importTable, ImportString importStatement) {
        IdentifierString key = importStatement.getTarget();
        String keyValue = key.getValue();
        if (!importTable.containsKey(keyValue)) {
            importTable.put(keyValue, importStatement);
            return;
        }
        ImportString hasInMap = importTable.get(keyValue);
        if (Arrays.equals(hasInMap.getStringPath(), importStatement.getStringPath())) {
            return;
        }
        throw new AnalysisExpressionException(importStatement.getPosition(), "Repeat target name of " +
                                                                             hasInMap.getTarget().getValue() +
                                                                             " with import cache at: " +
                                                                             hasInMap.getPosition() +
                                                                             " and " +
                                                                             importStatement.getPosition());
    }


    // 一个depart, 如果其是变量声明, 那么;是其结束, 往下找一直到分号位置, 有{也不停
    // 一个depart, 如果其是复合结构声明, 或者是方法声明, 往下找到{, 然后在找对应的}
    // 一个Body代码块, 如果没用声明, 那么就是代码块
    // 如何快速辨别一个字符串是方法/组合类型还是变量呢...()
    private static void completeVariableAttachment(
            Iterator<DepartedPart> iterator, SourceTextContext variableAttachment, SourceTextContext body) {
        variableAttachment.addAll(body);
        SourcePosition lastPosition = variableAttachment.getLast().getPosition();
        while (iterator.hasNext()) {
            DepartedPart more = iterator.next();
            SourceTextContext moreStatement = more.getStatement();
            variableAttachment.addAll(moreStatement);
            SourceTextContext moreBody = more.getBody();
            variableAttachment.addAll(moreBody);
            if (more.isSentenceEnd()) {
                variableAttachment.removeLast();// 删除最后一个;
                return;
            }
            lastPosition = moreBody.getLast().getPosition();
        }
        throw new AnalysisExpressionException(lastPosition, "expected " + SourceFileConstant.SENTENCE_END);
    }


}
