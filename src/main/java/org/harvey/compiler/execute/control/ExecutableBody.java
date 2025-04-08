package org.harvey.compiler.execute.control;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerRegister;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 23:27
 */
@Getter
@AllArgsConstructor
public class ExecutableBody extends ArrayList<Executable> {
    @Deprecated
    public static final ExecutableBody EMPTY = new ExecutableBody(SourcePosition.UNKNOWN);
    private final SourcePosition position;

    public ExecutableBody(Collection<? extends Executable> c, SourcePosition position) {
        super(c);
        this.position = position;
    }

    @Override
    public void add(int index, Executable element) {
        if (index != size()) {
            throw new CompilerException("can not add randomly");
        }
        super.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Executable> c) {
        if (index != size()) {
            throw new CompilerException("can not add randomly");
        }
        return super.addAll(index, c);
    }

    @Override
    public Executable remove(int index) {
        throw new CompilerException("can not remove");
    }

    @Override
    public boolean remove(Object o) {
        throw new CompilerException("can not remove");
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new CompilerException("can not remove");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new CompilerException("can not remove");
    }

    @Override
    public boolean removeIf(Predicate<? super Executable> filter) {
        return super.removeIf(filter);
    }


    public static class Serializer implements StreamSerializer<ExecutableBody> {
        private static final SourcePosition.Serializer SOURCE_POSITION_SERIALIZER = StreamSerializerRegister.get(
                SourcePosition.Serializer.class);

        static {
            StreamSerializerRegister.register(new Serializer());
        }

        public final Map<Byte, Function<InputStream, Executable>> factory = new HashMap<>();

        private Serializer() {
            factory.put(BodyEnd.CODE, BodyEnd::in); // 0
            factory.put(BodyStart.CODE, BodyStart::in); // 1
            factory.put(Break.CODE, Break::in); // 2
            factory.put(Case.CODE, Case::in);// 3
            factory.put(CatchStart.CODE, CatchStart::in); // 4
            factory.put(Continue.CODE, Continue::in); // 5
            factory.put(DeclareExecutable.CODE, DeclareExecutable::in); // 6
            factory.put(Default.CODE, Default::in); // 7
            factory.put(DoStart.CODE, DoStart::in); // 8
            factory.put(ElseIfStart.CODE, ElseIfStart::in); // 9
            factory.put(ElseStart.CODE, ElseStart::in); // 10
            factory.put(ExpressionExecutable.CODE, ExpressionExecutable::in);// 11
            factory.put(FinallyStart.CODE, FinallyStart::in); // 12
            factory.put(ForEachStart.CODE, ForEachStart::in); // 13
            factory.put(ForIndexStart.CODE, ForIndexStart::in); // 14
            factory.put(IfStart.CODE, IfStart::in); // 15
            factory.put(Return.CODE, Return::in); // 16
            factory.put(SwitchStart.CODE, SwitchStart::in); // 17
            // abstract factory.put(SequentialExecutable.CODE, SequentialExecutable::in);// 18
            factory.put(TryStart.CODE, TryStart::in);// 19
            factory.put(WhileEnd.CODE, WhileEnd::in);// 20
            factory.put(WhileStart.CODE, WhileStart::in);//21
        }

        @Override
        public ExecutableBody in(InputStream is) {
            SourcePosition sp = SOURCE_POSITION_SERIALIZER.in(is);
            ExecutableBody executableBody = new ExecutableBody(sp);
            long size = StreamSerializerUtil.readNumber(is, 32, false);
            for (int i = 0; i < size; i++) {
                byte code = (byte) StreamSerializerUtil.readNumber(is, 8, false);
                executableBody.add(in(is, code));
            }
            return executableBody;
        }

        private Executable in(InputStream is, byte code) {
            return factory.get(code).apply(is);
        }

        @Override
        public int out(OutputStream os, ExecutableBody src) {
            int length = SOURCE_POSITION_SERIALIZER.out(os, src.position) +
                         StreamSerializerUtil.writeNumber(os, src.size(), 32, false);
            for (Executable executable : src) {
                byte code = executable.getCode();
                length += StreamSerializerUtil.writeNumber(os, code, 8, false);
                length += executable.out(os);
            }
            return length;
        }
    }

}
