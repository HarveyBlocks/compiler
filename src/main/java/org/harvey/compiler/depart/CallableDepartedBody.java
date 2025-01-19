package org.harvey.compiler.depart;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-02 16:13
 */
@Deprecated
@Getter
@AllArgsConstructor
public class CallableDepartedBody {
    private final LinkedList<DepartedPart> executableBody;
    private final List<DeclaredDepartedPart> localCallables;
}
