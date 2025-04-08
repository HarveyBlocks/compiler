package org.harvey.compiler.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.CompileMultipleFileException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.execution.CompileExecutionException;
import org.harvey.compiler.exception.io.CompileFileException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * 对异常进行封装的工具类
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 15:22
 */
public class ExceptionUtil {
    public static <T, R> R throwExceptionForNull(T t, Function<T, SourcePosition> positionSupplier, String msg) {
        SourcePosition sp = null;
        if (t != null) {
            sp = positionSupplier.apply(t);
        }
        if (sp == null) {
            throw new CompilerException(msg);
        }
        throw new AnalysisExpressionException(sp, msg);
    }

    public static void iteratorHasNext(ListIterator<SourceString> iterator, String msg) {
        if (iterator.hasNext()) {
            return;
        }
        if (iterator.hasPrevious()) {
            throw new AnalysisExpressionException(iterator.previous().getPosition(), msg);
        } else {
            throw new CompilerException(msg);
        }
    }

    @FunctionalInterface
    public interface ThrowsTask {
        void execute() throws Exception;
    }

    @AllArgsConstructor
    @Getter
    public static class FileExceptionFactory {
        private final File file;

        public CompileMultipleFileException create(SourcePosition errorPosition, String message) {
            return new CompileMultipleFileException(file, errorPosition, message);
        }

        public CompileMultipleFileException create(
                SourcePosition errorPositionBegin,
                SourcePosition errorPositionEnd,
                String message) {
            return new CompileMultipleFileException(file, errorPositionBegin, errorPositionEnd, message);
        }

        public CompileMultipleFileException create(SourcePosition errorPosition, String message, Throwable throwable) {
            return new CompileMultipleFileException(file, errorPosition, message, throwable);
        }

        public CompileMultipleFileException create(
                SourcePosition errorPositionBegin, SourcePosition errorPositionEnd, String message,
                Throwable throwable) {
            return new CompileMultipleFileException(file, errorPositionBegin, errorPositionEnd, message, throwable);
        }
    }

    public static class ExceptionWrap {
        public static void invoke(String[] packages, String file, ThrowsTask task) {
            try {
                task.execute();
            } catch (IOException | CompileFileException | CompileExecutionException ce) {
                System.err.println(errorMessage(packages, file, ce));
                System.err.println("------------------------------------------");
                ce.printStackTrace(System.err);
            } catch (Exception e) {
                throw new CompilerException(errorMessageOnCompilerSelf(packages, file), e);
            }
        }

        public static void invoke(String[] packages, File file, ThrowsTask task) {
            invoke(packages, file.getName(), task);
        }

        private static String errorMessageOnCompilerSelf(String[] packages, String file) {
            return "Compiler internal exceptions, it is recommended to report" +
                   "[mailto:harvey.blocks@outlook.com] on " +
                   Arrays.toString(packages) +
                   "." +
                   file;
        }

        private static String errorMessage(String[] packages, String file, Exception ce) {
            return "[ERROR] " +
                   " `" +
                   Arrays.toString(packages) +
                   "." +
                   file +
                   "`" +
                   ce.getClass().getSimpleName() +
                   ":" +
                   ce.getMessage();
        }
    }
}
