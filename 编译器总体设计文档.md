# 编译器总体设计文档

## 一、参考编译器介绍

> 总结所阅读的编译器的总体结构、接口设计、文件组织等内容

编译器设计参考《编译器设计》、《编译原理》（北航）、LLVM 官方中间代码文档和其他开源论文资料。

## 二、编译器总体介绍

编译器整体分为词法分析、语法分析、语义分析生成中间代码、目标代码生成和代码优化共5个阶段。同时将有错误处理过程和符号表的建立和维护过程。

整个词法分析阶段主要的工作是从源文件中获得程序内容基本信息，形成一个个Token，并将token序列发送给语法分析阶段。在词法分析阶段将要完成的操作是记录源文件行号、匹配token和预定字、消除源程序中的注释，并划分源程序和字符串。

语法分析阶段的主要工作是从token序列中构建语法树，构建语法树的目的是还原部分程序信息。在语法分析阶段就可能同时出现错误处理内容。到这一步，程序中所有的信息基本已经提取完毕，随后的过程将根据语法树信息，构建中间代码和目标代码。

在语法分析结束后还会进行更加系统的错误处理过程，这个阶段将编译中要求的错误进行系统处理。

语义分析生成中间代码的过程将利用语法树。建立对变量进行重命名等操作建立SSA形式的中间代码。具体中间代码我参考LLVM的中间代码形式，先建立load+store形式的中间代码。在之后的优化阶段将把load+store形式的中间代码优化成mem2reg形式的中间代码。

目标代码生成阶段，将根据中间代码，生成面向目标机器的目标代码，在这个阶段中，将会进行寄存器分配并同步进行代码优化。

代码优化的主要任务是根据目标机和指令罚时要求生成符合代码规范的目标代码。

![image-20221225104130905](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225104130905.png)

## 三、特色设计（敝帚自珍）

==<font color = red>**整篇文档的重点，其他的都可以不看**</font>==

### 使用状态机去除源程序中注释

### FIRST和FOLLOW集合求解图

### 使用 LLVM IR 作为中间代码

- LLVM 真不容易

### 使用狂放不羁的手写代替打字以理清思路（也是本报告的特点）

- **如果让我仅保留这篇报告的某些内容，我一定会保留我的手稿，这体现了我在整个编译器设计过程的思考和心血，每当看到我的手稿，我都回忆起遇到一个个困难百思不得其解时的冥思苦想。**
- **手稿比较多，整篇文档中仅选择了其中一个部分**

### mem2reg优化SSA形式并消除 Phi 函数

- **参考各种论文、博客和《编译器设计》，就是没有直接看其他编译大赛的代码或者往届学长的代码，100%自主完成**

### 使用贪心方法分配寄存器

- 来源是某天上完算法课，突然看到算法课中**“活动选择问题”**和寄存器分配问题又天然的类似性，所以尝试将算法课程中学习到的结果应用到寄存器分配中。效果非常好
- 这种融汇算法中**活动选择问题**的解法进行寄存器分配的思路，我目前没有在任何往届或者公开的报告中看到，所以我一直非常兴奋于我将知识融会贯通起来。
- 如果让我说一个**整个编译器过程中令我印象最深刻的细节**，这个小 trick 无疑是最令我难忘的，因为无论其他任何思路和方法都是前人写诸于纸上，但是这个是我独自发现的。**非常有趣**

### 使用 FilePrinter 进行输出控制

- FilePrinter中定义了输出路径，在每个阶段几乎都有输出，但是通过这个文件就能确定输出到哪里，什么时候输出

- ```java
      private FilePrinter(){
          File outFile = new File("./mips.txt");
  //        File outFile = new File("./llvm_ir.txt");
          try {
              out = new PrintWriter(outFile);
          } catch (FileNotFoundException e) {
              e.printStackTrace();
          }
      }
  
      public void outPrintlnSyntax(String line) {
  //        out.println(line);
      }
  
      public void outPrintlnError(String line) {
  //        out.println(line);
      }
  
      public void outPrintlnLLVM(String line) {
  //        out.println(line);
          System.out.println(line);
      }
  
      public void outPrintlnMIPS(String line) {
          out.println(line);
  //        System.out.println(line);
      }
  ```

## 四、词法分析设计

### 阶段目标

词法分析需要将源程序中的字符串进行拆分，拆成一个个Token

同时，需要满足如下要求，以便于后续分析更加流畅：

- 消除源程序中的注释：注释对于编译器来说是没用的，因此需要删除
- 标记每个Token的行号：最开始编译的时候其实不需要有行号，当我们需要进行错误处理的时候，才需要使用Tokne中的行号。以便代码编写者可以查看对应行号和错误编码号。
- 标注每个Token的类型，并在Token中保存源程序的字符串：Token是一个个字符串，但是我们需要的信息远不止字符串这么简单。例如对于关键字 `if`，不仅仅需要if本身，还需要 `IFTK` 标识其特殊意义。

### 设计方法

#### 源程序的输入

使用BasicScanner类，由它处理源程序的输入。

BasicScanner类将源程序一行行输入，并使用状态机决定当前扫描到的源程序是否处于正常文本、注释和字符串其中一种，主要方法是根据输入的字符进行转移。具体状态转移图如下所示：

![image-20221225104808425](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225104808425.png)

对于注释，可以自由将其删除。经过上述处理，我们得到了和源程序完全等价的、**没有注释的**新程序。

- 在新程序中，所有字符排成一行。这样便于词法分析中对于源程序扫描和行号的确定

#### 源程序的解析

经过上述处理，接着需要进行源程序的解析。所谓解析就是从源程序的一坨字符串中**提取对分析程序有利的字符串**。因此需要：

- 扫描源程序，跳过空白字符（扫描的同时记录当前行数，便于后续进行错误处理工作）
- 生成从源程序中截取有意义的字符串：这里我使用的方法是先获取一个字符，从这个字符开始查看能否进行字符串的拓展，直到不能拓展（即得到了一个有意义的字符串）。
  - 例如：当获取到的第一个非空字符为 `=` 时，我们需要判断其后是否紧跟着 `=`，这将决定该 Token 是 ASSIGN 还是 EQL。（这类Token的特点是**他们是定长的**，所以可以进行枚举，但是下面的输入情况不一定是定长的字符串） 
  - 如果第一个非空字符为 `"` 则表示识别到一个字符串的开头，这将要求我们的词法分析程序不断检测直到出现下一个 `"`。
  - 如果第一个非空字符为数字，则表示这是一个数字，这同样要不断输入，直到第一个非数字符号
  - 如果第一个非空字符不是任何保留字符，则有可能是输入错误，也有可能是这是变量名，同样需要不断进行添加，直到第一个不满足变量名的字符出现。

#### 行号的标注

行号的标注使用了朴素方法

- 首先BasicScanner中可以获得源程序中每一行的长度，经过BasicScanner的处理，源程序变成了一行字符串。每一行的行号变成了一段字符串在总体字符串中的位置。
- 在生成一个个Token 的时候，将 Token 对应在源程序中的（一行）的位置记录下来
- 遍历所有 Token 确定 Token 所在行号

## 五、语法分析设计

### 阶段目标

- 语法分析将获得 Token 序列生成语法树
- 同时为了便于进行错误处理，这里需要确定一些错误

### 改写文法

**由于原来的文法中有左递归问题，这里进行消除。最终得到的文法基本满足课上所讲的LL(k)要求，改写后的文法如下所示：**

```
编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef 
声明 Decl → ConstDecl | VarDecl 
常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' 
基本类型 BType → 'int' 
常数定义 ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal 
常量初值 ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}' 
变量声明 VarDecl → BType VarDef { ',' VarDef } ';' 
变量定义 VarDef → Ident { '[' ConstExp ']' } 
				| Ident { '[' ConstExp ']' } '=' InitVal 
变量初值 InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
函数定义 FuncDef → FuncType Ident '(' [FuncFParams] ')' Block 
主函数定义 MainFuncDef → 'int' 'main' '(' ')' Block 
函数类型 FuncType → 'void' | 'int' 
函数形参表 FuncFParams → FuncFParam { ',' FuncFParam } 
函数形参 FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }] 
语句块 Block → '{' { BlockItem } '}' 
语句块项 BlockItem → Decl | Stmt 
标识符 identifier → identifier-nondigit 
 				| identifier identifier-nondigit
语句 Stmt → LVal '=' Exp ';' 
    		| [Exp] ';'
			| Block 
			| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] 
			| 'while' '(' Cond ')' Stmt 
			| 'break' ';' 
			| 'continue' ';' 	
			| 'return' [Exp] ';' 
			| LVal '=' 'getint''('')'';' 
			| 'printf''('FormatString{','Exp}')'';' 
Stmt → (LVal '=')('getint''('')'';' | Exp ';') 
    | Block 
    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] 
    | 'while' '(' Cond ')' Stmt 
    | 'break' ';' 
    | 'continue' ';' 
    | 'return' [Exp] ';' 
    | 'printf''('FormatString{','Exp}')'';' 
表达式 Exp → AddExp 注：SysY 表达式是int 型表达式 
条件表达式 Cond → LOrExp 
左值表达式 LVal → Ident {'[' Exp ']'} 
基本表达式 PrimaryExp → '(' Exp ')' | LVal | Number 
数值 Number → IntConst 
一元表达式 UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' 
					| UnaryOp UnaryExp 
单目运算符 UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中 
函数实参表 FuncRParams → Exp { ',' Exp } 
乘除模表达式 MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp 
MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
加减表达式 AddExp → MulExp | AddExp ('+' | '−') MulExp 
AddExp → MulExp {('+' | '-') MulExp}
关系表达式 RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp 
RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
相等性表达式 EqExp → RelExp | EqExp ('==' | '!=') RelExp 
EqExp → RelExp {('==' | '!=') RelExp}
逻辑与表达式 LAndExp → EqExp | LAndExp '&&' EqExp 
LAndExp → EqExp {'&&' EqExp}
逻辑或表达式 LOrExp → LAndExp | LOrExp '||' LAndExp 
LOrExp → LAndExp {'||' LAndExp}
常量表达式 ConstExp → AddExp 注：使用的Ident 必须是常量 
```

### 求解FIRST和FOLLOW集合

**为了使用课上所学的 LL(k) 分析法，这里需要求解 FIRST 和 FOLLOW 集合**

==<font color = red>**已将所有非终结符的 FIRST 和 FOLLOW 集合放到附录，请在保证充分思考的前提下自行查看**</font>==

#### FIRST 求解图

使用方式是从根节点开始按照逆拓扑排序逐个求解每个非终结符的 FIRST 集合

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\放大求解.png" alt="放大求解" style="zoom:67%;" />

#### FOLLOW求解图

使用方式是从根节点开始按照逆拓扑排序的方式求解每个非终结符的 FOLLOW 集合。

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\FOLLOW放大.png" alt="FOLLOW放大" style="zoom:67%;" />

## 六、错误处理设计

### 所有错误

| 错误标号 | 名称                               | 描述和处理方法                   |
| -------- | ---------------------------------- | -------------------------------- |
| a        | 字符串中出现非法符号               | 如果遇到string，进行扫描判断即可 |
| b        | 名字重定义                         | 操作符号表进行处理               |
| c        | 未定义的名字                       | 操作符号表进行处理               |
| d        | 函数参数个数不匹配                 | 操作符号表进行处理               |
| e        | 函数参数类型不匹配                 | 属性文法进行处理+符号表          |
| f        | 无返回值的函数有不匹配的return语句 | 处理函数的时候进行判断           |
| g        | 有返回值函数缺少return             | 处理函数的时候进行判断           |
| h        | 不能改变常量的值                   | 符号表                           |
| i        | 少分号                             | 语法分析阶段出错                 |
| j        | 少右小括号                         | 语法分析阶段出错                 |
| k        | 少右中括号                         | 语法分析阶段出错                 |
| L        | printf格式字符与表达式数目不匹配   | 比较个数即可                     |
| M        | 非循环块中使用break，continue      | 遍历树即可                       |

按照处理阶段需要将IJK和其他错误类型分开。对于IJK类型，需要在语法分析阶段将其挑拣出来，进行处理。

### 处理IJK

分号的情况比较复杂，先处理一下右小括号和右中括号。

**小括号**

我们需要对IJK在语法分析阶段处理，所以这里需要对原有的语法分析进行扩展。**如果在语法分析时考虑了FOLLOW（比如我），就会导致语法分析几乎不能进行扩展。因为考虑FOLLOW的时候，一切语法错误都能快人一步的发现。**<font color = red>**FOLLOW 是一个非常严格的设定，一旦在语法分析中使用FOLLOW则几乎无法再对文法进行扩展。在大作业中由于没有任何一个非终结符有可能是空串，所以可以不适用FOLLOW集合。**</font>

考虑如下错误：

```c
if (a > max {
    ...
}
```

如果考虑FOLLOW集合后，则在解析LVal（即max）时就能根据之后的FOLLOW集合判断出大括号不能出现在语法中，从而直接报错。而理想中进行错误处理的时间点应该在Stmt中，解析完Cond（即`a < max`）之后，此处判断有无右小括号。

目前我的解决方法就是把所有考虑FOLLOW的地方删除掉了，提交语法分析的辅助测评，都没有问题，通过了回归测试。

**优化Stmt结构**

```c
Stmt → (LVal '=')('getint''('')'';' | Exp ';') 
    | Block 
    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] 
    | 'while' '(' Cond ')' Stmt 
    | 'break' ';' 
    | 'continue' ';' 
    | 'return' [Exp] ';' 
    | 'printf''('FormatString{','Exp}')'';' 
```

对于Stmt中的各个选择，按照分支末尾是否解析分号将Block、if分支和while分支归为一类，其他归为另一类。对于解析分号的分支，统一处理分号，这样可以减少重复代码。

### 设计+建立符号表

最顶层类为`SymbolRecord`，它的子类有`SymbolTable`和`SymbolTableItem`。

采用面向对象思想，符号表用单独的类进行表示`SymbolTable`，其中为一个个`SymbolRecord`。由于Record有两个子类，因此实现了Table和Item可以同时存在于同一层符号表的Array中。`SymbolTable`中还要存储上一级的`SymbolTable`和本级`Table`在上级Table中的数组下标位置，原因将在之后说明。

对于Item的分类需要更加细致：Item的类型应该包括函数类型、数组类型、变量类型

- 对于函数类型：
  - 需要存储函数返回类型（void或者int）、函数名字（name）、函数参数列表（Array，注意应该是有序的）。
  - 为了便于之后进行函数的形参和实参的匹配，应该将函数进行符号化，这样在程序中出现函数调用时，可以直接进行字符串的比较。因此函数类型的`toString`方法应该返回“函数类型+函数名字+参数列表的有序排列”。对于函数的参数列表，如果某个参数为普通变量，则输出为类型即可；如果某个参数为数组，则输出类型+维度信息。如`int a(int b, int c[][3][4])`则输出`int@a@int@int0*3*4`

- 对于数组类型：
  - 需要存储：数组类型（必然是int）、数组名字（name）、数组维度和大小、是否为const、如果有初始值则还应该记录初始值
  - 为了便于进行数组的匹配，应该对数组进行符号化，方法同函数类型：对于`int c[2][3][4]`则输出`int0*3*4`，名称匹配时直接比较名称即可。
- 对于变量类型：
  - 需要存储：变量类型（必然为int）、变量名字（name）、是否为const、初始值等信息。
  - 比较时直接比较函数名字即可。

### 使用符号表进行错误检查

需要配合遍历语法树进行错误处理。遍历语法树应该按照从左到右的顺序进行遍历。

**为什么不把错误处理和语法分析一起做：**

1. 除IJK错误之外，其他错误类型并不影响我们进行语法分析，并且程序可以正确建立语法树、确定各个非终结符和终结符的行号。将错误处理和语法分析分开说明我们进入了新阶段、需要采用新方法
2. 语法分析阶段的代码已经写完，从面向对象的开闭原则来说，不应该在其上做增量开发了，而是应该对其进行合理封装
3. 语法分析阶段的代码逻辑较多，分支判断比较复杂。这个时候

下面将按照各个错误类型进行错误处理和输出说明

==<font color = red>**放入附录，请在充分思考的前提下自行查看**</font>==

## 七、中间代码生成

**中间代码使用SSA（静态单赋值）的形式，采用LLVM IR，便于后续进行优化**

### 中间代码语法概述

#### 类型

**寄存器上的数据和栈上的数据**

LLVM中引入了虚拟寄存器的概念。一个函数的局部变量可以是寄存去或者栈上的变量。寄存器变量要以%开头

```
%local_variable = add i32 1, 2
```

如果不需要操作地址并且寄存器数量足够的话，可以直接使用寄存器。在需要操作地址以及需要可变变量的时候，需要使用栈

```
%local_variable = alloca i32
```

**全局变量和栈上变量都是指针**

```
; global variable
@global_variable = global i32 0
%local_variable = alloca i32
```

如果要操作这些值，需要使用load和store指令

```
%1 = load i32, i32* @global_variable
store i32 1, i32* @global_varibale
```

**基本数据类型**

- **LLVM IR**中整型默认是有符号整型，LLVM中提供了sdiv适用于有符号整型除法

- 数组类型在LLVM中的声明，例如要声明`int a[4]`

  ```
  %a = alloca [4 x i32]
  ```

  初始化

  ```
  @global_array = global [4 x i32] [i32 0, i32 1, i32 2, i32 3]
  ```

  字符串

  ```
  @global_string = global [12 x i8] c"Hello world\00"
  ```

**基本块**

- 一个函数由多个基本块组成
- 每个基本块包含：
  - 开头的标签（**可省略**）
  - 一系列指令
  - 结尾是终结指令
- 一个基本块没有标签时，会自动给他赋一个标签

- 终结指令**就是改变执行顺序的指令，如跳转、返回等**

#### 语句

还有一些重要指令可能会在实验中用到

**比较指令**

- icmp 接受三个参数：比较方案和两个比较参数

- ```
  %comparison_result = icmp uge i32 %a, %b
  ```

- icmp返回一个i1类型的数，用来表示结果是否为真。

  支持的比较方案：

  - `eq, ne`：表示相等或不相等
  - 无符号比较`ugt, uge, ult, ule` 大于，大于等于，小于、小于等于
  - 有符号比较`sgt, sge, slt, sle`符号版本

**跳转指令**

- LLVM提供的跳转指令为 `br`，接受三个参数，第一个参数是 `i1` 类型的值，用于作判断；第二个和第三个参数分别是值为 true和false时需要跳转的标签

- ```
  br i1 %comparison_result, label %A, label %B
  ```

- **无条件跳转**：直接跳转到某一标签处，同样使用br实现，例如要跳转到start标签处，使用：

- ```
  br label %start
  ```

**函数声明和调用**

- 函数定义： 

  ```
  define i32 @main() {
  	ret i32 0
  }
  ```

- 函数调用

  ```
  define i32 @foo(i32 %a) {
  	; ...
  }
  define void @bar() {
  	%1 = call i32 @foo(i32 1)
  }
  ```

- **Call**指令可以像高级语言一样直接调用函数。完成了如下工作：

  - 传递参数
  - 执行函数
  - 获得返回值

#### mem2reg 和 Phi 函数

LLVM 是静态单赋值的，但是我们不能一口吃个胖子，也几乎不太能一下子写出标准而且优美的SSA形式，因此LLVM最初提供了一个 Load 和 Store 形式的 SSA。具体方式就是将一些变量放到栈上（使用 ALLOCA 声明）。从这些变量中获取值或者将值保存到这个变量上需要使用 Load 和 Store 指令。在简单形式的 SSA 中，对这些变量的 Store 可以是无限次的，因此源程序中的所有变量都可以声明为栈上的变量，源程序中对他们的重复定义可以使用 Store 来代替，我们需要保证的是所有的中间变量都仅被定义一次即可。

事物的发展是螺旋上升的，从 Load 和 Store 形式的 SSA 需要进一步升级。其中，将部分 Load 和 Store 指令消除，将对应的变量转变为普通虚拟寄存器，并在合适的位置添加 Phi 函数就是 mem2reg 的主要内容。

Phi 函数的本质首先是一个赋值语句，但是赋值的数据来源不是单一的。对于同一个变量的赋值，会随着从哪个基本块跳转过来的而变化。在分布位置上，所有的 Phi 函数都在基本块的开头，并且执行起来是同步执行的（进入一个基本块后，基本块内部的 Phi 同时执行）。

### 流程分析

**接下来将逐步介绍我如何生成 Load 和 Store 形式的 LLVA，并如何进行转化的**

#### 消除变量作用域

经过观察可以发现，在 LLVM 几乎消除了作用域的概念（LLVM 中函数提供了唯一的一层作用域抽象），而在 MIPS 中一个寄存器的作用域是全局的。所以我们需要先消除变量的作用域。

**大部分变量没有重名的问题，仅有小部分变量有重名问题**，主要涉及变量-函数两个部分。

**函数-函数重名：**即后来定义的函数和之前定义的函数重名

- 这种方式不可能出现，错误处理部分将处理这种类型

**函数-变量重名：**即后来定义的函数和之前定义的变量重名

- 这种方式不可能出现，错误处理部分将处理这种类型

**变量-函数重名：**即后来定义的变量和之前定义的函数重名

- 这种方式只能出现在不同作用域中，当声明变量时，同时进行递归查找所有表项，如果找到了相同的变量，则进行重名处理

**变量-变量重名：**即后来定义的变量和之前定义的变量重名

- 这种方式只能出现在不同作用域中，当声明变量时，同时进行递归查找所有表项，如果找到了相同的变量，则进行重名处理

**这里涉及的数据结构是：**HashTable，第一维度存储String类型的name，表示原本变量在程序中的定义；第二维存储证书类型的值，表示这是第几次出现，用name_id进行重命名

#### 变量重命名

- 需要的额外数据结构：一个**原名->修改后姓名末尾的i**的全局对照表；一个**记录当前出现过的符号的表**

- 当遇到一个**声明**：

  - 寻找全局+已有符号表，如果ident在任何一个表中出现，**则表明需要改名**。具体的方法是，在ident后+i，例如`a1, a2...`
  - 如果a需要改名，则首先获得`a1`，用`a1`同样需要在全局+已有的符号表中查找，如果没有则**声明成功**，在全局符号表中记录`a 1`，已有符号表中添加`a1`；如果依然查找到，则将a1改成a2，再次进行查找，直到不在两个表中出现为止。

- 当遇到一个**引用or使用：**

  - 按照之前的方法，现在本层（没有再递归向外层）中寻找变量
  - 如果找到，则检查它是否有变化后的名称，如果有则使用变化后的名称；没有则使用原来的名称即可

#### 整体结构

- **顶层Module：**包含**全局变量BasicBlock**，包含函数数组**`ArrayList<Function>`**，包含全局变量数组**`ArrayList<GlobalVariable>`**

- **Module下是Function：**每个函数中包含基本块数组**`ArrayList<BasicBlock>`**

- **Function下是BasicBlock：**
- **BasicBlock下是Instruction**

#### Value 类的理解和处理

Value类是LLVM中的核心类，几乎中端的所有类（除Type系列）都继承自Value。称为Value表示这个类有**值**，并且可能会被用到！

所有的类都是Value，所以不必刻意要求某个函数传入或者传出参数是Value。而是需要什么就定义什么。

例如对于`b = a + 1`，这里右端是`AddExp`。当我们解析的时候，只需要返回Instruction即可，因为Instruction就是Value。可以用这个Value去赋值。**当然，由于Value是上一个计算式结果，所以可能它需要保存到一个寄存器中。**这个Value是一个加法表达式`add i32 %axx, 1`。它是一个Instruction，但是Instruction继承自User，所以它会用到Value，就是一个虚拟寄存器和一个常数值。由于User也继承自Value，所以它可能被使用。返回Instruction，当使用它的时候就可以进行赋值例如`%reg = add i32 %axx, 1`。这里分配reg是在assign中分配的。**我认为不需要在AddExp中考虑.**

这里需要考虑的主要是局部变量和全局变量，寄存器就是**随用随分配**，不需要考虑太多。

#### User 类的理解和处理

User也继承Value。由于User也是Value，所以他有`ArrayList<Use>`，表示使用这个Value的使用者的集合；由于User本身，它有`ArrayList<Value>`表示它使用的Value集合。

#### Type 类的理解和处理

这个Type类型系统是表示一块内存中存储的数据是什么类型的。任何一个Value都有Type

Type一般可以用静态的内容来表示，i32、i1、Function、Array等，

**Type中有：**

- Array：表示数组类型（Array类型中应该包含：元素类型Type，和个数大小）这样子递归定义出来
- Function：函数类型，包括返回值类型和参数类型
- Integer：整数类型，包括i32类型和i1类型表示bool
- Label：标签类型，表示一个标签
- Pointer：指针类型，表示某个基类（基类指的是Type）的指针，任何Type型的变量都有指针
- void：void类型，只能在Function的返回值类型中出现

#### 短路求值 && Break && Continue

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221104162741296.png" alt="image-20221104162741296" style="zoom: 33%;" />

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221104162801397.png" alt="image-20221104162801397" style="zoom: 33%;" />

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221104162814378.png" alt="image-20221104162814378" style="zoom: 33%;" />

#### 数组 && 初始值 && GEP && 传参

**LLVM官网：**https://llvm.org/docs/GetElementPtr.html

**GEP指令Manual：**https://releases.llvm.org/15.0.0/docs/LangRef.html#getelementptr-instruction

**Overview**

GEP指令用来获取聚合数据结构的子元素地址。进行地址的计算，但是不获取内存，并且可以用于计算vector的相关地址。

**Arguments**

第一个参数总是用于计算地址所用的基础类型；第二个参数总是一个指针或者一个指针的vector，并且是计算的基地址。其余参数是indices。indices可以指示哪个元素被indexed了

如何翻译（解释）每个index取决于被indexed的类型。

第一个index通常indexes作为第二个参数的pointer value。

第二个index通常indexes被指向的type的value

> 第二个索引索引指向的类型的值（不一定是直接指向，因为第一个索引可以是非零）

第一个被indexed的类型一定是pointer类型，后续的类型可以是arrays，vectors，结构体等

后续被indexed的类型永远不能是指针类型，否则将需要在计算前load这个指针。

每个index argument的类型取决于它将要indexing的类型。当indexing一个结构体，则只有i32类型常数被允许；如果indexing一个数组，指针或者vector，任何长度的指针都可以并且不需要是常数。这些整数在相关时被视为有符号值。

#### mem2reg

==<font color = red>**这部分也属于优化，放到优化部分喽！**</font>==

### 难点分析

生成 SSA 形式的中间代码是整个编译过程中最复杂的部分之一，涉及到大量的图论知识，需要查找大量的资料。但是好在**前人已将这部分的浑水探明白了**，所以我只需要学习就可以了（仅学习公开的论文、博客文章、《编译器设计》等教科书、**不包括编译大赛或者其他实际代码**），这确实是编译器设计中非常幸福的一件事。

## 八、生成目标代码

### 一个摆烂但有效的实现

#### 整理 Alloca 和 String

将所有alloca和String放到data段中声明

**alloca**

```
%a1 = alloca i32
```

转变为

```assembly
.data
a1: .space 4
```

**String**

```
@STRCON_NO_0 = constant [10 x i8] c"20373184\0a\00"
```

转变为（将`\0a`转变为`\n`，将`\00`转变为`\0`）

```assembly
STRCON_NO_0: .ascii "20373184\n\0"
```

#### 处理所有函数声明

**函数声明使用标签进行声明**

```
define i32 @main() {
}
```

转变为

```assembly
main:
```

**函数参数如何实现传递？**

目前方法：全部存到栈上

#### 虚拟寄存器映射到物理寄存器

使用贪心算法找到一个物理寄存器可以管理哪些虚拟寄存器。具体思路是将寄存器看成一个**从某点开始到某点结束的活动**，寄存器的分配过程看成**从中选择一些不重复的活动参加，**示意图类似

![image-20221225080211824](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225080211824.png)

这里的 $a_1\sim a_{11}$ 就像一个虚拟寄存器，而<font color = red>使用红色</font>选出来的虚拟寄存器的活动范围不重叠，所以可以使用一个物理寄存器来代替。

- **物理寄存器够用时：**一遍一遍寻找，直到所有虚拟寄存器找到一个对应的物理寄存器
- **物理寄存器不够用时：**如果一遍遍寻找之后，物理寄存器仍然不够使用，则某些虚拟寄存器需要保存在栈中。

所有输出寄存器都需要用`a0`保存地址or值，并且`v0`保存系统调用号

**数据结构支持：**

- 一个集合存储还有哪些虚拟寄存器没有分配
- 一个Map存储虚拟寄存器映射到了哪个物理寄存器

**处理步骤**

- 确定任意一个虚拟寄存器的开始位置和结束位置
- 运用算法不断给某个物理寄存器分配多个它需要负责的虚拟寄存器。
- 如果所有物理寄存器使用完，分配栈or内存空间

**需要保留的寄存器**

经过分析最终需要保存寄存器a0，v0，t0用于存常数，t1，t2，t3用于访问内存时的寄存器（当某个虚拟寄存器为栈上寄存器or内存中的寄存器，则使用这三个寄存器进行访问）

- **指令store**

```
store i32 3, i32* %_de
```

对于这种指令：需要有一个固定的寄存器，保存数字

```assembly
li $t0, 3
sw $t0, _de
```

- **指令call**

这里由于离开了当前函数，所以

- 调用getint

  v0为系统调用号为5，返回值为v0，将结果move回分配好的寄存器

- 调用putstr

  在准备输出字符串之前，一定有虚拟寄存器获得某个字符串的地址。如果是GEP指令，**目前翻译成la指令，到那个虚拟寄存器**

  当遇到putstr时，使用move将对应寄存器保存的地址复制到a0中。

  设置v0系统调用号为4，系统调用

- 调用putint

  如果是常数，则使用li指令对a0进行赋值

  如果是虚拟寄存器，则使用move指令对a0进行赋值

  设置v0系统调用号为1，输出

- **乘法指令mul**

  使用乘法指令，直接将结果使用这种方式保存`mul result operand1 operand2`

- **除法指令sdiv -> div**

  结果同样：`div result, operand1, operand2`

- **取余指令srem -> div + mfhi**

  使用一个中间变量存储结果（不重要

  ```assembly
  div $t3, $t3, 3
  mfhi $t3 # 这是最终结果
  ```

### 消除 Phi 节点

消除 Phi 节点的过程基本属于目标代码生成的范畴，因为消除 Phi 节点后，同一个虚拟寄存器可能有多个定义点，这种形式不符合 SSA 形式，所以需要进行消除。

消除 Phi 节点的参考博客来自 https://www.cnblogs.com/AANA/p/16315795.html

**这篇博客的参考文章是《编译器设计》和同作者写的论文：**Softw Pract Exp - 1999 - Briggs - Practical improvements to the construction and destruction of static single assignment

> **关键边**
>
> - 所谓关键边，就是如果一个基本块 X 有多个后继节点，它的其中一个后继基本块 Y 又有多个前驱基本块，则连接 X 和 Y 的边称为关键边。

消除 Phi 节点中可能出现**丢失复制问题和交换问题**

#### 丢失复制问题

丢失复制问题的本质是生成 Phi 函数是，进行了变量赋值的折叠，这导致某些虚拟寄存器的活动周期被延长了，从而不利于消除 Phi。需要知道，**几乎所有的 Phi 函数都可以通过拆分关键边来完成，**但是这样由于引入更多的基本块所以会导致非常差的性能，要消除引入基本块带来的问题，还需要进行更加细致的控制流图分析、无用基本块的合并和删除。

为了避免这个问题，编译器必须注意到被赋值语句定义的变量的活跃范围超过了他原本赋值语句的位置。当编译器检查到这种情况时，需要引入一个复制到临时寄存器的赋值语句，并覆写后续指令中对于这个变量的引用（将引用改成复制的临时变量）。赋值到临时寄存器并覆写后续指令的操作和构建 SSA （mem2reg）的过程非常类型，意味着这个过程仍然需要遍历支配树。

#### 交换问题

在源程序中所有 Phi 函数都时同时执行的，但是插入的赋值语句不是，具体来讲就是一些 Phi 函数的参数可能被其他 Phi 函数所定义。如果不对插入的赋值语句进行处理分析，则插入的赋值语句可能导致错误。

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225082947040.png" alt="image-20221225082947040" style="zoom: 50%;" />

一个简单的解决方案是对每个值都引入一个临时变量。但是这不是一个理想的方式，因为它潜在的将赋值操作翻倍了。编译器需要插入最少的赋值操作。

在某种意义上，选择如何为 Phi 函数插入副本操作以及何时将副本插入到临时文件是一个调度问题。复制操作有两个参数，即复制的源和复制的目的地。我们希望插入一组受到如下限制的 Phi 函数的副本：调度一个复制c，必须首先调度 c 的目标作为其源的所有其他复制操作。

解决这个问题的另一个方法是将一组复制操作交互建模成为一个图，节点表示复制操作，边表示由一个复制操作定义并在另一个复制操作中使用的名称。如果图是无环的，那么复制操作的的调度可以通过简单的拓扑图来实现。

论文中的算法进行了三次遍历。第一次遍历中，编译器会计算其他 Phi 函数使用一个名称的次数，将一个 <src, dest> 的组合保存到一个copy_list中。第二次遍历中，它构建了一个在其他 Phi 函数中没有使用的名称的 workList。第三次遍历workList，为工作列表中的每个元素安排复制操作。每次编译器插入一个复制操作时，都可以将其源加入到工作列表中。

#### 具体代码

**核心思路是打破环的依赖**

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225082657785.png" alt="image-20221225082657785" style="zoom:50%;" />

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225082715772.png" alt="image-20221225082715772" style="zoom:50%;" />

## 九、代码优化

### 常数传播

常数传播使用的是 LLVM 的 ConstInteger 类，这个类表示常数。

当源程序进行常数的定义或者常数数组的定义的时候，在符号表中就会标记这个变量or 数组对应的是常数，同时使用 ConstInteger or ConstInteger组成的数组来初始化变量 or 常数数组。

当后续的程序中，使用标记为 ConstInteger 的变量 or 从数组中取值时，语法分析阶段就可以检测并将对应的常数取出来，进行赋值。

### mem2reg

**参考博客或资料：**软院 LLVM 教程、《编译器设计》（**未参考任何编译大赛或者往届同学代码**）

**最后结果通过辅助测试库，证明生成 Phi 正确**

在 mem2reg 中，LLVM 会识别出局部变量的 `alloca` 指令，将局部变量改为虚拟寄存器的 SSA 形式，将变量的 `store/load` 修改为虚拟寄存器之间的 def-use/use-def 关系，并在适当的地方加入 phi 指令和进行变量的重命名。

#### 前置概念

- **定义：**对变量进行初始化、赋值等改变变量值的行为
- **使用：**在语句/指令中将变量的值作为参数的行为
- **控制流图（CFG）：**控制流图是程序中基本块之间的转移图，图中的一个节点表示一个基本块，如果基本块最后是跳转指令，则有向边代表基本块之间的跳转关系。控制流图只有一个入口基本块表示程序的开始，但是程序可能有多个叶子基本块，表示程序的结束。
- **支配（Dom）：**对于控制流图中的节点两个基本块 X 和 Y，X 配 Y 当且仅当所有从入口节点到 Y 的路径中都包含 X。显然每个基本块都支配自身。
- **严格支配：**当且仅当 $X\in DOM(Y)-\{Y\}$ 时，X 严格支配 Y。
- **直接支配者（IDOM）：**基本块 X 的直接支配者是离 X 最近的严格支配 X 的节点（**具体描述可以参考软院教程**），流图的入口没有直接支配节点。
- **支配树：**流图的支配树包含流图中的每个节点，树的边用一种简单方式编码了 IDom 集合。如果 m 为 IDom(n)，那么支配树中有一条从 n 指向 m 的边。支配树简洁的编码了每个节点的 IDom 信息及其完整的 Dom 集合。给出支配树的一个结点 n，IDom(n) 只是其在树上的父节点。Dom(n) 中的各个结点，就是从支配树的根节点到 n 之间的路径上的哪些结点（含根结点和 n）
- **支配边界（Dominance Frontier，DF）：**对于结点 n 和结点 m，如果 n 支配 m 的一个前驱，但是 n 不严格支配 m。则将相对于结点 n 具有这种性质的结点 m 的集合称为 n 的支配边界。

#### 支配树分析

支配树的分析包括求解前驱和后继基本块、获得支配集合Dom，获得直接支配者 IDom，获得支配边界 DF。

**求解前驱和后继基本块获得控制流图**

这个阶段中需要遍历基本块中的指令，根据跳转指令的目的地确定控制流图。

**求解Dom集合**

求解 Dom 集合基于《编译器设计》中的描述

![image-20221225092348670](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225092348670.png)

![image-20221225092402725](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225092402725.png)

**求解 IDom 集合**

求解直接支配者我没有找到任何说明的伪代码，一开始我想从定义出发遍历所有的基本块Y，然后遍历它的所有支配者X，如果Y的所有其他支配者Z的严格支配者集合不含X，则X直接支配Y。

但是这是一个时间复杂度 $O(n^3)$ 的算法，因此需要对它进行优化，另一种求解方式基于如下事实：A的直接支配者是支配A的块的集合中dom集最小的。

*一个基本块只有1个直接支配者吗？可能有多个吗？*——只能有一个。

*会不会有多个A的支配者dom集合大小相同呢？*——不可能，A的支配者之间必然一个支配另一个。

**求解 DF**

求解 DF 我使用了《Static Single Assignment Book》中的算法描述：

![image-20221225093146196](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225093146196.png)

#### 插入 Phi 函数

插入 Phi 函数我使用了《Static Single Assignment Book》中的算法描述：

![image-20221225093340271](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225093340271.png)

#### 重命名

变量重命名我参考了《Static Single Assignment Book》中的算法描述，但是加入了一些自己的理解：

![image-20221225093450610](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225093450610.png)

![image-20221225093515268](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225093515268.png)

### 活跃变量分析

活跃变量分析是消 Phi 和进行后续图着色必不可少的一项工作。我主要使用的方法是课内介绍的活跃变量分析的方法。即首先求出每个基本块内部的 Use 和 Def 集合，使用公式 $In = Use\cup(Out-Def)$ 迭代到不动点。

在中端阶段的活跃变量分析只需要分析到基本块级别即可，因为这里的活跃变量分析主要服务于消 Phi，不需要分析到指令级别。

后端的活跃变量分析需要分析到指令级别（主要服务于图着色寄存器分配），由于寄存器分配我没有区分全局变量和局部变量，因此，需要将活跃变量分析到指令级别，将所有寄存器都看成全局变量，从而建立更大的冲突图。

### 图着色寄存器分配方法

当我完成 mem2reg 和消 Phi 之后，以为同学告诉我，《编译器设计》中有一个针对 Phi 函数的图着色分配方式，具体方法是将 Phi 函数的源操作数和目标操作数都看成一个变量——用活动范围来表示。随后针对活动范围进行分配。但是由于我得知这个消息在最后一周的周一（此时竞速前50名已经确定，只剩最后一周的补交时间，而且还有好几场考试），时间不够了，所以我果断放弃，使用书上介绍的图着色方法：

- 根据活跃变量分析确定冲突图，每次找到第一个度小于 K 的结点并将其冲突图中移除。 

- 如果没有度小于 K 的结点，则将其中一个度大于等于 K 的结点标记为溢出（随机标记，一种更好的方法是确定每个结点的溢出代价，选择溢出代价最小的结点）
- 按照结点移除倒序给每个结点着色。

**但是遗憾的是我还不知道如何进行迭代图着色分析：**

![image-20221225100658978](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225100658978.png)

一个例子就是这样的冲突图，我还是无法分配到左边的结果。

### 乘除优化

**乘法：**

- 若乘数的绝对值为2的幂，可用一条移位指令
- 若乘数的绝对值为2的幂+1，可用一条移位指令和加法指令
- 若乘数的绝对值为2的幂-1，可用一条移位指令和减法指令
- 若乘数为负数，将结果取反即可

**除法：复现论文Divison by Invariant Integers using Multiplication**

![image-20221225221600599](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225221600599.png)

![image-20221225221606021](D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225221606021.png)

## 跋、总结

今天12/25号，编译实验的最后一天，我在昨天晚上11点多完成编译的图着色寄存器分配，作为唯一的优化，通过了竞速排序，基本上所有测试点都有一定的提升，只是显著程度的不同。

随后进行文档的编写。好在之前编写了一部分，并且实时将自己的思考记录下来，在写这篇文档时，我又打开了之前记录的各个阶段的思考。几乎每个文件都有1w字左右，我从中选取了一些重要的部分，并将内容串联起来，这篇文档就是这么诞生的。

现在编译实验要结束了，回望这一个学期以来的编译实验之路，我想到了无数个日日夜夜、抓耳挠腮、几近崩溃，尤其是面对日益紧迫的 DDL。但是现在我更多的是平静，我平常使用 Pomodoro Logger 记录每项工作的投入时间，经过一个学期的努力，《编译》我投入了**636.27小时**，**从7月13日第一次打开《编译器设计》学习 DFA 和 NFA开始，到12月25日完成图着色**，一共记录了**876次专注（一次专注时间<=45min）**。上个学期我在 OS 和 OO 投入时间最长，分别是**424.41小时和217.84小时**。看着这些数据，我很欣慰，我觉得我这个学期的付出是有收获的。

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225101822494.png" alt="image-20221225101822494" style="zoom:50%;" />

<img src="D:\zhang_kg\BUAA_undergraduate\TyporaImage\image-20221225101746488.png" alt="image-20221225101746488" style="zoom: 50%;" />

如果要给后面的同学一些建议，我想同学们最好在假期就开始学习编译的相关理论知识，学习《龙书》《虎书》《鲸书》《编译器设计》，徜徉在先辈知识的海洋中，谦卑的思考、实践。开始阅读代码，尤其是优秀代码，不断提高自己的编程能力。==<font color = red>**祝你们成功！**</font>==

北航编译实验的旅程到这里就结束了，但是**我的编译旅程不会就此结束**，我希望能够继续进行编译的优化，并以此继续锻炼我的编程能力。**我多次将编译实验比作“造个玩具”，现在也是这样，它就是一个“大模拟”，我相信我能通过不断重构写出越来越好的代码。**

# 想，都是问题；做，才是答案

## 十一、附录

### 语法分析-所有FIRST和FOLLOW集合

**CompUnit**

**FIRST**

```c
FIRST(Decl) && FIRST(FuncDef) && FIRST(MainFuncDef)
'int', 'const', 'void'
```

**FOLLOW**

```c
eof
```

**Decl**

**FIRST**

```c
FIRST(ConstDecl) && FIRST(VarDecl)
'int', 'const'
```

**FOLLOW**

```c
FIRST(Decl), FIRST(FuncDef), FIRST(MainFuncDef), FOLLOW(BlockItem)
'int', 'const', 'void', '}'
```

**ConstDecl**

**FIRST**

```c
'const'
```

**FOLLOW**

```c
FOLLOW(Decl)
'int', 'const', 'void', '}'
```

**BType**

**FIRST**

```c
'int'
```

**FOLLOW**

```c
FIRST(ConstDef), FIRST(VarDef), FIRST(Ident)
',', ';', Ident
```

**ConstDef**

**FIRST**

```c
Ident
```

**FOLLOW**

```c
',', ';'
```

**ConstInitVal**

**FIRST**

```c
FIRST(ConstExp), '{'
'+', '-', '!', ident, '(', IntConst, '{'
```

**FOLLOW**

```c
FOLLOW(ConstDef), ',', '}'
',', ';', '}'
```

**VarDecl**

**FIRST**

```c
FIRST(BType)
'int'
```

**FOLLOW**

```c
FOLLOW(Decl)
'int', 'const', 'void', '}'
```

**VarDef**

**FIRST**

```c
文法中: VarDef -> Ident {'[' ConstExp ']'}
				|Ident {'[' ConstExp ']'} '=' ConstInitVal
FIRST
Ident
```

**FOLLOW**

```c
',', ';'
```

**InitVal**

**FIRST**

```c
FIRST(Exp), '{'
'+', '-', '!', ident, '(', IntConst, '{'
```

**FOLLOW**

```c
FOLLOW(VarDef), ',', '}'
',', ';', '}'
```

**FuncDef**

**FIRST**

```c
FIRST(FuncType)
'void', 'int'
```

**FOLLOW**

```c
FIRST(MainFuncDef)
'int'
```

**MainFuncDef**

**FIRST**

```c
'int'c
```

**FOLLOW**

```c
FOLLOW(CompUnit)
eof
```

**FuncType**

**FIRST**

```c
'void', 'int'
```

**FOLLOW**

```c
Ident
```

**FuncFParams**

**FIRST**

```c
文法中：FuncFParams → FuncFParam { ',' FuncFParam }
FIRST(FuncFParam)
int 
```

**FOLLOW**

```c
')'
```

**FuncFParam**

**FIRST**

```c
FIRST(BType)
-- int 
```

**FOLLOW**

```c
FOLLOW(FuncFParams), ','
')', ','
```

**Block**

**FIRST**

```c
'{'
```

**FOLLOW**

```c
FOLLOW(FuncDef), FOLLOW(MainFuncDef), FOLLOW(Stmt)
'int', eof, '}', 'else'
```

**BlockItem**

**FIRST**

```c
FIRST(Decl), FIRST(Stmt)
Ident, '+', '-', '!', '(', IntConst, ';', '{', 'if', 'while', 'break', 'return', 'printf', 'int', 'const', 'continue'
```

**FOLLOW**

```c
'}'
```

**Stmt**

**FIRST**

```c
FIRST(LVal){如果是LVal，则之后需要判断是否是getint}, FIRST(Exp), ';', FIRST(BLOCK), 'if', 'while', 'break', 'return', 'printf', 'continue'
Ident, '+', '-', '!', '(', IntConst, ';', '{', 'if', 'while', 'break', 'return', 'printf'
```

**FOLLOW**

```c
FOLLOW(BolckItem), 'else', FOLLOW(Stmt)
'}', 'else'
```

**Exp**

**FIRST**

```c
FIRST(AddExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(InitVal), ';', ')', ']', FOLLOW(FuncRParams)
',', ';', '}', ')', ']'
```

**Cond**

**FIRST**

```c
FIRST(LOrExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
')'
```

**LVal**

**FIRST**

```c
Ident
```

**FOLLOW**

```c
'=', FOLLOW(PrimaryExp)
'=', ')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']', '+', '-', '*', '/', '%'
```

**PrimaryExp**

**FIRST**

```c
'(', LVal, Number
'(', IntConst, Ident
```

**FOLLOW**

```c
FOLLOW(UnaryExp)
')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']', '+', '-', '*', '/', '%'
```

**Number**

**FIRST**

```c
IntConst
```

**FOLLOW**

```c
FOLLOW(PrimaryExp)
')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']', '+', '-', '*', '/', '%'
```

**UnaryExp**

**FIRST**

```c
FIRST(PrimaryExp), Ident, FIRST(UnaryOp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(UnaryExp), FOLLOW(MulExp), '*', '/', '%'
')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']', '+', '-', '*', '/', '%'
```

**UnaryOP**

**FIRST**

```c
'+', '-', '!'
```

**FOLLOW**

```c
FIRST(UnaryExp)
'+', '-', '!', ident, '(', IntConst
```

**FuncRParams**

**FIRST**

```c
FIRST(Exp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
')'
```

**MulExp**

**FIRST**

```c
FIRST(UnaryExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(AddExp), '+', '-'
')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']', '+', '-'
```

**AddExp**

**FIRST**

```c
AddExp → MulExp {('+' | '-') MulExp}
FIRST(MulExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(RelExp), '<', '>', '<=', '>=', FOLLOW(Exp), FOLLOW(ConstExp)
')', '||', '&&', '==', '!=', '<', '>', '<=', '>=', ',', ';', '}', ']'
```

**RelExp**

**FIRST**

```c
RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
FIRST(AddExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(EqExp), '==', '!='
')', '||', '&&', '==', '!='
```

**EqExp**

**FIRST**

```c
EqExp → RelExp {('==' | '!=') RelExp}
FIRST(RelExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(LAndExp), '&&'
')', '||', '&&'
```

**LAndExp**

**FIRST**

```c
LAndExp → EqExp {'&&' EqExp}
FIRST(EqExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(LOrExp), '||'
')', '||'
```

**LOrExp**

**FIRST**

```c
LOrExp → LAndExp {'||' LAndExp}
FIRST(LAndExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
FOLLOW(Cond)
')'
```

**ConstExp**

**FIRST**

```c
FIRST(AddExp)
'+', '-', '!', ident, '(', IntConst
```

**FOLLOW**

```c
']', FOLLOW(ConstInitVal)
',', ';', '}', ']'
```

### 错误处理-使用符号表进行错误检查

需要配合遍历语法树进行错误处理。遍历语法树应该按照从左到右的顺序进行遍历。

**为什么不把错误处理和语法分析一起做：**

1. 除IJK错误之外，其他错误类型并不影响我们进行语法分析，并且程序可以正确建立语法树、确定各个非终结符和终结符的行号。将错误处理和语法分析分开说明我们进入了新阶段、需要采用新方法
2. 语法分析阶段的代码已经写完，从面向对象的开闭原则来说，不应该在其上做增量开发了，而是应该对其进行合理封装
3. 语法分析阶段的代码逻辑较多，分支判断比较复杂。这个时候

下面将按照各个错误类型进行错误处理和输出说明

- a类：当遍历语法树遍历到**字符串**时，判断字符串中是否含有非法字符。如果含有，则应该将字符串的行号和类别a记录到错误数组中

- b类：函数名或者变量名在**当前作用域下**重复定义，报错行号为`Ident`所在行数。可能涉及到的非终结符为`ConstDef, VarDef, FuncDef, FuncFParam`

  - `ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal`：当检测到某个节点的类型为`ConstDef`时，探查其中的`Ident`，并且在符号表的当前Table中进行查找（只考虑Item；Table类型的不考虑；并且注意不要超过当前位置）。如果在当前层的Table中找到了同名的点，则报错
  - `VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal`：当检测到某个节点为`VarDef`时，探查其中的`Ident`，并在符号表中当前Table进行查找。如果找到同名的Item，则报错。
  - `FuncDef → FuncType Ident '(' [FuncFParams] ')' Block `：当检测到某个节点为`FuncDef`时，探查其中的`Ident`，并在符号表中当前Table进行查找。如果找到同名的Item，则报错。
  - `FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]`：这个文法的同级节点是什么东西呀，就是这个函数的符号表吗？检测的时候就是后面的形参不能和前面的形参重名，是这样吗？典型错误是`int f(int a, int a)`

- c类：使用了未定义的标识符，报错行号是`Ident`所在行数。可能涉及的非终结符是`LVal, UnaryExp`。**注意：**这里需要查找某个`Ident`是否在前面出现过，可能需要进行多级查找。

  **进行多级查找的步骤：**首先查找本scope内的Item，确定是否有`Ident`；如果本级没有，则查找更高一级的scope，这里不失一般性，需要在当前scope的`SymbolTable`中储存父节点的`SymbolTable`以便进行回溯查找，同时也要记录本scope对应的`SymbolTable`在上级`SymbolTable`中的数组下标号，以免出现查询到scope后的内容。常见错误例子是：

  ```c
  int f() {
      int a = 1;
      {
          b = 1;
      }
      int b = 1;
  }
  ```

  这里在确定第四行的b时，不能查找到第6行的b声明。

  - `LVal → Ident {'[' Exp ']'} `：当检测到某个节点为`LVal`时，按照多级查找的步骤对`Ident`进行多级查找。
  - `UnaryExp → Ident '(' [FuncRParams] ')'`：当检测到某个节点为`UnaryExp`时，首先需要判断其分支是否是`Ident`分支。如果是，则按照多级查找步骤对`Ident`进行多级查找。

- d类：函数调用语句中，参数个数与函数定义中的参数个数不匹配，报错行号是函数名调用语句的**函数名**所在行数。可能涉及的非终结符号是`UnaryExp`

  - `UnaryExp → Ident '(' [FuncRParams] ')'`：这条分支同样涉及到c类错误的检测，需要**注意检测顺序**。应该先检测该函数是否出现，即C类错误。如果找到之前声明过此函数，再判断函数参数个数是否一致。

- e类：函数调用语句中，参数类型与函数定义中的对应位置的参数类型不匹配，报错行号是函数名调用语句的**函数名**所在行数。可能涉及的非终结符号是`UnaryExp`

  - `UnaryExp → Ident '(' [FuncRParams] ')'`：这条分支同样涉及到c类和d类错误的检测，需要**注意检测顺序**。应该先检测C类错误；如果找到之前声明过此函数，再判断d类错误；如果参数数量一致，则再判断类型对应是否正确。判断类型的时候，需要按照上面的定义计算出函数声明语句和函数调用语句的**函数签名**之后进行字符串的比较。

- f类：无返回值的函数存在不匹配的return语句，报错行号为`return`所在的行号。可能涉及到的

  - `Stmt → 'return' [Exp] ';'`：需要检测函数内部所有return语句是否有返回值。**检测方法是**，如果当前是函数声明语句，即`FuncDef → FuncType Ident '(' [FuncFParams] ')' Block`，则分析Block和`FuncFParams`所在的Table中是否有return语句，并且return语句中是否有Exp。如果有`Exp`则报错。
  - 每当检测Stmt时，如果其生成return exp类型，则根据type判断

- g类：有返回值的函数缺少return语句，**考虑函数末尾是否存在return语句即可，无需考虑数据流**。报错行号为函数结尾的**大括号**所在行号

  - `FuncDef → FuncType Ident '(' [FuncFParams] ')' Block`：只需要考虑函数体内最后一条语句，判断是否是return语句，不用检查函数体内其他return语句是否有值

- 对于f和g类函数的说明：

  - g类错误只需要考虑函数体**最后一条**语句，且只判断有没有return语句，不需要考虑return语句是否有返回值；也不需要检查函数体内的其他return语句是否有值

  - f类错误需要检查`void`类型函数体内**每一条**return语句都没有值，遇到有值的return语句就报f类错误，有多少个就报出多少个f类型错误

  - 一个**有返回值**的函数最多有一个g类错误，不会有f类错误；一个**无返回值**的函数只有f类错误，且允许有多个f类错误

  - 为简化开发难度，**保证有返回值的函数**，Block的最后一句一定会显示的给出return语句，否则当作“无返回语句的错误”处理

- h类：不能改变常量的值，当`LVal`为常量时，不考虑对其修改。报错行号为`LVal`所在行号。

  - `Stmt → (LVal '=')('getint''('')'';' | Exp ';')`：当检测到`Stmt`时，判断子节点是否是`LVal`，如果是，则判断其是否是常量。如果是常量，则报错

- i，j，k类错误已经处理过，此时生成的语法树中不包含此类错误。

- l类：printf中格式字符与表达式个数不匹配，报错行号为`printf`所在行号。

  - `Stmt → 'printf''('FormatString{','Exp}')'';'`：当处理到`Stmt`时，考虑子节点是否是`printf`。如果是，则处理字符串时，记录字符串中格式字符的个数。并与`printf`语句中表达式个数进行匹配

- m类：在非循环块中使用`break`和`continue`语句，报错行号为`break`和`continue`所在行号。

  - 这里只有`Stmt → 'while' '(' Cond ')' Stmt`语句是循环块。所以只有检测到while时，继续解析子节点中的`Stmt`才会允许含有break或者continue。一个简单的方法是设置一个`isLoopStmt`，并且可以进行父子传递，这样保证内部包含or不包含break和continue。
