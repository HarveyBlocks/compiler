package org.harvey.compiler.analysis.calculate;

import org.harvey.compiler.io.source.SourcePosition;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class OperatorsTest {

    @Test
    public void tryToFind() {
        Arrays.stream(Operator.values())
                .collect(Collectors.groupingBy(Operator::getName, Collectors.counting()))
                .forEach((k, v) -> System.out.println(k + "\t" + v));
        // ++,--, 左边是常量/符号的, 此++是右结合, 否则报错, 如果Number的前面是++, 直接报错
        // +, -, 左边是符号的,是正负号, 左边是Identity的, 是加减号
        // (), 左边是Identity的, 是函数调用, 否则, 是括号
        // <>,
        System.out.println();
        System.out.println(Operators.trySplit("++?++-+-(())()(())((()()()", SourcePosition.UNKNOWN));
    }
}