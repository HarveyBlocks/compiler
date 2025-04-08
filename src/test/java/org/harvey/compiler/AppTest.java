package org.harvey.compiler;

import org.harvey.compiler.text.SimpleTextDecomposer;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.decomposer.CommitClearChecker;
import org.harvey.compiler.text.decomposer.TextDecomposerChain;
import org.harvey.compiler.text.depart.*;
import org.harvey.compiler.text.mixed.MixedTextDecomposer;
import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.define.FileDefinition;
import org.junit.Test;

/**
 * Unit test1 for simple App.
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
            Application.application(
                    new String[]{"src/main/resources/callable_declare.vie", "_", "target=targetPath", "base=BasePath"});
            FileDefinition fileDefinition = null;//CoreCompiler.compileFile(null, null);
            String jsonStr = JsonUtils.toJsonStr(fileDefinition);
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
        /*StatementContextBuilder builder = new StatementContextBuilder();

        RecursivelyDepartedBody depart = RecursivelyDepartedBodyFactory.depart(departedBody);
        FileContext genericDefine = builder.genericDefine(
                null,
                new String[]{"packageString"}
        );*/
        /*for (ValueContext mfv : genericDefine.getFileVariableTable()) {
            System.out.println(mfv.getAccessControl());
            System.out.println(mfv.getEmbellish());
            System.out.println(mfv.getType());
            System.out.println(mfv.getIdentifierReference());
            System.out.println(mfv.getAttachments());
        }*/
    }
}
