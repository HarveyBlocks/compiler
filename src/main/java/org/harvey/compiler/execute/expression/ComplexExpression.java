package org.harvey.compiler.execute.expression;

import org.harvey.compiler.io.ss.PolymorphismSerializable;
import org.harvey.compiler.io.ss.PolymorphismStreamSerializer;
import org.harvey.compiler.io.ss.StreamSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 *
 * @date 2025-01-08 16:49
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public class ComplexExpression extends PolymorphismSerializable {
    protected ComplexExpression() {
    }
    public static enum Type{
        ARRAY_INIT,
        STRUCT_CLONE,
        LAMBDA

    }    // 1. Struct
    //      是new IDENTIFIER(obj){
    //      是new(obj){
    // 2. Lambda -> 已经在上面考虑, 故排除
    // 3. ArrayInit
    //      是 = {
    //      是 , {
    //      (不太可能, 因为已经包含到上面去了)是 {{
    //      Array<int> arr = {1,2,3,4};  // 怎么限定长度呢?
    //      Array<int> arr = [5]{1,2,3,4};
    //      Array<int> arr = new Array<>(4); // 容量
    //      Array<int> arr = new Array<>(4,2); // 容量, 默认值
    //      Array<int> arr = new Array<>(4,index->index++); // 容量, 初始化函数
    //      Array<int> arr = {4,3,2,1};
    // 4. 多维数组怎么声明
    //      Array<Array<int>> arr = new Array<>(4,index->new Array<>(index)); // 锯齿数组
    //      DimensionArray<int> arr = new DimensionArray<>({3,4},0); // 多维数组,3行, 4列
    //      形如:
    //      {{1,2,3,4},
    //      {e,e,e,e},
    //      {a,x,f,g}}
    //      Array<Array<int>> dArr = {{1,2,3,4}, {e,e,e,e},  {a,x,f,g}}
    //      DimensionArray<int> dArr = {{1,2,3,4}, {e,e,e,e},  {a,x,f,g}}; // 报错, 异常
    // 4. 不合法
    //       ....

    // 对于Lambda的STC是可执行语句, 需要作为函数body进行分析
    // 对于StructClone的STC是expression, 可迭代
    // 对于的ArrayInit的STC是expression, 可迭代
    // 对于迭代, 如何防止递归?
    // 1. 延迟检查, 到下一轮, filter出ComplexExpression的子类, 然后抽出, 然后分析
    // 函数(){
    //      ()->{
    //          ()->{
    //
    //          }
    //      }
    // }
    // 函数分析器(){
    //   函数.表达式.for(item){
    //      List sonExpressions;
    //      while(true){
    //          List expression = 表达式分析器.分析(item);
    //          expression.for(son){
    //              if(是ComplexExpression){
    //                  sonExpressions.add(son);
    //              }
    //          };
    //      }
    //   }
    // }
    // 可以让所有的lambda表达式指向函数之外的一片Executable空间
    // 和其他函数属于一个层, 命名可以用$$之类的


    // 函数body的困难:
    // argument+变量声明+变量赋值表达式+控制结构


    private static final StreamSerializer<ComplexExpression> SERIALIZER = StreamSerializer.get(
            ComplexExpression.Serializer.class);
    @Override
    public int out(OutputStream os) {
        return SERIALIZER.out(os, this);
    }


    public static class Serializer extends PolymorphismStreamSerializer<ComplexExpression> {
        private static final Map<Integer, StreamSerializer<? extends ComplexExpression>> MAP = new HashMap<>();

        static {
            StreamSerializer.register(new Serializer());
        }

        private Serializer() {
        }

        public static void register(int code, StreamSerializer<? extends ComplexExpression> serializer) {
            PolymorphismStreamSerializer.register(MAP, code, serializer);
        }

        @Override
        public ComplexExpression in(InputStream is) {
            return PolymorphismStreamSerializer.in(MAP, is);
        }
    }
}
