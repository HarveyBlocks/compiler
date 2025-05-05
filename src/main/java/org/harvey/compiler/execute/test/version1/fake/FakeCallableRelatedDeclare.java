package org.harvey.compiler.execute.test.version1.fake;

import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.UnfinishedException;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.FileRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.StructureRelatedDeclare;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;

import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-12 00:54
 */
public class FakeCallableRelatedDeclare implements CallableRelatedDeclare {
    @Override
    public StructureRelatedDeclare outerStructure() {
        throw new UnfinishedException("");
    }

    @Override
    public FileRelatedDeclare outerFile() {
        throw new UnfinishedException("");
    }

    @Override
    public MemberType getReturnType(int i) {
        if (i >= this.returnTypeSize()) {
            throw new CompilerException("");
        }
        return Int32Type.INT32;
    }

    @Override
    public RelatedGenericDefine getGeneric(int i) {
        return null;
    }

    @Override
    public MemberType getParameterType(int i) {
        if (!this.testParameterSize(i)) {
            throw new CompilerException("");
        }
        // 0, 1 都是 int
        return Int32Type.INT32;
    }

    @Override
    public MemberType[] getThrowsTypes() {
        return new MemberType[0];
    }

    @Override
    public boolean testParameterSize(int size) {
        return size == 2;
    }

    @Override
    public int returnTypeSize() {
        return 1;
    }

    @Override
    public boolean testGenericList(MemberType[] genericList) {
        return false;
    }

    @Override
    public boolean testOnlyResult(MemberType determinedType) {
        return determinedType == getReturnType(0);
    }

    @Override
    public Embellish getEmbellish() {
        return new Embellish(Set.of(Embellish.EmbellishWord.CONST, Embellish.EmbellishWord.STATIC));
    }

}
