package org.harvey.compiler.declare.identifier;

import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.NormalOperatorString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.expression.ReferenceType;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * 更进, name 指向field的索引, callable的索引, inner class的索引. alias的索引, 如何?
 * 构造:
 * 1. 文件级别的, 直接存储自己在文件中的索引, 没有outer
 * 2. class 是 中继, field, callable, alias 是端点, 端点绝无可能含有inner
 * 3. field 由于声明的复杂性, 存在一个声明有多个field的情况
 * 3.1. 在field所在类里建立索引表, 例如第一个声明有2个field, 第二个声明有3个field, 第三个声明有1个field, 就建立表
 * start
 * 0
 * 2
 * 5
 * 要多存储一张表
 * 3.2 存储 index_of_declare, offset两个字段
 * - 可读性更强, 检索速度快
 * - 要存储两个字段, 在identifier pool 处消耗空间
 * 4. callable 由于可以重名, 有两种方案:
 * 4.1. list存储所有索引
 * 4.2. callable按名称排序, 然后存储start, offset两个字段, 或设计reference指针, 蕴含这两个字段(同理)
 * - 可读性更强, 检索速度快
 * - 要存储两个字段, 在identifier pool 处消耗空间
 * 4.3. callable存储一张start表, 然后reference指向start表中的同名callable的位置
 * - 在reference存储效率高
 * - 需要访问一次表
 * 5. 否则,自己的outer一定是个类, 而outer一定会在这个pool里, 指向这个outer在pool里的identifier fullname
 * 6. 在获取outer的对象, 即可获取索引
 * 7. 由于不同的端点需要不同的, 对reference的解释, 所以需要一个字段, 标注这个identifier是个啥...?!非常悲伤, 这消耗空间
 * - 为了减少空间的浪费, 可以使用重构这个 int reference 在 bit上的解释 也就是 位图
 * - 在内存阶段是否就需要这么考虑? 还是说, 内存以效率优先, 所以就增加字段?
 * -   Fullname, DeclareType, Reference, OuterReference
 * - OuterReference: (nullable or 0 表示no outer,其他情况下, 要获取outer就要-1获取真索引?)
 * 指向pool中的元素,
 * - Fullname, 对于是否需要按Dot拆分, 保持怀疑
 * - DeclareType, complex-structure, file, package, field, method_or_function, constructor, operator, cast
 * 大概需要4bit, 剩下的7个, 可以用作拓展, 还蛮占用空间的
 * - Reference, 简单的索引, 16bit比较合适了, 因为outer不会给成员超过16bit, 还是说20bit, 和4bit凑24bit?
 * 由于指向的的是中间表, 所以要么就12bit, 也就是说, outer中的表项也只能有12bit吗?
 * 使用:
 * 1. 先获取到outer的reference,如果outer没有outer了(就是文件了), 进入3
 * 2. 返回1
 * 3. 获取自己的索引, 从对文件对象中获取自己的对象
 * 4. 将获取到的自己的对象往回传递, inner可以根据这个对象获取inner的对象
 * 5. identifier pool 只有一个存储的功能, 所以需要一个Manager来解释identifier pool 中的索引的意义
 * 6. 由于有manager来处理identifier pool中的逻辑, 那么就要跟进identifier manager的结构
 * - identifier manager, 由于把本文件内declare和import混为一谈, 所以解析极为痛苦!
 * - 既然有了manager, 那就分开存储几个pool吧,
 * - 由于field要注意检查不能向前引用, 虽然能在pool里找到field, 但是位置在后面的field不能获取, 非法向前引用
 *      也就是说, field的解析是有序的, 不允许循环依赖
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-25 16:21
 */
public class IdentifierPoolFactory {
    private static final int NO_OUTER = 0;
    private final List<DeclareIdentifierString> pool;
    private final Map<String, Integer> aliasMap = new HashMap<>();
    private final Map<String, List<Integer>> callableMap = new HashMap<>();
    private final Map<String, Integer> fieldMap = new HashMap<>();
    private final Map<String, Integer> innerComplexStructureMap = new HashMap<>();
    private int outerReference;

    public IdentifierPoolFactory(IdentifierPoolFactory outer, int outerReference) {
        this(outer.pool, outerReference(outerReference));
    }

    public IdentifierPoolFactory(List<DeclareIdentifierString> pool, int outerReference) {
        this.pool = pool;
        this.outerReference = outerReference;
    }

    public IdentifierPoolFactory() {
        this(new ArrayList<>(), NO_OUTER);
    }

    private static int outerReference(int outerReference) {
        return outerReference + 1;
    }

    private static List<Integer> computeOnCallableMap(int reference, List<Integer> callables) {
        if (callables == null) {
            callables = new ArrayList<>();
            callables.add(reference);
        } else {
            callables.add(reference);
        }
        return callables;
    }

    public ReferenceElement addFile(String[] packages, String filename) {
        if (outerReference != NO_OUTER) {
            throw new CompilerException("outer exists,then it should obey the `outer`'s packages and filename");
        }
        for (String each : packages) {
            pool.add(new DeclareIdentifierString(NO_OUTER, SourcePosition.PROPERTY, each,
                    DeclareIdentifierString.DeclareType.PACKAGE, DeclareIdentifierString.MEANINGLESS_OBJECT_REFERENCE
            ));
        }
        outerReference = pool.size();
        pool.add(new DeclareIdentifierString(NO_OUTER, SourcePosition.PROPERTY, filename,
                DeclareIdentifierString.DeclareType.FILE, DeclareIdentifierString.MEANINGLESS_OBJECT_REFERENCE
        ));
        return new ReferenceElement(SourcePosition.PROPERTY, ReferenceType.IDENTIFIER, outerReference);
    }

    public ReferenceElement addIdentifier(
            SourcePosition position, String name,
            DeclareIdentifierString.DeclareType type,
            int objectReference) {
        int reference = pool.size();
        if (duplicateName(name, type, reference)) {
            pool.add(new DeclareIdentifierString(outerReference, position, name, type, objectReference));
        } else {
            throw new AnalysisDeclareException(position, "identifier has declared");
        }
        return new ReferenceElement(position, ReferenceType.IDENTIFIER, reference);
    }

    private boolean duplicateName(String name, DeclareIdentifierString.DeclareType type, int reference) {
        switch (type) {
            case COMPLEX_STRUCTURE:
                if (aliasMap.containsKey(name) ||
                    callableMap.containsKey(name) ||
                    fieldMap.containsKey(name) ||
                    innerComplexStructureMap.containsKey(name)) {
                    return false;
                }
                innerComplexStructureMap.put(name, reference);
                return true;
            case ALIAS:
                if (aliasMap.containsKey(name) ||
                    callableMap.containsKey(name) ||
                    fieldMap.containsKey(name) ||
                    innerComplexStructureMap.containsKey(name)) {
                    return false;
                }
                aliasMap.put(name, reference);
                return true;
            case FIELD:
                if (aliasMap.containsKey(name) ||
                    callableMap.containsKey(name) ||
                    fieldMap.containsKey(name) ||
                    innerComplexStructureMap.containsKey(name)) {
                    return false;
                }
                fieldMap.put(name, reference);
                return true;
            case FUNCTION_OR_METHOD:
                // 其余全部不重名, 方法可以重名
                if (aliasMap.containsKey(name) ||
                    fieldMap.containsKey(name) ||
                    innerComplexStructureMap.containsKey(name)) {
                    return false;
                }
                callableMap.compute(
                        name,
                        (k, v) -> computeOnCallableMap(reference, v)
                );
                return true;
            case PACKAGE:
            case FILE:
            case CONSTRUCTOR:
            case OPERATOR:
            case CAST:
                throw new CompilerException("type: [" + type + "] should not check on duplicating name");
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public ReferenceElement operatorCallable(SourcePosition position, Operator operator, int objectReference) {
        // type name outer-refer
        this.pool.add(new DeclareIdentifierString(outerReference, position, operator,
                DeclareIdentifierString.DeclareType.OPERATOR, objectReference
        ));
        return ReferenceElement.of(new NormalOperatorString(position, operator));
    }
}
