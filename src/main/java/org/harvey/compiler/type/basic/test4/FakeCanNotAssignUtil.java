package org.harvey.compiler.type.basic.test4;

import lombok.AllArgsConstructor;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.function.BiConsumer;

/**
 * TODO  
 *
 * @date 2025-03-31 19:53
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
 class FakeCanNotAssignUtil {
    public static CompilerException throwExp(String name, String msg) {
        return new FakeCanNotAssignExpression(name, msg);
    }


    @AllArgsConstructor
    private static class FakeCanNotAssignExpression extends CompilerException {
        private final String name;
        private final String msg;
    }

    public static boolean catchExp(Runnable castTask, BiConsumer<String, String> onCatch) {
        try {
            castTask.run();
            return true;
        } catch (FakeCanNotAssignExpression e) {
            if (onCatch!=null){
                onCatch.accept(e.name, e.msg);
            }

            return false;
        }
    }

    public static boolean catchExp(Runnable castTask, String exchangeName) {
        try {
            castTask.run();
            return true;
        } catch (FakeCanNotAssignExpression e) {
            throw new FakeCanNotAssignExpression(exchangeName, e.msg);
        }
    }
}
