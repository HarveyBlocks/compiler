package org.harvey.compiler.type.raw;

import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 19:53
 */
public class RelationshipPhaser {
    private RelationshipPhaser() {
    }
    // ----------------------------------使用关系分析-----------------------------------------

    /**
     * @return specially, 两者相同返回true
     */
    public static boolean isExtends(RelationRawType derive, RelationRawType base) {
        for (; derive != null; derive = derive.getParent()) {
            if (derive == base) { // 其实有版本不一致等等的情况怎么处理呢? 答案是不处理
                return true;
            }
            derive = derive.getParent();
        }
        return false;
    }

    public static boolean isInterfaceExtends(RelationRawType implementType, RelationRawType interfaceType) {
        LinkedList<RelationRawType> queue = new LinkedList<>();
        queue.add(implementType);
        while (!queue.isEmpty()) {
            RelationRawType first = queue.removeFirst();
            if (first == interfaceType) { // 其实有版本不一致等等的情况
                return true;
            }
            for (RelationRawType each : first.getInterfaces()) {
                queue.addLast(each);
            }
        }
        return false;
    }

    /**
     * @return specially, 两者相同返回true
     */
    public static boolean isUpper(RelationRawType son, RelationRawType parent) {
        return isExtends(son, parent) || isInterfaceExtends(son, parent);
    }
}
