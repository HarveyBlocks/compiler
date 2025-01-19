package org.harvey.compiler.analysis.text.type;

import lombok.Data;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.text.context.SourceTextContext;

/**
 * 类型<br>
 * 1. var<br>
 * <p>
 * 2. Identifier<br>
 * 3. int8 int32, bool等<br>
 * 4. unsigned int8<br>
 * <p>
 * 5. void func<br>
 * 7. Identifier func<br>
 * 8. (Identifier) func<br>
 * 9. (Identifier1,Identifier2) func<br>
 * 10. (Identifier1,Identifier2) abstract func<br>
 * <p>
 * 11. struct/enum/class/interface<br>
 * 12. abstract class<br>
 * <p>
 * 14. 类型[]<br>
 * 15. 类型{@code <类型,类型,...>}<br>
 * <p>
 * 15. ...<br>
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


    public static SourceType basicType(Keyword basicType) {
        return new BasicType(basicType);
    }

    public static SourceType type(Keyword basicType, String identifierType) {
        if (basicType != null) {
            return basicType(basicType);
        } else {
            return identifierType(identifierType);
        }
    }

    public static SourceType arrayType(SourceType elementType, int dimension) {
        return new ArrayType(elementType, dimension);
    }


    public static boolean simplyLegalGenericList(SourceTextContext origin) {
        return origin.isEmpty() ||
                Operator.GENERIC_LIST_PRE.nameEquals(origin.getFirst().getValue()) &&
                        Operator.GENERIC_LIST_POST.nameEquals(origin.getLast().getValue());
    }
}
