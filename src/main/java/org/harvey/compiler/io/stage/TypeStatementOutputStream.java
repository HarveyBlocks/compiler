package org.harvey.compiler.io.stage;

import org.harvey.compiler.exception.CompilerException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <li><p><span>阶段二</span></p>
 *     <ul>
 *         <li><span>魔数</span></li>
 *         <li><span>完成阶段</span></li>
 *         <li><span>import表</span></li>
 *         <li><span>常量池</span></li>
 *         <li><span>已割裂源码信息</span></li>
 *     </ul>
 * </li>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 01:48
 */
public class TypeStatementOutputStream<T extends OutputStream> extends CompileFileOutputStream<T> {
    private static final CompileStage STAGE = CompileStage.TYPE_STATEMENT;

    public TypeStatementOutputStream(Class<T> streamType) {
        super(streamType);
    }

    /**
     * @param target 目标文件名, 编译后的文件名
     * @throws IOException 抛出异常
     */
    public void write(String target, CompileMessage msg) throws IOException {
        if (msg == null) {
            return;
        }
        if (!(msg instanceof TypeStatementMessage)) {
            throw new CompilerException("This output stream is not suitable");
        }
        TypeStatementMessage message = (TypeStatementMessage) msg;
        try (OutputStream os = streamConstructor.instance(target)) {
            writeHead(os, STAGE);
            // new TypeStatementSerializer(null, os).serialize(message);
            // 释放缓冲区
            os.flush();
        }
    }
}
