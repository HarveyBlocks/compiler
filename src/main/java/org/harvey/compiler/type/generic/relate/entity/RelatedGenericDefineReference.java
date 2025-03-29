package org.harvey.compiler.type.generic.relate.entity;

import lombok.Getter;
import org.harvey.compiler.declare.context.StructureType;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.RelationshipBuildStage;
import org.harvey.compiler.type.raw.RelationRawType;

import java.io.File;
import java.util.Collection;
import java.util.Objects;

/**
 * {@link RelationRawType}到Generic的一个转换, RawType类型于GenericDefine有太多不同, 作为一个中间桥接的部分
 * <p>
 * 不加入cache
 * 构造器上的泛型, 不能依据参数解析的情况
 * 实例化<pre>{@code
 * Example<Type,On,Example,Structure> example = new<Type,On,Constructor>();
 * Example<Type,On,Example,Structure> example = new Example<Type,On,Example,Structure>();
 * var example = new Example<Type,On,Example,Structure>();
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 21:28
 */
@Getter
public class RelatedGenericDefineReference implements RelationRawType {
    private final String joinedDefineFrom;
    private final String[] defineFrom;
    /**
     * <pre>{@code
     *       <basic declare index on generic defines, generic define index offset>
     *  }</pre>
     */
    private final int genericDefineIndexBase;
    private final int genericDefineIndexOffset;
    private final IdentifierString declareIdentifierString;
    private final File fromFile;
    private final boolean onCallable;

    public RelatedGenericDefineReference(
            String[] defineFrom,
            String joinedDefineFrom,
            int genericDefineIndexBase,
            int genericDefineIndexOffset,
            IdentifierString declareIdentifier,
            File fromFile,
            boolean onCallable) {
        this.defineFrom = defineFrom;
        this.joinedDefineFrom = joinedDefineFrom;
        this.genericDefineIndexBase = genericDefineIndexBase;
        this.genericDefineIndexOffset = genericDefineIndexOffset;
        this.declareIdentifierString = declareIdentifier;
        this.fromFile = fromFile;
        this.onCallable = onCallable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RelatedGenericDefineReference)) {
            return false;
        }
        RelatedGenericDefineReference reference = (RelatedGenericDefineReference) o;
        return genericDefineIndexBase == reference.genericDefineIndexBase &&
               genericDefineIndexOffset == reference.genericDefineIndexOffset &&
               onCallable == reference.onCallable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(genericDefineIndexBase, genericDefineIndexOffset, onCallable);
    }

    public static RelatedGenericDefineReference adapt(RelationRawType rawType) {
        return rawType instanceof RelatedGenericDefineReference ? (RelatedGenericDefineReference) rawType : null;
    }

    @Override
    public boolean needSetEndOrigin() {
        return false;
    }


    @Override
    public SourcePosition getDeclarePosition() {
        return declareIdentifierString.getPosition();
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
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public String getSimpleName() {
        return declareIdentifierString.getValue();
    }

    @Override
    public RelationRawType getEndOrigin() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public void setEndOrigin(RelationRawType origin) {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public String[] getFullname() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public boolean isSealed() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public FullIdentifierString getDeclareIdentifier() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public void addInterfaces(RelationRawType rawType) {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public Collection<RelationRawType> getInterfaces() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public RelationRawType getParent() {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public void setParent(RelationRawType rawType) {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public StructureType getType() {
        return StructureType.GENERIC_DEFINE;
    }

    @Override
    public RelationshipBuildStage getStage() {
        return RelationshipBuildStage.GENERIC_DEFINE_AND_UPPER_PARAMETERIZED_TYPE_CHECK;
    }

    @Override
    public void updateStage(RelationshipBuildStage stage) {
        throw new CompilerException(
                "it is generic define reference, please refer to true message in type",
                new UnsupportedOperationException()
        );
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
