package org.harvey.compiler.type.basic.test2;

import java.util.function.BiConsumer;

/**
 * TODO  
 *
 * @date 2025-03-30 23:05
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@SuppressWarnings("DuplicatedCode")
class AssignableGenericDefine implements Assignable {
    final TempGenericDefine to;
    Assignable parentAassignable;
    final Assignable[] interfacesAssignable;

    AssignableGenericDefine(TempGenericDefine genericDefine) {
        to = genericDefine;
        interfacesAssignable = to == null ? null : new Assignable[to.interfaces.length];
    }

    @Override
    public void assign(Parameterized from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw FakeCanNotAssignUtil.throwExp("from no name", "can not assign for parent: " + to.parent);
        }
        if (to.parent != null) {
            // 要成功
            getParentAssign().assign(from);
        }
        if (to.interfaces != null) {
            for (int i = 0; i < to.interfaces.length; i++) {
                Assignable interfaceAssign = getInterfaceAssign(i);
                // 每个都要符合
                interfaceAssign.assign(from);
            }
        }
    }

    private Assignable getParentAssign() {
        if (parentAassignable == null) {
            parentAassignable = AssignableFactory.create(to.parent);
        }
        return parentAassignable;
    }

    private Assignable getInterfaceAssign(int interfaceIndex) {
        Assignable interfaceAssign = interfacesAssignable[interfaceIndex];
        if (interfaceAssign == null) {
            interfacesAssignable[interfaceIndex] = AssignableFactory.create(to.interfaces[interfaceIndex]);
        }
        return interfacesAssignable[interfaceIndex];
    }

    @Override
    public void assign(TempGenericDefine from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw FakeCanNotAssignUtil.throwExp("from is null", "can not assign");
        }
        if (to.parent != null) {
            assignParent(from, from.parent);
        }
        // 任意一个to的interface, 都有一个from的interface对应
        if (to.interfaces != null) {
            for (int i = 0; i < to.interfaces.length; i++) {
                findOneInterfaceToAssign(i, from);
            }
        }

    }

    private void findOneInterfaceToAssign(int eachToInterfaceIndex, TempGenericDefine from) {
        Assignable eachToInterfaceAssignable = getInterfaceAssign(eachToInterfaceIndex);
        eachToInterfaceAssignable.assign(from.parent);
        boolean successful = FakeCanNotAssignUtil.catchExp(
                () -> eachToInterfaceAssignable.assign(from.parent), (BiConsumer<String, String>) null);
        if (successful) {
            // 可以了
            return;
        }
        for (Parameterized eachFromInterface : from.interfaces) {
            // 存在一个能匹配
            successful = FakeCanNotAssignUtil.catchExp(
                    () -> eachToInterfaceAssignable.assign(eachFromInterface), (BiConsumer<String, String>) null);
            if (successful) {
                break;
            }
        }
        if (!successful) {
            throw FakeCanNotAssignUtil.throwExp(
                    from.name,
                    "can not assign for no upper: " + to.interfaces[eachToInterfaceIndex].getRawType().getName()
            );
        }
    }

    private void assignParent(TempGenericDefine from, Parameterized fromParent) {
        if (to.parent == null) {
            return;
        }
        if (fromParent == null) {
            throw FakeCanNotAssignUtil.throwExp(from.name, "can not assign");
        }
        getParentAssign().assign(from.parent);
    }
}

