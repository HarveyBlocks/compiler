package org.harvey.compiler.analysis.text.type;

import lombok.Getter;
import org.harvey.compiler.declare.phaser.visitor.Environment;

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
@Getter
public enum ComplexStructureTypeSingleton {
    CLASS(Environment.CLASS),
    STRUCT(Environment.STRUCT),
    INTERFACE(Environment.INTERFACE),
    ENUM(Environment.ENUM),
    ABSTRACT_CLASS(null);
    private final Environment environment;

    ComplexStructureTypeSingleton(Environment environment) {
        this.environment = environment;
    }
}