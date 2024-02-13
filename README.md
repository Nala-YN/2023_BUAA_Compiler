# 2023_BUAA_Compiler
2023北航编译技术课设（SysY2Mips）
将SysY类C语言翻译至Mips汇编语言，实现了词法分析、语法分析、中间代码生成、错误处理和目标代码生成等编译阶段，并支持SSA下的活跃变量分析、GVN、GCM、FunctionInline、死代码删除、窥孔优化、线性寄存器分配等中后端编译优化。竞速排名：2.
参考：
qsgg：<https://github.com/Thysrael/Pansy>
hyggge：<https://github.com/Hyggge/Petrichor>
