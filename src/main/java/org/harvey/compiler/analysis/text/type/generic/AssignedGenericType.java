package org.harvey.compiler.analysis.text.type.generic;

import lombok.Getter;
import org.harvey.compiler.common.SourceFileConstant;
import org.harvey.compiler.common.util.CollectionUtil;
import org.harvey.compiler.io.source.SourceString;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-04 21:31
 */
@Getter
public class AssignedGenericType extends NormalType {
    private final AssignedGenericArgument[] args;

    public AssignedGenericType(SourceString name, NormalType parent, AssignedGenericArgument[] args) {
        super(name, parent);
        this.args = args;
    }

    public AssignedGenericType(SourceString name, AssignedGenericArgument[] args) {
        super(name);
        this.args = args;
    }

    public AssignedGenericType(NormalType type, AssignedGenericArgument[] args) {
        super(type);
        this.args = args;
    }

    /**
     * 专注于泛型的检查
     */
    public static boolean isExtends(AssignedGenericType lower, AssignedGenericType upper,
                                    Map<String, NormalType> register) {
        if (lower == null) {
            return false;
        }
        if (upper == null) {
            return true;
        }
        if (lower.isGenericWildcard() || upper.isGenericWildcard()) {
            return true;
        }
        AssignedGenericType type = lower;
        while (true) {
            if (type == upper) {
                return true;
            }
            if (type == null) {
                return false;
            }
            if (!NormalType.nameEquals(type, upper)) {
                // 还没到同一层
                type = type.getAssignedParent(register);
                continue;
            }
            // 到同一层了
            // 进行泛型列表检查
            AssignedGenericArgument[] typeArgs = type.getArgs();
            AssignedGenericArgument[] upperArgs = upper.getArgs();
            if (typeArgs.length != upperArgs.length) {
                // 默认值已经被填充
                return false;
            }
            for (int i = 0; i < typeArgs.length; i++) {
                if (!AssignedGenericArgument.rangeSmaller(typeArgs[i], upperArgs[i], register)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean registerCorrectly(Map<String, NormalType> register) {
        AssignedGenericType assignedParent = this.getAssignedParent(register);
        if (assignedParent == null) {
            // 注册正确
            return true;
        }
        for (AssignedGenericArgument arg : assignedParent.getArgs()) {
            if (!arg.isLegalBound(register)) {
                return false;
            }
        }

        return true;
    }

    public AssignedGenericType getAssignedParent(Map<String, NormalType> register) {
        // 获取lower的父类声明
        NormalType parent = this.getParent();
        if (parent == null) {
            return null;
        }
        // 依据lower的赋值, 给父亲赋值
        // 填充register
        if (parent instanceof AssignedGenericType) {
            return (AssignedGenericType) parent;
        }
        if (!(parent instanceof GenericType)) {
            // 只是普通的NormalType, 不需要检查是否正确
            return null;
        }
        // parent可能遇到的需要的都需要填入
        Map<String, NormalType> unionRegister = this.getArgs() == null ? register : CollectionUtil.union(
                register, Arrays.stream(this.getArgs()).collect(Collectors.toMap(
                        arg -> arg.getIdentifier().getName().getValue(),
                        AssignedGenericArgument::getUpper)
                )
        );
        return GenericTypeLogicPhaser.assign((GenericType) parent, unionRegister);
    }

    public boolean isGenericWildcard() {
        return SourceFileConstant.GENERIC_WILDCARD.equals(this.getName().getValue());
    }


}
