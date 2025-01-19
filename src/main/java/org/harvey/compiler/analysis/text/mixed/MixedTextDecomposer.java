package org.harvey.compiler.analysis.text.mixed;

import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.analysis.text.decomposer.TextDecomposer;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

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
        if (source.getType() != SourceStringType.MIXED) {
            return null;
        }
        SourceTextContext phaseString = new MixedSourceDecomposer(source).phase();
        for (ListIterator<SourceString> it = phaseString.listIterator(); it.hasNext(); ) {
            SourceString item = it.next();
            if (item.getType() == SourceStringType.ITEM) {
                it.remove();
                SourceTextContext phasedItemContext = new MixedItemDecomposer(item).phase();
                for (SourceString phasedItem : phasedItemContext) {
                    it.add(phasedItem);
                }
            } else if (item.getType() == SourceStringType.OPERATOR) {
                it.remove();
                SourceTextContext phasedItemContext = new MixedOperatorDecomposer(item).phase();
                for (SourceString phasedItem : phasedItemContext) {
                    it.add(phasedItem);
                }
            }
        }

        return phaseString;
    }


}
