package org.harvey.compiler.io.source;


import lombok.Getter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 23:59
 */
@Getter
public enum SourceCharset {
    UTF_8(StandardCharsets.UTF_8),
    ISO_8859_1(StandardCharsets.ISO_8859_1),
    US_ASCII(StandardCharsets.US_ASCII),
    UTF_16(StandardCharsets.UTF_16),
    UTF_16LE(StandardCharsets.UTF_16LE),
    UTF_16BE(StandardCharsets.UTF_16BE);
    private final Charset charset;


    SourceCharset(Charset charset) {
        this.charset = charset;
    }

    public static SourceCharset get(int ordinal) {
        return SourceCharset.values()[ordinal];
    }

}
