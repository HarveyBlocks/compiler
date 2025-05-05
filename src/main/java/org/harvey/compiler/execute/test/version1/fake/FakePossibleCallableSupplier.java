package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-11 00:11
 */
public class FakePossibleCallableSupplier extends PossibleCallableSupplier {
    DIdentifierManager manager;

    public FakePossibleCallableSupplier(
            SourcePosition position,
            CallableRelatedDeclare[] possible) {
        super(position, possible);
    }

    @Override
    public String show() {
     /*   CallableRelatedDeclare one = super.one();
        FileRelatedDeclare fileRelatedDeclare = one.outerFile();
        Embellish embellish = one.getEmbellish();
        manager.getReferenceAndAddIfNotExist(one.getPath());
        return fileRelatedDeclare + one.getReference();*/
        return super.toString();
    }
}
