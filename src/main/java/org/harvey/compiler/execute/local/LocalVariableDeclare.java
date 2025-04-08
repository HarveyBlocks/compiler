package org.harvey.compiler.execute.local;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.define.LocalTypeDefinition;
import org.harvey.compiler.declare.identifier.IdentifierManager;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.serializer.SourceStringStreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * type identifier [assign];
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-10 20:48
 */
@Getter
@Deprecated
@AllArgsConstructor
public class LocalVariableDeclare {
    /**
     * 多个变量共享一个type...这好吗? 这不好... 不好吗?
     */
    private final LocalType type;

    private final IdentifierString identifier;
    /**
     * int a = 2, b ,c = 3, e, f, g;
     * 只有后面初始化的表达式的部分, 没有签名的`a=`等, empty表示没有默认表达式
     */
    private final List<SourceString> assign;

    @Deprecated
    public LocalVariableDeclare(
            SourceVariableDeclare declare, IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        this.type = new LocalType(new LocalTypeDefinition.Builder().embellish(declare.getEmbellish())
                .type(declare.getType().listIterator())
                .build(), identifierManager);
        // TODO
        this.identifier = new IdentifierString(declare.getIdentifier());
        this.assign = declare.getAssign();
    }


    private static Pair<IdentifierString, Expression> checkDeclare(Expression part, SourcePosition position) {
        if (part.isEmpty()) {
            throw new AnalysisExpressionException(position, "expected an identifier");
        }
        ExpressionElement identifier = part.get(0);
        /*if (!(identifier instanceof IdentifierString)) {
            throw new AnalysisExpressionException(identifier.getPosition(), "expected an identifier");
        }
        return new Pair<>((IdentifierString) identifier, part);*/
        return null;
    }

    public static ArrayList<Pair<IdentifierString, Expression>> departAssign(
            List<SourceString> assign,
            IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        return null;//sourceStrings;
    }

    public ArrayList<LocalVariableDeclare> depart(
            IdentifierManager manager, IdentifierManager identifierManager,
            LocalVariableManager localVariableManager) {
        ArrayList<LocalVariableDeclare> result = new ArrayList<>();
        ListIterator<SourceString> iterator = assign.listIterator();
        while (iterator.hasNext()) {
            SourceTextContext sourceStrings = SourceTextContext.skipUntilComma(iterator);
            result.add(new LocalVariableDeclare(type, new IdentifierString(sourceStrings.getFirst()), sourceStrings));
            if (!iterator.hasNext()) {
                break;
            }
            if (!CollectionUtil.skipIf(iterator, s -> Operator.COMMA.nameEquals(s.getValue()))) {
                throw new AnalysisExpressionException(iterator.next().getPosition(), "expected ,");
            }
            if (!iterator.hasNext()) {
                throw new AnalysisExpressionException(
                        iterator.previous().getPosition(), "not expected empty after comma");
            }

        }

        return result;
    }

    public static class Serializer implements StreamSerializer<LocalVariableDeclare> {
        private static final SourceStringStreamSerializer SOURCE_SERIALIZER = StreamSerializerRegister.get(
                SourceStringStreamSerializer.class);
        private static final IdentifierString.Serializer IDENTIFIER_STRING_SERIALIZER = StreamSerializerRegister.get(
                IdentifierString.Serializer.class);
        private static final LocalType.Serializer LOCAL_TYPE_SERIALIZER = StreamSerializerRegister.get(
                LocalType.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        private Serializer() {
        }

        @Override
        public LocalVariableDeclare in(InputStream is) {
            LocalType type = LOCAL_TYPE_SERIALIZER.in(is);
            IdentifierString identifierString = IDENTIFIER_STRING_SERIALIZER.in(is);
            ArrayList<SourceString> context = StreamSerializerUtil.collectionIn(is, SOURCE_SERIALIZER);
            return new LocalVariableDeclare(type, identifierString, context);
        }

        @Override
        public int out(OutputStream os, LocalVariableDeclare src) {
            return LOCAL_TYPE_SERIALIZER.out(os, src.type) + IDENTIFIER_STRING_SERIALIZER.out(os, src.identifier) +
                   StreamSerializerUtil.collectionOut(os, src.assign, SOURCE_SERIALIZER);
        }
    }
}
