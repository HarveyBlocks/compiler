# Calculate

[TOC]

## 选择 oper

选择 operName 有多种可能,

依据上下文决定oper

identifier就直接可以了吗?

load_const_int32 001101010 把一个常量加载到stack里去, 然后来运算

load_reference_this

load_member_callable 000101010 ( 获取 this 引用中的, 编号为 00101010的成员)

... 准备callable的参数列表, 并将值计算得出, 放入stack

set_arguments 把 stack 中的值个个取出, 然后set到第一个是callable的stack元素里去

### operator distinguish and decide

```text
前一个是identifier{
     前一个是类型{
         GENERIC_LIST_PRE
     }else{
         ARRAY_AT_PRE
     }
 }
 
前一个是identifier{
     CALL_PRE
}else{
     PARENTHESES_PRE
}

(_stack boolean true for call else parentheses
[_stack boolean true for generic else array

```

加和正, 减和负, 前后看
前加加减减, 后加加渐渐, 前后看

boolean 表达式涉及的 jmp signed int32 jmp 可能还涉及 这个保存值的stack要pop多少吗?
Boolean 表达式里又不声明新的值, 所以不用担心啊

## bool 表达式的提前跳出

### &&

> bool x = a && b:


分析:

```text
t=a
if(!t) t=b
result = t
```

```text

[32] load_variable_table                 000100101 (a offset) 1      (bool_type_size)
[33] ifn_stack_top                       jmp                  +2
[33] stack_pop
[34] load_variable_table                 000100102 (b offset) 1      (bool_type_size)
[36] assign_stack_top_to_variable_table  000100103 (x offset) 1      (bool_type_size)
[36] stack_clear                         ;一个表达式结束就clear一下, 逗号也clear一下


```

### ||

> bool x = a || b:


分析:

```text
t=a
if(t) t=b
result = t
```

```text
[42] load_variable_table                 000100101 (a offset) 1      (bool_type_size)
[43] if_stack_top_jmp                    +2
[43] stack_pop
[44] load_variable_table                 000100102 (b offset) 1      (bool_type_size)
[46] assign_stack_top_to_variable_table  000100103 (x offset) 1      (bool_type_size)
[46] stack_clear                         ;一个表达式结束就clear一下, 逗号也clear一下


```

### 混合使用

> bool x = exp1 || exp2 && exp3 || exp4:

分两步?
分析:

```text
t=exp1
if(!t) {
     t=exp2
     if(t)t=exp3
}
if(!t) t = exp4
result = t
```



```
[52] stack_push_calculate                exp1
[53] if_stack_top_goto                   ->L1
[54] stack_pop
[55] stack_push_calculate                exp2
[56] ifn_stack_top_goto                  ->L2
[57] stack_pop
[58] stack_push_calculate                exp3
[59] L2:
[60] L1:
[61] if_stack_top_goto                   ->L3
[63] stack_pop
[64] stack_push_calculate                exp3
[65] L3
[66] assign_stack_top_to_variable_table  000100103 (x offset) 1      (bool_type_size)
[67] stack_clear                         ;一个表达式结束就clear一下, 逗号也clear一下
goto->jmp
[52] stack_push_calculate                exp1
[53] if_stack_top_jmp                    ->59
[54] stack_pop
[55] stack_push_calculate                exp2
[56] ifn_stack_top_jmp                   ->59
[57] stack_pop
[58] stack_push_calculate                exp3
[59] if_stack_top_jmp                    ->62
[60] stack_pop
[61] stack_push_calculate                exp3
[62] assign_stack_top_to_variable_table  000100103 (x offset) 1      (bool_type_size)
[63] stack_clear                         ;一个表达式结束就clear一下, 逗号也clear一下
load_reference Type.Filed
load_reference



```
### 三元运算符

> a = b?c:d?e?f:g:h?i:j;
```java
class Condition {
    void condition() {
        // 用了贪心, 懒汉
        a = b ? c : d ? e ? f : g : h ? i : j;
        // ? stack.push(new "jmp L?"(LabelName))
        // : stack.pop().getLabel();
        // 有些东西啊, 需要stack, 有些东西似乎不需要
        // 有逗号的, 需要stack, 没逗号的, 不需要
        // emmmm
        // 一个表达式如果含有复杂表达式, 就把复杂表达式分成单独的表达式
        // 这个单独的表达式按照逗号分割
        // 那么, 所有的子表达式都没有`,` 了, 那么所有的都可以用一个t来进行运算
        // `,` 之间可以用stack来存储结果
        a = b ? c : (d ? (e ? f : g) : (h ? i : j));
        t = b;
        if (t) { // 读到?
            t = c;
        } else { // 跳到下一个:
            t = d;
            if (t) {
                t = e;
                if (t) {
                    t = f;
                } else {
                    t = g;
                }
            } else {
                t = h;
                if (t) {
                    t = i;
                } else {
                    t = j;
                }
            }
        }
        result = t;
    }
}
```
## 多返回值

参考C sharp:

```C#
public static (int, bool) Execute() {
return (1, true);
}

public static void Use() {
var (item1, item2) = Execute();
var valueTuple = Execute();
item1 = valueTuple.Item1;
item2 = valueTuple.Item2;
(int, bool) valueTuple2 = Execute();
}
```