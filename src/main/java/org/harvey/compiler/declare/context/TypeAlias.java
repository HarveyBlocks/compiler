package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO
 *
 * @date 2025-01-13 23:56
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public class TypeAlias {
    private final AccessControl accessControl;
    private final int aliasNameReference;
    private final Expression aliasGenericMessage;
    private final Expression origin;

    public static class Serializer implements StreamSerializer<TypeAlias> {
        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public TypeAlias in(InputStream is) {
            return null;
        }

        @Override
        public int out(OutputStream os, TypeAlias src) {
            return 0;
        }
    }
}
