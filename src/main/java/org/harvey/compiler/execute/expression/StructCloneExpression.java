package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 结构体克隆的表达式
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-08 16:50
 */
@Getter
public class StructCloneExpression extends ComplexExpression {
    public static final String END_MSG = "__struct__clone__end__";
    // 声明
    // struct Student{
    //  int age;
    //  String name;
    //  int class;
    //  long id;
    //  float score;
    // }
    // 调用构造器
    // var stu = new Student(12,"Mike",4,114514,75.5);
    // int idBias = 1;
    //
    // Student newStu = new(stu){
    //      score =  99.9 ,
    //      name = "John",
    //      id=stu.id+idBias;
    // }
    public static final int UNSURE_OTHER_SIDE = 0x7f_ff_ff_ff;
    public static final ComplexExpression.Type TYPE = Type.STRUCT_CLONE;
    private final boolean start;//不是start, 就是end
    @Setter
    private int otherSide;


    public StructCloneExpression(boolean start) {
        this(start, UNSURE_OTHER_SIDE);
    }

    private StructCloneExpression(boolean start, int otherSide) {
        this.start = start;
        this.otherSide = otherSide;
    }

    public static class Serializer implements StreamSerializer<StructCloneExpression> {


        static {
            ComplexExpression.Serializer.register(TYPE.ordinal(), new StructCloneExpression.Serializer(),
                    StructCloneExpression.class
            );
        }

        private Serializer() {
        }

        @Override
        public StructCloneExpression in(InputStream is) {
            HeadMap[] head = StreamSerializerUtil.readHeads(is, 4, 1, 31);
            boolean start = head[0].getUnsignedValue() != 0;
            int otherSide = (int) head[1].getUnsignedValue();
            return new StructCloneExpression(start, otherSide);
        }

        @Override
        public int out(OutputStream os, StructCloneExpression src) {
            return StreamSerializerUtil.writeHeads(os, new HeadMap(src.start ? 1 : 0, 1),
                    new HeadMap(src.otherSide, 31).inRange(true, "array init other size reference")
            );
        }
    }
}
