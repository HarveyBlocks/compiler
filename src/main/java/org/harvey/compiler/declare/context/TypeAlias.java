package org.harvey.compiler.declare.context;

import lombok.Getter;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.define.AliasDefinition;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisException;
import org.harvey.compiler.exception.io.CompilerFileReadException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ListIterator;
import java.util.Objects;

/**
 * 不要null字段
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-13 23:56
 */

@Getter
public class TypeAlias {
    public static final Boolean SEALED = Boolean.TRUE;
    public static final Boolean NOT_SEALED = Boolean.FALSE;
    public static final Boolean UNKNOWN_SEALED = null;
    private final AccessControl accessControl;
    private final boolean staticAlias;
    private final ReferenceElement aliasNameReference;
    private final GenericDefine[] aliasGenericMessage;
    private final ParameterizedType<ReferenceElement> origin;
    private Boolean sealed = UNKNOWN_SEALED;

    private TypeAlias(
            AccessControl accessControl,
            boolean staticAlias,
            ReferenceElement aliasNameReference,
            GenericDefine[] aliasGenericMessage,
            ParameterizedType<ReferenceElement> origin) {
        this.accessControl = accessControl;
        this.staticAlias = staticAlias;
        this.aliasNameReference = aliasNameReference;
        this.aliasGenericMessage = aliasGenericMessage;
        this.origin = origin;
    }

    public TypeAlias(AliasDefinition definition, IdentifierManager manager) {
        this.accessControl = definition.getPermissions();
        this.aliasNameReference = definition.getIdentifierReference();
        this.aliasGenericMessage = definition.getGenericDefine()
                .stream()
                .map(e -> GenericFactory.genericForDefine(e.getKey(), e.getValue(), manager))
                .toArray(GenericDefine[]::new);
        ListIterator<SourceString> iterator = definition.getOrigin().listIterator();
        this.origin = GenericFactory.parameterizedType(iterator, manager);
        if (this.origin.getRawType().getType() == ReferenceType.GENERIC_IDENTIFIER) {
            throw new AnalysisException(this.origin.getRawType().getPosition(), "can not be a generic");
        }
        this.staticAlias = definition.isStaticAlias();

        if (iterator.hasNext()) {
            throw new CompilerException("unexpected after parameterized type");
        }
    }

    public void setSealed(boolean sealed) {
        if (Objects.equals(this.sealed, UNKNOWN_SEALED)) {
            this.sealed = sealed;
        } else {
            throw new CompilerException("has set sealed");
        }
    }

    private byte serializeSealedOut() {
        if (Objects.equals(sealed, NOT_SEALED)) {
            return 0;
        } else if (Objects.equals(sealed, SEALED)) {
            return 1;
        } else {
            return 2;
        }
    }

    private void serializeSealedIn(long sealed) {
        if (sealed == 0) {
            this.sealed = NOT_SEALED;
        } else if (sealed == 1) {
            this.sealed = SEALED;
        } else {
            this.sealed = UNKNOWN_SEALED;
        }
    }

    public static class Serializer implements StreamSerializer<TypeAlias> {

        public static final int[] HEAD_LENGTH_BITS = {8, 1, 2, 13};
        public static final int HEAD_BYTE = StreamSerializerUtil.headByte(HEAD_LENGTH_BITS);
        private static final GenericDefine.Serializer GENERIC_DEFINE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.Serializer.class);
        private static final GenericDefine.GenericUsingSerializer PARAMETERIZED_TYPE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.GenericUsingSerializer.class);
        private static final ReferenceElement.Serializer REFERENCE_ELEMENT_SERIALIZER = StreamSerializerRegister.get(
                ReferenceElement.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public TypeAlias in(InputStream is) {
            HeadMap[] head;
            try {
                head = new SerializableData(is.readNBytes(HEAD_BYTE)).phaseHeader(HEAD_LENGTH_BITS);
            } catch (IOException e) {
                throw new CompilerFileReadException(e);
            }
            AccessControl accessControl = new AccessControl((byte) head[0].getUnsignedValue());
            boolean staticAlias = head[1].getUnsignedValue() == 1;
            ReferenceElement aliasNameReference = REFERENCE_ELEMENT_SERIALIZER.in(is);
            long sealedCode = head[2].getUnsignedValue();
            GenericDefine[] aliasGenericMessage = StreamSerializerUtil.readElements(is, head[3].getUnsignedValue(),
                    GENERIC_DEFINE_SERIALIZER
            ).toArray(GenericDefine[]::new);

            ParameterizedType<ReferenceElement> origin = PARAMETERIZED_TYPE_SERIALIZER.in(is);
            TypeAlias typeAlias = new TypeAlias(
                    accessControl, staticAlias, aliasNameReference, aliasGenericMessage, origin);
            typeAlias.serializeSealedIn(sealedCode);
            return typeAlias;
        }

        @Override
        public int out(OutputStream os, TypeAlias src) {
            byte accessControl = src.accessControl.getPermission();
            byte[] head = Serializes.makeHead(
                    new HeadMap(accessControl, HEAD_LENGTH_BITS[0]),
                    new HeadMap(src.staticAlias ? 1 : 0, HEAD_LENGTH_BITS[1]),
                    new HeadMap(src.serializeSealedOut(), HEAD_LENGTH_BITS[2]).inRange(
                            true,
                            "sealed"
                    ),
                    new HeadMap(src.aliasGenericMessage.length, HEAD_LENGTH_BITS[3]).inRange(
                            true,
                            "type alias generic message name"
                    )
            ).data();
            try {
                os.write(head);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return head.length +
                   REFERENCE_ELEMENT_SERIALIZER.out(os, src.getAliasNameReference()) +
                   StreamSerializerUtil.writeElements(os, src.aliasGenericMessage, GENERIC_DEFINE_SERIALIZER) +
                   PARAMETERIZED_TYPE_SERIALIZER.out(os, src.origin);
        }
    }
}
