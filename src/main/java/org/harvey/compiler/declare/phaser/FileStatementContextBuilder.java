package org.harvey.compiler.declare.phaser;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.calculate.Operators;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.core.AccessControls;
import org.harvey.compiler.analysis.core.Permission;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.EnumConstantDeclarable;
import org.harvey.compiler.declare.context.*;
import org.harvey.compiler.declare.phaser.phaser.CallablePhaser;
import org.harvey.compiler.declare.phaser.phaser.StructurePhaser;
import org.harvey.compiler.declare.phaser.phaser.ValuePhaser;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.depart.DeclaredDepartedPart;
import org.harvey.compiler.depart.RecursivelyDepartedBody;
import org.harvey.compiler.depart.SimpleComplexStructure;
import org.harvey.compiler.depart.SourceTypeAlias;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.ExpressionFactory;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceStringType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-26 18:08
 */
@Getter
public class FileStatementContextBuilder {
    public static final String INNER_STRUCTURE = ".";
    public static final String MEMBER = "#";
    public static final int NOT_HAVE_IDENTIFIER = -1;
    public static final int CONSTRUCTOR = -2;
    public static final int CAST_OPERATOR = -3;
    public static final ValuePhaser VALUE_PHASER = ValuePhaser.instance();
    public static final CallablePhaser CALLABLE_PHASER = CallablePhaser.instance();
    public static final StructurePhaser STRUCTURE_PHASER = StructurePhaser.instance();
    private final FileContext context = new FileContext();

    public FileStatementContextBuilder() {
    }

    public static int operToId(Operator oper) {
        return -oper.ordinal() - 10;
    }

    public static Operator idToOper(int id) {
        int index = -id - 10;
        Operator[] values = Operator.values();
        if (id == NOT_HAVE_IDENTIFIER || index < 0 || index >= values.length) {
            return null;
        }
        return values[index];
    }

    private static Operator reloadableOperator(String operator) {
        Operator[] operators = Operators.get(operator);
        if (operators.length == 0) {
            throw new CompilerException("Unknown operator");
        } else if (operators.length == 1) {
            return operators[0];
        }
        for (Operator oper : operators) {
            if (oper.getPriority() > 10) {
                return oper;
            }
        }
        throw new CompilerException("Unknown operator");
    }

    private static ArrayList<FieldInitialization> departDeclare(Declarable field) {
        SourceTextContext assign = field.getAttachment();
        assign.addFirst(field.getIdentifier());
        return ExpressionFactory.depart(assign).splitWithComma(FileStatementContextBuilder::checkInit);
    }

    private static FieldInitialization checkInit(Expression part, SourcePosition position) {
        if (part.isEmpty()) {
            throw new AnalysisExpressionException(position, "expected an identifier");
        }
        ExpressionElement identifier = part.get(0);
        if (!(identifier instanceof IdentifierString)) {
            throw new AnalysisExpressionException(identifier.getPosition(), "expected an identifier");
        }
        return new FieldInitialization((IdentifierString) identifier, part);
    }

    /**
     * 也就是说, 这个方法里的任务就是解析了访问控制和修饰
     * 解析identifier, 不能有重复的, 同级同类(cs和cs, var和var, call和call)之间不能重复
     * 本identifier不能和上级重复
     * 修饰, 对于abstract, 检查其内部方法都没有方法体吧
     */
    public FileContext build(RecursivelyDepartedBody body) {
        context.addImport(body.getImportTable());
        context.addAliases(alasMap(body.getAliasList(), Environment.FILE, -1));
        dealFunction(body.getCallableList());
        dealStructure(body.getSimpleComplexStructureList());
        checkContextIdentifier();
        // 泛型, 什么时候解析?
        // 泛型, 什么时候判断正误?
        // 1. 注册类名和方法名, 可以区分什么是泛型, 什么是大于小于
        // 2. 然后就可以把连起来的变量声明(var a,b=2;)分开
        // 3. 也可也解析所有的信息了, 可以解析函数参数, 可以解析返回值, 可以解析泛型参数列表
        // 4. 但是不能解析泛型参数列表的定义还可能涉及两个类循环依赖, 如果A类定义泛型用到了B类, 那么A类需要B类的类定义信息
        // 5. 如果泛型涉及了super 和 extends, 那么就需要知道各个类的继承关系, 要获取到整个继承链(糟糕qwq)
        // 6. A super T extends C, 如果A和C之间间隔了好几个类, 就需要读好几个文件, 非常糟糕
        // 7. 继承链是一条, 如果加上实现, 那就更多了qwq咋办啊
        //
        // 1. 从A开始找, 广度优先, 找到C, 保留到C的一条链
        //      or 获取整继承树上的声明链
        // 第一阶段: 修饰是可以判断的, 访问控制是可以判断的
        // 第二阶段: 类型的声明方式是可以判断的, 类型的声明逻辑是不可判断的
        //         表达式是可以解析的, 表达式的自动的类型转换是不可以做的
        // 第三阶段: 可以判断声明类型的逻辑(继承的偏序关系)是否正确
        //          可以进行类型自动转换的检查和判断()不好做, 全文都要检查, 全文都要反复查找,
        //          继承啊继承, 继承怎么知道对与否?
        //          A B 类, 引入之后就要判断是否是继承关系, 是的话就注册, 然后先从注册里访问吧
        // 也就是说, 这个方法里的任务就是解析了访问控制和修饰
        // 解析identifier, 不能有重复的, 同级同类(cs和cs, var和var, call和call)之间不能重复
        // 本identifier不能和上级重复
        // 修饰, 对于abstract, 检查其内部方法都没有方法体吧
        // 啊啊

        return context;
    }

    private void checkContextIdentifier() {
        // 检查param list
        // 检查generic list (super, extends, 不行不行... 因为有些类型是依据.获取的, 不能用那个啥获取...)
        // 检查return type
        // 检查extends
        // 检查 implements list
        // 不可不可...
    }

    private void dealStructure(List<SimpleComplexStructure> structureList) {
        if (structureList == null || structureList.isEmpty()) {
            return;
        }
        for (SimpleComplexStructure structure : structureList) {
            ComplexStructureContext outer;
            if (structure.getOuterStructure() >= 0) {
                outer = context.getComplexStructure(structure.getOuterStructure());
            } else {
                outer = null;
            }
            context.addStructure(dealStructure(structure, outer));
        }
    }

    private ComplexStructureContext dealStructure(SimpleComplexStructure structure, ComplexStructureContext outer) {
        Declarable declarable = structure.getDeclarable();
        SourceString identifier;
        if (outer == null) {
            identifier = completeMemberIdentifier(structure);
        } else {
            identifier = completeMemberIdentifier(context.getIdentifier(outer.getIdentifierReference()),
                    declarable.getIdentifier());
        }
        int index = suitableIdentifier(identifier, false, SourceStringType.IDENTIFIER);

        Environment outerEnvironment = outer == null ? Environment.FILE : outer.getEnvironment();
        ComplexStructureContext csc = STRUCTURE_PHASER.phase(declarable, index, outerEnvironment);
        LinkedList<EnumConstantDeclarable> enumConstantDeclarableList = structure.getEnumConstantDeclarableList();
        if (enumConstantDeclarableList != null && !enumConstantDeclarableList.isEmpty()) {
            if (csc.getType() == StructureType.ENUM) {
                csc.addEnumConstantList(
                        enumConstantDeclarableList.stream().map(ComplexStructureContext.EnumConstant::new)
                                .collect(Collectors.toList()));
            } else {
                throw new CompilerException("only enum can have enum constant declare list");
            }
        }
        csc.addTypeAlias(alasMap(structure.getSourceTypeAliaseList(), csc.getEnvironment(), index));
        csc.addBlocks(structure.getBlocks().stream().map(csc::executableBodyDepart).collect(Collectors.toList()));
        csc.addStaticBlocks(
                structure.getStaticBlocks().stream().map(csc::executableBodyDepart).collect(Collectors.toList()));
        csc.addInnerStructure(structure.getInternalStructureReferenceList());
        csc.setOuterStructure(structure.getOuterStructure());
        csc.addField(fieldMap(structure.getFieldTable(), csc));
        csc.addMethod(callableMap(structure.getMethodTable(), csc));
        return csc;
    }

    private List<TypeAlias> alasMap(List<SourceTypeAlias> typeAliases, Environment environment,
                                    int outerIdentifierReference) {

        Permission defaultPermission = AccessControls.getDefaultPermission(environment);
        Function<SourceTextContext, AccessControl> accessControlFunction;
        if (environment == Environment.FILE) {
            accessControlFunction = c -> AccessControls.buildFileAccessControl(c, "type alias", defaultPermission);
        } else {
            accessControlFunction = c -> AccessControls.buildMemberAccessControl(SourceTextContext.EMPTY,
                    defaultPermission);
        }

        return typeAliases.stream().map(t -> {
            AccessControl accessControl = accessControlFunction.apply(t.getPermissions());
            ListIterator<SourceString> iterator = t.getAlias().listIterator();
            if (!iterator.hasNext()) {
                throw new CompilerException("alias can not be empty");
            }
            SourceString first = iterator.next();
            if (outerIdentifierReference >= 0) {
                first = completeMemberIdentifier(context.getIdentifier(outerIdentifierReference), first);
            }
            int reference = suitableIdentifier(first, false, SourceStringType.IDENTIFIER);
            Expression genericMessage = iterator.hasNext() ? ExpressionFactory.genericMessage(iterator) : null;
            Expression origin = ExpressionFactory.type(t.getOrigin());
            return new TypeAlias(accessControl, reference, genericMessage, origin);
        }).collect(Collectors.toList());
    }

    private List<ValueContext> fieldMap(List<Declarable> fieldTable, ComplexStructureContext outer) {
        String outerIdentifier = context.getIdentifier(outer.getOuterReference());
        Environment environment = outer.getEnvironment();
        List<ValueContext> result = new ArrayList<>();
        for (Declarable field : fieldTable) {
            ValueContext origin = VALUE_PHASER.phase(field, -1, environment);
            for (FieldInitialization initialization : departDeclare(field)) {
                IdentifierString identifierString = completeMemberIdentifier(outerIdentifier,
                        initialization.identifier);
                int fieldReference = suitableIdentifier(identifierString.getValue(), identifierString.getPosition(),
                        false);
                origin.putAssign(fieldReference, initialization.assign);
            }
            result.add(origin);
        }
        return result;
    }

    private List<CallableContext> callableMap(List<DeclaredDepartedPart> methodTable, ComplexStructureContext outer) {
        Environment environment = outer.getEnvironment();
        String outerIdentifier = context.getIdentifier(outer.getOuterReference());
        String simpleName = getSimpleName(outerIdentifier);
        return methodTable.stream().map(m -> {
            Declarable statement = m.getStatement();
            SourceString identifier = statement.getIdentifier();
            int methodReference = getMethodReference(identifier, statement, simpleName, outerIdentifier);
            CallableContext cc = CALLABLE_PHASER.phase(statement, methodReference, environment);
            cc.setBody(outer.executableBodyDepart(m.getBody()), statement.getAttachment().getLast().getPosition());
            return cc;
        }).collect(Collectors.toList());
    }

    private int getMethodReference(SourceString identifier, Declarable statement, String simpleName,
                                   String outerIdentifier) {
        if (identifier != null) {
            return suitableIdentifier(completeMemberIdentifier(outerIdentifier, identifier), true,
                    SourceStringType.IDENTIFIER, SourceStringType.OPERATOR);
        }
        SourceTextContext type = statement.getType();
        // 构造器 or cast
        if (type.size() == 1) {
            SourceString sourceString = type.get(0);
            if (sourceString.getType() == SourceStringType.IDENTIFIER && simpleName.equals(sourceString.getValue())) {
                //  构造器
                return CONSTRUCTOR;
            } else {
                return CAST_OPERATOR;
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean expectedDot = false;
        for (SourceString sourceString : type) {
            if (expectedDot) {
                if (sourceString.getType() != SourceStringType.OPERATOR ||
                        !Operator.GET_MEMBER.nameEquals(sourceString.getValue())) {
                    return CAST_OPERATOR;
                }
                sb.append(Operator.GET_MEMBER.getName());
            } else {
                if (sourceString.getType() != SourceStringType.IDENTIFIER) {
                    return CAST_OPERATOR;
                }
                sb.append(sourceString.getValue());
            }
            expectedDot = !expectedDot;
        }
        return outerIdentifier.contentEquals(sb) ? CONSTRUCTOR : CAST_OPERATOR;

    }

    private String getSimpleName(String identifier) {
        int i = identifier.lastIndexOf(".");
        return i >= 0 ? identifier.substring(i + 1) : identifier;
    }

    private IdentifierString completeMemberIdentifier(String structureIdentifier, IdentifierString memberIdentifier) {
        if (structureIdentifier == null) {
            return null;
        }
        return new IdentifierString(memberIdentifier.getPosition(),
                structureIdentifier + MEMBER + memberIdentifier.getValue());
    }

    private SourceString completeMemberIdentifier(String structureIdentifier, SourceString memberIdentifier) {
        if (structureIdentifier == null) {
            return null;
        }
        return new SourceString(memberIdentifier.getType(), structureIdentifier + MEMBER + memberIdentifier.getValue(),
                memberIdentifier.getPosition());
    }

    /**
     * 补充标识符, 主要是, 全类名之类, 前面的前缀
     */
    private SourceString completeMemberIdentifier(SimpleComplexStructure structure) {
        SourceString identifier = structure.getDeclarable().getIdentifier();
        if (!structure.hasOuter()) {
            return identifier;
        }
        int outer = structure.getOuterStructure();
        int outerIdentifierReference = context.getComplexStructure(outer).getIdentifierReference();
        String outerIdentifier = context.getIdentifier(outerIdentifierReference);
        return completeMemberIdentifier(outerIdentifier, identifier);
    }

    private void dealFunction(List<DeclaredDepartedPart> callableList) {
        if (callableList == null || callableList.isEmpty()) {
            return;
        }
        for (DeclaredDepartedPart callable : callableList) {
            Declarable statement = callable.getStatement();
            int index = callableIdentifier(statement.getIdentifier(), statement.getType());
            CallableContext funcContext = CALLABLE_PHASER.phase(statement, index, Environment.FILE);
            funcContext.setBody(context.executableBodyDepart(callable.getBody()),
                    statement.getAttachment().getLast().getPosition());
            context.addFunction(funcContext);
        }
    }

    private int callableIdentifier(SourceString identifier, SourceTextContext type) {
        return identifier == null ? CAST_OPERATOR : suitableIdentifier(identifier, true, SourceStringType.IDENTIFIER);
    }

    private int suitableIdentifier(String identifier, SourcePosition position, boolean allowedRepeat) {
        int reference = context.addIdentifier(identifier);
        if (!allowedRepeat) {
            assertNewIdentifier(reference, position);
        }
        return reference;
    }

    /**
     * @param suitableType can't be null
     */
    private int suitableIdentifier(SourceString identifier, boolean allowedRepeat, SourceStringType... suitableType) {
        if (identifier == null) {
            return NOT_HAVE_IDENTIFIER;
        }
        if (identifier.getType() == SourceStringType.OPERATOR) {
            return operToId(reloadableOperator(identifier.getValue()));
        }
        /*if (identifier.getType() != SourceStringType.IDENTIFIER) {
            throw new AnalysisExpressionException(identifier.getPosition(),
                    "Source string type of " + identifier.getValue() + "is illegal");
        }*/
        if (suitableType == null) {
            throw new CompilerException("suitable type can not be null");
        }
        if (!ArrayUtil.contains(suitableType, identifier.getType())) {
            throw new AnalysisExpressionException(identifier.getPosition(),
                    "the identifier is:" + identifier.getType() + " is not suitable at file.");
        }
        return suitableIdentifier(identifier.getValue(), identifier.getPosition(), allowedRepeat);
    }

    private void assertNewIdentifier(int reference, SourcePosition position) {
        int expectedIndex = context.getIdentifierTable().size() - 1;
        if (reference != expectedIndex) {
            // 出现重复的identifier
            throw new AnalysisExpressionException(position, "identifier repeated");
        }
    }

    @AllArgsConstructor
    private static class FieldInitialization {
        IdentifierString identifier;
        Expression assign;
    }


}
