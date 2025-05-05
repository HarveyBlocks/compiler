package org.harvey.compiler.type.generic.link;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;

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
 * @date 2025-04-14 22:33
 */
@AllArgsConstructor
public class LinkedGenericDefine {
    private final IdentifierString name;
    private final boolean multiple;
    private final List<LocalParameterizedLink[]> constructorParameters;
    // nullable
    private final Using defaultType;
    // nullable
    private final Using lower;
    /**
     * 一个类, 多个接口
     */
    private final Using[] uppers;

    public LinkedGenericDefine(IdentifierString name) {
        this(name, false, Collections.emptyList(), null, null, new Using[0]);
    }

    public SourcePosition getPosition() {
        return name.getPosition();
    }

    public static class TypeSequential extends
            ParameterizedTypeLink.Sequential<FullIdentifierString> {
        public TypeSequential(List<Pair<FullIdentifierString, Integer>> list) {
            super(list);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Using {
        private final List<? extends TypeSequential> sequential;

        public static Using emptyUsing() {
            return new Using(Collections.emptyList());
        }
    }

    public static class UsingSerializer implements StreamSerializer<Using> {
        private static final FullIdentifierString.Serializer FULL_IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
                FullIdentifierString.Serializer.class);

        static {
            StreamSerializerRegister.register(new UsingSerializer());
        }

        private UsingSerializer() {

        }

        @Override
        public int out(
                OutputStream os, Using src) {
            int length = 0;
            length += StreamSerializerUtil.writeNumber(os, src.sequential.size(), 16, false);
            for (ParameterizedTypeLink.Sequential<FullIdentifierString> each : src.sequential) {
                List<Pair<FullIdentifierString, Integer>> list = each.getList();
                length += StreamSerializerUtil.writeNumber(os, list.size(), 16, false);
                for (Pair<FullIdentifierString, Integer> pair : list) {
                    length += FULL_IDENTIFIER_STRING_SERIALIZER.out(os, pair.getKey());
                    length += StreamSerializerUtil.writeNumber(os, pair.getValue(), 16, false);
                }
            }
            return length;
        }

        @Override
        public Using in(InputStream is) {
            int size = (int) StreamSerializerUtil.readNumber(is, 16, false);
            List<TypeSequential> result = new ArrayList<>(
                    size);
            for (int i = 0; i < size; i++) {
                int linkSize = (int) StreamSerializerUtil.readNumber(is, 16, false);
                List<Pair<FullIdentifierString, Integer>> links = new ArrayList<>(linkSize);
                for (int j = 0; j < linkSize; j++) {
                    FullIdentifierString rawType = FULL_IDENTIFIER_STRING_SERIALIZER.in(is);
                    int childSize = (int) StreamSerializerUtil.readNumber(is, 16, false);
                    links.add(new Pair<>(rawType, childSize));
                }
                result.add(new TypeSequential(links));
            }
            return new Using(result);
        }
    }

    // header 6 bits for upper, 1 bit for lower_exist, 1 bit for default_exist
    public static class DefineSerializer implements StreamSerializer<LinkedGenericDefine> {
        private static final IdentifierString.Serializer IDENTIFIER_SERIALIZER = StreamSerializerRegister.get(
                IdentifierString.Serializer.class);
        private static final UsingSerializer USING_SERIALIZER = StreamSerializerRegister.get(UsingSerializer.class);
        private static final LocalParameterizedLink.Serializer LOCAL_PARAMETERIZED_LINK_SERIALIZER = StreamSerializerRegister.get(
                LocalParameterizedLink.Serializer.class);

        static {
            StreamSerializerRegister.register(new DefineSerializer());
        }


        private DefineSerializer() {
        }

        @Override
        public LinkedGenericDefine in(InputStream is) {
            HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 2, 5, 1, 1, 1, 8);
            int constructorSize = (int) headMaps[0].getUnsignedValue();
            boolean multiple = headMaps[1].getUnsignedValue() != 0;
            boolean existDefault = headMaps[2].getUnsignedValue() != 0;
            boolean existLower = headMaps[3].getUnsignedValue() != 0;
            int upperSize = (int) headMaps[4].getUnsignedValue();
            IdentifierString name = IDENTIFIER_SERIALIZER.in(is);
            List<LocalParameterizedLink[]> constructorParameters = new ArrayList<>(constructorSize);
            for (int i = 0; i < constructorSize; i++) {
                long eachSize = StreamSerializerUtil.readNumber(is, 8, false);
                ArrayList<LocalParameterizedLink> parameterizedTypes = StreamSerializerUtil.readElements(is, eachSize,
                        LOCAL_PARAMETERIZED_LINK_SERIALIZER
                );
                constructorParameters.add(i, parameterizedTypes.toArray(LocalParameterizedLink[]::new));
            }
            Using defaultType = existDefault ? USING_SERIALIZER.in(is) : null;
            Using lower = existLower ? USING_SERIALIZER.in(is) : null;
            Using[] uppers = StreamSerializerUtil.readElements(is, upperSize, USING_SERIALIZER).toArray(Using[]::new);
            return new LinkedGenericDefine(name, multiple, constructorParameters, defaultType, lower, uppers);
        }

        @Override
        public int out(OutputStream os, LinkedGenericDefine src) {
            int length = StreamSerializerUtil.writeHeads(
                    os,
                    new HeadMap(src.constructorParameters.size(), 5).inRange(true, "lower exist"),
                    new HeadMap(src.multiple ? 1 : 0, 1).inRange(true, "indefinite length"),
                    new HeadMap(src.defaultType != null ? 1 : 0, 1).inRange(true, "default exist"),
                    new HeadMap(src.lower != null ? 1 : 0, 1).inRange(true, "lower exist"),
                    new HeadMap(src.uppers.length, 8).inRange(true, "default exist")
            ) + IDENTIFIER_SERIALIZER.out(os, src.name);
            for (LocalParameterizedLink[] types : src.constructorParameters) {
                length += StreamSerializerUtil.writeNumber(os, types.length, 8, false) +
                          StreamSerializerUtil.writeElements(os, types, LOCAL_PARAMETERIZED_LINK_SERIALIZER);
            }
            return length +
                   (src.defaultType == null ? 0 : USING_SERIALIZER.out(os, src.defaultType)) +
                   (src.lower == null ? 0 : USING_SERIALIZER.out(os, src.lower)) +
                   StreamSerializerUtil.writeElements(os, src.uppers, USING_SERIALIZER);
        }


    }
}
