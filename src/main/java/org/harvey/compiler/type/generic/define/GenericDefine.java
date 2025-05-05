package org.harvey.compiler.type.generic.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.type.generic.using.LocalParameterizedType;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-02 00:05
 */
@AllArgsConstructor
@Getter
@Deprecated
public class GenericDefine {

    private final IdentifierString name;
    private final boolean multiple;
    private final List<LocalParameterizedType[]> constructorParameters;
    // nullable
    private final ParameterizedType<ReferenceElement> defaultType;
    // nullable
    private final ParameterizedType<ReferenceElement> lower;
    /**
     * 一个类, 多个接口
     */
    private final ParameterizedType<ReferenceElement>[] uppers;

    public GenericDefine(IdentifierString name) {
        this(name, false, Collections.emptyList(), null, null, new ParameterizedType[0]);
    }

    public SourcePosition getPosition() {
        return name.getPosition();
    }

    public static class GenericUsingSerializer extends ParameterizedType.Serializer<ReferenceElement> {
        private static final ReferenceElement.Serializer RAW_TYPE_SERIALIZER = StreamSerializerRegister.get(
                ReferenceElement.Serializer.class);

        static {
            StreamSerializerRegister.register(new GenericUsingSerializer());
        }

        private GenericUsingSerializer() {
        }

        @Override
        protected StreamSerializer<ReferenceElement> getRawTypeSerializer() {
            return RAW_TYPE_SERIALIZER;
        }
    }

    // header 6 bits for upper, 1 bit for lower_exist, 1 bit for default_exist
    public static class Serializer implements StreamSerializer<GenericDefine> {
        private static final GenericUsingSerializer PARAMETERIZED_TYPE_SERIALIZER = StreamSerializerRegister.get(
                GenericUsingSerializer.class);
        private static final LocalParameterizedType.Serializer LOCAL_PARAMETERIZED_TYPE_SERIALIZER = StreamSerializerRegister.get(
                LocalParameterizedType.Serializer.class);
        private static final IdentifierString.Serializer IDENTIFIER_SERIALIZER = StreamSerializerRegister.get(
                IdentifierString.Serializer.class);
        private static final ReferenceElement.Serializer REFERENCE_ELEMENT_SERIALIZER = StreamSerializerRegister.get(
                ReferenceElement.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }


        private Serializer() {
        }

        @Override
        public GenericDefine in(InputStream is) {
            HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 2, 5, 1, 1, 1, 8);
            int constructorSize = (int) headMaps[0].getUnsignedValue();
            boolean multiple = headMaps[1].getUnsignedValue() != 0;
            boolean existDefault = headMaps[2].getUnsignedValue() != 0;
            boolean existLower = headMaps[3].getUnsignedValue() != 0;
            int upperSize = (int) headMaps[4].getUnsignedValue();
            IdentifierString name = IDENTIFIER_SERIALIZER.in(is);
            List<LocalParameterizedType[]> constructorParameters = new ArrayList<>(constructorSize);
            for (int i = 0; i < constructorSize; i++) {
                long eachSize = StreamSerializerUtil.readNumber(is, 8, false);
                ArrayList<LocalParameterizedType> parameterizedTypes = StreamSerializerUtil.readElements(is, eachSize,
                        LOCAL_PARAMETERIZED_TYPE_SERIALIZER
                );
                constructorParameters.add(i, parameterizedTypes.toArray(LocalParameterizedType[]::new));
            }
            ParameterizedType<ReferenceElement> defaultType = existDefault ? PARAMETERIZED_TYPE_SERIALIZER.in(
                    is) : null;
            ParameterizedType<ReferenceElement> lower = existLower ? PARAMETERIZED_TYPE_SERIALIZER.in(is) : null;
            ParameterizedType[] uppers = StreamSerializerUtil.readElements(is, upperSize, PARAMETERIZED_TYPE_SERIALIZER)
                    .toArray(ParameterizedType[]::new);
            return new GenericDefine(name, multiple, constructorParameters, defaultType, lower, uppers);
        }

        @Override
        public int out(OutputStream os, GenericDefine src) {
            int length = StreamSerializerUtil.writeHeads(
                    os,
                    new HeadMap(src.constructorParameters.size(), 5).inRange(true, "lower exist"),
                    new HeadMap(src.multiple ? 1 : 0, 1).inRange(true, "indefinite length"),
                    new HeadMap(src.defaultType != null ? 1 : 0, 1).inRange(true, "default exist"),
                    new HeadMap(src.lower != null ? 1 : 0, 1).inRange(true, "lower exist"),
                    new HeadMap(src.uppers.length, 8).inRange(true, "default exist")
            ) + IDENTIFIER_SERIALIZER.out(os, src.name);
            for (LocalParameterizedType[] types : src.constructorParameters) {
                length += StreamSerializerUtil.writeNumber(os, types.length, 8, false) +
                          StreamSerializerUtil.writeElements(os, types, LOCAL_PARAMETERIZED_TYPE_SERIALIZER);
            }
            return length + (src.defaultType == null ? 0 : PARAMETERIZED_TYPE_SERIALIZER.out(os, src.defaultType)) +
                   (src.lower == null ? 0 : PARAMETERIZED_TYPE_SERIALIZER.out(os, src.lower)) +
                   StreamSerializerUtil.writeElements(os, src.uppers, PARAMETERIZED_TYPE_SERIALIZER);

        }
    }
}
