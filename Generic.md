# 泛型

## 粗糙的思考

分为for use/for declare.
for declare, `<X,X extends org.M.Y>` .
for use , `<org.M,org.M.Y,_ extends org.X>`
for use , `<org.M,org.M.Y,_ super org.X>`
for use , `<org.M,org.M.Y,_ super org.X>`

1. for declare
    - 允许`extends`
    - 不允许`super` why不允许? 因为这样就不能明确
    - 不允许 `T[]`
    - `extends` 代表上界, 即包括本身的所有子类, 或者说, 是能够自动转换成该类型的所有元素
    - `<T extends Number & Comparable<?>>` 继承且实现接口, 这样T就能作为一个暂时的类代表
2. for use
    - _ extends
    - _ super
    - super 包含本类的所有父类/父接口
    - extends 不包含本类的所有子类
    - `<? extends Number & Comparable<?>>` 不被允许 why不被允许?, 因为这样从类中取出数据, 就不能明确是什么类了
    - `<? extends Number & super Integer>` 作为返回值时的检查, 作为参数的检查
    - `<>`
      为什么要用? `extends`, 这样org.X作为子类就不能用了
      无论什么样子的泛型, 都要考虑两个问题:
3. 泛型能否转换成这个类型的判断
4. 类型能否转换成这个泛型的判断
5. 通配符能否转换成声明泛型
6. 声明泛型能否转换成通配符

做减法:

1. 通配符
    1. 完整版: `<? extends BaseClass & BaseInterface & super LowerClass & LowerInterface>`
       由于一个类不能是一个类的父类且是一个接口的子类,
       结构能单一继承结构, 也能实现接口, 这里结构和类完全一致
       枚举不能有子类, 枚举的唯一父亲的Enum类, 可以实现接口
       接口只能继承一个接口
2. 定义
    1. 完整版: `<T extends BaseClass & BaseInterface & super LowerClass & LowerInterface>`
3. 对于泛型内部, super的部分不能和extends矛盾
4. 对于任意一个类型, 都能
   or 直接删除通配符的概念...
5. 不支持通配符(参考C#)
   `<T extends BaseClass & BaseInterface & super LowerClass & LowerInterface>`
6. 思考如何规范构造器
    1. new T(); 重载运算符new, 让子类都重载运算符new
    2. 分析结构分析, 然后给泛型加信息, 说, 就是需要这些方法, 然后再检查声明
    3. 构造器不能继承, 如何规范...
       重载new运算符?

## 泛型参数定义语法设计

### 示例

`<T extends BaseClass & BaseInterfaces & super LowerClass & new<int> & new<long>>`

`<T extends BaseClass & BaseInterfaces & super LowerInterface & new<int,int> & new<long> = DefaultType >`

1. `extends`限制泛型的上界
    1. `BaseClass`表示继承, 可以有0个或1个
    2. `BaseInterface`表示实现的接口, 可以有0个或多个
    3. `BaseInterface`必须在`BaseClass`之后
    4. 每个类或接口前都可以加`extends`, 最后回作为集合合并, 可以出现重复的, 只不过会被无视或者extends后面跟着多个
2. `super`限制泛型的下界
    1. `LowerClass`的父类是T, 或`LowerClass`实现了T接口, 可以有0个或1个
    2. `LowerInterface`继承了T接口, 可以有0个或1个
    3. `LowerClass`或者`LowerInterface`二选一
3. `new<...>` 表示泛型需要含有的构造器, 中括号中表示构造器的参数列表
4. `DefaultType` 表示默认的泛型类型, 必须在泛型列表的最后, 可以有0个或多个

## 泛型的自洽

1. T.Lower<=T.Upper

   对于任意i, T.Lower<=T.Interfaces[i]. 见[类型转换](#带泛型的类型之间的转换)
2. DefaultType必须能转换成T. 见[类型转换](#泛型参数和普通类型可能带泛型之间的转换)

## 类型转换

### 带泛型的类型之间的转换

要使得 FROM<T [define of T]> 转换成 TO<T [define of T]>

即`FROM<T [define of T]> <= TO<T [define of T]>`

1. FROM <= TO
2. 对于任意i, FROM.T[i] <= TO.T[i]. 见[泛型参数转换](#泛型参数和泛型参数之间的类型转换)

### 泛型参数和普通类型(可能带泛型)之间的转换

要使得定义为

`Lower<=T<=Upper & T <= Interfaces & new<....> in T`

的泛型和类型Obj相互转化

- 类型转换为泛型参数

  即`FROM <= TO [define of TO]`

    1. TO.Lower<=FROM<=TO.Upper
    2. 且, 对于任意i, FROM<=TO.Interfaces[i]
    3. 且, 对于任意i, TO.new[i] in FROM

- 泛型参数转化为类型

  即`FROM [define of FROM] <= TO`

    1. FROM >= TO.Upper
    2. 或, 存在i, 使得FROM >= TO.Interface[i]

### 泛型参数和泛型参数之间的类型转换

泛型参数FROM怎么转化成泛型参数TO

即`FROM <= TO`

可得FROM, TO的定义:

`Lower<=FROM<=Upper & FROM<= Interfaces & new<....> in FROM`

`Lower<=TO<=Upper & TO<= Interfaces & new<....> in TO`

FROM转TO需要, FROM是TO的子集

- FROM.Lower>=TO.Lower
- FROM.Upper<=TO.Upper
- 对于任意j, 都存在i, 使FROM.Interfaces[i]<=TO.Interfaces[j]
- 对于任意j, 都存在i, 使FROM.new[i]能转换成TO.new[j]. 见[类型转换](#带泛型的类型之间的转换)

### 对于类型转化中`<=`的说明

`FROM <= TO`, 表示 FROM 能转化为 TO.

1. 基本数据类型的转化
2. 有继承关系类型的转化表示子类的情况
3. 带泛型的复杂的转化