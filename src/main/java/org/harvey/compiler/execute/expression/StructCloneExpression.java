package org.harvey.compiler.execute.expression;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @date 2025-01-08 16:50
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@Getter
public class StructCloneExpression extends ComplexExpression {
    public static final String END_MSG = "__array__init__end__";
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
    private final boolean start;//不是start, 就是end
    @Setter
    private int otherSide;

    public StructCloneExpression(boolean start) {
        this.start = start;
    }
}
