package org.harvey.compiler.declare.context;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.define.FieldDefinition;
import org.harvey.compiler.declare.identifier.DIdentifierManager;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.serializer.*;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.RawType;
import org.harvey.compiler.type.generic.define.GenericDefine;
import org.harvey.compiler.type.generic.using.ParameterizedType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * 不显示作用域, 作用域取决于这个对象存在哪里, 存在局部变量表就是在方法里, 存在全局上下文就是全局范围
 * context的成员可以为Empty, 但不应该为null, 否则不好序列化区分null和Empty
 * 不对! 这个一定是字段, 不可能是局部变量或者全局变量, 全局变量已经被删除了, 局部变量不会在这里被解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:45
 */
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class ValueContext implements DeclaredContext {
    // -------------------第二阶段加载----------------------------
    private final AccessControl accessControl;
    private final Embellish embellish;
    // 趁着序列化, 把类型转一转吧
    private final ParameterizedType<ReferenceElement> type;
    private final List<Pair<ReferenceElement, Integer>> assignList;

    public ValueContext(
            FieldDefinition definition,
            DIdentifierManager identifierManager,
            SourceStringContextPoolFactory poolFactory) {
        this.accessControl = definition.getPermissions();
        this.embellish = definition.getEmbellish();
        this.type = dealType(definition.getType(), identifierManager);
        this.assignList = dealAssign(definition.getIdentifierMap(), poolFactory);
    }

    private static List<Pair<ReferenceElement, Integer>> dealAssign(
            List<Pair<ReferenceElement, SourceTextContext>> assignMap, SourceStringContextPoolFactory poolFactory) {
        return assignMap.stream()
                .map(p -> new Pair<>(p.getKey(), poolFactory.add(p.getValue())))
                .collect(Collectors.toList());
    }

    private static ParameterizedType<ReferenceElement> dealType(
            Pair<RawType, SourceTextContext> typePair, DIdentifierManager identifierManager) {
        ReferenceElement referenceElement = GenericFactory.rawType2Reference(typePair.getKey(), identifierManager);
        ListIterator<SourceString> iterator = typePair.getValue().listIterator();
        return GenericFactory.parameterizedType(referenceElement, iterator, identifierManager);
    }

    @Override
    public ReferenceElement getIdentifierReference() {
        throw new CompilerException(new UnsupportedOperationException());
    }


    public static class Serializer implements StreamSerializer<ValueContext> {
        public static final SourceStringStreamSerializer SOURCE_STRING_SERIALIZER = StreamSerializerRegister.get(
                SourceStringStreamSerializer.class);
        public static final GenericDefine.GenericUsingSerializer TYPE_SERIALIZER = StreamSerializerRegister.get(
                GenericDefine.GenericUsingSerializer.class);
        private static final ReferenceElement.Serializer REFERENCE_ELEMENT_SERIALIZER = StreamSerializerRegister.get(
                ReferenceElement.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }


        @Override
        public ValueContext in(InputStream is) {
            HeadMap[] headMaps = StreamSerializerUtil.readHeads(is, 4, 8, 8, 16);
            ParameterizedType<ReferenceElement> type = TYPE_SERIALIZER.in(is);
            List<Pair<ReferenceElement, Integer>> assignList = readAssign(
                    is, headMaps[2].getUnsignedValue());
            return new ValueContext(new AccessControl((byte) headMaps[0].getUnsignedValue()),
                    new Embellish((byte) headMaps[1].getUnsignedValue()), type, assignList
            );
        }

        @Override
        public int out(OutputStream os, ValueContext src) {
            return StreamSerializerUtil.writeHeads(os, new HeadMap(src.accessControl.getPermission(), 8),
                    new HeadMap(src.embellish.getCode(), 8),
                    new HeadMap(src.assignList.size(), 16).inRange(true, "assign list size")
            ) + TYPE_SERIALIZER.out(os, src.type) + writeAssign(os, src.assignList);
        }

        private int writeAssign(
                OutputStream os, List<? extends Pair<ReferenceElement, Integer>> assignList) {
            int length = 0;
            for (Pair<ReferenceElement, Integer> pair : assignList) {
                length += REFERENCE_ELEMENT_SERIALIZER.out(os, pair.getKey()) +
                          StreamSerializerUtil.writeNumber(os, pair.getValue(), 32, false);
            }
            return length;
        }

        private List<Pair<ReferenceElement, Integer>> readAssign(InputStream is, long length) {
            List<Pair<ReferenceElement, Integer>> assignList = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                ReferenceElement referenceElement = REFERENCE_ELEMENT_SERIALIZER.in(is);
                int value = (int) StreamSerializerUtil.readNumber(is, 32, false);
                assignList.add(new Pair<>(referenceElement, value));
            }
            return assignList;
        }

    }
}
