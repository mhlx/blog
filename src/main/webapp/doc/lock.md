## lock标签

### 完整写法
```
<lock id=""/>

<lock id="" type="block">
  <locked>
    解开锁后渲染的内容
  </locked>
  <unlocked>
    没有解开锁时渲染的内容
  </unlocked>
</lock>
```

### 说明
用于控制页面必须在解锁后才能被访问，如果lock标签存在值为block的type属性，那么可以根据解锁情况分别渲染

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |     
|  -  |  -  |  -  |  -  | - |
|  id  | 锁ID   | 是   |  string|long  |  | 
| type   | 渲染类型，如果指定且值为block，那么可以根据解锁情况分别渲染   | 否  |  ['block']  |  |   

### 动态属性
否