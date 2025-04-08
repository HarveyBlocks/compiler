package org.harvey.compiler.io.cache.resource;

import org.harvey.compiler.declare.context.FileContext;
import org.harvey.compiler.declare.context.StructureContext;
import org.harvey.compiler.io.PackageMessage;

import java.util.function.Function;

/**
 * 将文件资源的raw转换成需要的类型, 方便进一步使用W
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 10:48
 */
public class RawTransitionPipeline<R> {
    private Function<StructureContext, R> function1;
    private Function<FileContext, R> function2;
    private Function<PackageMessage, R> function3;


    public RawTransitionPipeline<R> addDealComplexStructureContext(Function<StructureContext, R> function) {
        this.function1 = function;
        return this;
    }

    public RawTransitionPipeline<R> addDealFileContext(Function<FileContext, R> function) {
        this.function2 = function;
        return this;
    }

    public RawTransitionPipeline<R> addDealBeforeFilePackageMessage(Function<PackageMessage, R> function) {
        this.function3 = function;
        return this;
    }

    public R invoke(StatementResource resource) {
        Object raw = resource.getRaw();
        if (raw instanceof StructureContext) {
            return function1 == null ? null : function1.apply((StructureContext) raw);
        }
        if (raw instanceof FileContext) {
            return function2 == null ? null : function2.apply((FileContext) raw);
        }
        if (raw instanceof PackageMessage) {
            return function3 == null ? null : function3.apply((PackageMessage) raw);
        }
        return null;
    }
}
