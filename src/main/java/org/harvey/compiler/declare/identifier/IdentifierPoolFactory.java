package org.harvey.compiler.declare.identifier;

import lombok.Getter;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.analysis.DetailedDeclarationType;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.calculate.Operators;
import org.harvey.compiler.execute.expression.*;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.*;

/**
 * 1. using, type, generic中对于引用的解析, 依据当前环境, 获取期望的声明
 * - 此时Permission的检查(仅限于文件内)
 * 2. identifier注册时保证不与import重叠
 * - 特别的, 如果import本质上指向的就是当前文件的一部分, 且指向正确, 则无视这条import
 * 3. 不同类型重名
 * - alias全路径和类名全路径不得重名
 * - 除了构造器的callable与类名重名了, 由于构造器的调用主要看new, 所以可以和类名重名
 * - callable和field重名了, 由于value可以重载运算符, 所以不允许和callable重名
 * - method和function重名了, 设置限定file, 表示当前文件级别的
 * - 局部变量和callable重名, 一律优先看作局部变量, 如果要表示callable, 可用file/this
 * 3. 同类型之间, callable可以重名, Class内全路径不可重名
 * callable  不得和field重名
 * inner_class 不得和field重名
 * alias 不得和field重名
 * field 不可和同级所有成员重名
 * <p>
 * 除了add能查询, 其余不能查询
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-25 20:02
 */
public class IdentifierPoolFactory {
    public static final String MEMBER = String.valueOf(SourceFileConstant.PACKAGE_SEPARATOR);
    public static final String GENERIC_LIST_POST_NAME = Operator.GENERIC_LIST_POST.getName();
    public static final String GENERIC_LIST_PRE_NAME = Operator.GENERIC_LIST_PRE.getName();
    private final Set<Integer> structure;
    private final Set<Integer> fields;
    private final Set<Integer> callables;
    private final Set<String> genericDefinitions;
    @Getter
    private final String outerPre;
    @Getter
    private final List<IdentifierString> declaredIdentifierPool;
    private final Map<String, Integer> declaredIdentifierMap;
    @Getter
    private final int preLength;
    private String outer = null;

    public IdentifierPoolFactory(String filePathPre) {
        this.outerPre = filePathPre;
        preLength = StringUtil.count(outerPre, SourceFileConstant.GET_MEMBER) - 1;
        structure = new HashSet<>();
        fields = new HashSet<>();
        callables = new HashSet<>();
        genericDefinitions = new HashSet<>();
        declaredIdentifierPool = new ArrayList<>();
        declaredIdentifierMap = new HashMap<>();
    }

    private IdentifierPoolFactory(IdentifierPoolFactory outerFactory, String outerPathPre) {
        this.outerPre = outerPathPre;
        preLength = StringUtil.count(outerPre, SourceFileConstant.GET_MEMBER) - 1;
        structure = new HashSet<>(outerFactory.structure);
        fields = new HashSet<>(outerFactory.fields);
        callables = new HashSet<>(outerFactory.callables);
        genericDefinitions = new HashSet<>(outerFactory.genericDefinitions);
        declaredIdentifierPool = new ArrayList<>(outerFactory.declaredIdentifierPool);
        declaredIdentifierMap = new HashMap<>(outerFactory.declaredIdentifierMap);
    }



    /**
     * @return 新类型的identifier reference, clone出来的factory
     */
    public IdentifierPoolFactory cloneForInner(ReferenceElement thisReference) {
        return new IdentifierPoolFactory(
                this, this.declaredIdentifierPool.get(thisReference.getReference()).getValue() + MEMBER);
    }


    /**
     * @param type FIELD CALLABLE STRUCTURE
     */
    public ReferenceElement add(DetailedDeclarationType type, String identifier, SourcePosition position) {
        if (type == DetailedDeclarationType.GENERIC_DEFINITION) {
            throw new CompilerException("generic define should use method of 'addStructureGeneric'");
        }
        String identifierToAdd = outerPre + identifier;
        int reference = indexOf(identifierToAdd);
        return testRepeatedAndReAdd(type, identifierToAdd, position, reference);

    }

    /**
     * @param type FIELD CALLABLE STRUCTURE
     */
    private ReferenceElement testRepeatedAndReAdd(
            DetailedDeclarationType type, String identifierToAdd,
            SourcePosition position, int reference) {
        switch (type) {
            case FIELD:
                // 不在identifierPool内
                boolean newIdentifier = reference == -1;
                if (!newIdentifier) {
                    throw new AnalysisExpressionException(
                            position, "Identifier already exist at" + positionAt(reference));
                } else {
                    reference = add(position, identifierToAdd);
                    fields.add(reference);
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case CALLABLE:
                // 可以在CALLABLE内
                if (reference == -1) {
                    reference = add(position, identifierToAdd);
                    callables.add(reference);
                    return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
                }
                callables.add(reference);
                // identifier 不在fields内
                if (fields.contains(reference)) {
                    throw new AnalysisExpressionException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case STRUCTURE:
                // 不在STRUCTURE内
                if (reference == -1) {
                    reference = add(position, identifierToAdd);
                    structure.add(reference);
                    return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
                }
                if (structure.contains(reference)) {
                    throw new AnalysisExpressionException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                structure.add(reference);
                // identifier 不在fields内
                if (fields.contains(reference)) {
                    throw new AnalysisExpressionException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case GENERIC_DEFINITION:
                throw new CompilerException("generic define should use method of 'addStructureGeneric'");
            default:
                throw new CompilerException("not support type");
        }

    }


    private int add(SourcePosition position, String identifierToAdd) {
        this.declaredIdentifierPool.add(new IdentifierString(position, identifierToAdd));
        int reference = this.declaredIdentifierPool.size() - 1;
        declaredIdentifierMap.put(identifierToAdd, reference);
        return reference;
    }

    private SourcePosition positionAt(int reference) {
        return this.declaredIdentifierPool.get(reference).getPosition();
    }

    private int indexOf(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new CompilerException("identifier can not be empty");
        }

        Integer reference = declaredIdentifierMap.get(identifier);
        return reference == null ? -1 : reference;
    }

    /**
     * @param referenceStack 索引从前到后, 结构从里到外,
     *                       索引0表示泛型定义的位置的identifierReference
     *                       到第一个static的structure, 或者文件级为止(有且仅仅有最后一个是static或文件级)
     *                       如果本身是static, 那么size只有1
     */
    public ReferenceElement addStructureGeneric(
            Stack<ReferenceElement> referenceStack, String genericName,
            SourcePosition position) {
        if (referenceStack.isEmpty()) {
            throw new CompilerException("outerStructuresList can not be empty", new IllegalArgumentException());
        }
        String identifierToAdd = genericIdentifier(genericName, referenceStack.peek());
        int reference = indexOf(identifierToAdd);
        reference = resetGenericReference(reference, genericName, identifierToAdd, position, referenceStack);
        return new ReferenceElement(position, ReferenceType.GENERIC_IDENTIFIER, reference);
    }

    private String genericIdentifier(String genericName, ReferenceElement outerReference) {
        return declaredIdentifierPool.get(outerReference.getReference()).getValue() + MEMBER + GENERIC_LIST_PRE_NAME +
               genericName + GENERIC_LIST_POST_NAME;
    }

    private int resetGenericReference(
            int genericReference, String genericName, String identifierToAdd, SourcePosition position,
            Stack<ReferenceElement> referenceStack) {
        if (genericReference != -1) {
            // 同级重复
            throw new AnalysisExpressionException(position, "declared in pre generic definition in this structure");
        }
        genericReference = add(position, identifierToAdd);
        testGenericReference(genericName, position, referenceStack);
        // 没有冲突, 加入缓存
        genericDefinitions.add(declaredIdentifierPool.get(genericReference).getValue());
        return genericReference;
    }

    public void testGenericReference(
            String genericName,
            SourcePosition position,
            Stack<ReferenceElement> referenceStack) {
        Stack<ReferenceElement> referenceStackVar = CollectionUtil.cloneStack(referenceStack);
        // 不在STRUCTURE内
        // 例如class的内部类, identifier会在外部类存一份, 本类类名会在本类文件存一份
        // 泛型类型由于只会在本类中出现
        // 如果内部类要访问外部类的泛型, 该怎么办呢?
        // generic应该以什么形式存在于pool?
        // 加载静态内部类的时候不会加载外部类, 怎么办?
        // 对于泛型, 由于static的内部类不能访问外部泛型
        // 而对于非static的内部类, 能访问泛型, 但一定要能优先加载外部类
        // 那么, 以下是对非static内部类如何存储Generic泛型的思考, OuterReference<GenericDefinitionIndex>
        // 非静态类的IdentifierReference, 要不停地往外找, 直到找到一个static修饰的类型
        // 这是因为一个类是static的了, 而generic define是非static的, generic 无法影响static的类的内部元素
        while (!referenceStackVar.empty()) {
            // 前面的重复
            ReferenceElement outerReference = referenceStackVar.pop();
            String key = genericIdentifier(genericName, outerReference);
            if (genericDefinitions.contains(key)) {
                throw new AnalysisExpressionException(
                        position,
                        "declared in pre generic definition in outer structure of: " +
                        declaredIdentifierPool.get(outerReference.getReference())
                );
            }
        }
        // Generic的泛型定义不能在泛型定义内有重复
        // 不能和外界的T有重合
        // 可以接受和同级的同名static类型的重合, 因为泛型类型不允许用DOT来获取, 而类型可以
    }

    public ReferenceElement normalMethodReference(SourceString identifier) {
        return suitableCallableIdentifier(identifier, SourceType.IDENTIFIER, SourceType.OPERATOR);
    }

    /**
     * @param identifier can not be null
     */
    public ReferenceElement functionIdentifier(SourceString identifier) {
        return suitableCallableIdentifier(identifier, SourceType.IDENTIFIER);
    }

    /**
     * @param suitableType can't be null
     */
    private ReferenceElement suitableCallableIdentifier(SourceString identifier, SourceType... suitableType) {
        if (identifier == null) {
            throw new CompilerException("identifier can not be null", new IllegalArgumentException());
        }
        SourceType type = identifier.getType();
        SourcePosition position = identifier.getPosition();
        if (!ArrayUtil.contains(suitableType, type)) {
            throw new AnalysisExpressionException(position, "the identifier is:" + type + " is not suitable at file.");
        }
        String value = identifier.getValue();
        if (type == SourceType.OPERATOR) {
            Operator oper = Operators.reloadableOperator(value);
            if (oper == null) {
                throw new CompilerException("Unknown operator");
            }
            return ReferenceElement.of(new OperatorString(position, oper));
        }
        if (suitableType == null) {
            throw new CompilerException("suitable type can not be null");
        }
        return add(DetailedDeclarationType.CALLABLE, value, position);
    }

    /**
     * 构造器 or cast
     * 如果是和外部类的类名一致, 认为是构造器, 否则, 不管是否合法, 认为是cast, 然后返回
     */
    public ReferenceElement noIdentifierMethodReference(SourceTextContext type) {
        // 构造器 or cast
        if (type.isEmpty()) {
            throw new CompilerException("expected type");
        }
        SourcePosition position = type.get(0).getPosition();
        if (type.size() == 1) {
            SourceString typeString = type.get(0);
            String structureName = getSimpleNameFromOuter();
            if (typeString.getType() == SourceType.IDENTIFIER && structureName.equals(typeString.getValue())) {
                //  构造器
                // 返回值的rawType和simpleStructureName一致
                return ReferenceElement.ofConstructor(position);
            } else {
                return ReferenceElement.ofCast(position);
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean expectedDot = false;
        for (SourceString sourceString : type) {
            if (expectedDot) {
                if (sourceString.getType() != SourceType.OPERATOR ||
                    !Operator.GET_MEMBER.nameEquals(sourceString.getValue())) {
                    // 不符合构造器
                    return ReferenceElement.ofCast(position);
                }
                sb.append(Operator.GET_MEMBER.getName());
            } else {
                if (sourceString.getType() != SourceType.IDENTIFIER) {
                    // 不符合构造器
                    return ReferenceElement.ofCast(position);
                }
                sb.append(sourceString.getValue());
            }
            expectedDot = !expectedDot;
        }
        // 如果从根目录来的呢?
        return getOuter().contentEquals(sb) ? ReferenceElement.ofConstructor(position) :
                ReferenceElement.ofCast(position);
    }

    private String getOuter() {
        if (this.outer == null) {
            this.outer = outerPre.substring(0, outerPre.length() - MEMBER.length());/*最后一位一定是MEMBER*/
        }
        return this.outer;
    }

    private String getSimpleNameFromOuter() {
        int i = getOuter().lastIndexOf(MEMBER);
        return i >= 0 ? outerPre.substring(i + 1) : outerPre;
    }


}

