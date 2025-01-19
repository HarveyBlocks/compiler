package org.harvey.compiler.declare.context;

import org.junit.Test;

import java.util.List;

public class CallableContextTest {

    @Test
    public void matchParam() {
        System.out.println(CallableContext.matchParam(
                List.of(),
                List.of()
        ));
    }
}