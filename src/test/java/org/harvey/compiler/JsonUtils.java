package org.harvey.compiler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * json工具类, 仅用于测试
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-04-26 15:40
 */
public class JsonUtils {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 对所有调用次方法之后的转化都生效, 不会报错了
     */
    public static void ignoreUnknownFieldInJson() {
        // 对于Json中的未知字段选择忽略
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 对所有调用次方法之后的转化都生效, 继续报错
     */
    public static void emphasisUnknownFieldInJson() {
        // 对于Json中的未知字段选择重视
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    public static String toJsonStr(Object bean) {
        try {
            return MAPPER.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public static String toJsonStr(HashMap<String, Object> map) {
        //map<String,String>转json
        try {
            return MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T toBean(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> T[] toBeanArray(String arrayJson, Class<T[]> type) {
        try {
            return MAPPER.readValue(arrayJson, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> toBeanList(String listJson, Class<T> type) {
        try {
            return MAPPER.readValue(listJson, new TypeReference<List<T>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> toMap(String json) {
        try {
            return MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Map<?, ?> toMap(Object bean) {
        return MAPPER.convertValue(bean, Map.class);
    }

    public static <T> T toBean(Map<String, Object> map, Class<T> type) {
        return MAPPER.convertValue(map, type);
    }
}
