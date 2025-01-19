package org.harvey.compiler.io.stage;

import org.harvey.compiler.common.CompileFileConstants;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.reflect.VieConstructor;
import org.harvey.compiler.io.serializer.HeadMap;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-30 18:13
 */
public abstract class CompileFileOutputStream<T extends OutputStream> {
    protected final VieConstructor<T> streamConstructor;

    public CompileFileOutputStream(Class<T> streamType) {
        this.streamConstructor = new VieConstructor<>(streamType, String.class);
    }

    protected static void writeHead(OutputStream os, CompileStage stage) throws IOException {
        // 魔数
        os.write(CompileFileConstants.COMPILE_FILE_MAGIC);
        // 完成阶段
        Serializes.notTooMuch(stage.ordinal(), "compile file stage", CompileFileConstants.MAX_STAGE);
        os.write(Serializes.makeHead(new HeadMap(stage.ordinal(), CompileFileConstants.STAGE_BC)).data());
    }

    public abstract void write(String target, CompileMessage msg) throws IOException;
}
