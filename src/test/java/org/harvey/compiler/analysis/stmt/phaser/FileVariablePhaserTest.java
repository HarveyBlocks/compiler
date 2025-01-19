package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;
import org.junit.Test;

public class FileVariablePhaserTest {

    private static SourcePosition sp = new SourcePosition(0, 0);

    private static void addToContext(SourceTextContext context, SourceStringType keyword, String internal) {
        context.add(new SourceString(keyword, internal, sp = SourcePosition.moveToEnd(sp, internal + " ")));
    }

    @Test
    public void isTargetPart() {
        SourceTextContext context = new SourceTextContext();
        addToContext(context, SourceStringType.KEYWORD, "internal");
        addToContext(context, SourceStringType.KEYWORD, "package");
        addToContext(context, SourceStringType.KEYWORD, "const");
        addToContext(context, SourceStringType.KEYWORD, "final");
        addToContext(context, SourceStringType.KEYWORD, "unsigned");
        addToContext(context, SourceStringType.KEYWORD, "int32");
        addToContext(context, SourceStringType.OPERATOR, "[]");
        addToContext(context, SourceStringType.OPERATOR, "[]");
        addToContext(context, SourceStringType.OPERATOR, "[]");
        addToContext(context, SourceStringType.OPERATOR, "[]");
        addToContext(context, SourceStringType.IDENTIFIER, "aaa");
        addToContext(context, SourceStringType.OPERATOR, "=");
        addToContext(context, SourceStringType.OPERATOR, "+");
        addToContext(context, SourceStringType.OPERATOR, "(");
        addToContext(context, SourceStringType.BOOL, "true");
        addToContext(context, SourceStringType.OPERATOR, ")");
        addToContext(context, SourceStringType.OPERATOR, ",");
        addToContext(context, SourceStringType.IDENTIFIER, "bbb");
        addToContext(context, SourceStringType.OPERATOR, "=");
        addToContext(context, SourceStringType.OPERATOR, "+");
        addToContext(context, SourceStringType.BOOL, "true");
        addToContext(context, SourceStringType.SIGN, ";");
        context.forEach(ss -> System.out.print(ss.getValue() + ' '));
        System.out.println();
        phase(context);
    }

    public void phase(SourceTextContext context) {
        /*final FileContext fileContext = new FileContext();
        final FileFunctionPhaser phaser = new FileFunctionPhaser(fileContext);
        DepartedPart dp = new DepartedPart(context);
        Declarable sentence = DeclarePhaserUtil.statementBasicPhase(dp.getStatement());
        phaser.phase(sentence, dp.getBody());*/
    }
}