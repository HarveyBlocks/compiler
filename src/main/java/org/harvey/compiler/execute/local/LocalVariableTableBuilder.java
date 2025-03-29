package org.harvey.compiler.execute.local;

import lombok.AllArgsConstructor;

import java.util.*;

/**
 * 思考局部变量表
 * 所谓从局部变量表中取出局部变量, 其实只要依据类型, 直到取多长, 依据offset, 直到从哪里开始取就行了
 * offset需要在编译期间思考作用域
 * 局部变量的类型, 将涉及这些二进制数的编码方式, 还是很重要的呢
 * <pre>{@code
 * reference 0
 * bool      1
 * int8      2
 * int16     3
 * int32     4
 * int64     5
 * float32   6
 * float64   7
 * }</pre>
 * char, 不再作为基础数据类型, 而是作为引用类型, 因为依据编码不同长度不同
 * 编译期间看起来是基础类型, 其实是引用类型
 * 编译期间决定offset是什么
 * <pre>{@code
 * variable_table_get_reference offset 从offset取出长reference字节的, 编码为reference类型
 * variable_table_get_bool      offset 从offset取出长一字节的, 编码为bool类型
 * variable_table_get_int8      offset 从offset取出长1字节的, 编码为int8类型
 * variable_table_get_int16     offset 从offset取出长2字节的, 编码为int16类型
 * variable_table_get_int32     offset
 * variable_table_get_int64     offset
 * variable_table_get_float32   offset
 * variable_table_get_float64   offset
 * }</pre>
 * 思考作用域作用域的概念只在编译期间出现, 其实就是对局部变量表的操作
 * <pre>{@code
 * int a0; // 0
 * {
 *     int a1; // 4
 *     int b1; // 8
 *     {
 *         int a2; // 12
 *         int b2; // 16
 *     }
 *     int c1; // 12
 * }
 * }</pre>
 * <p>
 * 局部变量表
 * 测试数据:
 * <pre>{@code
 * List.of(
 *      START,
 *      DECLARE_ + INT32, "a0",
 *      START,
 *      DECLARE_ + INT8, "a1",
 *      USING_, "a0",
 *      DECLARE_ + INT16, "b1",
 *      START,
 *      DECLARE_ + INT64, "a2",
 *      USING_, "a0",
 *      DECLARE_ + INT64, "b2",
 *      USING_, "b2",
 *      DECLARE_ + INT16, "b3",
 *      USING_, "b3",
 *      END,
 *      USING_, "a1",
 *      END,
 *      USING_, "a0",
 *      END
 * )
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-09 23:55
 */
public class LocalVariableTableBuilder {

    // 入
    public static final String START = "start";
    public static final String END = "end";
    // name, 在declare 和 using 后面一个
    // 出
    public static final String DECLARE_ = "declare_";
    public static final String INT8 = "int8";
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    // 出的时候吧后面的填好
    public static final String USING_ = "using_";

    public static List<String> buildVariableTable(List<String> src) {
        Stack<Integer> counter = new Stack<>();
        counter.push(0);
        Stack<Map<String, VarMsg>> nameOffset = new Stack<>();
        List<String> result = new ArrayList<>();
        for (Iterator<String> it = src.iterator(); it.hasNext(); ) {
            String next = it.next();
            if (next.startsWith(DECLARE_)) {
                String name = it.next();
                if (containsKeyInMap(nameOffset, name)) {
                    throw new RuntimeException("重复声明");
                }
                String type = next.substring(DECLARE_.length());
                Map<String, VarMsg> peek = nameOffset.peek();
                peek.put(name, new VarMsg(name, counter.peek(), type));
                counter.push(counter.pop() + occupy(type));
                result.add(next);
            } else if (next.startsWith(USING_)) {
                String name = it.next();
                VarMsg msg = getFormMap(nameOffset, name);
                if (msg == null) {
                    throw new RuntimeException("未声明变量");
                }
                result.add(next + msg.type + "_" + msg.offset);
            } else if (END.equals(next)) {
                nameOffset.pop();
                counter.pop();
            } else if (START.equals(next)) {
                nameOffset.push(new HashMap<>());
                counter.push(counter.peek());
            }
        }
        return result;
    }

    private static VarMsg getFormMap(Stack<Map<String, VarMsg>> nameOffset, String key) {
        for (Map<String, VarMsg> map : nameOffset) {
            VarMsg msg = map.get(key);
            if (msg != null) {
                return msg;
            }
        }
        return null;
    }

    private static boolean containsKeyInMap(Stack<Map<String, VarMsg>> nameOffset, String key) {
        for (Map<String, VarMsg> map : nameOffset) {
            if (map.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private static int occupy(String type) {
        switch (type) {
            case INT8:
                return 1;
            case INT16:
                return 2;
            case INT32:
                return 4;
            case INT64:
                return 8;
        }
        throw new RuntimeException("不存在的类型");
    }

    public static void main(String[] args) {
        List<String> variableTable = buildVariableTable(List.of(
                START,
                DECLARE_ + INT32, "a0",
                START,
                DECLARE_ + INT8, "a1",
                USING_, "a0",
                DECLARE_ + INT16, "b1",
                START,
                DECLARE_ + INT64, "a2",
                USING_, "a0",
                DECLARE_ + INT64, "b2",
                USING_, "b2",
                DECLARE_ + INT16, "b3",
                USING_, "b3",
                END,
                DECLARE_ + INT16, "b3",
                USING_, "a1",
                END,
                USING_, "a0",
                END
        ));
        System.out.println(variableTable);
    }

    @AllArgsConstructor
    public static class VarMsg {
        String name;
        int offset;
        String type;
    }
}
