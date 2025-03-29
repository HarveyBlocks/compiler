package org.harvey.compiler.type.raw;

import lombok.Getter;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.exception.CompilerException;
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
 * @date 2025-03-20 21:34
 */
@Getter
public class AliasRelationRawType implements RelationRawType {
    private final String joinedFullname;
    private final FullIdentifierString declareIdentifier;
    private final File fromFile;
    public RelationRawType origin;
    public List<RelationRawType> interfaces;
    private RelationshipBuildStage stage;
    private RelationRawType endOrigin;
    private final boolean staticMarked;

    @Override
    public RelationRawType getParent() {
        originPrepared();
        return endOrigin;
    }

    public AliasRelationRawType(
            FullIdentifierString fullname,
            RelationshipBuildStage stage,
            File fromFile, boolean staticMarked) {
        this(stage, fullname, fullname.joinFullnameString(FileNode.GET_MEMBER), fromFile, staticMarked);
    }

    private AliasRelationRawType(
            RelationshipBuildStage stage,
            FullIdentifierString declareIdentifier,
            String joinedFullname,
            File fromFile, boolean staticMarked) {
        this.stage = stage;
        this.declareIdentifier = declareIdentifier;
        this.joinedFullname = joinedFullname;
        this.fromFile = fromFile;
        this.staticMarked = staticMarked;
    }

    @Override
    public String[] getFullname() {
        return declareIdentifier.getFullname();
    }

    @Override
    public boolean isSealed() {
        originPrepared();
        return endOrigin.isSealed();
    }

    @Override
    public RelationRawType getEndOrigin() {
        originPrepared();
        return endOrigin;
    }

    @Override
    public void setEndOrigin(RelationRawType origin) {
        if (this.endOrigin != null) {
            throw new CompilerException("set origin too many times, origin can not update!");
        }
        if (origin.isAlias()) {
            throw new CompilerException(
                    "alias can not be origin"
            );
        }
        this.endOrigin = origin;
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
        return !endOrigin.needSetEndOrigin();
    }

    @Override
    public boolean hasImplements() {
        return !interfaces.isEmpty();
    }

    @Override
    public void setParent(RelationRawType rawType) {
        if (!rawType.isAlias()) {
            if (needSetEndOrigin()) {
                endOrigin = rawType;
            } else {
                throw new CompilerException("has set end origin");
            }
        }
        this.origin = rawType;

    }

    @Override
    public void addInterfaces(RelationRawType rawType) {
        interfaces.add(rawType);
    }

    @Override
    public StructureType getType() {
        originPrepared();
        return endOrigin.getType();
    }

    private void originPrepared() {
        if (needSetEndOrigin()) {
            throw new CompilerException("set origin first");
        }
    }

    @Override
    public void updateStage(RelationshipBuildStage stage) {
        this.stage = stage;
    }

    @Override
    public boolean isStatic() {
        return this.staticMarked;
    }
}
