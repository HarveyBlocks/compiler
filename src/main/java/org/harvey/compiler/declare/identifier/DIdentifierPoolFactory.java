package org.harvey.compiler.declare.identifier;

import lombok.Getter;
import org.harvey.compiler.common.constant.SourceFileConstant;
import org.harvey.compiler.common.util.StringUtil;
import org.harvey.compiler.declare.analysis.DetailedDeclarationType;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourcePosition;

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
 * <p>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-25 20:02
 */
@Deprecated
public class DIdentifierPoolFactory {
    public static final String MEMBER = String.valueOf(SourceFileConstant.PACKAGE_SEPARATOR);
    private final Set<Integer> structure;
    private final Set<Integer> fields;
    private final Set<Integer> callables;
    @Getter
    private final String outerPre;
    @Getter
    private final List<IdentifierString> declaredIdentifierPool;
    private final Map<String, Integer> declaredIdentifierMap;
    @Getter
    private final int preLength;
    private String outer = null;


    public DIdentifierPoolFactory(String filePathPre) {
        this.outerPre = filePathPre;
        preLength = StringUtil.count(outerPre, SourceFileConstant.GET_MEMBER) - 1;
        structure = new HashSet<>();
        fields = new HashSet<>();
        callables = new HashSet<>();
        declaredIdentifierPool = new ArrayList<>();
        declaredIdentifierMap = new HashMap<>();
    }

    private DIdentifierPoolFactory(
            DIdentifierPoolFactory outerFactory, String outerPathPre) {
        this.outerPre = outerPathPre;
        preLength = StringUtil.count(outerPre, SourceFileConstant.GET_MEMBER) - 1;
        structure = new HashSet<>(outerFactory.structure);
        fields = new HashSet<>(outerFactory.fields);
        callables = new HashSet<>(outerFactory.callables);
        declaredIdentifierPool = new ArrayList<>(outerFactory.declaredIdentifierPool);
        declaredIdentifierMap = new HashMap<>(outerFactory.declaredIdentifierMap);

    }


    /**
     * @return 新类型的identifier reference, clone出来的factory
     */
    public DIdentifierPoolFactory cloneForInner(ReferenceElement thisReference) {
        return new DIdentifierPoolFactory(
                this, this.declaredIdentifierPool.get(thisReference.getReference()).getValue() + MEMBER
        );
    }


    /**
     * @param type FIELD CALLABLE STRUCTURE
     */
    public ReferenceElement addIdentifier(DetailedDeclarationType type, String identifier, SourcePosition position) {
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
            DetailedDeclarationType type, String identifierToAdd, SourcePosition position, int reference) {
        switch (type) {
            case FIELD:
                // 不在identifierPool内
                boolean newIdentifier = reference == -1;
                if (!newIdentifier) {
                    throw new AnalysisDeclareException(
                            position, "Identifier already exist at" + positionAt(reference));
                } else {
                    reference = addIdentifier(position, identifierToAdd);
                    fields.add(reference);
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case CALLABLE:
                // 可以在CALLABLE内
                if (reference == -1) {
                    reference = addIdentifier(position, identifierToAdd);
                    callables.add(reference);
                    return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
                }
                callables.add(reference);
                // identifier 不在fields内
                if (fields.contains(reference)) {
                    throw new AnalysisDeclareException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case STRUCTURE:
                // 不在STRUCTURE内
                if (reference == -1) {
                    reference = addIdentifier(position, identifierToAdd);
                    structure.add(reference);
                    return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
                }
                if (structure.contains(reference)) {
                    throw new AnalysisDeclareException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                structure.add(reference);
                // identifier 不在fields内
                if (fields.contains(reference)) {
                    throw new AnalysisDeclareException(
                            position, "Identifier already exist at" + positionAt(reference));
                }
                return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
            case GENERIC_DEFINITION:
                throw new CompilerException("generic define won't check");
            default:
                throw new CompilerException("not support type");
        }

    }


    private int addIdentifier(SourcePosition position, String identifierToAdd) {
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


    public String getOuter() {
        // outer pre 去最后一维get member
        if (this.outer == null) {
            this.outer = outerPre.substring(0, outerPre.length() - MEMBER.length());/*最后一位一定是MEMBER*/
        }
        return this.outer;
    }

    public String getSimpleNameFromOuter() {
        // outer选中最后一位
        int i = getOuter().lastIndexOf(MEMBER);
        return i >= 0 ? outerPre.substring(i + 1) : outerPre;
    }

    public ReferenceElement operatorCallable(SourcePosition position, Operator operator) {
        return ReferenceElement.of(new NormalOperatorString(position, operator));
    }
}

