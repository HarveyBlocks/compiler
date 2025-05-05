package org.harvey.compiler.declare.identifier;

import lombok.Getter;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-25 11:33
 */
public class DeclareIdentifierString implements SourcePositionSupplier {

    public static final int MEANINGLESS_OBJECT_REFERENCE = 0;
    /**
     * 这个可以由identifier factory同一
     */
    private final int outerReference;
    /**
     * 由于outer reference, 似乎只需要一个最短的名字就可以了?
     * 是否需要一个全名? 如果要, 是否设计成懒加载?
     */
    @Getter
    private final SourcePosition position;
    @Getter
    private final Object name;
    @Getter
    private final DeclareType type;
    /**
     * 需要outer把表构建完毕
     */
    private final int objectReference;

    public DeclareIdentifierString(
            int outerReference, SourcePosition position, Object name, DeclareType type, int objectReference) {
        this.outerReference = outerReference;
        this.position = position;
        this.name = name;
        this.type = type;
        this.objectReference = objectReference;
        validName(type, name);
    }

    private void validName(DeclareType type, Object name) {
        switch (type) {
            case PACKAGE:
            case FILE:
            case COMPLEX_STRUCTURE:
            case ALIAS:
            case FIELD:
            case FUNCTION_OR_METHOD:
                if (!(name instanceof String)) {
                    throw new CompilerException("name should be String for " + type);
                }
                break;
            case OPERATOR:
                if (!(name instanceof Operator)) {
                    throw new CompilerException("name should be Operator for " + type);
                }
                break;
            case CONSTRUCTOR:
            case CAST:
                if (name != null) {
                    throw new CompilerException("name should be null for " + type);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public boolean noOuter() {
        return outerReference == 0;
    }

    public int getOuterIndex() {
        return outerReference - 1;
    }

    public int getObjectReference() {
        switch (type) {
            case PACKAGE:
            case FILE:
                return MEANINGLESS_OBJECT_REFERENCE; // 没有意义
            case COMPLEX_STRUCTURE:
            case ALIAS:
            case FIELD:
            case FUNCTION_OR_METHOD:
            case CONSTRUCTOR:
            case OPERATOR:
            case CAST:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return objectReference;
    }

    public enum DeclareType {
        PACKAGE, FILE, COMPLEX_STRUCTURE, ALIAS, FIELD, FUNCTION_OR_METHOD, CONSTRUCTOR, OPERATOR, CAST
    }

}
