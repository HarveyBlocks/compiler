/**
 * <p>
 * 用于将一个源码文件拆解成一个个部分<br>
 * 1. 去除注释和空格, 将文本存在一个链表<br>
 * 2. {, }, ;总是单独存一个元素
 * 2. <s>将一些如`1.2e2`的, 含有数字,符号., 字母e的合成在一起</s><br>
 * 3. <s>将一些由多个字符组成的运算符组合在一起</s><br>
 * 4. 将字符串中的转义字符转义<br>
 * 5. 将声明和块联合, if/while后面不是块的, 合成一个块, 然后组成一个Map<br>
 * 6. Map<某种签名(可为null), 块(不可为null)> , 块中也是嵌套的Map, 方法/函数的块中是链表, 存程序语句<br>
 * 7. 方法中的块, 可以是定义方法的块, 方法中的方法存在map里<br>
 * 8. 此为完成第一步, 生成声明文件, 不进行调用的匹配<br>
 * <p>
 * 如何将几个不是在一起的运算符搞成在一起?<br>
 * 1. 将所有运算符分类: 三个字符串的运算符, 两个字符串的运算符, 一个字符串的运算符<br>
 * 问:<br>
 * 1. "1. 2"或"1 .2"或"1 . 2"要被分析成常量"1.2"吗<br>
 * 答:<br>
 * 1. 不需要. C语言都没有实现这个功能<br>
 * <p>
 * <p>
 * 字符分为NUMBER, CHARACTER, SIGN<br>
 * 1. 读取第一个<br>
 * 2. 依据第一个Guess<br>
 * 1. 是NUMBER, 之后只能是NUMBER, 所有NUMBER合成, 遇到SIGN结束<br>
 * 如果是以0x或0X开头,<br>
 * - 只允许数字0-9, 大小写字母a-f, 否则结束<br>
 * - 对于.调用, 可以被结束, 也不妨碍被视作运算符被解析<br>
 * 如果是以0o或0O开头<br>
 * - 只允许数字0-7, 符号_, 否则结束<br>
 * - 对于.调用, 可以被结束, 也不妨碍被视作运算符被解析<br>
 * 如果是以0B或0b开头<br>
 * - 只允许数字0-1, 否则结束<br>
 * - 对于.调用, 可以被结束, 也不妨碍被视作运算符被解析<br>
 * 在读取number的过程中, 如果遇到<br>
 * 0. "_", 读入<br>
 * 1. `.` , `.`之后是<br>
 * 1. 数字, 解析为浮点数<br>
 * 2. 字符串结尾, 认为当前数字是.0的浮点数<br>
 * 3. 大/小写e<br>
 * 4. CHARACTER, 认为使用了调用Number类的函数, .是调用运算符, 数字解析也也结束了<br>
 * 5. SIGN, 认为结束了<br>
 * 2. 大/小写e, e之后是<br>
 * 1. 数字, 保存<br>
 * 2. CHARACTER, 结束了<br>
 * 3. SIGN,<br>
 * 3. 字符串结尾->结尾了<br>
 * 5. 字母(也可能是"L", "l"等, 单独存)->结尾了<br>
 * 6. SIGN->结尾了<br>
 * 2. 是CHARACTER, 之后可以是NUMBER和CHARACTER, 遇到SIGN结束<br>
 * 3. 是SIGN, 后面只能是SIGN, 遇到非SIGN结束<br>
 */
package org.harvey.compiler.text;

/*
a++2--3- -5(){
}
NUMBER
WORLD
SIGN
"a ++ 2 -- 3 -"->"-"->"5"->"()"->"{"->"}"
"a"->"++"->"2"->"--"->"3"

*/