package org.harvey.compiler.text.mixed;

import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.text.decomposer.TextDecomposer;

import java.util.List;
import java.util.ListIterator;

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
        if (source.getType() != SourceType.MIXED) {
            return null;
        }
        SourceTextContext phaseString = new MixedSourceDecomposer(source).phase();
        for (ListIterator<SourceString> it = phaseString.listIterator(); it.hasNext(); ) {
            SourceString item = it.next();
            if (item.getType() == SourceType.ITEM) {
                it.remove();
                List<SourceString> phasedItemContext = new MixedItemDecomposer(item).phase();
                for (SourceString phasedItem : phasedItemContext) {
                    it.add(phasedItem);
                }
            } else if (item.getType() == SourceType.OPERATOR) {
                it.remove();
                List<SourceString> phasedItemContext = new MixedOperatorDecomposer(item).phase();
                for (SourceString phasedItem : phasedItemContext) {
                    it.add(phasedItem);
                }
            }
        }

        return phaseString;
    }


}
