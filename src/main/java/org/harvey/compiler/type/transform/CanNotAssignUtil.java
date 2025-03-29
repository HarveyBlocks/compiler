package org.harvey.compiler.type.transform;

import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.VieCompilerException;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.File;

/**
 * 规范, 如果要catch异常, catch哪一个, 如果要throw, throw 哪一个.
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-25 23:57
 */
public class CanNotAssignUtil {
    private static class CanNotAssign extends CompileMultipleFileException {
        private final File file;
        private final SourcePosition errorPosition;
        private final String message;

        public CanNotAssign(File file, SourcePosition errorPosition, String message) {
            super(file, errorPosition, message);
            this.file = file;
            this.errorPosition = errorPosition;
            this.message = message;
        }
    }

    public static VieCompilerException canNotAssign(File file, SourcePosition errorPosition, String message) {
        return new CanNotAssign(file, errorPosition, message);
    }

    @FunctionalInterface
    public interface OnCanNotAssign {
        public void accept(File file, SourcePosition errorPosition, String message);
    }

    public static boolean catchCanNotAssign(Runnable task, OnCanNotAssign afterThrow) {
        try {
            task.run();
            return true;
        } catch (CanNotAssign cna) {
            if (afterThrow!=null){
                afterThrow.accept(cna.file, cna.errorPosition, cna.message);
            }
            return false;
        }
    }
}
