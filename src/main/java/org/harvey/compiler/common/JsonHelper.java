package org.harvey.compiler.common;

import org.harvey.compiler.common.util.StringUtil;

/**
 * 将Lombok的ToString转换成Json心事
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-27 12:54
 */
@Deprecated
public class JsonHelper {
    public static String toJson(Object o) {
        return toJson(String.valueOf(o));
    }

    public static String toJson(String lombokString) {
        String str = lombokString;
        if (StringUtil.isBlank(str)) {
            return "Parameter Cannot Be Empty";
        }
        str = str.replace(")", "}");
        while (str.contains("(")) {
            int i = str.indexOf("(");
            int i1 = str.lastIndexOf("=", i);
            String str1 = str.substring(0, i1 + 1);
            String str4;
            str4 = str.substring(i1 + 1, i1 + 2);
            String str2 = "{";
            String str3 = str.substring(i + 1);
            str = "[".equals(str4) ? str1 + str4 + str2 + str3 : str1 + str2 + str3;
        }
        str = str.replace(" ", "")
                .replace("=", "\"=\"")
                .replace(",", "\",\"")
                .replace("\"null\"", "null")
                .replace("\"{", "{\"")
                .replace("}\"", "}")
                .replace("}", "\"}")
                .replace("\"}\"}", "\"}}")
                .replace("\"[{", "[{\"")
                .replace("]\"", "]");

        str = "\"" + str.replace("=", ":");
        if ("\"{".equals(str.substring(0, 2))) {
            str = "{\"" + str.substring(2);
        } else {
            str = str.substring(str.indexOf(":") + 1);
        }
        str = str.replace("\"null\"", "null");
        return str;
    }

}
