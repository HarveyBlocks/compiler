package org.harvey.compiler.type.transform;

import lombok.AllArgsConstructor;
import org.harvey.compiler.execute.expression.KeywordString;
import org.harvey.compiler.type.generic.relate.ParameterizedRelationCache;
import org.harvey.compiler.type.generic.relate.ParameterizedRelationLoader;
import org.harvey.compiler.type.generic.relate.RelatedGenericDefineCache;
import org.harvey.compiler.type.generic.relate.entity.*;
import org.harvey.compiler.type.raw.RelationRawType;
import org.harvey.compiler.type.raw.RelationUsing;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 21:44
 */
@AllArgsConstructor
public class GenericDefineAssignable implements Assignable {
    private final RelatedGenericDefine to;
    private final ParameterizedRelationCache parameterizedRelationCache;
    private final AssignableFactory assignableFactory;
    private final RelatedGenericDefineCache relatedGenericDefineCache;

    @Override
    public void assign(RelatedGenericDefine from) {
    }

    @Override
    public void assign(RelatedParameterizedType from) {

    }

    /**
     * lower<=from<=任意Upper
     * <pre>{@code
     *  if(from < lower):
     *      throw ERROR
     *  for upper in uppers:
     *      if(!(from < upper)):
     *          throw ERROR
     * }</pre>
     */
    @Override
    public void assign(RelationUsing from) {
        RelationRawType fromRawType = from.getRawType();
        if (fromRawType.isBasicType()) {
            assignKeyword(fromRawType);
            return;
        }
        if (fromRawType.isGenericDefine()) {
            RelatedGenericDefineReference reference = (RelatedGenericDefineReference) fromRawType;
            RelatedGenericDefine fromGenericDefine = relatedGenericDefineCache.get(reference);
            CanNotAssignUtil.catchCanNotAssign(() -> assign(fromGenericDefine), (f, e, m) -> {
                throw CanNotAssignUtil.canNotAssign(from.getRawType().getFromFile(), from.getPosition(), m);
            });
            return;
        }
        // to.lower <= from
        RelatedParameterizedType lower = to.getLower();
        RawTypeAssignable fromAssignable = assignableFactory.of(fromRawType);
        fromAssignable.assign(lower);
        // from <= to.parent
        RelatedParameterizedType parent = to.getParent();
        assignableFactory.of(parent).assign(from);
        // from<= to.interfaces
        RelatedParameterizedType[] interfaces = to.getInterfaces();
        for (RelatedParameterizedType each : interfaces) {
            assignableFactory.of(each).assign(from);
        }
        RelatedParameterizedStructure endOrigin = parameterizedRelationCache.get(fromRawType).getEndOrigin();
        List<CallableSignature> constructorHave = endOrigin.getConstructors();
        /*constructor匹配*/
        // 怎么匹配 constructor的
        // constructor 的param有参数
        CanNotAssignUtil.catchCanNotAssign(
                () -> matchConstructors(constructorHave, from, from.getRawType().getFromFile()), (f, e, m) -> {
                    throw CanNotAssignUtil.canNotAssign(from.getRawType().getFromFile(), from.getPosition(), m);
                });
        /*endOrigin*/
    }


    private void assignKeyword(RelationRawType from) {
        if (!to.getLower().isNull() || !to.getParent().isNull() || to.getInterfaces().length != 0) {
            throw CanNotAssignUtil.canNotAssign(from.getFromFile(), from.getPosition(),
                    "basic type can not assign to this generic define, for generic define has bounds"
            );
        }
        // 正确
        // constructor
        for (RelatedLocalParameterizedType[] constructor : to.getConstructors()) {
            if (constructor.length == 0) {
                // 无参数的, 可以, 过
                continue;
            }
            if (constructor.length == 1) {
                RelatedLocalParameterizedType onlyParam = constructor[0];
                RelationRawType rawType = onlyParam.getType().getRawType().getRawType();
                if (rawType.isBasicType()) {
                    KeywordString keywordExcept = ParameterizedRelationLoader.loadBasic(rawType);
                    KeywordString keywordHas = ParameterizedRelationLoader.loadBasic(from);
                    DefaultAssignManager.assignable(keywordExcept, keywordHas, from.getFromFile());
                    continue;
                }
            }
            throw CanNotAssignUtil.canNotAssign(from.getFromFile(), from.getPosition(),
                    "basic type can not assign to this generic define, " +
                    "for generic define has can not match constructor"
            );
        }
    }

    /**
     * 对于任意的{@code params} in {@code expectedConstructor},
     * 都存在{@code param} in {@code constructorHave},
     * 使得{@code expectedConstructor} >= {@code constructorHave}
     */
    private void matchConstructors(
            List<CallableSignature> constructorsHave, RelationUsing from, File fromFile) {
        List<RelatedLocalParameterizedType[]> expectedConstructors = to.getConstructors();
        int[] map = new int[expectedConstructors.size()];
        for (int i = 0; i < expectedConstructors.size(); i++) {
            RelatedLocalParameterizedType[] expectedConstructor = expectedConstructors.get(i);
            boolean success = false;
            for (int j = 0; j < constructorsHave.size(); j++) {
                CallableSignature constructorHave = constructorsHave.get(j);
                success = CanNotAssignUtil.catchCanNotAssign(
                        () -> this.matchConstructor(expectedConstructor, constructorHave, fromFile), null);
                if (success) {
                    // 就是一个index了
                    map[i] = j;
                    break;
                }
            }
            if (!success) {
                String message = Arrays.stream(expectedConstructor)
                        .map(rt -> rt.toString(r -> r.toString(relatedGenericDefineCache)))
                        .collect(Collectors.joining(","));
                throw CanNotAssignUtil.canNotAssign(to.getDefineFile(), to.getPosition(),
                        "can not match constructor: " + message
                );
            }
        }
        from.setConstructorReferenceIfNeeded(map);
    }

    private void matchConstructor(
            RelatedLocalParameterizedType[] expectedConstructor, CallableSignature constructorHave, File fromFile) {
        // TODO 考虑默认啊. 类型啊,
        CallableSignature signatureOnDefineConstructor = new DefaultCallableSignature(new RelatedGenericDefine[0],
                expectedConstructor, false
        );
        CallableSignatureMatcher matcher = assignableFactory.getCallableSignatureMatcher();
        // 这个match它正经吗?
        // 就是说, 怎么样子的函数是可以进行的要求更低是吧...
        int index = matcher.match(signatureOnDefineConstructor, constructorHave);
        if (index < 0) {
            return;
        }
        throw CanNotAssignUtil.canNotAssign(fromFile,
                constructorHave.getParam(index).getType().getRawType().getPosition(),
                "constructor param not matched at index of: " + index
        );
    }


    @Override
    public void assign(RelatedLocalParameterizedType from) {
        assign(from.getType());
    }

    /**
     * lower<=default<=任意Upper
     * <pre>{@code
     *  if(default < lower):
     *      throw ERROR
     *  for upper in uppers:
     *      if(!(lower < upper)):
     *          throw ERROR
     *      if(!(default < upper)):
     *          throw ERROR
     * }</pre>
     */
    @Override
    public void selfConsistent() {

    }
}
