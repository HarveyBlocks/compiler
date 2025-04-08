package org.harvey.compiler.type.raw;

import lombok.Getter;
import org.harvey.compiler.declare.analysis.Keyword;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.RelationshipBuildStage;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 15:14
 */
@Getter
public enum KeywordBasicType implements RelationRawType {
    BOOL(Keyword.BOOL),
    CHAR(Keyword.CHAR),
    FLOAT32(Keyword.FLOAT32),
    FLOAT64(Keyword.FLOAT64),
    INT8(Keyword.INT8),
    INT16(Keyword.INT16),
    INT32(Keyword.INT32),
    INT64(Keyword.INT64),
    UINT8(Keyword.UINT8),
    UINT16(Keyword.UINT16),
    UINT32(Keyword.UINT32),
    UINT64(Keyword.UINT64);
    private final Keyword basicType;

    KeywordBasicType(Keyword basicType) {
        this.basicType = basicType;
    }

    public static RelationRawType get(Keyword keyword) {
        switch (keyword) {
            case BOOL:
                return BOOL;
            case CHAR:
                return CHAR;
            case FLOAT32:
                return FLOAT32;
            case FLOAT64:
                return FLOAT64;
            case INT8:
                return INT8;
            case INT16:
                return INT16;
            case INT32:
                return INT32;
            case INT64:
                return INT64;
            case UINT8:
                return UINT8;
            case UINT16:
                return UINT16;
            case UINT32:
                return UINT32;
            case UINT64:
                return UINT64;
        }
        return null;
    }

    @Override
    public File getFromFile() {
        // 没有from file
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public SourcePosition getDeclarePosition() {
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public boolean hasParent() {
        return false;
    }

    @Override
    public boolean hasImplements() {
        return false;
    }

    @Override
    public String getJoinedFullname() {
        return basicType.getValue();
    }

    @Override
    public String getSimpleName() {
        return basicType.getValue();
    }

    @Override
    public RelationRawType getEndOrigin() {
        return null;
    }

    @Override
    public void setEndOrigin(RelationRawType origin) {
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public String[] getFullname() {
        return new String[]{basicType.getValue()};
    }

    @Override
    public boolean isSealed() {
        return true;
    }

    @Override
    public FullIdentifierString getDeclareIdentifier() {
        return null;
    }

    @Override
    public void addInterfaces(RelationRawType rawType) {
        // DO NOTHING
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public Collection<RelationRawType> getInterfaces() {
        return Collections.emptyList();
    }

    @Override
    public RelationRawType getParent() {
        return null;
    }

    @Override
    public void setParent(RelationRawType rawType) {
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public StructureType getType() {
        return StructureType.KEYWORD_BASIC;
    }

    @Override
    public RelationshipBuildStage getStage() {
        return RelationshipBuildStage.FINISHED;
    }

    @Override
    public void updateStage(RelationshipBuildStage stage) {
        throw new CompilerException("basic type", new UnsupportedOperationException());
    }

    @Override
    public boolean isStatic() {
        return true;
    }
}
