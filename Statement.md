## 声明

### 访问控制

#### 不同的可用访问控制范围

- 作为一个文件层的变量/函数/复合结构, 其一定是的file|public|package|internal package

- 作为一个成员, 其可选作用域一定是任意作用域

- 作为一个代码块里的局部成员, 一定没有作用域

- 作为一个抽象方法, 其方法一定要被重写

  所以其作用域一定是public或[protected|internal protected]+[ 无|internal package|package|file ]

- 由于内部类都是同一个程序员写的, 所以private的成员可以被内部类和外部类访问

对于private, 这个外部类能不能访问呢?

package包, 外部包能不能访问? 能吗?

#### 冲突

- 和自身冲突

- public 和所有的冲突

- protected 和 protected , private , internal protected, internal private 两两冲突

- file 和 package 和 internal package 两两

### 类型

1. 基本数据类型
2. 带有signed和unsigned修饰的基本数据类型
3. 自定义类型
4. 数组元素类型[]

### 参数列表

1. 括号包围

2. 有类型也有标识符

3. 有逗号

4. ([类型 标识符 [....],[...]])

5. 按顺序有:

    1. 普通参数

    2. 缺省参数, 已经有了默认值, 可以选择用形式参数名来指定

       type identifier = default

    3. 不定参数 类型... 一个函数只能有一个, 不定参数如果没有就是null

       type... identifier

    4. 关键字参数, 需要被指明形式参数名的参数, 已经有了默认值,不指定使用默认值

       type identifier = default

6. 一个缺省参数在不定参数前, 表示普通缺省参数; 在不定参数后, 表示关键字参数

7. 缺省参数也可以用形式参数名指定

### 返回值(列表?)

- 返回值就是给函数发放一块空间, 这篇空间可以存放一个地址, 这个地址指向

  返回的时候就将这片地址中的空间给填上返回值的地址即可

- void表示不返回

- 可以省略括号表示返回一个值

- 有括号表示返回多个值(其实是打包返回)

- 有括号, 但是一个值, 表示返回打包的一个值(不一样)

- 有括号的返回值是元组, 成员是[index,index]的有序列

- 可以依据索引获取, 底层原理是依据索引从数组中获取指针, 依据指针返回值

### 修饰

- final

    - 变量只读
    - 类可以被继承, 但不能重写父类方法, 父类的成员不能在子类直接修改

- const

    - 变量只能调用const修饰的函数

      变量可以改变

    - 函数内的同层以及外层变量

      (对于方法, 外层变量即文件层和同类成员类)不可写

      只能使用const修饰的成员方法, 局部变量可写

      方法只能调用本类const成员(其实是this变成了const)

- sealed

    - 类/结构不可继承
    - 方法不可被重写

-

是否允许

|                | static | final | const        | sealed | abstract |
|----------------|--------|-------|--------------|--------|----------|
| class          | FALSE  | TRUE  | FALSE        | TRUE   | TRUE     |
| interface      | FALSE  | FALSE | FALSE        | FALSE  | TRUE     |
| function       | FALSE  | FALSE | FALSE        | FALSE  | FALSE    |
| field          | TRUE   | TRUE  | TRUE         | FALSE  | FALSE    |
| method         | TRUE   | FALSE | TRUE         | TRUE   | TRUE     |
| operator       | FALSE  | FALSE | 依据Operator情况 | TRUE   | TRUE     |
| inner class    | TRUE   | TRUE  | FALSE        | TRUE   | TRUE     |
| local variable | FALSE  | TRUE  | TRUE         | FALSE  | FALSE    |
|                |        |       |              |        |          |
| construct      | FALSE  | FALSE | FALSE        | FALSE  | FALSE    |
| alias          | FALSE  | FALSE | FALSE        | FALSE  | FALSE    |
| inner alias    | FALSE  | FALSE | FALSE        | FALSE  | FALSE    |
|                |        |       |              |        |          |
| blocks         | TRUE   | FALSE | FALSE        | FALSE  | FALSE    |

默认

|                       | 修饰           |
|-----------------------|--------------|
| interface             | abstract     |
| interface field       | static final |
| interface method      | abstract     |
| interface operator    | abstract     |
| interface inner class | static       |
| struct                | final        |
| struct field          | const final  |
| struct method         | const sealed |
| struct operator       | const sealed |
| struct operator       | const sealed |
| class inner interface | static       |

禁止

abstract和sealed 不能同时出现
abstract和final 不能同时出现
final 和 sealed 不能同时出现

方法时
static不能和const, abstract. final, sealed同时出现

## 变量

### 声明

- LocalVariable/Argument
    1. 代码块层作用域
    2. [const|final]
    3. 类型
    4. identifier
    5. 用逗号分割的多个identifier和不定的赋值语句
- FileVariable
    1. 文件层作用域
    2. [const|final]
    3. 类型
    4. 用逗号分割的多个identifier和不定的赋值语句
- Field
    1. 成员层作用域
    2. [const|final|static]
    3. 类型
    4. 用逗号分割的多个identifier和不定的赋值语句

### 赋值

不定的"赋值语句"

## 代码块

- static-body 初始化静态字段
- 无-body 初始化非静态字段

静态代码块的声明是`static{...}`

非静态代码块的声明是`{}`

只能在复合类型和方法中出现

1. 局部变量
2. 内部函数
3. 控制结构 关键字+非静态代码块/ 非静态代码块+关键字
4. 非静态代码块

##复合类型

### 声明

- Class
    1. 文件层作用域
    2. [abstract]
    3. [final, sealed]
    4. class
    5. Identifier
    6. [extends Identifier(必须是class)]
    7. [implements ...(必须是interface)]
- Interface(为了多继承, class只允许单继承)
    - 文件层作用域
    - interface
    - Identifier
    - [implements ...(必须是interface)]
- Enum
    - 文件层作用域
    - enum
    - class
    - Identifier
- Struct
    - 文件层作用域
    - [sealed]
    - struct
    - Identifier
    - [extends Identifier(必须是Struct)]
- 内部类
    - 全层作用域
    - [final, sealed, static]
        - 没用static标注的内部类, 将能够访问外部类的成员
    - class/struct/interface/Identifier
    -
- 成员复合类型
    1. 成员型作用域
    2. 其余同对应复合类型声明

### Body

- Class(没用Abstract Class, 有AbstractMethod的, 就是AbstractClass)

    - ComplexStructure List
    - Field List
    - Method List
    - Constructor List
    - AbstractMethod List
    - body List
    - static-body List

- Enum

    - 第一句一定是成员数组
    - 同Class

- Struct
    - 所有成员可读不可写
    - 所有的成员必须加const, 不加默认加上
    - 所有的成员变量必须加final, 不加默认加上
    - 必须在创建出成员的时候全部初始化成员
    - 想要修改值, 一定是被拷贝, 可以在拷贝过程中修改成员生成新成员
    - 同Class
- Interface

    - body

        - FieldList, 抽象层作用域,

          必须是(final修饰, 不写默认加final)

          必须是static, 不写自动加static

        - AbstractMethod

        - static修饰Method

- Internal ComplexStructure

    - body
        - 同本ComplexStructure
    - [作用域(全部)]
        - [abstract]
        - [final, sealed]
        - class
        - Identifier
        - [extends Identifier(必须是class) [implements [...(必须是interface)]]]

## 函数

使用func关键字来减少编译时区分变量和函数的时间

### 声明

- Function
    - 文件层作用域
    - func?
    - [const] 能不能文件层变量的非const
    - Identifier
    - 参数列表
- Method
    - 成员层作用域
    - func?
    - [static|const|sealed] 能不能文件层&成员层变量的非const
    - static和const两两矛盾
    - Identifier
    - 参数列表
- LocalFunction
    - 代码块层访问控制
    - func?
    - [const] 能不能文件层&成员层&代码块层变量的非const
    - type
    - Identifier
    - 参数列表
- Constructor
    - 成员层访问控制
    - func?
    - 本Class/Struct名
    - 参数列表
- AbstractMethod
    - 成员层访问控制
    - func?
    - [] 不能有修饰(static和const和sealed都不合适)
    - type
    - Identifier
    - 参数列表

### Body

- Function
    - 同代码块
- Method
    - 同代码块
- LocalFunction
    - 同代码块
- Constructor
    - 第一行必须用`this()`或`super()`指明用什么构造
    - 吗?
    - 同代码块
- AbstractMethod
    - 无