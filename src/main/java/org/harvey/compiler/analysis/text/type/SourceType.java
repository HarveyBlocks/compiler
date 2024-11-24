package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;

/**
 * 类型<br>
 * 1. var<br>
 *
 * 2. Identifier<br>
 * 3. int8 int32, bool等<br>
 * 4. unsigned int8<br>
 *
 * 5. void func<br>
 * 7. Identifier func<br>
 * 8. (Identifier) func<br>
 * 9. (Identifier1,Identifier2) func<br>
 * 10. (Identifier1,Identifier2) abstract func<br>
 *
 * 11. struct/enum/class/interface<br>
 * 12. abstract class<br>
 * 13. ...<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 19:56
 */
@Data
public abstract class SourceType {
    public static SourceType unsureType() {
        return UnsureType.instance();
    }

    public static SourceType identifierType(String identifierType) {
        return new IdentifierType(identifierType);
    }

    public static SourceType embellishAbleBasicType(Keyword embellish, Keyword basicType) {
        return new EmbellishAbleBasicType(embellish, basicType);
    }

    public static SourceType basicType(Keyword basicType) {
        if (Keywords.isBasicEmbellishAbleType(basicType)) {
            return new EmbellishAbleBasicType(Keyword.SIGNED, basicType);
        } else {
            return new BasicType(basicType);
        }
    }

    public static SourceType type(Keyword embellish, Keyword basicType, String identifierType) {
        if (embellish != null) {
            return embellishAbleBasicType(embellish, basicType);
        } else if (basicType != null) {
            return basicType(basicType);
        } else {
            return identifierType(identifierType);
        }
    }

}
