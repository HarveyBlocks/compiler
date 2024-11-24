package org.harvey.compiler.analysis.text.context;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:03
 */
@Data
@NoArgsConstructor
public class UserConfiguration {
    private String workPath = ""; // 工作目录
    private String compileTargetPath = ""; // 编译目标目录
    private String importBasePath = ""; // import是基于哪个目录开始的?
}
