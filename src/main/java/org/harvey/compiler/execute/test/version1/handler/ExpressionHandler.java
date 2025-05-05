package org.harvey.compiler.execute.test.version1.handler;


import org.harvey.compiler.execute.test.version1.pipeline.ExpressionContext;

/**
 * 接口规范
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-03 21:24
 */
public interface ExpressionHandler {
    boolean handle(ExpressionContext context);
}
