package org.harvey.compiler.io.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 12:20
 */
@Getter
@AllArgsConstructor
public class ImportMessageContext {
    private final List<ImportMessage> table;
}
