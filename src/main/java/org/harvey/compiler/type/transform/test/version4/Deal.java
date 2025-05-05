package org.harvey.compiler.type.transform.test.version4;

import java.util.LinkedList;

/**
 * TODO
 * <p>
 * 1. 基础 1 upper 1 parents 多 generic define
 * 1.1 工程量变大, 多个文件拆分一下
 * 2. 增加 多 uppers 和 多 parents
 * 2.1 多uppers , generic define 的检查 和 assign 改变
 * 2.2 多parents, raw type检查改变, 查找路径的方法改变, 要'选择'路径了;
 * 3. 增加lower
 * 3.1 增加自我检查lower的正确性
 * 3.2 检查lower在parents之下
 * 3.3 检查 gd = p 时, p 在gd.lower之上
 * 3.4 检查 gd_t = gd_f 时, gd_f.lower在gd_t.lower之上
 * 3.5 p = gd 时 不需要检查 lower
 * 4. 增加multiple
 * 4.1 在strict之后, 还有的, 都行last故事, 赋值
 * 4.2 gd = p 的, 不需要
 * 4.3 gd = gd 的, 需要, multiple不能赋值到非multiple上去
 * 4.4 p = gd 的, 不需要, p rawType 不是 genericDefine, 一定是Structure
 * 但如果gd是multiple, 那么一定不能赋值到p上去
 * 4.5 p = p 的,  需要, 严格检查, 也要考虑multiple
 * 5. 增加 alias
 * 6. 增加 default, default 的检查 和 generic list 匹配算法
 * 7. 增加 new / constructor
 * 8. 部署
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 23:09
 */
@SuppressWarnings("DuplicatedCode")
class Deal {
    static TempStructure upper1 = new TempStructure("Upper1");
    static TempStructure upper2 = new TempStructure("Upper2");
    static TempStructure interface1 = new TempStructure("Interface1");
    static TempStructure interface2 = new TempStructure("Interface2");
    static TempStructure interface3 = new TempStructure("Interface3");
    static TempStructure interface4 = new TempStructure("interface4");
    static TempStructure exampleClass1 = new TempStructure("exampleClass1");
    static TempStructure object = new TempStructure("Object");


    static void buildRelation() {
        // 这一阶段测试多继承(接口继承)

    }

    public static void main(String[] args) {
        buildRelation();
//        System.out.println(upper);
//        System.out.println(exampleClass1);
//        System.out.println(exampleClass2);

        // extracted(exampleClass1);
        // extracted(exampleClass2);

        System.out.println(exampleClass1);


        /*upper.selfConsistent(constant.TODO, toBeChecks);
        upper.selfConsistent(constant.RAW_TYPE, toBeChecks);
        upper.selfConsistent(constant.PARAMETER, toBeChecks);
        upper.selfConsistent(constant.FINISH, toBeChecks);*/
    }

    private static void deal(TempStructure type) {
        int i = 0;
        do {
            LinkedList<ToBeCheck> toBeChecks = new LinkedList<>();
            toBeChecks.addLast(new ToBeCheck(null, type));
            while (!toBeChecks.isEmpty()) {
                ToBeCheck first = toBeChecks.removeLast();
                Level outerLevel = first.earnFromStatus == null ? Level.TODO : first.earnFromStatus.getLevel();
                System.out.println(first.earnTarget + ", " + outerLevel);
                first.earnTarget.selfConsistent(outerLevel, toBeChecks);
            }
            System.out.println(i++);
        } while (type.getLevel() != Level.FINISH);
    }
}

// 为什么有了多继承就不能有接口了?
