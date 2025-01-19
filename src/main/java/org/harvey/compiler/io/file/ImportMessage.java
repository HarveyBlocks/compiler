package org.harvey.compiler.io.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 12:15
 */
@Getter
@AllArgsConstructor
public class ImportMessage {
    private final ImportType type;
    private final String name;
}
