package org.harvey.compiler.declare.phaser.phaser;

import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.core.AccessControls;
import org.harvey.compiler.analysis.core.Permission;
import org.harvey.compiler.analysis.text.context.SourceTextContext;
import org.harvey.compiler.common.util.Singleton;
import org.harvey.compiler.declare.Declarable;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.declare.EmbellishSourceString;
import org.harvey.compiler.declare.context.ValueContext;
import org.harvey.compiler.declare.phaser.visitor.Environment;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.ExpressionFactory;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-12-06 22:27
 */
public class ValuePhaser implements DeclarePhaser<ValueContext> {
    public static final Singleton<ValuePhaser> SINGLETON = new Singleton<>();

    private ValuePhaser() {
    }

    public static ValuePhaser instance() {
        return SINGLETON.instance(ValuePhaser::new);
    }

    @Override
    public ValueContext phase(Declarable declarable, int identifierIndex, Environment environment) {
        // TODO
        //
        //  局部变量要能支持static, 表示只加载一次, 用来帮助加载函数内定义的类和lambda!
        //  考虑到逻辑, lambda里可能有对外部局部变量的引用
        //  所以, 如果lambda被修饰了static, 其引用的局部变量也应该修饰static的
        //  程序员可以
        //  Func<int> a(int num){
        //      static class Num{
        //           public int num;
        //           public Num(int num){
        //                  this.num = num;
        //          }
        //      }
        //      static Num n = new Num(num);
        //      n.num = num;
        //      static Func<int> result =  new Func<int>(){
        //                   return n.num; // 每次, 这个n都是指向外面那个类产生的对象
        //                  // 那个对象只会被实例化一次
        //                  // 但是n.num会被执行多次, 所以, num会被改变
        //                  // 全矣!
        //      };
        //      return result; // 返回值返回一个static, 外部应该接收到什么呢?
        //      // 如果返回一个非static的定义的类, 或者函数, 该怎么办呢?
        //  }
        //  bool(*p)(int,int) =  [](int a,int b){return a>b;};
        //  callable<bool,(int,int)> = [](int a,int b){return a>b;};
        //  还是要准备多个函数参数类型吗....泛型类型是怎么体现多个个数的呢?
        //  那么问题来了, 有这个功能, 又要怎么实现呢?
        //  怎么判断这个是static了, 还是没有static呢?
        //  🤔, 要不就是执行了 static 的一段逻辑, 执行完之后, 从代码块中删除, (or 在代码块之间加入goto字段啦)
        //  第二次运行直接赋值成第一次初始化时的结果...
        //  是不是还要准备一个static表, 用于存储在函数中的static初始化之后的
        //   class Tuple<T...,L extends Collection<Object> =List<Object>>{
        //        L value;
        //        public Tuple(T... value){
        //            // 在函数内是object[], 在函数外, 那就要参与编译检查
        //            value = new L(value);
        //        }
        //   }
        //  问: int和long这种....? 咋办呢? 在T... 中进行装箱吗? 那么在一般的地方呢? 进行装箱吗?
        // void f(){
        //      Tuple<int, long> tuple = new Tuple(1,2L);
        //      // int和long做编译的检查, 然后这个组合注册到Tuple类的字节码对象里, 返回token
        //      // Tuple的klass地址和token共同组成tuple对象的头,
        //      // 拿着klass地址获取到Tuple对象,
        //      // 拿着token, 获取到注册在Tuple字节码对象中的对应泛型(泛型要不要查重啊?)
        //      hash_table<list<Klass*>> // 红黑树会不会更佳? 只要给出hash值就好了...
        // }
        return new ValueContext.Builder().embellish((phaseEmbellish(declarable.getEmbellish(), environment)))
                .accessControl((phasePermission(declarable.getPermissions(), environment)))
                .type(ExpressionFactory.type(declarable.getType())).build(declarable.getStart());
    }

    protected AccessControl phasePermission(SourceTextContext permissions, Environment environment) {
        switch (environment) {
            case FILE:
                throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                        permissions.getLast().getPosition(), "field can not in file");
            case ENUM:
            case CLASS:
            case ABSTRACT_CLASS:
                return AccessControls.buildMemberAccessControl(permissions, Permission.PRIVATE);
            case ABSTRACT_STRUCT:
            case STRUCT:
                return AccessControls.buildMemberAccessControl(permissions, Permission.PUBLIC);
            case INTERFACE:
                AccessControl accessControl = AccessControls.buildMemberAccessControl(permissions, Permission.PUBLIC);
                if (!accessControl.canPublic()) {
                    throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                            permissions.getLast().getPosition(), "only public is allowed in interface");
                }
                return accessControl;
            default:
                throw new CompilerException("unknown environment");
        }
    }

    protected Embellish phaseEmbellish(EmbellishSourceString embellish, Environment environment) {
        if (environment == Environment.FILE) {
            throw new CompilerException("field can not in file");
        }
        DeclarePhaser.forbidden(embellish.getAbstractMark());
        DeclarePhaser.forbidden(embellish.getSealedMark());
        // 可选 static const final
        // struct/abstract 非静态默认 const final
        // interface 默认 static final
        return new Embellish(embellish);

    }
}
