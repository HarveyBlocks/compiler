package org.harvey.compiler.type.generic.relate.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.type.generic.using.ParameterizedType;
import org.harvey.compiler.type.raw.RelationUsing;

import java.text.MessageFormat;
import java.util.function.Function;

import static org.harvey.compiler.common.OnlySetOnce.legalArgument;
import static org.harvey.compiler.common.OnlySetOnce.settable;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 21:50
 */
@Getter
@AllArgsConstructor
public class RelatedLocalParameterizedType {
    private final boolean finalMark;
    private final boolean constMark;
    private RelatedParameterizedType type;

    public RelatedLocalParameterizedType(boolean finalMark, boolean constMark) {
        this.finalMark = finalMark;
        this.constMark = constMark;
    }

    public void setType(RelatedParameterizedType type) {
        settable(this.type);
        legalArgument(type);
        this.type = type;
    }


    /**
     * @see ParameterizedType#toString()
     */
    public String toString(Function<RelationUsing, String> toString) {
        return MessageFormat.format(
                "{0} {1} {2}", finalMark ? "" : "finale", constMark ? "" : "const", type.toString(toString));
    }
}
