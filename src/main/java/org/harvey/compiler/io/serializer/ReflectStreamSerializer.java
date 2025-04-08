package org.harvey.compiler.io.serializer;

import lombok.AllArgsConstructor;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.ByteUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.exception.self.CompilerException;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 利用反射将对象序列化, 解耦合度高, 但是很不完善, 而且效率低, 而且存储效率不高, 应对多变环境的能力差
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-29 16:39
 */
public class ReflectStreamSerializer {
    public static <T> StreamSerializer<T> create(Class<T> entityType) {
        // 1. entityType->JsonObject
        for (Field declaredField : entityType.getDeclaredFields()) {
            Type genericType = declaredField.getGenericType();

        }
        // 2. JsonObject->OnlyFileStatementSerializer
        return null;
    }

    public static JsonObject getJsonObject(Object entity, Type entityType, Type... typeParam)
            throws IllegalAccessException {
        if (entity == null) {
            return new JsonNull();
        }
        if (typeParam == null) {
            typeParam = new Type[0];
        }
        if (entityType == int.class) {
            return new JsonNumber(ByteUtil.toRawBytes((int) entity));
        } else if (entityType == long.class) {
            return new JsonNumber(ByteUtil.toRawBytes((long) entity));
        } else if (entityType == short.class) {
            return new JsonNumber(ByteUtil.toRawBytes((short) entity));
        } else if (entityType == byte.class) {
            return new JsonNumber(ByteUtil.toRawBytes((byte) entity));
        } else if (entityType == boolean.class) {
            return new JsonBoolean((boolean) entity);
        } else if (isString(entityType)) {
            return new JsonString((String) entity);
        } else if (entityType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) entityType;

            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) {
                return dealAsEntity(entity, (Class<?>) entityType, typeParam);
            }
            Class<?> rawClass = (Class<?>) rawType;
            if (Collection.class.isAssignableFrom(rawClass)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length != 1) {
                    throw new CompilerException("不对");
                }
                return phaseAsCollection((Collection<?>) entity, actualTypeArguments[0]);
            } else if (Map.class.isAssignableFrom(rawClass)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length != 2 || !isString(actualTypeArguments[0])) {
                    throw new CompilerException("不对");
                }
                return phaseAsMap((Map<String, ?>) entity, actualTypeArguments[1]);
            } else if (Enum.class.isAssignableFrom(rawClass)) {
                return new JsonNumber(ByteUtil.toRawBytes(((Enum) entity).ordinal()));
            } else {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                return dealAsEntity(entity, (Class<?>) rawType, actualTypeArguments);
            }
        } else if (entityType instanceof Class) {
            Class<?> entityClass = (Class<?>) entityType;
            if (entityClass.isArray()) {
                Class<?> componentType = entityClass.getComponentType();
                ArrayList<JsonObject> arr = new ArrayList<>();
                for (int i = 0; i < Array.getLength(entity); i++) {
                    arr.add(getJsonObject(Array.get(entity, i), componentType));
                }
                return new JsonArray(arr);
            }
            if (Integer.class.isAssignableFrom(entityClass)) {
                return new JsonNumber(ByteUtil.toRawBytes((int) entity));
            } else if (Long.class.isAssignableFrom(entityClass)) {
                return new JsonNumber(ByteUtil.toRawBytes((long) entity));
            } else if (Short.class.isAssignableFrom(entityClass)) {
                return new JsonNumber(ByteUtil.toRawBytes((short) entity));
            } else if (Byte.class.isAssignableFrom(entityClass)) {
                return new JsonNumber(ByteUtil.toRawBytes((byte) entity));
            } else if (Boolean.class.isAssignableFrom(entityClass)) {
                return new JsonBoolean((boolean) entity);
            } else {
                return dealAsEntity(entity, (Class<?>) entityType, typeParam);
            }
        } else {
            throw new CompilerException(entityType.getTypeName());
        }
    }

    private static JsonArray phaseAsCollection(Collection<?> c, Type actualTypeArgument)
            throws IllegalAccessException {
        ArrayList<JsonObject> arr = new ArrayList<>();
        for (Object o : c) {
            Type[] actualTypeArguments = new Type[0];
            if (actualTypeArgument instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) actualTypeArgument;
                if (o.getClass().isAssignableFrom((Class<?>) parameterizedType.getRawType())) {
                    actualTypeArguments = parameterizedType.getActualTypeArguments();
                }
            }
            arr.add(getJsonObject(o, o.getClass(), actualTypeArguments));
        }
        return new JsonArray(arr);
    }

    private static JsonEntity phaseAsMap(Map<String, ?> entity, Type actualTypeArgument) {
        Type[] actualTypeArguments = actualTypeArgument instanceof ParameterizedType ?
                ((ParameterizedType) actualTypeArgument).getActualTypeArguments() : new Type[0];
        return new JsonEntity(
                entity.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    try {
                        return getJsonObject(e.getValue(), e.getValue().getClass(), actualTypeArguments);
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                })));
    }

    private static JsonObject dealAsEntity(Object entity, Class<?> entityClass, Type[] actualTypeArguments)
            throws IllegalAccessException {
        Map<String, JsonObject> map = new HashMap<>();
        for (Class<?> currentType = entityClass; currentType != null; ) {
            Type genericSuperclass = currentType.getGenericSuperclass();
            if (genericSuperclass == null) {
                break;
            }
            JsonObject j = getJsonObject(entity, genericSuperclass);
            if (!(j instanceof JsonEntity)) {
                return j;
            } else {
                map.putAll(((JsonEntity) j).nameMap);
            }
            currentType = currentType.getSuperclass();
        }
        TypeVariable<? extends Class<?>>[] typeParameters = entityClass.getTypeParameters();
        for (Field declaredField : entityClass.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            Type genericType = declaredField.getGenericType();
            int index = ArrayUtil.at(typeParameters, genericType);
            if (index >= 0) {
                genericType = actualTypeArguments[index];
            }
            declaredField.setAccessible(true);
            map.put(declaredField.getName(), getJsonObject(declaredField.get(entity), genericType));
        }
        return new JsonEntity(map);
    }

    private static boolean isString(Type type) {
        return type instanceof Class && String.class.isAssignableFrom((Class<?>) type);
    }

    public static class JsonObject {
    }

    @AllArgsConstructor
    public static class JsonEntity extends JsonObject {
        Map<String, JsonObject> nameMap;

        @Override
        public String toString() {
            return StringUtil.concat(nameMap.entrySet(), e -> "\"" + e.getKey() + "\":" + e.getValue(), ",", "{", "}");
        }
    }

    @AllArgsConstructor
    public static class JsonArray extends JsonObject {
        ArrayList<JsonObject> array;

        @Override
        public String toString() {
            return StringUtil.concat(array, Object::toString, ",", "[", "]");
        }
    }

    @AllArgsConstructor
    public static class JsonNumber extends JsonObject {
        byte[] data;

        @Override
        public String toString() {
            return ByteUtil.phaseRawBytes(data) + "";
        }
    }

    @AllArgsConstructor
    public static class JsonString extends JsonObject {
        String value;

        @Override
        public String toString() {
            return "\"" + value + "\"";
        }
    }

    @AllArgsConstructor
    public static class JsonBoolean extends JsonObject {
        boolean value;

        @Override
        public String toString() {
            return value ? "true" : "false";
        }
    }

    private static class JsonNull extends JsonObject {
        @Override
        public String toString() {
            return "null";
        }
    }

    private static class SuperEntity {
        private int a;
        private long b;
        private boolean[] x;
        private List<Integer> ls;
        private List<String> ss;
    }

    @AllArgsConstructor
    private static class Entity extends SuperEntity {
        private int a;
        private long b;
        private boolean[] x;
        private List<Integer> ls;
        private List<String> ss;
    }


}
