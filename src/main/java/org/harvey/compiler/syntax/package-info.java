package org.harvey.compiler.syntax;
// exp ? exp : exp
// 逻辑更清晰:
//       ?
//    /      \
// condition   :
//            /  \
//       on_true   on_false

// 更符合优先级算法:
//              :
//          /      \
//        ?         on_false
//    /      \
// condition  on_true
//
//
//
//

// 逻辑表达式到if的转变, ? : 同样有只执行一边的作用
//  a&&b
//  a==false?false:b
//              :
//          /      \
//        ?         b
//    /      \
//   !       false
//    \
//      a
//
//  a||b
//  a==true?true:b
//
//              :
//          /      \
//        ?         b
//    /      \
//  a       true


// 括号和函数调用, path和get_member
//


// 复杂的表达式
// array init
// a = {1,2,3,4,5}
//
// 本质是链表
//      =
// a       array_init
//             ,
//          1`     ,
//              2     ,
//                  3    ,
//                     4   5
//
// new_instance
//
// 读到关键字 new 之后, 往下再读一个类型/读到括号
//      new
//  type    arguments
//  如果没有 type, 那就审查上下文, 看看能不能分析出type
// 如果有, 那pass
//
// structure clone:
//


// 控制结构转化成抽象语法树, 例子如下:
// 顺序结构:
//  ;;;;
//
//                  ;
//          ;           declare
//      ;       break    type   exp
//  exp     return
//              exp

// 分支结构:
//                           else
//                        /      \
//                      else     block
//                  /          \
//              if              if
//           /    \          /      \
//   condition     block  condition  block
//
// switch-case 不好搞!
// 就是switch=case表[condition:jmp]
//
//


// 三种表达式 TYPE-EXPRESSION-CONTROL
// 他们各自互相一列
// CONTROL里用TYPE解析DECLARE; 用EXPRESSION解析CONDITION和EXPRESSION
// EXPRESSION 用TYPE解析NEW, CAST, GENERIC;用CONTROL解析LAMBDA
// TYPE完全独立不需要它俩
// 需要一个IdentifierManager
//      1. 将static path 合并
//      2. 将identifier 的 前缀补全
//      3. identifier 转 reference
//      4. local_variable_table
//      5. 辨析是否从"org"开始的全包名
//      6. import
//      7. file_declare
//      8. 是类型的话, 返回类型
// 用这个IdentifierManager, 发现一个identifier是type, 就转换成TYPE
// 需要一个解析结束的条件,
//  例如所有的TYPE解析都是先得知IDENTIFIER, 再得知后面的GenericList的,
//      这其实很复杂, 毕竟不能用. 后面可能是成员, 可能是静态的内部类
//  比如expression是无lambda嵌套下的';'
//  比如condition是没有被'('匹配的')'
//  control是没有被'{'匹配的'}'
// 为了避免递归转换, 我认为应该control
//  while(sourceIterator.hasNext()){
//      handler = handlerMap.get(condition);
//      nextSource = sourceIterator.next(); // 当然, sourceIterator,stack可以封装进一个context里面
//      if(handler.isBreakCondition(nextSource,stack)){
//          condition = handler.out();
//      }else if (isCondition) {
//          condition = handler.handle(nextSource,stack); // 可能会改变, 例如读到一个类型之后可能希望能改变拎一个类型
//      }
//  }
//
