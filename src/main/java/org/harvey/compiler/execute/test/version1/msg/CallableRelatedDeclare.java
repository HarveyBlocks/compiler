package org.harvey.compiler.execute.test.version1.msg;

import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.type.generic.relate.entity.RelatedGenericDefine;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-05 15:52
 */
public interface CallableRelatedDeclare {

    StructureRelatedDeclare outerStructure();

    FileRelatedDeclare outerFile();


    MemberType getReturnType(int i);

    RelatedGenericDefine getGeneric(int i);


    MemberType getParameterType(int i);


    MemberType[] getThrowsTypes();

    boolean testParameterSize(int size);

    int returnTypeSize();

    boolean testGenericList(MemberType[] genericList);

    /**
     * only and can assign to determined type
     */
    boolean testOnlyResult(MemberType determinedType);

    Embellish getEmbellish();
}
