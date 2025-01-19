package org.harvey.compiler.execute.control;

import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * TODO  
 *
 * @date 2025-01-08 23:27
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class ExecutableBody extends ArrayList<Executable> {
    public static final ExecutableBody EMPTY = new ExecutableBody();

    public static class Serializer implements StreamSerializer<ExecutableBody> {
        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public ExecutableBody in(InputStream is) {
            return null;
        }

        @Override
        public int out(OutputStream os, ExecutableBody src) {
            return 0;
        }
    }
}
