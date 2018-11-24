## period标签

### 完整写法
```
<period begin="" end="" include=""/>
```

### 说明
用于控制页面片段只能在指定时间内或者不在该时间段内才能被解析

### 属性说明
|  属性名称  |说明| 是否必须   | 允许值   | 默认值  |     
|  -  |  -  |  -  |  -  | - |
|  begin  | 开始时间   | 和end属性必须存在一个，可同时存在   |  string|long  |  | 
| end   | 结束时间   | 和start属性必须存在一个，可同时存在  |  string|long  |  |  
|  include  | 是否在时间段内 |  否  |  [true,false]  |true  |  

### 动态属性
是