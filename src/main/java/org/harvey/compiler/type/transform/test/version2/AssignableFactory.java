package org.harvey.compiler.type.transform.test.version2;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 23:03
 */
@SuppressWarnings("DuplicatedCode")
class AssignableFactory {
    public static Assignable create(Parameterized parameterized) {
        RawTypeCanInParameter value = parameterized.getRawType();
        if (value instanceof GenericDefineReference) {
            if (parameterized.childSize() != 0) {
                throw new CompilerException("Generic define 不能有 parameter");
            }
            return create((GenericDefineReference) value);
        } else if (value instanceof TempStructure) {
            return new AssignableParameterized(parameterized);
        } else {
            throw new CompilerException("Unknown type: " + value.getClass());
        }
    }

    public static AssignableGenericDefine create(GenericDefineReference value) {
        return create(value.getGeneric());
    }

    public static AssignableGenericDefine create(TempGenericDefine genericDefine) {
        return new AssignableGenericDefine(genericDefine);
    }

}
