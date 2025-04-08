package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.*;
import org.harvey.compiler.declare.context.CallableType;
import org.harvey.compiler.declare.identifier.CallableIdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierPoolFactory;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.KeywordString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;

import java.util.*;

/**
 * callable 不需要identifier manager, 但是需要对generic的信息进行存储, callable的函数名可以一致,
 * 但是参数和函数独立, 没有很好的分辨函数签名的方法
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:06
 */
@AllArgsConstructor
@Getter
public class CallableDefinition implements Definition {
    private final AccessControl permissions;
    private final Embellish embellish;
    private final CallableType type;
    private final List<LocalTypeDefinition> returnTypes;
    private final ReferenceElement identifierReference;
    private final List<Pair<IdentifierString, SourceTextContext>> genericDefines;
    private final Map<String, Integer> genericMap;
    private final List<ParamDefinition> paramLists;
    private final List<LocalTypeDefinition> throwsTypes;
    private final SourceTextContext body;

    public final CallableIdentifierManager wrap(IdentifierManager origin) {
        // 如果用这种方法获取的Generic, 怎么直到是方法内的Generic呢, 还是方法外的Generic呢?
        // 获取到一个Generic,
        return new CallableIdentifierManager(genericMap, origin);
    }

    public static class Builder {
        private final IdentifierPoolFactory identifierPoolFactory;
        private final Environment environment;

        private AccessControl permission;
        private Embellish embellish;
        private CallableType type;
        private List<LocalTypeDefinition> returnTypes;
        private ReferenceElement identifierReference = null;

        private List<Pair<IdentifierString, SourceTextContext>> genericDefine;
        private List<ParamDefinition> paramLists;
        private List<LocalTypeDefinition> throwsTypes;
        private SourceTextContext body;
        private Map<String, Integer> genericMap;


        public Builder(IdentifierPoolFactory identifierPoolFactory, Environment environment) {
            this.identifierPoolFactory = identifierPoolFactory;
            this.environment = environment;
        }

        private static DetailedDeclarationType getOnIllegal(CallableType type) {
            DetailedDeclarationType onIllegal;
            if (CallableType.FUNCTION == type) {
                onIllegal = DetailedDeclarationType.onIllegalInFile(1);
            } else if (CallableType.METHOD == type) {
                onIllegal = DetailedDeclarationType.onIllegalInStructure(2);
            } else if (CallableType.OPERATOR == type) {
                onIllegal = DetailedDeclarationType.onIllegalInStructure(3);
            } else {
                onIllegal = null;
            }
            return onIllegal;
        }

        private static List<LocalTypeDefinition> localTypesOnDeclare(ListIterator<SourceString> iterator) {
            List<LocalTypeDefinition> result = new ArrayList<>();
            while (iterator.hasNext()) {
                LocalTypeDefinition.Builder builder = new LocalTypeDefinition.Builder();
                LocalTypeDefinition eachThrows = builder.embellish(DeclarableFactory.getEmbellish(iterator))
                        .type(iterator)
                        .build();
                if (eachThrows.isMarkFinal()) {
                    throw new AnalysisExpressionException(eachThrows.getMarkConst(), "not allowed here");
                }
                if (Definition.skipIf(iterator, Operator.COMMA)) {
                    result.add(eachThrows);
                } else {
                    break;
                }
            }
            return result;
        }

        public Builder identifierReference(
                SourceString identifier,
                SourcePosition forEmpty,
                SourceTextContext returnType,
                SourcePosition positionForNoIdentifier) {
            return environment == Environment.FILE ? identifierReferenceAtFile(
                    identifier, forEmpty) : identifierReferenceForMember(
                    returnType, identifier, positionForNoIdentifier);
        }

        public Builder identifierReferenceAtFile(SourceString identifier, SourcePosition position) {
            if (identifier == null) {
                throw new AnalysisExpressionException(position, "expected identifier");
            } else {
                this.identifierReference = identifierPoolFactory.functionIdentifier(identifier);
            }

            this.type = CallableType.FUNCTION;
            return this;
        }

        public Builder identifierReferenceForMember(
                SourceTextContext returnType,
                SourceString identifier,
                SourcePosition positionForNoIdentifier) {
            if (identifier == null) {
                identifierReference = identifierPoolFactory.noIdentifierMethodReference(returnType);
            } else {
                identifierReference = identifierPoolFactory.normalMethodReference(identifier);
            }
            this.type = decideType(positionForNoIdentifier);
            return this;
        }

        private CallableType decideType(SourcePosition positionForNoIdentifier) {
            switch (identifierReference.getType()) {
                case IDENTIFIER:
                    return CallableType.METHOD;
                case CAST_OPERATOR:
                    return CallableType.CAST_OPERATOR;
                case CONSTRUCTOR:
                    if (environment == Environment.INTERFACE) {
                        throw new AnalysisExpressionException(
                                positionForNoIdentifier, "constructor is illegal in interface");
                    }
                    return CallableType.CONSTRUCTOR;
                case OPERATOR:
                    return CallableType.OPERATOR;
                default:
                    throw new CompilerException("unknown identifier reference : " + identifierReference);
            }
        }

        public Builder permission(SourceTextContext permissions) {
            Definition.notNullValid(embellish, "embellish");
            this.permission = AccessControls.buildAccessControl(environment, permissions);
            if (embellish.isMarkedStatic()) {
                return this;
            }
            if (embellish.isMarkedAbstract() && !permission.canChildrenClass()) {
                throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                        permissions.getLast().getPosition(), "permission is conflict with abstract"
                );
            }
            return this;
        }

        /**
         * @see Embellish#create(DetailedDeclarationType, DetailedDeclarationType, EmbellishSource, boolean, boolean)
         */
        public Builder embellish(EmbellishSource source) {
            Definition.notNullValid(identifierReference, "identifier reference");
            Definition.notNullValid(type, "type");
            if (CallableType.CONSTRUCTOR == this.type && !source.isNull()) {
                source.illegalOn("constructor", Embellish.EmbellishWord.values());
            }
            // 不可能是构造器
            boolean constForOperator = false;
            if (CallableType.CAST_OPERATOR == this.type) {
                constForOperator = true;
            } else if (identifierReference.getType() == ReferenceType.OPERATOR) {
                Operator operator = identifierReference.operator();
                if (operator == null) {
                    throw new CompilerException(identifierReference + " not a operator reference");
                }
                constForOperator = Embellish.readOnlyOperator(operator, source.getConstMark());
            }
            DetailedDeclarationType onDefault = DetailedDeclarationType.onDefaultForMember(
                    CallableType.METHOD == this.type || CallableType.OPERATOR == this.type ? 1 : -1, environment);
            embellish = Embellish.create(onDefault, getOnIllegal(this.type), source, true, constForOperator);
            return this;
        }

        public Builder returnTypes(SourceTextContext type, SourcePosition forEmpty) {
            // (type1,type2)
            if (type.isEmpty()) {
                throw new AnalysisExpressionException(forEmpty, "expect type");
            }

            boolean parenthesesPre = Operator.PARENTHESES_PRE.nameEquals(type.getFirst().getValue());
            boolean parenthesesPost = Operator.PARENTHESES_POST.nameEquals(type.getLast().getValue());
            if (parenthesesPre && parenthesesPost) {
                type.removeLast();
                type.removeFirst();
            } else if (parenthesesPre != parenthesesPost) {
                if (!parenthesesPre) {
                    throw new AnalysisExpressionException(type.getFirst().getPosition(), "expected (");
                } else {
                    throw new AnalysisExpressionException(type.getLast().getPosition(), "expected )");
                }
            } else if (this.type == CallableType.CAST_OPERATOR) {
                throw new AnalysisExpressionException(
                        type.getFirst().getPosition(),
                        "expected () circle return type when it is a cast operator overload"
                );
            }
            if (type.size() == 1) {
                SourceString mayVoid = type.get(0);
                if (mayVoid.getType() == SourceType.KEYWORD && Keyword.VOID.equals(mayVoid.getValue())) {
                    this.returnTypes = List.of(
                            new LocalTypeDefinition(null, null, new KeywordString(mayVoid.getPosition(), Keyword.VOID),
                                    SourceTextContext.empty()
                            ));
                    return this;
                }
            }
            ListIterator<SourceString> iterator = type.listIterator();
            this.returnTypes = localTypesOnDeclare(iterator);
            if (iterator.hasNext()) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "expected )");
            }
            return this;
        }

        public Builder genericDefine(
                ListIterator<SourceString> attachmentIterator,
                Stack<ReferenceElement> outerReferenceStack) {
            Definition.notNullValid(identifierReference, "identifier reference");
            Definition.notNullValid(embellish, "embellish");
            ListIterator<SourceString> genericDefineIterator = trueGenericDefineIterator(attachmentIterator);
            setGenericDefine(outerReferenceStack, genericDefineIterator);
            return this;
        }

        private ListIterator<SourceString> trueGenericDefineIterator(ListIterator<SourceString> attachmentIterator) {
            // 特别的, 思考cast. cast如果作为return type, 而return type的Generic
            if (type != CallableType.CONSTRUCTOR) {
                // 如果不是CONSTRUCTOR
                return attachmentIterator;
            }
            // 构造器, 其definition要从type来
            if (this.returnTypes == null) {
                throw new CompilerException("expected return type first for constructor's generic define");
            }
            if (this.returnTypes.size() != 1) {
                throw new CompilerException("constructor's return type's size must equals 1");
            }
            // 构造器, 所以return.get(0).getKey()一定是类名
            SourceTextContext genericDefine = this.returnTypes.get(0).getTypeParameter();
            this.returnTypes.clear();
            return genericDefine.listIterator();
        }

        private void setGenericDefine(
                Stack<ReferenceElement> outerReferenceStack,
                ListIterator<SourceString> genericDefineIterator) {
            if (!GenericFactory.genericPreCheck(genericDefineIterator)) {
                this.genericDefine = Collections.emptyList();
                this.genericMap = Collections.emptyMap();
                return;
            }
            List<Pair<IdentifierString, SourceTextContext>> pairs = GenericFactory.defineSourceDepart(
                    genericDefineIterator);
            this.genericMap = new HashMap<>();
            for (int i = 0; i < pairs.size(); i++) {
                Pair<IdentifierString, SourceTextContext> pair = pairs.get(i);
                // not repeated
                IdentifierString identifier = pair.getKey();
                String identifierValue = identifier.getValue();
                if (genericMap.containsKey(identifierValue)) {
                    // 本级不重复
                    throw new AnalysisExpressionException(
                            identifier.getPosition(), "repeated in this callable definition");
                }
                // 往后不重复
                identifierPoolFactory.testGenericReference(
                        identifierValue, identifier.getPosition(), outerReferenceStack);
                genericMap.put(identifierValue, i);
            }
            this.genericDefine = pairs;
        }

        public Builder paramList(ListIterator<SourceString> attachmentIterator) {
            // param list的几条性质特点:
            // 1. (开头, ) 结尾
            // 2. param-> [const] [final] type [...] identifier = default
            // 3. type-> keyword | rawType[<generic message>]
            // 4. identifier 只有一个单词 or ignore_identifier
            // 5. default, 表达式,
            SourceTextContext paramSource = SourceTextContext.skipNest(attachmentIterator,
                    Operator.PARENTHESES_PRE.getName(), Operator.PARENTHESES_POST.getName(), true
            );

            paramSource.removeFirst();
            paramSource.removeLast();// 去除前置(和后置)
            ParamDefinition.Factory factory = new ParamDefinition.Factory();
            ListIterator<SourceString> paramListIterator = paramSource.listIterator();
            while (paramListIterator.hasNext()) {
                paramListIterator = factory.create(paramListIterator);
                if (!paramListIterator.hasNext()) {
                    break;
                }
                SourceString next = paramListIterator.next();
                if (Operator.COMMA.nameEquals(next.getValue())) {
                    if (!paramListIterator.hasNext()) {
                        throw new AnalysisExpressionException(
                                paramListIterator.previous().getPosition(), "empty is illegal after ','");
                    }
                    continue;
                }
                throw new AnalysisExpressionException(paramListIterator.previous().getPosition(), "expect ','");
            }
            this.paramLists = factory.getParams();
            return this;
        }

        /**
         * @return throws 也考虑 const 修饰
         */
        public Builder throwsList(ListIterator<SourceString> attachmentIterator) {
            if (!attachmentIterator.hasNext()) {
                this.throwsTypes = Collections.emptyList();
                return this;
            }
            SourceString mayThrows = attachmentIterator.next();
            if (mayThrows.getType() != SourceType.KEYWORD || !Keyword.THROWS.equals(mayThrows.getValue())) {
                this.throwsTypes = Collections.emptyList();
                return this;
            }
            this.throwsTypes = localTypesOnDeclare(attachmentIterator);
            return this;
        }

        public Builder noMore(ListIterator<SourceString> attachmentIterator) {
            if (attachmentIterator.hasNext()) {
                throw new AnalysisExpressionException(attachmentIterator.next().getPosition(), "expected {");
            }
            return this;
        }

        public Builder body(SourceTextContext body) {
            this.body = body;
            return this;
        }

        public CallableDefinition build() {
            valid();
            return new CallableDefinition(permission, embellish, type, returnTypes, identifierReference, genericDefine,
                    genericMap, paramLists, throwsTypes, body
            );
        }

        private void valid() {
            Definition.notNullValid(identifierReference, "identifier reference");
            Definition.notNullValid(permission, "permission");
            Definition.notNullValid(embellish, "embellish");
            Definition.notNullValid(type, "type");
            Definition.notNullValid(returnTypes, "return types");
            Definition.notNullValid(genericDefine, "generic define");
            Definition.notNullValid(genericMap, "generic define map");
            Definition.notNullValid(paramLists, "param lists");
            Definition.notNullValid(throwsTypes, "throws types");
            Definition.notNullValid(body, "body");
        }
    }

}
