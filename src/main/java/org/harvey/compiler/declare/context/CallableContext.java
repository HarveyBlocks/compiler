package org.harvey.compiler.declare.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.define.CallableDefinition;
import org.harvey.compiler.declare.define.LocalTypeDefinition;
import org.harvey.compiler.declare.define.ParamDefinition;
import org.harvey.compiler.declare.identifier.CallableIdentifierManager;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.define.GenericDefine;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 需要被序列化的的Callable对象
 * 1. alias 要映射
 * 2. Generic的分析
 * 3. 函数签名的分析
 * 4. Operator是否允许
 * 5. Expression的解析
 * 3. Executable的解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:43
 */
@EqualsAndHashCode
@Getter
public class CallableContext implements DeclaredContext {
    private final ReferenceElement identifierReference;
    private final AccessControl accessControl;
    private final Embellish embellish;
    private final CallableType type;
    private final List<LocalType> returnType;
    /**
     * 包含前后缀`<>`
     */
    private final List<GenericDefine> genericMessage;
    private final List<ParamContext> paramList;

    private final int body;
    // throws的要不要考虑const? catch的
    private final List<LocalType> throwsExceptions;
    private final boolean lastMultiply;
    private final Map<String, Integer> genericMap;

    public CallableContext(
            ReferenceElement identifierReference,
            AccessControl accessControl,
            Embellish embellish,
            CallableType type,
            List<LocalType> returnType,
            List<GenericDefine> genericMessage,
            List<ParamContext> paramList,
            int body,
            List<LocalType> throwsExceptions,
            boolean lastMultiply) {
        this.identifierReference = identifierReference;
        this.accessControl = accessControl;
        this.embellish = embellish;
        this.type = type;
        this.returnType = returnType;
        this.genericMessage = genericMessage;
        this.paramList = paramList;
        this.body = body;
        this.throwsExceptions = throwsExceptions;
        this.lastMultiply = lastMultiply;
        this.genericMap = new HashMap<>();
    }

    public CallableContext(
            CallableDefinition definition,
            IdentifierManager manager,
            SourceStringContextPoolFactory poolFactory,
            boolean openCheckVisited) {
        this.identifierReference = definition.getIdentifierReference();
        this.accessControl = definition.getPermissions();
        this.embellish = definition.getEmbellish();
        this.type = definition.getType();

        // reference->ge
        CallableIdentifierManager managerInCallable = definition.wrap(manager);
        if (openCheckVisited) {
            managerInCallable.openVisitedGenericCheck();
        }
        this.genericMessage = definition.getGenericDefines()
                .stream()
                .map(p -> GenericFactory.genericForDefine(genericOnCallableToReference(p.getKey(), managerInCallable),
                        p.getValue(), managerInCallable
                ))
                .collect(Collectors.toList());
        this.paramList = paramList(definition.getParamLists(), managerInCallable, poolFactory);
        int notVisitedGeneric = managerInCallable.notVisitedGeneric();
        if (notVisitedGeneric >= 0) {
            // 只能通过Param确定GenericDefine
            GenericDefine define = genericMessage.get(notVisitedGeneric);
            throw new AnalysisExpressionException(
                    define.getPosition(),
                    "define can not analysis from param. So, it's meaning less"
            );
        }
        managerInCallable.closeVisitedGenericCheck();
        this.genericMap = definition.getGenericMap();
        this.returnType = localTypeList(definition.getReturnTypes(), managerInCallable);
        this.lastMultiply = hasMultipleType(definition.getParamLists());
        this.throwsExceptions = localTypeList(definition.getThrowsTypes(), managerInCallable);
        this.body = poolFactory.add(definition.getBody());
    }

    private static ReferenceElement genericOnCallableToReference(
            IdentifierString identifierString, IdentifierManager managerInCallable) {
        return CallableIdentifierManager.getFromDeclare(
                identifierString.getPosition(),
                GenericFactory.rawType2Reference(identifierString, managerInCallable)
        );
    }

    public static boolean hasMultipleType(List<ParamDefinition> definitions) {
        if (definitions.isEmpty()) {
            return false;
        }
        return definitions.get(definitions.size() - 1).isMultipleType();
    }

    public static List<LocalType> localTypeList(List<LocalTypeDefinition> definitions, IdentifierManager manager) {
        return definitions.stream().map(d -> new LocalType(d, manager)).collect(Collectors.toList());
    }

    public static List<ParamContext> paramList(
            List<ParamDefinition> definitions, IdentifierManager manager, SourceStringContextPoolFactory poolFactory) {
        return definitions.stream()
                .map(d -> new ParamContext(new LocalType(d.getLocalTypeDefinition(), manager), d.getIdentifier(),
                       poolFactory.add(d.getInitAssign())
                ))
                .collect(Collectors.toList());
    }

    public void buildGenericMap(IdentifierManager identifierManager) {
        for (int i = 0, end = genericMessage.size(); i < end; i++) {
            ReferenceElement reference = genericMessage.get(i).getName();
            FullIdentifierString identifier = identifierManager.getIdentifier(reference);
            if (reference.getType() != ReferenceType.CALLABLE_GENERIC_IDENTIFIER || identifier.length() != 1) {
                throw new CompilerException("illegal generic serializer");
            }
            genericMap.put(identifier.get(0), i);
        }
    }

    public static class Serializer implements StreamSerializer<CallableContext> {
        public static final int[] HEAD_LENGTH_BITS = {8, 8, 8, 32, 12, 12, 11, 1, 12};
        public static final int HEAD_BYTE = StreamSerializerUtil.headByte(HEAD_LENGTH_BITS);
        private static final LocalType.Serializer LOCAL_TYPE_SERIALIZER = StreamSerializerRegister.get(
                LocalType.Serializer.class);
        private static final GenericDefine.Serializer GENERIC_DEFINE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.Serializer.class);
        private static final ParamContext.Serializer PARAM_CONTEXT_SERIALIZER = StreamSerializerRegister.get(
                ParamContext.Serializer.class);
        private static final ReferenceElement.Serializer REFERENCE_SERIALIZER = StreamSerializerRegister.get(
                ReferenceElement.Serializer.class);
        private static final SourceStringStreamSerializer SOURCE_STRING_STREAM_SERIALIZER = StreamSerializerRegister.get(
                SourceStringStreamSerializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public CallableContext in(InputStream is) {
            HeadMap[] headMap = StreamSerializerUtil.readHeads(is, HEAD_BYTE, HEAD_LENGTH_BITS);
            byte accessControlCode = (byte) headMap[0].getUnsignedValue(); // 8
            byte embellishCode = (byte) headMap[1].getUnsignedValue(); // 8
            int typeOrdinal = (int) headMap[2].getUnsignedValue(); // 8
            int bodyReference = (int) headMap[3].getUnsignedValue(); // 16
            long returnTypeSize = headMap[4].getUnsignedValue(); // 12
            long genericMessageSize = headMap[5].getUnsignedValue();// 12
            long paramListSize = headMap[6].getUnsignedValue(); // 11
            boolean lastIsMultiply = headMap[7].getUnsignedValue() == 0; // 1
            long throwsExceptionsSize = headMap[8].getUnsignedValue(); // 12
            AccessControl accessControl = new AccessControl(accessControlCode);
            Embellish embellish = new Embellish(embellishCode);
            CallableType type = CallableType.values()[typeOrdinal];
            ReferenceElement identifierReference = REFERENCE_SERIALIZER.in(is);
            List<LocalType> returnType = StreamSerializerUtil.readElements(is, returnTypeSize, LOCAL_TYPE_SERIALIZER);
            List<GenericDefine> genericMessage = StreamSerializerUtil.readElements(
                    is, genericMessageSize, GENERIC_DEFINE_SERIALIZER);
            List<ParamContext> paramList = StreamSerializerUtil.readElements(
                    is, paramListSize, PARAM_CONTEXT_SERIALIZER);

            List<LocalType> throwsExceptions = StreamSerializerUtil.readElements(is, throwsExceptionsSize,
                    LOCAL_TYPE_SERIALIZER
            );

            return new CallableContext(identifierReference, accessControl, embellish, type, returnType, genericMessage,
                    paramList, bodyReference, throwsExceptions, lastIsMultiply
            );
        }


        @Override
        public int out(OutputStream os, CallableContext src) {
            byte accessControl = src.accessControl.getPermission();
            byte embellish = src.embellish.getCode();
            int type = src.type.ordinal();
            int body = src.body;
            List<LocalType> returnType = src.returnType;
            List<GenericDefine> genericMessage = src.genericMessage;
            List<ParamContext> paramList = src.paramList;
            List<LocalType> throwsExceptions = src.throwsExceptions;

            return StreamSerializerUtil.writeHeads(
                    os,
                    new HeadMap(accessControl, HEAD_LENGTH_BITS[0]).inRange(true, "access control"),
                    new HeadMap(embellish, HEAD_LENGTH_BITS[1]),
                    new HeadMap(type, HEAD_LENGTH_BITS[2]).inRange(true, "callable type ordinal"),
                    new HeadMap(body, HEAD_LENGTH_BITS[3]).inRange(true, "body size"),
                    new HeadMap(returnType.size(), HEAD_LENGTH_BITS[4]).inRange(true, "return type size"),
                    new HeadMap(genericMessage.size(), HEAD_LENGTH_BITS[5]).inRange(true, "generic message length"),
                    new HeadMap(paramList.size(), HEAD_LENGTH_BITS[6]).inRange(true, "param list size"),
                    new HeadMap(src.lastMultiply ? 1 : 0, HEAD_LENGTH_BITS[7]).inRange(true, "last multiply"),
                    new HeadMap(throwsExceptions.size(), HEAD_LENGTH_BITS[8]).inRange(
                            true,
                            "throws exception list size"
                    )
            ) +
                   REFERENCE_SERIALIZER.out(os, src.identifierReference) +
                   StreamSerializerUtil.writeElements(os, returnType, LOCAL_TYPE_SERIALIZER) +
                   StreamSerializerUtil.writeElements(os, genericMessage, GENERIC_DEFINE_SERIALIZER) +
                   StreamSerializerUtil.writeElements(os, paramList, PARAM_CONTEXT_SERIALIZER) +
                   StreamSerializerUtil.writeElements(os, throwsExceptions, LOCAL_TYPE_SERIALIZER);
        }

    }


}
// 1. 作为变量解析
// 2. 作为代码块解析
// 2. 作为复合结构的解析
//      1. 作为字段解析
//      2. 作为方法解析
// `    3. 作为代码块解析
//      4. 对内部类的引用
//      5. 对外部类的引用
// 3. 作为函数解析声明
// 6. 嵌套的泛型<A,A<B,C>,D<E,F<D,E>>, G<H,I>>声明
// A,B<B,C,>
// stc 进行序列化咯
// 函数参数类型复合类型X
// 只能控制结构和修饰的关键字解析
// 类型和赋值表达式不能进行解析
// 也不知道变量的声明表达式是有哪些变量是声明的
// 常量池里可以有了函数名和复合结构名
// 复合类型(非基本数据类型), 可以被引用了
// 实现的代码块, 可以被引用了
// a = size()
// add(block)
// blockReference = a;