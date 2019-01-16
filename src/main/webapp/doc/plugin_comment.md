## comment标签

### 完整写法
```
<comment id="" module="">
  <commented>
    用户评论指定模块后的渲染内容
  </locked>
  <uncommented>
    用户没有评论指定模块时的渲染内容
  </uncommented>
</lock>
```


### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |     
|  -  |  -  |  -  |  -  | - |
|  id  | 锁ID   | 否   |  int|  |  | 
| module   | 模块类型   | 否  |  ['userpage','article','news']  |  |   

**如果模块和ID都为空，那么只需要在任何地方有过评论即可，如果ID为空，那么只需要在某个模块下评论即可，如果都不为空，那么只有在指定的具体模块下评论才能渲染**

### 动态属性
是