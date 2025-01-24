package org.harvey.compiler.declare.context;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.harvey.compiler.analysis.calculate.Operator;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.text.type.callable.ReturnType;
import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.execute.control.ExecutableBodyFactory;
import org.harvey.compiler.execute.expression.*;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.harvey.compiler.io.ss.StreamSerializer.*;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:43
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class CallableContext extends DeclaredContext {
    public static final IgnoreArgument IGNORE_ARGUMENT = new IgnoreArgument();
    public CallableType type;
    private ReturnType returnType; // 16bit
    /**
     * 包含前后缀`<>`
     */
    private Expression genericMessage; // 16bit
    private List<LocalVariableDeclare> paramList; // 16bit
    private int body; // 16bit
    // throws的要不要考虑const? catch的
    private List<SourceVariableDeclare.LocalType> throwsExceptions;


    /**
     * @param patterns 声明的参数列表
     * @param params   调用的参数列表
     */
    public static List<Expression> matchParam(List<LocalVariableDeclare> patterns, List<LocalVariableDeclare> params) {
        // paramList一定比pattern长
        // 只保留默认值和不定参数的功能
        // 含默认值参数只能写在不定参数之前
        // 如果不定参数和不定参数前一个/前几个含默认值参数有相同的类型, 报错
        // 不定参数不能带默认值
        LocalVariableDeclare lastDeclare = patterns.get(patterns.size() - 1);
        Expression lastType = lastDeclare.getType();
        Expression multipleTypeOrigin =
                isMultipleType(lastType) ? lastType.subExpression(0, lastType.size() - 1) : null;
        List<Expression> assign = new ArrayList<>(patterns.size());
        // 初始化
        for (int i = 0; i < patterns.size(); i++) {
            assign.add(new Expression());
        }
        int normalIndexEnd = Math.min(patterns.size() + (multipleTypeOrigin == null ? 0 : -1), params.size());
        int normal = 0; // 指向paramList最后一个normal
        // 简单模式
        for (; normal < normalIndexEnd; normal++) {
            LocalVariableDeclare pattern = patterns.get(normal);
            LocalVariableDeclare param = params.get(normal);
            if (pattern.getAssign() != null) {
                normal--;
                break;
            }
            if (matchType(pattern.getType(), param.getType())) {
                assign.get(normal).addAll(param.getAssign());
            } else {
                throw new RuntimeException("参数类型不匹配");
            }
        }
        // 默认值赋值模式
        // 从后往前, 和不定参数相同类型的, 统统加入, 直到类型不一样
        int multiply = params.size(); // 指向paramList最后一个符合multiply
        if (multipleTypeOrigin != null) {
            multiply--;
            for (; multiply >= normal; multiply--) {
                LocalVariableDeclare param = params.get(multiply);
                if (matchType(multipleTypeOrigin, param.getType())) {
                    assign.get(assign.size() - 1).addAll(param.getAssign());
                } else {
                    multiply++;
                    break;
                }
            }
        }
        // 皆为不定参数
        for (int i = normal + 1; i < multiply; i++) {
            if (i > patterns.size()) {
                throw new RuntimeException("参数个数不匹配");
            }
            LocalVariableDeclare param = params.get(i);
            LocalVariableDeclare pattern = patterns.get(i);
            if (matchType(pattern.getType(), param.getType())) {
                assign.get(i).addAll(param.getAssign());
            } else {
                throw new RuntimeException("参数类型不匹配");
            }
        }
        if (multiply < patterns.size()) {
            // 省略的参数
            for (int i = multiply - 1; i < patterns.size(); i++) {
                assign.get(i).addAll(patterns.get(i).getAssign());
            }
        }
        return assign;
    }

    private static boolean matchType(Expression pattern, Expression valueType) {
        // TODO
       /* if (valueType.isConst() && !pattern.isConst()) {
            return false;
        }*/
        if (pattern.size() != valueType.size()) {
            return false;
        }
        for (int i = 0; i < pattern.size(); i++) {
            /*if (!pattern.get(i).getValue().equals(valueType.get(i).getValue())) {
                return false;
            }*/
        }
        return true;
    }

    private static boolean isMultipleType(Expression type) {
        ExpressionElement last = type.get(type.size() - 1);
        return last instanceof OperatorString && ((OperatorString) last).getValue() == Operator.MULTIPLY;

    }

    public void setBody(int body, SourcePosition sp) {
        this.body = legalBody(body, sp);
    }

    /**
     * 检查函数体和abstract是否冲突
     */
    public int legalBody(int body, SourcePosition sp) {
        boolean noBody = body < 0;
        boolean abstractMarked = embellish.isMarkedAbstract();
        if (noBody && abstractMarked) {
            return ExecutableBodyFactory.NO_BODY_REFERENCE;
        } else if (!noBody && !abstractMarked) {
            return body;
        } else if (!noBody) {
            throw new AnalysisExpressionException(sp, "abstract is conflict with body");
        } else {
            throw new AnalysisExpressionException(sp, "callable is body needed");
        }
    }

    public static class Builder {
        private final CallableContext product;

        public Builder() {
            product = new CallableContext();
        }

        public Builder returnType(ReturnType returnType) {
            product.returnType = returnType;
            return this;
        }

        public Builder genericMessage(Expression genericMessage) {
            product.genericMessage = genericMessage;
            return this;
        }

        public Builder paramList(List<LocalVariableDeclare> paramList) {
            product.paramList = paramList;
            return this;
        }

        public Builder identifierReference(int identifierReference) {
            product.identifierReference = identifierReference;
            return this;
        }

        public Builder accessControl(AccessControl accessControl) {
            product.accessControl = accessControl;
            return this;
        }

        public Builder embellish(Embellish embellish) {
            product.embellish = embellish;
            return this;
        }

        public Builder type(CallableType type) {
            product.type = type;
            return this;
        }

        public Builder throwsList(List<SourceVariableDeclare.LocalType> throwsExceptions) {
            product.throwsExceptions = throwsExceptions;
            return this;
        }

        public CallableContext build(SourcePosition position) {
            assertValid(position);
            return product;
        }

        private void assertValid(SourcePosition position) {
            DeclaredContext.assertNotNull(position, product.accessControl, "accessControl");
            DeclaredContext.assertNotNull(position, product.embellish, "embellish");
            DeclaredContext.assertNotNull(position, product.type, "type");
            DeclaredContext.assertNotNull(position, product.returnType, "return list");
            DeclaredContext.assertNotNull(position, product.genericMessage, "generic message");
            DeclaredContext.assertNotNull(position, product.paramList, "param list");
            DeclaredContext.assertNotNull(position, product.throwsExceptions, "throws exceptions list");
        }


    }

    public static class IgnoreArgument extends LocalVariableDeclare {
        private IgnoreArgument() {
            super(false, false, null, null, null);
        }
    }


    // 没有abstract, 没有函数体的就是abstract

    /*public byte[] localVariableToList() {
        // TODO
        List<Byte> originData = new ArrayList<>();
        int size = localVariableTable.size();
        if (size > Short.MAX_VALUE) {
            throw new FunctionBuildException(declarePosition,
                    "Too much local variable in this function!? " + size + "????");
        }
        for (short i = 0; i < size; i++) {
            // id   类型      值
            // 2Byte     8Byte   8Byte
            originData.add((byte) ((i & 0xff00) >>> 8));
            originData.add((byte) (i & 0xff));
            // 第一次编译: 从Import表中获取类型标识符信息(文件唯一)
            // 第二次编译: Import连接到真实的类, Import表的变为真实引用
            // 是吗? 真实引用怎么搞? 引用怎么搞? 要求能从文件里的编号获取真实信息的啊, 获取到的是二进制文件啊?
            // 信息是指向一个常量池, 常量池指向一个字符串, 就是全类名
        }
        byte[] data = new byte[originData.size()];
        for (int i = 0; i < originData.size(); i++) {
            data[i] = originData.get(i);
        }
        return data;
    }*/
    public static class Serializer implements StreamSerializer<CallableContext> {
        public static final int[] HEAD_LENGTH_BITS = {8, 8, 8, 16, 16, 12, 12, 12, 12};
        public static final int HEAD_BYTE = Serializes.bitCountToByteCount(ArrayUtil.sum(HEAD_LENGTH_BITS));

        static {
            register(new Serializer());
        }

        private Serializer() {
        }

        private static final SourceVariableDeclare.LocalType.Serializer LOCAL_TYPE_SERIALIZER = StreamSerializer.get(
                SourceVariableDeclare.LocalType.Serializer.class);
        private static final ExpressionElement.Serializer EXPRESSION_ELEMENT_SERIALIZER = StreamSerializer.get(
                ExpressionElement.Serializer.class);
        private static final LocalVariableDeclare.Serializer LOCAL_VARIABLE_DECLARE_SERIALIZER = StreamSerializer.get(
                LocalVariableDeclare.Serializer.class);

        @Override
        public CallableContext in(InputStream is) {
            byte[] head;
            try {
                head = is.readNBytes(HEAD_BYTE);
            } catch (IOException e) {
                throw new CompilerFileReaderException(e);
            }
            HeadMap[] headMap = new SerializableData(head).phaseHeader(HEAD_LENGTH_BITS);
            byte accessControl = (byte) headMap[0].getValue(); // 8
            byte embellish = (byte) headMap[1].getValue(); // 8
            int type = (int) headMap[2].getValue(); // 8
            int bodyReference = (int) headMap[3].getValue(); // 16
            int identifierReference = (int) headMap[4].getValue(); // 16
            long returnTypeSize = headMap[5].getValue(); // 12
            long genericMessageSize = headMap[6].getValue();// 12
            long paramListSize = headMap[7].getValue(); // 12
            long throwsExceptionsSize = headMap[8].getValue(); // 12
            CallableContext result = new CallableContext();
            result.accessControl = new AccessControl(accessControl);
            result.embellish = new Embellish(embellish);
            result.type = CallableType.values()[type];
            result.identifierReference = identifierReference;
            result.returnType = new ReturnType(readElements(is, returnTypeSize, LOCAL_TYPE_SERIALIZER));
            result.genericMessage = new Expression(readElements(is, genericMessageSize, EXPRESSION_ELEMENT_SERIALIZER));
            result.paramList = readElements(is, paramListSize, LOCAL_VARIABLE_DECLARE_SERIALIZER);
            result.body = bodyReference;
            result.throwsExceptions = readElements(is, throwsExceptionsSize, LOCAL_TYPE_SERIALIZER);
            return result;
        }


        @Override
        public int out(OutputStream os, CallableContext src) {
            byte accessControl = src.accessControl.getByte();
            byte embellish = src.embellish.getCode();
            int type = src.type.ordinal();
            int body = src.body;
            int identifierReference = src.identifierReference;
            List<SourceVariableDeclare.LocalType> returnType = src.returnType.getTypes();
            Expression genericMessage = src.genericMessage;
            List<LocalVariableDeclare> paramList = src.paramList;
            List<SourceVariableDeclare.LocalType> throwsExceptions = src.throwsExceptions;
            SerializableData head = Serializes.makeHead(new HeadMap(accessControl, HEAD_LENGTH_BITS[0]),
                    new HeadMap(embellish, HEAD_LENGTH_BITS[1]),
                    new HeadMap(type, HEAD_LENGTH_BITS[2]).inRange(true, "callable type ordinal"),
                    new HeadMap(body, HEAD_LENGTH_BITS[3]).inRange(false, "body reference"),
                    new HeadMap(identifierReference, HEAD_LENGTH_BITS[4]).inRange(false, "identifier reference"),
                    new HeadMap(returnType.size(), HEAD_LENGTH_BITS[5]).inRange(true, "return type size"),
                    new HeadMap(genericMessage.size(), HEAD_LENGTH_BITS[6]).inRange(true, "generic message length"),
                    new HeadMap(paramList.size(), HEAD_LENGTH_BITS[7]).inRange(true, "param list size"),
                    new HeadMap(throwsExceptions.size(), HEAD_LENGTH_BITS[8]).inRange(true,
                            "throws exception list size"));
            try {
                os.write(head.data());
            } catch (IOException e) {
                throw new CompilerFileWriterException(e);
            }
            return head.length() + writeElements(os, returnType, LOCAL_TYPE_SERIALIZER) +
                    writeElements(os, genericMessage, EXPRESSION_ELEMENT_SERIALIZER) +
                    writeElements(os, paramList, LOCAL_VARIABLE_DECLARE_SERIALIZER) +
                    writeElements(os, throwsExceptions, LOCAL_TYPE_SERIALIZER);
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