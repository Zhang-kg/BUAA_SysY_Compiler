# 代码优化——mem2reg

1. mem2reg是在函数范围内进行的
2. 构造控制流图
3. 得到每个基本块的支配者、支配边界
4. 针对所有alloca变量（全局变量）计算其def的基本块集合
5. 根据算法插入phi函数
6. 重命名虚拟寄存器
7. 从SSA转换到其他形式（消除phi，好像可以中端也可以后端
   1. 将赋值操作放到“中间基本块”中
   2. 注意复制合并问题
   3. 注意swap问题

## 基本概念

- **def：**对变量的初始化、赋值等改变变量的行为
- **use：**在语句/指令中将变量的值作为参数的行为
- **控制流图（CFG）：**一个程序中所有基本块执行的可能流向图，图中每个节点代表一个基本块，有向边代表基本块间的跳转关系，CFG有一个入口基本块和一/多个出口基本块，分别对应程序的开始和终止
- **支配（dominate）：**对于 CFG 中的节点 $n1$ 和 $n_2$，$n_1$ 支配 $n_2$ 当且仅当所有从入口节点到 $n_2$ 的路径中都包含 $n_1$，即 $n_1$ 是从入口节点到 $n_2$ 的必经节点. 每个基本块都支配自身.
- **严格支配（strictly dominate）：** $n_1$ 严格支配 $n_2$ 当且仅当 $n_1$ 支配 $n_2$ 且 $n_1\neq n_2$
- **直接支配者（immediate dominator，idom）：**节点 $n$ 的直接支配者是离 $n$ 最近的严格支配 $n$ 的节点（标准定义是：[严格支配 $n$，且不严格支配任何严格支配 $n$ 的节点]的节点）. 入口节点以外的节点都有直接支配者. 节点之间的直接支配关系可以形成一棵支配树（dominator tree）.
- **支配边界（dominance frontier）：**节点 $n$ 的支配边界是 CFG 中刚好**不**被 $n$ 支配到的节点集合. 形式化一点的定义是：节点 $n$ 的支配边界 $DF(n) = \{x | n 支配 x 的前驱节点，n 不严格支配 x\}$.

## 构造控制流图

**先将支配树构造出来**

### 进行初始化工作

初始化工作包含：

- 初始化 label to basic block for a function 集合
- 初始化前驱、后继map
- 初始化支配集合 dom

### 前驱后继基本块

方法是遍历所有的基本块根据基本块最后一条跳转指令判断其后继基本块是哪两个or哪一个（因为跳转指令最多有两个结果，所以只能是2个基本块）。设置跳转前基本块的后继（predecessor）和跳转后基本块的前驱（successor）。

## 支配者

> 本部分算法参考《编译器设计》

## 消除phi函数

### 基本概念

**关键边：**关键边就是包含Phi函数的前驱块内有多个后继基本块，此时简单的将复制操作放到前驱块中，则可能会导致程序出现错误。对于这种基本块有多个后继，后继基本块存在phi函数的边称为关键边。

**基本思路：**

- 对于不包含关键边的块，可以直接将复制指令放到函数末尾。
- 对于包含关键边的块，需要进行关键边的拆分。
- 复制丢失问题
- 交换问题

```
testfile.txt
y = i
i = i + 1
llvm_ir

```

算法

```
REPLACE_PHI_NODES()
	perform live analysis
	For each variable v
		Stacks[v] <= emptystack()
	insert_copies(start)

insert_copies(block)
	pushed <= empty_set
	For all instructions i in block
		Replace all uses u with Stacks[u]
	
	schedule_copies(block)
	For each child c of block in the dominator tree
		insert_copies(c)
	For each name n in pushed
		pop(Stacks[n])


schedule_copies(block)
	/* Pass One: Initialize the data structures */
	copy_set <= empty_set
	For all successors s of block
		j <= whichPred(s, block)
		For each phi-function dest <= phi(...) in s
			src <= 

```



## 参考链接

**Use官方文档：**https://www.llvm.org/docs/ProgrammersManual.html#the-user-and-owned-use-classes-memory-layout