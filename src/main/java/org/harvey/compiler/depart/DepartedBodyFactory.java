package org.harvey.compiler.depart;

import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.DeclarePhaserUtil;
import org.harvey.compiler.declare.context.ImportContext;
import org.harvey.compiler.declare.phaser.ImportPhaser;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 递归地
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
        Map<String, ImportContext> importContext = new HashMap<>();
        LinkedList<SimpleBlock> blocks = new LinkedList<>();
        LinkedList<DeclaredDepartedPart> declarableRecursiveList = new LinkedList<>();
        LinkedList<Declarable> declarableSentenceList = new LinkedList<>();
        boolean afterImport = false;
        for (DepartedPart part : departList) {
            boolean importPart = ImportPhaser.isImport(part);
            if (!afterImport && importPart) {
                DepartedBodyFactory.addImport(importContext, ImportPhaser.phase(part));
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
            Declarable declarable = DeclarePhaserUtil.statementBasicPhase(part.getStatement());
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
            DepartedBodyFactory.completeVariableAttachment(departList,
                    declarable.getAttachment(), part.getBody());
            // throw new AnalysisExpressionException(part.getPosition(),"file variable is not allowed.");
            declarableSentenceList.add(declarable);
        }
        return new DepartedBody(importContext, blocks, declarableSentenceList, declarableRecursiveList);
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
        boolean isStatic = last.getType() == SourceStringType.KEYWORD &&
                Keyword.get(last.getValue()) == Keyword.STATIC;
        return isStatic ? true : null;
    }

    private static void addImport(Map<String, ImportContext> importTable, ImportContext importStatement) {
        IdentifierString key = importStatement.getTarget();
        String keyValue = key.getValue();
        if (!importTable.containsKey(keyValue)) {
            importTable.put(keyValue, importStatement);
            return;
        }
        ImportContext hasInMap = importTable.get(keyValue);
        if (Arrays.equals(hasInMap.getStringPath(), importStatement.getStringPath())) {
            return;
        }
        throw new AnalysisExpressionException(importStatement.getPosition(),
                "Repeat target name of " + hasInMap.getTarget().getValue() + " with import statement at: " +
                        hasInMap.getPosition() + " and " + importStatement.getPosition());
    }


    // 一个depart, 如果其是变量声明, 那么;是其结束, 往下找一直到分号位置, 有{也不停
    // 一个depart, 如果其是复合结构声明, 或者是方法声明, 往下找到{, 然后在找对应的}
    // 一个Body代码块, 如果没用声明, 那么就是代码块
    // 如何快速辨别一个字符串是方法/组合类型还是变量呢...()
    private static void completeVariableAttachment(
            LinkedList<DepartedPart> departList, SourceTextContext attachment,
            SourceTextContext body) {
        attachment.addAll(body);
        boolean finished = false;
        SourcePosition lastPosition = attachment.getLast().getPosition();
        while (!departList.isEmpty()) {
            DepartedPart more = departList.removeFirst();
            SourceTextContext moreStatement = more.getStatement();
            attachment.addAll(moreStatement);
            SourceTextContext moreBody = more.getBody();
            attachment.addAll(moreBody);
            lastPosition = moreStatement.getLast().getPosition();
            if (more.isSentenceEnd()) {
                finished = true;
                break;
            }
            lastPosition = moreBody.getLast().getPosition();
        }
        if (!finished) {
            throw new AnalysisExpressionException(lastPosition, "expected " + SourceFileConstant.SENTENCE_END);
        }
    }


}
