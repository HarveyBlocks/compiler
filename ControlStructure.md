```
if(exp1){
	exp2
}
exp3
```

```embeddedjs
temp_st_store = exp1
nif_goto temp_st_pop label_next_branch
exp2
goto label_end_branch
label_next_branch
label_end_branch
exp3
```

```
if(exp1){
	exp2
}else{
	exp3   
}
exp4
```

```embeddedjs
temp_st_store = exp1
nif_goto temp_st_pop label_next_branch
exp2
goto label_end_branch
label_next_branch
exp3
label_end_branch
exp4
```

```
if(exp1){
	exp2
}else if(exp3){
	exp4   
}
exp5
```

```
temp_st_store = exp1
nif_goto temp_st_pop label_next_branch_1
exp2
goto label_end_branch
label_next_branch_1
temp_st_store = exp3
nif_goto temp_st_pop label_next_branch_2
exp4
goto label_end_branch
label_next_branch_2
label_end_branch
exp5
```

// 局部变量 结束, 开始是在声明,没有呢

Table[]

end

capacity

end = Table.push(类型);

计算表达式/调用构造器 类型()

Assign id_end 值

Table.pop()

```
if(exp1){
	exp2
}else if(exp3){
	exp4   
}else{
    exp5
}
exp6
```

```
temp_st_store = exp1
nif_goto temp_st_pop label_next_branch_1
exp2
goto label_end_branch
label_next_branch_1
temp_st_store = exp3
nif_goto temp_st_pop label_next_branch_2
exp4
goto label_end_branch
label_next_branch_2
exp5
label_end_branch
exp6
```

1. 注册end_branch
2. 任何if分支结束都是goto end_branch
3. nif_goto的都是next_branch, 所以要先获得next_branch
4.   

