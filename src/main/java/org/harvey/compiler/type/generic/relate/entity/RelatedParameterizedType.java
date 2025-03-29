package org.harvey.compiler.type.generic.relate.entity;

import lombok.Getter;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.RelationUsing;

import java.io.File;
import java.util.function.Function;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 21:46
 */
@Getter
public class RelatedParameterizedType {
    private final File fromFile;
    private final ParameterizedType<RelationUsing> value;

    public RelatedParameterizedType(File fromFile, ParameterizedType<RelationUsing> value) {
        this.fromFile = fromFile;
        this.value = value;
    }

    public static RelatedParameterizedType absent(File fromFile) {
        return new RelatedParameterizedType(fromFile, new ParameterizedType<>((MultipleTree<RelationUsing>) null));
    }


    public RelationUsing getRawType() {
        return value.getRawType();
    }

    public boolean isNull() {
        return value == null || value.isNull();
    }

    /**
     * @see ParameterizedType#toString()
     */
    public String toString(Function<RelationUsing, String> toString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : value.toStringList(toString)) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }
}
