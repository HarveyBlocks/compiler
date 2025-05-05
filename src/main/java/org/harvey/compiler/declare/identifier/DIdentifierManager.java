package org.harvey.compiler.declare.identifier;


import org.harvey.compiler.declare.context.ImportString;
import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO 逻辑太混乱, 需要重构
 * 重构成什么样子的呢?
 * 1. import的最后一个映射到全部
 * 2. inner class 对 outer 有不同的理解啊
 *   2.1 inner 重名者覆盖outer
 * 3. 依旧是分成两部分, declare 和 use
 * 4. declare 的 部分, inner 覆盖 outer
 * 5. 和import合并时, 不能有重复
 * 6. 依据路径进行搜索
 * <pre>{@code
 *  class Type{
 *      static Inner{
 *          static int obj;
 *      }
 *  }
 * }</pre>
 * obj 的访问方法 obj, Inner.obj, Type.Inner.obj, file.Type.Inner.obj
 *
 * <pre>{@code
 *  class Type{
 *      Inner{
 *          static int obj;
 *      }
 *  }
 * }</pre>
 * obj 的访问方法 obj, Inner.obj, Type.Inner.obj, file.Type.Inner.obj
 *
 * <pre>{@code
 *  class Type{
 *     static Inner{
 *           int obj;
 *      }
 *  }
 * }</pre>
 * obj 的访问方法 obj, 非静态的字段和方法, 不能通过路径获取
 * 非静态的类, 可以通过路径获取
 *
 * <pre>{@code
 * static class T<T> {
 *     class M<T> {}
 * }
 *
 * static void a() {
 *     T<Object>.M<Object> objectM = new T<Object>().new M<>();
 * }
 *
 * }</pre>
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-10 22:56
 */
public interface DIdentifierManager {


    IdentifierString getGenericIdentifier(ReferenceElement reference);

    ReferenceElement getReferenceAndAddIfNotExist(FullIdentifierString fullname);

    ReferenceElement getReference(FullIdentifierString fullname);

    boolean isImport(FullIdentifierString fullname);

    int getPreLength();


    boolean isImport(ReferenceElement reference);


    boolean isDeclarationInFile(ReferenceElement index);

    /**
     * read only
     */
    boolean afterRead();

    /**
     * read only
     *
     * @return null for not found
     */
    ReferenceElement getFromDeclare(String[] fullname);

    FullIdentifierString getIdentifier(ReferenceElement reference);

    int getImportReferenceAfterIndex();

    List<FullIdentifierString> getAllIdentifierTable();

    Map<String, ImportString> getImportTable();

    void canGetGenericDefineOnStructure(boolean can);

    Set<Integer> getDisableSet();
}
