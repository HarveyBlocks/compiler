package org.harvey.compiler.type.raw;

import lombok.Getter;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.io.cache.node.FileNode;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.RelationshipBuildStage;

import java.io.File;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-17 23:02
 */
@Getter
public class StructureRelationRawType implements RelationRawType {
    private final boolean staticMarked;
    private final StructureType type;
    private final String joinedFullname;
    private final FullIdentifierString declareIdentifier;
    private final File fromFile;
    private final boolean sealed;
    public RelationRawType parent;
    private List<RelationRawType> interfaces;
    private RelationshipBuildStage stage;

    public StructureRelationRawType(
            FullIdentifierString fullname,
            StructureType type,
            RelationshipBuildStage stage,
            File fromFile, boolean sealed, boolean staticMarked) {
        this(type, stage, fullname, fullname.joinFullnameString(FileNode.GET_MEMBER), fromFile, sealed, staticMarked);
    }

    private StructureRelationRawType(
            StructureType type,
            RelationshipBuildStage stage,
            FullIdentifierString declareIdentifier,
            String joinedFullname,
            File fromFile, boolean sealed, boolean staticMarked) {
        this.type = type;
        this.stage = stage;
        this.declareIdentifier = declareIdentifier;
        this.joinedFullname = joinedFullname;
        this.fromFile = fromFile;
        this.sealed = sealed;
        this.staticMarked = staticMarked;
    }

    @Override
    public String[] getFullname() {
        return declareIdentifier.getFullname();
    }

    @Override
    public boolean isSealed() {
        return sealed;
    }

    @Override
    public RelationRawType getEndOrigin() {
        return this;
    }

    @Override
    public void setEndOrigin(RelationRawType origin) {
        if (origin == this) {
            return;
        }
        throw new CompilerException(
                "structure can not set origin. origin is it's self",
                new UnsupportedOperationException()
        );
    }

    @Override
    public String getSimpleName() {
        return declareIdentifier.get(declareIdentifier.length() - 1);
    }

    @Override
    public SourcePosition getDeclarePosition() {
        return declareIdentifier.getPosition();
    }

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    @Override
    public boolean hasImplements() {
        return !interfaces.isEmpty();
    }

    @Override
    public void setParent(RelationRawType rawType) {
        parent = rawType;
    }

    @Override
    public void addInterfaces(RelationRawType rawType) {
        interfaces.add(rawType);
    }

    @Override
    public void updateStage(RelationshipBuildStage stage) {
        this.stage = stage;
    }

    @Override
    public boolean isStatic() {
        return staticMarked;
    }
}
