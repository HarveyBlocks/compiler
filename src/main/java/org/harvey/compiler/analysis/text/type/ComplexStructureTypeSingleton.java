package org.harvey.compiler.analysis.text.type;

/**
 * class<br>
 * interface<br>
 * struct<br>
 * enum<br>
 * abstract class<br>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-23 21:25
 */
public enum ComplexStructureTypeSingleton {
    CLASS(new Type()),
    STRUCT(new Type()),
    INTERFACE(new Type()),
    ENUM(new Type()),
    ABSTRACT_CLASS(new Type());
    private final Type type;

    public static class Type extends SourceType {
    }

    ComplexStructureTypeSingleton(Type type) {
        this.type = type;
    }

    public static ComplexStructureTypeSingleton get(SourceType type) {
        for (ComplexStructureTypeSingleton value : ComplexStructureTypeSingleton.values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }

    public SourceType getType() {
        return type;
    }
}