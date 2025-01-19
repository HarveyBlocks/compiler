package org.harvey.compiler;

import org.harvey.compiler.analysis.text.SimpleTextDecomposer;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.decomposer.CommitClearChecker;
import org.harvey.compiler.analysis.text.decomposer.TextDecomposerChain;
import org.harvey.compiler.analysis.text.mixed.MixedTextDecomposer;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.phaser.FileStatementContextBuilder;
import org.harvey.compiler.depart.DepartedBody;
import org.harvey.compiler.depart.DepartedBodyFactory;
import org.harvey.compiler.depart.RecursivelyDepartedBodyFactory;
import org.harvey.compiler.depart.SimpleDepartedBodyFactory;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static TextDecomposerChain registerChain() {
        TextDecomposerChain chain = new TextDecomposerChain();
        chain.register(new CommitClearChecker())
                //.register(new SourceFileRebuilder())
                .register(new MixedTextDecomposer())
        //.register(new SourceFileRebuilder())
        ;//.register(new StringDecomposer());
        //.register(new SourceFileRebuilder());
        return chain;
    }

    @Test
    public void testMain() {
        try {
            FileContext fileContext = Application.compileFile("src/main/resources/CallableDeclare.via");
            String jsonStr = JsonUtils.toJsonStr(fileContext);
            System.out.println(jsonStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testLine() {
        SimpleTextDecomposer decomposer = new SimpleTextDecomposer();
        decomposer.appendDecomposed(false, "package final const String[][][] aaa;");
        TextDecomposerChain decomposerChain = registerChain();
        SourceTextContext context = decomposerChain.execute(decomposer.get());
        DepartedBody departedBody = DepartedBodyFactory.depart(SimpleDepartedBodyFactory.depart(context));
        FileStatementContextBuilder builder = new FileStatementContextBuilder();

        FileContext build = builder.build(RecursivelyDepartedBodyFactory.depart(departedBody));
        /*for (ValueContext mfv : build.getFileVariableTable()) {
            System.out.println(mfv.getAccessControl());
            System.out.println(mfv.getEmbellish());
            System.out.println(mfv.getType());
            System.out.println(mfv.getIdentifierReference());
            System.out.println(mfv.getAttachments());
        }*/
    }
}
