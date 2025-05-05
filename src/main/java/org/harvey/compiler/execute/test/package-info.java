/**
 * <h1>版本计划</h1>
 * version 1 表达式框架
 * version 2 if-else_if-else
 * version 3 while 和 do-while
 * version 4 switch-case-default
 * version 4 break 和 continue
 * version 8 expression 初步完善OuterEnvironment(虚假数据), 初步测试
 * version 5 declare
 * version 6 for
 * version 7 return
 * version 9 ...
 * version 10 try-catch-finally
 *
 *
 * <h1>THROW</h1>
 * <pre>{@code
 *  try
 *      exp;
 *  catch(type|type|type n)
 *      exp;
 *  catch(exp)
 *      exp;
 *  finally
 *      exp;
 * }</pre?>
 *
 * <pre>{@code
 *      [00] register_catch label_to_first_catch
 *      [01] register_finally_if_exist label_to_finally
 *      [02] try_exp
 * label_to_first_catch:
 *      [03] release_label_to_first_catch ; 释放资源, 因为不需要了
 *      [04] assign_type_from_throw_exception_register_with ExceptionType1_1
 *                      将异常类型寄存器里的类型尝试赋值给ExceptionType1, 然后stack_push true(可行) false(不可行)
 *      [05] if_true_goto L1
 *      [06] assign_type_from_exception_type_register_with ExceptionType1_2
 *      [07] if_true_goto L1
 *      [08] assign_type_from_exception_type_register_with ExceptionType1_3
 *      [09] if_true_goto L1
 *      [10] assign_type_from_exception_type_register_with ExceptionType1_4
 *      [11] if_true_goto L1
 *      [12] goto L2
 * L1:
 *      [13] catch_exp_1
 *      [14] goto label_to_finally
 * L2:
 *      [15] assign_type_from_exception_type_register_with ExceptionType2_1
 *      [16] if_true_goto L3
 *      [17] goto L4
 * L3:
 *      [18] catch_exp_2
 *      [19] goto label_to_finally
 * L4: label_to_finally:
 *      [20] finally_exp
 *      [21] throw_exception_register_if_exist
 *      [22] return_if_exist
 *      [23]
 *      [24]
 *      [25]
 * }</pre>
 * <p>
 * <p>
 *
 * <h1>RETURN</h1>
 *
 * <pre>{@code
 * return exp;
 * }</pre>
 *
 * <exp>{@code
 *          [00] exp
 *          [01] if_finally_exist_goto
 *          [03] return 1; 1表示返回个数是1个
 *          [04]
 * }</exp>
 * <p>
 *
 * <h1>BREAK-CONTINUE</h1>
 *
 * <pre>{@code
 *  while(condition){
 *      exp1;
 *      break;
 *      exp2;
 *      continue;
 *      exp3
 *  }
 *  exp5
 * }</pre>
 * <pre>{@code
 * L1:
 *      [00] condition
 *      [01] if_false_goto L2
 *      [02] exp1
 *      [03] goto L2 ; break
 *      [04] exp2
 *      [05] goto L1 ; continue
 *      [06] exp3
 * L2:
 *      [07] exp5
 * }</pre>
 * <p>
 * break 和 continue 是跨block的
 * 特别注意break可以在switch使用
 *
 * <h1>
 *     DECLARE
 * </h1>
 * <pre>{@code
 * declare -> (const)?(final)? [TYPE|var] variable (=exp)? (,variable (=exp))*;
 * declare -> var(a,b,c,d) = func();
 * }</pre>
 *
 * <h1>switch-case-default</h1>
 * switch{
 *     case "":
 *     case "":
 *     case "":
 *     case "":
 * }
 * 构建 case的常量值到label的映射 的 Entry集合
 * <pre>{@code
 *      switch_case_line 6 ;
 *              ;以下6条是case , 解释器读到, 直接pc+3,
 *              ;不行, 然后 pc-2, 不行, 然后pc+1这样,
 *              ;最终 goto default;
 *      case_value_eq_goto "1"  Label1
 *      case_value_eq_goto "2"  Label2
 *      case_value_eq_goto "3"  Label3
 *      case_value_eq_goto "4"  Label4
 *      case_value_eq_goto "5"  Label5
 *      case_value_eq_goto "6"  Label6
 * Label1:
 *      exp1;
 * Label2:
 *      exp2;
 * Label3:
 *      exp3;
 *      goto L_end
 * Label4:
 *      exp4;
 *      goto L_end
 * Label5:
 *      exp5;
 * Label6:
 *      exp6;
 * default_Label:
 *      default_exp
 * L_end:
 * }</pre>
 * string可以考虑使用hash值先, 再具体值
 * hash 具体值 label
 *
 * <h1>declare</h1>
 * <p>
 * declare 和 局部变量表 问题 declare如果一趟式,
 * <p>
 * 坏处就在于无法分辨是否是一个declare的名字, 如果通过outer来判断的话, 也行吧... 但是要下两步,
 * <p>
 * handler的nexthandler的操作, 必要性不大, 每次都要递归, 调用多态, 函数变多, 反而造成冗余了,
 * <p>
 * 全面完成之后, 可以改掉
 */
package org.harvey.compiler.execute.test;