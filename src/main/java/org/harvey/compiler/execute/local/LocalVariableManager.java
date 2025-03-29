package org.harvey.compiler.execute.local;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-11 22:08
 */
public interface LocalVariableManager {
    LocalTableElementDeclare forDeclare(IdentifierString name, ParameterizedType parameterizedType);

    LocalTableElementDeclare forDeclare(LocalVariableType type, IdentifierString name);

    LocalTableElementDeclare forDeclare(
            LocalVariableType type, IdentifierString name,
            ParameterizedType parameterizedType);

    boolean hasDeclared(String name);

    void intoBody();

    void leaveBody();

    LocalTableElementDeclare forUse(String name, SourcePosition position);

    /**
     * @return 返回对一个ExecutableBody的LocalVariableTable
     */
    List<LocalTableElementDeclare> getDeclarePool();

    ParameterizedType getType(LocalTableElementDeclare declare);

    @Getter
    @AllArgsConstructor
    enum LocalVariableType {
        BOOL(1),
        CHAR(2),
        INT8(1),
        UINT8(1),
        INT16(2),
        UINT16(2),
        INT32(4),
        UINT32(4),
        INT64(8),
        UINT64(8),
        FLOAT32(4),
        FLOAT64(8),
        REFERENCE(8);
        private final int offset;// 字节
    }
}
