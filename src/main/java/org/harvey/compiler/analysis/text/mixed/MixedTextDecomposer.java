package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.analysis.text.decomposer.TextDecomposer;

import java.util.ListIterator;
import java.util.Objects;

/**
 * 解析属性是Mixed的字段
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-18 00:11
 */
public class MixedTextDecomposer implements TextDecomposer {

    @Override
    public SourceTextContext decompose(SourceString source) {
        if (source.getType() != SourceStringType.MIXED) {
            return null;
        }
        SourceTextContext phaseString = new MixedStringDecomposer(source).phase();
        for (ListIterator<SourceString> it = phaseString.listIterator(); it.hasNext(); ) {
            SourceString item = it.next();
            if (Objects.requireNonNull(item.getType()) == SourceStringType.ITEM) {
                it.remove();
                SourceTextContext phasedItemContext = new MixedItemDecomposer(item).phase();
                for (SourceString phasedItem : phasedItemContext) {
                    it.add(phasedItem);
                }
            }
        }

        return phaseString;
    }


}
