package org.harvey.compiler.declare.identifier;


import org.harvey.compiler.execute.expression.FullIdentifierString;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.execute.test.version1.env.OuterEnvironment;

import java.util.List;

/**
 * TODO
 * 依据import和当前的declare, 判断得出这个full_path的完整是什么
 * 如果无法判断, 认为整串是从base_package开始的,
 * 这里就有一个问题在于target和source来判断包, 不过可以用file cache, 这就容易了
 * 要找declare, 可以用当前"environment.path+path.start"来获取declare,
 * 如果失败了, 可以用"environment.path.remove_last() + path.start"来获取, 反复做, 知道可以获取,
 * 不行的话直接进入base_package判断
 * <p>
 * 由于import是基于Map的, 效率更高, 所以import在第一步判断
 * 1. Import
 * 2. 循环往前获取declare
 * 3. base_package
 * 4. 最抽象的一点就在于generic了, generic或许可以放在environment里比较合适, 如果放在identifier manager, 那么分级困难
 * 5. environment创建新的(内部的)的时候, 可以依据当前是否是静态类来选择是否可以向前查询generic
 * 6. 其实在获取到reference之后, 还需要检查能否获取,
 *      访问控制权限
 *      子类不能继承非sealed的问题
 *      有 static 不能调用non_static,
 *      const不能调用非const函数
 *      由于related_raw_type的嵌套结构过于复杂, 要遍历检查也很复杂, 所以何不使用pool, 然后用cur来引用, 这样是否可以方便一点
 * 7. 既然如此, 思考一下, 建立了关系之后的related-raw-type, 如何检查能否继承/调用, 是每次结束一个继承后检查, 还是每次调用都要检查能否调用?
 * 8. 还有alias, alias是需要构建了outer才能引用的
 * 9. 获取alias, 加载alias的outer
 * 10. 我一直纠结与, 是否要接在一些源码资源来进行分析, 虽说目前进行懒加载, 那么, 加载入多少数量的资源算多了呢?
 * 11. 一个文件涉及的类资源有限, 那我能不能先用import, 加载其他文件, 然后再加载最终这个文件呢?
 * 12. 也就是说, 目标是明确, 什么样的懒加载是好的懒加载, 懒加载一部分之后, 是否需要放回
 * 13. 以下是一个思路:
 *      一个文件, 引入资源, 发现是第一步编译, 改变stage, 标记为"引用的发起者", 然后读取所有import加入queue, 本类加入stack
 *      遍历import,
 *          import指向一个"已完成的编译", 递归出口
 *          import指向一个"没有import"的文件, 将本文件完成全部编译, 递归出口
 *          import指向目录, 编译目录下所有文件
 *          import指向文件/文件内类/类内静态成员, 将文件标记为"引用的发起者", 所有import加入queue;
 *          import指向"引用的发起者", 保留双方到queue2, 继续编译其他import,
 *          import全部解析完毕, 发现
 *     问题在于, import只是一个名字, 用于补全名字的, 似乎不适合用来指示解析
 *     是否需要将函数体全部解析后, 发现import的内容的所有引用(此时引用一定指向类/函数/类内成员), 然后再进行链接?
 *     但有一个问题在于, 是否有计数, 再将函数体解析完毕之后, 我依旧有能力检查函数体经过链接后的语法问题?
 *     然而事实上, 似乎只需要有声明就可以完成链接了,
 *     解析一点函数体, 让函数体的所有标识符变成引用, 然后再解析第二阶段会比较好吗?
 *     所以是
 *     1. 解析声明 2. 建立类型关系 3. 检查声明正确性, 5. 检查表达式的正确性
 *     是否需要初步解析函数体, 初步解析函数体对解决循环依赖是否有帮助
 *     或许是没有帮助的, 依据全类名, 我们获取类型, 然后建立related, 这能够完成, 说明不需要import
 *     但如果没有补一句import, 那么在建立关系之后, 是否需要释放资源, 还是依旧保存缓存?
 * 14. 依旧思考, 如何处理"建立类型关系"和"检查类型能否继承"两个问题, 是建立起链接之后马上检查, 还是全部建立之后逐个检查?
 *      建立起的关系怎么构建, 才能方便检查?
 *      思路1. 放到queue里, 然后检查
 *      这里就会产生一个问题: 检查内部类的继承关系的时候是否需要建立外部类的继承关系?
 *          如果加载, 那么就会加载更多的类, 而这些类不会被用到
 *          如果不加载, 那么就需要在产生需求之后加载,
 *              这样就导致代码产生了嵌套,
 *              如果要拆开嵌套, 就要stack+循环,
 *              这将导致代码更加混乱, 而且难以检查程序的正确性
 * 15 整理问题: 解析完关系后是否释放资源. 是否为了极致的懒加载, 而放弃代码的可读性和可维护性?
 *          如果要极致的懒加载, 那么代码将非常复杂, 在检查的过程中发现需要建立关系, 那么需要将当前任务全部存入callback
 *          然后等待关系重新建立之后, 再调用Callback检查关系是否成立的
 *          那么要存入callback,要么匿名内部类, 要么建立一个类来存储当前信息
 *          而且更复杂的是, 要解析outer的几个类型, 然后再建立关系吗.....好累...
 *          而且有alias混入其中增加了代码的复杂度
 *          一个类型继承了alias->alias在内部->报错
 *                          \->存入
 *          复杂的是要解析alias到origin, 这个问题在于没有处理好check的问题, 如果check在整个继承树建立之后进行, 或许会方便一些
 *          1. 建立整个树
 *          2. 检查 static, 要有outer资源
 *          3. 建立访问控制, 要有outer资源
 *          4. 检查sealed, 不需要outer资源, alias需要class, 可以用并查集, 将是否origin复制到各个alias上, 但是这种被动的方式, 合适吗? 会不会出bug?
 *          5. 建立outer的有关树, 后续加入callback, check继续无法完成, 又要建立树, 然后再继续增加阶段, 来避免重复检查
 *          6. 在声明阶段就reference, 显然是不合适的, 因为需要知道是否是org, 从头开始
 *                  声明阶段, 把声明加到reference里去就好了
 *                  GenericDefine 就不加到IdentifierPool里去了, 因为它的逻辑更类似于局部变量
 * 16. 如果有一种好方法, 可以对初步编译后的Executable进行检查就好了
 *     Executable的检查, 主要是Expression的检查, 控制结构没什么好检查的
 *     有些东西, 不知道了, 是不是就不能编译了?
 *     Expression的检查, 除了运算符和运算的item之间的表达式, 就只有类型的检查了
 *     而类型的检查是需要链接的, 如何对一个sequential进行类型检查?
 *     a && b
 *         load "a"
 *         if_false_goto L1 ;这是一个消耗栈顶元素的命令, 需要栈顶元素是bool值
 *         ; 如果我不是顺序执行下来的, 而是goto到这一行的呢? 那么将不能进行检查
 *         load "b"
 *         jmp       +1
 *     L1:
 *         const false ; 这一整个结构返回bool值
 *     ...
 *     对Expression的解析, 难道必须到link阶段吗?
 *     在link阶段对类型进行检查, GetMember 和 StaticPath, 函数的确认, 那就不能抛弃importTable
 *     函数还要考虑到父类的函数
 * 17. 更进IdentifierPool
 * 18. IdentifierManager的需要完成的任务
 *      1. 本文件import
 *      2. 本文件declare
 *      3. 找出最开始的package, 例如org和路径
 *      - 准备一个最开始路径检查器吧, {@link org.harvey.compiler.io.PackageMessageFactory} 感觉还不够
 *          PackageMessageFactory 的 功能感觉太耦合了, 承担了太多的功能
 * 19. 加载文件
 *      1. 全部加载完毕? 还是有需要时才加载?
 *      2. 我认为全部加载的好
 *      3. 如果只编译一个文件的时候, 这个文件链接了其他文件, 不应该去加载其他文件
 *      4. 多线程地, 独立编译所有文件到独立阶段, 然后存入target文件夹下, 然后所有内容都可以从target找了
 *      5. 也就是说, 不要使用懒加载
 *      6. 要加载,
 *          - 解析第一个文件, 就载入所有文件的第一阶段信息, 如何呢? 全部载入之后建立联系
 *          - 如果解析一个文件, 载入所需的文件, 那么, 如果这个文件为开始的一整个闭包全部解析完毕后, 还有一部分文件没有解析到建立联系这个阶段
 *          - 如果是这样, 就释放所有这个闭包内的元素, 然后重新解析另外的文件吗?
 *          - 如果A和B不是继承关系, 而是依赖关系, 那么, 在上面的闭包中, 两者没有关系, 但是释放了资源将导致来回
 *          - 解析表达式阶段倒是可以完全独立,
 *      7.
 *          完全独立的声明解析阶段->链接类->解析声明的类型/签名->完全独立的实现解析阶段
 *      8. 解析声明的类型也是完全独立的吧?
 *      9. 全部加载吧...
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-13 20:06
 */
public interface IdentifierManager {

    FullIdentifierString get(ReferenceElement reference);

    /**
     * 从inner到outer
     */
    ReferenceElement getDeclareReference(OuterEnvironment environment, IdentifierString string);

    /**
     * 需要其他字段另外实现
     * 1. 在函数上, 函数的generic define
     * 2. 当前类的generic define
     * 3. 没generic, 若不是static, 向外
     */
    ReferenceElement referGeneric(IdentifierString string);

    /**
     * 是 import 则 refer, 然后指向后面的
     */
    IdentifierString[] getImport(IdentifierString string);


    boolean isImport(IdentifierString string);


    List<FullIdentifierString> getPool();

    void addPool(FullIdentifierString identifierString);


}
