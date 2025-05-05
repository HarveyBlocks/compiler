package org.harvey.compiler.type.generic.relate.entity;

import lombok.Getter;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;
import org.harvey.compiler.type.transform.AssignManager;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static org.harvey.compiler.common.OnlySetOnce.legalArgument;
import static org.harvey.compiler.common.OnlySetOnce.settable;

/**
 * 将关系建立起来的GenericDefine, 不适合序列化, 用于检查类型匹配是否正确
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-22 22:04
 */
@Getter
public class RelatedGenericDefine implements SourcePositionSupplier {
    private final File defineFile;
    private final IdentifierString name;
    private final boolean multiple;
    private final AssignManager assignManager;
    private RelatedParameterizedType lower;
    private RelatedParameterizedType parent;
    private RelatedParameterizedType[] interfaces;
    private List<RelatedLocalParameterizedType[]> constructors;
    private RelatedParameterizedType defaultType;

    public RelatedGenericDefine(
            File defineFile,
            IdentifierString name,
            boolean multiple,
            AssignManager assignManager,
            RelatedParameterizedType lower,
            RelatedParameterizedType parent,
            RelatedParameterizedType[] interfaces,
            List<RelatedLocalParameterizedType[]> constructors,
            RelatedParameterizedType defaultType) {
        this.defineFile = defineFile;
        this.name = name;
        this.multiple = multiple;
        this.assignManager = assignManager;
        this.lower = lower;
        this.parent = parent;
        this.interfaces = interfaces;
        this.constructors = constructors;
        this.defaultType = defaultType;
        checkValidIfAllSet();
    }

    public RelatedGenericDefine(File defineFile, IdentifierString name, boolean multiple, AssignManager assignManager) {
        this.defineFile = defineFile;
        this.name = name;
        this.multiple = multiple;
        this.assignManager = assignManager;
    }

    public <T> void set0(T argument, T filed, Consumer<T> consumer) {
        settable(filed);
        legalArgument(argument);
        consumer.accept(argument);
        checkValidIfAllSet();
    }

    public void setLower(RelatedParameterizedType lower) {
        set0(lower, this.lower, l -> this.lower = l);
    }

    public void setAbsentParent() {
        setParent(RelatedParameterizedType.absent(defineFile));
        checkValidIfAllSet();
    }

    public void setParent(RelatedParameterizedType parent) {
        set0(parent, this.parent, l -> this.parent = l);
    }

    public void setInterfaces(RelatedParameterizedType[] interfaces) {
        set0(interfaces, this.interfaces, l -> this.interfaces = l);
    }

    public void setInterface(int index, RelatedParameterizedType interfaceUpper) {
        set0(interfaceUpper, this.interfaces[index], l -> this.interfaces[index] = l);
    }

    /**
     * @param constructors 必须全部都有元素占位
     */
    public void setConstructors(List<RelatedLocalParameterizedType[]> constructors) {
        set0(constructors, this.constructors, l -> this.constructors = l);
    }

    public void setDefaultType(RelatedParameterizedType defaultType) {
        set0(defaultType, this.defaultType, l -> this.defaultType = l);
    }

    private void checkValidIfAllSet() {
        if (this.assignManager != null && allSet()) {
            assignManager.selfConsistent(this);
        }
    }

    private boolean allSet() {
        return lower != null &&
               parent != null &&
               constructors != null &&
               defaultType != null &&
               ArrayUtil.allNotNull(interfaces) &&
               constructors.stream().allMatch(ArrayUtil::allNotNull);
    }

    public void setAbsentInterfaces() {
        setInterfaces(new RelatedParameterizedType[0]);
        checkValidIfAllSet();
    }

    @Override
    public SourcePosition getPosition() {
        return name.getPosition();
    }
}
