package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.analysis.stmt.context.StatementContext;
import org.harvey.compiler.analysis.stmt.phaser.depart.DepartedSentencePart;
import org.harvey.compiler.analysis.stmt.phaser.variable.FileVariablePhaser;
import org.harvey.compiler.analysis.stmt.phaser.variable.FileVariableSentencePhaser;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourcePosition;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.junit.Test;

public class FileVariablePhaserTest {


    @Test
    public void isTargetPart() {
        SourceTextContext context = new SourceTextContext();
        context.add(new SourceString(SourceStringType.KEYWORD, "internal", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.KEYWORD, "package", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.KEYWORD, "const", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.KEYWORD, "final", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.KEYWORD, "signed", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.KEYWORD, "int64", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.IDENTIFIER, "aaa", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.OPERATOR, "=", new SourcePosition(0, 0)));
        context.add(new SourceString(SourceStringType.INT64, "1", new SourcePosition(0, 0)));
        phase(context);
    }

    public void phase(SourceTextContext context) {
        final StatementContext statementContext = new StatementContext();
        final FileVariablePhaser phaser = new FileVariablePhaser(statementContext);
        DepartedSentencePart dp = new DepartedSentencePart(context);
        if (phaser.isTargetPart(dp)) {
            phaser.phase(dp);
            System.out.println(statementContext.getFileVariableTable());
        } else {
            System.out.println("no");
        }
    }
}