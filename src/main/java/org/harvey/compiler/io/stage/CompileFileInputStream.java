package org.harvey.compiler.io.stage;

import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.reflect.VieConstructor;
import org.harvey.compiler.exception.io.CompilerFileReaderException;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-30 15:37
 */
public class CompileFileInputStream<T extends InputStream> {
    private static final int STAGE_BC = CompileFileConstants.STAGE_BC;
    private static final int MAGIC_BC = CompileFileConstants.COMPILE_FILE_MAGIC_BC;
    private static final int MAGIC = CompileFileConstants.COMPILE_FILE_MAGIC;
    private final VieConstructor<T> streamConstructor;

    public CompileFileInputStream(Class<T> streamType) {
        this.streamConstructor = new VieConstructor<>(streamType, String.class);
    }

    /**
     * @param target 目标文件名, 编译后的文件名
     */
    public CompileMessage read(String target) throws IOException {
        try (InputStream is = streamConstructor.instance(target)) {
            // 魔数
            byte[] magicData = is.readNBytes(Serializes.bitCountToByteCount(MAGIC_BC));
            long magic = Serializes.toNumber(magicData);
            if (magic != MAGIC) {
                // 不适合用当前文件读
                return null;
            }
            // 完成阶段
            byte[] stageData = is.readNBytes(Serializes.bitCountToByteCount(STAGE_BC));
            long ordinal = Serializes.toNumber(stageData);
            CompileStage stage = CompileStage.at((int) ordinal);
            switch (stage) {
                case NONE:
                    return null;
                case STATEMENT:
                    return null;// new TypeStatementSerializer(is, null).deserialize();
                case TYPE_STATEMENT:
                    // TODO
                case FINISHED:
                    return null;
                default:
                    throw new CompilerFileReaderException("Unknown stage");
            }
        }
    }

}
