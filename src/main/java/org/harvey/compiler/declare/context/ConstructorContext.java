package org.harvey.compiler.declare.context;

import lombok.Getter;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.local.LocalType;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.type.generic.define.GenericDefine;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-27 14:40
 */
@Getter
public class ConstructorContext implements DeclaredContext {
    private final AccessControl accessControl;

    private final List<GenericDefine> genericMessage;
    private final List<ParamContext> paramList;
    private final boolean lastMultiply;
    private final List<LocalType> throwsExceptions;
    private final int body;
    private final Map<String, Integer> genericMap;

    public ConstructorContext(
            AccessControl accessControl,
            List<GenericDefine> genericMessage,
            List<ParamContext> paramList,
            int body,
            List<LocalType> throwsExceptions,
            boolean lastMultiply) {
        this.accessControl = accessControl;
        this.genericMessage = genericMessage;
        this.paramList = paramList;
        this.body = body;
        this.throwsExceptions = throwsExceptions;
        this.lastMultiply = lastMultiply;
        this.genericMap = new HashMap<>();
    }

    public ConstructorContext(CallableContext method) {
        if (method.getType() != CallableType.CONSTRUCTOR) {
            throw new CompilerException("illegal method type: " + method.getType(), new IllegalArgumentException());
        }
        if (!method.getReturnType().isEmpty()) {
            throw new CompilerException("illegal return type: " + method.getType(), new IllegalArgumentException());
        }
        this.accessControl = method.getAccessControl();
        this.genericMessage = method.getGenericMessage();
        this.paramList = method.getParamList();
        this.body = method.getBody();
        this.throwsExceptions = method.getThrowsExceptions();
        this.lastMultiply = method.isLastMultiply();
        this.genericMap = method.getGenericMap();
    }

    @Override
    public ReferenceElement getIdentifierReference() {
        throw new CompilerException("it is a constructor control", new UnsupportedOperationException());
    }

    @Override
    public Embellish getEmbellish() {
        throw new CompilerException("it is a constructor control", new UnsupportedOperationException());
    }

    /**
     * @return default <= (param.length - 1 if defaultStartIndex  else param.length)
     */
    public int startOfDefaultParam() {
        int i = 0, end = paramList.size() - (lastMultiply ? 1 : 0);
        for (; i < end; i++) {
            ParamContext paramContext = paramList.get(i);
            if (paramContext.getDefaultAssign() != SourceStringContextPoolFactory.REFERENCE_FOR_NULL) {
                // 没有default assign
                break;
            }
        }
        return i;
    }

    public static class Serializer implements StreamSerializer<ConstructorContext> {
        public static final int[] HEAD_LENGTH_BITS = {8, 32, 8, 11, 1, 12};
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
        public ConstructorContext in(InputStream is) {
            HeadMap[] headMap = StreamSerializerUtil.readHeads(is, HEAD_BYTE, HEAD_LENGTH_BITS);
            byte accessControlCode = (byte) headMap[0].getUnsignedValue(); // 8
            int bodyReference = (int) headMap[1].getUnsignedValue(); // 32
            long genericMessageSize = headMap[2].getUnsignedValue();// 8
            long paramListSize = headMap[3].getUnsignedValue(); // 11
            boolean lastIsMultiply = headMap[4].getUnsignedValue() == 0; // 1
            long throwsExceptionsSize = headMap[5].getUnsignedValue(); // 12
            AccessControl accessControl = new AccessControl(accessControlCode);
            List<GenericDefine> genericMessage = StreamSerializerUtil.readElements(
                    is, genericMessageSize, GENERIC_DEFINE_SERIALIZER);
            List<ParamContext> paramList = StreamSerializerUtil.readElements(
                    is, paramListSize, PARAM_CONTEXT_SERIALIZER);

            List<LocalType> throwsExceptions = StreamSerializerUtil.readElements(is, throwsExceptionsSize,
                    LOCAL_TYPE_SERIALIZER
            );

            return new ConstructorContext(accessControl, genericMessage, paramList, bodyReference, throwsExceptions,
                    lastIsMultiply
            );
        }


        @Override
        public int out(OutputStream os, ConstructorContext src) {
            byte accessControl = src.accessControl.getPermission();
            int body = src.body;
            List<GenericDefine> genericMessage = src.genericMessage;
            List<ParamContext> paramList = src.paramList;
            List<LocalType> throwsExceptions = src.throwsExceptions;

            return StreamSerializerUtil.writeHeads(
                    os,
                    new HeadMap(accessControl, HEAD_LENGTH_BITS[0]).inRange(true, "access control"),
                    new HeadMap(body, HEAD_LENGTH_BITS[1]).inRange(true, "body size"),
                    new HeadMap(genericMessage.size(), HEAD_LENGTH_BITS[2]).inRange(true, "generic message length"),
                    new HeadMap(paramList.size(), HEAD_LENGTH_BITS[3]).inRange(true, "param list size"),
                    new HeadMap(src.lastMultiply ? 1 : 0, HEAD_LENGTH_BITS[4]).inRange(true, "last multiply"),
                    new HeadMap(throwsExceptions.size(), HEAD_LENGTH_BITS[5]).inRange(
                            true,
                            "throws exception list size"
                    )
            ) +
                   StreamSerializerUtil.writeElements(os, genericMessage, GENERIC_DEFINE_SERIALIZER) +
                   StreamSerializerUtil.writeElements(os, paramList, PARAM_CONTEXT_SERIALIZER) +
                   StreamSerializerUtil.writeElements(os, throwsExceptions, LOCAL_TYPE_SERIALIZER);
        }

    }
}
