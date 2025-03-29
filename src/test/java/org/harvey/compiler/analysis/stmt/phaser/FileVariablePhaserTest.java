package org.harvey.compiler.analysis.stmt.phaser;

import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.junit.Test;

public class FileVariablePhaserTest {

    private static SourcePosition sp = new SourcePosition(0, 0);

    private static void addToContext(SourceTextContext context, SourceType keyword, String internal) {
        context.add(new SourceString(keyword, internal, sp = SourcePosition.moveToEnd(sp, internal + " ")));
    }

    @Test
    public void isTargetPart() {
        SourceTextContext context = new SourceTextContext();
        addToContext(context, SourceType.KEYWORD, "internal");
        addToContext(context, SourceType.KEYWORD, "package");
        addToContext(context, SourceType.KEYWORD, "const");
        addToContext(context, SourceType.KEYWORD, "final");
        addToContext(context, SourceType.KEYWORD, "unsigned");
        addToContext(context, SourceType.KEYWORD, "int32");
        addToContext(context, SourceType.OPERATOR, "[]");
        addToContext(context, SourceType.OPERATOR, "[]");
        addToContext(context, SourceType.OPERATOR, "[]");
        addToContext(context, SourceType.OPERATOR, "[]");
        addToContext(context, SourceType.IDENTIFIER, "aaa");
        addToContext(context, SourceType.OPERATOR, "=");
        addToContext(context, SourceType.OPERATOR, "+");
        addToContext(context, SourceType.OPERATOR, "(");
        addToContext(context, SourceType.BOOL, "true");
        addToContext(context, SourceType.OPERATOR, ")");
        addToContext(context, SourceType.OPERATOR, ",");
        addToContext(context, SourceType.IDENTIFIER, "bbb");
        addToContext(context, SourceType.OPERATOR, "=");
        addToContext(context, SourceType.OPERATOR, "+");
        addToContext(context, SourceType.BOOL, "true");
        addToContext(context, SourceType.SIGN, ";");
        context.forEach(ss -> System.out.print(ss.getValue() + ' '));
        System.out.println();
        phase(context);
    }

    public void phase(SourceTextContext context) {
        /*final FileContext fileContext = new FileContext();
        final FileFunctionPhaser phaser = new FileFunctionPhaser(fileContext);
        DepartedPart dp = new DepartedPart(context);
        Declarable sentence = DeclarableFactory.statementBasic(dp.getStatement());
        phaser.phase(sentence, dp.getBody());*/
    }
}