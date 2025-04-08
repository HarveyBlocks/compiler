package org.harvey.compiler.type.raw;

import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;
import org.harvey.compiler.type.RelationshipBuildStage;

import java.io.File;
import java.util.Collection;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 22:47
 */
public interface RelationRawType extends SourcePositionSupplier {
    File getFromFile();

    SourcePosition getDeclarePosition();

    @Override
    default SourcePosition getPosition() {
        return getDeclarePosition();
    }

    boolean hasParent();

    boolean hasImplements();

    String getJoinedFullname();

    String getSimpleName();

    RelationRawType getEndOrigin();

    /**
     * @param origin 区别于upper,是最终的映射
     */
    void setEndOrigin(RelationRawType origin);

    String[] getFullname();

    boolean isSealed();

    default boolean needSetEndOrigin() {
        return getEndOrigin() == null;
    }

    FullIdentifierString getDeclareIdentifier();

    void addInterfaces(RelationRawType rawType);

    Collection<RelationRawType> getInterfaces();

    RelationRawType getParent();

    void setParent(RelationRawType rawType);

    default boolean isAlias() {
        return getType() == StructureType.ALIAS;
    }

    default boolean isBasicType() {
        return getType() == StructureType.KEYWORD_BASIC;
    }

    default boolean isGenericDefine() {
        return getType() == StructureType.GENERIC_DEFINE;
    }

    default boolean isStructure() {
        return !(isAlias() || isBasicType() || isGenericDefine());
    }

    StructureType getType();

    RelationshipBuildStage getStage();

    void updateStage(RelationshipBuildStage stage);

    boolean isStatic();
}
