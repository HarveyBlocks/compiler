package org.harvey.compiler.common.reflect;

import java.lang.reflect.*;

/**
 * 获取泛型, 但并不完整, 且不能保证正确性
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-11 23:29
 */
@Deprecated
public class GenericGetter {
    public static Class<?> find(Object obj, Class<?> parametrizedSuperclass, String typeParamName) {
        Class<?> thisClass = obj.getClass();
        Class<?> current = thisClass;
        while (true) {
            if (current.getSuperclass() != parametrizedSuperclass) {
                current = current.getSuperclass();
                if (current == null) {
                    return fail(thisClass, typeParamName);
                }
                continue;
            }

            int typeParamIndex = getTypeParamIndex(typeParamName, current, parametrizedSuperclass);
            Type genericSuperType = current.getGenericSuperclass();
            if (!(genericSuperType instanceof ParameterizedType)) {
                return Object.class;
            }

            Type actualTypeParam = getActualTypeParam((ParameterizedType) genericSuperType, typeParamIndex);
            if (actualTypeParam instanceof Class) {
                return (Class<?>) actualTypeParam;
            }
            Class<?> componentType = asGenericArray(actualTypeParam);
            if (componentType != null) {
                return componentType;
            }
            if (!(actualTypeParam instanceof TypeVariable)) {
                return fail(thisClass, typeParamName);
            }
            TypeVariable<?> v = (TypeVariable<?>) actualTypeParam;
            if (!(v.getGenericDeclaration() instanceof Class)) {
                return Object.class;
            }
            current = thisClass;
            parametrizedSuperclass = (Class<?>) v.getGenericDeclaration();
            typeParamName = v.getName();
            if (parametrizedSuperclass.isAssignableFrom(thisClass)) {
                continue;
            }
            return Object.class;
        }
    }

    private static Type getActualTypeParam(ParameterizedType genericSuperType, int typeParamIndex) {
        Type[] actualTypeParams = genericSuperType.getActualTypeArguments();

        Type actualTypeParam = actualTypeParams[typeParamIndex];
        if (actualTypeParam instanceof ParameterizedType) {
            actualTypeParam = ((ParameterizedType) actualTypeParam).getRawType();
        }
        return actualTypeParam;
    }

    private static Class<?> asGenericArray(Type actualTypeParam) {
        if (actualTypeParam instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) actualTypeParam).getGenericComponentType();
            if (componentType instanceof ParameterizedType) {
                componentType = ((ParameterizedType) componentType).getRawType();
            }
            if (componentType instanceof Class) {
                return Array.newInstance((Class<?>) componentType, 0).getClass();
            }
        }
        return null;
    }

    private static int getTypeParamIndex(String typeParamName, Class<?> current, Class<?> parametrizedSuperclass) {
        TypeVariable<?>[] typeParams = current.getSuperclass().getTypeParameters();
        for (int i = 0; i < typeParams.length; i++) {
            if (typeParamName.equals(typeParams[i].getName())) {
                return i;
            }
        }
        throw new IllegalStateException(
                "unknown type parameter '" + typeParamName + "': " + parametrizedSuperclass);
    }

    private static Class<?> fail(Class<?> type, String typeParamName) {
        throw new IllegalStateException(
                "cannot determine the type of the type parameter '" + typeParamName + "': " + type);
    }
}
