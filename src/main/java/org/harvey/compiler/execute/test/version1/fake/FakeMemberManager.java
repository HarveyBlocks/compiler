package org.harvey.compiler.execute.test.version1.fake;

import lombok.AllArgsConstructor;
import org.harvey.compiler.execute.test.version1.manager.MemberManager;
import org.harvey.compiler.execute.test.version1.msg.CallableRelatedDeclare;
import org.harvey.compiler.execute.test.version1.msg.MemberSupplier;
import org.harvey.compiler.execute.test.version1.msg.MemberType;
import org.harvey.compiler.execute.test.version1.msg.PossibleCallableSupplier;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.Map;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-11 00:00
 */
@AllArgsConstructor
public class FakeMemberManager implements MemberManager {
    /**
     * value name-type
     */
    private final Map<String, MemberType> memberSupplierMap;
    private final Map<String, CallableRelatedDeclare[]> possibleCallableSupplierHashMap;

    @Override
    public MemberSupplier create(SourcePosition using, String name) {
        MemberType memberTypeForValue = memberSupplierMap.get(name);
        if (memberTypeForValue != null) {
            return new FakeMemberSupplier(using, memberTypeForValue);
        }
        return createPossibleCallable(using, name);
    }

    @Override
    public PossibleCallableSupplier createPossibleCallable(SourcePosition using, String callableName) {
        return new FakePossibleCallableSupplier(using, possibleCallableSupplierHashMap.get(callableName));
    }

}
