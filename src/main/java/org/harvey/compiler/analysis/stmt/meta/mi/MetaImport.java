package org.harvey.compiler.analysis.stmt.meta.mi;

import lombok.Getter;
import org.harvey.compiler.analysis.stmt.meta.MetaMessage;
import org.harvey.compiler.common.entity.SourcePosition;

/**
 * Import的信息
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:40
 */
@Getter
public class MetaImport extends MetaMessage {
    // 用`.`分割
    private final String[] path;

    public MetaImport(String[] path, SourcePosition sp) {
        super(sp);
        this.path = path;
    }

    public String getTarget() {
        return path[path.length - 1];
    }
}
