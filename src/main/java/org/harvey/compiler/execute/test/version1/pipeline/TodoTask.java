package org.harvey.compiler.execute.test.version1.pipeline;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.test.version1.element.ComplexExpressionWrap;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;
import org.harvey.compiler.io.source.SourceString;

import java.util.List;


@AllArgsConstructor
@Getter
public class TodoTask {
    private final OuterEnvironment outerEnvironment;
    private final List<SourceString> todoSource;
    private final ComplexExpressionWrap wrap;
}
